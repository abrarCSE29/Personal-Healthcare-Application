package com.example.personalhealthcareapplication.model;

public class Schedule {
    private String time; // Format "HH:mm"
    private String dosage;

    public Schedule() { }

    public Schedule(String time, String dosage) {
        this.time = time;
        this.dosage = dosage;
    }

    // Getters and Setters
}