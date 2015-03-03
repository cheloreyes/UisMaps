package com.proyecto.uis.uismaps;

import android.content.pm.ActivityInfo;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends FragmentActivity {
    private static final String MAP_FRAGMENT_TAG = "Map_Fragment_CartoType";

/*
    Llamado cuando la actividad es creada por primera vez.
 */

    private MapView miMapa;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        FragmentManager fm = this.getSupportFragmentManager();
        if(fm.findFragmentByTag(MAP_FRAGMENT_TAG) == null) {
            ViewController iViewController = ViewController.newInstance();
            fm.beginTransaction().add(R.id.map_container,iViewController,MAP_FRAGMENT_TAG).commit();
        }
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


}
