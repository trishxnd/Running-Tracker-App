package com.example.runningtracker.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "run_table")
public class Run {

    @PrimaryKey
    @NonNull
    private long timeLogged;

    private String dateLogged;
    private long timeRan;
    private double distance;
    private double averageSpeed;
    private String comment;
//    private byte[] image;

    public Run(@NonNull long timeLogged, String dateLogged, long timeRan, double distance, double averageSpeed, String comment){
        this.timeLogged = timeLogged;
        this.dateLogged = dateLogged;
        this.timeRan = timeRan;
        this.distance = distance;
        this.averageSpeed = averageSpeed;
        this.comment = comment;
//        this.image = image;
    }
    public String getDateLogged() {
        return dateLogged;
    }

    public long getTimeLogged() {
        return timeLogged;
    }

    public long getTimeRan() {
        return timeRan;
    }

    public double getDistance() {
        return distance;
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }

    public String getComment(){return comment;}

//    public byte[] getImage(){return image;}



}
