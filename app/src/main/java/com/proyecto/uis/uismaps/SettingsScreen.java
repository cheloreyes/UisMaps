package com.proyecto.uis.uismaps;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by cheloreyes on 7/03/15.
 * Muestra una lista de objetos de diferentes preferencias, Que son guardadas
 * automáticamente en @SharedPreferences según el usuario especifique.
 * Estas preferencias se pueden recuperar usando @getDefaultSharedPreferences desde
 * este mismo paquete.
 */
public class SettingsScreen extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_screen);
    }
}
