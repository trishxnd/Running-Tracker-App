package com.example.runningtracker.db;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class Repository {

    private RunDao runDao;
    private LiveData<List<Run>> allRuns;
    private LiveData<List<Run>> runsSortedByDate;
    private LiveData<Long> totalTimeRan;
    private LiveData<Double> totalDistanceRan;
    private LiveData<Double> meanAverageSpeed;
    private LiveData<Run> mostRecentRun;

    public Repository(Application application){

        MyDatabase db = MyDatabase.getDatabase(application);
        runDao = db.runDao();

        allRuns = runDao.getRuns();
        runsSortedByDate = runDao.getRunsByDate();
        totalTimeRan = runDao.getSumOfTimeRan();
        totalDistanceRan = runDao.getSumOfDistance();
        meanAverageSpeed = runDao.getMeanAverageSpeed();
        mostRecentRun = runDao.getRecentRun();
    }

    LiveData<List<Run>> getAllRuns(){
        return allRuns;
    }
    LiveData<Long> getTotalTimeRan(){return totalTimeRan;}
    LiveData<Double> getTotalDistance(){return totalDistanceRan;}
    LiveData<Double> getMeanAverageSpeed(){return meanAverageSpeed;}
    LiveData<List<Run>> getRunsByDate(){return runsSortedByDate;}
    LiveData<Run> getMostRecentRun(){return mostRecentRun;}

    public void insert(Run run){
        MyDatabase.databaseWriteExecutor.execute(() ->{
           runDao.insert(run);
        });
    }

    public void deleteAll(){
        MyDatabase.databaseWriteExecutor.execute(() ->{
            runDao.deleteAll();
        });
    }

    public void deleteSingleRun(Run run){
        MyDatabase.databaseWriteExecutor.execute(() ->{
            runDao.deleteSingleRun(run);
        });
    }
}
