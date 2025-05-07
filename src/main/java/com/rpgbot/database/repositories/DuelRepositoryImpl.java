package com.rpgbot.database.repositories;

import com.rpgbot.database.models.Character;
import com.rpgbot.database.models.Duel;
import com.rpgbot.database.models.DuelChallenge;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class DuelRepositoryImpl implements DuelRepository {
    private final EntityManager em;

    public DuelRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public void saveChallenge(DuelChallenge challenge) {
        if (challenge.getId() == null) {
            em.persist(challenge);
        } else {
            em.merge(challenge);
        }
    }

    @Override
    public Optional<DuelChallenge> findChallengeById(long id) {
        return Optional.ofNullable(em.find(DuelChallenge.class, id));
    }

    @Override
    public List<DuelChallenge> findChallengesByCharacter(Character character) {
        return em.createQuery(
                        "SELECT dc FROM DuelChallenge dc WHERE dc.challenger = :character OR dc.opponent = :character",
                        DuelChallenge.class)
                .setParameter("character", character)
                .getResultList();
    }

    @Override
    public void saveDuel(Duel duel) {
        if (duel.getId() == null) {
            em.persist(duel);
        } else {
            em.merge(duel);
        }
    }

    @Override
    public Optional<Duel> findActiveDuel(Character character) {
        TypedQuery<Duel> query = em.createQuery(
                "SELECT d FROM Duel d WHERE (d.challenger = :character OR d.opponent = :character) AND d.completed = false",
                Duel.class);
        query.setParameter("character", character);
        return query.getResultStream().findFirst();
    }

    @Override
    public void completeDuel(Duel duel) {
        duel.setCompleted(true);
        em.merge(duel);
    }

    @Override
    public List<Duel> findCompletedDuels(Character character) {
        return em.createQuery(
                        "SELECT d FROM Duel d WHERE (d.challenger = :character OR d.opponent = :character) AND d.completed = true",
                        Duel.class)
                .setParameter("character", character)
                .getResultList();
    }
}
