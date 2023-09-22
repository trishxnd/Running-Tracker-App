package com.example.runningtracker.db;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runningtracker.R;
import com.example.runningtracker.ui.itemSelectListener;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class RunAdapter extends RecyclerView.Adapter<RunAdapter.RunViewHolder> {

    private List<Run> data;
    private Context context;
    private LayoutInflater layoutInflater;
    private itemSelectListener itemListener;


    public RunAdapter(Context context, itemSelectListener listener) {

        this.data = new ArrayList<>();
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.itemListener = listener;
    }

    @Override
    public RunViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.db_run_layout, parent, false);
        return new RunViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RunViewHolder holder, int position) {
        holder.bind(data.get(position));
        final int pos = position;
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemListener.onItemClicked(data.get(pos));
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<Run> newData) {
        if (data != null) {
            data.clear();
            data.addAll(newData);
            notifyDataSetChanged();
        } else {
            data = newData;
        }
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

    class RunViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout cardView;
        TextView dateView;
        TextView trView;
        TextView distanceView;
        TextView avgSpeedView;
        String comment;
        ImageView image;

        RunViewHolder(View itemView) {
            super(itemView);

            dateView = itemView.findViewById(R.id.dateView);
            cardView = itemView.findViewById(R.id.main_container);
            trView = itemView.findViewById(R.id.timeRanView);
            distanceView = itemView.findViewById(R.id.distanceView);
            avgSpeedView = itemView.findViewById(R.id.avgSpeedView);
        }

        void bind(final Run run) {

            DecimalFormat df = new DecimalFormat("#.##");

            if (run != null) {
                dateView.setText(""+ run.getDateLogged());
                comment = run.getComment();
                trView.setText(timeConversionString(run.getTimeRan()));
                distanceView.setText(""+ df.format(run.getDistance())+ "Km");
                avgSpeedView.setText(""+ df.format(run.getAverageSpeed())+ "Km/h");



            }
        }

    }
}
