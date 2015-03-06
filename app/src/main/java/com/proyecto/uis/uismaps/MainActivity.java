package com.proyecto.uis.uismaps;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;


public class MainActivity extends ActionBarActivity implements View.OnClickListener{

    public static final int MATCH_PARENT = RelativeLayout.LayoutParams.MATCH_PARENT;
    public static final int WRAP_CONTENT = RelativeLayout.LayoutParams.WRAP_CONTENT;
    public static final int END_ROUTE_BUTTON_ID = 3;
    private static final int FIND_ME_BUTTON_ID = 1;
    public static final int START_ROUTE_BUTTON_ID = 2;

    private static TextView informationText;
    private static FloatingActionButton setRouteEndButton;
    private static FloatingActionButton setRouteStartButton;
    private RelativeLayout containerLayout;
    private DisplayMetrics miDisplayMetrics;
    private LayoutManager miLayoutManager;
    private MapView miMapa;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        miDisplayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(miDisplayMetrics);

        miMapa = new MapView(this, miDisplayMetrics.densityDpi);
        //setContentView(miMapa);
        setMyContent();
        //LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,3000,10,miMapa);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onPause() {
        super.onPause();
        //Guarda el estado actual del mapa
        miMapa.saveState();
        miMapa.toggleGPS(false);
        //TODO: agregar a los estados guardados el estado del GPS y la ubicación del punto seleccionado.

    }

    public void setMyContent() {
        containerLayout = new RelativeLayout(this);

        SearchView searchView = new SearchView(this);
        searchView.setBackgroundColor(Color.LTGRAY);

        informationText = new TextView(this);
        informationText.setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault);
        informationText.setText("Info:");

        //Crea y configura los botones de localizar, establecer punto de partida y punto de llegada.
        FloatingActionButton findMeButton = new FloatingActionButton(this);
        findMeButton.setColorNormal(getResources().getColor(R.color.my_material_green));
        findMeButton.setColorPressed(getResources().getColor(R.color.my_material_light_green));
        findMeButton.setImageResource(android.R.drawable.ic_menu_mylocation);
        findMeButton.setOnClickListener(this);
        findMeButton.setId(FIND_ME_BUTTON_ID);

        setRouteStartButton = new FloatingActionButton(this);
        setRouteStartButton.setColorNormal(getResources().getColor(R.color.my_material_green));
        setRouteStartButton.setColorPressed(getResources().getColor(R.color.my_material_light_green));
        setRouteStartButton.setImageResource(android.R.drawable.ic_dialog_map);
        setRouteStartButton.setOnClickListener(this);
        setRouteStartButton.setId(START_ROUTE_BUTTON_ID);
        setRouteStartButton.setVisibility(View.INVISIBLE);

        setRouteEndButton = new FloatingActionButton(this);
        setRouteEndButton.setColorNormal(getResources().getColor(R.color.my_material_green));
        setRouteEndButton.setColorPressed(getResources().getColor(R.color.my_material_light_green));
        setRouteEndButton.setImageResource(android.R.drawable.ic_menu_send);
        setRouteEndButton.setOnClickListener(this);
        setRouteEndButton.setId(END_ROUTE_BUTTON_ID);
        setRouteEndButton.setVisibility(View.INVISIBLE);

        //Crea y configura los layouts que contiene cada elemento
        RelativeLayout.LayoutParams mapViewLayout = new RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        RelativeLayout.LayoutParams searchViewLayaout = new RelativeLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        RelativeLayout.LayoutParams findMeButtonLayout = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        RelativeLayout.LayoutParams rbLayout = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        RelativeLayout.LayoutParams informationTextLayout = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);

        searchViewLayaout.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        searchViewLayaout.addRule(RelativeLayout.CENTER_VERTICAL);
        searchViewLayaout.setMargins(px(20),px(10),px(20),px(0));

        findMeButtonLayout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        findMeButtonLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        findMeButtonLayout.setMargins(px(0), px(0), px(10), px(10));

        rbLayout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        rbLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rbLayout.setMargins(px(0), px(0), px(10), px(10));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(px(5),px(5),px(5),px(5));

        informationTextLayout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        informationTextLayout.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        informationTextLayout.setMargins(px(10),px(0),px(0),px(10));

        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        ll.setGravity(Gravity.RIGHT);
        ll.addView(setRouteEndButton, layoutParams);
        ll.addView(setRouteStartButton, layoutParams);
        ll.addView(findMeButton, layoutParams);


        //Añade cada elemento al Layout que contiene el resto.
        containerLayout.addView(miMapa, mapViewLayout);
        containerLayout.addView(searchView, searchViewLayaout);
        containerLayout.addView(ll, findMeButtonLayout);
        containerLayout.addView(informationText,informationTextLayout);

        setContentView(containerLayout);

    }
    public static void showFloatingMenu(boolean hide) {
        if (hide) {
            setRouteStartButton.setVisibility(View.VISIBLE);
            setRouteEndButton.setVisibility(View.VISIBLE);
        }
        else {
            setRouteStartButton.setVisibility(View.INVISIBLE);
            setRouteEndButton.setVisibility(View.INVISIBLE);
        }
    }
    public static void setInformationText(String text) {
        informationText.setText(text);
    }

    /**
     * Devuelve la cantidad de pixeles deseados deacuerdo a la resolución de la pantalla, dependiendo si es
     * QHD, HD, FullHD, o pantallas de menor resolución.
     * @param dips hace referencia a los pixeles deseados.
     * @return
     */
    private int px(float dips)
    {
        float DP = getResources().getDisplayMetrics().density;
        return Math.round(dips * DP);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case FIND_ME_BUTTON_ID:
                miMapa.locateMe();
                //miMapa.notifyMessage("Ubicame!");
                break;
            case START_ROUTE_BUTTON_ID:
                miMapa.setRouteStart();
                break;
            case END_ROUTE_BUTTON_ID:
                miMapa.setRouteEnd();
                break;
        }
    }
}
