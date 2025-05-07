package com.rpgbot.database.repositories;

import com.rpgbot.database.models.Character;
import com.rpgbot.database.models.User;
import java.util.List;
import java.util.Optional;

public interface CharacterRepository {
    void save(Character character);
    Optional<Character> findById(long id);
    Optional<Character> findByName(String name);
    List<Character> findByUser(User user);
    void delete(Character character);
}