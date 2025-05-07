package com.rpgbot.database.repositories;

import com.rpgbot.database.models.Item;
import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    void save(Item item);
    Optional<Item> findById(Long id);
    List<Item> findAll();
    List<Item> findByRarity(String rarity);
    void delete(Item item);
}