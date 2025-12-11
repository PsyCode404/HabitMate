package com.studentlife.scoreboard.repository;

import com.studentlife.scoreboard.entity.HabitEntry;
import com.studentlife.scoreboard.entity.Category;
import com.studentlife.scoreboard.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for HabitEntry entity.
 * Provides database access and custom query methods for habit entries.
 * All queries are filtered by user to ensure data isolation.
 */
@Repository
public interface HabitEntryRepository extends JpaRepository<HabitEntry, Long> {
    
    // User-isolated queries - all entries must belong to the specified user
    
    // Find all entries for a specific user, ordered by most recent first
    List<HabitEntry> findByUserOrderByDateDesc(User user);
    
    // Find entries for a user within a date range, ordered by most recent first
    List<HabitEntry> findByUserAndDateBetweenOrderByDateDesc(User user, LocalDate startDate, LocalDate endDate);
    
    // Find all entries of a specific user and category, ordered by most recent first
    List<HabitEntry> findByUserAndCategoryOrderByDateDesc(User user, Category category);
    
    // Find entries for a user by category within a date range
    @Query("SELECT h FROM HabitEntry h WHERE h.user = :user AND h.category = :category AND h.date BETWEEN :startDate AND :endDate ORDER BY h.date DESC")
    List<HabitEntry> findByUserAndCategoryAndDateRange(@Param("user") User user, 
                                                        @Param("category") Category category, 
                                                        @Param("startDate") LocalDate startDate, 
                                                        @Param("endDate") LocalDate endDate);
    
    // Find entries for a user on a specific date, ordered by category and description
    @Query("SELECT h FROM HabitEntry h WHERE h.user = :user AND h.date = :date ORDER BY h.category.name, h.description")
    List<HabitEntry> findByUserAndDateOrderByCategory(@Param("user") User user, @Param("date") LocalDate date);
    
    // Count total entries for a user on a specific date
    @Query("SELECT COUNT(h) FROM HabitEntry h WHERE h.user = :user AND h.date = :date")
    Long countByUserAndDate(@Param("user") User user, @Param("date") LocalDate date);
    
    // Calculate average score for a user's entries on a specific date
    @Query("SELECT AVG(h.score) FROM HabitEntry h WHERE h.user = :user AND h.date = :date AND h.score IS NOT NULL")
    Double getAverageScoreByUserAndDate(@Param("user") User user, @Param("date") LocalDate date);
}
