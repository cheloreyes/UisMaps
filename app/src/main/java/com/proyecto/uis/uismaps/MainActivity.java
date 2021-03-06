package com.proyecto.uis.uismaps;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.melnykov.fab.FloatingActionButton;
import com.proyecto.uis.uismaps.Content.Alerts;
import com.proyecto.uis.uismaps.Content.ContentManager;
import com.proyecto.uis.uismaps.Content.SettingsActivity;
import com.proyecto.uis.uismaps.categories.CategoriesAdapter;
import com.proyecto.uis.uismaps.categories.CategoriesBuilder;
import com.proyecto.uis.uismaps.finder.Finder;
import com.proyecto.uis.uismaps.mapview.MapView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements Constants {

    private static final String TAG = "MainUISMapsActivity";

    private boolean doubleBackToExitPressedOnce;
    private VoiceManager iVoiceManager;
    private DisplayMetrics miDisplayMetrics;
    private MapView miMapa;
    private SharedPreferences preferences;
    private ContentManager miContent;

    private SlidingUpPanelLayout mLayout;
    private Finder iFinder;
    private SearchView searchView;
    private CompassCtrl compass;
    private int panelStatus = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FlurryAgent.init(this, "NWPQQCYW2ZTKTSKSBQVH");

        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        miDisplayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(miDisplayMetrics);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        miMapa = new MapView(this, miDisplayMetrics.densityDpi);
        iVoiceManager = new VoiceManager(this);
        iVoiceManager.setMapView(miMapa);
        init_Componets();
        miMapa.setVoiceManager(iVoiceManager);
        miMapa.setContentManager(miContent);
        miMapa.setiCompass(compass);
        miContent.setCallingActivity(this);
        lunchTutorial();
        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        iFinder = new Finder(this, searchView, miMapa);
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
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            return true;
        }
        if(id == R.id.action_poll) {
            lunchPoll();
        }
        if(id == R.id.show_tutorial) {
            if(preferences.getBoolean(EYESIGHT_ASSISTANT, false)) {
                iVoiceManager.textToSpeech(this.getString(R.string.voice_intro), true);
            }else{
                new Alerts(this).tutorialScreen(panelStatus);
            }
        }
        if(id == R.id.action_categories) {
            CategoriesBuilder categories = new CategoriesBuilder(this);
            categories.setiFider(iFinder);
            //startActivity(new Intent(getApplicationContext(), CategoriesActivity.class));
            new Alerts(this).showCategories(categories);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        miMapa.removeMapObjects();
        //miMapa.toggleGPS(false);
        miContent.restoreContent();
        //iVoiceManager.stop();
        if (doubleBackToExitPressedOnce) {
            miMapa.switchOffLocation(false);

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
    protected void onPause() {
        super.onPause();
        miMapa.saveState();
        compass.pauseSensor();
        //miMapa.toggleGPS(false);
        //iVoiceManager.stop();
        //TODO: agregar a los estados guardados el estado del GPS y la ubicación del punto seleccionado.
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        iVoiceManager.shutdown();
        compass.pauseSensor();
        iFinder.closeDataBase();
        FlurryAgent.onEndSession(this);
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
        //miContent.setMyContent(preferences.getBoolean(EYESIGHT_ASSISTANT, false));
        miContent.checkViews();
        compass.resumeSensors();
    }

    /**
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
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

    /**
     * Inicializa los componentes de la interfaz que luego son referidos a la istancia de @ContentManager para su disposición y control.
     */
    private void init_Componets() {
        mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mLayout.setAnchorPoint(0.35f);
        //mLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
        FrameLayout mapContainer = (FrameLayout) findViewById(R.id.map_container);
        mapContainer.addView(miMapa);
        FloatingActionButton btnLocation = (FloatingActionButton) findViewById(R.id.loc_btn);
        final FloatingActionButton btnSlider= (FloatingActionButton) findViewById(R.id.btn_slider);
        ImageView imgSlider = (ImageView) findViewById(R.id.img_slider);
        TextView titleText = (TextView)findViewById(R.id.title_slider);
        TextView infoTextA = (TextView) findViewById(R.id.info_text_a);
        TextView infoTextB = (TextView) findViewById(R.id.info_text_b);
        TextView statusText = (TextView) findViewById(R.id.status_text);
        TextView bodyTextA= (TextView) findViewById(R.id.office);
        ListView listView = (ListView) findViewById(R.id.body_list);
        TextView descriptionTxt = (TextView) findViewById(R.id.description_text);
        TextView navInfoA= (TextView) findViewById(R.id.nav_info_a);
        TextView navInfoB= (TextView) findViewById(R.id.nav_info_b);
        final ImageView compassPointer = (ImageView) findViewById(R.id.compass_pointer);
        final ImageView compassBase = (ImageView) findViewById(R.id.compass);
        mLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
            }
            @Override
            public void onPanelExpanded(View panel) {
                btnSlider.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onPanelCollapsed(View panel) {
                btnSlider.setVisibility(View.VISIBLE);
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) compassPointer.getLayoutParams();
                lp.topMargin = 0;
                compassPointer.setLayoutParams(lp);
                compassBase.setLayoutParams(lp);
                panelStatus = 1;

            }
            @Override
            public void onPanelAnchored(View panel) {
                btnSlider.setVisibility(View.VISIBLE);
                compassBase.setVisibility(View.VISIBLE);
                compassPointer.setVisibility(View.VISIBLE);
            //miMapa.fixCenterView();
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) compassPointer.getLayoutParams();
                Point size = new Point();
                getWindowManager().getDefaultDisplay().getSize(size);
                int height = size.y;
                if(miMapa.isNavigating()){
                    lp.topMargin = (int) (height * 0.35f);
                }
                else{
                    lp.topMargin = (int) (height * 0.3f);
                }

                compassBase.setLayoutParams(lp);
                compassPointer.setLayoutParams(lp);
                panelStatus = 1;
            }
            @Override
            public void onPanelHidden(View panel) {
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) compassPointer.getLayoutParams();
                lp.topMargin = 0;
                compassPointer.setLayoutParams(lp);
                compassBase.setLayoutParams(lp);
                panelStatus = 0;
            }
        });
        FloatingActionButton btnInfo = (FloatingActionButton) findViewById(R.id.info_btn);
        miContent = new ContentManager(this, miMapa, iVoiceManager, btnLocation, btnSlider,
                                              imgSlider, titleText, infoTextA, infoTextB, bodyTextA, mLayout, mapContainer, statusText, listView, descriptionTxt,
                navInfoA, navInfoB, btnInfo);
        //miContent.setVoiceManager(iVoiceManager);
        compass = new CompassCtrl(this, compassPointer);
    }
    public void lunchPoll() {
        String url = "http://goo.gl/forms/Kra0xi76Yj";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }
    private void lunchTutorial()
    {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean ranBefore = preferences.getBoolean(SHOW_TUTORIAL, false);
        if (!ranBefore) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(SHOW_TUTORIAL, true);
            editor.commit();
            new Alerts(this).tutorialScreen(0);
        }
    }

}
