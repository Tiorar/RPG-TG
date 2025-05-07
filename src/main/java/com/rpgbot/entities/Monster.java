package com.rpgbot.entities;

import com.rpgbot.util.RandomUtil;

public class Monster {
    private final String name;
    private final Stats stats;
    private int currentHealth;
    private final String damageType;

    public Monster(String name, Stats stats) {
        this(name, stats, "physical");
    }

    public Monster(String name, Stats stats, String damageType) {
        this.name = name;
        this.stats = stats;
        this.currentHealth = stats.getHealth();
        this.damageType = damageType;
    }

    // Конструкторы для быстрого создания
    public Monster(String name, int health, int minDamage, int maxDamage) {
        this(name, new Stats(health, minDamage, maxDamage));
    }

    public Monster(String name, int health, int minDamage, int maxDamage, String damageType) {
        this(name, new Stats(health, minDamage, maxDamage), damageType);
    }

    public void takeDamage(int amount) {
        currentHealth = Math.max(0, currentHealth - amount);
    }

    public boolean isAlive() {
        return currentHealth > 0;
    }

    public int getAttack() {
        return RandomUtil.randomInt(stats.getMinDamage(), stats.getMaxDamage());
    }

    // Геттеры
    public String getName() { return name; }
    public Stats getStats() { return stats; }
    public int getCurrentHealth() { return currentHealth; }
    public int getMaxHealth() { return stats.getHealth(); }
    public String getDamageType() { return damageType; }
}