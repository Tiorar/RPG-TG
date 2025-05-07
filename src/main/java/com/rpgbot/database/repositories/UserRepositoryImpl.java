package com.rpgbot.database.repositories;

import com.rpgbot.database.models.User;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Optional;

public class UserRepositoryImpl implements UserRepository {
    private final EntityManager em;

    public UserRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public void save(User user) {
        if (user.getId() == null) {
            em.persist(user);
        } else {
            em.merge(user);
        }
    }

    @Override
    public Optional<User> findByChatId(long chatId) {
        TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u WHERE u.chatId = :chatId", User.class);
        query.setParameter("chatId", chatId);
        return query.getResultStream().findFirst();
    }

    @Override
    public Optional<User> findByCharacterId(long characterId) {
        TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u JOIN u.characters c WHERE c.id = :characterId", User.class);
        query.setParameter("characterId", characterId);
        return query.getResultStream().findFirst();
    }

    @Override
    public void delete(User user) {
        em.remove(user);
    }
}