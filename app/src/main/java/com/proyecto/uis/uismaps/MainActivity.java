package com.proyecto.uis.uismaps;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity implements View.OnClickListener{

    public static final int MATCH_PARENT = RelativeLayout.LayoutParams.MATCH_PARENT;
    public static final int WRAP_CONTENT = RelativeLayout.LayoutParams.WRAP_CONTENT;
    private static final int FIND_ME_BUTTON_ID = 1;

    private RelativeLayout containerLayout;
    private DisplayMetrics miDisplayMetrics;
    private MapView miMapa;
    private LayoutManager miLayoutManager;
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

    }

    public void setMyContent() {
        containerLayout = new RelativeLayout(this);
        TextView titleTextView = new TextView(this);
        ImageButton findMeButton = new ImageButton(this);
        SearchView searchView = new SearchView(this);

        titleTextView.setTextAppearance(this, android.R.style.TextAppearance_Material_Medium);
        titleTextView.setText(this.getString(R.string.title));

        searchView.setBackgroundColor(Color.LTGRAY);

        findMeButton.setImageResource(android.R.drawable.ic_menu_mylocation);
        findMeButton.setId(FIND_ME_BUTTON_ID);

        RelativeLayout.LayoutParams mapViewLayout = new RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        RelativeLayout.LayoutParams searchViewLayaout = new RelativeLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        RelativeLayout.LayoutParams findMeButtonLayout = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);

        searchViewLayaout.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        searchViewLayaout.addRule(RelativeLayout.CENTER_VERTICAL);
        searchViewLayaout.setMargins(px(20),px(10),px(20),px(0));

        findMeButtonLayout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        findMeButtonLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        findMeButtonLayout.setMargins(px(0), px(0), px(10), px(10));

        containerLayout.addView(miMapa, mapViewLayout);
        containerLayout.addView(searchView, searchViewLayaout);
        containerLayout.addView(findMeButton, findMeButtonLayout);

        setContentView(containerLayout);

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
                miMapa.notifyMessage("Ubicame!");
                break;
        }
    }
}
