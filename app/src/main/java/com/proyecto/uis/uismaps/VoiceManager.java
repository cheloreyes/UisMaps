package com.proyecto.uis.uismaps;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

/**
 * Created by cheloreyes on 9/03/15.
 */
public class VoiceManager implements TextToSpeech.OnInitListener{

    private final Context miContext;
    private boolean isEngineInitialized = false;
    private TextToSpeech miTts;
    private Locale colombia;

    public VoiceManager(Context pContext) {
        miContext = pContext;
        miTts = new TextToSpeech(miContext,this);
        colombia = new Locale("es", "COL");

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
            }
        }
        else {
            Log.e("error", "No se pudo inicializar");
        }
    }
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
        }
    }
    public void stop() {
        miTts.stop();
    }
    public void shutdown() {
        miTts.shutdown();
    }
}
