package com.tcd.aransena.steer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.MediaController;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {link RobotCamFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RobotCamFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RobotCamFragment extends Fragment {

    private String mUrl = "";
    private JSONObject mNetMessage;
    //private String sRgbUrl = "httpf://192.168.0.12:8080/stream?topic=/camera/rgb/image_raw&quality=15";
    //private String sDepthUrl = "http://192.168.0.12:8080/stream?topic=/camera/depth/image&quality=15";
    private String sRgbUrl = "";
    private String sDepthUrl = "";
    private WebSocketClient mWebSocket;
    private WebView mWebView;
    private Activity mAct;

    private final String LOG_TAG = RobotCamFragment.class.getSimpleName();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    //private static final String ARG_PARAM1 = "param1";
    //private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    //private String mParam1;
    // private String mParam2;

    // private OnFragmentInteractionListener mListener;

    public RobotCamFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @ param param1 Parameter 1.
     * @ param param2 Parameter 2.
     * @ return A new instance of fragment RobotCamFragment.
     */
  /*  // TODO: Rename and change types and number of parameters
    public static RobotCamFragment newInstance(String param1, String param2) {
        RobotCamFragment fragment = new RobotCamFragment();
        Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Context context = getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String ip = prefs.getString(context.getString(R.string.pref_key_ip), context.getString(R.string.pref_default_ip));
        String cam_sock = prefs.getString(context.getString(R.string.pref_key_cam_sock),context.getString(R.string.pref_default_cam_sock));

        String cam_url = "http://" +ip + ":" + cam_sock;
        sRgbUrl = cam_url+"/stream?topic=/kinect/rgb/image_color&quality=15";
        sDepthUrl = cam_url+"/stream?topic=/kinect/depth/image&quality=15";


        init();


        //sRgbUrl = cam_url+"/stream?topic=/camera/rgb/image_raw&quality=15";
        //sDepthUrl = cam_url+"/stream?topic=/camera/depth/image&quality=15";

        //Log.v("cam_url: ", cam_url);
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //mParam1 = getArguments().getString(ARG_PARAM1);
            //mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_robot_cam, container, false);
        mAct = getActivity();

        //final VideoView videoView = (VideoView) rootView.findViewById(R.id.robot_cam_videoview);
        mWebView = (WebView) rootView.findViewById(R.id.robot_cam_videoview);

        mUrl = sRgbUrl;
        mWebView.loadUrl(mUrl);
        //Log.v(LOG_TAG, String.valueOf(mWebView.getProgress()));

        mWebView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {
                mWebView.setInitialScale(getScale());
                // do your stuff here
            }
        });

        mWebView.setOnTouchListener(new View.OnTouchListener() {
            float startX,startY,endX,endY;



            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    startX=event.getX();
                    startY=event.getY();

                    return false;
                }

                if(event.getAction() == MotionEvent.ACTION_MOVE){
                    return true;
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    endX=event.getX();
                    endY=event.getY();
                    if(endX>startX && endX-startX > 100){
                        //Log.v(LOG_TAG,"SWIPE RIGHT");
                        mWebView.loadUrl(sRgbUrl);
                    }
                    else if(endX<startX && startX-endX> 100){
                        //Log.v(LOG_TAG,"SWIPE LEFT"+sDepthUrl);
                        mWebView.loadUrl(sDepthUrl);
                    }
                    else if(endY>startY && endY-startY> 100){
                        Log.v(LOG_TAG, "SWIPE DOWN");
                        mWebSocket.send("TILT_DOWN");
                        //((MainActivity)mAct).setTilt(-1);
                    }
                    else if(endY<startY && startY-endY> 100){
                        Log.v(LOG_TAG, "SWIPE UP");
                        mWebSocket.send("TILT_UP");
                        //((MainActivity)mAct).setTilt(1);
                    }

                    /*if (mUrl == sRgbUrl) {
                        mUrl = sDepthUrl;
                    } else {
                        mUrl = sRgbUrl;
                    }*/

                    //Log.v(LOG_TAG, mUrl);
                    //mUrl = sRgbUrl;
                    //mWebView.loadUrl(mUrl);

                    return true;
                }

                return false;

            }
        });

            //videoView.requestFocus();
            //videoView.start();
            //mc.show();
            //Log.v(LOG_TAG,"in fragment");

            return rootView;
        }

    private int getScale() {

        int width = mWebView.getWidth();
        Double val = new Double(width) / new Double(640);
        val = val * 100d;
        return val.intValue();
    }

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
                //Log.v("Websocket", "Opened");
                mWebSocket.send("USER");
            }

            @Override
            public void onMessage(String s) {
                //Log.v("Websocket", "Received >" + s + "<");
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
        try {
            mWebSocket.connect();
        }catch(Exception e){
            Log.v("Error", "HERE");
            e.printStackTrace();

        }
    }
    private void init() {
        Context context = getContext();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String ip = prefs.getString(context.getString(R.string.pref_key_ip), context.getString(R.string.pref_default_ip));
        String ws = prefs.getString(context.getString(R.string.pref_key_ws), context.getString(R.string.pref_default_ws));
        String uri_s = "ws://" + ip + ":" + ws + "/ws";

        if(!ip.equals("0.0.0.0")) {
            Log.v("IP: ","HERE "+ip);
            connect_to_server(uri_s);
        }
    }
}
