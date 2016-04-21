package com.tcd.aransena.steer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by aransena on 21/04/16.
 */
public class TeleopFragment extends Fragment {

    public TeleopFragment(){
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_teleop, container, false);

        FrameLayout fl = (FrameLayout) rootView.findViewById(R.id.teleop_frame);
        TeleopControl tlc = new TeleopControl(getContext());
        fl.addView(tlc);


        return rootView;
    }



}

