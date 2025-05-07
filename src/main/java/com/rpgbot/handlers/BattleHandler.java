package com.rpgbot.handlers;

import com.rpgbot.RpgBot;
import com.rpgbot.commands.Command;
import com.rpgbot.database.models.Character;
import com.rpgbot.database.models.Item;
import com.rpgbot.database.repositories.CharacterRepository;
import com.rpgbot.database.repositories.InventoryRepository;
import com.rpgbot.database.repositories.UserRepository;
import com.rpgbot.entities.Battle;
import com.rpgbot.entities.Monster;
import com.rpgbot.util.TextFormatter;

import java.util.List;

public class BattleHandler implements Command {
    private final RpgBot bot;
    private final long chatId;
    private final UserRepository userRepository;
    private final CharacterRepository characterRepository;
    private final InventoryRepository inventoryRepository;
    private final int messageId;
    private final Character character;
    private final Monster enemy;
    private final int battleStage;
    private Battle battle;

    public BattleHandler(RpgBot bot, long chatId, UserRepository userRepository,
                         CharacterRepository characterRepository,
                         InventoryRepository inventoryRepository,
                         int messageId, Character character,
                         Monster enemy, int battleStage) {
        this.bot = bot;
        this.chatId = chatId;
        this.userRepository = userRepository;
        this.characterRepository = characterRepository;
        this.inventoryRepository = inventoryRepository;
        this.messageId = messageId;
        this.character = character;
        this.enemy = enemy;
        this.battleStage = battleStage;
    }

    public void startBattle() {
        this.battle = new Battle(character, enemy);
        updateBattleScreen();
    }

    @Override
    public void setArgs(String[] args) {}

    @Override
    public void setMessageId(int messageId) {}

    @Override
    public void execute() {
        if (battle == null) startBattle();
        handleBattleAction();
    }

    private void updateBattleScreen() {
        String message = TextFormatter.formatBattleStatus(
                character, enemy,
                battle.isCharacterTurn(),
                battle.isBattleEnded()
        );

        InlineKeyboardMarkup keyboard = createBattleKeyboard();
        bot.editMessage(chatId, messageId, message, keyboard);
    }

    private InlineKeyboardMarkup createBattleKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        if (battle.isCharacterTurn() && !battle.isBattleEnded()) {
            // Кнопки атаки и заклинаний
            List<InlineKeyboardButton> row1 = new ArrayList<>();
            row1.add(InlineKeyboardButton.builder()
                    .text("⚔️ Атаковать")
                    .callbackData("/battle attack")
                    .build());
            row1.add(InlineKeyboardButton.builder()
                    .text("🔮 Заклинание")
                    .callbackData("/battle spell")
                    .build());
            keyboard.add(row1);

            // Кнопки бонусных действий
            List<InlineKeyboardButton> row2 = new ArrayList<>();
            List<Item> potions = inventoryRepository.findByCharacterAndType(character, "potion");
            if (!potions.isEmpty()) {
                row2.add(InlineKeyboardButton.builder()
                        .text("🧪 Использовать зелье (" + potions.size() + ")")
                        .callbackData("/battle potion")
                        .build());
            }

            List<Item> weapons = inventoryRepository.findByCharacterAndType(character, "weapon");
            if (weapons.size() > 1) {
                row2.add(InlineKeyboardButton.builder()
                        .text("🔫 Сменить оружие")
                        .callbackData("/battle weapon")
                        .build());
            }

            if (!row2.isEmpty()) {
                keyboard.add(row2);
            }

            // Кнопка завершения хода
            List<InlineKeyboardButton> row3 = new ArrayList<>();
            row3.add(InlineKeyboardButton.builder()
                    .text("⏹ Закончить ход")
                    .callbackData("/battle end")
                    .build());
            keyboard.add(row3);
        }

        return bot.createKeyboard(keyboard);
    }

    private void handleBattleAction() {
        if (battle.isBattleEnded()) {
            if (battle.isCharacterAlive()) {
                handleVictory();
            } else {
                handleDefeat();
            }
            return;
        }

        if (battle.isCharacterTurn()) {
            // Обработка действий игрока
            // (реализация в зависимости от callback данных)
        } else {
            // Ход врага
            battle.enemyAttack();
            updateBattleScreen();

            if (!battle.isCharacterAlive()) {
                handleDefeat();
            }
        }
    }

    private void handleVictory() {
        int expGain = enemy.getExpReward();
        int goldGain = enemy.getGoldReward();

        character.gainExperience(expGain);
        character.setGold(character.getGold() + goldGain);
        characterRepository.save(character);

        String message = String.format(
                "🎉 Вы победили %s!\nПолучено: %d опыта и %d золота",
                enemy.getName(), expGain, goldGain
        );

        if (character.getExperience() >= character.getExpToNextLevel()) {
            character.levelUp();
            message += "\n\n✨ Уровень повышен! Теперь вы " + character.getLevel() + " уровня";
        }

        bot.sendMessage(chatId, message);
    }

    private void handleDefeat() {
        character.setCurrentHealth(1); // Оставляем 1 HP
        characterRepository.save(character);
        bot.sendMessage(chatId, "💀 Вы проиграли... Ваше здоровье восстановлено до 1 HP");
    }
}