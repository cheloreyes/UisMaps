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
 * Esta clase carga los valores asociados a cada grupo de las categorías de la base de datos, cada grupo de las categorías hace referencia a una tabla de la base de datos
 * por el tipo de diseño establecido es sencilla esta asociación. Crea el adaptador necesario para la lista expandible y recibe la conexión a la base de datos previamente instanceada.
 */
public class CategoriesBuilder {
    // **********************
    // Constants
    // **********************
    private String[] categoryName = {"Auditorios", "Aulas", "Cafeterías", "Centros de Cómputo", "Centros de Estudio",  "Decanaturas", "Dirección Cultural",
            "Dirección Escuelas", "Escuelas", "Laboratorios", "Museos", "Oficinas", "Talleres"};
    private Context iContext;
    private HashMap<String, ArrayList<Spaces>> content;
    private Finder iFider;
    private CategoriesAdapter adapter;

    // **********************
    // Constructor
    // **********************

    /**
     * Toma y guarda una referencia del contexto utilizado con el fin de acceder a los recursos de la aplicación.
     * @param context
     */
    public CategoriesBuilder(Context context) {
        iContext = context;
        content = new HashMap<>();
    }

    // **********************
    // Methods
    // **********************

    /**
     * Inicializa la lista de categorías con los grupos y su contenido.
     */
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

    /**
     * Establece una referencia ya instanceada de tipo @Finder, con el fin de acceder a todas sus capacidades.
     * @param finder
     */
    public void setiFider(Finder finder) {
        iFider = finder;
        initCategories();
    }

    /**
     * Obtiene el adaptador ya cargado para ser utilizado en una lista del tipo @ExpandableListView
     * @return
     */
    public CategoriesAdapter getAdapter() {
        return adapter;
    }

    /**
     * Realiza la acción necesaria una vez es seleccionado un contenido de la lista de categorías.
     * @param groupPosition Posición en el grupo de categorías.
     * @param childPosition Posición en el contenido del grupo.
     * @param owner Dialogo donde se despliega la lista de categorías (necesario para cerrar el dialogo una vez se selecciona un elemento).
     */
    public void childClickAction(int groupPosition, int childPosition, Dialog owner) {
        Log.v("Categories", content.get(categoryName[groupPosition]).get(childPosition).getName() + " Pertenece a: " + content.get(categoryName[groupPosition]).get(childPosition).getBuilding());
        String edifice = content.get(categoryName[groupPosition]).get(childPosition).getBuilding();
        iFider.setFocus(edifice);
        owner.dismiss();
    }
}
