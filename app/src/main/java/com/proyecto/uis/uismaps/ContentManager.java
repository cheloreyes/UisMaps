package com.proyecto.uis.uismaps;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;

/**
 * Esta clase maneja los componentes de la UI de la app, habilitando y deshabilitando la interfaz adaptada para
 * las capacidades de voz.
 * Created by cheloreyes on 9/03/15.
 */
public class ContentManager extends View implements UISMapsSettingsValues, View.OnClickListener {

    public static final int DPI_NEXUS5 = 480;
    private static final int MATCH_PARENT = RelativeLayout.LayoutParams.MATCH_PARENT;
    private static final int WRAP_CONTENT = RelativeLayout.LayoutParams.WRAP_CONTENT;

    private RelativeLayout.LayoutParams buttonsParams;
    private RelativeLayout containerLayout;
    private FloatingActionButton findMeButton;
    private LinearLayout hideButtonsLayout;
    private RelativeLayout.LayoutParams infoParams;
    private TextView infoText;
    private RelativeLayout.LayoutParams mapViewParams;
    private Context miContext;
    private MapView miMapview;
    private FloatingActionButton routeEndButton;
    private FloatingActionButton routeStartButton;
    private RelativeLayout.LayoutParams searchParams;
    private SearchView searchView;

    private SharedPreferences preferences;


    /**
     *
     * @param context Context de la vista en que se crea el objeto.
     * @param mapView Objeto de la clase @MapView ya instanceado antes.
     */
    public ContentManager(Context context, MapView mapView) {
        super(context);
        miContext = context;
        containerLayout = new RelativeLayout(miContext);
        miMapview = mapView;
        preferences = PreferenceManager.getDefaultSharedPreferences(miContext);
        textViewManager();
        textFieldManager();
        buttonManager();
        layoutsManager();
        setMyContent(preferences.getBoolean(EYESIGHT_ASSISTANT, false));
    }

    /**
     * Crea y configura los @TextView necesarios para la UI.
     */
    private void textViewManager() {
        infoText = new TextView(miContext);
        infoText.setText(" ");
    }

    /**
     * Crea y configura los @Button necesarios para la UI.
     */
    private void buttonManager() {
        findMeButton = new FloatingActionButton(miContext);
        routeStartButton = new FloatingActionButton(miContext);
        routeEndButton = new FloatingActionButton(miContext);

        findMeButton.setColorNormal(getResources().getColor(R.color.my_material_green));
        findMeButton.setColorPressed(getResources().getColor(R.color.my_material_light_green));
        findMeButton.setImageResource(android.R.drawable.ic_menu_mylocation);
        findMeButton.setOnClickListener(this);
        findMeButton.setId(FIND_ME_BUTTON_ID);
        findMeButton.setShadow(false);

        routeStartButton.setColorNormal(getResources().getColor(R.color.my_material_green));
        routeStartButton.setColorPressed(getResources().getColor(R.color.my_material_light_green));
        routeStartButton.setImageResource(android.R.drawable.ic_dialog_map);
        routeStartButton.setOnClickListener(this);
        routeStartButton.setId(START_ROUTE_BUTTON_ID);
        routeStartButton.setVisibility(View.INVISIBLE);
        routeStartButton.setShadow(false);

        routeEndButton.setColorNormal(getResources().getColor(R.color.my_material_green));
        routeEndButton.setColorPressed(getResources().getColor(R.color.my_material_light_green));
        routeEndButton.setImageResource(android.R.drawable.ic_menu_send);
        routeEndButton.setOnClickListener(this);
        routeEndButton.setId(END_ROUTE_BUTTON_ID);
        routeEndButton.setVisibility(View.INVISIBLE);
        routeEndButton.setShadow(false);

    }

    /**
     * Crea y configura los @TextField necesarios para la UI.
     */
    private void textFieldManager() {
        searchView = new SearchView(miContext);

    }

    /**
     * Crea y configura los parámetros a los layouts para ubicar los componentes de la UI.
     */
    private void layoutsManager() {
        hideButtonsLayout = new LinearLayout(miContext);

        mapViewParams = new RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        searchParams = new RelativeLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        buttonsParams = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        infoParams = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);

        searchParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        searchParams.addRule(RelativeLayout.CENTER_VERTICAL);
        searchParams.setMargins(fixPixel(20), fixPixel(10), fixPixel(20), fixPixel(0));

        //hideButtonsLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        hideButtonsLayout.setGravity(Gravity.RIGHT);
        hideButtonsLayout.setGravity(Gravity.BOTTOM);
        hideButtonsLayout.setOrientation(LinearLayout.VERTICAL);

        buttonsParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        buttonsParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        buttonsParams.setMargins(fixPixel(5), fixPixel(5), fixPixel(10), fixPixel(20));

        infoParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        infoParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        infoParams.setMargins(fixPixel(10), fixPixel(0), fixPixel(0), fixPixel(10));
    }

    /**
     * Este método corrige el número de pixeles según la densidad de pixeles en el dispositivo.
     * Ya que esto varía de un dispositivo a otro.
     * @param pixels Número de pixeles deseado
     * @return
     */
    private int fixPixel(int pixels)
    {
        int currentDpi = getResources().getDisplayMetrics().densityDpi;
        int x = (currentDpi * pixels) / DPI_NEXUS5; //Se toma el DPI del nexus 5 porque es el dispositivo con que realiza el desarrollo.
        return x;

    }

    /**
     * Adiciona los componentes creados para la UI al layout que los contiene @containerLayout.
     * @param isSimple true: Si se quiere la interfaz sencilla para ser usada mediante comandos de voz, entonces no adiciona los botones a la UI.
     *                 false: Si no se quieren comandos de voz, se presenta la UI completa.
     */
    public void setMyContent(boolean isSimple) {
        hideButtonsLayout.removeAllViews();
        containerLayout.removeAllViews();

        hideButtonsLayout.addView(routeEndButton);
        hideButtonsLayout.addView(routeStartButton);
        hideButtonsLayout.addView(findMeButton);

        containerLayout.addView(miMapview, mapViewParams);

        if (!isSimple) {
            containerLayout.addView(searchView, searchParams);
            containerLayout.addView(hideButtonsLayout, buttonsParams);
            containerLayout.addView(infoText, infoParams);
            containerLayout.refreshDrawableState();
        }
    }

    /**
     * Llamado para habilitar o deshabilitar los botones de @routeStartButton y @routeEndButton cuando se selecciona un punto del mapa, para habilitar la funcion de rutas.
     * @param showIt indica si habilitar o deshabilitar los botones.
     */
    public void showFloatingMenu(boolean showIt) {
        if (showIt) {
            routeStartButton.setVisibility(View.VISIBLE);
            routeEndButton.setVisibility(View.VISIBLE);
        }
        else {
            routeStartButton.setVisibility(View.INVISIBLE);
            routeEndButton.setVisibility(View.INVISIBLE);
        }
    }

    public void setInfoText(String text) {
        infoText.setText(text);
    }

    public ViewGroup getContainer() {
        return containerLayout;
    }

    /**
     * Llamado cuando la vista a sido clikeada.
     *
     * @param v La vista que ha sido clikeada.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case FIND_ME_BUTTON_ID:
                miMapview.locateMe();
                miMapview.setRouteStart();
                //miMapa.notifyMessage("Ubicame!");
                break;
            case START_ROUTE_BUTTON_ID:
                miMapview.setRouteStart();
                break;
            case END_ROUTE_BUTTON_ID:
                miMapview.setRouteEnd();
                break;
        }
    }
}
