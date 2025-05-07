package com.rpgbot.database.repositories;

import com.rpgbot.database.models.Job;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class JobRepositoryImpl implements JobRepository {
    private final EntityManager em;

    public JobRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public void save(Job job) {
        if (job.getId() == null) {
            em.persist(job);
        } else {
            em.merge(job);
        }
    }

    @Override
    public Optional<Job> findById(long id) {
        return Optional.ofNullable(em.find(Job.class, id));
    }

    @Override
    public List<Job> findByMinLevelLessThanEqual(int maxLevel) {
        TypedQuery<Job> query = em.createQuery(
                "SELECT j FROM Job j WHERE j.minLevel <= :maxLevel", Job.class);
        query.setParameter("maxLevel", maxLevel);
        return query.getResultList();
    }

    @Override
    public List<Job> findByCompletedFalse() {
        return em.createQuery(
                        "SELECT j FROM Job j WHERE j.completed = false", Job.class)
                .getResultList();
    }

    @Override
    public void delete(Job job) {
        em.remove(job);
    }
}