package com.example.personalhealthcareapplication.model;

import java.util.ArrayList;
import java.util.List;

public class MedicineReminder {
    private String id; // Firestore document ID
    private String medicineName;
    private List<ReminderEntry> reminders; // List of reminder entries

    // No-argument constructor required for Firestore
    public MedicineReminder() {
        this.reminders = new ArrayList<>();
    }

    public MedicineReminder(String medicineName, List<ReminderEntry> reminders) {
        this.medicineName = medicineName;
        this.reminders = reminders != null ? reminders : new ArrayList<>(); // Ensure the list is initialized
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public List<ReminderEntry> getReminders() {
        return reminders;
    }

    public void setReminders(List<ReminderEntry> reminders) {
        this.reminders = reminders;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "MedicineReminder{" +
                "id='" + id + '\'' +
                ", medicineName='" + medicineName + '\'' +
                ", reminders=" + reminders +
                '}';
    }
}
