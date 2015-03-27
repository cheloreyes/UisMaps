package com.proyecto.uis.uismaps;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

/**
 * Esta clase permite la función de texto a voz.
 *
 * Created by cheloreyes on 9/03/15.
 */
public class VoiceManager implements TextToSpeech.OnInitListener{

    private final Context miContext;
    private String[] buildings = new String[]{"Porteria carrera 27", "Auditorio Luis A. Calvo", "Administración", "INSED", "Teatro al aire libre Jose Antonio Galan", "Administración 2",
                                                     "Bienestar Universitario", "La perla", "Mantenimineto y Planta Física", "Ingeniería Mecánica", "Aula Máxima de Mecánica",
                                                     "Biblioteca", "Planta Telefónica", "Instituto de Lenguas", "Ingeniería Industrial", "Laboratorios Fisiologia y Morfologia Vegetal",
                                                     "Laboratorios Livianos", "Camilo Torres", "CENTIC", "CAPRUIS - FAVUIS", "Federico Mamitza Bayer", "Ingeniería Eléctrica y Electrónica",
                                                     "Laboratorios de Posgrados", "Ingeniería Química", "Aula Máxima de Física", "CICELPA / CEIAM", "Laboratorios de Alta Tensión",
                                                     "Laboratorios de Hidraulica", "Laboratorios de Diseño Industrial", "Planta de Aceros", "Jorge Bautista Vesga",
                                                     "Laboratorios Pesados", "Daniel Casas", "Residencias Estudiantiles", "Porteria carrera 30", "Kiosco Campos deportivos",
                                                     "Ciencias Humanas", "Jardinería", "Cancha de tenis", "Cancha 1 de marzo", "Cancha de Futbol sur", "Canchas múltiples",
                                                     "Coliseo UIS", "Diamante de softbol", "CENIVAM", "Cafeteria Don Cafeto", "Porteria carrera 25", "Caracterización de Materiales",
                                                     "Albañileria y bodegas"};

    private boolean isEngineInitialized = false;
    private TextToSpeech miTts;
    private Locale colombia;
    private SharedPreferences preferences;
    private MapView iMapView;
    private int lastTurnType = 0;


    /**
     *
     * @param pContext
     */
    public VoiceManager(Context pContext) {
        miContext = pContext;
        miTts = new TextToSpeech(miContext,this);
        colombia = new Locale("es", "COL");
        miTts.setLanguage(colombia);
        preferences = PreferenceManager.getDefaultSharedPreferences(miContext);
    }

    /**
     * Este método "habla" el texto específico, según la versión de android.
     * @param pText Texto a que se quiere hablar.
     */
    public void textToSpeech(String pText) {
        if(isEngineInitialized) {
            if(pText != null && !miTts.isSpeaking()) {
                if( Build.VERSION.SDK_INT < 21) {
                    miTts.speak(pText, TextToSpeech.QUEUE_ADD, null);
                }
                else {
                    miTts.speak(pText, TextToSpeech.QUEUE_ADD, null, "");
                }
            }
        }
    }
    public void textRecognizer(String sentence) {
        Log.v("Voice", "Sentencia: " + sentence);
        boolean to = true;
        Thesaurus thesaurus = new Thesaurus(sentence);
        sentence = thesaurus.getResult();
        Log.v("Voice", "Sentencia arreglada: " + sentence);
        String delimiters = "[ .,;?!¡¿\'\"\\[\\]]+";
        String[] words = sentence.split(delimiters);
        String where = null;
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
                                        if(words[words.length - 1].equalsIgnoreCase(places[places.length - 1]))
                                        {
                                            Log.v("Voice", "La última palabra coincide");
                                            iMapView.foundFocus(buildings[j], true);
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
            textToSpeech(sentence + ", No encontrado, Por favor intente nuevamente");
        }
    }

    public void navigation(int turnType, double degrees){
        if(lastTurnType != turnType && lastTurnType != 0) {
            String toSpeech = "";
            switch (turnType) {
                case R.mipmap.ahead_arrow:
                    toSpeech = "Continúa adelante";
                    break;
                case R.mipmap.left_arrow:
                    toSpeech = "Gira a la izquierda";
                    break;
                case R.mipmap.soft_left_arrow:
                    toSpeech = "Gira a la izquierda";
                    break;
                case R.mipmap.right_arrow:
                    toSpeech = "Gira a la derecha";
                    break;
                case R.mipmap.soft_right_arrow:
                    toSpeech = "Gira a la derecha";
                    break;
            }
            textToSpeech(toSpeech);
        }
        lastTurnType = turnType;

    }

    public void stop() {
        miTts.stop();
    }

    public void shutdown() {
        miTts.shutdown();
    }


    public void setMapView(MapView mapView) {
        iMapView = mapView;
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
