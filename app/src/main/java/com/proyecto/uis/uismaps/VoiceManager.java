package com.proyecto.uis.uismaps;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import com.proyecto.uis.uismaps.finder.Finder;
import com.proyecto.uis.uismaps.mapview.MapView;

import java.util.Locale;

/**
 * La clase VoiceManager permite las opciones de sintetizador de voz y reconocimiento de voz
 * de UISMaps como herramientas de ayuda para su utilización por personas con discapacidad visual,
 * respondiendo a comandos de voz e indicando acciones a travez del sintetizador de voz.
 *
 * Created by cheloreyes on 9/03/15.
 */
public class VoiceManager implements TextToSpeech.OnInitListener{

    private final Context miContext;
    private int lastTurnType = 0;
    private boolean isEngineInitialized = false;
    private Finder iFinder;
    private Locale colombia;
    private MapView iMapView;
    private TextToSpeech miTts;
    private SharedPreferences preferences;
    private String[] buildings;
    private Thread speaking;

    /**
     * Inicializa el motor de voz en referencia al contexto de la aplicación. Se establecen
     * los parámetros de ubicación y lenguaje de habla.
     * @param pContext contexto utilizado.
     */
    public VoiceManager(Context pContext) {
        miContext = pContext;
        miTts = new TextToSpeech(miContext, this);
        colombia = new Locale("es", "COL");
        //miTts.setLanguage(colombia);
        preferences = PreferenceManager.getDefaultSharedPreferences(miContext);
        iFinder = new Finder(miContext);
        buildings = iFinder.getBuildingList();
    }

    /**
     * Este método "habla" el texto específico, agregando a la cola el texto nuevo si se es llamado durante
     * la ejecución.
     * @param pText Texto a que se quiere sintetizar.
     */
    public void textToSpeech(final String pText) {
        if(isEngineInitialized) {
            speaking = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (pText != null && !miTts.isSpeaking()) {
                        Log.v("textToSpeech", "hablando: " + pText);
                        if (Build.VERSION.SDK_INT < 21) {
                            miTts.speak(pText, TextToSpeech.QUEUE_ADD, null);
                        } else {
                            miTts.speak(pText, TextToSpeech.QUEUE_ADD, null, "");
                        }
                    }
                }
            });
            speaking.start();
        }
    }

    /**
     * Captura el resultado de el reconocimiento de voz del sistema, el cual es tratado por @Thesaurus
     * y es comparado con el listado de edificios del campus si coincide con alguno es referenciado por @MapView.
     * @param sentence resultado de la acción de reconocimiento de voz.
     */
    public void textRecognizer(String sentence) {
        Log.v("Voice", "Sentencia: " + sentence);
        boolean to = true;
        Thesaurus thesaurus = new Thesaurus(sentence);
        sentence = thesaurus.getResult();
        Log.v("Voice", "Sentencia arreglada: " + sentence);
        String delimiters = "[ .,;?!¡¿\'\"\\[\\]]+";
        String[] words = sentence.split(delimiters);
        String where = null;
        Log.v("Voice", "Numero de palabras " + words.length);
        for (int i = 0; i< words.length; i++) {
            if(to){
                for (int j= 0; j < buildings.length; j++) {
                    if(to){
                        Log.v("Voice", "Buscando por: "+words[i]);
                        String[] places = buildings[j].split(delimiters);
                        for (int k = 0; k < places.length; k++) {
                            if(to){
                                if(words[i].equalsIgnoreCase(places[k])){
                                    Log.v("Voice", "coincide con: " + buildings[j]);
                                    if(i <= words.length - 1){
                                        if(words[words.length - 1].equalsIgnoreCase(places[places.length - 1]) || words.length < 2)
                                        {
                                            Log.v("Voice", "La última palabra coincide");
                                            iMapView.foundFocus(buildings[j]);
                                            to = false;
                                        }
                                        else{
                                            Log.v("Voice", "No coincide la última palabra");
                                            to = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if(to){
            //textToSpeech(miContext.getString(R.string.place_no_found));
            textToSpeech(sentence + miContext.getString(R.string.place_no_found));
        }
    }

    /**
     * Sintetiza las acciones de navegación indicandole al usuario los comandos entregados por la
     * función de navegación de @MapView.
     * @param turnType El tipo de giro (Derecha, Izquierda, Adelante)
     * @param degrees Angulo de giro.
     * @param dist Distancia para el próximo giro.
     */
    public void navigation(int fulldistance, int turnType, double degrees, double dist, double nextDist, String place){
        if(lastTurnType != turnType && lastTurnType != 0) {
            String toSpeech = "";
            degrees = Math.abs(Math.round(degrees));
            switch (turnType) {
                case R.mipmap.ahead_arrow:
                    toSpeech = " Continúa adelante";
                    break;
                case R.mipmap.left_arrow:
                    toSpeech = " Gire a la izquierda";
                    break;
                case R.mipmap.soft_left_arrow:
                    toSpeech = " Gire a la izquierda";
                    break;
                case R.mipmap.right_arrow:
                    toSpeech = " Gire a la derecha";
                    break;
                case R.mipmap.soft_right_arrow:
                    toSpeech = " Gire a la derecha";
                    break;
            }
            if(fulldistance == 0) {
                stop();
                toSpeech = miContext.getString(R.string.arrive) + " a su destino: " + place;
            }
            else {
                stop();
                toSpeech = "A: " + Math.round(dist) +" Metros.\n" + toSpeech + ".";
                if(degrees > 40) {
                    toSpeech = toSpeech + degrees + " grados.";
                }
                if(nextDist > 0) {
                    toSpeech = toSpeech + " Luego, continúe " + (int) nextDist + " Metros, aproximada-mente.";
                }
            }
            if(!miTts.isSpeaking())textToSpeech(toSpeech);
        }
        lastTurnType = turnType;

    }

    /**
     * Si el motor de voz está en ejecución (miTts.isSpeaking)lo detiene.
     */
    public void stop() {
        miTts.stop();
    }

    /**
     * Apaga el motor de voz
     */
    public void shutdown() {
        miTts.shutdown();
    }

    /**
     * Inicializa mapView con un objeto ya instanceado anteriormente.
     * @param mapView
     */
    public void setMapView(MapView mapView) {
        iMapView = mapView;
    }

    public boolean isSpeaking() {
        return miTts.isSpeaking();
    }


    /**
     * Called to signal the completion of the TextToSpeech engine initialization.
     *
     * @param status {@link android.speech.tts.TextToSpeech#SUCCESS} or {@link android.speech.tts.TextToSpeech#ERROR}.
     */
    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS) {
            int result = miTts.setLanguage(colombia);
            if(result == TextToSpeech.LANG_MISSING_DATA) {
                Log.e("error", "lenguaje no soportado");
            }
            else {
                isEngineInitialized = true;
                Log.v("VoiceManager", "TTS inicializado");

            }
        }
        else {
            Log.e("error", "No se pudo inicializar");
        }
    }
}
