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
                                                     "Laboratorios de Posgrados", "Ingeniería Quimica", "Aula Máxima de Ciencias", "CICELPA / CEIAM", "Laboratorios de Alta Tensión",
                                                     "Laboratorios de Hidraulica", "Laboratorios de Diseño Industrial", "Planta de Aceros", "Jorge Bautista Vesga",
                                                     "Laboratorios Pesados", "Daniel Casas", "Residencias Estudiantiles", "Porteria carrera 30", "Kiosco Campos deportivos",
                                                     "Ciencias Humanas", "Jardinería", "Cancha de tenis", "Cancha 1 de marzo", "Cancha de Futbol sur", "Canchas múltiples",
                                                     "Coliseo UIS", "Diamante de softbol", "CENIVAM", "Cafeteria Don Cafeto", "Porteria carrera 25", "Caracterización de Materiales",
                                                     "Soccer Hot Dogs", "parqueaderos subterraneo plazoleta", "Albañileria y bodegas"};
    private String[] listFix = { "de", "a","al", "yo", "tu", "él", "ella", "ello",
                                    "ellos", "ellas", "nosotros", "nosotras", "ustedes", "vosotros",
                                    "vosotras", "mi", "conmigo", "ti", "contigo", "si", "quiero",
                                    "me", "se", "te", "lo", "la", "le", "nos", "os", "se", "y", "e",
                                    "ni", "mas", "pero", "sino", "o", "u", "porque", "pues", "si",
                                    "ir", "como", "tan", "ante", "bajo", "con", "contra", "desde",
                                    "entre", "hacia", "hasta", "para", "por", "según", "sin", "sobre",
                                    "tras", "mediante", "durante", "llegar" };
    private boolean isEngineInitialized = false;
    private TextToSpeech miTts;
    private Locale colombia;
    private SharedPreferences preferences;
    private MapView imaMapView;


    /**
     *
     * @param pContext
     */
    public VoiceManager(Context pContext, MapView mapView) {
        miContext = pContext;
        miTts = new TextToSpeech(miContext,this);
        colombia = new Locale("es", "COL");
        miTts.setLanguage(colombia);
        preferences = PreferenceManager.getDefaultSharedPreferences(miContext);
        imaMapView = mapView;
    }

    /**
     * Este método "habla" el texto específico, según la versión de android.
     * @param pText Texto a que se quiere hablar.
     */
    public void textToSpeech(String pText) {
        if(isEngineInitialized) {
            if(pText != null) {
                if( Build.VERSION.SDK_INT < 21) {
                    miTts.speak(pText, TextToSpeech.QUEUE_ADD, null);
                }
                else {
                    miTts.speak(pText, TextToSpeech.QUEUE_ADD, null, "");
                }
            }
            while(miTts.isSpeaking()){

            }
        }
    }
    public void textRecognizer(String sentence) {
        Log.v("Voice", "Sentencia: " + sentence);
        boolean to = true;
        sentence = fixSentence(sentence);
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
                                    if(i < words.length - 1){
                                        to = true;
                                    }
                                    else{
                                        Log.v("Voice", "Coincide: "+buildings[j]);
                                        imaMapView.foundFocus(buildings[j]);
                                        to = false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }
    private String fixSentence(String sentence) {
        for (int i = 0; i < listFix.length; i++)
            sentence = sentence.replaceAll("\\b" + listFix[i] + "\\b", "");

        return sentence;
    }

    public void stop() {
        miTts.stop();
    }

    public void shutdown() {
        miTts.shutdown();
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
