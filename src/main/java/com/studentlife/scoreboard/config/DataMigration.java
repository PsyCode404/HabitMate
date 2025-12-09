package com.studentlife.scoreboard.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Component
public class DataMigration implements CommandLineRunner {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Migrate any existing SLEEP entries to NAP
        int updatedCount = entityManager.createNativeQuery(
            "UPDATE habit_entries SET habit_type = 'NAP' WHERE habit_type = 'SLEEP'"
        ).executeUpdate();
        
        if (updatedCount > 0) {
            System.out.println("Migrated " + updatedCount + " entries from SLEEP to NAP");
        }
    }
}
