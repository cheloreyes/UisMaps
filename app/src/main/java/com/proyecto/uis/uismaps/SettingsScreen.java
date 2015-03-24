package com.proyecto.uis.uismaps;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by cheloreyes on 7/03/15.
 * Muestra una lista de objetos de diferentes preferencias, Que son guardadas
 * automáticamente en @SharedPreferences según el usuario especifique.
 * Estas preferencias se pueden recuperar usando @getDefaultSharedPreferences desde
 * este mismo paquete.
 */
public class SettingsScreen extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, Constants {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_screen);
    }

    /**
     * Called when a shared preference is changed, added, or removed. This
     * may be called even if a preference is set to its existing value.
     * <p/>
     * <p>This callback will be run on your main thread.
     *
     * @param sharedPreferences The {@link android.content.SharedPreferences} that received
     *                          the change.
     * @param key               The key of the preference that was changed, added, or
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(EYESIGHT_ASSISTANT)) {


        }
    }
    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
