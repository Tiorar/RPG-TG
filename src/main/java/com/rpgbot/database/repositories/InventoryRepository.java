package com.rpgbot.database.repositories;

import com.rpgbot.database.models.Character;
import com.rpgbot.database.models.InventoryItem;
import com.rpgbot.database.models.Item;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository {
    void addItem(Character character, Item item);
    void removeItem(Character character, Item item);
    List<InventoryItem> findByCharacter(Character character);
    List<InventoryItem> findByCharacterAndType(Character character, String type);
    Optional<InventoryItem> findById(long id);
    void updateQuantity(long id, int newQuantity);
}