package com.studentlife.scoreboard.controller;

import com.studentlife.scoreboard.entity.User;
import com.studentlife.scoreboard.service.HabitEntryService;
import com.studentlife.scoreboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.time.LocalDate;

/**
 * Controller for home, dashboard, and statistics pages.
 * Handles navigation and aggregates habit data for display.
 * All dashboard data is filtered by the current authenticated user.
 */
@Controller
public class HomeController {
    
    @Autowired
    private HabitEntryService habitEntryService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Displays the landing/home page.
     */
    @GetMapping("/")
    public String home() {
        return "index";
    }
    
    /**
     * Displays the main dashboard with today's and week's statistics.
     * All metrics are filtered for the current authenticated user only.
     */
    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        // Get current authenticated user
        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        LocalDate today = LocalDate.now();
        
        // Aggregate today's metrics - filtered by current user
        model.addAttribute("today", today);
        model.addAttribute("todayTotalPoints", habitEntryService.getTodayTotalPoints(currentUser));
        model.addAttribute("weekTotalPoints", habitEntryService.getWeekTotalPoints(currentUser));
        model.addAttribute("pointsByType", habitEntryService.getPointsByTypeForWeek(currentUser));
        model.addAttribute("balanceScore", habitEntryService.calculateBalanceScore(currentUser));
        model.addAttribute("todayEntries", habitEntryService.getTodayEntries(currentUser));
        model.addAttribute("todayCount", habitEntryService.getTodayEntryCount(currentUser));
        
        return "dashboard";
    }
    
    /**
     * Displays weekly statistics page with breakdown by habit type.
     * All statistics are filtered for the current authenticated user only.
     */
    @GetMapping("/stats")
    public String stats(Principal principal, Model model) {
        // Get current authenticated user
        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        // Weekly stats filtered by current user
        model.addAttribute("weeklyStats", habitEntryService.getWeeklyStats(currentUser));
        return "stats";
    }
}
