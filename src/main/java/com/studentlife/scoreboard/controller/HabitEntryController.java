package com.studentlife.scoreboard.controller;

import com.studentlife.scoreboard.entity.HabitEntry;
import com.studentlife.scoreboard.entity.Category;
import com.studentlife.scoreboard.entity.User;
import com.studentlife.scoreboard.service.HabitEntryService;
import com.studentlife.scoreboard.service.FileStorageService;
import com.studentlife.scoreboard.repository.CategoryRepository;
import com.studentlife.scoreboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller for habit entry CRUD operations.
 * Handles listing, creating, editing, and deleting habit entries.
 * Ensures all operations are isolated to the current authenticated user.
 */
@Controller
@RequestMapping("/entries")
public class HabitEntryController {
    
    @Autowired
    private HabitEntryService habitEntryService;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Lists all habit entries for the current user with optional filtering.
     * Ensures user data isolation by filtering by current authenticated user.
     */
    @GetMapping
    public String listEntries(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Principal principal,
            Model model) {
        
        // Get current authenticated user
        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
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
        
        // Filter entries by current user only
        List<HabitEntry> entries = habitEntryService.filterEntries(currentUser, category, start, end);
        
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
     * Sets the current user as the owner and handles optional file upload.
     */
    @PostMapping
    public String saveEntry(@ModelAttribute("habitEntry") HabitEntry habitEntry,
                           BindingResult result,
                           @RequestParam(value = "image", required = false) MultipartFile imageFile,
                           @RequestParam(value = "categoryId", required = false) Long categoryId,
                           Principal principal,
                           Model model) {
        
        // Get current authenticated user
        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
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
        
        // Set current user as owner - ensures data isolation
        habitEntry.setUser(currentUser);
        
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
     * Ensures the entry belongs to the current user before allowing edit.
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Principal principal, Model model) {
        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        return habitEntryService.getEntryById(id)
                .map(entry -> {
                    // Safety check: ensure entry belongs to current user
                    if (!entry.getUser().getId().equals(currentUser.getId())) {
                        return "redirect:/entries";
                    }
                    model.addAttribute("habitEntry", entry);
                    model.addAttribute("categories", categoryRepository.findAll());
                    return "habits/form";
                })
                .orElse("redirect:/entries");
    }
    
    /**
     * Updates an existing habit entry after validation.
     * Ensures the entry belongs to the current user before allowing update.
     */
    @PostMapping("/{id}")
    public String updateEntry(@PathVariable Long id,
                             @ModelAttribute("habitEntry") HabitEntry habitEntry,
                             BindingResult result,
                             @RequestParam(value = "image", required = false) MultipartFile imageFile,
                             @RequestParam(value = "categoryId", required = false) Long categoryId,
                             Principal principal,
                             Model model) {
        
        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
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
        
        // Preserve existing image and user if no new image is uploaded
        HabitEntry existingEntry = habitEntryService.getEntryById(id).orElse(null);
        if (existingEntry != null) {
            // Safety check: ensure entry belongs to current user
            if (!existingEntry.getUser().getId().equals(currentUser.getId())) {
                return "redirect:/entries";
            }
            
            if (imageFile != null && !imageFile.isEmpty()) {
                String filename = fileStorageService.store(imageFile);
                habitEntry.setImageFilename(filename);
            } else {
                habitEntry.setImageFilename(existingEntry.getImageFilename());
            }
            
            // Preserve user ownership
            habitEntry.setUser(existingEntry.getUser());
        }
        
        habitEntryService.saveEntry(habitEntry);
        return "redirect:/entries";
    }
    
    /**
     * Deletes a habit entry by ID.
     * Ensures the entry belongs to the current user before allowing deletion.
     */
    @PostMapping("/{id}/delete")
    public String deleteEntry(@PathVariable Long id, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        // Safety check: ensure entry belongs to current user before deleting
        habitEntryService.getEntryById(id).ifPresent(entry -> {
            if (entry.getUser().getId().equals(currentUser.getId())) {
                habitEntryService.deleteEntry(id);
            }
        });
        
        return "redirect:/entries";
    }
    
}
