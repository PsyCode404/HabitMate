package com.studentlife.scoreboard.controller;

import com.studentlife.scoreboard.service.HabitEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
public class HomeController {
    
    @Autowired
    private HabitEntryService habitEntryService;
    
    @GetMapping("/")
    public String home() {
        return "index";
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        LocalDate today = LocalDate.now();
        
        model.addAttribute("today", today);
        model.addAttribute("todayTotalPoints", habitEntryService.getTodayTotalPoints());
        model.addAttribute("weekTotalPoints", habitEntryService.getWeekTotalPoints());
        model.addAttribute("pointsByType", habitEntryService.getPointsByTypeForWeek());
        model.addAttribute("balanceScore", habitEntryService.calculateBalanceScore());
        model.addAttribute("todayEntries", habitEntryService.getTodayEntries());
        model.addAttribute("todayCount", habitEntryService.getTodayEntryCount());
        
        return "dashboard";
    }
    
    @GetMapping("/stats")
    public String stats(Model model) {
        model.addAttribute("weeklyStats", habitEntryService.getWeeklyStats());
        return "stats";
    }
}
