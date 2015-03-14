package com.proyecto.uis.uismaps.finder;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.proyecto.uis.uismaps.MapView;
import com.proyecto.uis.uismaps.R;

import java.util.List;

/**
 * Created by cheloreyes on 12/03/15.
 */
public class SuggestAdapter extends CursorAdapter {
    private List<String> items;
    private Context miContext;
    private TextView textViewResults;
    private MapView miMapView;


    public SuggestAdapter(Context context, Cursor c, List<String> list) {
        super(context, c, false);
        items = list;
        miContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.details, parent, false);
        textViewResults = (TextView) view.findViewById(R.id.item_finder);
        return view;
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        textViewResults.setText(items.get(cursor.getPosition()));
    }


}
