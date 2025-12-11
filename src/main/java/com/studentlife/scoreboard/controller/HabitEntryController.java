package com.studentlife.scoreboard.controller;

import com.studentlife.scoreboard.entity.HabitEntry;
import com.studentlife.scoreboard.entity.HabitType;
import com.studentlife.scoreboard.service.HabitEntryService;
import com.studentlife.scoreboard.service.FileStorageService;
import jakarta.validation.Valid;
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
    
    /**
     * Lists all habit entries with optional filtering by type and date range.
     * Supports pagination and filtering parameters.
     */
    @GetMapping
    public String listEntries(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model) {
        
        HabitType habitType = null;
        LocalDate start = null;
        LocalDate end = null;
        
        if (type != null && !type.isEmpty()) {
            try {
                habitType = HabitType.valueOf(type);
            } catch (IllegalArgumentException e) {
                // Invalid type, ignore
            }
        }
        
        if (startDate != null && !startDate.isEmpty()) {
            start = LocalDate.parse(startDate);
        }
        
        if (endDate != null && !endDate.isEmpty()) {
            end = LocalDate.parse(endDate);
        }
        
        List<HabitEntry> entries = habitEntryService.filterEntries(habitType, start, end);
        
        model.addAttribute("entries", entries);
        model.addAttribute("habitTypes", HabitType.values());
        model.addAttribute("selectedType", type);
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
        model.addAttribute("habitTypes", HabitType.values());
        return "habits/form";
    }
    
    /**
     * Saves a new habit entry after validation.
     * Handles optional file upload and defaults date to today if not provided.
     */
    @PostMapping
    public String saveEntry(@Valid @ModelAttribute("habitEntry") HabitEntry habitEntry,
                           BindingResult result,
                           @RequestParam(value = "image", required = false) MultipartFile imageFile,
                           Model model) {
        if (result.hasErrors()) {
            model.addAttribute("habitTypes", HabitType.values());
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
                    model.addAttribute("habitTypes", HabitType.values());
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
                             @Valid @ModelAttribute("habitEntry") HabitEntry habitEntry,
                             BindingResult result,
                             @RequestParam(value = "image", required = false) MultipartFile imageFile,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("habitTypes", HabitType.values());
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
    
    /**
     * Adds common attributes to all views (habit types dropdown).
     */
    @ModelAttribute
    public void addCommonAttributes(Model model) {
        model.addAttribute("habitTypes", HabitType.values());
    }
}
