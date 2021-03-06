package com.proyecto.uis.uismaps.finder;

import android.content.Context;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.support.v7.widget.SearchView;
import android.util.Log;

import com.proyecto.uis.uismaps.Content.Alerts;
import com.proyecto.uis.uismaps.Content.BodyAdapter;
import com.proyecto.uis.uismaps.mapview.MapView;
import com.proyecto.uis.uismaps.R;
import com.proyecto.uis.uismaps.db.DBHelper;
import android.support.v4.widget.SimpleCursorAdapter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Finder realiza las acciones de buscar en la base de datos tanto de @SearchView como de otras dependencias.
 */
public class Finder implements SearchView.OnQueryTextListener, SearchView.OnSuggestionListener{

    // **********************
    // Constants
    // **********************
    public static final int MAX_RESULTS = 10;

    // **********************
    // Fields
    // **********************
    private ArrayList<String> listResults;
    private Context iContext;
    private MapView iMapView;
    private SearchView iSearch;
    private DBHelper iDbHelper;
    private List<Spaces> iSpaces;
    // **********************
    // Constructor
    // **********************
    /**
     * Crea una nueva entidad de Finder.
     * @param context Referencia del contexto de donde se es instanceada.
     * @param searchView Objeto específico de busquedas.
     * @param mapView mapview.
     */
    public Finder(Context context, SearchView searchView, MapView mapView) {
        iContext = context;
        iSearch = searchView;
        iMapView = mapView;
        iSearch.setOnQueryTextListener(this);
        iSearch.setOnSuggestionListener(this);
        conectDataBase();
    }

    public Finder(Context context) {
        iContext = context;
        conectDataBase();
    }

    // **********************
    // Methods
    // **********************

    /**
     * Establece la conexión a la base de datos instanceando un objeto de tipo @DBHelper y abriendola para su uso.
     */
    private void conectDataBase() {
        iDbHelper = new DBHelper(iContext);
        iDbHelper.close();
        try {
            iDbHelper.createDataBase();
        } catch (IOException e) {
            throw new Error("No se creo base de datos");
        }
        try {
            iDbHelper.openDataBase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Carga la lista de resultados de la busqueda a @setSuggestionsAdapter para ser desplegados
     * como sugerencias al momento de buscar.
     * @param query
     */
    private void loadData(String query) {
        String[] from = {"text"};
        int[] to = {R.id.item_finder};
        if(query.length() > 4) {
            String[] colums = new String[] {"_id","text"};
            Object[] temp = new Object[] {0, "default"};
            MatrixCursor cursor = new MatrixCursor(colums);
            //listResults = iMapView.finder(query, Framework.FUZZY_STRING_MATCH_METHOD, MAX_RESULTS);
            listResults = toFind(query, MAX_RESULTS);
            for (int i=0; i < listResults.size(); i++) {
                temp[0] = i;
                temp[1] = listResults.get(i);
                cursor.addRow(temp);
            }
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(iContext, R.layout.finder, cursor, from, to);
            iSearch.setSuggestionsAdapter(adapter);
        }
    }

    /**
     * toFind realiza la busqueda en la base de datos local SQLite, capturando el resultado en un arreglo del tipo @NearbyPlace
     * y retornando un arreglo con las etiquetas de nombre encontradas.
     * @param query cadena a buscar.
     * @param sizeResult cantidad de resultados.
     * @return un arreglo con la etiqueta de nombres encontrados.
     */
    private ArrayList<String> toFind(String query, int sizeResult) {
        ArrayList<String> namePlaces = new ArrayList<>();
        iSpaces = iDbHelper.spaces(query, sizeResult);
        for(Spaces temp : iSpaces) {
            namePlaces.add(temp.getName());
        }
        return namePlaces;
    }

    /**
     * Obtiene el edificio asociado a la posición seleccionada de los resultado de @SearchView.
     * @param position posicion en la lista.
     * @return edificio relacionado.
     */
    private String getBuilding(int position) {
        return iSpaces.get(position).getBuilding();
    }

    /**
     * Crea un adaptador de tipo @BodyAdapter para agregar la información de las dependencias del edificio.
     * @param building Edificio que se requiere dependencias.
     * @return adaptador de tipo @BodyAdapter.
     */
    public BodyAdapter getDependencesAdapter(String building) {
        iSpaces = iDbHelper.dependences(building);
        return new BodyAdapter(iContext, iSpaces);
    }

    /**
     * Obtiene todos los edificios del campus
     * @return Arreglo con el nombre de los edificios del campus.
     */
    public String[] getBuildingList(){
        return iDbHelper.buildingsList();
    }

    /**
     * Obtiene la imágen básica del edificio encontrada en la base de datos.
     * @param building Edificio que se requiere imágen.
     * @return Un Bitmap con los datos de la imágen del edificio.
     */
    public Bitmap getImgBuilding( String building) {
        return iDbHelper.imageBuilding(building);
    }

    /**
     * Obtiene los espacios resultantes de la búsqueda
     * @return @Spaces
     */
    public List<Spaces> getiSpaces() {
        return iSpaces;
    }

    /**
     * Obtiene la descripción del edificio encontrada en la base de datos.
     * @param building Edificio que se requiere descripción.
     * @return @String con la descripción del edificio.
     */
    public String getDescriptionBuilding(String building) {
        return iDbHelper.descriptionBuilding(building);
    }

    /**
     * Obtiene la posición geográfica de la entrada del edificio encontrada en la base de datos.
     * @param building Edificio que se requiere entrada.
     * @return Arreglo con las cordenadas geográficas encontradas.
     */
    public double[] getBuildingEntrance(String building) {
        return  iDbHelper.getBuildingEntrance(building);
    }

    /**
     * Otiene el contenido de cada categoría.
     * @param table Cada categoría hace referencia a una tabla de la base de datos.
     * @return Arreglo con la lista de dependencias.
     */
    public ArrayList<Spaces> getTableContent(String table) {
        return iDbHelper.getTableContent(table);
    }

    public String getImageUrl(String building) {
        return iDbHelper.imageUrl(building);
    }

    /**
     * Coloca un marcador en la entrada del edificio seleccionado.
     * @param selectedResult Edificio al que se relaciona.
     */
    public void setFocus(String place, String selectedResult) {
        if(selectedResult!= null){
            Log.v("resultados", selectedResult + " " + getBuildingEntrance(selectedResult)[0] + ", " + getBuildingEntrance(selectedResult)[1]);

            iMapView.foundFocus(place, selectedResult, getBuildingEntrance(selectedResult));
        }
    }

    public void showInfoDialog(String place) {
        if(place != null){
            Log.v("place", "place");
            List<Spaces> spaces = iDbHelper.spaces(place, 1);
            Spaces space = spaces.get(0);
            if(space != null) new Alerts(iContext).showAlertDialog(space.getName(), "Dentro del edificio de " + space.getBuilding() + ", en la oficina " + space.getOffice(), "OK");
        }
        else Log.v("place", "no place");
    }

    /**
     * Cierra la conexión a la base de datos.
     */
    public void closeDataBase() {
        iDbHelper.close();
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
        Log.v("onSuggestionClick", "Pertenece al edificio: " + getBuilding(position));
        String selectedResult = getBuilding(position);
        iSearch.clearFocus();
        iMapView.foundFocus(iSpaces.get(position).getName(), selectedResult, getBuildingEntrance(selectedResult));
        return true;
    }

    @Override
    public boolean onSuggestionSelect(int position) {
        return false;
    }
}
