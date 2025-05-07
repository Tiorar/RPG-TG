package com.rpgbot.database.repositories;

import com.rpgbot.database.models.Job;
import java.util.List;
import java.util.Optional;

public interface JobRepository {
    void save(Job job);
    Optional<Job> findById(long id);
    List<Job> findByMinLevelLessThanEqual(int maxLevel);
    List<Job> findByCompletedFalse();
    void delete(Job job);
}