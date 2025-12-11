package com.studentlife.scoreboard.component;

import com.studentlife.scoreboard.entity.Category;
import com.studentlife.scoreboard.entity.HabitEntry;
import com.studentlife.scoreboard.repository.CategoryRepository;
import com.studentlife.scoreboard.repository.HabitEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataMigrationComponent implements CommandLineRunner {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private HabitEntryRepository habitEntryRepository;
    
    @Override
    public void run(String... args) throws Exception {
        try {
            seedDefaultCategories();
            migrateExistingData();
        } catch (Exception e) {
            System.err.println("Error during data migration: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void seedDefaultCategories() {
        List<String[]> defaultCategories = Arrays.asList(
            new String[]{"STUDY", "Study"},
            new String[]{"EXERCISE", "Exercise"},
            new String[]{"NAP", "Nap"},
            new String[]{"NUTRITION", "Nutrition"},
            new String[]{"SOCIAL", "Social"},
            new String[]{"MINDFULNESS", "Mindfulness"},
            new String[]{"CREATIVE", "Creative"},
            new String[]{"READING", "Reading"},
            new String[]{"OTHER", "Other"}
        );
        
        for (String[] catData : defaultCategories) {
            if (categoryRepository.findByName(catData[0]).isEmpty()) {
                Category category = new Category(catData[0], catData[1]);
                categoryRepository.save(category);
            }
        }
    }
    
    private void migrateExistingData() {
        try {
            List<HabitEntry> entries = habitEntryRepository.findAll();
            
            for (HabitEntry entry : entries) {
                try {
                    if (entry.getCategory() == null) {
                        categoryRepository.findByName("STUDY").ifPresent(entry::setCategory);
                        habitEntryRepository.save(entry);
                    }
                } catch (Exception e) {
                    System.err.println("Error migrating entry " + entry.getId() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error during data migration: " + e.getMessage());
        }
    }
}
