package com.martin.ads.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

import com.martin.ads.constant.GlobalConstant;
import com.martin.ads.utils.TextureUtils;
import com.martin.ads.rendering.gles.OrthoFilter;

import org.opencv.BuildConfig;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * This class is an implementation of the Bridge View between OpenCV and Java Camera.
 * This class relays on the functionality available in base class and only implements
 * required functions:
 * connectCamera - opens Java camera and sets the PreviewCallback to be delivered.
 * disconnectCamera - closes the camera and stops preview.
 * When frame is delivered via callback from Camera - it processed via OpenCV to be
 * converted to RGBA32 and then passed to the external callback for modifications if required.
 */
public class CameraGLView extends CameraGLViewBase implements PreviewCallback {

    private static final int MAGIC_TEXTURE_ID = 10;
    private static final String TAG = "JavaCameraView";

    private byte mBuffer[];
    private Mat[] mFrameChain;
    private int mChainIdx = 0;
    private Thread mThread;
    private boolean mStopThread;

    protected Camera mCamera;
    protected JavaCameraFrame[] mCameraFrame;
    private SurfaceTexture mSurfaceTexture;

    private OrthoFilter ortho;
    private Context context;

    public static class JavaCameraSizeAccessor implements ListItemAccessor {

        @Override
        public int getWidth(Object obj) {
            Camera.Size size = (Camera.Size) obj;
            return size.width;
        }

        @Override
        public int getHeight(Object obj) {
            Camera.Size size = (Camera.Size) obj;
            return size.height;
        }
    }

