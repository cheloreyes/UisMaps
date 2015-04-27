package com.proyecto.uis.uismaps.Content;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.proyecto.uis.uismaps.Constants;
import com.proyecto.uis.uismaps.R;

/**
 * Esta clase soporta a @Settings para ser una actividad con la que pueda interactuar el usuario.
 */
public class SettingsActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new Settings()).commit();
    }

    /**
     * Esta clase interna gestiona la lista de los diferentes ajustes que puede modificar el usuario, los cuales son guardadas
     * automáticamente en @SharedPreferences para ser accesadas por las diferentes clases del paquete de aplicación.
     */
    public static class Settings extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, Constants {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_screen);
            Preference aboutUs = (Preference) getPreferenceManager().findPreference("about_us");
            if(aboutUs != null) {
                aboutUs.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        lunchWebpage();
                        return true;
                    }
                });
            }
        }

        /**
         * Llamado cuando las preferencias son cambiadas, agregadas, o eliminadas.
         * <p>Este llamado se realiza en el hilo principal.
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
        public void lunchWebpage() {
            String url = "http://www.uismaps.com/#AcercaUISMaps";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }

    }
}
