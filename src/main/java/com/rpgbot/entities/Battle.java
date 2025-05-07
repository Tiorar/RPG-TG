package com.rpgbot.entities;

import com.rpgbot.database.models.Character;
import com.rpgbot.util.RandomUtil;
import com.rpgbot.database.models.*;
import com.rpgbot.database.repositories.*;
import com.rpgbot.commands.*;
import com.rpgbot.handlers.*;


public class Battle {
    private final Character player;
    private final Monster enemy;
    private boolean playerTurn;
    private boolean battleEnded;

    public Battle(Character player, Monster enemy) {
        this.player = player;
        this.enemy = enemy;
        this.playerTurn = determineInitiative();
        this.battleEnded = false;
    }

    private boolean determineInitiative() {
        int playerRoll = RandomUtil.rollD20() + player.getStats().getAgility() / 2;
        int enemyRoll = RandomUtil.rollD20() + enemy.getStats().getAgility() / 2;
        return playerRoll >= enemyRoll;
    }

    public void playerAttack() {
        if (!playerTurn || battleEnded) return;

        int damage = calculatePlayerDamage();
        enemy.takeDamage(damage);

        if (!enemy.isAlive()) {
            battleEnded = true;
            return;
        }

        playerTurn = false;
    }

    private int calculatePlayerDamage() {
        int baseDamage = player.getAttackPower();
        // Учет защиты врага
        return Math.max(1, baseDamage - enemy.getStats().getDefense());
    }

    public void enemyAttack() {
        if (playerTurn || battleEnded) return;

        int damage = calculateEnemyDamage();
        player.setCurrentHealth(player.getCurrentHealth() - damage);

        if (player.getCurrentHealth() <= 0) {
            player.setCurrentHealth(0);
            battleEnded = true;
            return;
        }

        playerTurn = true;
    }

    private int calculateEnemyDamage() {
        int baseDamage = enemy.getAttack();
        // Учет защиты игрока
        return Math.max(1, baseDamage - player.getStats().getDefense());
    }

    public void useItem(Item item) {
        if (!playerTurn || battleEnded) return;

        switch (item.getType()) {
            case "health_potion":
                int healAmount = item.getEffectValue();
                player.setCurrentHealth(Math.min(
                        player.getCurrentHealth() + healAmount,
                        player.getMaxHealth()
                ));
                break;
            // Другие типы предметов
        }

        playerTurn = false;
    }

    // Геттеры
    public boolean isPlayerTurn() { return playerTurn; }
    public boolean isBattleEnded() { return battleEnded; }
    public boolean isPlayerAlive() { return player.getCurrentHealth() > 0; }
    public boolean isEnemyAlive() { return enemy.isAlive(); }
    public Character getPlayer() { return player; }
    public Monster getEnemy() { return enemy; }
}