package com.rpgbot.database.repositories;

import com.rpgbot.database.models.Character;
import com.rpgbot.database.models.InventoryItem;
import com.rpgbot.database.models.Item;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class InventoryRepositoryImpl implements InventoryRepository {
    private final EntityManager em;

    public InventoryRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public void addItem(Character character, Item item) {
        InventoryItem invItem = new InventoryItem();
        invItem.setCharacter(character);
        invItem.setItem(item);
        invItem.setQuantity(1);
        em.persist(invItem);
    }

    @Override
    public void removeItem(Character character, Item item) {
        em.createQuery("DELETE FROM InventoryItem i WHERE i.character = :character AND i.item = :item")
                .setParameter("character", character)
                .setParameter("item", item)
                .executeUpdate();
    }

    @Override
    public List<InventoryItem> findByCharacter(Character character) {
        return em.createQuery(
                        "SELECT i FROM InventoryItem i WHERE i.character = :character", InventoryItem.class)
                .setParameter("character", character)
                .getResultList();
    }

    @Override
    public List<InventoryItem> findByCharacterAndType(Character character, String type) {
        return em.createQuery(
                        "SELECT i FROM InventoryItem i JOIN i.item it WHERE i.character = :character AND it.type = :type",
                        InventoryItem.class)
                .setParameter("character", character)
                .setParameter("type", type)
                .getResultList();
    }

    @Override
    public Optional<InventoryItem> findById(long id) {
        return Optional.ofNullable(em.find(InventoryItem.class, id));
    }

    @Override
    public void updateQuantity(long id, int newQuantity) {
        em.createQuery("UPDATE InventoryItem i SET i.quantity = :quantity WHERE i.id = :id")
                .setParameter("quantity", newQuantity)
                .setParameter("id", id)
                .executeUpdate();
    }
}