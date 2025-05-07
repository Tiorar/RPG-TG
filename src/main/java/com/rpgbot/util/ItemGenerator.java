package com.rpgbot.util;

import com.rpgbot.database.models.Item;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ItemGenerator {
    private static final List<String> WEAPON_NAMES = Arrays.asList(
            "Меч", "Топор", "Посох", "Кинжал", "Лук", "Арбалет", "Молот", "Копье"
    );

    private static final List<String> ARMOR_NAMES = Arrays.asList(
            "Кольчуга", "Латный доспех", "Кожаный доспех", "Мантия", "Роба", "Пластинчатый доспех"
    );

    private static final List<String> POTION_NAMES = Arrays.asList(
            "Зелье здоровья", "Зелье маны", "Зелье силы", "Зелье ловкости"
    );

    private static final List<String> RARITY_SUFFIXES = Arrays.asList(
            "Новичка", "Странника", "Искателя", "Героя", "Мастера", "Легенды"
    );

    private static final List<String> ELEMENTAL_SUFFIXES = Arrays.asList(
            "Огня", "Льда", "Молнии", "Тьмы", "Света", "Хаоса"
    );

    public static Item generateItem(int rarityLevel) {
        Random random = new Random();
        Item item = new Item();

        // Определяем редкость
        String rarity;
        int minStat, maxStat;

        switch (rarityLevel) {
            case 1: // Rare
                rarity = "rare";
                minStat = 1;
                maxStat = 3;
                break;
            case 2: // Epic
                rarity = "epic";
                minStat = 3;
                maxStat = 5;
                break;
            case 3: // Legendary
                rarity = "legendary";
                minStat = 5;
                maxStat = 8;
                break;
            default: // Common
                rarity = "common";
                minStat = 0;
                maxStat = 1;
        }

        // Генерируем тип предмета
        int itemType = random.nextInt(3);
        switch (itemType) {
            case 0: // Weapon
                item.setType("weapon");
                item.setName(generateWeaponName(rarityLevel));
                item.setDamage(random.nextInt(maxStat - minStat + 1) + minStat);

                if (rarityLevel > 0) {
                    addRandomStats(item, minStat, maxStat);
                    addElementalEffect(item, rarityLevel);
                }
                break;

            case 1: // Armor
                item.setType("armor");
                item.setName(generateArmorName(rarityLevel));
                item.setArmor(random.nextInt(maxStat - minStat + 1) + minStat);

                if (rarityLevel > 0) {
                    addRandomStats(item, minStat, maxStat);
                    addResistance(item, rarityLevel);
                }
                break;

            case 2: // Potion
                item.setType("potion");
                item.setSubType("health"); // Пока только зелья здоровья
                item.setName(POTION_NAMES.get(random.nextInt(POTION_NAMES.size())));
                item.setHealthBonus(10 + random.nextInt(20) * rarityLevel);
                break;
        }

        item.setRarity(rarity);
        item.setDescription(generateDescription(item));
        item.setSellPrice(calculateSellPrice(item));

        return item;
    }

    private static String generateWeaponName(int rarityLevel) {
        Random random = new Random();
        String name = WEAPON_NAMES.get(random.nextInt(WEAPON_NAMES.size()));

        if (rarityLevel > 0) {
            if (random.nextBoolean()) {
                name += " " + RARITY_SUFFIXES.get(rarityLevel + 1);
            } else {
                name += " " + ELEMENTAL_SUFFIXES.get(random.nextInt(ELEMENTAL_SUFFIXES.size()));
            }
        }

        return name;
    }

    private static String generateArmorName(int rarityLevel) {
        Random random = new Random();
        String name = ARMOR_NAMES.get(random.nextInt(ARMOR_NAMES.size()));

        if (rarityLevel > 0) {
            name += " " + RARITY_SUFFIXES.get(rarityLevel + 2);
        }

        return name;
    }

    private static void addRandomStats(Item item, int min, int max) {
        Random random = new Random();
        String[] stats = {"strength", "agility", "endurance", "intelligence", "wisdom"};
        int statCount = random.nextInt(2) + 1; // 1-2 дополнительных стата

        for (int i = 0; i < statCount; i++) {
            String stat = stats[random.nextInt(stats.length)];
            int value = random.nextInt(max - min + 1) + min;

            switch (stat) {
                case "strength":
                    item.setStrengthBonus(value);
                    break;
                case "agility":
                    item.setAgilityBonus(value);
                    break;
                case "endurance":
                    item.setEnduranceBonus(value);
                    break;
                case "intelligence":
                    item.setIntelligenceBonus(value);
                    break;
                case "wisdom":
                    item.setWisdomBonus(value);
                    break;
            }
        }
    }

    private static void addElementalEffect(Item item, int rarityLevel) {
        if (item.getType().equals("weapon")) {
            Random random = new Random();
            String[] elements = {"fire", "ice", "lightning", "dark", "holy", "chaos"};
            String element = elements[random.nextInt(elements.length)];

            item.setDamageType(element);
            item.setDamage(item.getDamage() + rarityLevel); // Дополнительный урон за стихию
        }
    }

    private static void addResistance(Item item, int rarityLevel) {
        if (item.getType().equals("armor")) {
            Random random = new Random();
            String[] elements = {"fire", "ice", "lightning", "dark", "holy", "chaos"};
            String element = elements[random.nextInt(elements.length)];

            item.setResistanceType(element);
            item.setResistanceValue(10 * rarityLevel); // 10%, 20% или 30% сопротивления
        }
    }

    private static String generateDescription(Item item) {
        StringBuilder desc = new StringBuilder();

        if (item.getType().equals("weapon")) {
            desc.append("Оружие");
            if (item.getDamageType() != null) {
                desc.append(" ").append(getElementName(item.getDamageType()));
            }
            desc.append(". Урон: ").append(item.getDamage());
        } else if (item.getType().equals("armor")) {
            desc.append("Доспех. Защита: ").append(item.getArmor());
            if (item.getResistanceType() != null) {
                desc.append(", сопротивление ").append(getElementName(item.getResistanceType()))
                        .append(": ").append(item.getResistanceValue()).append("%");
            }
        } else if (item.getType().equals("potion")) {
            desc.append("Восстанавливает ").append(item.getHealthBonus()).append(" здоровья.");
        }

        // Добавляем бонусы к статам
        if (item.getStrengthBonus() > 0) {
            desc.append("\n+").append(item.getStrengthBonus()).append(" к силе");
        }
        if (item.getAgilityBonus() > 0) {
            desc.append("\n+").append(item.getAgilityBonus()).append(" к ловкости");
        }
        if (item.getEnduranceBonus() > 0) {
            desc.append("\n+").append(item.getEnduranceBonus()).append(" к выносливости");
        }
        if (item.getIntelligenceBonus() > 0) {
            desc.append("\n+").append(item.getIntelligenceBonus()).append(" к интеллекту");
        }
        if (item.getWisdomBonus() > 0) {
            desc.append("\n+").append(item.getWisdomBonus()).append(" к мудрости");
        }

        return desc.toString();
    }

    private static String getElementName(String element) {
        switch (element) {
            case "fire": return "огня";
            case "ice": return "льда";
            case "lightning": return "молнии";
            case "dark": return "тьмы";
            case "holy": return "света";
            case "chaos": return "хаоса";
            default: return "";
        }
    }

    private static int calculateSellPrice(Item item) {
        int price = 0;

        if (item.getType().equals("weapon")) {
            price = item.getDamage() * 5;
        } else if (item.getType().equals("armor")) {
            price = item.getArmor() * 5;
        } else if (item.getType().equals("potion")) {
            price = item.getHealthBonus() / 2;
        }

        // Множитель редкости
        switch (item.getRarity()) {
            case "rare":
                price *= 3;
                break;
            case "epic":
                price *= 10;
                break;
            case "legendary":
                price *= 30;
                break;
        }

        // Добавляем цену за бонусы к статам
        price += (item.getStrengthBonus() + item.getAgilityBonus() +
                item.getEnduranceBonus() + item.getIntelligenceBonus() +
                item.getWisdomBonus()) * 2;

        return Math.max(1, price);
    }
}