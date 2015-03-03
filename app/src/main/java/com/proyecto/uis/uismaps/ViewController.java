package com.proyecto.uis.uismaps;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by cheloreyes on 27/02/15.
 */
public class ViewController extends Fragment{

    private Context iContext;
    private DisplayMetrics dm;

    // **********************
    // Constructors
    // **********************

    public static ViewController newInstance() {
        ViewController iViewController = new ViewController();
        return iViewController;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iContext = this.getActivity();
        dm = new DisplayMetrics();
        dm = iContext.getResources().getDisplayMetrics();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        MapView miMapa = new MapView(iContext, dm.densityDpi);
        return miMapa;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
