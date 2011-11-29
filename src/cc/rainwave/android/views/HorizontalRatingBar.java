package cc.rainwave.android.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

public class HorizontalRatingBar extends View {
	
	private static final String androidns = "http://schemas.android.com/apk/res/android";
	
	private String mLabel;
	
	private Resources mResources;
	
	private int mColorPrimary = DEFAULT_PRIMARY;
	
	private int mColorPrimaryShadow = DEFAULT_PRIMARY_SHADOW;
	
	private int mColorSecondary = DEFAULT_SECONDARY;
	
	private int mColorSecondaryShadow = DEFAULT_SECONDARY_SHADOW;
	
	private int mColorTicks = DEFAULT_TICKS;
	
	private float mPrimary = 0.0f;
	
	private float mSecondary = 0.0f;
	
	private float mMax = 5.0f;
	
	private float mCornerRadius = DEFAULT_RADIUS;
	
	private float mMinorIncrement = 0.5f;
	
	private float mMajorIncrement = 1.0f;

	public HorizontalRatingBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mResources = context.getResources();
		int resId = attrs.getAttributeResourceValue(androidns, "text", -1);
		
		if(resId >= 0) {
			setLabel(resId);
		}
	}
	
	
	public float getPrimary() {
		return mPrimary;
	}
	
	public float getSecondary() {
		return mSecondary;
	}
	
	public float getMax() {
		return mMax;
	}
	
	public void setLabel(String label) {
		mLabel = label;
		postInvalidate();
	}
	
	public void setLabel(int resId) {
		setLabel(mResources.getString(resId));
		postInvalidate();
	}
	
	public void setPrimaryValue(float value) {
		mPrimary = Math.max(0, Math.min(mMax, value));
		postInvalidate();
	}
	
	public void setSecondaryValue(float value) {
		mSecondary = Math.max(0, Math.min(mMax, value));
		postInvalidate();
	}
	
	public void setBothValues(float primary, float secondary) {
		setPrimaryValue(primary);
		setSecondaryValue(secondary);
	}

	public void onDraw(Canvas canvas) {
		Paint p = new Paint();
		p.setAntiAlias(true);
		
		drawPrimary(canvas, p);
		drawSecondary(canvas, p);
		drawTicks(canvas, p);
		drawLabel(canvas, p);
	}
	
	public float snapPositionToMinorIncrement(float x) {
		float pxPerMinorIncrement = scale(getWidth(), mMinorIncrement);
		float minorTicks = x / pxPerMinorIncrement;
		return Math.round(minorTicks) * mMinorIncrement;
	}
	
	private float scale(int scaleTarget, float arbitrary) {
		return (arbitrary * scaleTarget / mMax);
	}
	
	private float scalePrimary(int value) {
		return scale(value, mPrimary);
	}
	
	private float scaleSecondary(int value) {
		return scale(value, mSecondary);
	}
	
	private void drawPrimary(Canvas canvas, Paint p) {
		p.setShader(new LinearGradient(getWidth()/2, 0.0f, getWidth()/2, getHeight(),
			mColorPrimary, mColorPrimaryShadow, Shader.TileMode.CLAMP)
		);
		p.setPathEffect(new CornerPathEffect(mCornerRadius));
		
		canvas.drawRect(0, 0, scalePrimary(getWidth()), getHeight(), p);
	}
	
	private void drawSecondary(Canvas canvas, Paint p) {
		p.setShader(new LinearGradient(getWidth()/2, 0.0f, getWidth()/2, getHeight(),
			mColorSecondary, mColorSecondaryShadow, Shader.TileMode.CLAMP)
		);
		p.setPathEffect(null);
		
		canvas.drawRect(0, getHeight()-DEFAULT_SECONDARY_HEIGHT, scaleSecondary(getWidth()), getHeight(), p);
	}
	
	private void drawTicks(Canvas canvas, Paint p) {
		p.setShader(null);
		p.setPathEffect(null);
		p.setAntiAlias(false);
		p.setColor(mColorTicks);
		
		canvas.drawLine(0, getHeight()-1, getWidth(), getHeight()-1, p);
		canvas.drawLine(0, 0, 0, getHeight(), p);
		canvas.drawLine(getWidth()-1, 0, getWidth()-1, getHeight(), p);
		
		// Minor ticks.
		for(float f = mMinorIncrement; f <= mMax; f += mMinorIncrement) {
			int x = (int) scale(getWidth(), f);
			canvas.drawLine(x, getHeight() - DEFAULT_SECONDARY_HEIGHT/2, x, getHeight(), p);
		}
		
		// Major ticks.
		for(float f = mMajorIncrement; f <= mMax; f += mMajorIncrement) {
			int x = (int) scale(getWidth(), f);
			canvas.drawLine(x, getHeight() - DEFAULT_SECONDARY_HEIGHT, x, getHeight(), p);
		}
	}
	
	private void drawLabel(Canvas canvas, Paint p) {
		if(mLabel == null) return;
		
		p.setAntiAlias(true);
		int fontHeight = getHeight() - 2*DEFAULT_SECONDARY_HEIGHT;
		p.setTextSize(fontHeight);
		p.setTextAlign(Align.CENTER);
		
		canvas.drawText(mLabel, getWidth() / 2, fontHeight, p);
	}
	
	public static final float
		DEFAULT_RADIUS = 5.0f;
	
	public static final int
		DEFAULT_SECONDARY_HEIGHT = 10,
		DEFAULT_PRIMARY = 0xFF00FF00,
		DEFAULT_PRIMARY_SHADOW = 0xFF00AA00,
		DEFAULT_SECONDARY = 0xFF0000FF,
		DEFAULT_SECONDARY_SHADOW = 0xFF0000AA,
		DEFAULT_TICKS = 0xFFFFFFFF;
}
