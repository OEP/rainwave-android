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

public class PagerWidget extends View {
	
	private static final String androidns = "http://schemas.android.com/apk/res/android";
	
	private int mCount = 2;
	
	private int mCurrent = 0;
	
	public PagerWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		setCount( attrs.getAttributeResourceValue(androidns, "max", mCount) );
		setCurrent( attrs.getAttributeResourceValue(androidns, "value", mCurrent) );
	}
	
	public int getCurrent() {
		return mCurrent;
	}
	
	public int getCount() {
		return mCount;
	}
	
	public void setCurrent(int current) {
		mCurrent = Math.max(0, Math.min(current, mCount-1));
		invalidate();
	}
	
	public void setCount(int count) {
		mCount = Math.max(1, count);
		invalidate();
	}
	
	public void onDraw(Canvas canvas) {
		Paint p = new Paint();
		p.setAntiAlias(true);
		int h = getHeight();
		int w = getWidth();
		float barWidth = (w - (getCount() - 1) * DEFAULT_SPACE) / getCount();
		
		for(int i = 0; i < getCount(); i++) {
			float x = i * (barWidth + DEFAULT_SPACE);
			p.setColor((getCurrent() == i) ? COLOR_HILIGHT : COLOR_DEFAULT);
			canvas.drawRect(x, 0, x+barWidth, h, p);
		}
	}
	
	public static final int
		COLOR_DEFAULT = 0x55FFFFFF,
		COLOR_HILIGHT = 0xFF00BB00;
	
	public static final float
		DEFAULT_SPACE = 5.0f;
}
