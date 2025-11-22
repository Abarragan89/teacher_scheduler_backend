package com.mathfactmissions.teacherscheduler.model;

import java.time.LocalDate;

public class Holiday {
    private final String name;
    private final LocalDate date;
    private final String emoji;

    public Holiday(String name, LocalDate date, String emoji) {
        this.name = name;
        this.date = date;
        this.emoji = emoji;
    }

    public String getName() {
        return name;
    }

    public String getEmoji() {
        return emoji;
    }

    public LocalDate getDate() {
        return date;
    }
}

