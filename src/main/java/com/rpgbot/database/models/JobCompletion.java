package com.rpgbot.database.models;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_completions")
public class JobCompletion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    private Character character;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    private LocalDateTime completionDate = LocalDateTime.now();

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public Character getCharacter() { return character; }
    public void setCharacter(Character character) { this.character = character; }
    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }
    public LocalDateTime getCompletionDate() { return completionDate; }
    public void setCompletionDate(LocalDateTime completionDate) { this.completionDate = completionDate; }
}