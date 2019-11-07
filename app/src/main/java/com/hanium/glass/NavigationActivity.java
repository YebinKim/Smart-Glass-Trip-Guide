package com.hanium.glass;

import android.graphics.Movie;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Size;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class NavigationActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final Size[] RESOLUTIONS = {
            new Size(640, 480),
            new Size(1280, 720),
            new Size(1920, 1080),
    };

    private int mResolutionIndex;

    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;

    private ArrayList<features> geoArray = new ArrayList<>();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mSurfaceHolder = surfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mResolutionIndex = 0;
    }

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

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    mResolutionIndex--;
                    changeResolution();
                    break;
                case KeyEvent.KEYCODE_DPAD_UP:
                    mResolutionIndex++;
                    changeResolution();
                    break;
                default:
                    break;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void changeResolution() {
        mCamera.stopPreview();
        Camera.Parameters parameters = mCamera.getParameters();
        Size resolution = getResolution();
        parameters.setPreviewSize(resolution.getWidth(), resolution.getHeight());
        mCamera.setParameters(parameters);
        mCamera.startPreview();
    }

    private Size getResolution() {
        if (RESOLUTIONS.length <= mResolutionIndex) {
            mResolutionIndex = 0;
        } else if (mResolutionIndex < 0) {
            mResolutionIndex = RESOLUTIONS.length - 1;
        }
        return RESOLUTIONS[mResolutionIndex];
    }
}
