package com.studentlife.scoreboard.repository;

import com.studentlife.scoreboard.entity.HabitEntry;
import com.studentlife.scoreboard.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for HabitEntry entity.
 * Provides database access and custom query methods for habit entries.
 * Handles filtering, searching, and aggregation operations.
 */
@Repository
public interface HabitEntryRepository extends JpaRepository<HabitEntry, Long> {
    
    // Find all entries for a specific date, ordered by most recent first
    List<HabitEntry> findByDateOrderByDateDesc(LocalDate date);
    
    // Find entries within a date range, ordered by most recent first
    List<HabitEntry> findByDateBetweenOrderByDateDesc(LocalDate startDate, LocalDate endDate);
    
    // Find all entries of a specific category, ordered by most recent first
    List<HabitEntry> findByCategoryOrderByDateDesc(Category category);
    
    // Find entries for a specific date, ordered by category and description
    @Query("SELECT h FROM HabitEntry h WHERE h.date = :date ORDER BY h.category.name, h.description")
    List<HabitEntry> findByDateOrderByCategory(@Param("date") LocalDate date);
    
    // Count total entries for a specific date
    @Query("SELECT COUNT(h) FROM HabitEntry h WHERE h.date = :date")
    Long countByDate(@Param("date") LocalDate date);
    
    // Calculate average score for entries on a specific date
    @Query("SELECT AVG(h.score) FROM HabitEntry h WHERE h.date = :date AND h.score IS NOT NULL")
    Double getAverageScoreByDate(@Param("date") LocalDate date);
    
    // Find entries by category within a date range, ordered by most recent first
    @Query("SELECT h FROM HabitEntry h WHERE h.category = :category AND h.date BETWEEN :startDate AND :endDate ORDER BY h.date DESC")
    List<HabitEntry> findByCategoryAndDateRange(@Param("category") Category category, 
                                                 @Param("startDate") LocalDate startDate, 
                                                 @Param("endDate") LocalDate endDate);
    
    // Find all entries ordered by most recent first
    @Query("SELECT h FROM HabitEntry h ORDER BY h.date DESC")
    List<HabitEntry> findAllOrderByDateDesc();
}
