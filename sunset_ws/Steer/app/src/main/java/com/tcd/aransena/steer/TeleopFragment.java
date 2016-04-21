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

    public TeleopFragment(){
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
        mTeleopControl = new TeleopControl(getContext());
        fl.addView(mTeleopControl);

        mTeleopControl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //if (event.getAction() == MotionEvent.ACTION_DOWN) {
                int[] vLocation = new int[2];
                int[] offsets = new int[2];

                rootView.getLocationOnScreen(vLocation);

                offsets[0] = (vLocation[0]-(int)rootView.getX());
                offsets[1] = (vLocation[1]-(int)rootView.getY());

                mTeleopControl.setMotionEventInfo(event,offsets);
                mTeleopControl.invalidate();
                //Log.v(LOG_TAG, event.toString());
                //  return true;
                //} else if (event.getAction() == MotionEvent.ACTION_UP) {

                //    return true;
                //}
                return true;
            }

        });


        return rootView;
    }



}

