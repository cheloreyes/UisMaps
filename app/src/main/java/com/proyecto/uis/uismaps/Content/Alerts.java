package com.proyecto.uis.uismaps.Content;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;

import com.proyecto.uis.uismaps.R;

/**
 * La clase Alerts realiza notificaciones mas completas que pueden requerir de interacción con el usuario.
 * Created by cheloreyes on 2/03/15.
 */
public class Alerts {

    private Context iContext;
    public Alerts(Context context) {
        iContext = context;
    }

    /**
     * En el caso que el dispositivo tenga dehabilitado el servicio de GPS, se despliega un @AlertDialog con información
     * y opcion de acceder a activar este servicio.
     */
    public void configLocation() {
        new AlertDialog.Builder(iContext)
                .setTitle(iContext.getString(R.string.location_unable))
                .setMessage(iContext.getString(R.string.location_unable_message))
                .setPositiveButton(iContext.getText(R.string.config_location),
                                          new DialogInterface.OnClickListener() {
                                              @Override
                                              public void onClick(DialogInterface dialog, int which) {
                                                  Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                                  iContext.startActivity(i);
                                              }
                                          })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /**
     * Muestra un cuadro de dialogo mientras se conecta a los satelites GPS.
     * @return
     */
    public ProgressDialog gpsProgressDialog() {
        ProgressDialog progressDialog = new ProgressDialog(iContext);
        progressDialog.setTitle(iContext.getString(R.string.gps_device_on));
        progressDialog.setMessage(iContext.getString(R.string.conecting));
        return progressDialog;
    }

    public void cantShowRoute() {
        new AlertDialog.Builder(iContext)
                .setTitle("Error al generar ruta.")
                .setMessage("Ha ocurrido un problema al generar la ruta, por favor intente nuevamente.")
                .setPositiveButton("OK", null)
                .setCancelable(false)
                .show();
    }

    public void showAlertDialog(String title, String sms, String btnSms ) {
        new AlertDialog.Builder(iContext)
                .setTitle(title)
                .setMessage(sms)
                .setPositiveButton(btnSms, null)
                .setCancelable(false)
                .show();
    }
}
