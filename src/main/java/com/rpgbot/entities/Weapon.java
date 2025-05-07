package com.rpgbot.entities;

public class Weapon {
    private final String name;
    private final String type; // "sword", "axe", "bow", etc.
    private final int baseDamage;
    private final String damageType; // "physical", "fire", "ice", etc.
    private final int attackSpeed;
    private final int durability;

    public Weapon(String name, String type, int baseDamage) {
        this(name, type, baseDamage, "physical", 100, 100);
    }

    public Weapon(String name, String type, int baseDamage,
                  String damageType, int attackSpeed, int durability) {
        this.name = name;
        this.type = type;
        this.baseDamage = baseDamage;
        this.damageType = damageType;
        this.attackSpeed = attackSpeed;
        this.durability = durability;
    }

    public int calculateDamage(Stats stats) {
        int statBonus = 0;
        switch (type) {
            case "sword":
            case "axe":
                statBonus = stats.getStrength() / 2;
                break;
            case "bow":
            case "dagger":
                statBonus = stats.getAgility() / 2;
                break;
            case "staff":
                statBonus = stats.getIntelligence() / 2;
                break;
        }
        return baseDamage + statBonus;
    }

    // Геттеры
    public String getName() { return name; }
    public String getType() { return type; }
    public int getBaseDamage() { return baseDamage; }
    public String getDamageType() { return damageType; }
    public int getAttackSpeed() { return attackSpeed; }
    public int getDurability() { return durability; }
}