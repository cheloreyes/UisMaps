package com.proyecto.uis.uismaps.Content;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;
import com.proyecto.uis.uismaps.Constants;
import com.proyecto.uis.uismaps.mapview.MapView;
import com.proyecto.uis.uismaps.Notify;
import com.proyecto.uis.uismaps.R;
import com.proyecto.uis.uismaps.VoiceManager;
import com.proyecto.uis.uismaps.finder.Finder;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

/**
 * ContentManager controla los componentes de la UI de la app, habilitando y deshabilitando la interfaz adaptada para las personas con discapacidad
 * visual, también permite el control del panel deslizante que permite desplegar información.
 * Created by cheloreyes on 9/03/15.
 */
public class ContentManager extends View implements Constants, View.OnClickListener, View.OnLongClickListener {

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
    public static final int ID_MAP_CONTAINER = 15;
    public static final int MAX_TEXT_LENGTH = 20;
    public static final float MIN_SIZE = 18.0f;
    public static final float MAX_SIZE = 24.0f;
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
    private final TextView NavInfoA;
    private final TextView NavInfoB;
    private final Vibrator iVibrator;
    private final ListView iListView;
    private final TextView iDesciption;
    private int btn_switch = START_POINT_BTN;
    private Activity callerActivity;

    private Context miContext;
    private MapView miMapview;
    private SharedPreferences preferences;
    private Finder iFinder;
    private Notify iNotify;

    private int i = 0;
    private VoiceManager iVoiceManager;
    // **********************
    // Constructor
    // **********************


    /**
     * Toma y guada como referencia todos los componentes de la UI.
     * @param context
     * @param mapView
     * @param voiceManager
     * @param locationBtn
     * @param routesBtn
     * @param imgInfo
     * @param title
     * @param infoTextA
     * @param infoTextB
     * @param bodyText
     * @param panel
     * @param mapContainer
     * @param statusText
     * @param listView
     */
    public ContentManager(Context context, MapView mapView, VoiceManager voiceManager, FloatingActionButton locationBtn, FloatingActionButton routesBtn, ImageView imgInfo
    , TextView title, TextView infoTextA, TextView infoTextB, TextView bodyText, SlidingUpPanelLayout panel, FrameLayout mapContainer, TextView statusText, ListView listView,
                          TextView description, TextView navInfoA, TextView navInfoB) {
        super(context);
        miContext = context;
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
        iDesciption = description;
        NavInfoA = navInfoA;
        NavInfoB = navInfoB;

        preferences = PreferenceManager.getDefaultSharedPreferences(miContext);
        iPanel.setPanelState(PanelState.HIDDEN);
        iLocationBtn.setOnClickListener(this);
        iLocationBtn.setOnLongClickListener(this);
        iRoutesBtn.setOnClickListener(this);
        iListView = listView;
        iVibrator = (Vibrator) miContext.getSystemService(Context.VIBRATOR_SERVICE);
        iNotify = new Notify(miContext, voiceManager);
        iFinder = new Finder(miContext);
    }


    // **********************
    // Methods
    // **********************

    /**
     * Habilita o desabilita los componentes de la UI según esté habilitada o no la interfaz para personas con discapacidad visual.
     */
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
     * Establece al layout contenedor a registrar los eventos tactiles. Cuando se habilita la interfaz especial.
     */
    private void initBlindCapabilities() {
        iMapContainer.setOnClickListener(this);
        iMapContainer.setOnLongClickListener(this);
        iMapContainer.setId(ID_MAP_CONTAINER);
    }

    /**
     * Establece el contenido del panel flotante con información completa, hastá la mitad, bloqueando su interacción.
     * @param title Titulo a mostrar en la cabecera del panel.
     * @param textInfoA Información sencilla.
     * @param textInfoB Información sencilla.
     * @param img Imagen.
     */
    public void setPanelContent(String title, String textInfoA, String textInfoB, int img) {
        iPanel.setPanelState(PanelState.ANCHORED);
        resetViews();
        if(title != null) {
            resizeText(title);
            iTileTxt.setText(title);
        }
        if(textInfoA != null) NavInfoA.setText(textInfoA);
        if(textInfoB != null) NavInfoB.setText(textInfoB);
        iImgInfo.setImageResource(img);
        iImgInfo.setScaleY(1.0f);
        iImgInfo.setScaleX(1.0f);
        iPanel.setAnchorPoint(0.4f);
        iPanel.setTouchEnabled(false);
    }

