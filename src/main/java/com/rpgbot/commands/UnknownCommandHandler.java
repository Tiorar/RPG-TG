package com.rpgbot.commands;

import com.rpgbot.bot.RpgBot;

public class UnknownCommandHandler implements Command {
    private final RpgBot bot;
    private final long chatId;

    public UnknownCommandHandler(RpgBot bot, long chatId) {
        this.bot = bot;
        this.chatId = chatId;
    }

    @Override
    public void setArgs(String[] args) {}

    @Override
    public void setMessageId(int messageId) {}

    @Override
    public void execute() {
        bot.sendMessage(chatId, "Неизвестная команда. Используйте /start для начала.");
    }
}