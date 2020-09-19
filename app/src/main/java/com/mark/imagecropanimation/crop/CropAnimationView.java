package com.mark.imagecropanimation.crop;

import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.RectEvaluator;
import android.animation.TypeEvaluator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

public class CropAnimationView extends View {

    private Bitmap mBitmap;
    private Paint mPaint;

    private Rect srcRect = new Rect();
    private Rect dscRect = new Rect();

    private Matrix transformMatrix = new Matrix();
    private Matrix endMatrix = new Matrix();
    private Matrix startMatrix = new Matrix();

    public FourPoint startValues = new FourPoint();
    public FourPoint endValues = new FourPoint();

    private FourPoint animationValues = new FourPoint();

    private ObjectAnimator objectAnimator;

    private AnimatorListenerAdapter animatorListenerAdapter;

    public void setAnimatorListenerAdapter(AnimatorListenerAdapter listenerAdapter){
        this.animatorListenerAdapter = listenerAdapter;
    }

    public CropAnimationView(Context context) {
        this(context, null);
    }

    public CropAnimationView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropAnimationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(LAYER_TYPE_SOFTWARE,null);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, transformMatrix, mPaint);
        }
    }

    public void startCropAnimation(){
        if (objectAnimator != null) {
            objectAnimator.start();
            return;
        }
        PropertyValuesHolder valuesHolder = PropertyValuesHolder.ofObject("animationValues",
                new FourPoint.FourPointEvaluator(),
                startValues,
                endValues
        );

        PropertyValuesHolder clipHolder = PropertyValuesHolder.ofObject("clipBounds",
                new RectEvaluator(),
                srcRect,
                dscRect
        );

        objectAnimator = ObjectAnimator.ofPropertyValuesHolder(this, valuesHolder,clipHolder);
        objectAnimator.setDuration(800);

        if (animatorListenerAdapter != null) {
            objectAnimator.addListener(animatorListenerAdapter);
        }
        objectAnimator.start();
    }


    public FourPoint getAnimationValues() {
        return animationValues;
    }

    public void setAnimationValues(FourPoint values) {
        this.animationValues = values;
        transformMatrix.setPolyToPoly(startValues.getValues(),0,values.getValues(),0,4);
        transformMatrix.preConcat(startMatrix);
        invalidate();
    }


    public void setStartMatrix(Bitmap bitmap, int viewWidth, int viewHeight) {
        this.mBitmap = bitmap;
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();

        RectF srcRectF = new RectF(0, 0, bitmapWidth, bitmapHeight);
        RectF dstRectF = new RectF(0, 0, viewWidth, viewHeight);

        startMatrix.setRectToRect(srcRectF, dstRectF, Matrix.ScaleToFit.CENTER);

        transformMatrix.set(startMatrix);

        srcRect.set(0,0,viewWidth, viewHeight);
    }


    public void setEndMatrix(Point[] startCropPoints, Rect endRect) {
        dscRect.set(endRect);
        int length = startCropPoints.length;
        for (int i = 0; i < length; i++) {
            Point startCropPoint = startCropPoints[i];
            startValues.getValues()[i * 2] = startCropPoint.x;
            startValues.getValues()[i * 2 + 1] = startCropPoint.y;
        }

        endValues.getValues()[0] = endRect.left;
        endValues.getValues()[1] = endRect.top;
        endValues.getValues()[2] = endRect.right;
        endValues.getValues()[3] = endRect.top;
        endValues.getValues()[4] = endRect.right;
        endValues.getValues()[5] = endRect.bottom;
        endValues.getValues()[6] = endRect.left;
        endValues.getValues()[7] = endRect.bottom;

        endMatrix.setPolyToPoly(startValues.getValues(), 0, endValues.getValues(), 0, 4);
    }

    public void setEndBitmap(Bitmap bitmap, int viewWidth, int viewHeight) {
        this.mBitmap = bitmap;
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();

        RectF srcRectF = new RectF(0, 0, bitmapWidth, bitmapHeight);
        RectF dstRectF = new RectF(0, 0, viewWidth, viewHeight);

        endMatrix.setRectToRect(srcRectF, dstRectF, Matrix.ScaleToFit.CENTER);
        transformMatrix.set(endMatrix);
        invalidate();
    }


    private static class FourPoint {

        private float[] values = new float[8];

        public FourPoint() {
        }

        public float[] getValues(){
            return values;
        }


        public void setValues(float ...data){
            for (int i = 0; i < data.length; i++) {
                values[i] = data[i];
            }
        }

        private static class FourPointEvaluator implements TypeEvaluator<FourPoint> {

            private FourPoint fourPoint = new FourPoint();

            @Override
            public FourPoint evaluate(float fraction, FourPoint startValue, FourPoint endValue) {
                int length = startValue.getValues().length;
                for (int i = 0; i < length; i++) {
                    float start = startValue.getValues()[i];
                    float end = endValue.getValues()[i];
                    fourPoint.getValues()[i] = start + (fraction*(end - start)) ;
                }
                return fourPoint;
            }
        }

    }


}
