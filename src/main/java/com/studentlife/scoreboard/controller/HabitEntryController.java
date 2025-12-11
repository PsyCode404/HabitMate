package com.studentlife.scoreboard.controller;

import com.studentlife.scoreboard.entity.HabitEntry;
import com.studentlife.scoreboard.entity.Category;
import com.studentlife.scoreboard.service.HabitEntryService;
import com.studentlife.scoreboard.service.FileStorageService;
import com.studentlife.scoreboard.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for habit entry CRUD operations.
 * Handles listing, creating, editing, and deleting habit entries.
 * Manages file uploads and form validation.
 */
@Controller
@RequestMapping("/entries")
public class HabitEntryController {
    
    // Service for habit entry operations
    @Autowired
    private HabitEntryService habitEntryService;
    
    // Service for file upload handling
    @Autowired
    private FileStorageService fileStorageService;
    
    // Repository for category operations
    @Autowired
    private CategoryRepository categoryRepository;
    
    /**
     * Lists all habit entries with optional filtering by category and date range.
     * Supports pagination and filtering parameters.
     */
    @GetMapping
    public String listEntries(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model) {
        
        Category category = null;
        LocalDate start = null;
        LocalDate end = null;
        
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId).orElse(null);
        }
        
        if (startDate != null && !startDate.isEmpty()) {
            start = LocalDate.parse(startDate);
        }
        
        if (endDate != null && !endDate.isEmpty()) {
            end = LocalDate.parse(endDate);
        }
        
        List<HabitEntry> entries = habitEntryService.filterEntries(category, start, end);
        
        model.addAttribute("entries", entries);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedStartDate", startDate);
        model.addAttribute("selectedEndDate", endDate);
        
        return "habits/list";
    }
    
    /**
     * Displays the form for creating a new habit entry.
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("habitEntry", new HabitEntry());
        model.addAttribute("categories", categoryRepository.findAll());
        return "habits/form";
    }
    
    /**
     * Saves a new habit entry after validation.
     * Handles optional file upload and defaults date to today if not provided.
     */
    @PostMapping
    public String saveEntry(@ModelAttribute("habitEntry") HabitEntry habitEntry,
                           BindingResult result,
                           @RequestParam(value = "image", required = false) MultipartFile imageFile,
                           @RequestParam(value = "categoryId", required = false) Long categoryId,
                           Model model) {
        
        // Set category if provided
        if (categoryId != null) {
            categoryRepository.findById(categoryId).ifPresent(habitEntry::setCategory);
        }
        
        // Validate after setting category
        if (habitEntry.getCategory() == null) {
            result.rejectValue("category", "error.category", "Category is required");
        }
        if (habitEntry.getDescription() == null || habitEntry.getDescription().isBlank()) {
            result.rejectValue("description", "error.description", "Description is required");
        }
        if (habitEntry.getDuration() == null || habitEntry.getDuration() < 1) {
            result.rejectValue("duration", "error.duration", "Duration must be at least 1 minute");
        }
        
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            return "habits/form";
        }
        
        // Default to today's date if not provided
        if (habitEntry.getDate() == null) {
            habitEntry.setDate(LocalDate.now());
        }
        
        // Handle optional image upload
        if (imageFile != null && !imageFile.isEmpty()) {
            String filename = fileStorageService.store(imageFile);
            habitEntry.setImageFilename(filename);
        }
        
        habitEntryService.saveEntry(habitEntry);
        return "redirect:/entries";
    }
    
    /**
     * Displays the form for editing an existing habit entry.
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        return habitEntryService.getEntryById(id)
                .map(entry -> {
                    model.addAttribute("habitEntry", entry);
                    model.addAttribute("categories", categoryRepository.findAll());
                    return "habits/form";
                })
                .orElse("redirect:/entries");
    }
    
    /**
     * Updates an existing habit entry after validation.
     * Preserves the existing image if no new image is uploaded.
     */
    @PostMapping("/{id}")
    public String updateEntry(@PathVariable Long id,
                             @ModelAttribute("habitEntry") HabitEntry habitEntry,
                             BindingResult result,
                             @RequestParam(value = "image", required = false) MultipartFile imageFile,
                             @RequestParam(value = "categoryId", required = false) Long categoryId,
                             Model model) {
        
        // Set category if provided
        if (categoryId != null) {
            categoryRepository.findById(categoryId).ifPresent(habitEntry::setCategory);
        }
        
        // Validate after setting category
        if (habitEntry.getCategory() == null) {
            result.rejectValue("category", "error.category", "Category is required");
        }
        if (habitEntry.getDescription() == null || habitEntry.getDescription().isBlank()) {
            result.rejectValue("description", "error.description", "Description is required");
        }
        if (habitEntry.getDuration() == null || habitEntry.getDuration() < 1) {
            result.rejectValue("duration", "error.duration", "Duration must be at least 1 minute");
        }
        
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            return "habits/form";
        }
        
        // Preserve existing image if no new image is uploaded
        HabitEntry existingEntry = habitEntryService.getEntryById(id).orElse(null);
        if (existingEntry != null) {
            if (imageFile != null && !imageFile.isEmpty()) {
                String filename = fileStorageService.store(imageFile);
                habitEntry.setImageFilename(filename);
            } else {
                habitEntry.setImageFilename(existingEntry.getImageFilename());
            }
        }
        
        habitEntryService.saveEntry(habitEntry);
        return "redirect:/entries";
    }
    
    /**
     * Deletes a habit entry by ID.
     */
    @PostMapping("/{id}/delete")
    public String deleteEntry(@PathVariable Long id) {
        habitEntryService.deleteEntry(id);
        return "redirect:/entries";
    }
    
}
