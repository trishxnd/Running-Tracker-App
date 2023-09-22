package com.example.runningtracker.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RunDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Run run);

    @Delete
    void deleteSingleRun(Run run);

    @Query("DELETE FROM run_table")
    void deleteAll();

    @Query(("SELECT * FROM run_table WHERE timeLogged = :time"))
    Run getRunByTimeLogged(long time);

    @Query("SELECT * FROM run_table")
    LiveData<List<Run>> getRuns();

    @Query("SELECT SUM(timeRan) From run_table")
    LiveData<Long> getSumOfTimeRan();

    @Query("SELECT SUM(distance) From run_table")
    LiveData<Double> getSumOfDistance();

    @Query("SELECT AVG(averageSpeed) From run_table")
    LiveData<Double> getMeanAverageSpeed();

    @Query("SELECT * FROM run_table ORDER BY timeLogged DESC")
    LiveData<List<Run>> getRunsByDate();

    @Query("SELECT * FROM run_table ORDER BY timeLogged DESC LIMIT 1")
    LiveData<Run> getRecentRun();
}
