package com.proyecto.uis.uismaps;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.proyecto.uis.uismaps.Content.Alerts;
import com.proyecto.uis.uismaps.mapview.MapView;

/**
 * Controla al oyente del servicio GPS, siendo posible indicarle cuando iniciar al oyente o cuando detenerlo.
 * Con esto es posible determinar la posicion relativa del usuario en el mapa que además posibilita las función de navegación.
 * Created by CheloReyes on 1/04/15.
 */
public class LocationCtrl implements LocationListener {

    private static final int MIN_TIME = 5000;
    private static final int MIN_DISTANCE = 1;
    private final MapView iMapView;

    private boolean isGPSon = false;

    private final Context iContext;

    private ProgressDialog progressDialog;
    private boolean hasAccurancy = false;
    private boolean iWantNavigate = false;

    private boolean isNavigateStarted = false;

    public LocationCtrl(Context context, MapView mapView) {
        iContext = context;
        iMapView = mapView;
    }
    /**
     * Enciende o apaga el servicio GPS.
     * Inicia @LocationManager con proveedor @GPS_PROVIDER con tiempo de actualización @MIN_TIME y distancia @MIN_DISTANCE.
     * <p/>
     * La navegación Gps consume muchos recursos y no es necesario si el usuario está fuera del área o solo quiere hacer una consulta.
     *
     * @param status: Hace referencia al estado deseado del servicio. True para encendido, false para apagado.
     */
    public void switchLocation(boolean status) {
        Alerts alertsDialog = new Alerts(iContext);
        Notify notify = new Notify(iContext);
        progressDialog = new ProgressDialog(iContext);
        iMapView.switchCompass(true);
        LocationManager iLocationManager;
        if (!isGPSon && status) {
            iLocationManager = (LocationManager) iContext.getSystemService(Context.LOCATION_SERVICE);
            iLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
            if (iLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                progressDialog = alertsDialog.gpsProgressDialog();
                progressDialog.show();
                isGPSon = true;
            } else {
                PackageManager pm = iContext.getPackageManager();
                boolean hasGPS = pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
                if (hasGPS) {
                    alertsDialog.configLocation();
                } else {
                    notify.visualNotification(iContext.getString(R.string.no_gps));
                }
            }
        } else {
            if (isGPSon && !status) {
                iLocationManager = (LocationManager) iContext.getSystemService(Context.LOCATION_SERVICE);
                iLocationManager.removeUpdates(this);
                isGPSon = false;
                hasAccurancy = false;
                iMapView.setAccurancy(false);
                iMapView.switchCompass(false);

            }
        }
    }
    /**
     * Está el Gps iniciado?
     * @return Estado del Gps.
     */
    public boolean isGPSon() {
        return isGPSon;
    }

    /**
     * Se ha iniciado la navegación?
     * @return Estado de la navegación.
     */
    public boolean isNavigateStarted(){
        return isNavigateStarted;
    }

    public boolean isAccurancy() {
        return hasAccurancy;
    }

    /**
     * Establece que se quiere para intentar nuevamente iniciar la navegación si no se ha actualizado los datos del GPS.
     * @param iWantNavigate
     */
    public void setiWantNavigate(boolean iWantNavigate) {
        this.iWantNavigate = iWantNavigate;
    }

    /**
     * Establece el estado de la navegación.
     * @param isNavigateStarted estado de la navegación.
     */
    public void setNavigateStarted(boolean isNavigateStarted) {
        this.isNavigateStarted = isNavigateStarted;
    }

    @Override
    public void onLocationChanged(android.location.Location location) {
        if (location.hasAccuracy() && !hasAccurancy) {
            progressDialog.hide();
            progressDialog.dismiss();
            hasAccurancy = location.hasAccuracy();
            iMapView.setAccurancy(hasAccurancy);
        }
        iMapView.setICurrentLocation(location.getLongitude(), location.getLatitude());
        //miCurrentLon = location.getLongitude() + 0.00000895;
        //miCurrentLat = location.getLatitude() - 0.00004458;
        if (iWantNavigate) {
            iMapView.switchNavigation(true);
            iWantNavigate = false;
        }
        if (isNavigateStarted) {
            iMapView.navigate(location.getLongitude(), location.getLatitude());
            //iMapView.displayCurrentLocation();
        } else {
            //locateMe();
            iMapView.displayCurrentLocation();
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        iMapView.changeStatusIcon(status,provider);
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
