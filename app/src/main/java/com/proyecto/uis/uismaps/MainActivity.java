package com.proyecto.uis.uismaps;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements UISMapsSettingsValues{

    private boolean doubleBackToExitPressedOnce;
    private VoiceManager iVoiceManager;
    private DisplayMetrics miDisplayMetrics;
    private MapView miMapa;
    private SharedPreferences preferences;
    private ContentManager miContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        miDisplayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(miDisplayMetrics);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        miMapa = new MapView(this, miDisplayMetrics.densityDpi);
        iVoiceManager = new VoiceManager(this);
        iVoiceManager.setMapView(miMapa);

        miContent = new ContentManager(this, miMapa, iVoiceManager);
        setContentView(miContent.getContainer());

        miMapa.setContentManager(miContent);
        miMapa.setVoiceManager(iVoiceManager);
        miContent.setCallingActivity(this);

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
            startActivity(new Intent(getApplicationContext(), CouplingActivity.class));
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
        iVoiceManager.stop();
        //TODO: agregar a los estados guardados el estado del GPS y la ubicación del punto seleccionado.
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */
    @Override
    protected void onResume() {
        super.onResume();
        miContent.setMyContent(preferences.getBoolean(EYESIGHT_ASSISTANT, false));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        iVoiceManager.shutdown();
    }

    @Override
    public void onBackPressed() {
        miMapa.removeMapObjects();
        miContent.showFloatingMenu(false);
        miContent.navInfo_destroy();
        iVoiceManager.stop();
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        doubleBackToExitPressedOnce = true;
        //Toast.makeText(this, this.getString(R.string.confirm_exit), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 1000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MediaRecorder.AudioSource.VOICE_RECOGNITION && resultCode == RESULT_OK) {
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if(matches.size() > 0) {
                //miContent.setInfoText(matches.get(0) + " ");
                iVoiceManager.textRecognizer(matches.get(0) + "");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
