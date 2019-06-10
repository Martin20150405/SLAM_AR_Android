package com.martin.ads.rendering.render;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLES10;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.martin.ads.constant.GlobalConstant;
import com.martin.ads.slamar.NativeHelper;
import com.martin.ads.utils.BufferUtils;
import com.martin.ads.utils.TouchHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by ads on 17-2-21.
 */

public class GLES10Demo implements GLSurfaceView.Renderer,NativeHelper.OnMVPUpdatedCallback {
    private static final String TAG = "GLES10Demo";
    private GLSurfaceView arObjectView;

    private NativeHelper nativeHelper;

    private int surfaceWidth, surfaceHeight;

    private float modelRadius;
    private float planeRadius;
    private float scaleFactor;

    private Context context;
    private TouchHelper touchHelper;

    public float deltaX;
    public float deltaY;
    public float deltaZ;

    public float planeDeltaX;
    public float planeDeltaY;
    public float planeDeltaZ;

    public float rotateY;

    private float[] projectionMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] modelMatrix = new float[16];

    private boolean shouldDraw;

    private GLES10Demo() {
        resetDelta();
    }

    public static GLES10Demo newInstance() {
        return new GLES10Demo();
    }

    public GLES10Demo init(TouchHelper touchHelper1) {
        //arObjectView.setEGLContextClientVersion(2);
        touchHelper = touchHelper1;
        touchHelper.addScalingCallback(new TouchHelper.ScalingCallback() {
            @Override
            public void updateScale(float scaleFactor) {
                if (nativeHelper.getLastTrackingResult() == GlobalConstant.SLAM_ON) {
                    float zoomFac = scaleFactor - 1.0f;
                    zoomModel(zoomFac);
                }
            }
        });

        arObjectView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        arObjectView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        arObjectView.setZOrderOnTop(true);
        arObjectView.setRenderer(this);

        arObjectView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        //arObjectView.setClickable(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            arObjectView.setPreserveEGLContextOnPause(true);
        }
        return this;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        surfaceWidth = width;
        surfaceHeight = height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        drawGLDemo(surfaceWidth,surfaceHeight,modelMatrix,viewMatrix,projectionMatrix);
    }

    public GLES10Demo setArObjectView(GLSurfaceView arObjectView) {
        this.arObjectView = arObjectView;
        return this;
    }

    public GLES10Demo setNativeHelper(NativeHelper nativeHelper) {
        this.nativeHelper = nativeHelper;
        return this;
    }

    private float clamp(float val, float delta) {
        val += delta;
        val = Math.max(0.005f, Math.min(5.0f, val));
        return val;
    }

    public void zoomModel(float delta) {
        scaleFactor = clamp(scaleFactor, delta);
    }

    public GLES10Demo setContext(Context context) {
        this.context = context;
        return this;
    }

    public void updateDeltaXYZ(float dx, float dy, float dz) {
        Log.d(TAG, "updateDeltaXYZ: ");
        dx /= 100;
        dy /= 100;
        dz /= 100;
        deltaX += dx;
        deltaY += dy;
        deltaZ += dz;
    }

    public void updatePlaneDeltaXYZ(float dx, float dy, float dz) {
        Log.d(TAG, "updatePlaneDeltaXYZ: ");
        dx /= 100;
        dy /= 100;
        dz /= 100;
        planeDeltaX += dx;
        planeDeltaY += dy;
        planeDeltaZ += dz;
    }

    private void resetDelta() {
        deltaX = 0.0f;
        deltaY = 0.0f;
        deltaZ = 0.0f;
        planeDeltaX = 0.0f;
        planeDeltaY = 0.0f;
        planeDeltaZ = 0.0f;
        rotateY = 0.0f;
        shouldDraw=false;
        modelRadius = planeRadius = 0.05f;
        scaleFactor=1.0f;
    }

    private void drawGLDemo(int width, int height, float modelMatrix[], float viewMatrix[], float projectionMatrix[]) {
        GLES10.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES10.glClear(GLES10.GL_DEPTH_BUFFER_BIT | GLES10.GL_COLOR_BUFFER_BIT);
        if(!shouldDraw) return;
        GLES10.glEnable(GLES10.GL_DEPTH_TEST);
        GLES10.glEnable(GLES10.GL_BLEND);
        GLES10.glBlendFunc(GLES10.GL_SRC_ALPHA, GLES10.GL_ONE_MINUS_SRC_ALPHA);
        GLES10.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GLES10.glViewport(0, 0, width, height);
        GLES10.glClear(GLES10.GL_DEPTH_BUFFER_BIT);
        //////////P
        GLES10.glMatrixMode(GLES10.GL_PROJECTION);
        GLES10.glLoadMatrixf(BufferUtils.getFloatBuffer(projectionMatrix,0));
        //////////V
        GLES10.glMatrixMode(GLES10.GL_MODELVIEW);
        GLES10.glLoadMatrixf(BufferUtils.getFloatBuffer(viewMatrix,0));
        //////////M
        GLES10.glPushMatrix();
        GLES10.glMultMatrixf(BufferUtils.getFloatBuffer(modelMatrix,0));
        /////////MVP pushed.
        //rotate all around Y
        GLES10.glRotatef(rotateY, 0, 1, 0);

        drawPlane(10,
                planeRadius*scaleFactor,
                planeDeltaX,
                planeDeltaY,
                planeDeltaZ
        );

        drawCube(modelRadius*scaleFactor, deltaX, deltaY, deltaZ);
        GLES10.glPopMatrix();
    }

    private void drawColouredCube(float axis_min, float axis_max)  {

        float l = axis_min;
        float h = axis_max;

        float verts[] = {
                l, l, h, h, l, h, l, h, h, h, h, h,  // FRONT differ in Z CCW
                l, l, l, l, h, l, h, l, l, h, h, l,  // BACK CW
                l, l, h, l, h, h, l, l, l, l, h, l,  // LEFT differ in X
                h, l, l, h, h, l, h, l, h, h, h, h,  // RIGHT
                l * 2, h, h * 2, h * 2, h, h * 2, l * 2, h, l * 2, h * 2, h, l * 2,  // TOP differ in  Y CW
                l, l, h, l, l, l, h, l, h, h, l, l   // BOTTOM CW
        };

        GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, BufferUtils.getFloatBuffer(verts,0));
        GLES10.glEnableClientState(GLES10.GL_VERTEX_ARRAY);

        GLES10.glDisable(GLES10.GL_CULL_FACE);

