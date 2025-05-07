package com.rpgbot.database.repositories;

import com.rpgbot.database.models.Character;
import com.rpgbot.database.models.Duel;
import com.rpgbot.database.models.DuelChallenge;
import java.util.List;
import java.util.Optional;

public interface DuelRepository {
    void saveChallenge(DuelChallenge challenge);
    Optional<DuelChallenge> findChallengeById(long id);
    List<DuelChallenge> findChallengesByCharacter(Character character);
    void saveDuel(Duel duel);
    Optional<Duel> findActiveDuel(Character character);
    void completeDuel(Duel duel);
    List<Duel> findCompletedDuels(Character character);
}