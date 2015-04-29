package com.proyecto.uis.uismaps.categories;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;

import com.proyecto.uis.uismaps.R;
import com.proyecto.uis.uismaps.finder.Finder;
import com.proyecto.uis.uismaps.finder.Spaces;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by CheloReyes on 29/04/15.
 */
public class CategoriesBuilder {
    private String[] categoryName = {"Auditorios", "Aulas", "Cafeterías", "Centros de Cómputo", "Centros de Estudio",  "Decanaturas", "Dirección Cultural",
            "Dirección Escuelas", "Escuelas", "Laboratorios", "Museos", "Oficinas", "Talleres"};
    private Context iContext;
    private HashMap<String, ArrayList<Spaces>> content;
    private Finder iFider;
    private CategoriesAdapter adapter;

    public CategoriesBuilder(Context context) {
        iContext = context;
        content = new HashMap<>();
    }
    public CategoriesAdapter getAdapter() {
        return adapter;
    }
    public void childClickAction(int groupPosition, int childPosition, Dialog owner) {
        Log.v("Categories", content.get(categoryName[groupPosition]).get(childPosition).getName() + " Pertenece a: " + content.get(categoryName[groupPosition]).get(childPosition).getBuilding());
        String edifice = content.get(categoryName[groupPosition]).get(childPosition).getBuilding();
        iFider.setFocus(edifice);
        owner.dismiss();
    }

    public void setiFider(Finder finder) {
        iFider = finder;
        initCategories();
    }
    private void initCategories(){
        String[] tableName = {"Audience", "ClassRoom", "CoffeeShop", "CompRoom", "StudyRoom", "Dean", "CultureGroup", "Dir", "School",
        "Lab", "MuseumExp", "Office", "Atelier"};
        ArrayList<Spaces> result = new ArrayList<>();
        if(iFider != null)
            for(int i = 0; i < tableName.length; i++) {
                result = iFider.getTableContent(tableName[i]);
                content.put(categoryName[i], result);
            }
        adapter = new CategoriesAdapter(iContext, categoryName, content);
    }
}
