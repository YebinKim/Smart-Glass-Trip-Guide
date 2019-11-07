package com.hanium.glass;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class NavigationActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final Size RESOLUTION = new Size(1920, 1080);
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;

    private ArrayList<features> geoList = new ArrayList<>();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mSurfaceHolder = surfaceView.getHolder();
        mSurfaceHolder.addCallback(this);

        jsonParsing(getJsonString());

        Log.d ("Json", geoList.get(0).getType());
        Log.d ("Json", geoList.get(0).getCoordinates().getLongitude().toString());
        Log.d ("Json", geoList.get(0).getCoordinates().getLatitude().toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
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

            for(int i = 0; i < featArray.length(); i++) {
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
