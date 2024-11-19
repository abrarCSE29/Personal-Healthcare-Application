package com.example.personalhealthcareapplication.viewmodel;

import android.app.AlarmManager;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.personalhealthcareapplication.model.MedicineReminder;
import com.example.personalhealthcareapplication.notifications.MedicineNotificationReceiver;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MedicineReminderViewModel extends AndroidViewModel {
    private final FirebaseFirestore db;
    private final String userId;

    public MedicineReminderViewModel(@NonNull Application application) {
        super(application);
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        System.out.println(userId);
    }

    public void scheduleMedicineReminder(Context context, MedicineReminder reminder) {
        Intent intent = new Intent(context, MedicineNotificationReceiver.class);
        intent.putExtra("medicineName", reminder.getMedicineName());
        intent.putExtra("quantity", reminder.getQuantity());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) reminder.getReminderTimeInMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminder.getReminderTimeInMillis(),
                    pendingIntent
            );
        }
    }

    public void saveReminder(MedicineReminder reminder) {
        db.collection("Users")
                .document(userId)
                .collection("MedicineReminders")
                .add(reminder)
                .addOnSuccessListener(documentReference -> {
                    reminder.setId(documentReference.getId());
                    System.out.println("Reminder saved with ID: " + reminder.getId());
                })
                .addOnFailureListener(e -> {
                    System.out.println("Failed to save reminder: " + e.getMessage());
                });
    }

    public Task<List<MedicineReminder>> getReminders() {
        return db.collection("Users")
                .document(userId)
                .collection("MedicineReminders")
                .get()
                .continueWith(task -> {
                    List<MedicineReminder> reminders = new ArrayList<>();
                    for (DocumentSnapshot document : task.getResult()) {
                        MedicineReminder reminder = document.toObject(MedicineReminder.class);
                        if (reminder != null) {
                            reminder.setId(document.getId()); // Set the document ID
                            reminders.add(reminder);
                        }
                    }
                    return reminders;
                });
    }

    // New method to delete a reminder from Firebase Firestore
    public void deleteReminder(MedicineReminder reminder) {
        if (reminder == null || reminder.getId() == null) {
            System.out.println("Cannot delete reminder: Invalid reminder or ID is null");
            return;
        }
        db.collection("Users")
                .document(userId)
                .collection("MedicineReminders")
                .document(reminder.getId()) // Assuming `getId()` provides the document ID of the reminder
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Successfully deleted reminder
                    System.out.println("Deleted");
                })
                .addOnFailureListener(e -> {
                    // Handle deletion failure
                });
        System.out.println(reminder.getId());
    }

    // Method to cancel the reminder's notification and alarm
    public void cancelMedicineReminder(Context context, MedicineReminder reminder) {
        // Cancel the alarm
        Intent intent = new Intent(context, MedicineNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) reminder.getReminderTimeInMillis(),
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
            }
            pendingIntent.cancel();
        }

        // Cancel the notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel((int) reminder.getReminderTimeInMillis()); // Using reminder time as unique ID
        }
    }
    public Task<MedicineReminder> getUpcomingReminder() {
        return db.collection("Users")
                .document(userId)
                .collection("MedicineReminders")
                .orderBy("reminderTimeInMillis") // Order reminders by time
                .limit(1) // Fetch only the next upcoming reminder
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        MedicineReminder reminder = document.toObject(MedicineReminder.class);
                        if (reminder != null) {
                            reminder.setId(document.getId());
                        }
                        return reminder;
                    }
                    return null;
                });
    }
}
