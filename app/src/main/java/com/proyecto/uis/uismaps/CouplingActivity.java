package com.proyecto.uis.uismaps;

import android.app.Activity;
import android.os.Bundle;

/**
 * Esta clase pretende ser un Activity para acoplar los fragments necesarios para:
 * Ajustes @SettingsScreen,
 */
public class CouplingActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsScreen()).commit();
    }
}
