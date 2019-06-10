package com.martin.ads.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.opengl.GLES20;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Ads on 2016/11/8.
 */
public class BitmapUtils {

    public static Bitmap loadBitmapFromAssets(Context context,String filePath){
        InputStream inputStream = null;
        try {
            inputStream = context.getResources().getAssets().open(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(inputStream==null) return null;
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inScaled=false;
        Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
        return bitmap;
    }

    public static Bitmap loadBitmapFromRaw(Context context, int resourceId){
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inScaled=false;
        Bitmap bitmap= BitmapFactory.decodeResource(context.getResources(),resourceId,options);
        return bitmap;
    }
}
