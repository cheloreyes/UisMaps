package com.proyecto.uis.uismaps;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;

/**
 * Created by cheloreyes on 2/03/15.
 */
public class Alerts {
    Context miContexto;
    public Alerts() {
    }
    public void configLocation(final Context pContext) {
        new AlertDialog.Builder(pContext)
                .setTitle(pContext.getString(R.string.location_unable))
                .setMessage(pContext.getString(R.string.location_unable_message))
                .setPositiveButton(pContext.getText(R.string.config_location),
                                          new DialogInterface.OnClickListener() {
                                              @Override
                                              public void onClick(DialogInterface dialog, int which) {
                                                  Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                                  pContext.startActivity(i);
                                              }
                                          })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
    public ProgressDialog progressDialog(Context contexto) {
        ProgressDialog progressDialog = new ProgressDialog(contexto);
        progressDialog.setTitle(contexto.getString(R.string.gps_device_on));
        progressDialog.setMessage(contexto.getString(R.string.conecting));
        return progressDialog;
    }

}
