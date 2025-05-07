package com.rpgbot.classes;

import com.rpgbot.entities.Stats;
import com.rpgbot.entities.Weapon;

public class Cultist extends CharacterClass {
    public Cultist() {
        super(
                "💀 Культист",
                "Боль — это дар",
                new Stats(8, 12, 12, 10, 10),
                new Weapon("Кухонный нож", 1, "melee")
        );
    }

    @Override
    public int calculateDamage(Stats stats, Weapon weapon) {
        if (!weapon.getDamageType().equals("melee")) {
            return 0;
        }
        // Культист жертвует HP для урона
        int hpCost = 5;
        return weapon.getBaseDamage() + (int)(stats.getEndurance() * 0.5) - hpCost;
    }
}