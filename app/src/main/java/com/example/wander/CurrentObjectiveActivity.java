package com.example.wander;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class CurrentObjectiveActivity extends Fragment {
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    static ArrayList<ObjectiveItem> displayedObjectiveItems = new ArrayList<ObjectiveItem>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_current_objective, container, false);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        final Button foundIt = rootView.findViewById(R.id.found);
        final Gson gson = new Gson();
        final SharedPreferences sharedPref = getContext().getSharedPreferences("gameState", Context.MODE_PRIVATE);
        String json = sharedPref.getString("objectiveList", "");
        displayedObjectiveItems = gson.fromJson(json, new TypeToken<List<ObjectiveItem>>(){}.getType());

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                currentLocation = location;
                                foundIt.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        String name = sharedPref.getString("currName", "");
                                        float objLat = sharedPref.getFloat("currLat", 0f);
                                        float objLong = sharedPref.getFloat("currLong", 0f);

                                        if (verifyDistance(currentLocation.getLatitude(), currentLocation.getLongitude(), objLat, objLong, 500)) {
                                            boolean allFound = true;
                                            for (ObjectiveItem obj: displayedObjectiveItems) {
                                                if (obj.getName().equals(name)) {
                                                    obj.setFound(true);
                                                    obj.setWhen("Found");
                                                    String json = gson.toJson(displayedObjectiveItems);
                                                    SharedPreferences.Editor editor = sharedPref.edit();
                                                    editor.putString("objectiveList", json);
                                                    editor.commit();
                                                }
                                                if (!obj.getFound()) {
                                                    allFound = false;
                                                }
                                            }
                                            if (allFound) {
                                                Intent intent = new Intent(getActivity(), FinishActivity.class);
                                                startActivity(intent);
                                            } else {
                                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                                startActivity(intent);
                                            }
                                        }
                                    }
                                });
                            }

                        }
                    });

        }

        // Inflate the layout for this fragment
        return rootView;
    }
    // Called at the start of the visible lifetime.
    @Override
    public void onStart(){
        super.onStart();
        Log.d ("Content Fragment", "onStart");
        // Apply any required UI change now that the Fragment is visible.
    }

    // Called at the start of the active lifetime.
    @Override
    public void onResume(){
        super.onResume();
        Log.d ("Content Fragment", "onResume");
        SharedPreferences sharedPref = getContext().getSharedPreferences("gameState", Context.MODE_PRIVATE);
        int image = sharedPref.getInt("currImage", -1);
        String name = sharedPref.getString("currName", "No objective selected!");

        ImageView img = (ImageView) getActivity().findViewById(R.id.currentObjectiveImage);
        if (image != -1) {
            img.setImageResource(image);
        }

        TextView location = (TextView) getActivity().findViewById(R.id.currentObjectiveName);
        location.setText(name);
    }

    // Called at the end of the active lifetime.
    @Override
    public void onPause(){
        Log.d ("Content Fragment", "onPause");
        // Suspend UI updates, threads, or CPU intensive processes
        // that don't need to be updated when the Activity isn't
        // the active foreground activity.
        // Persist all edits or state changes
        // as after this call the process is likely to be killed.
        super.onPause();
    }

    // Called to save UI state changes at the
    // end of the active lifecycle.
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d ("Content Fragment", "onSaveInstanceState");
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate, onCreateView, and
        // onCreateView if the parent Activity is killed and restarted.
        super.onSaveInstanceState(savedInstanceState);
    }

    // Called at the end of the visible lifetime.
    @Override
    public void onStop(){
        Log.d ("Content Fragment", "onStop");
        // Suspend remaining UI updates, threads, or processing
        // that aren't required when the Fragment isn't visible.
        super.onStop();
    }

    // Called when the Fragment's View has been detached.
    @Override
    public void onDestroyView() {
        Log.d ("Content Fragment", "onDestroyView");
        // Clean up resources related to the View.
        super.onDestroyView();
    }

    // Called at the end of the full lifetime.
    @Override
    public void onDestroy(){
        Log.d ("Content Fragment", "onDestroy");
        // Clean up any resources including ending threads,
        // closing database connections etc.
        super.onDestroy();
    }

    // Called when the Fragment has been detached from its parent Activity.
    @Override
    public void onDetach() {
        Log.d ("Content Fragment", "onDetach");
        super.onDetach();
    }

    // Verify user is within given number of feet
    public boolean verifyDistance(double userLat, double userLong, double objLat, double objLong, double rad) {
        if ((userLat == objLat) && (userLong == objLong)) {
            return true;
        }
        else {
            double theta = userLong - objLong;
            double dist = Math.sin(Math.toRadians(userLat)) * Math.sin(Math.toRadians(objLat)) + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(objLat)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515 * 5280;

            return dist <= rad;
        }
    }
}

