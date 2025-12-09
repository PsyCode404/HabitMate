package com.studentlife.scoreboard.entity;

public enum HabitType {
    STUDY("Study"),
    EXERCISE("Exercise"),
    NAP("Nap"),
    NUTRITION("Nutrition"),
    SOCIAL("Social"),
    MINDFULNESS("Mindfulness"),
    CREATIVE("Creative"),
    READING("Reading"),
    OTHER("Other");

    private final String displayName;

    HabitType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
