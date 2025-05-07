package com.rpgbot.database.repositories;

import com.rpgbot.database.models.Character;
import com.rpgbot.database.models.User;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class CharacterRepositoryImpl implements CharacterRepository {
    private final EntityManager em;

    public CharacterRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public void save(Character character) {
        if (character.getId() == null) {
            em.persist(character);
        } else {
            em.merge(character);
        }
    }

    @Override
    public Optional<Character> findById(long id) {
        return Optional.ofNullable(em.find(Character.class, id));
    }

    @Override
    public Optional<Character> findByName(String name) {
        TypedQuery<Character> query = em.createQuery(
                "SELECT c FROM Character c WHERE c.name = :name", Character.class);
        query.setParameter("name", name);
        return query.getResultStream().findFirst();
    }

    @Override
    public List<Character> findByUser(User user) {
        TypedQuery<Character> query = em.createQuery(
                "SELECT c FROM Character c WHERE c.user = :user", Character.class);
        query.setParameter("user", user);
        return query.getResultList();
    }

    @Override
    public void delete(Character character) {
        em.remove(character);
    }
}