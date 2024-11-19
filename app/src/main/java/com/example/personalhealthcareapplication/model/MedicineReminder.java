package com.example.personalhealthcareapplication.model;

import kotlin.text.UStringsKt;

public class MedicineReminder {
    private String id;
    private String medicineName;
    private String quantity;
    private long reminderTimeInMillis;

    // No-argument constructor required for Firestore
    public MedicineReminder() {
    }

    public MedicineReminder(String medicineName, String quantity, long reminderTimeInMillis) {
        this.medicineName = medicineName;
        this.quantity = quantity;
        this.reminderTimeInMillis = reminderTimeInMillis;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public long getReminderTimeInMillis() {
        return reminderTimeInMillis;
    }

    public void setReminderTimeInMillis(long reminderTimeInMillis) {
        this.reminderTimeInMillis = reminderTimeInMillis;
    }

    // Optional: helper method to format time if needed
    public String getFormattedTime() {
        return android.text.format.DateFormat.format("hh:mm a", reminderTimeInMillis).toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}