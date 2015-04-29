package com.proyecto.uis.uismaps.Content;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.widget.ExpandableListView;
import android.widget.ImageView;

import com.proyecto.uis.uismaps.R;
import com.proyecto.uis.uismaps.categories.CategoriesBuilder;

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
    public void tutorialScreen(int tutorialIndex){

        final Dialog dialog = new Dialog(iContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.tutorial_screen);
        dialog.setCanceledOnTouchOutside(true);
        //for dismissing anywhere you touch
        if(tutorialIndex == 1) {
            ImageView img = (ImageView)dialog.findViewById(R.id.tutorial_img);
            img.setImageResource(R.drawable.tuto_dos_wite);
        }
        else{
            ImageView img = (ImageView)dialog.findViewById(R.id.tutorial_img);
            img.setImageResource(R.drawable.tuto_uno_wite);
        }
        View masterView = dialog.findViewById(R.id.coach_mark_master_view);
        masterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    public void showCategories(final CategoriesBuilder category){

        final Dialog dialog = new Dialog(iContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setTitle("Categorías");
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dialog.setContentView(R.layout.categories);
        dialog.setCanceledOnTouchOutside(true);
        ExpandableListView listCategories = (ExpandableListView) dialog.findViewById(R.id.list_categories);
        listCategories.setAdapter(category.getAdapter());
        listCategories.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                category.childClickAction(groupPosition, childPosition, dialog);
                return false;
            }
        });
        //for dismissing anywhere you touch
        dialog.show();
    }
}
