package com.studentlife.scoreboard.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
public class CurrentUserControllerAdvice {
    
    @ModelAttribute("currentUsername")
    public String currentUsername(Principal principal) {
        return principal != null ? principal.getName() : null;
    }
}
