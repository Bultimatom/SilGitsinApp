package com.bultimatom.silgitsin;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

public class ZoomableImageView extends AppCompatImageView {
    private static final float MIN_SCALE = 1f;
    private static final float MAX_SCALE = 5f;

    private final Matrix imageMatrix = new Matrix();
    private final ScaleGestureDetector scaleDetector;
    private final GestureDetector gestureDetector;
    private float currentScale = MIN_SCALE;
    private float lastX;
    private float lastY;

    public ZoomableImageView(@NonNull Context context) {
        this(context, null);
    }

    public ZoomableImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setScaleType(ScaleType.MATRIX);
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(@NonNull MotionEvent event) {
                float targetScale = currentScale > MIN_SCALE ? MIN_SCALE : 2.5f;
                float factor = targetScale / currentScale;
                imageMatrix.postScale(factor, factor, event.getX(), event.getY());
                currentScale = targetScale;
                constrainMatrix();
                return true;
            }
        });
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        resetImage();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                lastY = event.getY();
                return true;
            case MotionEvent.ACTION_MOVE:
                if (!scaleDetector.isInProgress() && currentScale > MIN_SCALE) {
                    float dx = event.getX() - lastX;
                    float dy = event.getY() - lastY;
                    imageMatrix.postTranslate(dx, dy);
                    constrainMatrix();
                }
                lastX = event.getX();
                lastY = event.getY();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                performClick();
                return true;
            default:
                return true;
        }
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private void resetImage() {
        Drawable drawable = getDrawable();
        if (drawable == null || getWidth() == 0 || getHeight() == 0) {
            return;
        }

        float drawableWidth = drawable.getIntrinsicWidth();
        float drawableHeight = drawable.getIntrinsicHeight();
        if (drawableWidth <= 0f || drawableHeight <= 0f) {
            return;
        }

        float fitScale = Math.min(getWidth() / drawableWidth, getHeight() / drawableHeight);
        float dx = (getWidth() - drawableWidth * fitScale) / 2f;
        float dy = (getHeight() - drawableHeight * fitScale) / 2f;
        imageMatrix.reset();
        imageMatrix.postScale(fitScale, fitScale);
        imageMatrix.postTranslate(dx, dy);
        currentScale = MIN_SCALE;
        setImageMatrix(imageMatrix);
    }

    private void constrainMatrix() {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }

        RectF bounds = new RectF(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        imageMatrix.mapRect(bounds);
        float dx = 0f;
        float dy = 0f;

        if (bounds.width() <= getWidth()) {
            dx = getWidth() / 2f - bounds.centerX();
        } else if (bounds.left > 0f) {
            dx = -bounds.left;
        } else if (bounds.right < getWidth()) {
            dx = getWidth() - bounds.right;
        }

        if (bounds.height() <= getHeight()) {
            dy = getHeight() / 2f - bounds.centerY();
        } else if (bounds.top > 0f) {
            dy = -bounds.top;
        } else if (bounds.bottom < getHeight()) {
            dy = getHeight() - bounds.bottom;
        }

        imageMatrix.postTranslate(dx, dy);
        setImageMatrix(imageMatrix);
    }

    private final class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(@NonNull ScaleGestureDetector detector) {
            float targetScale = Math.max(
                    MIN_SCALE,
                    Math.min(currentScale * detector.getScaleFactor(), MAX_SCALE)
            );
            float factor = targetScale / currentScale;
            imageMatrix.postScale(factor, factor, detector.getFocusX(), detector.getFocusY());
            currentScale = targetScale;
            constrainMatrix();
            return true;
        }
    }
}
