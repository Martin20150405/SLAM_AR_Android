package com.martin.ads.rendering.gles;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.martin.ads.rendering.obj.Plain;
import com.martin.ads.slamar.R;
import com.martin.ads.utils.TextureUtils;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Ads on 2016/11/19.
 * let the image pass through
 * and simply fit the image to the screen
 */

public class OrthoFilter{

    private GLPassThroughProgram glPassThroughProgram;
    private Plain plain;

    private float[] projectionMatrix = new float[16];
    private int surfaceWidth,surfaceHeight;
    public OrthoFilter(Context context) {
        glPassThroughProgram=new GLPassThroughProgram(context, R.raw.vertex_shader_pass_through,R.raw.fragment_shader_pass_through);
        plain=new Plain();
        Matrix.setIdentityM(projectionMatrix,0);
    }

    public void init() {
        glPassThroughProgram.create();
    }

    public void destroy() {
        glPassThroughProgram.onDestroy();
    }

    public void onSurfaceChanged(GL10 gl, int width, int height){
        this.surfaceWidth=width;
        this.surfaceHeight=height;
    }

    public void onDrawFrame(int textureId) {
        glPassThroughProgram.use();
        plain.uploadTexCoordinateBuffer(glPassThroughProgram.getMaTextureHandle());
        plain.uploadVerticesBuffer(glPassThroughProgram.getMaPositionHandle());
        GLES20.glUniformMatrix4fv(glPassThroughProgram.getMVPMatrixHandle(), 1, false, projectionMatrix, 0);

        TextureUtils.bindTexture2D(textureId, GLES20.GL_TEXTURE0,glPassThroughProgram.getTextureSamplerHandle(),0);
        GLES20.glViewport(0,0,surfaceWidth,surfaceHeight);
        plain.draw();
    }

}
