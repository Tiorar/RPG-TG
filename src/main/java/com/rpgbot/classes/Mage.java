package com.rpgbot.classes;

import com.rpgbot.entities.Stats;
import com.rpgbot.entities.Weapon;

public class Mage extends CharacterClass {
    public Mage() {
        super(
                "🧙 Волшебник",
                "Знание — сила, но магия — сила умноженная",
                new Stats(8, 10, 10, 13, 11),
                new Weapon("Посох новичка", 1, "magic")
        );
    }

    @Override
    public int calculateDamage(Stats stats, Weapon weapon) {
        if (!weapon.getDamageType().equals("melee")) {
            return 0;
        }
        return weapon.getBaseDamage() + (int)(stats.getWisdom() * 2.0);
    }

    public int getMaxMana(Stats stats) {
        return stats.getIntelligence() * 10;  // 12 int = 120 маны
    }
}