package com.proyecto.uis.uismaps;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

/**
 * ContentManager maneja los componentes de la UI de la app, habilitando y deshabilitando la interfaz adaptada para
 * las capacidades de voz.
 * Created by cheloreyes on 9/03/15.
 */
public class ContentManager extends View implements Constants, View.OnClickListener, View.OnTouchListener {

    // **********************
    // Constants
    // **********************
    public static final int START_POINT_BTN = 207;
    public static final int END_POINT_BTN = 17;
    public static final int CANCEL_BTN = 27;
    private static final int OUT_OF_SERVICE = 0 ;
    private static final int TEMPORARILY_UNAVAILABLE = 1;
    private static final int AVALIABLE = 2;
    public static final int DPI_NEXUS5 = 480;
    private static final String TAG = "ContentManager";
    // **********************
    // Fields
    // **********************

    private final SlidingUpPanelLayout iPanel;
    private final FrameLayout iMapContainer;
    private final FloatingActionButton iLocationBtn;
    private final FloatingActionButton iRoutesBtn;
    private final ImageView iImgInfo;
    private final TextView iInfoTextA;
    private final TextView iTileTxt;
    private final TextView iInfoTextB;
    private final TextView iBodyText;
    private final TextView iStatus;
    private final Vibrator iVibrator;
    private int btn_switch = START_POINT_BTN;

    private Activity callerActivity;
    private VoiceManager iVoiceManager;
    private boolean isRouting = false;
    private boolean isLocated = false;
    private boolean isFirstPoint = true;
    private Context miContext;
    private MapView miMapview;
    private SharedPreferences preferences;
    private SearchView searchView;
    private long touchTime;
    private CompassCtrl iCompass;

    // **********************
    // Constructor
    // **********************
    /**
     *
     * @param context Context de la vista en que se crea el objeto.
     * @param mapView Objeto de la clase @MapView ya instanceado antes.
     */
    public ContentManager(Context context, MapView mapView, VoiceManager voiceManager, FloatingActionButton locationBtn, FloatingActionButton routesBtn, ImageView imgInfo
    , TextView title, TextView infoTextA, TextView infoTextB, TextView bodyText, SlidingUpPanelLayout panel, FrameLayout mapContainer, TextView statusText) {
        super(context);
        miContext = context;
        iVoiceManager = voiceManager;
        iPanel = panel;
        iLocationBtn = locationBtn;
        iRoutesBtn = routesBtn;
        iImgInfo = imgInfo;
        iTileTxt = title;
        iInfoTextA = infoTextA;
        iInfoTextB = infoTextB;
        iBodyText = bodyText;
        iStatus = statusText;
        iMapContainer = mapContainer;
        miMapview = mapView;
        preferences = PreferenceManager.getDefaultSharedPreferences(miContext);
        iPanel.setPanelState(PanelState.HIDDEN);
        iLocationBtn.setOnClickListener(this);
        iRoutesBtn.setOnClickListener(this);

        iVibrator = (Vibrator) miContext.getSystemService(Context.VIBRATOR_SERVICE);
        iCompass = new CompassCtrl(miContext);
    }


    // **********************
    // Methods
    // **********************

    public void checkViews(){
        if(preferences.getBoolean(EYESIGHT_ASSISTANT,false)){
            if (iPanel.getPanelState() != PanelState.HIDDEN) {
                iPanel.setPanelState(PanelState.HIDDEN);
            }
            iPanel.setTouchEnabled(false);
            iLocationBtn.setEnabled(false);
            iLocationBtn.setVisibility(INVISIBLE);
            initBlindCapabilities();
        }
        else{
            iPanel.setTouchEnabled(true);
            iLocationBtn.setEnabled(true);
            iLocationBtn.setVisibility(VISIBLE);
        }
    }

    /**
     * Establece al layout contenedor a registrar los eventos tactiles.
     */
    private void initBlindCapabilities() {
        iMapContainer.setOnTouchListener(this);
        touchTime = 0;
    }

