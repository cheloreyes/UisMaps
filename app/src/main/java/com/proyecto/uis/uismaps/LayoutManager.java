package com.proyecto.uis.uismaps;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by cheloreyes on 3/03/15.
 */
public class LayoutManager {

    public static final int MATCH_PARENT = RelativeLayout.LayoutParams.MATCH_PARENT;
    public static final int WRAP_CONTENT = RelativeLayout.LayoutParams.WRAP_CONTENT;

    private RelativeLayout containerLayout;
    private MapView miMapView;
    private Context miContext;

    public LayoutManager(Context pContext, MapView pMapView) {
        miContext = pContext;
        miMapView = pMapView;
        containerLayout = new RelativeLayout(miContext);
        createItems();
    }

    public void createItems() {
        TextView titleTextView = new TextView(miContext);
        ImageButton findMeButton = new ImageButton(miContext);

        RelativeLayout.LayoutParams mapViewLayout = new RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        RelativeLayout.LayoutParams tileTextViewLayaout = new RelativeLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        RelativeLayout.LayoutParams findMeButtonLayout = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);

        titleTextView.setTextAppearance(miContext, android.R.style.TextAppearance_Material_Medium);
        titleTextView.setText(miContext.getString(R.string.title));

        findMeButton.setImageResource(android.R.drawable.ic_menu_mylocation);


        findMeButtonLayout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        findMeButtonLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        containerLayout.addView(titleTextView, tileTextViewLayaout);
        containerLayout.addView(findMeButton, findMeButtonLayout);
        containerLayout.addView(miMapView, mapViewLayout);
    }

    public RelativeLayout getContainerLayout() {
        return containerLayout;
    }
}
