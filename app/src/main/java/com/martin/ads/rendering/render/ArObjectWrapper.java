package com.martin.ads.rendering.render;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.view.View;

import com.martin.ads.constant.GlobalConstant;
import com.martin.ads.rendering.gles.Sphere3D;
import com.martin.ads.slamar.NativeHelper;
import com.martin.ads.utils.TouchHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by ads on 17-2-20.
 */

public class ArObjectWrapper implements GLSurfaceView.Renderer,NativeHelper.OnMVPUpdatedCallback{
    private GLSurfaceView arObjectView;
    private Context context;
    private Sphere3D sphere;

    private NativeHelper nativeHelper;
    private int surfaceWidth,surfaceHeight;

    private ArObjectWrapper() {

    }

    public static ArObjectWrapper newInstance(){
        return new ArObjectWrapper();
    }

    public ArObjectWrapper init(TouchHelper touchHelper){
        arObjectView.setEGLContextClientVersion(2);
        arObjectView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        arObjectView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        arObjectView.setZOrderOnTop(true);

        arObjectView.setRenderer(this);

        arObjectView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        //arObjectView.setClickable(true);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB){
            arObjectView.setPreserveEGLContextOnPause(true);
        }
        sphere=new Sphere3D(context);

        touchHelper.addScalingCallback(new TouchHelper.ScalingCallback() {
            @Override
            public void updateScale(float scaleFactor) {
                if (nativeHelper.getLastTrackingResult() == GlobalConstant.SLAM_ON) {
                    float zoomFac = (scaleFactor - 1.0f) / 5;
                    sphere.zoom(zoomFac);
                }
            }
        });
        return this;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        sphere.init();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        sphere.onFilterChanged(width,height);
        surfaceWidth=width;
        surfaceHeight=height;
        GLES20.glViewport(0,0,width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        sphere.onDrawFrame();
    }

    public ArObjectWrapper setArObjectView(GLSurfaceView arObjectView) {
        this.arObjectView = arObjectView;
        return this;
    }

    public ArObjectWrapper setContext(Context context) {
        this.context = context;
        return this;
    }

    public ArObjectWrapper setNativeHelper(NativeHelper nativeHelper) {
        this.nativeHelper = nativeHelper;
        return this;
    }

    @Override
    public void onUpdateModelMatrix(float[] M) {
        sphere.updateMatrix(M,null,null);
    }

    @Override
    public void onUpdateViewMatrix(float[] M) {
        sphere.updateMatrix(null,M,null);
    }

    @Override
    public void onUpdateProjectionMatrix(float[] M) {
        sphere.updateMatrix(null,null,M);
    }

    @Override
    public void requestReset() {
        sphere.reset();
    }

    @Override
    public void setDraw(boolean flag) {
        sphere.setDraw(flag);
    }
}
