package com.rpgbot.database.repositories;

import com.rpgbot.database.models.User;
import java.util.Optional;

public interface UserRepository {
    void save(User user);
    Optional<User> findByChatId(long chatId);
    Optional<User> findByCharacterId(long characterId);
    void delete(User user);
}