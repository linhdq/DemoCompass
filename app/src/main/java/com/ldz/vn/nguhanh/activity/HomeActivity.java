package com.ldz.vn.nguhanh.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.ldz.vn.nguhanh.R;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HomeActivity extends AppCompatActivity implements SensorEventListener,
        OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMapLoadedCallback, GoogleMap.OnCameraIdleListener, View.OnClickListener, Animation.AnimationListener {

    private static final String TAG = HomeActivity.class.getSimpleName();
    public static final String URL = "http://vienphongthuy.vn";
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    //view
    private TextView txtTitle;
    private ImageView imvCompass;
    private Dialog dialogInternet;
    private Button btnOk;
    private Dialog dialogGPS;
    private Button btnCancel;
    private Button btnSetting;
    private TextView txtStatusBottom;
    private ImageView btnDo;
    private ImageView btnLock;
    private ImageView btnCaptureScreen;
    private ImageView btnInfo;
    private ImageView btnShare;
    private View mapView;
    private View flashView;
    private ImageView imvLogo;
    private ImageView btnSearch;
    private AlphaAnimation alphaAnimation;
    //dialog
    private Dialog dialogWaiting;
    //
    private SensorManager sensorManager;
    //
    private float currentDegrees = 0f;
    private float degree;
    private float minusValue;
    //
    private GoogleMap mMap;
    private LocationManager locationManager;
    private Location location;
    private LatLng centerPosition;
    //
    private boolean isLocked;
    private boolean isOnMyLocation;
    private boolean isShareScreen;
    private boolean isFirst;
    private String today;
    private SupportMapFragment mapFragment;
    //
    private Uri screenShotUri;
    //
    private SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yyyy");
    private Date date;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
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
        txtTitle = (TextView) findViewById(R.id.txt_title);
        imvCompass = (ImageView) findViewById(R.id.imv_compass);
        txtStatusBottom = (TextView) findViewById(R.id.txt_status_bottom);
        btnDo = (ImageView) findViewById(R.id.btn_do);
        btnLock = (ImageView) findViewById(R.id.btn_lock);
        btnCaptureScreen = (ImageView) findViewById(R.id.btn_capture_screen);
        btnInfo = (ImageView) findViewById(R.id.btn_info);
        flashView = findViewById(R.id.flash_view);
        flashView.setVisibility(View.GONE);
        imvLogo = (ImageView) findViewById(R.id.imv_logo);
        btnSearch = (ImageView) findViewById(R.id.btn_search);
        btnShare = (ImageView) findViewById(R.id.btn_share);
        //dialog
        dialogWaiting = new Dialog(this);
        dialogWaiting.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialogWaiting.setContentView(R.layout.custom_dialog_waiting);
        dialogWaiting.setCanceledOnTouchOutside(false);
        //Animation
        alphaAnimation = new AlphaAnimation(1, 0);
        alphaAnimation.setDuration(500);
        alphaAnimation.setAnimationListener(this);
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
        mapView = mapFragment.getView();
        //
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //
        date = new Date();
        today = formatDate.format(date);
        txtStatusBottom.setText(String.format("Kinh độ: %.3f - Vĩ độ: %.3f\nNgày đo: %s", 0f, 0f, today));
        //
        isOnMyLocation = true;
        isShareScreen = false;
        isLocked = false;
        isFirst = true;
    }

    private void addListener() {
        btnOk.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnSetting.setOnClickListener(this);
        btnDo.setOnClickListener(this);
        btnLock.setOnClickListener(this);
        btnCaptureScreen.setOnClickListener(this);
        btnInfo.setOnClickListener(this);
        imvLogo.setOnClickListener(this);
        btnSearch.setOnClickListener(this);
        btnShare.setOnClickListener(this);
    }

    private void openSearchFunction() {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
            // TODO: Handle the error.
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (mMap != null) {
            centerPosition = mMap.getCameraPosition().target;
            if (centerPosition != null) {
                txtStatusBottom.setText(String.format("Kinh độ: %.3f - Vĩ độ: %.3f\nNgày đo: %s", centerPosition.longitude, centerPosition.latitude, today));
            }
        }
        if (!isLocked) {
            degree = sensorEvent.values[0];
            if (degree <= 180f) {
                minusValue = 180f;
            } else {
                minusValue = -180f;
            }
            txtTitle.setText(getDirection(Math.abs(degree + minusValue)));
            RotateAnimation ra = new RotateAnimation(
                    currentDegrees, -degree,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            ra.setDuration(210);
            ra.setFillAfter(true);
            imvCompass.startAnimation(ra);
            if (mMap != null && Math.abs(-currentDegrees - degree) >= 0.1f && !isOnMyLocation) {
                rotateMap(degree);
            }
            currentDegrees = -degree;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.setOnMyLocationButtonClickListener(HomeActivity.this);
        mMap.setOnMapLoadedCallback(HomeActivity.this);
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        //
        if (mapView != null &&
                mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 29,
                    getResources().getDimensionPixelSize(R.dimen.search_size));
        }
    }

    private void rotateMap(float bearing) {
        if (centerPosition != null) {
            CameraPosition current = mMap.getCameraPosition();
            CameraPosition position = new CameraPosition(centerPosition, current.zoom, current.tilt, bearing);
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
        if (isFirst) {
            Log.d(TAG, "onMapLoaded: ");
            mMap.setOnCameraIdleListener(HomeActivity.this);
            CameraPosition cameraPosition = new CameraPosition(new LatLng(21.027830828547962, 105.85224889218807), 16.5f, 0, 0);
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            txtStatusBottom.setText(String.format("Kinh độ: %.3f - Vĩ độ: %.3f\nNgày đo: %s", 105.85224889218807, 21.027830828547962f, today));
            isFirst = false;
        }
    }

    @Override
    public void onCameraIdle() {
        isOnMyLocation = false;
    }

    private void goToInfoActivity() {
        Intent intent = new Intent(this, InfoActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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
            case R.id.btn_do:
                isLocked = false;
                break;
            case R.id.btn_lock:
                isLocked = true;
                break;
            case R.id.btn_capture_screen:
                isShareScreen = false;
                flashView.startAnimation(alphaAnimation);
                break;
            case R.id.btn_info:
                goToInfoActivity();
                break;
            case R.id.imv_logo:
                openWebBrowser();
                break;
            case R.id.btn_search:
                openSearchFunction();
                break;
            case R.id.btn_share:
                dialogWaiting.show();
                if (screenShotUri != null) {
                    Log.d(TAG, "onClick: not null");
                    shareScreenShot();
                } else {
                    Log.d(TAG, "onClick: new");
                    isShareScreen = true;
                    captureMapScreen();
                }
                break;
            default:
                break;
        }
    }

    private void shareScreenShot() {
        Log.d(TAG, "shareScreenShot: " + screenShotUri);
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out my app.");
        shareIntent.putExtra(Intent.EXTRA_STREAM, screenShotUri);
        shareIntent.setType("image/*");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        dialogWaiting.dismiss();
        startActivity(Intent.createChooser(shareIntent, "Share via"));
        screenShotUri = null;
    }

    private void saveImage(Bitmap bm) {
        //Code below is saving to external storage
        final String dirPath = Environment.getExternalStorageDirectory() + "/Screenshots";
        File dir = new File(dirPath);
        if (!dir.exists())
            dir.mkdirs();
        String fileName = System.currentTimeMillis() + ".jpeg";
        File file = new File(dirPath, fileName);
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
            fOut.flush();
            fOut.close();
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(dirPath + "/" + fileName);
            screenShotUri = Uri.fromFile(f);
            mediaScanIntent.setData(screenShotUri);
            this.sendBroadcast(mediaScanIntent);
            if (isShareScreen) {
                shareScreenShot();
                isShareScreen = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void captureMapScreen() {
        GoogleMap.SnapshotReadyCallback snapshotReadyCallback = new GoogleMap.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(Bitmap bitmap) {
                getWindow().getDecorView().findViewById(android.R.id.content).setDrawingCacheEnabled(true);
                Bitmap backBitmap = getWindow().getDecorView().findViewById(android.R.id.content).getDrawingCache();
                Bitmap bmOverlay = Bitmap.createBitmap(
                        backBitmap.getWidth(), backBitmap.getHeight(),
                        backBitmap.getConfig());
                Canvas canvas = new Canvas(bmOverlay);
                canvas.drawBitmap(bitmap, new Matrix(), null);
                canvas.drawBitmap(backBitmap, 0, 0, null);
                getWindow().getDecorView().findViewById(android.R.id.content).setDrawingCacheEnabled(false);
                saveImage(bmOverlay);
            }
        };
        mMap.snapshot(snapshotReadyCallback);
    }

    private void openWebBrowser() {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(URL));
        startActivity(i);
    }

    private String getDirection(float degree) {
        if (degree >= 0.000f && degree <= 5.625f) {
            return String.format("Địa Lôi Phục 1/8\n(%.3f) Tý", degree);
        }
        if (degree >= 5.625f && degree <= 11.250f) {
            return String.format("Sơn Lôi Di 6/3\n(%.3f) Tý-Quý", degree);
        }
        if (degree >= 11.251f && degree <= 16.875f) {
            return String.format("Thủy Lôi Truân 7/4\n(%.3f) Quý", degree);
        }
        if (degree >= 16.876f && degree <= 22.500f) {
            return String.format("Phong Lôi Ích 2/9\n(%.3f) Quý", degree);
        }
        if (degree >= 22.501f && degree <= 28.125f) {
            return String.format("Chấn Vi Lôi 8/1\n(%.3f) Sửu", degree);
        }
        if (degree >= 28.126f && degree <= 33.750f) {
            return String.format("Hỏa Lôi Phệ Hạp 3/6\n(%.3f) Sửu", degree);
        }
        if (degree >= 33.751f && degree <= 39.375f) {
            return String.format("Trạch Lôi Tùy 4/7\n(%.3f) Sửu-Cấn", degree);
        }
        if (degree >= 39.376f && degree <= 45.000f) {
            return String.format("Thiên Lôi Vô Vọng 9/2\n(%.3f) Cấn", degree);
        }
        if (degree >= 45.001f && degree <= 50.625f) {
            return String.format("Địa Hỏa Minh Di 1/3\n(%.3f) Cấn", degree);
        }
        if (degree >= 50.626f && degree <= 56.250f) {
            return String.format("Sơn Hỏa Bí 6/8\n(%.3f) Cấn-Dần", degree);
        }
        if (degree >= 56.251f && degree <= 61.875f) {
            return String.format("Thủy Hỏa Ký Tế 7/9\n(%.3f) Dần", degree);
        }
        if (degree >= 61.876f && degree <= 67.500f) {
            return String.format("Phong Hỏa Gia Nhân 2/4\n(%.3f) Dần", degree);
        }
        if (degree >= 67.501f && degree <= 73.125f) {
            return String.format("Lôi Hỏa Phong 8/6\n(%.3f) Giáp", degree);
        }
        if (degree >= 73.126f && degree <= 78.750f) {
            return String.format("Ly Vi Hỏa 3/1\n(%.3f) Giáp", degree);
        }
        if (degree >= 78.751f && degree <= 84.375f) {
            return String.format("Trạch Hỏa Cách 4/2\n(%.3f) Giáp-Mão", degree);
        }
        if (degree >= 84.376f && degree <= 90.000f) {
            return String.format("Thiên Hỏa Đồng Nhân 9/7\n(%.3f) Mão", degree);
        }
        if (degree >= 90.001f && degree <= 95.625f) {
            return String.format("Địa Trạch Lâm 1/7\n(%.3f) Mão", degree);
        }
        if (degree >= 95.626f && degree <= 101.250f) {
            return String.format("Sơn Trạch Tổn 6/9\n(%.3f) Mão", degree);
        }
        if (degree >= 101.251f && degree <= 106.875f) {
            return String.format("Thủy Trạch Tiết 7/8\n(%.3f) Ất", degree);
        }
        if (degree >= 106.875f && degree <= 112.500f) {
            return String.format("Phong Trạch Trung Phu 2/3\n(%.3f) Ất", degree);
        }
        if (degree >= 112.501f && degree <= 118.125f) {
            return String.format("Lôi Trạch Quy Muội 8/7\n(%.3f) Thìn", degree);
        }
        if (degree >= 118.126f && degree <= 123.750f) {
            return String.format("Hỏa Trạch Khuê 3/2\n(%.3f) Thìn", degree);
        }
        if (degree >= 123.751f && degree <= 129.375f) {
            return String.format("Đoài Vi Trạch 4/1\n(%.3f) Thìn-Tốn", degree);
        }
        if (degree >= 129.376f && degree <= 135.000f) {
            return String.format("Thiên Trạch  Lý 9/6\n(%.3f) Tốn", degree);
        }
        if (degree >= 135.001f && degree <= 140.625f) {
            return String.format("Địa Thiên Thái 1/9\n(%.3f) Tốn", degree);
        }
        if (degree >= 140.626f && degree <= 146.250f) {
            return String.format("Sơn Thiên Đại Súc 6/4\n(%.3f) Tốn-Tỵ", degree);
        }
        if (degree >= 146.251 && degree <= 151.875f) {
            return String.format("Thủy Thiên Nhu 7/3\n(%.3f) Tỵ", degree);
        }
        if (degree >= 151.876f && degree <= 157.500f) {
            return String.format("Phong Thiên Tiểu Súc 2/8\n(%.3f) Tỵ", degree);
        }
        if (degree >= 157.501f && degree <= 163.125f) {
            return String.format("Lôi Thiên Đại Tráng 8/2\n(%.3f) Bính", degree);
        }
        if (degree >= 163.126f && degree <= 168.750f) {
            return String.format("Hỏa Thiên Đại Hữu 3/7\n(%.3f) Bính", degree);
        }
        if (degree >= 168.751f && degree <= 174.375f) {
            return String.format("Trạch Thiên Quải 4/6\n(%.3f) Bính-Ngọ", degree);
        }
        if (degree >= 174.376f && degree <= 180.000f) {
            return String.format("Càn Vi Thiên 9/1\n(%.3f) Ngọ", degree);
        }
        if (degree >= 180.001f && degree <= 185.625f) {
            return String.format("Thiên Phong Cấu 9/8\n(%.3f) Ngọ", degree);
        }
        if (degree >= 185.625f && degree <= 191.250f) {
            return String.format("Trạch Phong Đại Quá 4/3\n(%.3f) Đinh", degree);
        }
        if (degree >= 191.251f && degree <= 196.875f) {
            return String.format("Hỏa Phong Đỉnh 3/4\n(%.3f) Đinh", degree);
        }
        if (degree >= 196.876f && degree <= 202.500f) {
            return String.format("Lôi Phong Hằng 8/9\n(%.3f) Đinh", degree);
        }
        if (degree >= 202.501f && degree <= 208.125f) {
            return String.format("Tốn Vi Phong 2/1\n(%.3f) Mùi", degree);
        }
        if (degree >= 208.126f && degree <= 213.750f) {
            return String.format("Thủy Phong Tỉnh 7/6\n(%.3f) Mùi", degree);
        }
        if (degree >= 213.751f && degree <= 219.375f) {
            return String.format("Sơn Phong Cổ 6/7\n(%.3f) Mùi-Khôn", degree);
        }
        if (degree >= 219.376f && degree <= 225.000f) {
            return String.format("Địa Phong Thăng 3/2\n(%.3f) Khôn", degree);
        }
        if (degree >= 225.001f && degree <= 230.625f) {
            return String.format("Thiên Thủy Tụng 9/3\n(%.3f) Khôn", degree);
        }
        if (degree >= 230.626f && degree <= 236.250f) {
            return String.format("Trạch Thủy Khốn 4/8\n(%.3f) Khôn-Thân", degree);
        }
        if (degree >= 236.251f && degree <= 241.875f) {
            return String.format("Hỏa Thủy Vị Tế 3/9\n(%.3f) Thân", degree);
        }
        if (degree >= 241.876f && degree <= 247.500f) {
            return String.format("Lôi Thủy Giải 8/4\n(%.3f) Thân", degree);
        }
        if (degree >= 247.501f && degree <= 253.125f) {
            return String.format("Phong Thủy Hoán 2/6\n(%.3f) Canh", degree);
        }
        if (degree >= 253.126f && degree <= 258.750f) {
            return String.format("Khảm Vi Thủy 7/1\n(%.3f) Canh", degree);
        }
        if (degree >= 258.751f && degree <= 264.375f) {
            return String.format("Sơn Thủy Mông 6/2\n(%.3f) Canh-Dậu", degree);
        }
        if (degree >= 264.376f && degree <= 270.000f) {
            return String.format("Địa Thủy Sư 1/7\n(%.3f) Dậu", degree);
        }
        if (degree >= 270.001f && degree <= 275.625f) {
            return String.format("Thiên Sơn Độn 9/4\n(%.3f) Dậu", degree);
        }
        if (degree >= 275.626f && degree <= 281.250f) {
            return String.format("Trạch Sơn Hàm 4/9\n(%.3f) Dậu-Tân", degree);
        }
        if (degree >= 281.251f && degree <= 286.875f) {
            return String.format("Hỏa Sơn Lữ 3/8\n(%.3f) Tân", degree);
        }
        if (degree >= 286.876f && degree <= 292.500f) {
            return String.format("Lôi Sơn Tiểu Quá 8/3\n(%.3f) Tân", degree);
        }
        if (degree >= 292.501f && degree <= 298.125f) {
            return String.format("Phong Sơn Tiệm 2/7\n(%.3f) Tuất", degree);
        }
        if (degree >= 298.126f && degree <= 303.750f) {
            return String.format("Thủy Sơn Kiển 7/2\n(%.3f) Tuất", degree);
        }
        if (degree >= 303.751f && degree <= 309.375f) {
            return String.format("Cấn Vi Sơn 6/1\n(%.3f) Tuất-Càn", degree);
        }
        if (degree >= 309.376f && degree <= 315.000f) {
            return String.format("Địa Sơn Khiêm 1/6\n(%.3f) Càn", degree);
        }
        if (degree >= 315.001f && degree <= 320.625f) {
            return String.format("Thiên Địa Bĩ 9/9\n(%.3f) Càn", degree);
        }
        if (degree >= 320.626f && degree <= 326.250f) {
            return String.format("Trạch Địa Tụy 4/4\n(%.3f) Càn-Hợi", degree);
        }
        if (degree >= 326.251f && degree <= 331.875f) {
            return String.format("Hỏa Địa Tấn 3/3\n(%.3f) Hợi", degree);
        }
        if (degree >= 331.876f && degree <= 337.500f) {
            return String.format("Lôi Địa Dự 8/8\n(%.3f) Hợi", degree);
        }
        if (degree >= 337.501f && degree <= 343.125f) {
            return String.format("Phong Địa Quán 2/2\n(%.3f) Nhâm", degree);
        }
        if (degree >= 343.126f && degree <= 348.750f) {
            return String.format("Thủy Địa Tỷ 7/7\n(%.3f) Nhâm", degree);
        }
        if (degree >= 348.751f && degree <= 354.375f) {
            return String.format("Sơn Địa Bác 6/6\n(%.3f) Nhâm-Tý", degree);
        }
        if (degree >= 354.376f && degree <= 360.000f) {
            return String.format("Khôn Vi Địa 1/1\n(%.3f) Nhâm-Tý", degree);
        }
        return "";
    }

    @Override
    public void onAnimationStart(Animation animation) {
        flashView.setVisibility(View.VISIBLE);
        btnCaptureScreen.setClickable(false);
        captureMapScreen();
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        flashView.setVisibility(View.GONE);
        btnCaptureScreen.setClickable(true);
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                if (place != null) {
                    Log.d(TAG, "onActivityResult: " + place.getName() + ", " + place.getLatLng());
                    centerPosition = place.getLatLng();
                    rotateMap(0f);
                    txtStatusBottom.setText(String.format("Kinh độ: %.3f - Vĩ độ: %.3f\nNgày đo: %s", place.getLatLng().longitude, place.getLatLng().latitude, today));
                    CameraPosition current = mMap.getCameraPosition();
                    Log.d(TAG, "onActivityResult: " + current.target);
                }
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
                Log.d(TAG, "onActivityResult: canceled");
            }
        }
    }
}
