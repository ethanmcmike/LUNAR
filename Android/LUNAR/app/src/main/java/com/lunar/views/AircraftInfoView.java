package com.lunar.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.lunar.R;
import com.lunar.models.Aircraft;

public class AircraftInfoView extends CardView {

    private Context context;
    private Aircraft aircraft;

    private TextView title, altVal, altUnit, tempVal, tempUnit, latVal, latUnit, lonVal, lonUnit;
    private RXIndicatorView rxIndicator;

    public AircraftInfoView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public AircraftInfoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AircraftInfoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){

        this.context = context;

        //Inflate layout
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.aircraft_info, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        title = findViewById(R.id.title);
        rxIndicator = findViewById(R.id.transfer_image);
        altVal = findViewById(R.id.alt_value);
        altUnit = findViewById(R.id.alt_unit);
        tempVal = findViewById(R.id.temp_value);
        tempUnit = findViewById(R.id.temp_unit);
        latVal = findViewById(R.id.lat_value);
        latUnit = findViewById(R.id.lat_unit);
        lonVal = findViewById(R.id.lon_value);
        lonUnit = findViewById(R.id.lon_unit);
    }

    @Override
    public void invalidate() {

        if(aircraft != null) {
            title.setText(aircraft.title);
            altVal.setText(String.valueOf(aircraft.getAlt()));
            altUnit.setText(context.getString(aircraft.getAltUnit()));
            tempVal.setText(String.valueOf(aircraft.getTemp()));
            tempUnit.setText(context.getString(aircraft.getTempUnit()));
            latVal.setText(String.valueOf(aircraft.getLat()));
            latUnit.setText(context.getString(aircraft.getLatUnit()));
            lonVal.setText(String.valueOf(aircraft.getLon()));
            lonUnit.setText(context.getString(aircraft.getLonUnit()));
        }

        super.invalidate();
    }

    public void setAircraft(Aircraft aircraft) {
        this.aircraft = aircraft;
        aircraft.view = this;
        invalidate();
    }

    public void update(){
        rxIndicator.update();
        invalidate();
    }
}
