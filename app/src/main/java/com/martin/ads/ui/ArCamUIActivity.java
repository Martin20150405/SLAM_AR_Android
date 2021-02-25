package com.martin.ads.ui;

/**
 * Created by Ads on 2017/3/9.
 */

import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.martin.ads.constant.GlobalConstant;
import com.martin.ads.rendering.render.ArObjectWrapper;
import com.martin.ads.rendering.render.ObjRendererWrapper;
import com.martin.ads.slamar.NativeHelper;
import com.martin.ads.slamar.R;
import com.martin.ads.rendering.render.GLES10Demo;
import com.martin.ads.rendering.gles.GLRootView;
import com.martin.ads.utils.FpsMeter;
import com.martin.ads.utils.TouchHelper;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ArCamUIActivity extends AppCompatActivity implements
        CameraGLViewBase.CvCameraViewListener2{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ar_ui_content);
        initToolbar();
        initView();
    }

    private void initToolbar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        mToolbar.setNavigationIcon(R.drawable.btn_back);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.context_menu:
                detectPlane=true;
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        System.exit(0);
    }

    private static final String    TAG = "SlamCamActivity";

    private Mat mRgba;
    private Mat                    mIntermediateMat;
    private Mat                    mGray;

    private CameraGLView mOpenCvCameraView;
    private boolean initFinished;

    private NativeHelper nativeHelper;
    TouchHelper touchHelper;

    private boolean detectPlane;

    private FpsMeter mFpsMeter = null;
    private TextView fpsText;

    private void initView(){
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        nativeHelper=new NativeHelper(this);
        mOpenCvCameraView = (CameraGLView) findViewById(R.id.my_fake_glsurface_view);
        mOpenCvCameraView.setVisibility(View.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        initFinished=false;

        touchHelper=new TouchHelper(this);
        initGLES10Demo();
        //initGLES20Demo();
        initGLES20Obj();

        View touchView=findViewById(R.id.touch_panel);
        touchView.setClickable(true);
        touchView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return touchHelper.handleTouchEvent(event);
            }
        });
        touchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Camera camera=mOpenCvCameraView.getCamera();
                if (camera!=null) camera.autoFocus(null);
            }
        });
        //touchView.bringToFront();
        mOpenCvCameraView.init();

        fpsText = findViewById(R.id.text_fps);
        mFpsMeter = new FpsMeter();
        mFpsMeter.setResolution(GlobalConstant.RESOLUTION_WIDTH, GlobalConstant.RESOLUTION_HEIGHT);
    }

    private void initGLES10Demo() {
        final GLRootView glRootView=findViewById(R.id.ar_object_view_gles1);
        glRootView.setAspectRatio(GlobalConstant.RESOLUTION_WIDTH,GlobalConstant.RESOLUTION_HEIGHT);

        GLES10Demo gles10Demo=
                GLES10Demo.newInstance()
                        .setArObjectView(glRootView)
                        .setNativeHelper(nativeHelper)
                        .setContext(this)
                        .init(touchHelper);

        nativeHelper.addOnMVPUpdatedCallback(gles10Demo);
    }

//    private void initGLES20Demo() {
//        final GLRootView glRootView=findViewById(R.id.ar_object_view_gles2_sphere);
//        glRootView.setAspectRatio(GlobalConstant.RESOLUTION_WIDTH,GlobalConstant.RESOLUTION_HEIGHT);
//
//        ArObjectWrapper arObjectWrapper=
//                ArObjectWrapper.newInstance()
//                        .setArObjectView(glRootView)
//                        .setNativeHelper(nativeHelper)
//                        .setContext(this)
//                        .init(touchHelper);
//        nativeHelper.addOnMVPUpdatedCallback(arObjectWrapper);
//    }

    private void initGLES20Obj() {
        final GLRootView glRootView=findViewById(R.id.ar_object_view_gles2_obj);
        glRootView.setAspectRatio(GlobalConstant.RESOLUTION_WIDTH,GlobalConstant.RESOLUTION_HEIGHT);

        ObjRendererWrapper objRendererWrapper=
                ObjRendererWrapper.newInstance()
                        .setArObjectView(glRootView)
                        .setNativeHelper(nativeHelper)
                        .setContext(this)
                        .setObjPath("patrick.obj")
                        .setTexturePath("Char_Patrick.png")
                        .setInitSize(0.20f)
//                        .setObjPath("andy.obj")
//                        .setTexturePath("andy.png")
//                        .setInitSize(1.0f)
                        .init(touchHelper);
        nativeHelper.addOnMVPUpdatedCallback(objRendererWrapper);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.d(TAG, "OpenCV library found inside package. Using it!");
        mOpenCvCameraView.enableView();

        if (!initFinished) {
            initFinished=true;
            String resDir = this.getExternalFilesDir("SLAM").getAbsolutePath()+"/";
            Log.d(TAG, "onResume: "+resDir);
            nativeHelper.initSLAM(resDir);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
    }

    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
        mIntermediateMat.release();
    }

    public Mat onCameraFrame(CameraGLViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        if(initFinished){
            //Log.d("JNI_", "onCameraFrame: new image coming");
            int trackingResult=nativeHelper.processCameraFrame(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr());
            if(detectPlane){
                showHint("Request sent.");
                int detectResult=nativeHelper.detectPlane();
                detectPlane=false;
            }
            //Log.d("JNI_", "onCameraFrame: new image finished");
        }

        mFpsMeter.measure();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fpsText.setText(mFpsMeter.getText());
            }
        });
        return mRgba;
    }
    private void showHint(final String str){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ArCamUIActivity.this,str,Toast.LENGTH_LONG).show();
            }
        });
    }
}