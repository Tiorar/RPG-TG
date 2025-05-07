package com.rpgbot.database.repositories;

import com.rpgbot.database.models.Item;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class ItemRepositoryImpl implements ItemRepository {
    private final EntityManager em;

    public ItemRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public void save(Item item) {
        if (item.getId() == null) {
            em.persist(item);
        } else {
            em.merge(item);
        }
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(em.find(Item.class, id));
    }

    @Override
    public List<Item> findAll() {
        return em.createQuery("SELECT i FROM Item i", Item.class)
                .getResultList();
    }

    @Override
    public List<Item> findByRarity(String rarity) {
        return em.createQuery(
                        "SELECT i FROM Item i WHERE i.rarity = :rarity", Item.class)
                .setParameter("rarity", rarity)
                .getResultList();
    }

    @Override
    public void delete(Item item) {
        em.remove(item);
    }
}