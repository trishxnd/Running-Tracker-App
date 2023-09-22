package com.example.runningtracker.db;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class RunViewModel extends AndroidViewModel {

    private Repository repository;
    private LiveData<List<Run>> allRuns;
    private LiveData<Long> totalTimeRan;
    private LiveData<Double> totalDistanceRan;
    private LiveData<Double> meanAverageSpeed;
    private LiveData<List<Run>> runsSortedByDate;
    private LiveData<Run> mostRecentRun;


    public RunViewModel(@NonNull Application application) {
        super(application);
        repository = new Repository(application);

        allRuns = repository.getAllRuns();
        runsSortedByDate = repository.getRunsByDate();
        totalTimeRan = repository.getTotalTimeRan();
        totalDistanceRan = repository.getTotalDistance();
        meanAverageSpeed = repository.getMeanAverageSpeed();
        mostRecentRun = repository.getMostRecentRun();


    }

    public LiveData<List<Run>> getAllRuns(){return allRuns;}
    public LiveData<List<Run>> getRunsByDate(){return runsSortedByDate;}
    public LiveData<Long> getTotalTimeRan(){return totalTimeRan;}
    public LiveData<Double> getTotalDistance(){return totalDistanceRan;}
    public LiveData<Double> getMeanAverageSpeed(){return meanAverageSpeed;}
    public LiveData<Run> getMostRecentRun(){return mostRecentRun;}


    public void insert (Run run){
        repository.insert(run);
    }

    public void deleteSingleRUn(Run run){repository.deleteSingleRun(run);}
    public void deleteAll(){
        repository.deleteAll();
    }

}
