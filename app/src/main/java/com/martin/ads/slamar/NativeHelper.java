package com.martin.ads.slamar;

import android.content.Context;
import android.util.Log;

import com.martin.ads.constant.GlobalConstant;
import com.martin.ads.rendering.render.GLES10Demo;
import com.martin.ads.rendering.gles.Sphere3D;
import com.martin.ads.utils.Logger;

import java.util.ArrayList;

/**
 * Created by Ads on 2017/1/30.
 */

public class NativeHelper {
    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("SLAM_CORE");
        System.loadLibrary("SLAM_JNI");
    }
    private static final String TAG = "NativeHelper";

    private ArrayList<OnMVPUpdatedCallback> onMVPUpdatedCallbacks;
    private float[] projectionMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] modelMatrix = new float[16];
    private int lastTrackingResult;
    private int planeDetectResult;
    private boolean planeDetected;

    private int[] statusBuf=new int[233];

    private Context context;

    public NativeHelper(Context context) {
        this.context=context;
        onMVPUpdatedCallbacks=new ArrayList<>();
        planeDetected=false;
    }

    public int processCameraFrame(long matAddrGr, long matAddrRgba){
        Log.d("JNI_", "processCameraFrame: new image");
        nativeProcessFrameMat(matAddrGr,matAddrRgba,statusBuf);
        lastTrackingResult =statusBuf[0];
        if(lastTrackingResult==GlobalConstant.SLAM_ON){
            getV(viewMatrix);
            for(OnMVPUpdatedCallback onMVPUpdatedCallback:onMVPUpdatedCallbacks){
                onMVPUpdatedCallback.onUpdateViewMatrix(viewMatrix);
            }
        }
        for (OnMVPUpdatedCallback onMVPUpdatedCallback : onMVPUpdatedCallbacks) {
            boolean shouldDraw=lastTrackingResult==GlobalConstant.SLAM_ON && planeDetected;
            onMVPUpdatedCallback.setDraw(shouldDraw);
        }
        return lastTrackingResult;
    }

    public int detectPlane(){
        detect(statusBuf);
        planeDetectResult =statusBuf[1];
        if(planeDetectResult == GlobalConstant.PLANE_DETECTED) {
            planeDetected = true;
            getM(modelMatrix);
            getP(GlobalConstant.RESOLUTION_WIDTH, GlobalConstant.RESOLUTION_HEIGHT, projectionMatrix);
            for (OnMVPUpdatedCallback onMVPUpdatedCallback : onMVPUpdatedCallbacks) {
                onMVPUpdatedCallback.requestReset();
                onMVPUpdatedCallback.onUpdateModelMatrix(modelMatrix);
                onMVPUpdatedCallback.onUpdateProjectionMatrix(projectionMatrix);
            }
        }else planeDetected=false;
        for (OnMVPUpdatedCallback onMVPUpdatedCallback : onMVPUpdatedCallbacks) {
            boolean shouldDraw=lastTrackingResult==GlobalConstant.SLAM_ON && planeDetected;
            onMVPUpdatedCallback.setDraw(shouldDraw);
        }
        return planeDetectResult;
    }
    //bitmap/camera+ mat + pure mono slam
    private native void nativeProcessFrameMat(long matAddrGr, long matAddrRgba,int []statusBuf);
    private native void detect(int []statusBuf);
    public native void initSLAM(String path);


    private native void getM(float modelM[]);
    private native void getV(float viewM[]);
    private native void getP(int imageWidth,int imageHeight,float projectionM[]);

    public int getLastTrackingResult() {
        return lastTrackingResult;
    }

    public void addOnMVPUpdatedCallback(OnMVPUpdatedCallback onMVPUpdatedCallback){
        onMVPUpdatedCallbacks.add(onMVPUpdatedCallback);
    }

    public interface OnMVPUpdatedCallback{
        void onUpdateModelMatrix(float M[]);
        void onUpdateViewMatrix(float M[]);
        void onUpdateProjectionMatrix(float M[]);
        void requestReset();
        void setDraw(boolean flag);
    }

    public static void copyMatrix(float inM[],float outM[]){
        if(inM.length!=outM.length) throw new RuntimeException("copyMatrix: unequal length");
        System.arraycopy(inM,0,outM,0,inM.length);
    }
}
