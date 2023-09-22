package com.example.runningtracker.ui;

import static android.content.ContentValues.TAG;

import android.app.Application;
import android.graphics.Color;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.runningtracker.R;
import com.example.runningtracker.db.Run;
import com.example.runningtracker.db.RunAdapter;
import com.example.runningtracker.db.RunViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class MetricsFragment extends Fragment {

    private RunViewModel viewModel;
    private TextView totalDistanceRan;
    private TextView totalTimeRan;
    private TextView avgSpeed;
    private DecimalFormat df;
    private BarChart barChart;


    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_metrics, container, false);

        totalDistanceRan = v.findViewById(R.id.totalDistanceText);
        totalTimeRan = v.findViewById(R.id.totalTimeText);
        avgSpeed = v.findViewById(R.id.averageSpeedText);
        barChart = v.findViewById(R.id.barChart);
        df = new DecimalFormat("#.##");

        setupBarChart();
        viewModel = new ViewModelProvider((ViewModelStoreOwner) this,
                (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(RunViewModel.class);

        //Observes changes to totalTimeRan and updates textfield on change to new value
        viewModel.getTotalTimeRan().observe(getViewLifecycleOwner(), new Observer<Long>() {
            @Override
            public void onChanged(Long aLong) {
                if(aLong == null){
                    totalTimeRan.setText("NaN");
                    return;
                }
                totalTimeRan.setText(timeConversionString(aLong));
            }
        });

        //Observes updates to totalDistanceRan and updates textfield on change to new value
        viewModel.getTotalDistance().observe(getViewLifecycleOwner(), new Observer<Double>(){
            @Override
            public void onChanged(Double aDouble) {
                if(aDouble == null){
                    totalDistanceRan.setText("NaN");
                            return;
                }
                totalDistanceRan.setText(df.format(aDouble)+"Km");
            }
        });

        //Observes updates to averageSpeed and updates text field on change to new value
        viewModel.getMeanAverageSpeed().observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double aDouble) {
                if(aDouble == null){
                    avgSpeed.setText("NaN");
                    return;
                }
                avgSpeed.setText(df.format(aDouble)+"Km/h");
            }
        });

        viewModel.getRunsByDate().observe(getViewLifecycleOwner(), new Observer<List<Run>>() {
            @Override
            public void onChanged(List<Run> runs) {
                if (runs != null) {
                    List<BarEntry> allAvgSpeeds = new ArrayList<>();
                    for (int i = 0; i < runs.size(); i++) {
                        allAvgSpeeds.add(new BarEntry(i, (float) runs.get(i).getAverageSpeed()));
                    }
                    BarDataSet bardataSet = new BarDataSet(allAvgSpeeds, "Avg Speed Over Time");
                    bardataSet.setValueTextColor(Color.WHITE);
                    bardataSet.setColor(ContextCompat.getColor(requireContext(), R.color.teal_200));
                    barChart.setData(new BarData(bardataSet));
                    barChart.invalidate();
                }
            }
        });

        return v;
    }

    //Converts millisecond long value in to hours minutes and seconds and returns it in the form of a string
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

    private void setupBarChart() {
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setDrawLabels(false);
        barChart.getXAxis().setAxisLineColor(Color.BLACK);
        barChart.getXAxis().setTextColor(Color.BLACK);
        barChart.getXAxis().setDrawGridLines(false);

        barChart.getAxisLeft().setAxisLineColor(Color.BLACK);
        barChart.getAxisLeft().setTextColor(Color.BLACK);
        barChart.getAxisLeft().setDrawGridLines(false);

        barChart.getAxisRight().setAxisLineColor(Color.BLACK);
        barChart.getAxisRight().setTextColor(Color.BLACK);
        barChart.getAxisRight().setDrawGridLines(false);

        barChart.getDescription().setText("Avg Speed Over Time");
        barChart.getLegend().setEnabled(false);
    }
}