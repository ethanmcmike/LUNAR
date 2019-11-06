package com.lunar.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lunar.R;
import com.lunar.models.Aircraft;
import com.lunar.models.InputData;
import com.lunar.views.AircraftInfoView;

public class DataFragment extends Fragment {

    private static DataFragment instance;
    private static Aircraft rocket;

    public AircraftInfoView rocketView;

    public static DataFragment getInstance(){
        if(instance == null){
            instance = new DataFragment();
            rocket = new Aircraft("Rocket", 2);
        }

        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.data_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rocketView = view.findViewById(R.id.rocket_view);
        rocketView.setAircraft(rocket);
    }

    public void update(InputData data){
        rocket.altitude = data.altitude;
        rocket.temp = data.temp;
        rocket.lat = data.lat;
        rocket.lon = data.lon;

        if(rocketView != null) {
            rocketView.update();
        }
    }
}
