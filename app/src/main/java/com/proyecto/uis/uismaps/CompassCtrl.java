package com.proyecto.uis.uismaps;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

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
     * Pausa al oyente de los sensores. Se utiliza al salir de la aplicación.
     */
    public void pauseSensor() {
        iSensorManager.unregisterListener(this, iSensorAccelerometer);
        iSensorManager.unregisterListener(this, iSensorMagnometer);
    }

    // **********************
    // Getter and Setter
    // **********************

    /**
     * Obtiene el ángulo actual con respecto al norte en grados centigrados. 0 para el norte.
     * @return
     */
    public float getCurrentDegree() {
        return currentDegree;
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
            if(Math.abs(currentDegree + degress) > 85 && Math.abs(currentDegree + degress) < 95) {
                currentDegree = -degress;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
