package com.rpgbot.classes;

import com.rpgbot.entities.Stats;
import com.rpgbot.entities.Weapon;

public class Archer extends CharacterClass {
    public Archer() {
        super(
                "🏹 Лучник",
                "Тень стрелы быстрее страха",
                new Stats(10, 13, 10, 8, 11),
                new Weapon("Старый лук", 1, "ranged")
        );
    }

    @Override
    public int calculateDamage(Stats stats, Weapon weapon) {
        if (!weapon.getDamageType().equals("ranged")) {
            return 0;
        }
        return weapon.getBaseDamage() + (int)(stats.getAgility() * 1.8);
    }

    public double getDodgeChance(Stats stats) {
        return stats.getAgility() * 0.01;  // 13 agility = 13% шанс уклона
    }
}