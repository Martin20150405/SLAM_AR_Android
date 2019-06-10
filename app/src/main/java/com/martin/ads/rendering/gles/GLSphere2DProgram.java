package com.martin.ads.rendering.gles;

import android.content.Context;
import android.opengl.GLES20;

import com.martin.ads.slamar.R;
import com.martin.ads.utils.ShaderUtils;


/**
 * Created by Ads on 2016/11/8.
 * draw texture2D on sphere
 * with MVP/Sampler2D
 */
public class GLSphere2DProgram extends GLAbsProgram {

    private int muMVPMatrixHandle;
    private int uTextureSamplerHandle;

    public GLSphere2DProgram(Context context){
        super(context, R.raw.vertex_shader_pass_through,R.raw.fragment_shader_pass_through);
    }

    @Override
    public void create(){
        super.create();

        muMVPMatrixHandle = GLES20.glGetUniformLocation(getProgramId(), "uMVPMatrix");
        ShaderUtils.checkGlError("glGetUniformLocation uMVPMatrix");
        if (muMVPMatrixHandle == -1) {
            throw new RuntimeException("Could not get attrib location for uMVPMatrix");
        }
        uTextureSamplerHandle= GLES20.glGetUniformLocation(getProgramId(),"sTexture");
        ShaderUtils.checkGlError("glGetUniformLocation uniform samplerExternalOES sTexture");
    }

    public int getMuMVPMatrixHandle() {
        return muMVPMatrixHandle;
    }

    public int getUTextureSamplerHandle() {
        return uTextureSamplerHandle;
    }
}
