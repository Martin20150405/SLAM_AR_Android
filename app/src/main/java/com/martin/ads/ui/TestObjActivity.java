package com.martin.ads.ui;

import android.opengl.GLES10;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;

import com.martin.ads.rendering.render.ObjectRenderer;
import com.martin.ads.slamar.R;
import com.martin.ads.utils.BufferUtils;
import com.martin.ads.utils.Logger;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import androidx.appcompat.app.AppCompatActivity;

public class TestObjActivity extends AppCompatActivity implements GLSurfaceView.Renderer{
    private static final String TAG = "MainActivity";
    private GLSurfaceView surfaceView;
    private final ObjectRenderer virtualObject = new ObjectRenderer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        surfaceView = findViewById(R.id.surfaceview);

        surfaceView.setPreserveEGLContextOnPause(true);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
        surfaceView.setRenderer(this);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        surfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        surfaceView.onPause();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        // Prepare the other rendering objects.
        try {
           // virtualObject.createOnGlThread(/*context=*/ this, "andy.obj", "andy.png");
            virtualObject.createOnGlThread(/*context=*/ this, "patrick.obj", "Char_Patrick.png");
            virtualObject.setMaterialProperties(0.0f, 3.5f, 1.0f, 6.0f);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read obj file");
        }

    }

    private int width,height;

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        this.width=width;
        this.height=height;
    }

    float rotateDeg=0;
    @Override
    public void onDrawFrame(GL10 gl) {
        // Clear screen to notify driver it should not load any pixels from previous frame.
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        //GLES20.glClearColor(0.0f, 1.0f, 1.0f, 1.0f);
        rotateDeg+=1;
        // Get projection matrix.
        float[] modelMatrix = new float[16];
        float[] tmp = new float[16];
        Matrix.setIdentityM(tmp,0);
        Matrix.translateM(modelMatrix,0,tmp,0,0,0,-2);
        Matrix.rotateM(modelMatrix, 0, rotateDeg, 0.0f, 1.0f, 0.0f);
        float[] projectionMatrix = new float[16];
        Matrix.perspectiveM(projectionMatrix, 0, 90, (float)width/height, 1f, 500f);
        // Get camera matrix and draw.
        float[] viewMatrix = new float[16];
        Matrix.setIdentityM(viewMatrix,0);
        Matrix.setLookAtM(viewMatrix, 0,
                0.0f, 0.0f, 0.0f,
                0.0f, 0.0f,-1.0f,
                0.0f, 10.0f, 0.0f);
        // Compute lighting from average intensity of the image.
        final float lightIntensity = 0.5f;
                //frame.getLightEstimate().getPixelIntensity();

        // Visualize anchors created by touch.
        float scaleFactor = 1.0f;

        Logger.logMatrix(modelMatrix,"Model");
        Logger.logMatrix(viewMatrix,"View");
        Logger.logMatrix(projectionMatrix,"Projection");

        virtualObject.updateModelMatrix(modelMatrix, scaleFactor);
        virtualObject.draw(viewMatrix, projectionMatrix, lightIntensity);
    }
}
