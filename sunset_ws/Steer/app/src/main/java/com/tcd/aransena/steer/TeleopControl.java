package com.tcd.aransena.steer;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.preference.PreferenceManager;
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
    private int mControlMode;
    private float mMaxVel;


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

    float mCanvW=0;
    float mCanvH=0;

    public void connect_to_server(String uri_s){

        URI uri=null;
        Log.v(LOG_TAG,"uri_s: "+uri_s);
        try{
            //uri = new URI("ws://192.168.1.102:8888/ws");
            uri = new URI(uri_s);
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
                //Log.v("Websocket", code + ": Closed " + s);

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
            Log.v(LOG_TAG,"Sending..."+mNetMessage.toString());
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
        mCanvW = canvas.getWidth();
        mCanvH = canvas.getHeight();

        clearCanvas(canvas);

        //Log.v(LOG_TAG,String.valueOf(mTouchCount));

        if(mTouchCount<1) {
            if (mMotionEvent == null) {
                drawStandbyCircle(canvas);
            } else {

                //Log.v(LOG_TAG, mMotionEvent.toString());
                mTPaint.setColor(getResources().getColor(R.color.colorPrimaryDark));
                mTPaint.setStyle(Paint.Style.FILL);

                mMotionEventX = mMotionEvent.getRawX()-mOffsetInfo[0];
                mMotionEventY = mMotionEvent.getRawY()-mOffsetInfo[1];

                if(mMotionEvent.getAction()==MotionEvent.ACTION_DOWN){
                    mHandler.postDelayed(netComms, mMetCommRate);
                    try {
                        mNetMessage.put("ControlLevel", 1);
                    }catch(JSONException e){
                        e.printStackTrace();
                    }

                    mCircX = mMotionEventX;
                    mCircY = mMotionEventY;
                }
                else if(mMotionEvent.getAction()==MotionEvent.ACTION_MOVE){
                    if (mControlMode == 1) {
                        drawTouchCircle_mode1(canvas);
                    } else if (mControlMode == 2) {
                        drawTouchCircle_mode2(canvas);
                    } else if (mControlMode == 3) {
                        drawTouchCircle_mode3(canvas);
                    }else {
                        drawTouchCircle_mode4(canvas);
                    }
                }else{//(mMotionEvent.getAction()==MotionEvent.ACTION_UP){
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
        canvas.drawCircle((float) mCanvW / 2, (float) mCanvH / 2, (float) mCanvW / 2 - 200, mTPaint);
    }

    private void drawTouchCircle_mode1(Canvas canvas){

        /*if(mMotionEvent.getAction()==MotionEvent.ACTION_DOWN){
            mHandler.postDelayed(netComms, mMetCommRate);
             try {
                mNetMessage.put("ControlLevel", 1);
            }catch(JSONException e){
                e.printStackTrace();
            }

            mCircX = mMotionEventX;
            mCircY = mMotionEventY;
        }*/
        //else if(mMotionEvent.getAction()==MotionEvent.ACTION_MOVE){
            updateColor();

        int direction_modifier = 1;
        float radius_H = mCircY-mMotionEventY;//calcRadius();
        if(radius_H<0){
            direction_modifier = -1;
        }
        float setRadius_H = Math.abs(radius_H);
        setRadius_H=Math.min(setRadius_H, (mCanvW / 2));

        canvas.drawCircle(mCircX, mCircY, calcRadius(), mMainCircPaint);
            mTPaint.setColor(getResources().getColor(R.color.colorAccent));
            canvas.drawCircle(mMotionEventX, mMotionEventY, (float) 150, mTPaint);
            canvas.drawLine(mCircX, mCircY, mMotionEventX, mMotionEventY, mTPaint);

            try {
               // Log.v(LOG_TAG,"VEL: " + String.valueOf(calcRadius()/100));
                //Log.v(LOG_TAG,"VEL: " + String.valueOf(calcAngle()));
                //float vel = calcRadius() / 500;
                //float angle = calcAngle();
                //float maxSpeed = 1.5f;
                float setVel_H=0;

                if(setRadius_H<(mCanvW/10)){
                    setVel_H = 0;
                }else {
                    setVel_H = setRadius_H * direction_modifier;
                }

                float vel = (setVel_H/(mCanvW/2))*mMaxVel;

                float angle = (mCircX-mMotionEventX)/(mCanvW/2)*mMaxVel;
                /*if(mCircY<mMotionEventY){
                    vel = vel*-1;
                }*/
                //Log.v("Vel", String.valueOf(vel));
                mNetMessage.put("VEL", vel);
                mNetMessage.put("ANGLE", angle);
                Log.v(LOG_TAG, "JSON: " + mNetMessage.toString());
            }catch(JSONException e){
                e.printStackTrace();
            }

        //}
        /*
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

        }*/
        mMainCircPaint.setColor(getResources().getColor(R.color.colorPrimaryDark));

    }

    private void drawTouchCircle_mode2(Canvas canvas){


        /*if(mMotionEvent.getAction()==MotionEvent.ACTION_DOWN){
            mHandler.postDelayed(netComms,mMetCommRate);
            try {
                mNetMessage.put("ControlLevel", 1);
            }catch(JSONException e){
                e.printStackTrace();
            }

            mCircX = mMotionEventX;
            mCircY = mMotionEventY;
        }*/
        //else if(mMotionEvent.getAction()==MotionEvent.ACTION_MOVE){
            updateColor();
            int direction_modifier = 1;
            float radius = calcRadius();//mCircY-mMotionEventY;//calcRadius();
            if(mCircY-mMotionEventY<0){
                direction_modifier = -1;
            }
            float radius_check = Math.abs(radius);
            Log.v(LOG_TAG, String.valueOf(radius_check));

            float setRadius = getRadius(radius_check,mCanvW);
            canvas.drawCircle(mCircX, mCircY, setRadius, mMainCircPaint);

            mTPaint.setColor(getResources().getColor(R.color.colorAccent));
            canvas.drawCircle(mMotionEventX, mMotionEventY, (float) 150, mTPaint);
            canvas.drawLine(mCircX, mCircY, mMotionEventX, mMotionEventY, mTPaint);

            try {
                setRadius=setRadius*direction_modifier;

                float vel = ((getRadius(Math.abs(mCircY-mMotionEventY),mCanvW)*direction_modifier)/(mCanvW/2))*mMaxVel;
                float angle = (mCircX-mMotionEventX)/(canvas.getWidth()/2)*mMaxVel;
                //Log.v("Vel", String.valueOf(vel));
                mNetMessage.put("VEL",vel);
                mNetMessage.put("ANGLE", angle);
                //Log.v(LOG_TAG,"JSON: " + mNetMessage.toString());
            }catch(JSONException e){
                e.printStackTrace();
            }

      /*  }
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

        }*/
        mMainCircPaint.setColor(getResources().getColor(R.color.colorPrimaryDark));

    }

    private void drawTouchCircle_mode3(Canvas canvas){
        if(mMotionEvent.getAction()==MotionEvent.ACTION_MOVE){
            updateColor();
            int direction_modifier = 1;
            float radius_H = mCircY-mMotionEventY;//calcRadius();
            float radius_W = mCircX-mMotionEventX;//calcRadius();
            if(radius_H<0){
                direction_modifier = -1;
            }

            //float radius_check_H = Math.abs(radius_H);
            //float radius_check_W = Math.abs(radius_W);

            //float setRadius_H = getRadius(radius_H,mCanvW);
            //float setRadius_W = getRadius(radius_W,mCanvW);

            float setRadius_H = Math.abs(radius_H);
            float setRadius_W = Math.abs(radius_W);

            setRadius_H=Math.min(setRadius_H, (mCanvW / 2));
            setRadius_W=Math.min(setRadius_W, (mCanvW / 2));

            float leftOval = mCircX-Math.max(setRadius_W/2, mCanvW / 12);
            float rightOval = mCircX+Math.max(setRadius_W/2, mCanvW/12);
            float topOval = mCircY-Math.max(setRadius_H/2,mCanvW/12);
            float bottomOval = mCircY+Math.max(setRadius_H/2,mCanvW/12);

            canvas.drawOval(leftOval, topOval, rightOval, bottomOval, mMainCircPaint);

            mTPaint.setColor(getResources().getColor(R.color.colorAccent));
            canvas.drawCircle(mMotionEventX, mMotionEventY, (float) 150, mTPaint);
            canvas.drawLine(mCircX, mCircY, mMotionEventX, mMotionEventY, mTPaint);

            try {
               float setVel_H=0;

                if(setRadius_H<(mCanvW/10)){
                    setVel_H = 0;
                }else {
                    setVel_H = setRadius_H * direction_modifier;
                }

                float vel = (setVel_H/(mCanvW/2))*mMaxVel;
                float angle = (mCircX-mMotionEventX)/(mCanvW/2)*mMaxVel;
                //Log.v("Vel", String.valueOf(mMaxVel) + "\t" + String.valueOf(vel));
                mNetMessage.put("VEL",vel);
                mNetMessage.put("ANGLE", angle);
            }catch(JSONException e){
                e.printStackTrace();
            }

        }
        mMainCircPaint.setColor(getResources().getColor(R.color.colorPrimaryDark));

    }

    private void drawTouchCircle_mode4(Canvas canvas){
        if(mMotionEvent.getAction()==MotionEvent.ACTION_MOVE){
            updateColor();
            int direction_modifier = 1;
            float radius_H = mCircY-mMotionEventY;//calcRadius();
            float radius_W = mCircX-mMotionEventX;//calcRadius();
            if(radius_H<0){
                direction_modifier = -1;
            }

            float radius_check_H = Math.abs(radius_H);
            float radius_check_W = Math.abs(radius_W);

            float setRadius_H = getRadius(radius_check_H, mCanvW);
            float setRadius_W = getRadius(radius_check_W,mCanvW);



            float leftOval = mCircX-Math.max(setRadius_W, mCanvW / 12);
            float rightOval = mCircX+Math.max(setRadius_W, mCanvW/12);
            float topOval = mCircY-Math.max(setRadius_H,mCanvW/12);
            float bottomOval = mCircY+Math.max(setRadius_H,mCanvW/12);

            canvas.drawOval(leftOval, topOval, rightOval, bottomOval, mMainCircPaint);

            mTPaint.setColor(getResources().getColor(R.color.colorAccent));
            canvas.drawCircle(mMotionEventX, mMotionEventY, (float) 150, mTPaint);
            canvas.drawLine(mCircX, mCircY, mMotionEventX, mMotionEventY, mTPaint);

            try {
                //float maxSpeed = 1.5f;
                setRadius_H=setRadius_H*direction_modifier;
                float vel = (setRadius_H/(mCanvW/2))*mMaxVel;
                //Log.v("Vel", String.valueOf(vel));
                float angle = (mCircX-mMotionEventX)/(canvas.getWidth()/2)*mMaxVel;
                mNetMessage.put("VEL",vel);
                mNetMessage.put("ANGLE", angle);
            }catch(JSONException e){
                e.printStackTrace();
            }

        }
        mMainCircPaint.setColor(getResources().getColor(R.color.colorPrimaryDark));

    }

    private float getRadius(float radius_check, float canvRef){
        float setRadius = 0;
        if(radius_check<(canvRef/10)){
            setRadius = 0;//canvRef/10;
        }else if(((canvRef/10)<=radius_check)&&(radius_check<(canvRef/9))){
            setRadius = canvRef/10;
        }else if(((canvRef/9)<=radius_check)&&(radius_check<(canvRef/7))){
            setRadius = canvRef/9;
        }else if(((canvRef/7)<=radius_check)&&(radius_check<(canvRef/5))){
            setRadius = canvRef/7;
        }else if(((canvRef/5)<=radius_check)&&(radius_check<(canvRef/4))){
            setRadius = canvRef/5;
        }else if(((canvRef/4)<=radius_check)&&(radius_check<(canvRef/3))) {
            setRadius = canvRef/3;
        }else {
            setRadius = canvRef/2;
        }
        return setRadius;
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
        //Log.v(LOG_TAG, "in updateView()");
        //Log.v(LOG_TAG, "Canvas cleared");

        //Log.v(LOG_TAG, "Circle drawn");
    }

    private void clearCanvas(Canvas canvas){
        Paint clearPaint = new Paint();
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawRect(0, 0, 0, 0, clearPaint);
    }


    private void init() {
        Context context = getContext();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String ip = prefs.getString(context.getString(R.string.pref_key_ip), context.getString(R.string.pref_default_ip));
        String ws = prefs.getString(context.getString(R.string.pref_key_ws), context.getString(R.string.pref_default_ws));
        String uri_s = "ws://"+ip + ":" + ws + "/ws";
        mControlMode = Integer.parseInt(prefs.getString(context.getString(R.string.pref_key_control_mode), context.getString(R.string.pref_default_control_mode)));
        Log.v("Control Mode", String.valueOf(mControlMode));
        mMaxVel = Float.parseFloat(prefs.getString(context.getString(R.string.pref_key_max_vel), context.getString(R.string.pref_default_max_vel)));


        connect_to_server(uri_s);
        mHandler = new Handler();
        mNetMessage = new JSONObject();
        try {
            mNetMessage.put("Device","SmartPhone");
            mNetMessage.put("ControlLevel", 0);
            mNetMessage.put("VEL", 0);
            mNetMessage.put("ANGLE", 0);
            //mNetMessage.put("TILT", 0);
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
            //mNetMessage.put("TILT",0);

            mWebSocket.send(mNetMessage.toString());

        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    /*public void setTilt(int tilt){
            Log.v(LOG_TAG,"Tilt");
            try {
                mNetMessage.put("TILT", tilt);
            }
            catch(JSONException e){
                e.printStackTrace();
            }
            //mWebSocket.send("TILT_UP");

        /*else{
            Log.v(LOG_TAG,"Tilt down");
            mWebSocket.send("TILT_DOWN");
        }
    }*/

}