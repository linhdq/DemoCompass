package com.ldz.fpt.democompass;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends AppCompatActivity implements SensorEventListener,
        OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMapLoadedCallback, GoogleMap.OnCameraIdleListener, View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    //view
    private TextView txtTitle;
    private ImageView imvCompass;
    private Dialog dialogInternet;
    private Button btnOk;
    private Dialog dialogGPS;
    private Button btnCancel;
    private Button btnSetting;
    //
    private SensorManager sensorManager;
    //
    private float currentDegrees = 0f;
    //
    private GoogleMap mMap;
    private LocationManager locationManager;
    private Location location;
    private boolean isOnMyLocation;
    private boolean isAnimate;
    private SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //
        init();
        addListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // for the system's orientation sensor registered listeners
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
        checkGPSEnable();
        mapFragment.getMapAsync(this);
        //
        if (!checkInternetAvailable()) {
            dialogInternet.show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        if (dialogInternet.isShowing()) {
            dialogInternet.dismiss();
        }
        if (dialogGPS.isShowing()) {
            dialogGPS.dismiss();
        }
    }

    private void init() {
        //view
        isOnMyLocation = true;
        isAnimate = false;
        txtTitle = (TextView) findViewById(R.id.txt_title);
        imvCompass = (ImageView) findViewById(R.id.imv_compass);
        //
        dialogInternet = new Dialog(this);
        dialogInternet.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialogInternet.setContentView(R.layout.custom_dialog_internet_connection);
        dialogInternet.setCanceledOnTouchOutside(false);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = dialogInternet.getWindow();
        lp.copyFrom(window.getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        //
        btnOk = (Button) dialogInternet.findViewById(R.id.btn_ok);
        //
        dialogGPS = new Dialog(this);
        dialogGPS.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialogGPS.setContentView(R.layout.custom_dialog_gps);
        dialogGPS.setCanceledOnTouchOutside(false);
        dialogGPS.getWindow().setAttributes(lp);
        //
        btnCancel = (Button) dialogGPS.findViewById(R.id.btn_cancel);
        btnSetting = (Button) dialogGPS.findViewById(R.id.btn_setting);
        //
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        //
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    private void addListener() {
        btnOk.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnSetting.setOnClickListener(this);
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
        if (!isAnimate) {
            location = mMap.getMyLocation();
            if (location != null) {
                mMap.setOnCameraIdleListener(MainActivity.this);
                CameraPosition cameraPosition = new CameraPosition(new LatLng(location.getLatitude(), location.getLongitude()), 16.5f, 0, 0);
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                isAnimate = true;
            }
        }
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
        if (location != null) {
            CameraPosition current = mMap.getCameraPosition();
            CameraPosition position = new CameraPosition(new LatLng(location.getLatitude(), location.getLongitude()), current.zoom, current.tilt, bearing);
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
        }
    }

    private boolean checkInternetAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    @Override
    public boolean onMyLocationButtonClick() {
        location = mMap.getMyLocation();
        return false;
    }

    private void checkGPSEnable() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            dialogGPS.show();
        } else {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapLoaded() {
        location = mMap.getMyLocation();
        if (location != null) {
            mMap.setOnCameraIdleListener(MainActivity.this);
            CameraPosition cameraPosition = new CameraPosition(new LatLng(location.getLatitude(), location.getLongitude()), 16.5f, 0, 0);
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    @Override
    public void onCameraIdle() {
        isOnMyLocation = false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                dialogInternet.dismiss();
                break;
            case R.id.btn_cancel:
                dialogGPS.dismiss();
                finish();
                break;
            case R.id.btn_setting:
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);
                break;
            default:
                break;
        }
    }
}
