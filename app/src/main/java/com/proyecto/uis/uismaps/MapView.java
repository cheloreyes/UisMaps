package com.proyecto.uis.uismaps;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewStub;
import android.widget.Toast;

import com.cartotype.Framework;
import com.cartotype.MapObject;
import com.cartotype.RouteProfile;
import com.cartotype.Turn;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Created by cheloreyes on 21-01-15.
 * Implementa la vista del mapa CartoType
 */
public class MapView extends View implements View.OnTouchListener, LocationListener,
        ScaleGestureDetector.OnScaleGestureListener {
    // **********************
    // Constants
    // **********************

    private static final double MAX_XA = -73.12329138853613;
    private static final double MAX_XB = -73.1155327517166;
    private static final double MAX_YA = 7.143398173714375;
    private static final double MAX_YB = 7.1382743178385315;
    private static final int MIN_DISTANCE = 3;
    private static final int MIN_TIME = 2000;
    private static final String ACT_OPTIONS = "UisMapPreferencias";
    private static final int DEFAULT_PROFILE = RouteProfile.WALKING_PROFILE;
    private static final int DPI_MIN = 160;
    private static final int GRAY_COLOR = 0xFFCCCCCC;
    private static final int ID_CURRENT_LOCATION = 1;
    private static final int ID_ROUTE_END = 3;
    private static final int ID_ROUTE_START = 2;
    private static final int ID_SELECTED_POINT = 1;
    private static final String MAP_ESCALE = "last_scale";
    private static final String MAP_GROUND = "last_terrain";
    private static final String MAP_LAT = "last_lat";
    private static final String MAP_LONG = "last_lon";
    private static final String MAP_NAME = "last_map";
    private static final String MAP_PERSPECTIVE = "last_perspective";
    private static final String MAP_ROTATION = "last_rotation";
    private static final String MAP_UNIT = "display_units";
    private static final int PRED_SCALE = 2500000;
    private static final String TAG = "MapView";
    private static final String uisMapsFolder = Environment.getExternalStorageDirectory().getPath() + "/UISMaps";
    private static final String miDefaultMap = uisMapsFolder + "/mapa/mapa.ctm1";
    private static final String FILE_STYLE = uisMapsFolder + "/estilos/osm-style.xml";
    private static final String FILE_FONT = uisMapsFolder + "/fuentes/DejaVuSans.ttf";

    // **********************
    // Fields
    // **********************

    private final Context miContext;
    private int dpiScreen;
    private String iNavTimeLeft;
    private boolean iWantNavigate = false;
    private double interestingPointLat;
    private double interestingPointLong;
    private boolean isGPSon = false;
    private boolean isNavigateStarted = false;
    private boolean isNavigating = false;
    private Bitmap miBitmap;
    private byte[] miBitmapData;
    private ByteBuffer miBuffer;
    private double miCurrentLat;
    private double miCurrentLon;
    private View miDetailsView;
    private boolean miDisplayTerrain;
    private String miDistanceUnits;
    private Turn miFirstTurn;
    private double miFirstTurnDistance;
    private Framework miFramework;
    private String miMapFile;
    private Matrix miMatrix;
    private int miNavigationState;
    private boolean miPerspective;
    private float miPrevScaleFocusX;
    private float miPrevScaleFocusY;
    private RouteProfile miRouteProfile;
    private float miScale = 1;
    private ScaleGestureDetector miScaleGestureDetector;
    private Turn miSecondTurn;
    private double miSecondTurnDistance;
    private boolean miSimulatingNavigation;
    private float miStartTouchPointX;
    private float miStartTouchPointY;
    private int miTouchCount;
    private int miTurnAction;
    private float miXOffset;
    private float miYOffset;
    private ProgressDialog progressDialog;
    private double routeEndLat;
    private double routeEndLon;

    // **********************
    // Constructors
    // **********************

    /**
     * Constructor principal
     * @param iContext
     * @param iDpi densidad de pixeles de la pantalla del dispositivo.
     */

    public MapView(Context iContext, int iDpi) {
        super(iContext);
        setOnTouchListener(this);
        dpiScreen = iDpi;
        miContext = iContext;
    }

    /**
     * Constructor estandar
     * @param iContexto
     */
    public MapView(Context iContexto) {
        super(iContexto);
        setOnTouchListener(this);
        dpiScreen = DPI_MIN;
        miContext = iContexto;
    }

    // **********************
    // Methods from SuperClass
    // **********************

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (miScaleGestureDetector == null || miFramework == null) {
            return false;
        }
        miScaleGestureDetector.onTouchEvent(event);
        if (miScaleGestureDetector.isInProgress()) {
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (miTouchCount == 0) {
                    miTouchCount = 1;
                    miStartTouchPointX = event.getX();
                    miStartTouchPointY = event.getY();
                    return true;
                }
            case MotionEvent.ACTION_MOVE:
                if (miTouchCount == 1) {
                    miXOffset = event.getX() - miStartTouchPointX;
                    miYOffset = event.getY() - miStartTouchPointY;
                    invalidate();
                }
                return true;
            case MotionEvent.ACTION_UP:
                if (miTouchCount != 1) {
                    break;
                }
                miXOffset = event.getX() - miStartTouchPointX;
                miYOffset = event.getY() - miStartTouchPointY;

                if (miXOffset > -13 && miXOffset < 13 && miYOffset > -13 && miYOffset < 13) {
                    double arrayOfDouble[] = new double[2];
                    arrayOfDouble[0] = event.getX();
                    arrayOfDouble[1] = event.getY();
                    miFramework.convertCoords(arrayOfDouble, Framework.SCREEN_COORDS, Framework.DEGREE_COORDS);
                    setSelectedPoint(arrayOfDouble[0], arrayOfDouble[1]);
                    miTouchCount = 0;
                    if (miSimulatingNavigation) {
                        //  setSimulatedNavLocation(arrayOfDouble[0], arrayOfDouble[1]);
                    }
                } else {
                    miTouchCount = 0;
                    miFramework.pan((int) Math.floor(-miXOffset + 0.5), (int) Math.floor(-miYOffset + 0.5));
                    miXOffset = 0;
                    miYOffset = 0;
                    getMap();
                    invalidate();
                }
                return true;
        }
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
        if (miBitmap == null) {
            init();
            miMatrix = canvas.getMatrix();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && canvas.isHardwareAccelerated()) {
                int[] arrayOfInt = new int[2];
                getLocationOnScreen(arrayOfInt);
                miMatrix.preTranslate(arrayOfInt[0], arrayOfInt[1]);
            }
        }
        if (miScale != 1.0 || miXOffset != 0 || miYOffset != 0) {
            Matrix m = canvas.getMatrix();
            m.set(miMatrix);
            m.preTranslate(getWidth() / 2 + miXOffset, getHeight() / 2 + miYOffset);
            m.preScale(miScale, miScale);
            m.preTranslate(-getWidth() / 2, -getHeight() / 2);
            canvas.setMatrix(m);
        } else {
            canvas.setMatrix(miMatrix);
        }

        if (miBitmap != null) {
            canvas.drawBitmap(miBitmap, 0.0F, 0, null);
        }
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        miScaleGestureDetector = detector;
        miScale *= miScaleGestureDetector.getScaleFactor();
        float x = miScaleGestureDetector.getFocusX();
        float y = miScaleGestureDetector.getFocusY();
        miXOffset += x - miPrevScaleFocusX;
        miYOffset += y - miPrevScaleFocusY;
        miPrevScaleFocusX = x;
        miPrevScaleFocusY = y;
        invalidate();
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        miScaleGestureDetector = detector;
        miPrevScaleFocusX = miScaleGestureDetector.getFocusX();
        miPrevScaleFocusY = miScaleGestureDetector.getFocusY();
        miTouchCount = 2;
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        miScaleGestureDetector = detector;
        if (miScale != 1) {
            miFramework.zoom(miScale);
        }
        miScale = 1;
        if (miXOffset != 0 || miYOffset != 0) {
            miFramework.pan((int) Math.floor(-miXOffset + 0.5),
                    (int) Math.floor(-miYOffset + 0.5));
        }
        miXOffset = 0;
        miYOffset = 0;

        miTouchCount = 0;
        getMap();
        invalidate();
    }

    @Override
    public void onLocationChanged(Location cLocation) {
        if(cLocation.hasAccuracy()){
            progressDialog.hide();
        }
        Log.d(TAG,"Location update "+ miCurrentLon + ", " + miCurrentLat);
        miCurrentLon = cLocation.getLongitude();
        miCurrentLat = cLocation.getLatitude();
        displayCurrentLocation();
        if(iWantNavigate) {
            navigateStart();
            iWantNavigate = false;
        }
        if(isNavigateStarted) {
            int validity = Framework.POSITION_VALID | Framework.TIME_VALID;
            if(cLocation.hasSpeed()) {
                validity |= Framework.SPEED_VALID;
            }
            if(cLocation.hasBearing()) {
                validity |= Framework.COURSE_VALID;
            }
            if(cLocation.hasAltitude()) {
                validity |= Framework.HEIGHT_VALID;
            }
            double time = (cLocation.getTime()) / 1000;
            double speed = cLocation.getSpeed() * 3.6;
            setUpNavigation(validity, time , miCurrentLon, miCurrentLat, speed, cLocation.getBearing(), cLocation.getAltitude());
            miFramework.setViewCenterLatLong(miCurrentLon,miCurrentLat);
            displayCurrentLocation();
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    // **********************
    // Methods
    // **********************

    /**
     * Inicializa la vista del mapa. Se necesita cuando se inicia la app
     */
    public void init()
    {
        //miRouteProfile = new RouteProfile(1);
        setKeepScreenOn(true);
        miScaleGestureDetector = new ScaleGestureDetector(miContext,this);

        //Nos asegurarnos que existan los archivos y carpetas.
        folderCheck();
        //Color de fondo para las zonas que no cubre el mapa.
        setBackgroundColor(GRAY_COLOR);

        int width = getWidth();
        int height = getHeight();
//TODO restoreDefaultMap() puede que no se necesite, ya que es para cuando usamos diferentes mapas
        restoreDefaultMap();

        //Comprueba que exista el archivo del mapa antes de iniciar el API
        File f = new File(miMapFile);
        if(!f.exists()) {
            //Se asigna el mapa predeterminado
            Log.e(TAG,"Estableciendo mapa"+ miDefaultMap);
            miMapFile = miDefaultMap;
//TODO averiguar para que sirve deleteState()
            deleteState();
        }
        Log.v(TAG, "Creando nuevo framework");
        //Se crea la instancia del framework
        miFramework = new Framework(miMapFile, FILE_STYLE, FILE_FONT, width, height);
        miFramework.setResolutionDpi(dpiScreen);

        //Para las funciones de navegacion con un factor de correción de 5 metros y 15 seg.
        miFramework.setNavigatorMinimumFixDistance(5);
        miFramework.setNavigatorTimeTolerance(15);

        //Aplica los estados de Escala, rotacion, perspectiva, latitud etc.. de la ultima sesion.
        //restoreState();

        miBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        getMap();
        invalidate();
    }

    /**
     * getMap() Dibuja el mapa.
     */
    private void getMap() {
        miBitmap = miFramework.getMap();
    }

    /**
     * Obtiene los estados guardados de la última sesion y los restaura para continuar desde ahí.
     * Si no existen estos valores, asigna los predetermiandos.
     */

    private void restoreState() {
        SharedPreferences preferences = miContext.getSharedPreferences(ACT_OPTIONS, Context.MODE_PRIVATE);
        int map_scale = 0;
        double map_rotation = 0;
        double map_lat = 0.0;
        double map_lon = 0.0;
        //Obtiene los ultimos estados (posición, escala, rotacion etc.).
        try {
            miDistanceUnits = preferences.getString(MAP_UNIT, "metric");
            miDisplayTerrain = preferences.getBoolean(MAP_GROUND, false);
            map_scale = preferences.getInt(MAP_ESCALE, 0);
            map_rotation = Double.longBitsToDouble(preferences.getLong(MAP_ROTATION, 0));
            miPerspective = preferences.getBoolean(MAP_PERSPECTIVE, false);
            map_lat = Double.longBitsToDouble(preferences.getLong(MAP_LAT, 0));
            map_lon = Double.longBitsToDouble(preferences.getLong(MAP_LONG, 0));
        } catch (ClassCastException e) {
            Log.e(TAG, "No se pudo restaurar los valores anteriores, se utilizan los predeterminados");
            e.printStackTrace();
        }
        Log.v(TAG, "restoreState() lat = " + map_lat + "Lon = " + map_lon + "Escala = " + map_scale + "Rotacion = " + map_rotation);
        //Establece la escala.
        if (map_scale != 0) {
            Log.v(TAG, "MapSetUp() con ultima escala conocida: " + map_scale);
            miFramework.setScale(map_scale);
        } else {
            Log.v(TAG, "MapSetUp() con escala predeterminada:" + PRED_SCALE);
            miFramework.setScale(PRED_SCALE);
        }
        //TODO verificar si es necesario enableLayer
        //miFramework.enableLayer("terrain-height-feet", true);
        //miFramework.enableLayer("terrain-shadow", true);
        if (miPerspective) {
            miFramework.setPerspective(true);
        }
        if (map_rotation != 0) {
            Log.v(TAG, "MapSetUp() con ultima rotacion conocida:" + map_rotation);
            miFramework.setRotation(map_rotation);
        }
        if (map_lat != 0 && map_lon != 0) {
            Log.v(TAG, "MapSetUp() con centro en: Lat= " + map_lat + "Lon= " + map_lon);
            miFramework.setViewCenterLatLong(map_lon, map_lat);
        }
    }

    /**
     * deleteState() es usado para eliminar la escala, rotación, latitud y longitud guardadas.
     */
    private void deleteState() {
        SharedPreferences preferences = miContext.getSharedPreferences(ACT_OPTIONS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(MAP_ESCALE);
        editor.remove(MAP_LONG);
        editor.remove(MAP_LAT);
        editor.remove(MAP_ROTATION);
        editor.commit();
    }

    private void restoreDefaultMap() {
        SharedPreferences prefs = miContext.getSharedPreferences(ACT_OPTIONS, Context.MODE_PRIVATE);
        miMapFile = prefs.getString(MAP_NAME, miDefaultMap);
    }

    /**
     * Comprueba que existan las carpetas y archivos en la memoria y los adiciona si no estan.
     * Carpetas creadas por software.
     * Archivos copiados de la carpeta "assets".
     */
    private void folderCheck()
    {
        //Existe la carpeta principal?
        File dir = new File(uisMapsFolder);
        if(!dir.exists()) {
            dir.mkdirs();
        }
        //Existe la carpeta Log?
        dir = new File(uisMapsFolder + "/log");
        if(!dir.exists()) {
            dir.mkdirs();
        }
        //Existe el archivo mapa?
        File f = new File(miDefaultMap);
        if(!f.exists()) {
            assetCopy("mapa");
            Log.i(TAG, "Mapa no encontrado, se copia de asset");
        }
        //Existe la hoja de estilo?
        f = new File(FILE_STYLE);
        if(!f.exists()) {
            assetCopy("estilos");
            Log.i(TAG,"Hoja de estilos no encontrada, se compia de asset");
        }
        f = new File(FILE_FONT);
        if (!f.exists()) {
            assetCopy("fuentes");
            Log.i(TAG, "archivo de fuentes no encontrado, se compia de asset");

        }
    }

    private void assetCopy(String assetItem)
    {
        File sdCardDir = new File(uisMapsFolder + "/" + assetItem);
        if(!sdCardDir.exists()) {
            sdCardDir.mkdirs();
        }
        AssetManager assetManager = getResources().getAssets();
        String[] files = null;

        try {
            files = assetManager.list(assetItem);
        } catch (IOException e) {
            Log.e("Error al leer carpeta assets", "");
            e.printStackTrace();
        }

        for(int i = 0; i<files.length; i++) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(assetItem + "/" + files[i]);
                out = new FileOutputStream(uisMapsFolder + "/" + assetItem + "/" + files[i]);
                copyFile(in, out);
                in.close();
                in=null;
                out.flush();
                out.close();
                out=null;
            } catch (IOException e) {
                Log.e("Error al copiar de asset", "");
                e.printStackTrace();
            }
        }

    }

    /**
     * Usado por assetCopy()
     * @param in
     * @param out
     * @throws IOException
     */
    private void copyFile(InputStream in, OutputStream out) throws IOException{
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer,0,read);
        }
    }

    /**
     * Agrega el punto seleccionado a la pantalla y lo guarda como latitud y longitud
     * para ser accedidos por otros métodos.
     *
     * @param aLongitude Coordenada X del evento.
     * @param aLatitude  Coordenarda Y del evento.
     */
    public void setSelectedPoint(double aLongitude, double aLatitude) {
        interestingPointLong = aLongitude;
        interestingPointLat = aLatitude;
        miFramework.deleteObjects(ID_SELECTED_POINT, ID_SELECTED_POINT);
        miFramework.addPointObject("pushpin", interestingPointLong, interestingPointLat, Framework.DEGREE_COORDS,
                                          "", 0, ID_SELECTED_POINT, 0);
        Log.v(TAG, "pone marcador");
        //notifyMessage(getNearbyPlaces());
        notifyMessage(interestingPointLong +", "+ interestingPointLat);
        getMap();
        invalidate();
    }

    /**
     * Retira los puntos de la pantalla.
     */
    private void deleteSelectedPoint() {
        miFramework.deleteObjects(ID_SELECTED_POINT, ID_SELECTED_POINT);
        interestingPointLat = 0.0;
        interestingPointLong = 0.0;

    }

    public void setUpNavigation(int aValidity, double aTime, double aLong,
                                double aLat, double aSpeed, double aBearing, double aHeight) {
        miFramework.navigate(aValidity, aTime, aLong, aLat, aSpeed, aBearing, aHeight);
        miNavigationState = miFramework.getNavigationState();
        double distance = miFramework.distanceToDestination();
        double seconds = miFramework.estimatedTimeToDestination();
        double hours = (int) (seconds / 3600);
        seconds -= hours * 3600;
        double minutes = (int) (seconds * 60);
        seconds -= minutes * 60;
        iNavTimeLeft = String.format("%02d", (int) hours) + ":" + String.format("%02d", (int) minutes) + ":"
                + String.format("%02d", (int) seconds);

        miFirstTurn = new Turn();
        miSecondTurn = new Turn();
        miFirstTurnDistance = miFramework.getFirstTurn(miFirstTurn);
        miSecondTurnDistance = miFramework.getSecondTurn(miSecondTurn);
        miTurnAction = Turn.TURN_NONE;
        switch (miNavigationState) {
            case NavigationState.NO_ACTION:
                break;
            case NavigationState.TURN:
                miTurnAction = miFirstTurn.iTurnType;
                break;
            case NavigationState.TURN_ROUND:
                break;
            case NavigationState.ARRIVAL:
                break;
            case NavigationState.OFF_ROUTE:
                break;
        }
        //showNavigation();
        getMap();
        invalidate();
    }
    private void showNavigation_init() {
        ViewStub miViewStub = (ViewStub) findViewById(R.id.stub_details);
        miDetailsView = miViewStub.inflate();
    }

    public void showNavigation() {
        if(!miDetailsView.isActivated()) {
            showNavigation_init();
        }
        switch (miTurnAction) {
            case Turn.TURN_NONE:
                break;
            case Turn.TURN_AHEAD:
                break;
            case Turn.TURN_BEAR_RIGHT:
                break;
            case Turn.TURN_RIGHT:
                break;
            case Turn.TURN_SHARP_RIGHT:
                break;
            case Turn.TURN_AROUND:
                break;
            case Turn.TURN_SHARP_LEFT:
                break;
            case Turn.TURN_LEFT:
                break;
            case Turn.TURN_BEAR_LEFT:
                break;
        }
    }

    public void navigateStart() {
        setMiRouteProfile(DEFAULT_PROFILE);
        if(routeEndLon == 0 && routeEndLat == 0) {
            notifyMessage(miContext.getString(R.string.destination_not_know));
            isNavigating = false;
        }
        else {
            isNavigating = true;
            if(!isNavigateStarted) {
                if (isGPSon) {
                    if (miCurrentLon == 0 || miCurrentLat == 0) {
                        notifyMessage(miContext.getString(R.string.waiting_GPS));
                        iWantNavigate = true;
                    }
                } else {
                    iWantNavigate = true;
                    toggleGPS(true);
                }
                int result = miFramework.startNavigation(miCurrentLon, miCurrentLat,
                        Framework.DEGREE_COORDS, routeEndLon, routeEndLat,
                        Framework.DEGREE_COORDS);

                if (result != 0) {
                    notifyMessage(miContext.getString(R.string.navigation_error));
                } else {
                    isNavigateStarted = true;
                    showNavigation();
                }
            }
            getMap();
            invalidate();
        }
    }

    /**
     * Enciende o apaga el servicio GPS.
     * Inicia @LocationManager con proveedor @GPS_PROVIDER con tiempo de actualización @MIN_TIME y distancia @MIN_DISTANCE.
     *
     * La navegación Gps consume muchos recursos y no es necesario si el usuario está fuera del área o solo quiere hacer una consulta.
     * @param cState : Hace referencia al estado deseado del servicio. True para encendido, false para apagado.
     */
    private void toggleGPS(boolean cState) {
        Alerts alertsDialog = new Alerts();
        progressDialog = new ProgressDialog(miContext);
        if(!isGPSon && cState) {
            LocationManager vLocationManager = (LocationManager) miContext.getSystemService(Context.LOCATION_SERVICE);
            vLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
            if(vLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                progressDialog = alertsDialog.progressDialog(miContext);
                progressDialog.show();
                isGPSon = true;
            }
            else {
                PackageManager pm = miContext.getPackageManager();
                boolean hasGPS = pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
                if(hasGPS) {
                    alertsDialog.configLocation(miContext);
                }
                else{
                    notifyMessage(miContext.getString(R.string.no_gps));
                }
            }
        }
        else {
            if(isGPSon && !cState) {
                LocationManager vLocationManager = (LocationManager) miContext.getSystemService(Context.LOCATION_SERVICE);
                vLocationManager.removeUpdates(this);
                isGPSon = false;
            }
        }
    }

    /**
     * Referencia la ubicacion del usuario como un icono en la pantalla.
     */
    private void displayCurrentLocation() {
        if(miCurrentLon != 0 && miCurrentLat != 0 && miFramework != null) {
            miFramework.deleteObjects(ID_CURRENT_LOCATION, ID_CURRENT_LOCATION);
            miFramework.addPointObject("route-position", miCurrentLon, miCurrentLat,
                                              Framework.DEGREE_COORDS,
                                              miContext.getString(R.string.current_location), 0, ID_CURRENT_LOCATION, 0);
            getMap();
            invalidate();
        }
    }

    /**
     * Ubica al usuario en el mapa.
     */
    public void locateMe() {
        toggleGPS(true);
        if(miCurrentLon != 0 && miCurrentLat != 0) {
            if(MAX_XA < miCurrentLon && miCurrentLon < MAX_XB) {
                if(MAX_YA < miCurrentLat && miCurrentLat < MAX_YB) {
                    miFramework.setViewCenterLatLong(miCurrentLon, miCurrentLat);
                    displayCurrentLocation();
                }
            }
            else {
                notifyMessage(miContext.getString(R.string.inside_campus));
            }
        }

    }

    /**
     * Busca los lugares cercanos al toque del usuario en la pantalla.
     * @return un @String con el nombre del lugar si toca un edifico o informa si no se encuentran lugares.
     */
    public String getNearbyPlaces() {
        String places = null;
        double[] point = new double[2];
        point[0] = interestingPointLong;
        point[1] = interestingPointLat;
        miFramework.convertCoords(point,Framework.DEGREE_COORDS,Framework.SCREEN_COORDS);
        MapObject[] nearby = miFramework.findInDisplay(point[0], point[1], 20, 5);
        if(nearby != null && nearby.length > 0) {
            int i=0;
            for(MapObject iNearby : nearby) {
                if(iNearby.getLabel().length() != 0) {
                    places = iNearby.getLabel();
                    i++;

                }
            }
            if(i == 0) {
                places = miContext.getString(R.string.no_places_nearby);
            }
        }
        return places;
    }

    public void notifyMessage(String cMessage) {
        Toast.makeText(getContext(), cMessage, Toast.LENGTH_SHORT).show();
    }
    public void setMiRouteProfile(int profile) {
        miRouteProfile = new RouteProfile(profile);
    }
}
