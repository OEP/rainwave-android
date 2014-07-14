/*
 * Copyright (c) 2013, Paul M. Kilgo
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * 
 * * Neither the name of Paul Kilgo nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package cc.rainwave.android.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class PagerWidget extends View {

    private static final String androidns = "http://schemas.android.com/apk/res/android";

    private int mCount = 2;

    private int mCurrent = 0;

    private Paint mPaint = new Paint();

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
        mPaint.setAntiAlias(true);
        int h = getHeight();
        int w = getWidth();
        float barWidth = (w - (getCount() - 1) * DEFAULT_SPACE) / getCount();

        for(int i = 0; i < getCount(); i++) {
            float x = i * (barWidth + DEFAULT_SPACE);
            mPaint.setColor((getCurrent() == i) ? COLOR_HILIGHT : COLOR_DEFAULT);
            canvas.drawRect(x, 0, x+barWidth, h, mPaint);
        }
    }

    public static final int
        COLOR_DEFAULT = 0x55FFFFFF,
        COLOR_HILIGHT = 0xFF00BB00;

    public static final float
        DEFAULT_SPACE = 5.0f;
}
