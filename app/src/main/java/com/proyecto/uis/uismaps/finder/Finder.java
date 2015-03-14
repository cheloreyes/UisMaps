package com.proyecto.uis.uismaps.finder;

import android.content.Context;
import android.database.MatrixCursor;
import android.widget.SearchView;
import com.cartotype.Framework;
import com.proyecto.uis.uismaps.MapView;
import java.util.ArrayList;

public class Finder implements SearchView.OnQueryTextListener, SearchView.OnSuggestionListener{

    // **********************
    // Constants
    // **********************
    public static final int MAX_RESULTS = 5;

    // **********************
    // Fields
    // **********************
    private ArrayList<String> listResults;
    private Context iContext;
    private MapView iMapView;
    private SearchView iSearch;

    // **********************
    // Constructor
    // **********************
    /**
     * Crea una nueva entidad de Finder.
     * @param context Contexto en que es creada.
     * @param searchView
     * @param mapView
     */
    public Finder(Context context, SearchView searchView, MapView mapView) {
        iContext = context;
        iSearch = searchView;
        iMapView = mapView;
        iSearch.setOnQueryTextListener(this);
        iSearch.setOnSuggestionListener(this);
    }

    // **********************
    // Methods
    // **********************
    /**
     * Carga la lista de resultados de la busqueda a @setSuggestionsAdapter para ser desplegados
     * como sugerencias al momento de buscar.
     * @param query
     */
    private void loadData(String query) {
        String[] colums = new String[] {"_id","text"};
        Object[] temp = new Object[] {0, "default"};
        MatrixCursor cursor = new MatrixCursor(colums);
        listResults = iMapView.finder(query, Framework.FUZZY_STRING_MATCH_METHOD, MAX_RESULTS);
        for (int i=0; i < listResults.size(); i++) {
            temp[0] = i;
            temp[1] = listResults.get(i);
            cursor.addRow(temp);
        }
        SuggestAdapter adapter = new SuggestAdapter(iContext, cursor, listResults);
        iSearch.setSuggestionsAdapter(adapter);
    }

    // **********************
    // Methods from SuperClass
    // **********************
    /**
     * Método Override de @OnQueryTextListener, llamado al momento en que el usuario presiona la tecla
     * "enter" o pulsa el boton enviar de el objeto @SearchView.
     * @param query Texto digitado por el usuario y es enviado.
     * @return Cierto si la consulta ha sido manejado por el oyente, falso para que el SearchView realizar la acción predeterminada.
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        loadData(query);
        return true;
    }

    /**
     * Método Override de @OnQueryTextListener, llamado cuando el usuario cambia el texto de la consulta.
     * @param newText Nuevo contenido del campo de busqueda.
     * @return
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        loadData(newText);
        return true;
    }

    /**
     * Método Override de @OnSuggestionListener, llamado cuando una sugerencia es clickeada.
     * @param position la posición absoluta del item clikeado de la lista de sugerencias.
     * @return
     */
    @Override
    public boolean onSuggestionClick(int position) {
        String selectedResult = listResults.get(position);
        iSearch.clearFocus();
        iMapView.foundFocus(selectedResult);
        return true;
    }

    @Override
    public boolean onSuggestionSelect(int position) {
        return false;
    }
}
