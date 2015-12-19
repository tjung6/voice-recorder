package com.deitel.voicerecorder;

import android.view.animation.Animation;
import android.view.animation.Transformation;

public class SeamlessAnimation extends Animation {

    private float mFromDegrees;

    private float mToDegrees;

    private float mPivotX;

    private float mPivotY;

    private int mPivotXType;

    private float mPivotXValue;

    private int mPivotYType;

    private float mPivotYValue;

    private boolean mCancelled;

    private float mDegree;

    public SeamlessAnimation(float fromDegrees, float toDegrees, int pivotXType, float pivotXValue,
            int pivotYType, float pivotYValue) {
        mFromDegrees = fromDegrees;
        mToDegrees = toDegrees;
        mPivotXType = pivotXType;
        mPivotXValue = pivotXValue;
        mPivotYType = pivotYType;
        mPivotYValue = pivotYValue;
        mCancelled = false;
        mDegree = fromDegrees;
    }

    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        mPivotX = resolveSize(mPivotXType, mPivotXValue, width, parentWidth);
        mPivotY = resolveSize(mPivotYType, mPivotYValue, height, parentHeight);
    }

    public float getDegree() {
        return mDegree;
    }

    @Override
    public void cancel() {
        super.cancel();
        mCancelled = true;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        if (!mCancelled) {
            mDegree = mFromDegrees + ((mToDegrees - mFromDegrees) * interpolatedTime);
        }
        t.getMatrix().setRotate(mDegree, mPivotX, mPivotY);
    }

}
