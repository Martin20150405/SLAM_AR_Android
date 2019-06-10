package com.martin.ads.rendering.gles;

import android.content.Context;
import android.opengl.GLES20;

import com.martin.ads.utils.ShaderUtils;


/**
 * Created by Ads on 2016/11/19.
 */

public abstract class GLAbsProgram {
    private int mProgramId;
    private String mVertexShader;
    private String mFragmentShader;

    private int maPositionHandle;
    private int maTextureHandle;

    public GLAbsProgram(Context context
            , final int vertexShaderResourceId
            , final int fragmentShaderResourceId){
        mVertexShader = ShaderUtils.readRawTextFile(context, vertexShaderResourceId);
        mFragmentShader= ShaderUtils.readRawTextFile(context, fragmentShaderResourceId);
    }

    public void create(){
        mProgramId = ShaderUtils.createProgram(mVertexShader, mFragmentShader);
        if (mProgramId == 0) {
            return;
        }

        maPositionHandle = GLES20.glGetAttribLocation(getProgramId(), "aPosition");
        ShaderUtils.checkGlError("glGetAttribLocation aPosition");
        if (maPositionHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aPosition");
        }
        maTextureHandle = GLES20.glGetAttribLocation(getProgramId(), "aTextureCoord");
        ShaderUtils.checkGlError("glGetAttribLocation aTextureCoord");
        if (maTextureHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aTextureCoord");
        }
    }

    public void use(){
        GLES20.glUseProgram(getProgramId());
        ShaderUtils.checkGlError("glUseProgram");
    }

    public int getProgramId() {
        return mProgramId;
    }

    public void onDestroy(){
        GLES20.glDeleteProgram(mProgramId);
    }


    public int getMaPositionHandle() {
        return maPositionHandle;
    }

    public int getMaTextureHandle() {
        return maTextureHandle;
    }
}
