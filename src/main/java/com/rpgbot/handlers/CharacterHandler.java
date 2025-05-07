package com.rpgbot.handlers;

import com.rpgbot.RpgBot;
import com.rpgbot.commands.Command;
import com.rpgbot.database.models.Character;
import com.rpgbot.database.models.User;
import com.rpgbot.database.repositories.CharacterRepository;
import com.rpgbot.database.repositories.UserRepository;
import com.rpgbot.entities.Stats;
import com.rpgbot.util.TextFormatter;

import java.util.ArrayList;
import java.util.List;

public class CharacterHandler implements Command {
    private final RpgBot bot;
    private final long chatId;
    private final UserRepository userRepository;
    private final CharacterRepository characterRepository;
    private String[] args;
    private int messageId;

    public CharacterHandler(RpgBot bot, long chatId,
                            UserRepository userRepository,
                            CharacterRepository characterRepository) {
        this.bot = bot;
        this.chatId = chatId;
        this.userRepository = userRepository;
        this.characterRepository = characterRepository;
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
            showMainMenu();
        } else {
            switch (args[0]) {
                case "create": handleCharacterCreation(); break;
                case "stats": showCharacterStats(); break;
                case "distribute": distributeStats(); break;
                default: showMainMenu();
            }
        }
    }

    private void showMainMenu() {
        User user = userRepository.findByChatId(chatId).orElseGet(() -> {
            User newUser = new User();
            newUser.setChatId(chatId);
            userRepository.save(newUser);
            return newUser;
        });

        characterRepository.findByUser(user).ifPresentOrElse(
                this::showCharacterInfo,
                this::startCharacterCreation
        );
    }

    private void startCharacterCreation() {
        bot.sendMessage(chatId,
                "Добро пожаловать в королевство Традена!\n" +
                        "Как зовут вашего персонажа?");
    }

    private void showCharacterInfo(Character character) {
        String message = TextFormatter.formatCharacterInfo(character);
        InlineKeyboardMarkup keyboard = createCharacterMenu();
        bot.sendMessageWithKeyboard(chatId, message, keyboard);
    }

    private InlineKeyboardMarkup createCharacterMenu() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder()
                .text("📊 Характеристики")
                .callbackData("/character stats")
                .build());
        row1.add(InlineKeyboardButton.builder()
                .text("🎒 Инвентарь")
                .callbackData("/inventory")
                .build());

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(InlineKeyboardButton.builder()
                .text("💼 Задания")
                .callbackData("/job")
                .build());
        row2.add(InlineKeyboardButton.builder()
                .text("⚔️ Дуэли")
                .callbackData("/duel")
                .build());

        keyboard.add(row1);
        keyboard.add(row2);

        return bot.createKeyboard(keyboard);
    }

    private void handleCharacterCreation() {
        if (args.length == 1) {
            bot.sendMessage(chatId, "Введите имя персонажа:");
            return;
        }

        User user = userRepository.findByChatId(chatId).orElseThrow();

        if (args.length == 2) {
            // Сохранение имени
            Character character = new Character();
            character.setName(args[1]);
            character.setUser(user);
            characterRepository.save(character);

            showClassSelection();
        } else if (args.length == 3) {
            // Сохранение класса
            Character character = characterRepository.findByUser(user).orElseThrow();
            character.setClassName(args[2]);
            character.setStats(new Stats());
            character.setStatPoints(4);
            characterRepository.save(character);

            distributeStats();
        }
    }

    private void showClassSelection() {
        String message = "Выберите класс персонажа:";
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder()
                .text("🛡️ Воин")
                .callbackData("/character create Warrior")
                .build());
        row1.add(InlineKeyboardButton.builder()
                .text("🏹 Лучник")
                .callbackData("/character create Archer")
                .build());

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(InlineKeyboardButton.builder()
                .text("🔮 Маг")
                .callbackData("/character create Mage")
                .build());
        row2.add(InlineKeyboardButton.builder()
                .text("☠️ Культист")
                .callbackData("/character create Cultist")
                .build());
        row2.add(InlineKeyboardButton.builder()
                .text("🌀 Хаотический маг")
                .callbackData("/character create ChaosMage")
                .build());

        keyboard.add(row1);
        keyboard.add(row2);

        bot.sendMessageWithKeyboard(chatId, message, bot.createKeyboard(keyboard));
    }

    private void distributeStats() {
        Character character = characterRepository.findByUser(
                userRepository.findByChatId(chatId).orElseThrow()
        ).orElseThrow();

        if (character.getStatPoints() <= 0) {
            completeCharacterCreation(character);
            return;
        }

        String message = buildStatsDistributionMessage(character);
        InlineKeyboardMarkup keyboard = createStatsKeyboard(character);

        if (messageId == 0) {
            bot.sendMessageWithKeyboard(chatId, message, keyboard);
        } else {
            bot.editMessage(chatId, messageId, message, keyboard);
        }
    }

    private String buildStatsDistributionMessage(Character character) {
        Stats stats = character.getStats();
        return String.format(
                "Распределите %d очков характеристик:\n\n" +
                        "💪 Сила: %d\n" +
                        "🏃 Ловкость: %d\n" +
                        "❤️ Выносливость: %d\n" +
                        "📚 Интеллект: %d\n" +
                        "🔮 Мудрость: %d",
                character.getStatPoints(),
                stats.getStrength(),
                stats.getAgility(),
                stats.getEndurance(),
                stats.getIntelligence(),
                stats.getWisdom()
        );
    }

    private InlineKeyboardMarkup createStatsKeyboard(Character character) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        // Кнопки увеличения характеристик
        String[] stats = {"strength", "agility", "endurance", "intelligence", "wisdom"};
        String[] emojis = {"💪", "🏃", "❤️", "📚", "🔮"};

        for (int i = 0; i < stats.length; i++) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(InlineKeyboardButton.builder()
                    .text(emojis[i] + " +1")
                    .callbackData("/character distribute " + stats[i])
                    .build());
            keyboard.add(row);
        }

        // Кнопка завершения
        if (character.getStatPoints() < 4) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(InlineKeyboardButton.builder()
                    .text("✅ Завершить")
                    .callbackData("/character complete")
                    .build());
            keyboard.add(row);
        }

        return bot.createKeyboard(keyboard);
    }

    private void completeCharacterCreation(Character character) {
        character.calculateDerivedStats();
        characterRepository.save(character);
        bot.sendMessage(chatId,
                "Отлично, " + character.getName() + "! Теперь вы готовы к приключениям!\n" +
                        "Используйте меню для взаимодействия с миром.");
    }

    private void showCharacterStats() {
        Character character = characterRepository.findByUser(
                userRepository.findByChatId(chatId).orElseThrow()
        ).orElseThrow();

        bot.sendMessage(chatId, TextFormatter.formatCharacterStats(character));
    }
}