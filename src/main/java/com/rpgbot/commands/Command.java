package com.rpgbot.commands;

public interface Command {
    void setArgs(String[] args);
    void setMessageId(int messageId);
    void execute();
}