    /**
     * Establece el contenido del panel flotante en estado @PanelState.COLLAPSED, posibilitando su interacción según sea requerido.
     * @param title Titulo a mostrar en la cabecera del panel.
     * @param enablePanel Habilitar interacción.
     */
    public void setPanelContent(String title, boolean enablePanel) {
        Bitmap imgBuilding = null;
        String description = null;
        int dependencesSize = 0;
        resetViews();
        if(title != null) {
            resizeText(title);
            iTileTxt.setText(title);
            if(enablePanel){
                iPanel.setAnchorPoint(0.35f);
                iInfoTextA.setTextColor(miContext.getResources().getColor(R.color.my_material_green));
                iInfoTextB.setTextColor(miContext.getResources().getColor(R.color.my_material_green));
                iInfoTextA.setText("Dependencia");
                iInfoTextB.setText("Espacio");
                iInfoTextB.setTextSize(19.0f);
                iInfoTextA.setTextSize(19.0f);
                iListView.setAdapter(iFinder.getDependencesAdapter(title));
                dependencesSize = iFinder.getiSpaces().size();
                if(dependencesSize== 0) {
                    iInfoTextA.setVisibility(INVISIBLE);
                    iInfoTextB.setVisibility(INVISIBLE);
                }
                else{
                    iInfoTextA.setVisibility(VISIBLE);
                    iInfoTextB.setVisibility(VISIBLE);
                }
                imgBuilding = iFinder.getImgBuilding(title);
                if(imgBuilding != null) {
                    iImgInfo.setImageBitmap(imgBuilding);
                    iImgInfo.setScaleY(1.6f);
                    iImgInfo.setScaleX(1.6f);
                }
                else iImgInfo.setImageBitmap(null);
                description = iFinder.getDescriptionBuilding(title);
                if(description != null) {
                    iDesciption.setVisibility(VISIBLE);
                    iDesciption.setText(description);
                }
                else iDesciption.setVisibility(INVISIBLE);
            }
            else {
                iInfoTextA.setTextColor(miContext.getResources().getColor(R.color.primary_dark_material_dark));
                iInfoTextB.setTextColor(miContext.getResources().getColor(R.color.primary_dark_material_dark));
            }
            iPanel.setTouchEnabled(enablePanel);
            iPanel.setPanelState(PanelState.COLLAPSED);
        }
        if(title == miContext.getString(R.string.no_places_nearby)){
            iPanel.setTouchEnabled(false);
        }
        if(imgBuilding == null && description == null && dependencesSize == 0){
            iPanel.setTouchEnabled(false);
        }
    }

    /**
     * Cambia el tamaño del texto mostrado en el título según la longitud de la cadena de texto.
     * @param text
     */
    private void resizeText(String text) {
        if(text != null) {
            if (text.length() > MAX_TEXT_LENGTH) {
                iTileTxt.setTextSize(MIN_SIZE);
            } else {
                iTileTxt.setTextSize(MAX_SIZE);
            }
        }
    }

    /**
     * Cambia el icono del botón sobre el panel flotante según el tipo de acción a realizar, que puede ser:
     *      * @START_POINT_BTN: Agregar punto de inicio.
     *      * @END_POINT_BTN: Agregar punto de destino.
     *      * @CANCEL_BTN: Cancelar .
     */
    public void changeRouteBtnIcon(int btn_switch) {
        this.btn_switch = btn_switch;
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

    /**
     * Restaura el contenido de la UI.
     */
    public void restoreContent(){
        changeRouteBtnIcon(START_POINT_BTN);
        setStatusLocationBtn(OUT_OF_SERVICE);
        if (iPanel != null && iPanel.getPanelState() != PanelState.HIDDEN) {
            iPanel.setPanelState(PanelState.HIDDEN);
        }
        invalidate();
    }

    /**
     * Cambia el estado del botón de ubicar, resaltando su icono cuando está activo el GPS.
     * @param status Estado del GPS.
     */
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

    private void resetViews() {
        NavInfoB.setText("");
        NavInfoA.setText("");
        iInfoTextA.setText("");
        iInfoTextB.setText("");
    }
    public void setVoiceManager(VoiceManager voiceManager){
        iVoiceManager = voiceManager;
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
                //miMapview.showNavigation();

                break;

            case R.id.btn_slider:
               switch(btn_switch) {
                   case START_POINT_BTN:
                       miMapview.setRouteStart();
                       Log.v(TAG,"switch: "+btn_switch);
                       changeRouteBtnIcon(END_POINT_BTN);
                       break;
                   case END_POINT_BTN:
                       miMapview.setRouteEnd();
                       Log.v(TAG,"switch: "+btn_switch);
                       changeRouteBtnIcon(CANCEL_BTN);
                       break;
                   case CANCEL_BTN:
                       miMapview.removeMapObjects();
                       Log.v(TAG,"switch: "+btn_switch);
                       restoreContent();
                       break;
               }
                break;
            case ID_MAP_CONTAINER:
                if(iVoiceManager.isSpeaking()){
                    iVoiceManager.stop();
                }
                else{
                    Log.v(TAG, "Click Simple");
                    miMapview.locateMe();
                }
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        Log.v(TAG, "Long Click ");
        switch (v.getId()) {
            case ID_MAP_CONTAINER:
                if(miMapview.isNavigating()){
                    miMapview.switchNavigation(false);
                }
                else{
                    iNotify.newNotification(miContext.getString(R.string.after_tone));
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            startVoiceRecognition();
                        }
                    }, 3000);
                }
                break;
            case R.id.loc_btn:
                miMapview.switchOffLocation(false);
                miMapview.removeMapObjects();
        }
        //startVoiceRecognition();
        return true;
    }
}
