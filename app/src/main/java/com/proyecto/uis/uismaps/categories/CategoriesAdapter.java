package com.proyecto.uis.uismaps.categories;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.proyecto.uis.uismaps.R;
import com.proyecto.uis.uismaps.finder.Spaces;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by CheloReyes on 28/04/15.
 * Esta clase es un adaptador personalizado para establecer el contenido de la lista de categorías, hereda de @BaseExpandableListAdapter
 * con el fin de tener control sobre el grupo, y el hijo del grupo que es seleccionado.
 */
public class CategoriesAdapter extends BaseExpandableListAdapter {
    private final Context iContext;
    private String[] categoryName = {"Talleres", "Laboratorios"};
    private HashMap<String, ArrayList<Spaces>> categoryContent;

    public CategoriesAdapter(Context context, String[] categoryName, HashMap<String, ArrayList<Spaces>> categoryContent) {
        this.categoryName = categoryName;
        this.categoryContent = categoryContent;
        iContext = context;
    }

    @Override
    public int getGroupCount() {
    return categoryName.length;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return categoryContent.get(categoryName[groupPosition]).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return categoryName[groupPosition];
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return categoryContent.get(categoryName[groupPosition]).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) iContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.group_categories, parent, false);
        }
        TextView parentCategory = (TextView) convertView.findViewById(R.id.parent_categories);
        parentCategory.setText(getGroup(groupPosition).toString());

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) iContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.child_categories, parent, false);
        }
        TextView parentCategory = (TextView) convertView.findViewById(R.id.child_categories);
        Spaces temp = (Spaces) getChild(groupPosition, childPosition);
        parentCategory.setText(temp.getName());
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
