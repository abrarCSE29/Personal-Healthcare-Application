package com.example.personalhealthcareapplication.model;

public class ReminderEntry {
    private String quantity; // Quantity of the medicine
    private long reminderTimeInMillis; // Reminder time in milliseconds

    public ReminderEntry() {
    }

    public ReminderEntry(String quantity, long reminderTimeInMillis) {
        this.quantity = quantity;
        this.reminderTimeInMillis = reminderTimeInMillis;
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

    public String getFormattedTime() {
        return android.text.format.DateFormat.format("hh:mm a", reminderTimeInMillis).toString();
    }

    @Override
    public String toString() {
        return "ReminderEntry{" +
                "quantity='" + quantity + '\'' +
                ", reminderTimeInMillis=" + reminderTimeInMillis +
                '}';
    }
}
