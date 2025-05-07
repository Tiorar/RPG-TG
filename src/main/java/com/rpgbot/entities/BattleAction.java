package com.rpgbot.entities;

public enum BattleAction {
    ATTACK("⚔️ Атаковать"),
    USE_ITEM("🧪 Использовать предмет"),
    FLEE("🏃 Бежать"),
    END_TURN("⏹ Завершить ход");

    private final String displayName;

    BattleAction(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
