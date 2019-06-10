package com.martin.ads.rendering.gles;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.martin.ads.rendering.obj.Ball;


/**
 * Created by Ads on 2016/11/19.
 */

public class Sphere3D{
    private static final String TAG = "Sphere3D";
    private static boolean USE_OGL_COORDINATE=true;

    private static boolean OGL_DEBUG=true;

    private Ball sphere;
    private GLSphere2DProgram glSphereProgram;

    private float[] modelMatrix = new float[16];
    private float[] tmpMatrix = new float[16];

    private float[] viewMatrix = new float[16];
    private float[] projectionMatrix = new float[16];

    private float[] modelViewMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private float ratio;
    private float radius;
    private float currentRadius=0;

    private boolean draw;
    private BitmapTexture bitmapTexture;
    private Context context;
    private float rotateDegree;

    public Sphere3D(Context context) {
        sphere=new Ball();
        this.context=context;
        glSphereProgram =new GLSphere2DProgram(context);
        bitmapTexture=new BitmapTexture();
        reset();
    }

    public void reset() {
        radius=0.14f;
        rotateDegree=0.0f;
        draw=false;
        initMatrix();
    }

    public void init() {
        glSphereProgram.create();
        bitmapTexture.load(context,"texture_360_n.jpg");
    }

    public void destroy() {
        glSphereProgram.onDestroy();
    }

    public void onDrawFrame() {
        if(draw){
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA,GLES20.GL_ONE_MINUS_SRC_ALPHA);
            if(Math.abs(currentRadius-radius)>0.005f){
                sphere=sphere.build(radius,75,150);
                currentRadius=radius;
            }

            rotateDegree+=1f;
            if(rotateDegree>360) rotateDegree-=360.0f;
            System.arraycopy(modelMatrix,0,tmpMatrix,0,modelMatrix.length);
            Matrix.translateM(tmpMatrix,0,0,-radius,0);
            Matrix.rotateM(tmpMatrix,0, rotateDegree, 0.0f, 1.0f, 0.0f);

            glSphereProgram.use();
            sphere.uploadTexCoordinateBuffer(glSphereProgram.getMaTextureHandle());
            sphere.uploadVerticesBuffer(glSphereProgram.getMaPositionHandle());

            //P * V * M * T
            Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, tmpMatrix, 0);
            Matrix.multiplyMM(mMVPMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);

            //Log.d("Logger", "after mMVPMatrix: ");
            //Logger.logMatrix(mMVPMatrix);

            GLES20.glUniformMatrix4fv(glSphereProgram.getMuMVPMatrixHandle(), 1, false, mMVPMatrix, 0);

            bindTexture2D(bitmapTexture.getImageTextureId(), GLES20.GL_TEXTURE0,glSphereProgram.getUTextureSamplerHandle(),0);

            sphere.draw();
        }
    }

    public void onFilterChanged(int width, int height) {
        ratio=(float)width/ height;
        Matrix.perspectiveM(projectionMatrix, 0, 90, ratio, 1f, 500f);
    }

    private void initMatrix() {
        Matrix.setIdentityM(modelMatrix,0);
        Matrix.setIdentityM(viewMatrix, 0);
        Matrix.setLookAtM(viewMatrix, 0,
                0.0f, 10.0f, 10.0f,
                0.0f, 0.0f,-1.0f,
                0.0f, 1.0f, 0.0f);
    }


    public void updateMatrix(float []mM,float[] vM,float[] pM){
        Log.d(TAG, "updateMatrix: ");
        if(mM!=null){
            System.arraycopy(mM,0,modelMatrix,0,modelMatrix.length);
        }
        if(pM!=null){
            System.arraycopy(pM,0,projectionMatrix,0,projectionMatrix.length);
        }
        if(vM!=null){
            System.arraycopy(vM,0,viewMatrix,0,viewMatrix.length);
        }
    }

    public void setDraw(boolean draw) {
        this.draw = draw;
    }

    public void zoom(float delta){
        radius+=delta;
        radius=Math.max(0.03f,radius);
    }

    public static void bindTexture2D(int textureId,int activeTextureID,int handle,int idx){
        if (textureId != 0) {
            GLES20.glActiveTexture(activeTextureID);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glUniform1i(handle, idx);
        }
    }
}
