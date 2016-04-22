package com.tcd.aransena.steer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;


/**
 * Created by aransena on 21/04/16.
 */
public class TeleopControl extends View {

    private final String LOG_TAG = TeleopControl.class.getSimpleName();
    private MotionEvent mMotionEvent;
    private float mMotionEventX;
    private float mMotionEventY;


    private WebSocketClient mWebSocket;
    private Handler mHandler;
    private long mMetCommRate = 50;
    private JSONObject mNetMessage;

    private int mTouchCount;

    private int mW;
    private int mH;

    private int[] mOffsetInfo;

    private float mCircX;
    private float mCircY;

    private Paint mTPaint;
    private Paint mMainCircPaint;
    private float mTWidth = 0.0f;

    public void connect_to_server(){
        URI uri=null;
        try{
            uri = new URI("ws://192.168.1.102:8888/ws");
        }catch(URISyntaxException e){
            e.printStackTrace();
        }
        mWebSocket = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.v("Websocket", "Opened");
                mWebSocket.send("USER");
            }

            @Override
            public void onMessage(String s) {
                Log.v("Websocket", "Received >" + s + "<");
                if(s.equals("USER")){
                    Log.v("Websocket", "Starting net comms");
                    //mHandler.postDelayed(netComms, mMetCommRate);
                }
            }

            @Override
            public void onClose(int code, String s, boolean b) {
                Log.v("Websocket", code + ": Closed " + s);

            }

