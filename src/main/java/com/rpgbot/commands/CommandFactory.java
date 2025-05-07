package com.rpgbot.commands;

import com.rpgbot.bot.RpgBot;
import com.rpgbot.handlers.*;
import com.rpgbot.database.repositories.*;
import com.rpgbot.entities.Monster;

public class CommandFactory {
    private final RpgBot bot;
    private final UserRepository userRepository;
    private final CharacterRepository characterRepository;
    private final InventoryRepository inventoryRepository;
    private final JobRepository jobRepository;
    private final AuctionRepository auctionRepository;
    private final DuelRepository duelRepository;

    public CommandFactory(RpgBot bot,
                          UserRepository userRepository,
                          CharacterRepository characterRepository,
                          InventoryRepository inventoryRepository,
                          JobRepository jobRepository,
                          AuctionRepository auctionRepository,
                          DuelRepository duelRepository) {
        this.bot = bot;
        this.userRepository = userRepository;
        this.characterRepository = characterRepository;
        this.inventoryRepository = inventoryRepository;
        this.jobRepository = jobRepository;
        this.auctionRepository = auctionRepository;
        this.duelRepository = duelRepository;
    }

    public Command createFromMessage(String messageText, long chatId) {
        String[] parts = messageText.split(" ", 2);
        String command = parts[0].toLowerCase();
        String[] args = parts.length > 1 ? parts[1].split(" ") : new String[0];

        switch (command) {
            case "/start":
            case "/character":
                return new CharacterHandler(bot, chatId, userRepository, characterRepository);
            case "/inventory":
                return new InventoryHandler(bot, chatId, userRepository, characterRepository, inventoryRepository);
            case "/job":
                return new JobHandler(bot, chatId, userRepository, characterRepository, jobRepository);
            case "/market":
                return new MarketHandler(bot, chatId, userRepository, characterRepository, inventoryRepository, auctionRepository);
            case "/duel":
                return new DuelHandler(bot, chatId, userRepository, characterRepository, duelRepository);
            default:
                return new UnknownCommandHandler(bot, chatId);
        }
    }

    public Command createFromCallback(String callbackData, long chatId) {
        String[] parts = callbackData.split(" ");
        String command = parts[0].toLowerCase();
        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);

        switch (command) {
            case "/character":
                return createCharacterHandler(chatId, args);
            case "/inventory":
                return new InventoryHandler(bot, chatId, userRepository, characterRepository, inventoryRepository);
            case "/job":
                return new JobHandler(bot, chatId, userRepository, characterRepository, jobRepository);
            case "/market":
                return new MarketHandler(bot, chatId, userRepository, characterRepository, inventoryRepository, auctionRepository);
            case "/duel":
                return new DuelHandler(bot, chatId, userRepository, characterRepository, duelRepository);
            case "/battle":
                return createBattleHandler(chatId, args);
            default:
                return new UnknownCommandHandler(bot, chatId);
        }
    }

    private Command createCharacterHandler(long chatId, String[] args) {
        CharacterHandler handler = new CharacterHandler(bot, chatId, userRepository, characterRepository);
        handler.setArgs(args);
        return handler;
    }

    private Command createBattleHandler(long chatId, String[] args) {
        // Получаем текущего персонажа
        Character character = characterRepository.findByUser(
                userRepository.findByChatId(chatId).orElseThrow()
        ).orElseThrow();

        // Создаем врага в зависимости от стадии боя
        int battleStage = args.length > 1 ? Integer.parseInt(args[1]) : 1;
        Monster enemy = createEnemyForStage(battleStage);

        return new BattleHandler(
                bot, chatId, userRepository, characterRepository,
                inventoryRepository, 0, character, enemy, battleStage
        );
    }

    private Monster createEnemyForStage(int stage) {
        // Реализация создания врага (аналогично JobHandler)
        switch (stage) {
            case 1: return new Monster("Гоблин", 20, 3, 7);
            case 2: return new Monster("Гигантский червь", 30, 2, 10);
            case 3: return createBoss();
            default: return new Monster("Слайм", 10, 1, 3);
        }
    }

    private Monster createBoss() {
        int bossType = new java.util.Random().nextInt(3) + 1;
        switch (bossType) {
            case 1: return new Monster("Скелет Маршал", 60, 5, 15);
            case 2: return new Monster("Молодой Вармлинг", 50, 4, 20, "fire");
            default: return new Monster("Волчий король", 70, 3, 12, "ice");
        }
    }
}