package com.proyecto.uis.uismaps.Content;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.provider.Settings;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.View;
import android.view.Window;
import android.widget.ExpandableListView;
import android.widget.ImageView;

import com.proyecto.uis.uismaps.R;
import com.proyecto.uis.uismaps.categories.CategoriesBuilder;

/**
 * La clase Alerts realiza notificaciones más completas que pueden requerir de interacción con el usuario, además de informar y proporcionar una forma sencilla de entregar información.
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

    /**
     * Muestra un dialogo de tipo @AlertDialog a modo de informar al usuario, requiere confirmación por parte de este.
     * @param title Titulo del cuadro @AlertDialog
     * @param sms Mensaje de información
     * @param btnSms Texto en el botón.
     */
    public void showAlertDialog(String title, String sms, String btnSms ) {
        new AlertDialog.Builder(iContext)
                .setTitle(title)
                .setMessage(sms)
                .setPositiveButton(btnSms, null)
                .setCancelable(false)
                .show();
    }

    /**
     * Sobrepone una vista con señales y textos, con el propósito de explicar que realiza cada objeto de la vista.
     * @param tutorialIndex Cambia el tutorial a mostrar:
     *                      1 Para la el tutorial principal.
     *                      2 Para el tutorial del panel.
     */
    public void tutorialScreen(int tutorialIndex){

        final Dialog dialog = new Dialog(iContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.tutorial_screen);
        dialog.setCanceledOnTouchOutside(true);
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

    /**
     * Crea un dialogo con la vista de los espacios de la universidad organizados por categorías sea: Talleres, Laboratorios, Escuelas, Oficinas, etc.
     * @param category Lista de categorías ya instanceada para ser usada en @ExpandableListView.
     */
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
        dialog.show();
    }
    public void imageDialog(Bitmap image){
        final Dialog dialog = new Dialog(iContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.tutorial_screen);
        dialog.setCanceledOnTouchOutside(true);
        ImageView img = (ImageView)dialog.findViewById(R.id.tutorial_img);
        img.setImageBitmap(image);
        View masterView = dialog.findViewById(R.id.coach_mark_master_view);
        masterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

}
