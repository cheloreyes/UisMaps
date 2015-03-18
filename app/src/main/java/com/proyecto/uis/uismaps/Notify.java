package com.proyecto.uis.uismaps;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

/** Notify es una pequeña clase encargada de notificar al usuario según convenga, ya sea
 *  utilizando la sintesis de voz o notificaciones emergentes.
 * Created by cheloreyes on 18/03/15.
 */
public class Notify implements UISMapsSettingsValues{
    private Context iContext;
    private SharedPreferences iPreferences;
    private String notification;
    private VoiceManager iVoiceManager;

    /**
     * Crea una notificación teniendo en cuenta el contexto en que se solicita.
     * @param context Contexto de la aplicación.
     */
    public Notify(Context context) {
        iContext = context;
        iPreferences = PreferenceManager.getDefaultSharedPreferences(iContext);
        iVoiceManager = new VoiceManager(iContext);
    }

    /**
     * Crea una nueva notificación. Según la UI habilitada notifica al usuario por sintesis de voz o texto.
     * @param sms Mensaje a notificar.
     */
    public void newNotification(String sms) {
        notification = sms;
        if(iPreferences.getBoolean(EYESIGHT_ASSISTANT, false)) {
            visualNotification();
        }
        else{
            noVisualNotification();
        }
    }

    /**
     * Despliega una pequeña ventana emergente con información sencilla a fin de informar al usuario rápidamente. la ventana desaparece
     * automaticamente despues de un breve tiempo.
     **/
    private void visualNotification() {
        Toast.makeText(iContext, notification, Toast.LENGTH_SHORT).show();
    }

    /**
     * Utiliza la sintesis de voz @VoiceManager para notificar al usuario.
     */
    private void noVisualNotification() {
        iVoiceManager.textToSpeech(notification);
    }

}
