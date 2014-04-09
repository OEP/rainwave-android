package cc.rainwave.android.views;

import java.util.Locale;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CountdownView extends View {
    private static final String rainwavens = "http://rainwave.cc";

    private float mMax = 100.0f;

    private float mPrimary = mMax;

    private float mSecondary = 0.0f;

    private boolean mShowValue = false;

    private String mAlternateText;

    private Rect mTextBounds = new Rect();

    private RectF mOvalPrimary = new RectF();

    private RectF mOvalSecondary = new RectF();

    private Paint mPaint = new Paint();

    public CountdownView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mMax = interpret(attrs, "max", mMax);
        mPrimary = interpret(attrs, "progress", mPrimary);
        mSecondary = interpret(attrs, "progressSecondary", mSecondary);
        mShowValue = interpret(attrs, "showValue", mShowValue);
        mAlternateText = attrs.getAttributeValue(rainwavens, "alternateText");
    }

    public float getPrimary() {
        return mPrimary;
    }

    public float getSecondary() {
        return mSecondary;
    }

    public void decrementPrimary(float howMuch) {
        setPrimary(mPrimary - howMuch);
        postInvalidate();
    }

    public void decrementSecondary(float howMuch) {
        setSecondary(mSecondary - howMuch);
        postInvalidate();
    }

    public void setShowValue(boolean value) {
        mShowValue = value;
        postInvalidate();
    }

    public void setMax(float value) {
        mMax = Math.max(0.0f, value);
        setPrimary(mPrimary);
        setSecondary(mSecondary);
        postInvalidate();
    }

    public void setPrimary(float value) {
        mPrimary = bound(value);
        postInvalidate();
    }

    public void setSecondary(float value) {
        mSecondary = bound(value);
        postInvalidate();
    }

    public void setBoth(float primary, float secondary) {
        setPrimary(primary);
        setSecondary(secondary);
    }

    public void setAlternateText(String s) {
        mAlternateText = s;
        postInvalidate();
    }

    public void setAlternateText(int rid) {
        setAlternateText(getContext().getResources().getString(rid));
    }

    public void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        float radiansPrimary = (float) (360.0f * mPrimary / mMax);
        float radiansSecondary = (float) (360.0f * mSecondary / mMax);
        float start = 270.0f;

        mOvalPrimary.set(0, 0, w, h);
        mOvalSecondary.set(w/4, h/4, 3 * w / 4, 3 * h / 4);

        mPaint.setAntiAlias(true);

        mPaint.setColor(0xAA00FF00);
        if(mPrimary > 0) canvas.drawArc(mOvalPrimary, start, radiansPrimary, true, mPaint);

        mPaint.setColor(0xAA0000FF);
        if(mSecondary > 0) canvas.drawArc(mOvalSecondary, start, radiansSecondary, true, mPaint);


        if(mShowValue) {
            mPaint.setColor(Color.WHITE);
            mPaint.setTextAlign(Align.CENTER);
            String text = (mPrimary > 0 || mAlternateText == null)
                    ? String.format(Locale.US, "%1.1f", mPrimary)
                    : mAlternateText;

            mPaint.getTextBounds(text, 0, text.length(), mTextBounds);
            float height = Math.abs(mTextBounds.top - mTextBounds.bottom);

            canvas.drawText(text, w/2, h/2 + height/2, mPaint);
        }
    }

    public void onMeasure(int widthSpec, int heightSpec) {
        int maxHeight = measureDimension(heightSpec);
        int maxWidth = measureDimension(widthSpec);
        int minDim = Math.min(maxHeight,maxWidth);
        int maxMin = Math.max(getSuggestedMinimumHeight(), getSuggestedMinimumWidth());
        int result = Math.max(minDim, maxMin);
        setMeasuredDimension(result, result);
    }

    private int measureDimension(int measureSpec) {
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        switch(mode) {
        case MeasureSpec.AT_MOST:
        case MeasureSpec.EXACTLY:
            return size;
        }
        return Integer.MAX_VALUE;
    }

    private float interpret(AttributeSet attrs, String key, float defaultValue) {
        String value = attrs.getAttributeValue(rainwavens, key);
        return (value != null) ? Float.valueOf(value) : defaultValue;
    }

    private boolean interpret(AttributeSet attrs, String key, boolean defaultValue) {
        String value = attrs.getAttributeValue(rainwavens, key);
        return (value != null) ? Boolean.valueOf(value) : defaultValue;
    }

    private float bound(float value) {
        return Math.max(0.0f, Math.min(value, mMax));
    }
}