            @Override
            public void onError(Exception e) {
                Log.v("Websocket", "Error " + e.getMessage());
            }
        };
        mWebSocket.connect();
    }
    private Runnable netComms = new Runnable() {
        @Override
        public void run() {
            //Log.v(LOG_TAG,"Sending...");
            mWebSocket.send(mNetMessage.toString());
            mHandler.postDelayed(this, mMetCommRate);
        }
    };

    public void stopNetComms(){
        mHandler.removeCallbacks(netComms);
        mWebSocket.close();
    }

    public void pauseNetComms(){
        mHandler.removeCallbacks(netComms);
    }



    public TeleopControl(Context context){//, AttributeSet attrs){
        //super(context,attrs);
        super(context);
        init();
    }

    public void setMotionEventInfo(MotionEvent ev, int[] vloc){
        mMotionEvent = ev;
        mOffsetInfo = vloc;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        int cW = canvas.getWidth();
        int cH = canvas.getHeight();
        mW = cW;
        mH = cH;
        int mode = 1;
        clearCanvas(canvas);

        //Log.v(LOG_TAG,String.valueOf(mTouchCount));

        if(mTouchCount<1) {
            if (mMotionEvent == null) {
                drawStandbyCircle(canvas);
            } else {
                if(mode==0) {
                    drawTouchCircle(canvas);
                }else{
                    drawTouchCircle_mode2(canvas);
                }
            }
        }else{
            drawStandbyCircle(canvas);
            mHandler.postDelayed(netComms, mMetCommRate);
        }

        super.onDraw(canvas);
    }

    private void drawStandbyCircle(Canvas canvas){
        mTPaint.setColor(getResources().getColor(R.color.colorAccent));
        mTPaint.setStyle(Paint.Style.STROKE);
        mTPaint.setPathEffect(new DashPathEffect(new float[]{10, 20}, 0));
        canvas.drawCircle((float) mW / 2, (float) mH / 2, (float) mW / 2 - 200, mTPaint);
    }

    private void drawTouchCircle(Canvas canvas){
        //Log.v(LOG_TAG, mMotionEvent.toString());
        mTPaint.setColor(getResources().getColor(R.color.colorPrimaryDark));
        mTPaint.setStyle(Paint.Style.FILL);

        mMotionEventX = mMotionEvent.getRawX()-mOffsetInfo[0];
        mMotionEventY = mMotionEvent.getRawY()-mOffsetInfo[1];

        if(mMotionEvent.getAction()==MotionEvent.ACTION_DOWN){
            mHandler.postDelayed(netComms,mMetCommRate);
             try {
                mNetMessage.put("ControlLevel", 1);
            }catch(JSONException e){
                e.printStackTrace();
            }

            mCircX = mMotionEventX;
            mCircY = mMotionEventY;
        }
        else if(mMotionEvent.getAction()==MotionEvent.ACTION_MOVE){
            updateColor();
            canvas.drawCircle(mCircX, mCircY, calcRadius(), mMainCircPaint);
            mTPaint.setColor(getResources().getColor(R.color.colorAccent));
            canvas.drawCircle(mMotionEventX, mMotionEventY, (float) 150, mTPaint);
            canvas.drawLine(mCircX, mCircY, mMotionEventX, mMotionEventY, mTPaint);

            try {
               // Log.v(LOG_TAG,"VEL: " + String.valueOf(calcRadius()/100));
                //Log.v(LOG_TAG,"VEL: " + String.valueOf(calcAngle()));
                //float vel = calcRadius() / 500;
                //float angle = calcAngle();
                float maxSpeed = 1.5f;
                float vel = (mCircY-mMotionEventY)/(canvas.getWidth()/2)*maxSpeed;
                float angle = (mCircX-mMotionEventX)/(canvas.getWidth()/2)*maxSpeed;
                /*if(mCircY<mMotionEventY){
                    vel = vel*-1;
                }*/
                mNetMessage.put("VEL",vel);
                mNetMessage.put("ANGLE", angle);
                Log.v(LOG_TAG,"JSON: " + mNetMessage.toString());
            }catch(JSONException e){
                e.printStackTrace();
            }

        }
        else if(mMotionEvent.getAction()==MotionEvent.ACTION_UP){
            sendStop();
            mHandler.removeCallbacks(netComms);
            mMotionEvent=null;
            drawStandbyCircle(canvas);
            try {
                mNetMessage.put("ControlLevel", 0);
                mNetMessage.put("VEL", 0);
                mNetMessage.put("ANGLE", 0);
                Log.v(LOG_TAG,"JSON: " + mNetMessage.toString());
            }catch(JSONException e){
                e.printStackTrace();
            }

        }
        mMainCircPaint.setColor(getResources().getColor(R.color.colorPrimaryDark));

    }

    private void drawTouchCircle_mode2(Canvas canvas){
        //Log.v(LOG_TAG, mMotionEvent.toString());
        mTPaint.setColor(getResources().getColor(R.color.colorPrimaryDark));
        mTPaint.setStyle(Paint.Style.FILL);

        mMotionEventX = mMotionEvent.getRawX()-mOffsetInfo[0];
        mMotionEventY = mMotionEvent.getRawY()-mOffsetInfo[1];

        if(mMotionEvent.getAction()==MotionEvent.ACTION_DOWN){
            mHandler.postDelayed(netComms,mMetCommRate);
            try {
                mNetMessage.put("ControlLevel", 1);
            }catch(JSONException e){
                e.printStackTrace();
            }

            mCircX = mMotionEventX;
            mCircY = mMotionEventY;
        }
        else if(mMotionEvent.getAction()==MotionEvent.ACTION_MOVE){
            updateColor();
            int modifier = 1;
            float canvW = canvas.getWidth();
            float radius = mCircY-mMotionEventY;//calcRadius();
            if(radius<0){
                modifier = -1;
            }
            float radius_check = Math.abs(radius);
            Log.v(LOG_TAG, String.valueOf(radius_check));

            float setRadius = 0;
            if((0.0f<=radius_check)&&(radius_check<(canvW/10) )){
                setRadius = 0;
            }else if(((canvW/10)<=radius_check)&&(radius_check<(canvW/9))){
                setRadius = 30;
            }else if(((canvW/9)<=radius_check)&&(radius_check<(canvW/7))){
                setRadius= 75;
            }else if(((canvW/7)<=radius_check)&&(radius_check<(canvW/5))){
                setRadius= 150;
            }else if(((canvW/5)<=radius_check)&&(radius_check<(canvW/3))){
                setRadius= 225;
            }else if(((canvW/3)<=radius_check)&&(radius_check<(canvW/1))) {
                setRadius = 300;
            }else {
                setRadius=375;
            }

            float leftOval = mCircX-setRadius;
            float rightOval = mCircX+setRadius;
            float topOval = mCircY-setRadius;
            float bottomOval = mCircY+setRadius;
            Log.v(LOG_TAG,String.valueOf(leftOval) + " " + String.valueOf(rightOval)+ " " +String.valueOf(topOval)+" " +String.valueOf(bottomOval));
            //canvas.drawCircle(mCircX, mCircY, setRadius, mMainCircPaint);
            canvas.drawOval(leftOval,topOval,rightOval,bottomOval,mMainCircPaint);


            mTPaint.setColor(getResources().getColor(R.color.colorAccent));
            canvas.drawCircle(mMotionEventX, mMotionEventY, (float) 150, mTPaint);
            canvas.drawLine(mCircX, mCircY, mMotionEventX, mMotionEventY, mTPaint);

            try {
                Log.v(LOG_TAG,"VEL: " + String.valueOf(calcRadius()/100));
                Log.v(LOG_TAG,"VEL: " + String.valueOf(calcAngle()));
                //float vel = calcRadius() / 500;
                //float angle = calcAngle();
                float maxSpeed = 1.5f;
                //float vel = (mCircY-mMotionEventY)/(canvas.getWidth()/2)*maxSpeed;
                //float vel = ((0-setRadius)/375.0f)*maxSpeed;
                setRadius=setRadius*modifier;
                float vel = (setRadius/375.0f)*maxSpeed;
                float angle = (mCircX-mMotionEventX)/(canvas.getWidth()/2)*maxSpeed;
                /*if(mCircY<mMotionEventY){
                    vel = vel*-1;
                }*/
                mNetMessage.put("VEL",vel);
                mNetMessage.put("ANGLE", angle);
                //Log.v(LOG_TAG,"JSON: " + mNetMessage.toString());
            }catch(JSONException e){
                e.printStackTrace();
            }

        }
        else if(mMotionEvent.getAction()==MotionEvent.ACTION_UP){
            sendStop();
            mHandler.removeCallbacks(netComms);
            mMotionEvent=null;
            drawStandbyCircle(canvas);
            try {
                mNetMessage.put("ControlLevel", 0);
                mNetMessage.put("VEL", 0);
                mNetMessage.put("ANGLE", 0);
                //Log.v(LOG_TAG,"JSON: " + mNetMessage.toString());
            }catch(JSONException e){
                e.printStackTrace();
            }

        }
        mMainCircPaint.setColor(getResources().getColor(R.color.colorPrimaryDark));

    }

    private void updateColor(){
        float[] hsv = new float[3];
        int color = mMainCircPaint.getColor();
        Color.colorToHSV(color, hsv);
        float min = 0;
        float max = 1;
        float x = calcRadius();

        float darkness = ((float)x/300)*0.8f;
        hsv[2] = darkness; // value component
        mMainCircPaint.setColor(Color.HSVToColor(hsv));
    }

    private float calcRadius(){
        return (float) Math.sqrt(Math.pow(mMotionEventX-mCircX,2)+Math.pow(mMotionEventY-mCircY,2));
    }

    private float calcAngle(){
        if(mMotionEventY>mCircY) {
            return (float) Math.tan((double) (mMotionEventY - mCircY) / (double) (mMotionEventX - mCircX));
        }else{
            return (float) Math.tan((double) (Math.abs(mMotionEventY - mCircY)) / (double) (mMotionEventX - mCircX))*-1;
        }
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Try for a width based on our minimum
        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int w = resolveSizeAndState(minw, widthMeasureSpec, 1);

        // Whatever the width ends up being, ask for a height that would let the pie
        // get as big as it can
        int minh = MeasureSpec.getSize(w) - (int)mTWidth + getPaddingBottom() + getPaddingTop();
        int h = resolveSizeAndState(MeasureSpec.getSize(w) - (int) mTWidth, heightMeasureSpec, 0);

        setMeasuredDimension(w, h);
    }

    public void setColor(int colorInt){
        mTPaint.setColor(colorInt);
    }

    public void updateView(MotionEvent ev) {
        Log.v(LOG_TAG, "in updateView()");
        Log.v(LOG_TAG, "Canvas cleared");

        Log.v(LOG_TAG, "Circle drawn");
    }

    private void clearCanvas(Canvas canvas){
        Paint clearPaint = new Paint();
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawRect(0, 0, 0, 0, clearPaint);
    }


    private void init() {
        connect_to_server();
        mHandler = new Handler();
        mNetMessage = new JSONObject();
        try {
            mNetMessage.put("Device","SmartPhone");
            mNetMessage.put("ControlLevel", 0);
            mNetMessage.put("VEL", 0);
            mNetMessage.put("ANGLE", 0);
        }catch(JSONException e){
            e.printStackTrace();
        }


        mTPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTPaint.setColor(getResources().getColor(R.color.colorAccent));
        mTPaint.setStyle(Paint.Style.STROKE);
        mTPaint.setStrokeWidth(10);

        mMainCircPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMainCircPaint.setColor(getResources().getColor(R.color.colorPrimaryDark));
        mMainCircPaint.setStyle(Paint.Style.FILL);
        //mMainCircPaint.setShader(new RadialGradient(mCircX, mCircY, calcRadius(), Color.WHITE, getResources().getColor(R.color.colorPrimaryDark), Shader.TileMode.CLAMP));
    }

    public void setControlLevel(int touchCount){
        mTouchCount = touchCount;
        try {
            mNetMessage.put("ControlLevel", (touchCount + 1));
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    public void sendStop(){
        try {
            mNetMessage.put("ControlLevel", 0);
            mNetMessage.put("VEL", 0);
            mNetMessage.put("ANGLE", 0);
            mWebSocket.send(mNetMessage.toString());
        }catch(JSONException e){
            e.printStackTrace();
        }
    }


}