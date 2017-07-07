package com.ldz.fpt.democompass;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends AppCompatActivity implements SensorEventListener, OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMapLoadedCallback, GoogleMap.OnCameraIdleListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    //view
    private TextView txtTitle;
    private ImageView imvCompass;
    //
    private SensorManager sensorManager;
    //
    private float currentDegrees = 0f;
    //
    private GoogleMap mMap;
    private LocationManager locationManager;
    private Location location;
    private float bearing;
    private boolean isOnMyLocation;
    private SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // for the system's orientation sensor registered listeners
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
        checkGPSEnable();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    private void init() {
        //view
        isOnMyLocation = true;
        txtTitle = (TextView) findViewById(R.id.txt_title);
        imvCompass = (ImageView) findViewById(R.id.imv_compass);
        //
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        //
        bearing = 0;
        //
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // get the angle around the z-axis rotated
        float degree = sensorEvent.values[0];
        txtTitle.setText(getDirection(degree));
        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                currentDegrees, -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        // how long the animation will take place
        ra.setDuration(210);
        // set the animation after the end of the reservation status
        ra.setFillAfter(true);
        // Start the animation
        imvCompass.startAnimation(ra);
        if (mMap != null && Math.abs(-currentDegrees - degree) >= 0.1f && !isOnMyLocation) {
            rotateMap(degree);
        }
        currentDegrees = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setOnMyLocationButtonClickListener(MainActivity.this);
        mMap.setOnMapLoadedCallback(MainActivity.this);
    }

    private String getDirection(float degree) {
        if (degree <= 22.5f || degree > 337.5f) {
            return String.format("Facing: %.1f North", degree);
        }
        if (degree > 22.5f && degree <= 67.5f) {
            return String.format("Facing: %.1f North East", degree);
        }
        if (degree > 67.5f && degree <= 112.5f) {
            return String.format("Facing: %.1f East", degree);
        }
        if (degree > 112.5f && degree <= 157.5f) {
            return String.format("Facing: %.1f South East", degree);
        }
        if (degree > 157.5f && degree <= 202.5f) {
            return String.format("Facing: %.1f South", degree);
        }
        if (degree > 202.5f && degree <= 247.5f) {
            return String.format("Facing: %.1f South West", degree);
        }
        if (degree > 247.5f && degree <= 292.5f) {
            return String.format("Facing: %.1f West", degree);
        }
        if (degree > 292.5f && degree <= 337.5f) {
            return String.format("Facing: %.1f North West", degree);
        }
        return "";
    }

    private void rotateMap(float bearing) {
        location = mMap.getMyLocation();
        CameraPosition current = mMap.getCameraPosition();
        CameraPosition position = new CameraPosition(new LatLng(location.getLatitude(), location.getLongitude()), current.zoom, current.tilt, bearing);
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    private void checkGPSEnable() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Enable GPS")
                    .setMessage("You need to enable GPS ...")
                    .setPositiveButton("Setting", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(myIntent);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    });
            alertDialog.create().show();
        } else {
            mapFragment.getMapAsync(this);

        }
    }

    @Override
    public void onMapLoaded() {
        do {
            location = mMap.getMyLocation();
        } while (location == null);
        Log.d("fuck", "lat = " + location.getLatitude() + " - long = " + location.getLongitude());
        mMap.setOnCameraIdleListener(MainActivity.this);
        CameraPosition cameraPosition = new CameraPosition(new LatLng(location.getLatitude(), location.getLongitude()), 16.5f, 0, 0);
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        Log.d("fuck", "fuck");
    }

    @Override
    public void onCameraIdle() {
        isOnMyLocation = false;
    }
}
