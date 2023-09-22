package com.example.runningtracker.service;

import android.location.Location;
import android.os.IInterface;

public interface ICallBack{
    public void locationUpdate(Location location);

    public void distanceUpdate(double distance);
}