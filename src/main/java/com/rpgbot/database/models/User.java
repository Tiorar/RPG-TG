package com.rpgbot.database.models;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private long chatId;

    private String username;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Character> characters = new ArrayList<>();

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public long getChatId() { return chatId; }
    public void setChatId(long chatId) { this.chatId = chatId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public List<Character> getCharacters() { return characters; }
    public void setCharacters(List<Character> characters) { this.characters = characters; }

    public void addCharacter(Character character) {
        characters.add(character);
        character.setUser(this);
    }

    public void removeCharacter(Character character) {
        characters.remove(character);
        character.setUser(null);
    }
}