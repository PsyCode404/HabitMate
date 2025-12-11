package com.studentlife.scoreboard.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Represents a single habit entry recorded by a user.
 * Maps to the habit_entries table in the database.
 * Contains core habit tracking data: type, duration, date, score, and optional metadata.
 */
@Entity
@Table(name = "habit_entries")
public class HabitEntry {
    
    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Habit category (STUDY, EXERCISE, NAP, etc.)
    @NotNull(message = "Category is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    // User-provided description of the habit activity
    @NotBlank(message = "Description is required")
    @Column(nullable = false)
    private String description;
    
    // Date when the habit was performed
    @NotNull(message = "Date is required")
    @Column(nullable = false)
    private LocalDate date;
    
    // Duration in minutes (required, minimum 1)
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Column(nullable = false)
    private Integer duration;
    
    // Optional score rating (1-10)
    private Integer score;
    
    // Optional additional notes about the habit
    private String notes;
    
    // Filename of uploaded image (if any)
    @Column(length = 255)
    private String imageFilename;
    
    // Optional custom label for the habit
    @Column(length = 100)
    private String customLabel;
    
    // Default constructor for JPA
    public HabitEntry() {}
    
    // Constructor with required fields
    public HabitEntry(Category category, String description, LocalDate date, Integer duration) {
        this.category = category;
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
    
    public Category getCategory() {
        return category;
    }
    
    public void setCategory(Category category) {
        this.category = category;
    }
    
    public String getCategoryName() {
        return category != null ? category.getName() : null;
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
