package com.proyecto.uis.uismaps;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.proyecto.uis.uismaps.mapview.MapView;
import com.proyecto.uis.uismaps.mapview.NearbyPlace;

/**
 * La clase CompassCtrl controla los eventos de los sensores @TYPE_ACCELEROMETER y @TYPE_MAGNETIC_FIELD con el fin
 * de determinar el norte geográfico en base al magnetismo terrestre, asemejando el funcionamiento
 * de una brújula.
 * Usar @getCurrentDegree para obtener el ángulo con respecto al norte, siendo 0 para el norte geográfico.
 *
 * Se recomienda usar @pauseSensor para detener los oyentes del @Sensor para una mejor gestión de la batería.
 * Created by cheloreyes on 17/03/15.
 */
public class CompassCtrl implements SensorEventListener{

    // **********************
    // Fields
    // **********************

    private ImageView iImageView = null;
    private SensorManager iSensorManager;
    private Sensor iSensorAccelerometer;
    private Sensor iSensorMagnometer;
    private Context iContext;
    private float[] lastAccelerometer = new float[3];
    private float[] lastMagnometer = new float[3];
    private boolean accelrometerSet = false;
    private boolean magnometerSet = false;
    private float[] mR = new float[9];
    private float[] iOrientation = new float[3];

    private float currentDegree = 0f;
    private float oneFourtDegress = 0f;
    private MapView iMapView;
    private boolean findAngle = false;

    // **********************
    // Constructor
    // **********************

    /**
     * Crea una nueva instancia de brújula tomando de referencia el contexto utilizado en la
     * aplicación con el fin de acceder a sus servicios, en este caso al @sensor.
     * @param context
     */
    public CompassCtrl(Context context) {
        iContext = context;
        iSensorManager = (SensorManager) iContext.getSystemService(Context.SENSOR_SERVICE);
        iSensorAccelerometer = iSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        iSensorMagnometer = iSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        resumeSensors();
    }
    public CompassCtrl (Context context, ImageView imageView) {
        iImageView = imageView;
        iContext = context;
        iSensorManager = (SensorManager) iContext.getSystemService(Context.SENSOR_SERVICE);
        iSensorAccelerometer = iSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        iSensorMagnometer = iSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        resumeSensors();
    }

    // **********************
    // Methdos
    // **********************

