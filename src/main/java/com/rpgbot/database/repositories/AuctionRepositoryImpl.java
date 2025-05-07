package com.rpgbot.database.repositories;

import com.rpgbot.database.models.AuctionLot;
import com.rpgbot.database.models.Character;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

public class AuctionRepositoryImpl implements AuctionRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void save(AuctionLot lot) {
        if (lot.getId() == null) {
            entityManager.persist(lot);
        } else {
            entityManager.merge(lot);
        }
    }

    @Override
    public AuctionLot findById(Long id) {
        return entityManager.find(AuctionLot.class, id);
    }

    @Override
    public List<AuctionLot> findBySeller(Character seller) {
        TypedQuery<AuctionLot> query = entityManager.createQuery(
                "SELECT a FROM AuctionLot a WHERE a.seller = :seller", AuctionLot.class);
        query.setParameter("seller", seller);
        return query.getResultList();
    }

    @Override
    public List<AuctionLot> findActiveLots() {
        TypedQuery<AuctionLot> query = entityManager.createQuery(
                "SELECT a FROM AuctionLot a WHERE a.expiresAt > :now AND a.sold = false", AuctionLot.class);
        query.setParameter("now", LocalDateTime.now());
        return query.getResultList();
    }

    @Override
    public List<AuctionLot> findExpiredLots() {
        TypedQuery<AuctionLot> query = entityManager.createQuery(
                "SELECT a FROM AuctionLot a WHERE a.expiresAt <= :now AND a.sold = false", AuctionLot.class);
        query.setParameter("now", LocalDateTime.now());
        return query.getResultList();
    }

    @Override
    public void delete(AuctionLot lot) {
        entityManager.remove(lot);
    }
}
