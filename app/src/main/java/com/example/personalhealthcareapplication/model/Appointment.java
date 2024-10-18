package com.example.personalhealthcareapplication.model;

public class Appointment {
    private String doctorName;
    private String date;
    private String time;

    public Appointment() { }

    public Appointment(String doctorName, String date, String time) {
        this.doctorName = doctorName;
        this.date = date;
        this.time = time;
    }

    public String getDoctorName() { return doctorName; }
    public String getDate() { return date; }
    public String getTime() { return time; }
}
