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

@Service
public class HabitEntryService {
    
    @Autowired
    private HabitEntryRepository habitEntryRepository;
    
    public List<HabitEntry> getAllEntries() {
        return habitEntryRepository.findAll();
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
    
    public List<HabitEntry> getEntriesByDate(LocalDate date) {
        return habitEntryRepository.findByDateOrderByDateDesc(date);
    }
    
    public List<HabitEntry> getEntriesByDateRange(LocalDate startDate, LocalDate endDate) {
        return habitEntryRepository.findByDateBetweenOrderByDateDesc(startDate, endDate);
    }
    
    public List<HabitEntry> getEntriesByHabitType(HabitType habitType) {
        return habitEntryRepository.findByHabitTypeOrderByDateDesc(habitType);
    }
    
    public List<HabitEntry> getTodayEntries() {
        return getEntriesByDate(LocalDate.now());
    }
    
    public Long getTodayEntryCount() {
        return habitEntryRepository.countByDate(LocalDate.now());
    }
    
    public Double getTodayAverageScore() {
        return habitEntryRepository.getAverageScoreByDate(LocalDate.now());
    }
    
    public List<HabitEntry> getTodayEntriesByType() {
        return habitEntryRepository.findByDateOrderByHabitType(LocalDate.now());
    }
    
    public Integer getTodayTotalPoints() {
        return getTodayEntries().stream()
                .mapToInt(entry -> entry.getDuration() != null ? entry.getDuration() : 0)
                .sum();
    }
    
    public Integer getWeekTotalPoints() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minus(7, ChronoUnit.DAYS);
        return getEntriesByDateRange(weekStart, today).stream()
                .mapToInt(entry -> entry.getDuration() != null ? entry.getDuration() : 0)
                .sum();
    }
    
    public Map<String, Integer> getPointsByTypeForWeek() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minus(7, ChronoUnit.DAYS);
        List<HabitEntry> weekEntries = getEntriesByDateRange(weekStart, today);
        
        Map<String, Integer> pointsByType = new HashMap<>();
        for (HabitType type : HabitType.values()) {
            pointsByType.put(type.getDisplayName(), 0);
        }
        
        for (HabitEntry entry : weekEntries) {
            String typeName = entry.getHabitType().getDisplayName();
            pointsByType.put(typeName, pointsByType.get(typeName) + (entry.getDuration() != null ? entry.getDuration() : 0));
        }
        
        return pointsByType;
    }
    
    public Integer calculateBalanceScore() {
        Map<String, Integer> pointsByType = getPointsByTypeForWeek();
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
    
    public Map<HabitType, Integer> getWeeklyStats() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minus(7, ChronoUnit.DAYS);
        List<HabitEntry> weekEntries = getEntriesByDateRange(weekStart, today);
        
        Map<HabitType, Integer> stats = new LinkedHashMap<>();
        for (HabitType type : HabitType.values()) {
            stats.put(type, 0);
        }
        
        for (HabitEntry entry : weekEntries) {
            HabitType type = entry.getHabitType();
            stats.put(type, stats.get(type) + (entry.getDuration() != null ? entry.getDuration() : 0));
        }
        
        return stats;
    }
}