//        GLES10.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
//        GLES10.glDrawArrays(GLES10.GL_TRIANGLE_STRIP, 0, 4); //后：红色
//        GLES10.glColor4f(1.0f, 0.0f, 1.0f, 1.0f);
//        GLES10.glDrawArrays(GLES10.GL_TRIANGLE_STRIP, 4, 4); //前 紫色
//
//        GLES10.glColor4f(0.0f, 1.0f, 0.0f, 1.0f);
//        GLES10.glDrawArrays(GLES10.GL_TRIANGLE_STRIP, 8, 4); //左边 绿色
//        GLES10.glColor4f(1.0f, 1.0f, 0.0f, 1.0f);
//        GLES10.glDrawArrays(GLES10.GL_TRIANGLE_STRIP, 12, 4); //右边 黄色

        GLES10.glColor4f(0.0f, 1.0f, 1.0f, 0.3f);
        GLES10.glDrawArrays(GLES10.GL_TRIANGLE_STRIP, 16, 4); //底部 青色
//        GLES10.glColor4f(0.0f, 0.0f, 1.0f, 1.0f);
//        GLES10.glDrawArrays(GLES10.GL_TRIANGLE_STRIP, 20, 4); //顶部 蓝色

        GLES10.glDisableClientState(GLES10.GL_VERTEX_ARRAY);
    }

    private void drawCube(float size, float x, float y, float z) {
        GLES10.glPushMatrix();
        float[] M = new float[16];
        Matrix.setIdentityM(M, 0);
        M[12] = -x;
        M[13] = -size - y;
        M[14] = -z;
        GLES10.glMultMatrixf(BufferUtils.getFloatBuffer(M,0));
        drawColouredCube(-size, size);
        GLES10.glPopMatrix();
    }

    private void drawPlane(int ndivs, float ndivsize, float x, float y, float z) {
        GLES10.glPushMatrix();
        float[] M = new float[16];
        Matrix.setIdentityM(M, 0);
        M[12] = -x;
        M[13] = -y;
        M[14] = -z;
        GLES10.glMultMatrixf(BufferUtils.getFloatBuffer(M,0));

        // Plane parallel to x-z at origin with normal -y
        float minx = -ndivs * ndivsize;
        float minz = -ndivs * ndivsize;
        float maxx = ndivs * ndivsize;
        float maxz = ndivs * ndivsize;


        GLES10.glLineWidth(2);
        GLES10.glColor4f(0.7f, 0.7f, 1.0f, 1.0f);
        int TOT_LINES = (2 * ndivs + 1) * 4;
        float[] verts = new float[TOT_LINES * 3];
        int tot = 0;

        for (int n = 0; n <= 2 * ndivs; n++) {
            verts[tot++] = minx + ndivsize * n;
            verts[tot++] = 0;
            verts[tot++] = minz;

            verts[tot++] = minx + ndivsize * n;
            verts[tot++] = 0;
            verts[tot++] = maxz;

            verts[tot++] = minx;
            verts[tot++] = 0;
            verts[tot++] = minz + ndivsize * n;

            verts[tot++] = maxx;
            verts[tot++] = 0;
            verts[tot++] = minz + ndivsize * n;
        }
        GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, BufferUtils.getFloatBuffer(verts,0));
        GLES10.glEnableClientState(GLES10.GL_VERTEX_ARRAY);
        GLES10.glDrawArrays(GLES10.GL_LINES, 0, TOT_LINES);
        GLES10.glDisableClientState(GLES10.GL_VERTEX_ARRAY);

        GLES10.glPopMatrix();
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