    /**
     * Pone en marcha al oyente de los sensores. Se utiliza al iniciar la aplicación o al reanudarla.
     */
    public void resumeSensors() {
        iSensorManager.registerListener(this, iSensorAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        iSensorManager.registerListener(this, iSensorMagnometer, SensorManager.SENSOR_DELAY_GAME);
    }

    /**
     * Según donde apunte el dispositivo al momento de llamar este método, es posible saber respecto al usuario donde
     * está el edificio mas sercano encontrado por @MapView (si existe).
     * @param building edificio encontrado de tipo @NearbyPlace
     * @return String con la posición respecto al usuario.
     */
    public String whereIsTheBuilding(NearbyPlace building) {
        String iSeeing = "";
        Log.v("compass", "Norte a " + currentDegree);
        if(currentDegree > 315 || currentDegree < 45){
            switch (building.getWhere()){
                case NearbyPlace.SOUTH:
                    iSeeing = iContext.getString(R.string.ahead);
                    break;
                case NearbyPlace.NORTH:
                    iSeeing = iContext.getString(R.string.behind);
                    break;
                case NearbyPlace.WEST:
                    iSeeing = iContext.getString(R.string.righthand);
                    break;
                case NearbyPlace.EAST:
                    iSeeing = iContext.getString(R.string.lefthand);
                    break;
            }
        }
        else{
            if(currentDegree > 45 && currentDegree < 135){
                switch (building.getWhere()){
                    case NearbyPlace.SOUTH:
                        iSeeing = iContext.getString(R.string.lefthand);
                        break;
                    case NearbyPlace.NORTH:
                        iSeeing = iContext.getString(R.string.righthand);
                        break;
                    case NearbyPlace.WEST:
                        iSeeing = iContext.getString(R.string.ahead);
                        break;
                    case NearbyPlace.EAST:
                        iSeeing = iContext.getString(R.string.behind);
                        break;
                }
            }
            else
            {
                if(currentDegree > 135 && currentDegree < 225) {
                    switch (building.getWhere()){
                        case NearbyPlace.SOUTH:
                            iSeeing = iContext.getString(R.string.behind);
                            break;
                        case NearbyPlace.NORTH:
                            iSeeing = iContext.getString(R.string.ahead);
                            break;
                        case NearbyPlace.WEST:
                            iSeeing = iContext.getString(R.string.lefthand);
                            break;
                        case NearbyPlace.EAST:
                            iSeeing = iContext.getString(R.string.righthand);
                            break;
                    }
                }
                else {
                    if(currentDegree > 225 && currentDegree < 315){
                        switch (building.getWhere()){
                            case NearbyPlace.SOUTH:
                                iSeeing = iContext.getString(R.string.righthand);
                                break;
                            case NearbyPlace.NORTH:
                                iSeeing = iContext.getString(R.string.lefthand);
                                break;
                            case NearbyPlace.WEST:
                                iSeeing = iContext.getString(R.string.behind);
                                break;
                            case NearbyPlace.EAST:
                                iSeeing = iContext.getString(R.string.ahead);
                                break;
                        }
                    }
                }
            }
        }
        return iSeeing;
    }

    public int imLookingTo(){
        int toReturn = 0;
        if(currentDegree > 315 && currentDegree < 45) {
            toReturn = 0;
        }
        else {
            if(currentDegree > 45 && currentDegree < 135 ) {
                toReturn = 1;
            }
            else {
                if(currentDegree > 135 && currentDegree < 225) {
                    toReturn = 2;
                }
                else {
                    if(currentDegree > 225 && currentDegree < 315) {
                        toReturn = 3;
                    }
                }
            }
        }
        return toReturn;
    }

    /**
     * Pausa al oyente de los sensores. Se utiliza al salir de la aplicación.
     */
    public void pauseSensor() {
        iSensorManager.unregisterListener(this, iSensorAccelerometer);
        iSensorManager.unregisterListener(this, iSensorMagnometer);
    }

    /**
     * Necesario para determinar donde inicia la ruta con respecto al usuario.
     */
    public void whereIsTheRoute(){
        if(iMapView.isNavigating()) {
            int where = iMapView.getWhereIsThePoint();
            if(where != 0 ){
                iMapView.followRoute(where);
                iMapView.showNavigation();
            }
        }
    }

    // **********************
    // Getter and Setter
    // **********************


    public void setMapView(MapView mapView) {
        iMapView = mapView;
    }

    public void findAngle(boolean status) {
        findAngle = status;
    }

    public  boolean isFindAngle() {
        return findAngle;
    }

    // **********************
    // Methods from super class
    // **********************

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor == iSensorAccelerometer) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.length);
            accelrometerSet = true;
        }
        else {
            if(event.sensor == iSensorMagnometer) {
                System.arraycopy(event.values, 0, lastMagnometer, 0, event.values.length);
                magnometerSet = true;
            }
        }
        if(accelrometerSet && magnometerSet) {
            SensorManager.getRotationMatrix(mR, null, lastAccelerometer, lastMagnometer);
            SensorManager.getOrientation(mR, iOrientation);
            float radiants = iOrientation[0];
            float degress = (float)(Math.toDegrees(radiants) + 360) % 360;
            if(findAngle){
                if(Math.abs(degress - oneFourtDegress) > 45){
                    whereIsTheRoute();
                    oneFourtDegress = degress;
                }
            }
            if(Math.abs(degress - currentDegree) > 10){
                if(iImageView != null){
                    RotateAnimation ra = new RotateAnimation(Math.round(-currentDegree), Math.round(-degress), Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    ra.setDuration(250);
                    ra.setFillAfter(true);
                    iImageView.startAnimation(ra);
                }
                currentDegree = degress;
            }


        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
