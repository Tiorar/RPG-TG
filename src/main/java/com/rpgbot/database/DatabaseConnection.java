package com.rpgbot.database;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

public class DatabaseConnection {
    private static EntityManagerFactory emf;

    public static void initialize(String dbUrl, String dbUser, String dbPassword) {
        Map<String, String> properties = new HashMap<>();
        properties.put("javax.persistence.jdbc.url", dbUrl);
        properties.put("javax.persistence.jdbc.user", dbUser);
        properties.put("javax.persistence.jdbc.password", dbPassword);
        properties.put("javax.persistence.jdbc.driver", "org.postgresql.Driver");

        emf = Persistence.createEntityManagerFactory("rpgbot-pu", properties);
    }

    public static EntityManager createEntityManager() {
        if (emf == null) {
            throw new IllegalStateException("Database connection not initialized");
        }
        return emf.createEntityManager();
    }

    public static void close() {
        if (emf != null) {
            emf.close();
        }
    }
}