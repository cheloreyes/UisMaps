package com.proyecto.uis.uismaps.Content;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.proyecto.uis.uismaps.R;
import com.proyecto.uis.uismaps.finder.Spaces;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CheloReyes on 27/03/15.
 */
public class BodyAdapter extends ArrayAdapter<Spaces> {
    private Context iContext;
    private List<Spaces> dependences;

    public BodyAdapter(Context context, List<Spaces> spaces) {
        super(context, 0, spaces);
        iContext = context;
        dependences = spaces;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) iContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.body, parent, false);
        }
        TextView office = (TextView) convertView.findViewById(R.id.office);
        TextView num_office = (TextView) convertView.findViewById(R.id.num_office);

        office.setText(dependences.get(position).getName());
        num_office.setText(dependences.get(position).getOffice());
        return convertView;
    }
}
