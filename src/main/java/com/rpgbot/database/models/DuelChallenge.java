package com.rpgbot.database.models;

import javax.persistence.*;

@Entity
public class DuelChallenge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "challenger_id")
    private Character challenger;

    @ManyToOne
    @JoinColumn(name = "opponent_id")
    private Character opponent;

    private int betAmount;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Character getChallenger() {
        return challenger;
    }

    public void setChallenger(Character challenger) {
        this.challenger = challenger;
    }

    public Character getOpponent() {
        return opponent;
    }

    public void setOpponent(Character opponent) {
        this.opponent = opponent;
    }

    public int getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(int betAmount) {
        this.betAmount = betAmount;
    }
}