package com.martin.ads.rendering.gles;


import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * Created by Ads on 2017/2/14.
 */

public class GLRootView extends GLSurfaceView {
    private int surfaceWidth;
    private int surfaceHeight;
    private double surfaceRatio;

    public GLRootView(Context context) {
        super(context);
    }

    public GLRootView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        surfaceWidth = width;
        surfaceHeight = height;
        surfaceRatio=(double)surfaceWidth/surfaceHeight;
        RelativeLayout.LayoutParams layoutParams =
                new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        setLayoutParams(layoutParams);
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == surfaceWidth || 0 == surfaceHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * surfaceRatio) {
                setMeasuredDimension(width, (int) (width / surfaceRatio));
            } else {
                setMeasuredDimension((int) (height * surfaceRatio), height);
            }
        }
    }
}