package com.rpgbot.classes;

import com.rpgbot.entities.Stats;
import com.rpgbot.entities.Weapon;

public abstract class CharacterClass {
    protected String name;
    protected String description;
    protected Stats baseStats;
    protected Weapon startingWeapon;

    public CharacterClass(String name, String description, Stats baseStats, Weapon startingWeapon) {
        this.name = name;
        this.description = description;
        this.baseStats = baseStats;
        this.startingWeapon = startingWeapon;
    }

    // Геттеры
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Stats getBaseStats() { return baseStats; }
    public Weapon getStartingWeapon() { return startingWeapon; }

    // Метод для расчета урона (переопределяется в подклассах)
    public abstract int calculateDamage(Stats stats, Weapon weapon);
}