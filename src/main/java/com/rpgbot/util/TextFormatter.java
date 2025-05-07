package com.rpgbot.util;

import com.rpgbot.database.models.Character;
import com.rpgbot.database.models.Item;
import com.rpgbot.entities.Monster;

public class TextFormatter {
    public static String formatCharacterInfo(Character character) {
        StringBuilder sb = new StringBuilder();
        sb.append("👤 ").append(character.getName()).append("\n");
        sb.append("🏆 Уровень: ").append(character.getLevel()).append("\n");
        sb.append("💫 Опыт: ").append(character.getExperience()).append("/").append(character.getExpToNextLevel()).append("\n");
        sb.append("💰 Золото: ").append(character.getGold()).append("\n\n");

        sb.append("❤️ Здоровье: ").append(character.getCurrentHealth()).append("/").append(character.getMaxHealth()).append("\n");

        if (character.getEquippedWeapon() != null) {
            sb.append("⚔️ Оружие: ").append(character.getEquippedWeapon().getName()).append("\n");
        }

        if (character.getEquippedArmor() != null) {
            sb.append("🛡️ Броня: ").append(character.getEquippedArmor().getName()).append("\n");
        }

        return sb.toString();
    }

    public static String formatCharacterStats(Character character) {
        StringBuilder sb = new StringBuilder();
        sb.append("📊 Характеристики ").append(character.getName()).append(":\n\n");

        sb.append("💪 Сила: ").append(character.getStats().getStrength()).append("\n");
        sb.append("🏹 Ловкость: ").append(character.getStats().getAgility()).append("\n");
        sb.append("🛡️ Выносливость: ").append(character.getStats().getEndurance()).append("\n");
        sb.append("📚 Интеллект: ").append(character.getStats().getIntelligence()).append("\n");
        sb.append("🔮 Мудрость: ").append(character.getStats().getWisdom()).append("\n\n");

        sb.append("🎯 Атака: ").append(character.getAttackPower()).append("\n");
        sb.append("❤️ Здоровье: ").append(character.getMaxHealth()).append("\n");

        if (character.getStatPoints() > 0) {
            sb.append("\nДоступно очков характеристик: ").append(character.getStatPoints());
        }

        return sb.toString();
    }

    public static String formatBattleStatus(Character character, Monster enemy, boolean isCharacterTurn, boolean battleEnded) {
        StringBuilder sb = new StringBuilder();

        sb.append("⚔️ Бой с ").append(enemy.getName()).append(" ⚔️\n\n");

        // Статус персонажа
        sb.append("👤 ").append(character.getName()).append("\n");
        sb.append("❤️ ").append(createHealthBar(character.getCurrentHealth(), character.getMaxHealth())).append("\n");
        sb.append("   ").append(character.getCurrentHealth()).append("/").append(character.getMaxHealth()).append(" HP\n\n");

        // Статус врага
        sb.append("👹 ").append(enemy.getName()).append("\n");
        sb.append("❤️ ").append(createHealthBar(enemy.getCurrentHealth(), enemy.getMaxHealth())).append("\n");
        sb.append("   ").append(enemy.getCurrentHealth()).append("/").append(enemy.getMaxHealth()).append(" HP\n\n");

        if (battleEnded) {
            if (character.getCurrentHealth() > 0) {
                sb.append("🎉 Вы победили!");
            } else {
                sb.append("💀 Вы проиграли...");
            }
        } else if (isCharacterTurn) {
            sb.append("Ваш ход. Выберите действие:");
        } else {
            sb.append(enemy.getName()).append(" готовится атаковать...");
        }

        return sb.toString();
    }

    private static String createHealthBar(int current, int max) {
        int filled = (int) Math.round(10 * ((double) current / max));
        filled = Math.max(0, Math.min(10, filled));

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < 10; i++) {
            sb.append(i < filled ? "█" : "░");
        }
        sb.append("]");

        return sb.toString();
    }

    public static String formatItemDetails(Item item) {
        StringBuilder sb = new StringBuilder();

        sb.append("🛒 ").append(item.getName()).append("\n");
        sb.append("✨ Редкость: ").append(getRarityName(item.getRarity())).append("\n\n");

        sb.append(item.getDescription()).append("\n\n");

        sb.append("💰 Цена продажи: ").append(item.getSellPrice()).append(" золота");

        return sb.toString();
    }

    private static String getRarityName(String rarity) {
        switch (rarity) {
            case "common": return "Обычный";
            case "rare": return "Редкий";
            case "epic": return "Эпический";
            case "legendary": return "Легендарный";
            default: return rarity;
        }
    }
}