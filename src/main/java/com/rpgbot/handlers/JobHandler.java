package com.rpgbot.handlers;

import com.rpgbot.RpgBot;
import com.rpgbot.commands.Command;
import com.rpgbot.database.models.Character;
import com.rpgbot.database.models.Job;
import com.rpgbot.database.repositories.CharacterRepository;
import com.rpgbot.database.repositories.JobRepository;
import com.rpgbot.database.repositories.UserRepository;
import com.rpgbot.entities.Monster;
import com.rpgbot.util.RandomUtil;
import com.rpgbot.util.TextFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JobHandler implements Command {
    private final RpgBot bot;
    private final long chatId;
    private final UserRepository userRepository;
    private final CharacterRepository characterRepository;
    private final JobRepository jobRepository;
    private String[] args;
    private int messageId;

    public JobHandler(RpgBot bot, long chatId,
                      UserRepository userRepository,
                      CharacterRepository characterRepository,
                      JobRepository jobRepository) {
        this.bot = bot;
        this.chatId = chatId;
        this.userRepository = userRepository;
        this.characterRepository = characterRepository;
        this.jobRepository = jobRepository;
    }

    @Override
    public void setArgs(String[] args) {
        this.args = args;
    }

    @Override
    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    @Override
    public void execute() {
        if (args == null || args.length == 0) {
            showAvailableJobs();
            return;
        }

        switch (args[0]) {
            case "start": startJob(); break;
            case "complete": completeJob(); break;
            case "battle": handleBattle(); break;
            default: showAvailableJobs();
        }
    }

    private void showAvailableJobs() {
        List<Job> jobs = jobRepository.findAvailableForLevel(1); // Начальный уровень

        if (jobs.isEmpty()) {
            bot.sendMessage(chatId, "Нет доступных заданий");
            return;
        }

        String message = "Доступные задания:";
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (Job job : jobs) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(InlineKeyboardButton.builder()
                    .text(job.getTitle())
                    .callbackData("/job start " + job.getId())
                    .build());
            keyboard.add(row);
        }

        bot.sendMessageWithKeyboard(chatId, message, bot.createKeyboard(keyboard));
    }

    private void startJob() {
        if (args.length < 2) return;

        long jobId = Long.parseLong(args[1]);
        Optional<Job> jobOpt = jobRepository.findById(jobId);

        if (jobOpt.isEmpty()) {
            bot.sendMessage(chatId, "Задание не найдено");
            return;
        }

        Job job = jobOpt.get();
        Character character = getCurrentCharacter();

        switch (job.getTitle()) {
            case "Убийство гоблинов в лесу":
                handleGoblinHunt(character, job);
                break;
            case "Сбор полезных трав":
                handleHerbGathering(character, job);
                break;
            case "Поход в подземелье Охара":
                startDungeonCrawl(character, job);
                break;
            default:
                bot.sendMessage(chatId, "Неизвестный тип задания");
        }
    }

    private void handleGoblinHunt(Character character, Job job) {
        int hpLoss = RandomUtil.randomInt(30, 50);
        character.setCurrentHealth(Math.max(1, character.getCurrentHealth() - hpLoss));
        character.setGold(character.getGold() + 10);
        characterRepository.save(character);

        String message = String.format(
                "Вы потеряли %d HP в схватке с гоблинами\n" +
                        "Получено: 10 золота\n" +
                        "Текущее здоровье: %d/%d",
                hpLoss,
                character.getCurrentHealth(),
                character.getMaxHealth()
        );

        bot.sendMessage(chatId, message);
    }

    private void handleHerbGathering(Character character, Job job) {
        if (character.getLastHerbGathering() != null &&
                character.getLastHerbGathering().isAfter(java.time.LocalDateTime.now().minusDays(1))) {
            bot.sendMessage(chatId, "Вы уже собирали травы сегодня");
            return;
        }

        character.setGold(character.getGold() + 3);
        character.setLastHerbGathering(java.time.LocalDateTime.now());
        characterRepository.save(character);

        bot.sendMessage(chatId, "Вы собрали травы и получили 3 золота");
    }

    private void startDungeonCrawl(Character character, Job job) {
        String message = "Вы входите в подземелье Охара...\n" +
                "Впереди 3 испытания:\n" +
                "1. Бой с гоблином\n" +
                "2. Бой с гигантским червем\n" +
                "3. Бой с боссом\n\n" +
                "Подготовьтесь!";

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("Начать первый бой")
                .callbackData("/job battle 1")
                .build());
        keyboard.add(row);

        bot.sendMessageWithKeyboard(chatId, message, bot.createKeyboard(keyboard));
    }

    private void handleBattle() {
        if (args.length < 2) return;

        int stage = Integer.parseInt(args[1]);
        Character character = getCurrentCharacter();
        Monster enemy = createEnemyForStage(stage);

        BattleHandler battleHandler = new BattleHandler(
                bot, chatId, userRepository, characterRepository,
                null, messageId, character, enemy, stage
        );
        battleHandler.startBattle();
    }

    private Monster createEnemyForStage(int stage) {
        switch (stage) {
            case 1:
                return new Monster("Гоблин", 20, 3, 7);
            case 2:
                return new Monster("Гигантский червь", 30, 2, 10);
            case 3:
                return createBoss();
            default:
                return new Monster("Слайм", 10, 1, 3);
        }
    }

    private Monster createBoss() {
        int bossType = RandomUtil.randomInt(1, 3);
        switch (bossType) {
            case 1: return new Monster("Скелет Маршал", 60, 5, 15);
            case 2: return new Monster("Молодой Вармлинг", 50, 4, 20, "fire");
            default: return new Monster("Волчий король", 70, 3, 12, "ice");
        }
    }

    private void completeJob() {
        Character character = getCurrentCharacter();
        int goldReward = RandomUtil.randomInt(1, 50);
        character.setGold(character.getGold() + goldReward);

        String message = "Вы успешно завершили подземелье!\n" +
                "Получено: " + goldReward + " золота";

        // Шанс на предмет
        if (RandomUtil.randomInt(1, 100) <= 30) {
            Item item = ItemGenerator.generateItem(1); // Редкий
            // inventoryRepository.addItem(character, item);
            message += "\nДополнительно получен: " + item.getName();
        }

        characterRepository.save(character);
        bot.sendMessage(chatId, message);
    }

    private Character getCurrentCharacter() {
        return characterRepository.findByUser(
                userRepository.findByChatId(chatId).orElseThrow()
        ).orElseThrow();
    }
}