package com.tcd.aransena.steer;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.MediaController;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;


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
    private String sRgbUrl = "http://192.168.1.102:8080/stream?topic=/camera/rgb/image_raw";
    private String sDepthUrl = "http://192.168.1.102:8080/stream?topic=/camera/depth/image_raw";
    private WebView mWebView;

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

        View rootView = inflater.inflate(R.layout.fragment_robot_cam, container, false);

        //final VideoView videoView = (VideoView) rootView.findViewById(R.id.robot_cam_videoview);
        mWebView = (WebView) rootView.findViewById(R.id.robot_cam_videoview);
        //MediaController mc = new MediaController(getActivity());
        //mc.setMediaPlayer(videoView);
        //mc.setAnchorView(videoView);
        //videoView.setMediaController(mc);
        //Uri uri=Uri.parse("rtsp://r2---sn-a5m7zu76.c.youtube.com/Ck0LENy73wIaRAnTmlo5oUgpQhMYESARFEgGUg5yZWNvbW1lbmRhdGlvbnIhAWL2kyn64K6aQtkZVJdTxRoO88HsQjpE1a8d1GxQnGDmDA==/0/0/0/video.3gp");
        //videoView.setVideoURI(uri);
        //String sUrl = "http://www.ebookfrenzy.com/android_book/movie.mp4";


        mUrl = sRgbUrl;

        /*
        videoView.setVideoPath(sUrl);
        videoView.requestFocus();
        videoView.start();
        */
        mWebView.setInitialScale(getScale());
        mWebView.loadUrl(mUrl);


        mWebView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                    if(mUrl==sRgbUrl){
                        mUrl=sDepthUrl;
                    }else{
                        mUrl=sRgbUrl;
                    }
                mWebView.setInitialScale(getScale());
                mWebView.loadUrl(mUrl);

                return false;
            }
        });
        //videoView.requestFocus();
        //videoView.start();
        //mc.show();
        Log.v(LOG_TAG, "in fragment");

        return rootView;
    }

    private int getScale(){

        int width = mWebView.getWidth();
        Double val = new Double(width)/new Double(640);
        val = val * 100d;
        return val.intValue();
    }




    // TODO: Rename method, update argument and hook method into UI event
    /*public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }*/

    /*@Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }*/

    /*@Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }*/

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    /*public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }*/
}
