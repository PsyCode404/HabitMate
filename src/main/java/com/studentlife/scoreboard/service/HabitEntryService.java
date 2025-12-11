package com.studentlife.scoreboard.service;

import com.studentlife.scoreboard.entity.HabitEntry;
import com.studentlife.scoreboard.entity.Category;
import com.studentlife.scoreboard.repository.HabitEntryRepository;
import com.studentlife.scoreboard.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service layer for habit entry management.
 * Handles business logic for CRUD operations, filtering, and analytics calculations.
 * Provides methods for retrieving entries, calculating scores, and generating statistics.
 */
@Service
public class HabitEntryService {
    
    // Repository for database access
    @Autowired
    private HabitEntryRepository habitEntryRepository;
    
    // Repository for category access
    @Autowired
    private CategoryRepository categoryRepository;
    
    // CRUD Operations
    
    /**
     * Retrieves all habit entries ordered by most recent first.
     */
    public List<HabitEntry> getAllEntries() {
        return habitEntryRepository.findAllOrderByDateDesc();
    }
    
    /**
     * Retrieves a single habit entry by ID.
     */
    public Optional<HabitEntry> getEntryById(Long id) {
        return habitEntryRepository.findById(id);
    }
    
    /**
     * Saves a new or updated habit entry to the database.
     */
    public HabitEntry saveEntry(HabitEntry entry) {
        return habitEntryRepository.save(entry);
    }
    
    /**
     * Deletes a habit entry by ID.
     */
    public void deleteEntry(Long id) {
        habitEntryRepository.deleteById(id);
    }
    
    // Filtering Methods
    
    /**
     * Retrieves all entries for a specific date.
     */
    public List<HabitEntry> getEntriesByDate(LocalDate date) {
        return habitEntryRepository.findByDateOrderByDateDesc(date);
    }
    
    /**
     * Retrieves entries within a date range.
     */
    public List<HabitEntry> getEntriesByDateRange(LocalDate startDate, LocalDate endDate) {
        return habitEntryRepository.findByDateBetweenOrderByDateDesc(startDate, endDate);
    }
    
    /**
     * Retrieves all entries of a specific category.
     */
    public List<HabitEntry> getEntriesByCategory(Category category) {
        return habitEntryRepository.findByCategoryOrderByDateDesc(category);
    }
    
    /**
     * Retrieves all entries recorded today.
     */
    public List<HabitEntry> getTodayEntries() {
        return getEntriesByDate(LocalDate.now());
    }
    
    /**
     * Counts total entries recorded today.
     */
    public Long getTodayEntryCount() {
        return habitEntryRepository.countByDate(LocalDate.now());
    }
    
    /**
     * Calculates average score for entries recorded today.
     */
    public Double getTodayAverageScore() {
        return habitEntryRepository.getAverageScoreByDate(LocalDate.now());
    }
    
    /**
     * Retrieves today's entries grouped by category.
     */
    public List<HabitEntry> getTodayEntriesByCategory() {
        return habitEntryRepository.findByDateOrderByCategory(LocalDate.now());
    }
    
    // Analytics & Calculations
    
    /**
     * Calculates total points (duration in minutes) for entries recorded today.
     */
    public Integer getTodayTotalPoints() {
        return getTodayEntries().stream()
                .mapToInt(entry -> entry.getDuration() != null ? entry.getDuration() : 0)
                .sum();
    }
    
    /**
     * Calculates total points (duration in minutes) for the past 7 days.
     */
    public Integer getWeekTotalPoints() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minus(7, ChronoUnit.DAYS);
        return getEntriesByDateRange(weekStart, today).stream()
                .mapToInt(entry -> entry.getDuration() != null ? entry.getDuration() : 0)
                .sum();
    }
    
    /**
     * Calculates points breakdown by category for the past 7 days.
     * Returns a map of category names to their total duration.
     */
    public Map<String, Integer> getPointsByTypeForWeek() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minus(7, ChronoUnit.DAYS);
        List<HabitEntry> weekEntries = getEntriesByDateRange(weekStart, today);
        
        // Initialize map with all categories
        Map<String, Integer> pointsByType = new HashMap<>();
        for (Category category : categoryRepository.findAll()) {
            pointsByType.put(category.getName(), 0);
        }
        
        // Accumulate points by category
        for (HabitEntry entry : weekEntries) {
            if (entry.getCategory() != null) {
                String categoryName = entry.getCategory().getName();
                pointsByType.put(categoryName, pointsByType.get(categoryName) + (entry.getDuration() != null ? entry.getDuration() : 0));
            }
        }
        
        return pointsByType;
    }
    
    /**
     * Calculates a balance score (0-100) indicating how evenly distributed
     * habit time is across different habit types in the past 7 days.
     * Higher score = more balanced distribution.
     */
    public Integer calculateBalanceScore() {
        Map<String, Integer> pointsByType = getPointsByTypeForWeek();
        int totalPoints = pointsByType.values().stream().mapToInt(Integer::intValue).sum();
        
        if (totalPoints == 0) return 0;
        
        int typeCount = (int) pointsByType.values().stream().filter(p -> p > 0).count();
        if (typeCount == 0) return 0;
        
        double avgPoints = (double) totalPoints / typeCount;
        int balanceScore = 0;
        
        // Calculate deviation from average for each type
        for (Integer points : pointsByType.values()) {
            if (points > 0) {
                double deviation = Math.abs(points - avgPoints) / avgPoints;
                balanceScore += (int) (100 * (1 - Math.min(deviation, 1)));
            }
        }
        
        return Math.min(100, balanceScore / typeCount);
    }
    
    /**
     * Filters entries by optional category and/or date range.
     * Returns all entries if no filters are provided.
     */
    public List<HabitEntry> filterEntries(Category category, LocalDate startDate, LocalDate endDate) {
        if (category != null && startDate != null && endDate != null) {
            return habitEntryRepository.findByCategoryAndDateRange(category, startDate, endDate);
        } else if (startDate != null && endDate != null) {
            return getEntriesByDateRange(startDate, endDate);
        } else if (category != null) {
            return getEntriesByCategory(category);
        } else {
            return getAllEntries();
        }
    }
    
    /**
     * Generates weekly statistics: total duration per category for the past 7 days.
     * Returns a map of categories to their total duration.
     */
    public Map<Category, Integer> getWeeklyStats() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minus(7, ChronoUnit.DAYS);
        List<HabitEntry> weekEntries = getEntriesByDateRange(weekStart, today);
        
        // Initialize map with all categories
        Map<Category, Integer> stats = new LinkedHashMap<>();
        for (Category category : categoryRepository.findAll()) {
            stats.put(category, 0);
        }
        
        // Accumulate duration by category
        for (HabitEntry entry : weekEntries) {
            Category category = entry.getCategory();
            if (category != null) {
                stats.put(category, stats.get(category) + (entry.getDuration() != null ? entry.getDuration() : 0));
            }
        }
        
        return stats;
    }
}
