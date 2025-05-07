package com.rpgbot.entities;

import com.rpgbot.database.models.Item;

public class Equipment {
    private Item weapon;
    private Item armor;
    private Item ring1;
    private Item ring2;
    private Item amulet;

    public Equipment() {
        // Стартовое снаряжение
        this.weapon = new Item("Кулаки", "weapon", 1);
        this.armor = new Item("Тряпье", "armor", 0);
    }

    // Геттеры и сеттеры
    public Item getWeapon() { return weapon; }
    public void setWeapon(Item weapon) { this.weapon = weapon; }
    public Item getArmor() { return armor; }
    public void setArmor(Item armor) { this.armor = armor; }
    public Item getRing1() { return ring1; }
    public void setRing1(Item ring1) { this.ring1 = ring1; }
    public Item getRing2() { return ring2; }
    public void setRing2(Item ring2) { this.ring2 = ring2; }
    public Item getAmulet() { return amulet; }
    public void setAmulet(Item amulet) { this.amulet = amulet; }

    public int getTotalAttackBonus() {
        int bonus = 0;
        if (weapon != null) bonus += weapon.getAttackBonus();
        if (ring1 != null) bonus += ring1.getAttackBonus();
        if (ring2 != null) bonus += ring2.getAttackBonus();
        if (amulet != null) bonus += amulet.getAttackBonus();
        return bonus;
    }

    public int getTotalDefenseBonus() {
        int bonus = 0;
        if (armor != null) bonus += armor.getDefenseBonus();
        if (ring1 != null) bonus += ring1.getDefenseBonus();
        if (ring2 != null) bonus += ring2.getDefenseBonus();
        if (amulet != null) bonus += amulet.getDefenseBonus();
        return bonus;
    }
}