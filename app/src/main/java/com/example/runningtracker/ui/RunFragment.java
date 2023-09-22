package com.example.runningtracker.ui;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.runningtracker.R;
import com.example.runningtracker.db.Run;
import com.example.runningtracker.db.RunAdapter;
import com.example.runningtracker.db.RunViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class RunFragment extends Fragment implements itemSelectListener{

    private RunViewModel viewModel;
    private final static int LOCATION_PERMISSION = 1;
    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private RunAdapter adapter;

    //Initializes and populates recyclerview with data containing all previous runs in database
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            requestPermissions();
        }



        View v = inflater.inflate(R.layout.fragment_run, container, false);
        recyclerView = v.findViewById(R.id.r_recyclerview);
        Log.d(TAG, "onCreateView: before adapter");
        adapter = new RunAdapter(this.getActivity(), this);


        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));


        viewModel = new ViewModelProvider((ViewModelStoreOwner) this,
                (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(RunViewModel.class);
        viewModel.getAllRuns().observe(this.getActivity(), runs -> {
            adapter.setData(runs);
        });

        return v;
    }

    //Sets up listener for when FAB is pressed to initialize the RunActivity
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fab = view.findViewById(R.id.floatingActionButton3);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickPlus(view);
            }
        });

    }

    public void requestPermissions(){
        requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},LOCATION_PERMISSION );
    }
    //Creates new intent to start RunActivity
    public void onClickPlus(View v){
        Intent intent = new Intent(this.getActivity(), RunActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == LOCATION_PERMISSION && grantResults.length > 0){
            return;
        }
        else{
            Toast.makeText(getActivity(), "Location Permission denied, need to accept to use this feature", Toast.LENGTH_SHORT).show();
            requestPermissions();

        }
    }

    @Override
    public void onItemClicked(Run run) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder.setTitle("Run Comments");
        builder.setMessage(run.getComment());
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }

        });
        builder.show();
    }

}