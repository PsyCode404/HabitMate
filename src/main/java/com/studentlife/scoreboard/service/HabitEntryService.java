package com.studentlife.scoreboard.service;

import com.studentlife.scoreboard.entity.HabitEntry;
import com.studentlife.scoreboard.entity.HabitType;
import com.studentlife.scoreboard.repository.HabitEntryRepository;
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
     * Retrieves all entries of a specific habit type.
     */
    public List<HabitEntry> getEntriesByHabitType(HabitType habitType) {
        return habitEntryRepository.findByHabitTypeOrderByDateDesc(habitType);
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
     * Retrieves today's entries grouped by habit type.
     */
    public List<HabitEntry> getTodayEntriesByType() {
        return habitEntryRepository.findByDateOrderByHabitType(LocalDate.now());
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
     * Calculates points breakdown by habit type for the past 7 days.
     * Returns a map of habit type names to their total duration.
     */
    public Map<String, Integer> getPointsByTypeForWeek() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minus(7, ChronoUnit.DAYS);
        List<HabitEntry> weekEntries = getEntriesByDateRange(weekStart, today);
        
        // Initialize map with all habit types
        Map<String, Integer> pointsByType = new HashMap<>();
        for (HabitType type : HabitType.values()) {
            pointsByType.put(type.getDisplayName(), 0);
        }
        
        // Accumulate points by type
        for (HabitEntry entry : weekEntries) {
            String typeName = entry.getHabitType().getDisplayName();
            pointsByType.put(typeName, pointsByType.get(typeName) + (entry.getDuration() != null ? entry.getDuration() : 0));
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
     * Filters entries by optional habit type and/or date range.
     * Returns all entries if no filters are provided.
     */
    public List<HabitEntry> filterEntries(HabitType habitType, LocalDate startDate, LocalDate endDate) {
        if (habitType != null && startDate != null && endDate != null) {
            return habitEntryRepository.findByTypeAndDateRange(habitType, startDate, endDate);
        } else if (startDate != null && endDate != null) {
            return getEntriesByDateRange(startDate, endDate);
        } else if (habitType != null) {
            return getEntriesByHabitType(habitType);
        } else {
            return getAllEntries();
        }
    }
    
    /**
     * Generates weekly statistics: total duration per habit type for the past 7 days.
     * Returns a map of habit types to their total duration.
     */
    public Map<HabitType, Integer> getWeeklyStats() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minus(7, ChronoUnit.DAYS);
        List<HabitEntry> weekEntries = getEntriesByDateRange(weekStart, today);
        
        // Initialize map with all habit types
        Map<HabitType, Integer> stats = new LinkedHashMap<>();
        for (HabitType type : HabitType.values()) {
            stats.put(type, 0);
        }
        
        // Accumulate duration by type
        for (HabitEntry entry : weekEntries) {
            HabitType type = entry.getHabitType();
            stats.put(type, stats.get(type) + (entry.getDuration() != null ? entry.getDuration() : 0));
        }
        
        return stats;
    }
}
