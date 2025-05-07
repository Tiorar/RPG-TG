package com.rpgbot.database.models;

import com.rpgbot.entities.Stats;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "characters")
public class Character {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String className;

    private int level = 1;
    private int experience = 0;
    private int gold = 0;
    private int statPoints = 0;

    @Embedded
    private Stats stats;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "character", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InventoryItem> inventory = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "equipped_weapon_id")
    private Item equippedWeapon;

    @ManyToOne
    @JoinColumn(name = "equipped_armor_id")
    private Item equippedArmor;

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }
    public int getGold() { return gold; }
    public void setGold(int gold) { this.gold = gold; }
    public int getStatPoints() { return statPoints; }
    public void setStatPoints(int statPoints) { this.statPoints = statPoints; }
    public Stats getStats() { return stats; }
    public void setStats(Stats stats) { this.stats = stats; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public List<InventoryItem> getInventory() { return inventory; }
    public Item getEquippedWeapon() { return equippedWeapon; }
    public void setEquippedWeapon(Item equippedWeapon) { this.equippedWeapon = equippedWeapon; }
    public Item getEquippedArmor() { return equippedArmor; }
    public void setEquippedArmor(Item equippedArmor) { this.equippedArmor = equippedArmor; }

    public void addItemToInventory(Item item) {
        InventoryItem inventoryItem = new InventoryItem();
        inventoryItem.setCharacter(this);
        inventoryItem.setItem(item);
        inventoryItem.setQuantity(1);
        inventory.add(inventoryItem);
    }

    public int getMaxHealth() {
        return stats.getEndurance() * 10 + (level * 5);
    }

    public int getAttackPower() {
        int baseAttack = stats.getStrength() * 2;
        return equippedWeapon != null ? baseAttack + equippedWeapon.getDamage() : baseAttack;
    }
}