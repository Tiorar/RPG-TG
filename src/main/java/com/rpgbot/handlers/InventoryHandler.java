package com.rpgbot.handlers;

import com.rpgbot.RpgBot;
import com.rpgbot.commands.Command;
import com.rpgbot.database.models.Character;
import com.rpgbot.database.models.Item;
import com.rpgbot.database.repositories.CharacterRepository;
import com.rpgbot.database.repositories.InventoryRepository;
import com.rpgbot.database.repositories.UserRepository;
import com.rpgbot.util.TextFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InventoryHandler implements Command {
    private final RpgBot bot;
    private final long chatId;
    private final UserRepository userRepository;
    private final CharacterRepository characterRepository;
    private final InventoryRepository inventoryRepository;
    private String[] args;
    private int messageId;

    public InventoryHandler(RpgBot bot, long chatId,
                            UserRepository userRepository,
                            CharacterRepository characterRepository,
                            InventoryRepository inventoryRepository) {
        this.bot = bot;
        this.chatId = chatId;
        this.userRepository = userRepository;
        this.characterRepository = characterRepository;
        this.inventoryRepository = inventoryRepository;
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
            showInventory();
            return;
        }

        switch (args[0]) {
            case "view": viewItem(); break;
            case "equip": equipItem(); break;
            case "use": useItem(); break;
            case "sell": showSellMenu(); break;
            default: showInventory();
        }
    }

    private void showInventory() {
        Character character = getCurrentCharacter();
        List<Item> items = inventoryRepository.findByCharacter(character);
        int gold = character.getGold();

        String message = String.format(
                "💰 Золото: %d\n\n📦 Инвентарь (%d предметов):",
                gold,
                items.size()
        );

        if (items.isEmpty()) {
            message += "\nВаш инвентарь пуст";
            bot.sendMessage(chatId, message);
            return;
        }

        InlineKeyboardMarkup keyboard = createInventoryKeyboard(items);
        bot.sendMessageWithKeyboard(chatId, message, keyboard);
    }

    private InlineKeyboardMarkup createInventoryKeyboard(List<Item> items) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (Item item : items) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(InlineKeyboardButton.builder()
                    .text(item.getName())
                    .callbackData("/inventory view " + item.getId())
                    .build());
            keyboard.add(row);
        }

        List<InlineKeyboardButton> actions = new ArrayList<>();
        actions.add(InlineKeyboardButton.builder()
                .text("💰 Продать")
                .callbackData("/inventory sell")
                .build());
        actions.add(InlineKeyboardButton.builder()
                .text("🔙 Назад")
                .callbackData("/main")
                .build());

        keyboard.add(actions);
        return bot.createKeyboard(keyboard);
    }

    private void viewItem() {
        if (args.length < 2) return;

        long itemId = Long.parseLong(args[1]);
        Optional<Item> item = inventoryRepository.findById(itemId);

        if (item.isEmpty()) {
            bot.sendMessage(chatId, "Предмет не найден");
            return;
        }

        String message = TextFormatter.formatItemDetails(item.get());
        InlineKeyboardMarkup keyboard = createItemActionsKeyboard(item.get());
        bot.sendMessageWithKeyboard(chatId, message, keyboard);
    }

    private InlineKeyboardMarkup createItemActionsKeyboard(Item item) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        if (item.getType().equals("weapon") || item.getType().equals("armor")) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(InlineKeyboardButton.builder()
                    .text(item.isEquipped() ? "⛔ Снять" : "✅ Экипировать")
                    .callbackData("/inventory equip " + item.getId())
                    .build());
            keyboard.add(row);
        } else if (item.getType().equals("potion")) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(InlineKeyboardButton.builder()
                    .text("🍶 Использовать")
                    .callbackData("/inventory use " + item.getId())
                    .build());
            keyboard.add(row);
        }

        List<InlineKeyboardButton> backRow = new ArrayList<>();
        backRow.add(InlineKeyboardButton.builder()
                .text("🔙 Назад")
                .callbackData("/inventory")
                .build());
        keyboard.add(backRow);

        return bot.createKeyboard(keyboard);
    }

    private void equipItem() {
        if (args.length < 2) return;

        long itemId = Long.parseLong(args[1]);
        Character character = getCurrentCharacter();
        Optional<Item> item = inventoryRepository.findById(itemId);

        if (item.isEmpty()) {
            bot.sendMessage(chatId, "Предмет не найден");
            return;
        }

        if (item.get().isEquipped()) {
            inventoryRepository.unequip(item.get());
            bot.sendMessage(chatId, "Вы сняли " + item.get().getName());
        } else {
            inventoryRepository.equip(character, item.get());
            bot.sendMessage(chatId, "Вы экипировали " + item.get().getName());
        }

        showInventory();
    }

    private void useItem() {
        if (args.length < 2) return;

        long itemId = Long.parseLong(args[1]);
        Character character = getCurrentCharacter();
        Optional<Item> item = inventoryRepository.findById(itemId);

        if (item.isEmpty() || !item.get().getType().equals("potion")) {
            bot.sendMessage(chatId, "Это нельзя использовать");
            return;
        }

        switch (item.get().getSubType()) {
            case "health":
                int heal = item.get().getHealthBonus();
                character.setCurrentHealth(Math.min(
                        character.getCurrentHealth() + heal,
                        character.getMaxHealth()
                ));
                characterRepository.save(character);
                bot.sendMessage(chatId, "Вы восстановили " + heal + " HP");
                break;
            default:
                bot.sendMessage(chatId, "Вы использовали " + item.get().getName());
        }

        inventoryRepository.remove(character, item.get());
        showInventory();
    }

    private void showSellMenu() {
        Character character = getCurrentCharacter();
        List<Item> sellableItems = inventoryRepository.findSellable(character);

        if (sellableItems.isEmpty()) {
            bot.sendMessage(chatId, "Нет предметов для продажи");
            return;
        }

        String message = "Выберите предмет для продажи:";
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (Item item : sellableItems) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(InlineKeyboardButton.builder()
                    .text(item.getName() + " (" + item.getSellPrice() + " золота)")
                    .callbackData("/inventory sell_confirm " + item.getId())
                    .build());
            keyboard.add(row);
        }

        List<InlineKeyboardButton> backRow = new ArrayList<>();
        backRow.add(InlineKeyboardButton.builder()
                .text("🔙 Назад")
                .callbackData("/inventory")
                .build());
        keyboard.add(backRow);

        bot.sendMessageWithKeyboard(chatId, message, bot.createKeyboard(keyboard));
    }

    private Character getCurrentCharacter() {
        return characterRepository.findByUser(
                userRepository.findByChatId(chatId).orElseThrow()
        ).orElseThrow();
    }
}