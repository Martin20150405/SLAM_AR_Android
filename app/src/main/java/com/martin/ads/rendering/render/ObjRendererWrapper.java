package com.martin.ads.rendering.render;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Build;
import android.util.Log;

import com.martin.ads.constant.GlobalConstant;
import com.martin.ads.slamar.NativeHelper;
import com.martin.ads.utils.TouchHelper;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class ObjRendererWrapper implements GLSurfaceView.Renderer,NativeHelper.OnMVPUpdatedCallback{
    private static final String TAG = "ObjRendererWrapper";
    private GLSurfaceView arObjectView;
    private Context context;

    private final ObjectRenderer virtualObject;

    private NativeHelper nativeHelper;
    private int surfaceWidth,surfaceHeight;

    private float[] projectionMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] modelMatrix = new float[16];
    private float[] tmpMatrix = new float[16];

    private boolean shouldDraw;
    public float scaleFactor;

    private String objPath;
    private String texturePath;
    private float initSize;

    private ObjRendererWrapper() {
        virtualObject=new ObjectRenderer();
    }

    public static ObjRendererWrapper newInstance(){
        return new ObjRendererWrapper();
    }

    public ObjRendererWrapper init(TouchHelper touchHelper){
        arObjectView.setPreserveEGLContextOnPause(true);
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

        touchHelper.addScalingCallback(new TouchHelper.ScalingCallback() {
            @Override
            public void updateScale(float scaleFactor) {
                if (nativeHelper.getLastTrackingResult() == GlobalConstant.SLAM_ON) {
                    float zoomFac = scaleFactor - 1.0f;
                    zoomModel(zoomFac);
                }
            }
        });
        return this;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Prepare the other rendering objects.
        try {
            // virtualObject.createOnGlThread(/*context=*/ this, "andy.obj", "andy.png");
            virtualObject.createOnGlThread(/*context=*/ context, objPath,texturePath);
            virtualObject.setMaterialProperties(0.0f, 3.5f, 1.0f, 6.0f);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read obj file");
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        surfaceWidth=width;
        surfaceHeight=height;
        GLES20.glViewport(0,0,width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        if(!shouldDraw) return;
        System.arraycopy(modelMatrix,0,tmpMatrix,0,modelMatrix.length);
        //Matrix.translateM(tmpMatrix,0,0,-modelRadius,0);
        Matrix.rotateM(tmpMatrix,0, 180.0f, 1.0f, 0.0f, 0.0f);

        // Compute lighting from average intensity of the image.
        final float lightIntensity = 0.5f;
        //frame.getLightEstimate().getPixelIntensity();

//        Logger.logMatrix(modelMatrix,"Model");
//        Logger.logMatrix(viewMatrix,"View");
//        Logger.logMatrix(projectionMatrix,"Projection");

        virtualObject.updateModelMatrix(tmpMatrix, initSize*scaleFactor);
        virtualObject.draw(viewMatrix, projectionMatrix, lightIntensity);
    }

    public ObjRendererWrapper setArObjectView(GLSurfaceView arObjectView) {
        this.arObjectView = arObjectView;
        return this;
    }

    public ObjRendererWrapper setContext(Context context) {
        this.context = context;
        return this;
    }

    public ObjRendererWrapper setNativeHelper(NativeHelper nativeHelper) {
        this.nativeHelper = nativeHelper;
        return this;
    }

    public ObjRendererWrapper setObjPath(String objPath) {
        this.objPath = objPath;
        return this;
    }

    public ObjRendererWrapper setTexturePath(String texturePath) {
        this.texturePath = texturePath;
        return this;
    }

    public ObjRendererWrapper setInitSize(float initSize) {
        this.initSize = initSize;
        return this;
    }

    public void zoomModel(float delta) {
        scaleFactor = clamp(scaleFactor, delta);
    }

    private float clamp(float val, float delta) {
        val += delta;
        val = Math.max(0.005f, Math.min(5.0f, val));
        return val;
    }

    private void resetDelta() {
        shouldDraw=false;
        scaleFactor=1.0f;
    }
    @Override
    public void onUpdateModelMatrix(float[] M) {
        NativeHelper.copyMatrix(M,modelMatrix);
    }

    @Override
    public void onUpdateViewMatrix(float[] M) {
        NativeHelper.copyMatrix(M,viewMatrix);
    }

    @Override
    public void onUpdateProjectionMatrix(float[] M) {
        NativeHelper.copyMatrix(M,projectionMatrix);
    }

    @Override
    public void requestReset() {
        resetDelta();
    }

    @Override
    public void setDraw(boolean flag) {
        shouldDraw=flag;
    }
}
