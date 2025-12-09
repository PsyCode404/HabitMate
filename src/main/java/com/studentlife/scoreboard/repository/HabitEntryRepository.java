package com.studentlife.scoreboard.repository;

import com.studentlife.scoreboard.entity.HabitEntry;
import com.studentlife.scoreboard.entity.HabitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HabitEntryRepository extends JpaRepository<HabitEntry, Long> {
    
    List<HabitEntry> findByDateOrderByDateDesc(LocalDate date);
    
    List<HabitEntry> findByDateBetweenOrderByDateDesc(LocalDate startDate, LocalDate endDate);
    
    List<HabitEntry> findByHabitTypeOrderByDateDesc(HabitType habitType);
    
    @Query("SELECT h FROM HabitEntry h WHERE h.date = :date ORDER BY h.habitType, h.description")
    List<HabitEntry> findByDateOrderByHabitType(@Param("date") LocalDate date);
    
    @Query("SELECT COUNT(h) FROM HabitEntry h WHERE h.date = :date")
    Long countByDate(@Param("date") LocalDate date);
    
    @Query("SELECT AVG(h.score) FROM HabitEntry h WHERE h.date = :date AND h.score IS NOT NULL")
    Double getAverageScoreByDate(@Param("date") LocalDate date);
    
    @Query("SELECT h FROM HabitEntry h WHERE h.habitType = :habitType AND h.date BETWEEN :startDate AND :endDate ORDER BY h.date DESC")
    List<HabitEntry> findByTypeAndDateRange(@Param("habitType") HabitType habitType, 
                                             @Param("startDate") LocalDate startDate, 
                                             @Param("endDate") LocalDate endDate);
}
