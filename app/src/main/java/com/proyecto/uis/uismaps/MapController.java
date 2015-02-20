package com.proyecto.uis.uismaps;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.ResourceProxyImpl;
import org.osmdroid.views.MapView;

import java.util.ResourceBundle;

/**
 * Created by cheloreyes on 19/02/15.
 */
public class MapController extends Fragment {

    // **********************
    // Constants
    // **********************

    // **********************
    // Fields
    // **********************

    private MapView imapview;
    private ResourceProxy ireResourceProxy;


    // **********************
    // Methods
    // **********************
    public static MapController newInstance() {
        MapController controller = new MapController();
        return controller;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ireResourceProxy = new ResourceProxyImpl(inflater.getContext().getApplicationContext());
        imapview = new MapView(inflater.getContext(), 256, ireResourceProxy);
        return imapview;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Context icontext = this.getActivity();
        final DisplayMetrics dm = icontext.getResources().getDisplayMetrics();
        IMapController mapController = imapview.getController();
        imapview.setClickable(true);
        imapview.setBuiltInZoomControls(true);
        imapview.setMultiTouchControls(true);
        imapview.setUseDataConnection(false);
        imapview.setTileSource(TileSourceFactory.MAPNIK);
        mapController.setZoom(18);
        mapController.setCenter(new GeoPoint(7.1410242928925083, -73.11925510433737));

    }
}