    /**
     * Intenta una peticion para usar el reconocimiento de voz del sistema @VOICE_RECOGNITION,
     * especificando que reconozca no solo el ingles y la cantidad de resultados que se quieren recibir.
     *
     * El resultado de esto es capturado en @Activity.onActivityResult con el código de @VOICE_RECOGNITION.
     */
    public void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, RecognizerIntent.EXTRA_LANGUAGE_MODEL);
        intent.putExtra(RecognizerIntent.EXTRA_RESULTS, 1);

        callerActivity.startActivityForResult(intent, MediaRecorder.AudioSource.VOICE_RECOGNITION);
    }

    public void setPanelContent(String title, String textInfoA, String textInfoB, String bodyText, int img) {
        iPanel.setPanelState(PanelState.ANCHORED);
        if(title != null) {
            resizeText(title);
            iTileTxt.setText(title);
        }
        if(textInfoA != null) iInfoTextA.setText(textInfoA);
        if(textInfoB != null) iInfoTextB.setText(textInfoB);
        iImgInfo.setImageResource(img);
        iPanel.setTouchEnabled(false);
    }
    public void setPanelContent(String title) {
        iPanel.setPanelState(PanelState.COLLAPSED);
        if(title != null) {
            resizeText(title);
            iTileTxt.setText(title);
        }
        iPanel.setTouchEnabled(false);
    }

    private void resizeText(String text) {
        if(text != null) {
            if (text.length() > 20) {
                iTileTxt.setTextSize(18.0f);
            } else {
                iTileTxt.setTextSize(24.0f);
            }
        }
    }

    private void changeRouteBtnIcon() {
        switch(btn_switch) {
            case START_POINT_BTN:
                iRoutesBtn.setImageResource(R.mipmap.add_point);
                break;
            case END_POINT_BTN:
                iRoutesBtn.setImageResource(R.mipmap.route_point);
                break;
            case CANCEL_BTN:
                iRoutesBtn.setImageResource(R.mipmap.cancel_route);
                break;
        }
    }

    public void restoreContent(){
        btn_switch = START_POINT_BTN;
        changeRouteBtnIcon();
        setStatusLocationBtn(OUT_OF_SERVICE);
        if (iPanel != null && iPanel.getPanelState() != PanelState.HIDDEN) {
            iPanel.setPanelState(PanelState.HIDDEN);
        }
        invalidate();
    }
    public void setStatusLocationBtn(int status){
        int icon = 0;
        switch (status){
            case OUT_OF_SERVICE:
                icon = R.mipmap.gps_inactive;
                break;
            case TEMPORARILY_UNAVAILABLE:
                icon = R.mipmap.gps_inactive;
                break;
            case AVALIABLE:
                icon = R.mipmap.gps_active;
                break;
        }
        iLocationBtn.setImageResource(icon);
    }
    /**
     * Establece el @Activity donde se quiere iniciar el reconocimiento de voz, se prefiere usar el principal.
     * @param activity actividad donde se espera el resultado
     */
    public void setCallingActivity(Activity activity) {
        callerActivity = activity;
    }



    // **********************
    // Methods from SuperClass
    // **********************
    /**
     * Llamado cuando la vista a sido clikeada.
     *
     * @param v La vista que ha sido clikeada.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loc_btn:
                //iStatus.setText("rotación: " + iCompass.getCurrentDegree());
                miMapview.locateMe();
                if(!miMapview.isHasAccurancy()) {
                    btn_switch = END_POINT_BTN;
                    changeRouteBtnIcon();
                }
                break;

            case R.id.btn_slider:
                if(miMapview.isNavigating()){
                    btn_switch = CANCEL_BTN;
                }
               switch(btn_switch) {
                   case START_POINT_BTN:
                       miMapview.setRouteStart();
                       Log.v(TAG,"switch: "+btn_switch);
                       btn_switch = END_POINT_BTN;
                       changeRouteBtnIcon();
                       break;
                   case END_POINT_BTN:
                       miMapview.setRouteEnd();
                       Log.v(TAG,"switch: "+btn_switch);
                       btn_switch = CANCEL_BTN;
                       changeRouteBtnIcon();
                       break;
                   case CANCEL_BTN:
                       miMapview.removeMapObjects();
                       Log.v(TAG,"switch: "+btn_switch);
                       restoreContent();
                       break;
               }
                break;
        }
    }

    /**
     * Método Override de @OnTouchListener,
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchTime = System.currentTimeMillis();
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                    }
                }, 500);
                iVibrator.vibrate(50);
                return true;

            case MotionEvent.ACTION_UP:
                if((System.currentTimeMillis() - touchTime) > ViewConfiguration.getLongPressTimeout()) {
                    Log.v(TAG, "Long press.");
                    iVibrator.vibrate(200);
                    //iVoiceManager.textToSpeech(miContext.getString(R.string.start_voice_recognition));
                    startVoiceRecognition();
                    return false;
                }
                else{
                    Log.v(TAG, "Single press.");
                    //miMapview.locateMe();
                    return false;
                }
        }
        return true;
    }
}
