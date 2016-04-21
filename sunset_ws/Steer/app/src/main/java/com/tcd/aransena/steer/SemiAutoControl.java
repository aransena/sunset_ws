package com.tcd.aransena.steer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;
import android.view.View;

/**
 * Created by aransena on 21/04/16.
 */
public class SemiAutoControl extends View{
    private int mTouchCount = 0;
    private final String LOG_TAG = SemiAutoControl.class.getSimpleName();


    public SemiAutoControl(Context context){//, AttributeSet attrs){
        super(context);
        Log.v(LOG_TAG, "in constructor");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //Log.v(LOG_TAG, "in onDraw");
        if(mTouchCount==0) {
            fillCanvas(canvas, getResources().getColor(R.color.colorAccent));
        }else if(mTouchCount==1){
            fillCanvas(canvas, getResources().getColor(R.color.colorPrimary));
        }else{
            fillCanvas(canvas, getResources().getColor(R.color.colorPrimaryDark));
        }
        super.onDraw(canvas);
    }

    private void clearCanvas(Canvas canvas){
        Paint clearPaint = new Paint();
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawRect(0, 0, 0, 0, clearPaint);
    }

    private void fillCanvas(Canvas canvas, int color){
        Paint fillPaint = new Paint();
        //fillPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        fillPaint.setColor(color);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), fillPaint);
    }

    public void setControlLevel(int touchCount){
        mTouchCount = touchCount;
    }
}
