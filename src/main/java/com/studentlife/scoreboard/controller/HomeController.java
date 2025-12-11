package com.studentlife.scoreboard.controller;

import com.studentlife.scoreboard.service.HabitEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

/**
 * Controller for home, dashboard, and statistics pages.
 * Handles navigation and aggregates habit data for display.
 * Collects analytics from the service and passes them to templates.
 */
@Controller
public class HomeController {
    
    // Service for habit entry operations
    @Autowired
    private HabitEntryService habitEntryService;
    
    /**
     * Displays the landing/home page.
     */
    @GetMapping("/")
    public String home() {
        return "index";
    }
    
    /**
     * Displays the main dashboard with today's and week's statistics.
     * Includes: today's points, week's points, breakdown by type, balance score, and today's entries.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        LocalDate today = LocalDate.now();
        
        // Aggregate today's metrics
        model.addAttribute("today", today);
        model.addAttribute("todayTotalPoints", habitEntryService.getTodayTotalPoints());
        model.addAttribute("weekTotalPoints", habitEntryService.getWeekTotalPoints());
        model.addAttribute("pointsByType", habitEntryService.getPointsByTypeForWeek());
        model.addAttribute("balanceScore", habitEntryService.calculateBalanceScore());
        model.addAttribute("todayEntries", habitEntryService.getTodayEntries());
        model.addAttribute("todayCount", habitEntryService.getTodayEntryCount());
        
        return "dashboard";
    }
    
    /**
     * Displays weekly statistics page with breakdown by habit type.
     */
    @GetMapping("/stats")
    public String stats(Model model) {
        model.addAttribute("weeklyStats", habitEntryService.getWeeklyStats());
        return "stats";
    }
}
