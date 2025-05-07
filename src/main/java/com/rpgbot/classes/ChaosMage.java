package com.rpgbot.classes;

import com.rpgbot.entities.Stats;
import com.rpgbot.entities.Weapon;

public class ChaosMage extends CharacterClass {
    public ChaosMage() {
        super(
                "🌀 Хаотический Маг",
                "Что такое контроль? Просто отпусти...",
                new Stats(8, 10, 10, 12, 12),
                new Weapon("Посох новичка", 1, "magic")
        );
    }

    @Override
    public int calculateDamage(Stats stats, Weapon weapon) {
        if (!weapon.getDamageType().equals("melee")) {
            return 0;
        }
        // Случайный множитель урона (0.8 - 1.5)
        double chaosMultiplier = 0.8 + Math.random() * 0.7;
        return (int)(weapon.getBaseDamage() + stats.getWisdom() * chaosMultiplier);
    }
}