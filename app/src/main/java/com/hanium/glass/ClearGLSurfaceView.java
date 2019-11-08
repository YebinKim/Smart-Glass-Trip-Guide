package com.hanium.glass;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

class ClearGLSurfaceView extends GLSurfaceView {
    public ClearGLSurfaceView(Context context) {
        super(context);
        mRenderer = new ClearRenderer();
        setRenderer(mRenderer);
    }

    public boolean onTouchEvent(final MotionEvent event) {
        queueEvent(new Runnable(){
            public void run() {
                mRenderer.setColor(event.getX() / getWidth(),
                        event.getY() / getHeight(), 1.0f);
            }});
        return true;
    }

    ClearRenderer mRenderer;
}

//class ClearGLSurfaceView extends GLSurfaceView {
//    public ClearGLSurfaceView(Context context) {
//        super(context);
//
//        mRenderer = new ClearRenderer();
//        setRenderer(mRenderer);
//        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
//
//    }
//
//    public boolean onTouchEvent(final MotionEvent event) {
//        queueEvent(new Runnable() {
//            public void run() {
//
//                mRenderer.setColor(event.getX() / getWidth(),
//                        event.getY() / getHeight(),
//                        1.0f);
//
//
//                requestRender();
//
//            }
//        });
//        return true;
//    }
//
//    ClearRenderer mRenderer;
//}