    public CameraGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context=context;
    }

    public void init(){
        setAspectRatio(GlobalConstant.RESOLUTION_WIDTH,GlobalConstant.RESOLUTION_HEIGHT);
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(false);

        setRenderer(new CameraGLRender());

        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB){
            setPreserveEGLContextOnPause(true);
        }
        ortho=new OrthoFilter(context);
    }
    protected boolean initializeCamera(int width, int height) {
        Log.d(TAG, "Initialize java camera");
        boolean result = true;
        synchronized (this) {
            mCamera = null;
            try {
                mCamera = Camera.open(getCameraIdWithFacing(Camera.CameraInfo.CAMERA_FACING_BACK));
            }
            catch (Exception e){
                Log.e(TAG, "Camera is not available (in use or does not exist): " + e.getLocalizedMessage());
            }

            if (mCamera == null)
                return false;

            /* Now set camera parameters */
            try {
                Camera.Parameters params = mCamera.getParameters();
                Log.d(TAG, "getSupportedPreviewSizes()");
                List<Camera.Size> sizes = params.getSupportedPreviewSizes();

                if (sizes != null) {
                    /* Select the size that fits surface considering maximum size allowed */
                    Size frameSize = calculateCameraFrameSize(sizes, new JavaCameraSizeAccessor(), width, height);

                    params.setPreviewFormat(ImageFormat.NV21);
                    Log.d(TAG, "Set preview size to " + Integer.valueOf((int)frameSize.width) + "x" + Integer.valueOf((int)frameSize.height));
                    params.setPreviewSize((int)frameSize.width, (int)frameSize.height);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && !Build.MODEL.equals("GT-I9100"))
                        params.setRecordingHint(true);

                    List<String> FocusModes = params.getSupportedFocusModes();
                    if (FocusModes != null && FocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
                    {
                        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                    }

                    mCamera.setParameters(params);
                    params = mCamera.getParameters();

                    mFrameWidth = params.getPreviewSize().width;
                    mFrameHeight = params.getPreviewSize().height;

                    int size = mFrameWidth * mFrameHeight;
                    size  = size * ImageFormat.getBitsPerPixel(params.getPreviewFormat()) / 8;
                    mBuffer = new byte[size];

                    mCamera.addCallbackBuffer(mBuffer);
                    mCamera.setPreviewCallbackWithBuffer(this);

                    mFrameChain = new Mat[2];
                    mFrameChain[0] = new Mat(mFrameHeight + (mFrameHeight/2), mFrameWidth, CvType.CV_8UC1);
                    mFrameChain[1] = new Mat(mFrameHeight + (mFrameHeight/2), mFrameWidth, CvType.CV_8UC1);

                    AllocateCache();

                    mCameraFrame = new JavaCameraFrame[2];
                    mCameraFrame[0] = new JavaCameraFrame(mFrameChain[0], mFrameWidth, mFrameHeight);
                    mCameraFrame[1] = new JavaCameraFrame(mFrameChain[1], mFrameWidth, mFrameHeight);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        mSurfaceTexture = new SurfaceTexture(MAGIC_TEXTURE_ID);
                        mCamera.setPreviewTexture(mSurfaceTexture);
                    } else
                       mCamera.setPreviewDisplay(null);

                    /* Finally we are ready to start the preview */
                    Log.d(TAG, "startPreview");
                    mCamera.startPreview();
                }
                else
                    result = false;
            } catch (Exception e) {
                result = false;
                e.printStackTrace();
            }
        }

        return result;
    }

    protected void releaseCamera() {
        synchronized (this) {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);

                mCamera.release();
            }
            mCamera = null;
            if (mFrameChain != null) {
                mFrameChain[0].release();
                mFrameChain[1].release();
            }
            if (mCameraFrame != null) {
                mCameraFrame[0].release();
                mCameraFrame[1].release();
            }
        }
    }

    private boolean mCameraFrameReady = false;

    @Override
    protected boolean connectCamera(int width, int height) {

        /* 1. We need to instantiate camera
         * 2. We need to start thread which will be getting frames
         */
        /* First step - initialize camera connection */
        Log.d(TAG, "Connecting to camera");
        if (!initializeCamera(width, height))
            return false;

        mCameraFrameReady = false;

        /* now we can start update thread */
        Log.d(TAG, "Starting processing thread");
        mStopThread = false;
        mThread = new Thread(new CameraWorker());
        mThread.start();

        return true;
    }

    @Override
    protected void disconnectCamera() {
        /* 1. We need to stop thread which updating the frames
         * 2. Stop camera and release it
         */
        Log.d(TAG, "Disconnecting from camera");
        try {
            mStopThread = true;
            Log.d(TAG, "Notify thread");
            synchronized (this) {
                this.notify();
            }
            Log.d(TAG, "Wating for thread");
            if (mThread != null)
                mThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mThread =  null;
        }

        /* Now release camera */
        releaseCamera();

        mCameraFrameReady = false;
    }

    @Override
    public void onPreviewFrame(byte[] frame, Camera arg1) {
        Log.d(TAG, "onPreviewFrame: "+Thread.currentThread().getId());
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Preview Frame received. Frame size: " + frame.length);
        synchronized (this) {
            mFrameChain[mChainIdx].put(0, 0, frame);
            mCameraFrameReady = true;
            this.notify();
        }
        if (mCamera != null)
            mCamera.addCallbackBuffer(mBuffer);
    }

    private class JavaCameraFrame implements CvCameraViewFrame {
        @Override
        public Mat gray() {
            return mYuvFrameData.submat(0, mHeight, 0, mWidth);
        }

        @Override
        public Mat rgba() {
            Imgproc.cvtColor(mYuvFrameData, mRgba, Imgproc.COLOR_YUV2RGBA_NV21, 4);
            return mRgba;
        }

        public JavaCameraFrame(Mat Yuv420sp, int width, int height) {
            super();
            mWidth = width;
            mHeight = height;
            mYuvFrameData = Yuv420sp;
            mRgba = new Mat();
        }

        public void release() {
            mRgba.release();
        }

        private Mat mYuvFrameData;
        private Mat mRgba;
        private int mWidth;
        private int mHeight;
    };

    private class CameraWorker implements Runnable {

        @Override
        public void run() {
            do {
                boolean hasFrame = false;
                synchronized (CameraGLView.this) {
                    try {
                        while (!mCameraFrameReady && !mStopThread) {
                            CameraGLView.this.wait();
                        }
                        Log.d(TAG, "run: "+Thread.currentThread().getId());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (mCameraFrameReady)
                    {
                        mChainIdx = 1 - mChainIdx;
                        mCameraFrameReady = false;
                        hasFrame = true;
                    }
                }

                if (!mStopThread && hasFrame) {
                    if (!mFrameChain[1 - mChainIdx].empty())
                        deliverAndDrawFrame(mCameraFrame[1 - mChainIdx]);
                }
            } while (!mStopThread);
            Log.d(TAG, "Finish processing thread");
        }
    }
    public Camera getCamera() {
        return mCamera;
    }

    //Camera.CameraInfo.CAMERA_FACING_FRONT
    //Camera.CameraInfo.CAMERA_FACING_BACK
    private int getCameraIdWithFacing(int facing){
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == facing) {
                return camIdx;
            }
        }
        return 0;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        synchronized(mSyncObject) {
            mSurfaceExist = false;
            checkCurrentState();
            ortho.destroy();
        }
    }

    class CameraGLRender implements GLSurfaceView.Renderer {
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            Bitmap bitmap= Bitmap.createBitmap(GlobalConstant.RESOLUTION_WIDTH,GlobalConstant.RESOLUTION_HEIGHT, Bitmap.Config.ARGB_8888);
            imageTextureId= TextureUtils.loadTexture(bitmap,0);
            bitmap.recycle();
            ortho.init();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            Log.d(TAG, "call surfaceChanged event");
            synchronized(mSyncObject) {
                if (!mSurfaceExist) {
                    mSurfaceExist = true;
                    checkCurrentState();
                } else {
                    /** Surface changed. We need to stop camera and restart with new parameters */
                /* Pretend that old surface has been destroyed */
                    mSurfaceExist = false;
                    checkCurrentState();
                /* Now use new surface. Say we have it now */
                    mSurfaceExist = true;
                    checkCurrentState();
                }
            }
            ortho.onSurfaceChanged(gl,width,height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
            ortho.onDrawFrame(imageTextureId);
        }
    }
}
