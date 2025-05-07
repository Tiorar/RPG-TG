package com.rpgbot.classes;

import com.rpgbot.entities.Stats;
import com.rpgbot.entities.Weapon;

public class Warrior extends CharacterClass {
    public Warrior() {
        super(
                "🗡️ Воин",
                "Броня — моя вера, меч — мой закон",
                new Stats(12, 10, 12, 8, 10),  // strength, agility, endurance, int, wis
                new Weapon("Тупой меч", 1, "melee")  // Стартовое оружие
        );
    }

    @Override
    public int calculateDamage(Stats stats, Weapon weapon) {
        if (!weapon.getDamageType().equals("melee")) {
            return 0;  // Воин не может использовать магическое/дальнее оружие
        }
        return weapon.getBaseDamage() + (int)(stats.getStrength() * 1.5);
    }
}