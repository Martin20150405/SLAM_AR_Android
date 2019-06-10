package com.martin.ads.utils;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;


/**
 * Created by Ads on 2016/11/19.
 */

public class TextureUtils{
    private static final String TAG = "TextureUtils";

    public static int loadTexture(final Bitmap img, final int usedTexId) {
        int textures[] = new int[1];
        if (usedTexId == 0) {
            GLES20.glGenTextures(1, textures, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, img, 0);
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, usedTexId);

            GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, img);
            textures[0] = usedTexId;
        }
        return textures[0];
    }


    public static int getTextureFromBitmap(Bitmap bitmap,int imageSize[]){
        final int[] textureObjectIds=new int[1];
        GLES20.glGenTextures(1,textureObjectIds,0);
        if (textureObjectIds[0]==0){
            Log.d(TAG,"Failed at glGenTextures");
            return 0;
        }

        if (bitmap==null){
            Log.d(TAG,"Failed at decoding bitmap");
            GLES20.glDeleteTextures(1,textureObjectIds,0);
            return 0;
        }

        if(imageSize!=null && imageSize.length>=2){
            imageSize[0]=bitmap.getWidth();
            imageSize[1]=bitmap.getHeight();
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureObjectIds[0]);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,bitmap,0);
        bitmap.recycle();

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);
        return textureObjectIds[0];
    }

    public static void bindTexture2D(int textureId,int activeTextureID,int handle,int idx){
        if (textureId !=0) {
            GLES20.glActiveTexture(activeTextureID);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glUniform1i(handle, idx);
        }
    }
}
