package com.proyecto.uis.uismaps.finder;

import android.content.Context;
import android.database.MatrixCursor;
import android.widget.SearchView;

import com.cartotype.Framework;
import com.proyecto.uis.uismaps.MapView;

import java.util.ArrayList;

/**
 * Created by cheloreyes on 13/03/15.
 */
public class Finder implements SearchView.OnQueryTextListener, SearchView.OnSuggestionListener{
    public static final int MAX_RESULTS = 5;
    private MatrixCursor cursor;
    private Context iContext;
    private SearchView iSearch;
    private MapView iMapView;
    private ArrayList<String> listResults;


    public Finder(Context context, SearchView searchView, MapView mapView) {
        iContext = context;
        iSearch = searchView;
        iMapView = mapView;
        iSearch.setOnQueryTextListener(this);
        iSearch.setOnSuggestionListener(this);
    }

    private void loadData(String query) {
        String[] colums = new String[] {"_id","text"};
        Object[] temp = new Object[] {0, "default"};
        cursor = new MatrixCursor(colums);
        listResults = iMapView.finder(query, Framework.FUZZY_STRING_MATCH_METHOD, MAX_RESULTS);
        for (int i=0; i < listResults.size(); i++) {
            temp[0] = i;
            temp[1] = listResults.get(i);
            cursor.addRow(temp);
        }
        SuggestAdapter adapter = new SuggestAdapter(iContext, cursor, listResults);
        iSearch.setSuggestionsAdapter(adapter);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        loadData(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        loadData(newText);
        return true;
    }

    @Override
    public boolean onSuggestionSelect(int position) {
        return false;
    }

    @Override
    public boolean onSuggestionClick(int position) {
        String selectedResult = listResults.get(position);
        iSearch.clearFocus();
        iMapView.foundFocus(selectedResult);
        return true;
    }
}
