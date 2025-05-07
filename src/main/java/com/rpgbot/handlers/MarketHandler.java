package com.rpgbot.handlers;

import com.rpgbot.RpgBot;
import com.rpgbot.commands.Command;
import com.rpgbot.database.models.AuctionLot;
import com.rpgbot.database.models.Character;
import com.rpgbot.database.models.Item;
import com.rpgbot.database.repositories.AuctionRepository;
import com.rpgbot.database.repositories.CharacterRepository;
import com.rpgbot.database.repositories.InventoryRepository;
import com.rpgbot.database.repositories.UserRepository;
import com.rpgbot.util.TextFormatter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MarketHandler implements Command {
    private final RpgBot bot;
    private final long chatId;
    private final UserRepository userRepository;
    private final CharacterRepository characterRepository;
    private final InventoryRepository inventoryRepository;
    private final AuctionRepository auctionRepository;
    private String[] args;
    private int messageId;

    public MarketHandler(RpgBot bot, long chatId,
                         UserRepository userRepository,
                         CharacterRepository characterRepository,
                         InventoryRepository inventoryRepository,
                         AuctionRepository auctionRepository) {
        this.bot = bot;
        this.chatId = chatId;
        this.userRepository = userRepository;
        this.characterRepository = characterRepository;
        this.inventoryRepository = inventoryRepository;
        this.auctionRepository = auctionRepository;
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
            showMarketMainMenu();
            return;
        }

        switch (args[0]) {
            case "list": showAuctionList(); break;
            case "view": viewAuctionLot(); break;
            case "buy": buyItem(); break;
            case "sell": showSellMenu(); break;
            case "confirm_sell": confirmSellItem(); break;
            case "my_lots": showMyLots(); break;
            case "cancel": cancelAuctionLot(); break;
            default: showMarketMainMenu();
        }
    }

    private void showMarketMainMenu() {
        String message = "🏛️ Аукцион гильдии\n\n" +
                "Комиссия: 1 золотая монета за лот";

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder()
                .text("📜 Список лотов")
                .callbackData("/market list")
                .build());
        row1.add(InlineKeyboardButton.builder()
                .text("💰 Мои лоты")
                .callbackData("/market my_lots")
                .build());

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(InlineKeyboardButton.builder()
                .text("💎 Продать предмет")
                .callbackData("/market sell")
                .build());

        keyboard.add(row1);
        keyboard.add(row2);

        bot.sendMessageWithKeyboard(chatId, message, bot.createKeyboard(keyboard));
    }

    private void showAuctionList() {
        List<AuctionLot> lots = auctionRepository.findActive();

        if (lots.isEmpty()) {
            bot.sendMessage(chatId, "Нет активных лотов");
            return;
        }

        String message = "Активные лоты:";
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (AuctionLot lot : lots) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(InlineKeyboardButton.builder()
                    .text(lot.getItem().getName() + " - " + lot.getPrice() + " золота")
                    .callbackData("/market view " + lot.getId())
                    .build());
            keyboard.add(row);
        }

        List<InlineKeyboardButton> backRow = new ArrayList<>();
        backRow.add(InlineKeyboardButton.builder()
                .text("🔙 Назад")
                .callbackData("/market")
                .build());
        keyboard.add(backRow);

        bot.sendMessageWithKeyboard(chatId, message, bot.createKeyboard(keyboard));
    }

    private void viewAuctionLot() {
        if (args.length < 2) return;

        long lotId = Long.parseLong(args[1]);
        Optional<AuctionLot> lotOpt = auctionRepository.findById(lotId);

        if (lotOpt.isEmpty()) {
            bot.sendMessage(chatId, "Лот не найден");
            return;
        }

        AuctionLot lot = lotOpt.get();
        Character character = getCurrentCharacter();

        String message = buildLotInfoMessage(lot);
        InlineKeyboardMarkup keyboard = createLotActionsKeyboard(lot, character);

        bot.sendMessageWithKeyboard(chatId, message, keyboard);
    }

    private String buildLotInfoMessage(AuctionLot lot) {
        return String.format(
                "🏷️ Лот #%d\n" +
                        "📦 %s\n" +
                        "💰 %d золота\n" +
                        "⏳ Осталось: %d дней\n" +
                        "👤 Продавец: %s\n\n" +
                        "%s",
                lot.getId(),
                lot.getItem().getName(),
                lot.getPrice(),
                LocalDateTime.now().until(lot.getExpiresAt()).toDays(),
                lot.getSeller().getName(),
                lot.getItem().getDescription()
        );
    }

    private InlineKeyboardMarkup createLotActionsKeyboard(AuctionLot lot, Character character) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        if (!lot.getSeller().equals(character)) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(InlineKeyboardButton.builder()
                    .text("🛒 Купить")
                    .callbackData("/market buy " + lot.getId())
                    .build());
            keyboard.add(row);
        } else {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(InlineKeyboardButton.builder()
                    .text("❌ Отозвать")
                    .callbackData("/market cancel " + lot.getId())
                    .build());
            keyboard.add(row);
        }

        List<InlineKeyboardButton> backRow = new ArrayList<>();
        backRow.add(InlineKeyboardButton.builder()
                .text("🔙 Назад")
                .callbackData("/market list")
                .build());
        keyboard.add(backRow);

        return bot.createKeyboard(keyboard);
    }

    private void buyItem() {
        if (args.length < 2) return;

        long lotId = Long.parseLong(args[1]);
        Optional<AuctionLot> lotOpt = auctionRepository.findById(lotId);
        Character buyer = getCurrentCharacter();

        if (lotOpt.isEmpty()) {
            bot.sendMessage(chatId, "Лот не найден");
            return;
        }

        AuctionLot lot = lotOpt.get();

        if (buyer.getGold() < lot.getPrice()) {
            bot.sendMessage(chatId, "Недостаточно золота");
            return;
        }

        // Передача золота
        buyer.setGold(buyer.getGold() - lot.getPrice());
        lot.getSeller().setGold(lot.getSeller().getGold() + lot.getPrice());

        // Передача предмета
        inventoryRepository.addItem(buyer, lot.getItem());

        // Обновление статуса
        lot.setSold(true);
        auctionRepository.save(lot);

        // Уведомления
        bot.sendMessage(chatId,
                "Вы купили " + lot.getItem().getName() + " за " + lot.getPrice() + " золота");

        notifySeller(lot);
    }

    private void notifySeller(AuctionLot lot) {
        String message = String.format(
                "Ваш лот %s продан за %d золота",
                lot.getItem().getName(),
                lot.getPrice()
        );

        bot.sendMessage(
                userRepository.findByCharacter(lot.getSeller()).get().getChatId(),
                message
        );
    }

    private void showSellMenu() {
        Character character = getCurrentCharacter();
        List<Item> items = inventoryRepository.findSellable(character);

        if (items.isEmpty()) {
            bot.sendMessage(chatId, "Нет предметов для продажи");
            return;
        }

        String message = "Выберите предмет для продажи:";
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (Item item : items) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(InlineKeyboardButton.builder()
                    .text(item.getName())
                    .callbackData("/market select_item " + item.getId())
                    .build());
            keyboard.add(row);
        }

        List<InlineKeyboardButton> backRow = new ArrayList<>();
        backRow.add(InlineKeyboardButton.builder()
                .text("🔙 Назад")
                .callbackData("/market")
                .build());
        keyboard.add(backRow);

        bot.sendMessageWithKeyboard(chatId, message, bot.createKeyboard(keyboard));
    }

    private void confirmSellItem() {
        if (args.length < 3) return;

        long itemId = Long.parseLong(args[1]);
        int price = Integer.parseInt(args[2]);
        Character seller = getCurrentCharacter();

        if (seller.getGold() < 1) {
            bot.sendMessage(chatId, "Не хватает золота для комиссии (1 золотая)");
            return;
        }

        Optional<Item> itemOpt = inventoryRepository.findById(itemId);
        if (itemOpt.isEmpty()) {
            bot.sendMessage(chatId, "Предмет не найден");
            return;
        }

        Item item = itemOpt.get();

        // Создание лота
        AuctionLot lot = new AuctionLot();
        lot.setItem(item);
        lot.setSeller(seller);
        lot.setPrice(price);
        lot.setCreatedAt(LocalDateTime.now());
        lot.setExpiresAt(LocalDateTime.now().plusDays(14));

        // Оплата комиссии
        seller.setGold(seller.getGold() - 1);

        // Удаление предмета
        inventoryRepository.remove(seller, item);

        // Сохранение
        auctionRepository.save(lot);
        characterRepository.save(seller);

        bot.sendMessage(chatId,
                "Предмет выставлен на аукцион за " + price + " золота\n" +
                        "Комиссия: 1 золотая");
    }

    private void showMyLots() {
        Character character = getCurrentCharacter();
        List<AuctionLot> lots = auctionRepository.findBySeller(character);

        if (lots.isEmpty()) {
            bot.sendMessage(chatId, "У вас нет активных лотов");
            return;
        }

        String message = "Ваши лоты:";
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (AuctionLot lot : lots) {
            String status = lot.isSold() ? "✅ Продано" :
                    "⏳ Активно (" + lot.getExpiresAt().until(LocalDateTime.now()).toDays() + "д)";

            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(InlineKeyboardButton.builder()
                    .text(lot.getItem().getName() + " - " + lot.getPrice() + " золота (" + status + ")")
                    .callbackData("/market view " + lot.getId())
                    .build());
            keyboard.add(row);
        }

        List<InlineKeyboardButton> backRow = new ArrayList<>();
        backRow.add(InlineKeyboardButton.builder()
                .text("🔙 Назад")
                .callbackData("/market")
                .build());
        keyboard.add(backRow);

        bot.sendMessageWithKeyboard(chatId, message, bot.createKeyboard(keyboard));
    }

    private void cancelAuctionLot() {
        if (args.length < 2) return;

        long lotId = Long.parseLong(args[1]);
        Optional<AuctionLot> lotOpt = auctionRepository.findById(lotId);
        Character character = getCurrentCharacter();

        if (lotOpt.isEmpty() || !lotOpt.get().getSeller().equals(character)) {
            bot.sendMessage(chatId, "Нельзя отозвать этот лот");
            return;
        }

        AuctionLot lot = lotOpt.get();
        inventoryRepository.addItem(character, lot.getItem());
        auctionRepository.delete(lot);

        bot.sendMessage(chatId, "Лот отозван, предмет возвращен в инвентарь");
    }

    private Character getCurrentCharacter() {
        return characterRepository.findByUser(
                userRepository.findByChatId(chatId).orElseThrow()
        ).orElseThrow();
    }
}