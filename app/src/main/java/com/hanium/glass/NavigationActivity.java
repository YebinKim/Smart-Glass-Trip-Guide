package com.hanium.glass;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NavigationActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener {

    private static final Size RESOLUTION = new Size(1920, 1080);
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;

    TextView timeText;

    private ArrayList<features> geoList = new ArrayList<>();

    private GPSTracker gpsTracker;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    private GLSurfaceView mGLView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        findViewById(R.id.exitButton).setOnClickListener(this);

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mSurfaceHolder = surfaceView.getHolder();
        mSurfaceHolder.addCallback(this);

        mGLView = new GLSurfaceView(this);
//        setContentView(mGLView);
        mGLView.setRenderer(new ClearRenderer());

        // 시간 업데이트 스레드
        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                updateTime();
                            }

                        });

                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();

//        if (!checkLocationServicesStatus()) {
//            showDialogForLocationServiceSetting();
//        } else {
//            checkRunTimePermission();
//        }
//
//        gpsTracker = new GPSTracker(NavigationActivity.this);
//
//        double latitude = gpsTracker.getLatitude();
//        double longitude = gpsTracker.getLongitude();
//
//        String address = getCurrentAddress(latitude, longitude);
//        Log.d("Location", String.valueOf(latitude + " " + longitude));

        // JSON 파싱 호출
        jsonParsing(getJsonString());

        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        final LocationListener gpsLocationListener = new LocationListener() {
            public void onLocationChanged(Location location) {

                String provider = location.getProvider();

                double longitude = location.getLongitude();
                double latitude = location.getLatitude();

                Log.d("Location", String.valueOf(longitude + " " + latitude));

                short direct = bearingP1toP2(latitude, longitude, geoList.get(1).getCoordinates().getLatitude(), geoList.get(1).getCoordinates().getLongitude());
                Log.d("diretion", String.valueOf(direct));
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

//        short direct = bearingP1toP2(latitude, longitude, geoList.get(1).getCoordinates().getLatitude(), geoList.get(1).getCoordinates().getLongitude());

        Log.d("Json", geoList.get(0).getType());
        Log.d("Json", geoList.get(0).getCoordinates().getLongitude().toString());
        Log.d("Json", geoList.get(0).getCoordinates().getLatitude().toString());
//        Log.d("diretion", String.valueOf(direct));
    }

    @Override
    public void onClick(View v) {
        Log.d("aa", "aaaaa");
        switch (v.getId()) {
            case R.id.exitButton:
                this.finish();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLView.onPause();
    }

    // 카메라 설정
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mCamera = Camera.open();
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        changeResolution();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private void changeResolution() {
        mCamera.stopPreview();
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(RESOLUTION.getWidth(), RESOLUTION.getHeight());
        mCamera.setParameters(parameters);
        mCamera.startPreview();
    }

    public short bearingP1toP2(double P1_latitude, double P1_longitude, double P2_latitude, double P2_longitude) {
        // 현재 위치 : 위도나 경도는 지구 중심을 기반으로 하는 각도이기 때문에 라디안 각도로 변환한다.
        double Cur_Lat_radian = P1_latitude * (3.141592 / 180);
        double Cur_Lon_radian = P1_longitude * (3.141592 / 180);


        // 목표 위치 : 위도나 경도는 지구 중심을 기반으로 하는 각도이기 때문에 라디안 각도로 변환한다.
        double Dest_Lat_radian = P2_latitude * (3.141592 / 180);
        double Dest_Lon_radian = P2_longitude * (3.141592 / 180);

        // radian distance
        double radian_distance = 0;
        radian_distance = Math.acos(Math.sin(Cur_Lat_radian) * Math.sin(Dest_Lat_radian) + Math.cos(Cur_Lat_radian) * Math.cos(Dest_Lat_radian) * Math.cos(Cur_Lon_radian - Dest_Lon_radian));

        // 목적지 이동 방향을 구한다.(현재 좌표에서 다음 좌표로 이동하기 위해서는 방향을 설정해야 한다. 라디안값이다.
        double radian_bearing = Math.acos((Math.sin(Dest_Lat_radian) - Math.sin(Cur_Lat_radian) * Math.cos(radian_distance)) / (Math.cos(Cur_Lat_radian) * Math.sin(radian_distance)));        // acos의 인수로 주어지는 x는 360분법의 각도가 아닌 radian(호도)값이다.

        double true_bearing = 0;
        if (Math.sin(Dest_Lon_radian - Cur_Lon_radian) < 0) {
            true_bearing = radian_bearing * (180 / 3.141592);
            true_bearing = 360 - true_bearing;
        } else {
            true_bearing = radian_bearing * (180 / 3.141592);
        }

        return (short) true_bearing;
    }

    // 시간 업데이트
    private void updateTime() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        String nowTime = timeFormat.format(date);

        timeText = (TextView) findViewById(R.id.timeText);
        timeText.setText(nowTime);
    }

//    /*
//     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
//     */
//    @Override
//    public void onRequestPermissionsResult(int permsRequestCode,
//                                           @NonNull String[] permissions,
//                                           @NonNull int[] grandResults) {
//
//        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
//            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
//            boolean check_result = true;
//
//            // 모든 퍼미션을 허용했는지 체크합니다.
//            for (int result : grandResults) {
//                if (result != PackageManager.PERMISSION_GRANTED) {
//                    check_result = false;
//                    break;
//                }
//            }
//
//            if (check_result) {
//                // 위치 값을 가져올 수 있음
//            } else {
//                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.
//                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
//                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
//                    Toast.makeText(NavigationActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
//                    finish();
//                } else {
//                    Toast.makeText(NavigationActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();
//                }
//            }
//
//        }
//    }
//
//    void checkRunTimePermission() {
//        // 런타임 퍼미션 처리
//        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
//        int hasFineLocationPermission = ContextCompat.checkSelfPermission(NavigationActivity.this,
//                Manifest.permission.ACCESS_FINE_LOCATION);
//        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(NavigationActivity.this,
//                Manifest.permission.ACCESS_COARSE_LOCATION);
//
//        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
//                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
//
//            // 2. 이미 퍼미션을 가지고 있다면
//            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)
//
//            // 3.  위치 값을 가져올 수 있음
//
//        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.
//            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
//            if (ActivityCompat.shouldShowRequestPermissionRationale(NavigationActivity.this, REQUIRED_PERMISSIONS[0])) {
//                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
//                Toast.makeText(NavigationActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
//                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
//                ActivityCompat.requestPermissions(NavigationActivity.this, REQUIRED_PERMISSIONS,
//                        PERMISSIONS_REQUEST_CODE);
//            } else {
//                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
//                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
//                ActivityCompat.requestPermissions(NavigationActivity.this, REQUIRED_PERMISSIONS,
//                        PERMISSIONS_REQUEST_CODE);
//            }
//        }
//    }
//
//    public String getCurrentAddress(double latitude, double longitude) {
//        //지오코더... GPS를 주소로 변환
//        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
//        List<Address> addresses;
//
//        try {
//            addresses = geocoder.getFromLocation(
//                    latitude,
//                    longitude,
//                    7);
//        } catch (IOException ioException) {
//            //네트워크 문제
//            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
//            return "지오코더 서비스 사용불가";
//        } catch (IllegalArgumentException illegalArgumentException) {
//            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
//            return "잘못된 GPS 좌표";
//        }
//
//        if (addresses == null || addresses.size() == 0) {
//            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
//            return "주소 미발견";
//        }
//
//        Address address = addresses.get(0);
//        return address.getAddressLine(0).toString() + "\n";
//    }
//
//    //여기부터는 GPS 활성화를 위한 메소드들
//    private void showDialogForLocationServiceSetting() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(NavigationActivity.this);
//        builder.setTitle("위치 서비스 비활성화");
//        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
//                + "위치 설정을 수정하실래요?");
//        builder.setCancelable(true);
//
//        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int id) {
//                Intent callGPSSettingIntent
//                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
//            }
//        });
//
//        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int id) {
//                dialog.cancel();
//            }
//        });
//
//        builder.create().show();
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        switch (requestCode) {
//            case GPS_ENABLE_REQUEST_CODE:
//                //사용자가 GPS 활성 시켰는지 검사
//                if (checkLocationServicesStatus()) {
//                    Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
//                    checkRunTimePermission();
//                    return;
//                }
//                break;
//        }
//    }
//
//    public boolean checkLocationServicesStatus() {
//        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//
//        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
//                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//    }

    // - JSON 통신 설정
    private String getJsonString() {
        String json = "";

        try {
            InputStream is = getAssets().open("features.json");
            int fileSize = is.available();

            byte[] buffer = new byte[fileSize];
            is.read(buffer);
            is.close();

            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return json;
    }

    // JSON 파싱
    private void jsonParsing(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);

            JSONArray featArray = jsonObject.getJSONArray("features");

            for (int i = 0; i < featArray.length(); i++) {
                JSONObject featObject = featArray.getJSONObject(i);

                JSONObject geoObject = featObject.getJSONObject("geometry");

                features feature = new features();

                feature.setType(geoObject.getString("type"));
                feature.setCoordinates(geoObject.getString("coordinates"));

                geoList.add(feature);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
