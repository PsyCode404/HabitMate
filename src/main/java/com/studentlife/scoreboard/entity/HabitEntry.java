package com.studentlife.scoreboard.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Table(name = "habit_entries")
public class HabitEntry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Habit type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HabitType habitType;
    
    @NotBlank(message = "Description is required")
    @Column(nullable = false)
    private String description;
    
    @NotNull(message = "Date is required")
    @Column(nullable = false)
    private LocalDate date;
    
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Column(nullable = false)
    private Integer duration; // in minutes
    
    private Integer score; // 1-10 rating
    
    private String notes;
    
    @Column(length = 255)
    private String imageFilename;
    
    @Column(length = 100)
    private String customLabel;
    
    public HabitEntry() {}
    
    public HabitEntry(HabitType habitType, String description, LocalDate date, Integer duration) {
        this.habitType = habitType;
        this.description = description;
        this.date = date;
        this.duration = duration;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public HabitType getHabitType() {
        return habitType;
    }
    
    public void setHabitType(HabitType habitType) {
        this.habitType = habitType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public Integer getDuration() {
        return duration;
    }
    
    public void setDuration(Integer duration) {
        this.duration = duration;
    }
    
    public Integer getScore() {
        return score;
    }
    
    public void setScore(Integer score) {
        this.score = score;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getImageFilename() {
        return imageFilename;
    }
    
    public void setImageFilename(String imageFilename) {
        this.imageFilename = imageFilename;
    }
    
    public String getCustomLabel() {
        return customLabel;
    }
    
    public void setCustomLabel(String customLabel) {
        this.customLabel = customLabel;
    }
}
