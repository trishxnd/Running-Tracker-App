package com.example.runningtracker.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.runningtracker.R;
import com.example.runningtracker.db.Run;
import com.example.runningtracker.db.RunViewModel;

import java.text.DecimalFormat;


public class HomeFragment extends Fragment {

    private RunViewModel viewModel;
    private TextView distanceRan;
    private TextView timeRan;
    private TextView avgSpeed;
    private DecimalFormat df;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home, container, false);
        distanceRan = v.findViewById(R.id.hDistance);
        timeRan = v.findViewById(R.id.hTime);
        avgSpeed = v.findViewById(R.id.hSpeed);
        df = new DecimalFormat("#.##");

        viewModel = new ViewModelProvider((ViewModelStoreOwner) this,
                (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(RunViewModel.class);

        viewModel.getMostRecentRun().observe(getViewLifecycleOwner(), new Observer<Run>() {
            @Override
            public void onChanged(Run run) {
                if (run == null){
                    distanceRan.setText("N/A");
                    timeRan.setText("N/A");
                    avgSpeed.setText("N/A");
                    return;
                }

                distanceRan.setText(df.format(run.getDistance())+"Km");
                timeRan.setText(timeConversionString(run.getTimeRan()));
                avgSpeed.setText(df.format(run.getAverageSpeed())+"Km/h");

            }
        });

        return v;
    }

    private String timeConversionString(long milliseconds) {
        StringBuffer buf = new StringBuffer();

        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) ((milliseconds % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) (((milliseconds % (1000 * 60 * 60)) % (1000 * 60)) / 1000);

        buf
                .append(String.format("%02d", hours))
                .append(":")
                .append(String.format("%02d", minutes))
                .append(":")
                .append(String.format("%02d", seconds));

        return buf.toString();
    }
}