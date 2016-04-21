package com.tcd.aransena.steer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by aransena on 21/04/16.
 */
public class TeleopControl extends View {

    private Paint mPiePaint;
    private Paint mTextPaint;
    private Paint mShadowPaint;
    private int mTextColor;
    private float mTextHeight = 0.0f;

    private Paint mTPaint;
    private float mTWidth = 0.0f;


    public TeleopControl(Context context){//, AttributeSet attrs){
        //super(context,attrs);
        super(context);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int cW = canvas.getWidth();
        int cH = canvas.getHeight();
        canvas.drawCircle((float)cW/2,(float)cH/2, (float)cW/2-100,mTPaint);

        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Try for a width based on our minimum
        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int w = resolveSizeAndState(minw, widthMeasureSpec, 1);

        // Whatever the width ends up being, ask for a height that would let the pie
        // get as big as it can
        int minh = MeasureSpec.getSize(w) - (int)mTWidth + getPaddingBottom() + getPaddingTop();
        int h = resolveSizeAndState(MeasureSpec.getSize(w) - (int)mTWidth, heightMeasureSpec, 0);

        setMeasuredDimension(w, h);
    }

    private void init() {
        mTPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTPaint.setColor(getResources().getColor(R.color.colorAccent));
        mTPaint.setStyle(Paint.Style.FILL);
    }


}