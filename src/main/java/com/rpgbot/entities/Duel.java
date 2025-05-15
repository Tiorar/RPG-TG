package com.rpgbot.entities;

import com.rpgbot.database.models.Character;

public class Duel {
    private final Character challenger;
    private final Character opponent;
    private final int betAmount;
    private boolean challengerTurn;
    private boolean completed;

    public Duel(Character challenger, Character opponent, int betAmount) {
        this.challenger = challenger;
        this.opponent = opponent;
        this.betAmount = betAmount;
        this.challengerTurn = true; // Challenger goes first
        this.completed = false;
    }

    public Character getChallenger() {
        return challenger;
    }

    public Character getOpponent() {
        return opponent;
    }

    public int getBetAmount() {
        return betAmount;
    }

    public boolean isChallengerTurn() {
        return challengerTurn;
    }

    public void performAttack() {
        Character attacker = challengerTurn ? challenger : opponent;
        Character defender = challengerTurn ? opponent : challenger;

        int damage = calculateDamage(attacker);
        defender.setCurrentHealth(defender.getCurrentHealth() - damage);

        if (defender.getCurrentHealth() <= 0) {
            completed = true;
        }
    }

    public void endTurn() {
        challengerTurn = !challengerTurn;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Character getWinner() {
        if (!completed) return null;
        return challenger.getCurrentHealth() > 0 ? challenger : opponent;
    }

    private int calculateDamage(Character attacker) {
        return (int) (attacker.getAttack() * (0.8 + Math.random() * 0.4));
    }
}