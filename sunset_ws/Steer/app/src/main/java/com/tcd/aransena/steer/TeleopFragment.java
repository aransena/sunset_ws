package com.tcd.aransena.steer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by aransena on 21/04/16.
 */
public class TeleopFragment extends Fragment {

    private final String LOG_TAG = TeleopFragment.class.getSimpleName();

    private TeleopControl mTeleopControl;
    private SemiAutoControl mSemiAutoControl;


    public TeleopFragment() {
    }

    public void setTilt_frag(int tilt) {
        Log.v(LOG_TAG, "TILT");
        //mTeleopControl.setTilt(tilt);
    }

    @Override
    public void onPause() {
        mTeleopControl.stopNetComms();
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_teleop, container, false);

        FrameLayout fl = (FrameLayout) rootView.findViewById(R.id.teleop_frame);
        Log.v(LOG_TAG, "Create Teleop Fragment" + getContext().toString());
        mTeleopControl = new TeleopControl(getContext());

        fl.addView(mTeleopControl);

        //Log.v(LOG_TAG, "Getting frame");
        FrameLayout fl2 = (FrameLayout) rootView.findViewById(R.id.semiauto_frame);
        if (fl2 == null) {
            Log.v(LOG_TAG, "id not found");
        } else {
            //Log.v(LOG_TAG,"Getting mSemiA");
            mSemiAutoControl = new SemiAutoControl(getContext());
            //Log.v(LOG_TAG,"Adding semi view");
            try {
                fl2.addView(mSemiAutoControl);
            } catch (Exception e) {
                Log.v(LOG_TAG, e.toString());
            }
            Log.v(LOG_TAG, "Added semi view");
        }


        mTeleopControl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //if (event.getAction() == MotionEvent.ACTION_DOWN) {
                int[] vLocation = new int[2];
                int[] offsets = new int[2];

                rootView.getLocationOnScreen(vLocation);

                offsets[0] = (vLocation[0] - (int) rootView.getX());
                offsets[1] = (vLocation[1] - (int) rootView.getY());

                mTeleopControl.setMotionEventInfo(event, offsets);
                mTeleopControl.invalidate();
                //Log.v(LOG_TAG, event.toString());
                //  return true;
                //} else if (event.getAction() == MotionEvent.ACTION_UP) {

                //    return true;
                //}
                return true;
            }

        });
        Log.v(LOG_TAG, "touch listener 1");

        mSemiAutoControl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
//                Log.v("EVENT: ", event.toString());
                int touchCount = event.getPointerCount();
                //Log.v(LOG_TAG, String.valueOf(touchCount)+"\t"+String.valueOf(event.getAction())+"\t"+String.valueOf(MotionEvent.ACTION_POINTER_DOWN));
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    touchCount -= 1;
                    Log.v(LOG_TAG, String.valueOf(touchCount));
                    mTeleopControl.sendStop();
                    mSemiAutoControl.setControlLevel(touchCount);
                    mTeleopControl.setControlLevel(touchCount);
                    mSemiAutoControl.invalidate();
                    mTeleopControl.invalidate();
                    mTeleopControl.pauseNetComms();

                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == 261 || event.getAction() == 262 || event.getAction() == 517 || event.getAction() == 518 || event.getAction() == 773 || event.getAction() == 774) {

                    if (event.getAction() == 262 || event.getAction() == 518 || event.getAction() == 774) {
                        touchCount -= 1;
                    }
                    touchCount += 1;
                    Log.v(LOG_TAG, String.valueOf(touchCount));

                    mSemiAutoControl.setControlLevel(touchCount);
                    mTeleopControl.setControlLevel(touchCount);

                    mSemiAutoControl.invalidate();
                    mTeleopControl.invalidate();
                    mTeleopControl.pauseNetComms();


                    return true;
                }

                return true;

            }

        });
        Log.v(LOG_TAG, "touch listener 2");

        return rootView;
    }


}

