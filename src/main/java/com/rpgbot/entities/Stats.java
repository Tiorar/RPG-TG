package com.rpgbot.entities;

public class Stats {
    private int strength;
    private int agility;
    private int endurance;
    private int intelligence;
    private int wisdom;

    // Боевые характеристики (вычисляемые)
    private int health;
    private int attack;
    private int defense;

    public Stats() {
        this(0, 0, 0, 0, 0);
    }

    public Stats(int strength, int agility, int endurance, int intelligence, int wisdom) {
        this.strength = strength;
        this.agility = agility;
        this.endurance = endurance;
        this.intelligence = intelligence;
        this.wisdom = wisdom;
        calculateDerivedStats();
    }

    // Конструктор для монстров
    public Stats(int health, int minDamage, int maxDamage) {
        this.health = health;
        this.attack = (minDamage + maxDamage) / 2;
        this.defense = 0;
    }

    public void calculateDerivedStats() {
        this.health = endurance * 10;
        this.attack = strength * 2;
        this.defense = agility / 2;
    }

    // Геттеры и сеттеры
    public int getStrength() { return strength; }
    public void setStrength(int strength) {
        this.strength = strength;
        calculateDerivedStats();
    }

    public int getAgility() { return agility; }
    public void setAgility(int agility) {
        this.agility = agility;
        calculateDerivedStats();
    }

    public int getEndurance() { return endurance; }
    public void setEndurance(int endurance) {
        this.endurance = endurance;
        calculateDerivedStats();
    }

    public int getIntelligence() { return intelligence; }
    public void setIntelligence(int intelligence) {
        this.intelligence = intelligence;
        calculateDerivedStats();
    }

    public int getWisdom() { return wisdom; }
    public void setWisdom(int wisdom) {
        this.wisdom = wisdom;
        calculateDerivedStats();
    }

    public int getHealth() { return health; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getMinDamage() { return attack / 2; }
    public int getMaxDamage() { return attack; }
}