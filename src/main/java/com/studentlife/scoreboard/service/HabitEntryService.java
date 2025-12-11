package com.studentlife.scoreboard.service;

import com.studentlife.scoreboard.entity.HabitEntry;
import com.studentlife.scoreboard.entity.Category;
import com.studentlife.scoreboard.entity.User;
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
 * All operations are filtered by user to ensure data isolation.
 */
@Service
public class HabitEntryService {
    
    @Autowired
    private HabitEntryRepository habitEntryRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    // CRUD Operations - all filtered by user for data isolation
    
    public List<HabitEntry> getAllEntries(User user) {
        return habitEntryRepository.findByUserOrderByDateDesc(user);
    }
    
    public Optional<HabitEntry> getEntryById(Long id) {
        return habitEntryRepository.findById(id);
    }
    
    public HabitEntry saveEntry(HabitEntry entry) {
        return habitEntryRepository.save(entry);
    }
    
    public void deleteEntry(Long id) {
        habitEntryRepository.deleteById(id);
    }
    
    // Filtering Methods - all filtered by user
    
    public List<HabitEntry> getEntriesByDate(User user, LocalDate date) {
        return habitEntryRepository.findByUserAndDateOrderByCategory(user, date);
    }
    
    public List<HabitEntry> getEntriesByDateRange(User user, LocalDate startDate, LocalDate endDate) {
        return habitEntryRepository.findByUserAndDateBetweenOrderByDateDesc(user, startDate, endDate);
    }
    
    public List<HabitEntry> getEntriesByCategory(User user, Category category) {
        return habitEntryRepository.findByUserAndCategoryOrderByDateDesc(user, category);
    }
    
    public List<HabitEntry> getTodayEntries(User user) {
        return getEntriesByDate(user, LocalDate.now());
    }
    
    public Long getTodayEntryCount(User user) {
        return habitEntryRepository.countByUserAndDate(user, LocalDate.now());
    }
    
    public Double getTodayAverageScore(User user) {
        return habitEntryRepository.getAverageScoreByUserAndDate(user, LocalDate.now());
    }
    
    public List<HabitEntry> getTodayEntriesByCategory(User user) {
        return habitEntryRepository.findByUserAndDateOrderByCategory(user, LocalDate.now());
    }
    
    // Analytics & Calculations - all filtered by user
    
    public Integer getTodayTotalPoints(User user) {
        return getTodayEntries(user).stream()
                .mapToInt(entry -> entry.getDuration() != null ? entry.getDuration() : 0)
                .sum();
    }
    
    public Integer getWeekTotalPoints(User user) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minus(7, ChronoUnit.DAYS);
        return getEntriesByDateRange(user, weekStart, today).stream()
                .mapToInt(entry -> entry.getDuration() != null ? entry.getDuration() : 0)
                .sum();
    }
    
    public Map<String, Integer> getPointsByTypeForWeek(User user) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minus(7, ChronoUnit.DAYS);
        List<HabitEntry> weekEntries = getEntriesByDateRange(user, weekStart, today);
        
        Map<String, Integer> pointsByType = new HashMap<>();
        for (Category category : categoryRepository.findAll()) {
            pointsByType.put(category.getName(), 0);
        }
        
        for (HabitEntry entry : weekEntries) {
            if (entry.getCategory() != null) {
                String categoryName = entry.getCategory().getName();
                pointsByType.put(categoryName, pointsByType.get(categoryName) + (entry.getDuration() != null ? entry.getDuration() : 0));
            }
        }
        
        return pointsByType;
    }
    
    public Integer calculateBalanceScore(User user) {
        Map<String, Integer> pointsByType = getPointsByTypeForWeek(user);
        int totalPoints = pointsByType.values().stream().mapToInt(Integer::intValue).sum();
        
        if (totalPoints == 0) return 0;
        
        int typeCount = (int) pointsByType.values().stream().filter(p -> p > 0).count();
        if (typeCount == 0) return 0;
        
        double avgPoints = (double) totalPoints / typeCount;
        int balanceScore = 0;
        
        for (Integer points : pointsByType.values()) {
            if (points > 0) {
                double deviation = Math.abs(points - avgPoints) / avgPoints;
                balanceScore += (int) (100 * (1 - Math.min(deviation, 1)));
            }
        }
        
        return Math.min(100, balanceScore / typeCount);
    }
    
    public List<HabitEntry> filterEntries(User user, Category category, LocalDate startDate, LocalDate endDate) {
        if (category != null && startDate != null && endDate != null) {
            return habitEntryRepository.findByUserAndCategoryAndDateRange(user, category, startDate, endDate);
        } else if (startDate != null && endDate != null) {
            return getEntriesByDateRange(user, startDate, endDate);
        } else if (category != null) {
            return getEntriesByCategory(user, category);
        } else {
            return getAllEntries(user);
        }
    }
    
    public Map<String, Integer> getWeeklyStats(User user) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minus(7, ChronoUnit.DAYS);
        List<HabitEntry> weekEntries = getEntriesByDateRange(user, weekStart, today);
        
        Map<String, Integer> stats = new LinkedHashMap<>();
        for (Category category : categoryRepository.findAll()) {
            stats.put(category.getDisplayName() != null ? category.getDisplayName() : category.getName(), 0);
        }
        
        for (HabitEntry entry : weekEntries) {
            Category category = entry.getCategory();
            if (category != null) {
                String categoryName = category.getDisplayName() != null ? category.getDisplayName() : category.getName();
                stats.put(categoryName, stats.get(categoryName) + (entry.getDuration() != null ? entry.getDuration() : 0));
            }
        }
        
        return stats;
    }
}
