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
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Toast;

import com.cartotype.Framework;
import com.cartotype.MapObject;
import com.cartotype.PathPoint;
import com.cartotype.Rect;
import com.cartotype.RouteProfile;
import com.cartotype.Turn;
import com.proyecto.uis.uismaps.mapview.NearbyPlace;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by cheloreyes on 21-01-15.
 * Implementa la vista del mapa CartoType
 */
public class MapView extends View implements View.OnTouchListener, LocationListener,
                                                     ScaleGestureDetector.OnScaleGestureListener, UISMapsSettingsValues {
    // **********************
    // Constants
    // **********************
    public static final int MAX_PLACES = 50;
    public static final int MIN_DIST_TO_POINT = 13;
    public static final int RADIO = 1080;
    private static final int DEFAULT_PROFILE = RouteProfile.WALKING_PROFILE;
    private static final int DPI_MIN = 160;
    private static final int GRAY_COLOR = 0xFFCCCCCC;
    private static final int ID_CURRENT_LOCATION = 100;
    private static final int ID_ROUTE_END = 103;
    private static final int ID_ROUTE_START = 102;
    private static final int ID_SELECTED_POINT = 101;
    private static final double MAX_XA = -73.1233; //-73.12329138853613
    private static final double MAX_XB = -73.11553; //-73.1155327517166
    private static final double MAX_YA = 7.144; //7.143398173714375
    private static final double MAX_YB = 7.136; //7.1382743178385315
    private static final int MIN_DISTANCE = 2;
    public static final int MIN_SCALE = 500;
    private static final int MIN_TIME = 5000;
    private static final int PRED_SCALE = 16000;
    private static final String UIS_MAPS_FOLDER = Environment.getExternalStorageDirectory().getPath() + "/UISMaps";
    private static final String CAMPUS_MAP = UIS_MAPS_FOLDER + "/mapa/mapa.ctm1";
    private static final String FILE_STYLE = UIS_MAPS_FOLDER + "/estilos/osm-style.xml";
    private static final String FILE_FONT = UIS_MAPS_FOLDER + "/fuentes/DejaVuSans.ttf";
    private static final String TAG = "MapView";

    // **********************
    // Fields
    // **********************

    private final Context miContext;
    private SharedPreferences UIpreferences;
    private NearbyPlace currentPlace;
    private double currentScale;
    private int dpiScreen;
    private double endNavLon;
    private boolean hasAccurancy = false;
    private String iNavTimeLeft;
    private Notify iNotify;
    private boolean iWantNavigate = false;
    private double interestingPointLat;
    private double interestingPointLong;
    private boolean isDisplayingRoute = false;
    private boolean isGPSon = false;
    private boolean isNavigateStarted = false;
    private boolean isNavigating = false;
    private boolean itsInside = false;
    private NearbyPlace lastPlace;
    private Bitmap miBitmap;
    private byte[] miBitmapData;
    private ByteBuffer miBuffer;
    private ContentManager miContent;
    private float miCurrentAccurancy;
    private double miCurrentLat;
    private double miCurrentLon;
    private View miDetailsView;
    private String miDistanceUnits;
    private Turn miFirstTurn;
    private double miFirstTurnDistance;
    private Framework miFramework;
    private String miMapFile;
    private Matrix miMatrix;
    private int miNavigationState;
    private float miPrevScaleFocusX;
    private float miPrevScaleFocusY;
    private double miRouteEndLat;
    private double miRouteEndLon;
    private RouteProfile miRouteProfile;
    private double miRouteStartLat;
    private double miRouteStartLon;
    private float miScale = 1;
    private ScaleGestureDetector miScaleGestureDetector;
    private Turn miSecondTurn;
    private double miSecondTurnDistance;
    private float miStartTouchPointX;
    private float miStartTouchPointY;
    private int miTouchCount;
    private VoiceManager miVoice;
    private float miXOffset;
    private float miYOffset;
    private ProgressDialog progressDialog;
    private double routeEndLat;
    private String smsDistToTurn;
    private int smsFirstTurnType;
    private String smsFullDistance;
    private String smsInfoNav;
    private String smsInfoTurn;

    // **********************
    // Constructors
    // **********************

    /**
     * Constructor principal
     *
     * @param iContext
     * @param iDpi     densidad de pixeles de la pantalla del dispositivo.
     */

    public MapView(Context iContext, int iDpi) {
        super(iContext);
        setOnTouchListener(this);
        dpiScreen = iDpi;
        miContext = iContext;
        UIpreferences = PreferenceManager.getDefaultSharedPreferences(miContext);
        iNotify = new Notify(miContext);
    }

    /**
     * Constructor estandar
     *
     * @param iContexto
     */
    public MapView(Context iContexto) {
        super(iContexto);
        setOnTouchListener(this);
        dpiScreen = DPI_MIN;
        miContext = iContexto;
    }


    // **********************
    // Methods
    // **********************

    /**
     * Inicializa los componentes necesarios por el FrameWork de CartoType para dibujar el mapa.
     */
    public void init() {
        setKeepScreenOn(true);
        miScaleGestureDetector = new ScaleGestureDetector(miContext, this);

        //Nos asegurarnos que existan los archivos y carpetas.
        folderCheck();
        //Color de fondo para las zonas que no cubre el mapa.
        setBackgroundColor(GRAY_COLOR);

        int width = getWidth();
        int height = getHeight();
        miMapFile = CAMPUS_MAP;
        File f = new File(miMapFile);
        Log.v(TAG, "Creando nuevo framework");
        //Se crea la instancia del framework
        miFramework = new Framework(miMapFile, FILE_STYLE, FILE_FONT, width, height);
        miFramework.setResolutionDpi(dpiScreen);
        //Para las funciones de navegacion con un factor de correción de 5 metros y 15 seg.
        miFramework.setNavigatorMinimumFixDistance(1);
        miFramework.setNavigatorTimeTolerance(15);
        setRouteProfile(DEFAULT_PROFILE);

        //Aplica los estados de Escala, rotacion, perspectiva, latitud etc.. de la ultima sesion.
        restoreState();
        miBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        getMap();
        invalidate(); //indica que se necesita redibujar la vista.
    }

    /**
     * getMap() Dibuja el mapa.
     */
    private void getMap() {
        miBitmap = miFramework.getMap();
    }

    /**
     * Obtiene el denomidador de la escala del mapa.
     * @return
     */
    public int getMapScale() {
        int mapScale;
        try {
            mapScale = miFramework.getScale();
        } catch (NullPointerException e) {
            mapScale = PRED_SCALE;
        }
        return mapScale;
    }

    /**
     * Obtiene la posición del mapa en la pantalla.
     *
     * @param pPoint es un vector donde se guarda la posición.
     */
    public void getMapPosition(double[] pPoint) {
        try {
            miFramework.getViewDimensions(pPoint, Framework.DEGREE_COORDS);
        } catch (NullPointerException e) {
            pPoint[0] = 0.0;
            pPoint[1] = 0.0;
        }
    }

    /**
     * Guarda el estado del mapa @MAP_ESCALE, @MAP_LAT, @MAP_LONG, @MAP_ROTATION, @MAP_UNIT
     * cuando el usuario abandona la aplicación.
     */
    public void saveState() {
        SharedPreferences preferences = miContext.getSharedPreferences(ACT_OPTIONS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(MAP_ESCALE, getMapScale());
        double[] mapPos = new double[2];
        getMapPosition(mapPos);
        editor.putLong(MAP_LONG, Double.doubleToLongBits(mapPos[0]));
        editor.putLong(MAP_LAT, Double.doubleToLongBits(mapPos[1]));
        //editor.putLong(MAP_ROTATION, Double.doubleToLongBits((getMapRotation())));
        editor.putString(MAP_UNIT, miDistanceUnits);
        editor.commit();
    }
    /**
     * Enciende o apaga el servicio GPS.
     * Inicia @LocationManager con proveedor @GPS_PROVIDER con tiempo de actualización @MIN_TIME y distancia @MIN_DISTANCE.
     * <p/>
     * La navegación Gps consume muchos recursos y no es necesario si el usuario está fuera del área o solo quiere hacer una consulta.
     *
     * @param cState : Hace referencia al estado deseado del servicio. True para encendido, false para apagado.
     */
    public void toggleGPS(boolean cState) {
        Alerts alertsDialog = new Alerts();
        progressDialog = new ProgressDialog(miContext);
        if (!isGPSon && cState) {
            LocationManager vLocationManager = (LocationManager) miContext.getSystemService(Context.LOCATION_SERVICE);
            vLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
            if (vLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                progressDialog = alertsDialog.progressDialog(miContext);
                progressDialog.show();
                isGPSon = true;
            } else {
                PackageManager pm = miContext.getPackageManager();
                boolean hasGPS = pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
                if (hasGPS) {
                    alertsDialog.configLocation(miContext);
                } else {
                    notifyMessage(miContext.getString(R.string.no_gps));
                }
            }
        } else {
            if (isGPSon && !cState) {
                LocationManager vLocationManager = (LocationManager) miContext.getSystemService(Context.LOCATION_SERVICE);
                vLocationManager.removeUpdates(this);
                miFramework.deleteObjects(ID_CURRENT_LOCATION, ID_CURRENT_LOCATION);
                miCurrentLon = 0.0;
                miCurrentLat = 0.0;
                isGPSon = false;
                hasAccurancy = false;
            }
        }
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
        //se Intenta obtener los estados guardados (posición, escala, rotacion etc.).
        try {
            miDistanceUnits = preferences.getString(MAP_UNIT, "metric");
            map_scale = preferences.getInt(MAP_ESCALE, 0);
            map_rotation = Double.longBitsToDouble(preferences.getLong(MAP_ROTATION, 0));
            map_lat = Double.longBitsToDouble(preferences.getLong(MAP_LAT, 0));
            map_lon = Double.longBitsToDouble(preferences.getLong(MAP_LONG, 0));
        } catch (ClassCastException e) {
            Log.e(TAG, "No se pudo restaurar los valores anteriores, se utilizan los predeterminados");
            e.printStackTrace();
        }
        Log.v(TAG, "restoreState() lat = " + map_lat + "Lon = " + map_lon + "Escala = " + map_scale + "Rotacion = " + map_rotation);
        if (map_scale != 0) {
            Log.v(TAG, "MapSetUp() con ultima escala conocida: " + map_scale);
            miFramework.setScale(map_scale);
        } else {
            Log.v(TAG, "MapSetUp() con escala predeterminada:" + PRED_SCALE);
            miFramework.setScale(PRED_SCALE);
        }
        if (map_rotation != 0) {
            Log.v(TAG, "MapSetUp() con ultima rotacion conocida:" + map_rotation);
            miFramework.setRotation(0);
        }
        if (map_lat != 0 && map_lon != 0) {
            Log.v(TAG, "MapSetUp() con centro en: Lat= " + map_lat + "Lon= " + map_lon);
            miFramework.setViewCenterLatLong(map_lon, map_lat);
        }
    }

    /**
     * Comprueba que existan las carpetas y archivos en la memoria SD del dispositivo.
     */
    private void folderCheck() {
        //Existe la carpeta principal?
        File dir = new File(UIS_MAPS_FOLDER);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        //Existe la carpeta Log?
        dir = new File(UIS_MAPS_FOLDER + "/log");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        //Existe el archivo mapa?
        File f = new File(CAMPUS_MAP);
        if (!f.exists()) {
            assetCopy("mapa");
            Log.i(TAG, "Mapa no encontrado, se copia de asset");
        }
        //Existe la hoja de estilo?
        f = new File(FILE_STYLE);
        if (!f.exists()) {
            assetCopy("estilos");
            Log.i(TAG, "Hoja de estilos no encontrada, se compia de asset");
        }
        f = new File(FILE_FONT);
        if (!f.exists()) {
            assetCopy("fuentes");
            Log.i(TAG, "archivo de fuentes no encontrado, se compia de asset");

        }
    }

    /**
     * Se encarga de crear los directorios y copiar los archivos necesarios del contenido del paquete
     * de aplicación.
     *
     * @param assetItem es el fichero faltante encontrado por @folderCheck.
     */
    private void assetCopy(String assetItem) {
        File sdCardDir = new File(UIS_MAPS_FOLDER + "/" + assetItem);
        if (!sdCardDir.exists()) {
            sdCardDir.mkdirs();
        }
        AssetManager assetManager = getResources().getAssets();
        String[] files = null;

        try {
            files = assetManager.list(assetItem);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(files != null) {
            for(int i = 0; i < files.length; i++) {
                InputStream in = null;
                OutputStream out = null;
                try {
                    in = assetManager.open(assetItem + "/" + files[i]);
                    out = new FileOutputStream(UIS_MAPS_FOLDER + "/" + assetItem + "/" + files[i]);
                    copyFile(in, out);
                    in.close();
                    in = null;
                    out.flush();
                    out.close();
                    out = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Usado por assetCopy(), se encarga de crear el tunel de copia entre los ficheros contenidos
     * en la carpeta "Assets" del paquete de aplicación.
     *
     * @param in  fichero encontrado en la carpeta "Assets" del paquete de aplicación.
     * @param out ruta a donde se queire copiar el contenido encontrado.
     * @throws IOException
     */
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    /**
     * Dibuja el punto sobre el mapa y lo guarda con una latitud y longitud asociada.
     *
     * @param aLongitude Coordenada X del evento.
     * @param aLatitude  Coordenarda Y del evento.
     */
    public void setSelectedPoint(double aLongitude, double aLatitude) {
        ArrayList<NearbyPlace> neabyPoint;
        interestingPointLong = aLongitude;
        interestingPointLat = aLatitude;
        miFramework.deleteObjects(ID_SELECTED_POINT, ID_SELECTED_POINT);
        miFramework.addPointObject("pushpin", interestingPointLong, interestingPointLat, Framework.DEGREE_COORDS,
                                          "", 0, ID_SELECTED_POINT, 0);
        Log.v(TAG, "pone marcador");
        //getNearbyPlaces(interestingPointLong, interestingPointLat);
        //notifyMessage(getNearbyPlaces(interestingPointLong, interestingPointLat));
        //notifyMessage(aLongitude +"," + aLatitude);
        neabyPoint = getNearbyPlaces(interestingPointLong, interestingPointLat);
        currentPlace = neabyPoint.get(0);
        if(lastPlace == null) lastPlace= currentPlace;
        Log.v(TAG,"Imprime: "+neabyPoint.get(neabyPoint.size()-1).getLabel());
        Log.v(TAG,"Imprime: "+neabyPoint.get(0).getLabel());
        Log.v(TAG,"tamaño: " + neabyPoint.size());
        miContent.setPanelContent(neabyPoint.get(0).getLabel());
        getMap();
        invalidate();
    }

    /**
     * Establece el punto marcado por el usuario como punto inicial de la ruta, si existe un punto de destino, inicia la navegación.
     */
    public void setRouteStart() {
        if(hasAccurancy) {
            miRouteStartLon = miCurrentLon;
            miRouteStartLat = miCurrentLat;
        }
        else {
            miRouteStartLon = interestingPointLong;
            miRouteStartLat = interestingPointLat;
        }

        deleteSelectedPoint();
        Log.v(TAG,"Punto inicial seleccionado.");
        miFramework.deleteObjects(ID_ROUTE_START, ID_ROUTE_START);
        miFramework.addPointObject("route_start", miRouteStartLon, miRouteStartLat,
                                          Framework.DEGREE_COORDS, "Punto de inicio", 0, ID_ROUTE_START, 0);
        if(!hasAccurancy && !isGPSon)iNotify.newNotification(currentPlace.getLabel() + miContext.getString(R.string.asStartPoint));
        getMap();
        invalidate();
    }

    /**
     * Elimina el punto inicial de la ruta asignado.
     */
    public void deleteRouteStart() {
        miRouteStartLon = 0.0;
        miRouteStartLat = 0.0;
        miFramework.deleteObjects(ID_ROUTE_START, ID_ROUTE_START);
        getMap();
        invalidate();
    }

    /**
     * Establece el punto marcado por el usuario como punto de destino de la ruta, si existe un punto inical, muestra la ruta en el mapa.
     * Si esta localizado el usuario, lo toma como punto de inicio y marca el punto final para iniciar la navegación asistida.
     */
    public void setRouteEnd() {
        miRouteEndLon = interestingPointLong;
        miRouteEndLat = interestingPointLat;
        if(hasAccurancy) {
            setRouteStart();
        }
        deleteSelectedPoint();
        miFramework.deleteObjects(ID_ROUTE_END, ID_ROUTE_END);
        miFramework.addPointObject("route_end", miRouteEndLon, miRouteEndLat, Framework.DEGREE_COORDS, "", 0, ID_ROUTE_END, 0);
        Log.v(TAG,"Destino Seleccionado");
        if (isDisplayingRoute) {
            miFramework.endNavigation();
            showRoute();
        } else {
            if (miRouteEndLon != 0 && miRouteEndLat != 0 && miRouteStartLon != 0 && miRouteStartLat != 0) {
                if(!hasAccurancy) {
                    showRoute();
                    iNotify.newNotification(currentPlace.getLabel() + miContext.getString(R.string.asEndPoint));
                }
                else {
                    switchNavigation(true);
                }
            }
        }
        getMap();
        invalidate();
    }

    /**
     * Elimina el punto de destino de la ruta asignado.
     */
    public void deleteRouteEnd() {
        miRouteEndLon = 0.0;
        miRouteEndLat = 0.0;
        miFramework.deleteObjects(ID_ROUTE_END, ID_ROUTE_END);
        getMap();
        invalidate();
    }

    /**
     * Elimina el punto sobre el mapa marcado por el usuario.
     */
    private void deleteSelectedPoint() {
        miFramework.deleteObjects(ID_SELECTED_POINT, ID_SELECTED_POINT);
        interestingPointLat = 0.0;
        interestingPointLong = 0.0;

    }

    /**
     * Inicia o detiene la navegación invocando el método según convenga @navigateStart para iniciar la navegación
     * o @navigateStop para detenerla.
     * @param switcher true para iniciar la navegación, false para detener la navegación.
     */
    public void switchNavigation(boolean switcher) {
        if(switcher){
            Log.v(TAG,"inicia navegacion");
            navigateStart();
        }
        else{
            navigateStop();
        }

    }

    /**
     * Inicia la navegación con el punto de destino establecido.
     */
    public void navigateStart() {
        //setRouteProfile(DEFAULT_PROFILE);
        endNavLon = miRouteEndLon;
        routeEndLat = miRouteEndLat;
        if (endNavLon == 0 && routeEndLat == 0) {
            notifyMessage(miContext.getString(R.string.destination_not_know));
            isNavigating = false;
        } else {
            isNavigating = true;
            if (!isNavigateStarted) {
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
                                                                Framework.DEGREE_COORDS, endNavLon, routeEndLat,
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
     * Detiene la navegación eliminando el destino, la ruta indiscada sobre el mapa y deteniendo el GPS.
     */
    private void navigateStop(){
        isNavigating = false;
        notifyMessage(miContext.getString(R.string.navigation_turn_off));
        if(isNavigateStarted) {
            miFramework.endNavigation();
            miFramework.displayRoute(false);
            deleteRouteStart();
            deleteRouteEnd();
            isNavigateStarted = false;
        }
        if(isGPSon) {
            toggleGPS(false);
        }
    }

    /**
     * Realiza las indicaciones de la navegación según establece @miFramework.navigate
     * para dirigirse entre nodos de la ruta.
     * Según este habilitada la UI, estas indicaciones son mediante imágenes o síntesis de voz.
     */
    public void showNavigation() {
        int imageTurn = 0;
        if (isNavigating) { //!miContent.isNavigationView()
            showNavigation_init();
        }
        switch (smsFirstTurnType) {
            case Turn.TURN_NONE:
                imageTurn = R.mipmap.loading_arrow;
                break;
            case Turn.TURN_AHEAD:
                imageTurn = R.mipmap.ahead_arrow;
                break;
            case Turn.TURN_BEAR_RIGHT:
                imageTurn = R.mipmap.soft_right_arrow;
                break;
            case Turn.TURN_RIGHT:
                imageTurn = R.mipmap.right_arrow;
                break;
            case Turn.TURN_SHARP_RIGHT:
                imageTurn = R.mipmap.right_arrow;
                break;
            case Turn.TURN_AROUND:
                imageTurn = R.mipmap.left_arrow;
                break;
            case Turn.TURN_SHARP_LEFT:
                imageTurn = R.mipmap.left_arrow;
                break;
            case Turn.TURN_LEFT:
                imageTurn = R.mipmap.left_arrow;
                break;
            case Turn.TURN_BEAR_LEFT:
                imageTurn = R.mipmap.soft_left_arrow;
                break;
        }

        miContent.setPanelContent(smsInfoNav,smsInfoTurn, smsFullDistance, "", imageTurn);
    }

    /**
     * Inicializa los asistentes de navegación.
     */
    private void showNavigation_init() {
        smsFullDistance = "Calculando;";
        smsInfoNav = "Calculando;";
        smsInfoTurn = "Calculando;";
        smsDistToTurn = "Calculando;";
        smsFirstTurnType = 0;
    }

    /**
     * Éste método responde a @miFramework.navigate y lleva el control de los giros en la
     * ruta, determinando la distancia entre nodos, la velocidad de movimiento y la
     * direccion del póximo giro (el cual es recivido por @showNavigation)
     * @param aValidity
     * @param aTime
     * @param aLong
     * @param aLat
     * @param aSpeed
     * @param aBearing
     * @param aHeight
     */
    public void setUpNavigation(int aValidity, double aTime, double aLong,
                                double aLat, double aSpeed, double aBearing, double aHeight) {
        miFramework.navigate(aValidity, aTime, aLong, aLat, aSpeed, aBearing, aHeight);
        miNavigationState = miFramework.getNavigationState();
        double distance = miFramework.distanceToDestination();
        smsFullDistance = setFormatDistance(distance);
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

        smsInfoTurn = null;
        smsDistToTurn = null;
        smsFirstTurnType = Turn.TURN_NONE;

        smsInfoNav = null;
        Log.v(TAG, "Navegando:" + smsFullDistance );
        switch (miNavigationState) {
            case NavigationState.NO_ACTION:
                break;
            case NavigationState.TURN:
                smsFirstTurnType = miFirstTurn.iTurnType;
                smsDistToTurn = setFormatDistance(miFirstTurnDistance);
                if(!miSecondTurn.iContinue && miSecondTurn.iFromName.equals(miSecondTurn.iToName))
                {
                    smsInfoTurn = "Entonces: "+miSecondTurn.TurnCommand() + "Luego: " + (int) miSecondTurnDistance + "m";
                }
                break;
            case NavigationState.TURN_ROUND:
                smsInfoTurn = miContext.getString(R.string.turn_round);
                break;
            case NavigationState.NEW_ROUTE:
                smsInfoTurn = miContext.getString(R.string.new_route);
                break;
            case NavigationState.ARRIVAL:
                miFirstTurnDistance = miFramework.getFirstTurn(miFirstTurn);
                smsInfoNav = setFormatDistance(miFirstTurnDistance) + "para el destino.";
                break;
            case NavigationState.OFF_ROUTE:
                smsInfoTurn = "";
                break;
        }
        showNavigation();
        getMap();
        invalidate();
    }

    /**
     * Ajusta el formato de unidad de distancia.
     * @param distance
     * @return
     */
    private String setFormatDistance(double distance) {
        Log.v(TAG, "Distancia al destino: "+distance);
        String toReturn;
        if (distance > 1000) {
            toReturn = String.format("%.2f", (distance / 1000)) + "Km";
        }
        else {
            toReturn = String.format("%.2f", (distance)) + "m";
        }
        return toReturn;
    }

    /**
     * Ubica al usuario en el mapa.
     */
    public void locateMe() {
        if (!isGPSon) {
            toggleGPS(true);
        }
        if (miCurrentLon != 0 && miCurrentLat != 0) {
                    if(miFramework.getScale() > 2000) {
                        miFramework.setScale(2000);
                    }
                    miFramework.setViewCenterLatLong(miCurrentLon, miCurrentLat);
                    displayCurrentLocation();
                    //notifyMessage("("+miCurrentLon+", "+miCurrentLat+" )");
                    //notifyMessage(miContext.getString(R.string.locate_notify) + getNearbyPlaces(miCurrentLon, miCurrentLat));
        }
    }

    /**
     * Referencia la ubicacion del usuario como un icono en la pantalla.
     */
    private void displayCurrentLocation() {
        if (miCurrentLon != 0.0 && miCurrentLat != 0.0 && miFramework != null) {
            miFramework.deleteObjects(ID_CURRENT_LOCATION, ID_CURRENT_LOCATION);

            if (miCurrentLon > MAX_XA && miCurrentLon < MAX_XB) {
                if (miCurrentLat < MAX_YA && miCurrentLat > MAX_YB) {
                    miFramework.addPointObject("route-position", miCurrentLon, miCurrentLat,
                                                      Framework.DEGREE_COORDS,
                                                      "", 0, ID_CURRENT_LOCATION, 0);
                    Log.v(TAG,"Ubicación actualizada "+ miCurrentLon + " , " + miCurrentLat);
                }
            }else {
                Log.v(TAG + "Location", "No está dentro del campus. Ubicación: " + miCurrentLon + " , " + miCurrentLat);
                iNotify.newNotification(miContext.getString(R.string.not_inside_campus));
                //toggleGPS(false);
            }
            //miContent.setInfoText(miContext.getString(R.string.current_accuracy) + miCurrentAccurancy + " " + miContext.getString(R.string.meters));
            getMap();
            invalidate();
        }
    }

    /**
     * Busca los lugares en un radio @RADIO del punto establecido por @setSelectedPoint obteniendo un maximo de elementos @MAX_PLACES.
     *
     * @return un @String con el nombre del lugar si toca un edifico o informa si no se encuentran lugares.
     */
    public ArrayList<NearbyPlace> getNearbyPlaces(double longitud, double latitud) {
        ArrayList<NearbyPlace> arrayPlaces = new ArrayList<>();
        itsInside = false;
        currentScale = miFramework.getScale();
        double[] point = new double[2];
        point[0] = longitud;
        point[1] = latitud;
        miFramework.convertCoords(point, Framework.DEGREE_COORDS, Framework.SCREEN_COORDS);
        MapObject[] nearby = miFramework.findInDisplay(point[0], point[1], (RADIO * (MIN_SCALE / currentScale)), MAX_PLACES);
        if (nearby != null && nearby.length > 0) {
            int i = 0;
                for (MapObject iNearby : nearby) {
                    currentPlace = new NearbyPlace();
                    if (iNearby.getLabel().length() != 0) {
                        if(!itsInside) {
                            double[] distance = getCenterNearby(iNearby);
                            double[] bounds = getBoudsNearby(iNearby);
                            miFramework.convertCoords(bounds, Framework.MAP_COORDS, Framework.DEGREE_COORDS);
                            miFramework.convertCoords(distance,Framework.DEGREE_COORDS, Framework.DEGREE_COORDS);
                            miFramework.convertCoords(point,Framework.SCREEN_COORDS, Framework.DEGREE_COORDS);
                            Log.v(TAG, "Encuentra: " + iNearby.getLabel());
                            currentPlace.setLabel(iNearby.getLabel());
                            currentPlace.setDistance(getNearbyDistance(point, distance, bounds));
                            arrayPlaces.add(currentPlace);
                            i++;
                        }
                    }
                }
            //miVoice.textToSpeech(places);
            if (i == 0) {
                currentPlace.setLabel(miContext.getString(R.string.no_places_nearby));
                arrayPlaces.add(i,currentPlace);
            }
        }
        Log.v(TAG, "Radio: " + (RADIO * (MIN_SCALE / currentScale)));
        return arrayPlaces;
    }

    /**
     * Obtiene la distancia a los edificios cercanos encontrados por @getNearbyPlaces. los parámetros deben estar en
     * @Framework.DEGREE_COORDS.
     * @param currentLocation Ubicación actual del usuario, (longitud, latitud).
     * @param centerNearby Centro del polígono (longitud, latitud).
     * @param bounds Máximos y mímimos (X ,Y) del polígono.
     * @return
     */
    private double getNearbyDistance(double[] currentLocation, double[] centerNearby, double[] bounds) {
        double distance = 0.0;
        double[] locationMts = currentLocation;
        double[] centerMts = centerNearby;
        double[] boundsMts = bounds;

        miFramework.convertCoords(locationMts, Framework.DEGREE_COORDS, Framework.MAP_METER_COORDS);
        miFramework.convertCoords(centerMts, Framework.DEGREE_COORDS, Framework.MAP_METER_COORDS);
        miFramework.convertCoords(boundsMts, Framework.DEGREE_COORDS, Framework.MAP_METER_COORDS);

        double a = centerMts[0] - locationMts[0];
        double c = centerMts[1] - locationMts[1];
        double b = Math.sqrt(Math.pow(a,2) + Math.pow(c,2));

        double aE = boundsMts[0] - centerMts[0];
        double cE = boundsMts[1] - centerMts[1];
        double bEdificio = Math.sqrt(Math.pow(aE,2) + Math.pow(cE,2));

        if( currentLocation[0] > bounds[2] && currentLocation[0] < bounds[0] && currentLocation[1] > bounds[3] && currentLocation[1] < bounds[1])
        {
            Log.v(TAG, "Dentro del edificio");
            currentPlace.setInside(true);
            itsInside = true;
            return -1;
        }
        else {
            if( currentLocation[0] > bounds[2] && currentLocation[0] < bounds[0] && currentLocation[1] < bounds[3] && currentLocation[1] < bounds[1]) {
                distance = b - Math.abs((boundsMts[1] - boundsMts[3]) / 2);
                Log.v(TAG, "X > Xmin && X < Xmax && Y < Ymin && Y < Ymax ... al sur del edificio.");
            }
            else {
                if(currentLocation[0] > bounds[2] && currentLocation[0] < bounds[0] && currentLocation[1] > bounds[3] && currentLocation[1] > bounds[1]) {
                    Log.v(TAG, "X > Xmin && X < Xmax && Y > Ymin && Y > Ymax ... al norte del edificio.");
                    distance = b - Math.abs((boundsMts[1] - boundsMts[3]) / 2);
                }
                else {
                    if(currentLocation[0] > bounds[2] && currentLocation[0] > bounds[0] && currentLocation[1] > bounds[3] && currentLocation[1] < bounds[1]) {
                        Log.v(TAG, "X > Xmin && X > Xmax && Y > Ymin && Y < Ymax ... al este del edificio.");
                        distance = b - Math.abs((boundsMts[0] - boundsMts[2]) / 2);
                    }
                    else {
                        if (currentLocation[0] < bounds[2] && currentLocation[0] < bounds[0] && currentLocation[1] > bounds[3] && currentLocation[1] < bounds[1]) {
                            Log.v(TAG, "X < Xmin && X < Xmax && Y > Ymin && Y < Ymax ... al oeste del edificio.");
                            distance = b - Math.abs((boundsMts[0] - boundsMts[2]) / 2);
                        }
                        else {
                            if (currentLocation[0] < bounds[2] && currentLocation[1] > bounds[1]) {
                                Log.v(TAG, "X < Xmin && Y > Ymax ... al Nor-oeste del edificio.");
                                distance = b - bEdificio;
                            }
                            else {
                                if (currentLocation[0] < bounds[2] && currentLocation[1] < bounds[3]) {
                                    Log.v(TAG, "X < Xmin && Y < Ymin ... al Sur- oeste del edificio.");
                                    distance = b - bEdificio;
                                }
                                else {
                                    if (currentLocation[0] > bounds[0] && currentLocation[1] > bounds[1]) {
                                        Log.v(TAG, "X > Xmax && Y > Ymax ... al Nor-este del edificio.");
                                        distance = b - bEdificio;
                                    }
                                    else {
                                        if (currentLocation[0] > bounds[0] && currentLocation[1] < bounds[3]) {
                                            Log.v(TAG, "X > Xmax && Y < Ymin ... al Sur- este del edificio.");
                                            distance = b - bEdificio;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            distance = Math.round(distance);
            Log.v(TAG, "Distancia: " + distance);
            if(distance > 100) {
                return 100;
            }
            else {
                if(distance < 3) {
                    return 0;
                }
                return distance ;
            }
        }
    }

    /**
     * Obtiene el centro del polígono
     * @param aMapObject Polígono del tipo @MapObject a determinar el centro.
     * @return Retorna un arreglo de @Double con las coordenadas del centro (longitud, latitud).
     */
    private double[] getCenterNearby(MapObject aMapObject) {

        PathPoint pathPoint = new PathPoint();
        aMapObject.getCenter(pathPoint);
        double[] center = new double[2];
        center[0] = pathPoint.iX;
        center[1] = pathPoint.iY;

        miFramework.convertCoords(center, Framework.MAP_COORDS,
                                        Framework.DEGREE_COORDS);

        return center;
    }

    /**
     * Obtiene los límites del polígono del tipo @MapObject.
     * @param aMapObject Poligono para determinar sus límites.
     * @return Es un arreglo con Xmax, Ymax, Xmin, Ymin.
     */
    private double[] getBoudsNearby(MapObject aMapObject) {
        Rect rect = new Rect();
        aMapObject.getBounds(rect);
        double[] bounds = new double[4];
        bounds[0] = rect.iMaxX;
        bounds[1] = rect.iMaxY;
        bounds[2] = rect.iMinX;
        bounds[3] = rect.iMinY;
        return bounds;
    }

    /**
     * Despliega una pequeña ventana emergente con información sencilla a fin de informar al usuario rápidamente. la ventana desaparece
     * automaticamente despues de un breve tiempo.
     *
     * @param cMessage información que se quiere mostrar.
     */
    public void notifyMessage(String cMessage) {
        Toast.makeText(getContext(), cMessage, Toast.LENGTH_SHORT).show();
    }

    /**
     * Establece el perfil de la ruta, puede ser de coche o de caminar.
     *
     * @param profile según el tipo de ruta que se quiera, puede ser de coche o de caminar.
     */
    public void setRouteProfile(int profile) {
        miRouteProfile = new RouteProfile(profile);
        miFramework.addProfile(miRouteProfile);
        miFramework.setMainProfile(miRouteProfile);
    }


    /**
     * Muestra la ruta a seguir para ir del punto de inicio @setRouteStart() al punto de destino @setRouteEnd() sobre el mapa
     * una vez se han establecido. Puede que  no exista una ruta según el perfil asignado a @RouteProfile,
     * si es así se notifica al usuario mediante @notifyMessage().
     */
    public void showRoute() {
        int result = 1;
        if (miRouteStartLat == 0 || miRouteStartLon == 0) {
            notifyMessage(miContext.getString(R.string.missing_start_route));
        }
        if (miRouteEndLat == 0 || miRouteEndLon == 0) {
            notifyMessage(miContext.getString(R.string.missing_end_route));
        }
        if (miRouteStartLon != 0 && miRouteStartLat != 0 && miRouteEndLon != 0 && miRouteStartLat != 0) {
            result = miFramework.startNavigation(miRouteStartLon, miRouteStartLat, Framework.DEGREE_COORDS, miRouteEndLon, miRouteEndLat, Framework.DEGREE_COORDS);
        }
        //miFramework.chooseRoute(1);
        //miFramework.displayRoute(false);
        if (result != 0) {
            notifyMessage(miContext.getString(R.string.navigation_error));
            miFramework.displayRoute(false);
            isDisplayingRoute = false;
        } else {
            isDisplayingRoute = true;
            miFramework.displayRoute(true);
            miContent.setPanelContent("Ruta de: " + lastPlace.getLabel() + " a " +currentPlace.getLabel());
        }
        getMap();
        invalidate();
    }

    /**
     * Elimina los objetos creados sobre el mapa, como: Puntos de inicio y destino, rutas, ubicación.
     */
    public void removeMapObjects() {
        if(isGPSon) {
           toggleGPS(false);

        }
        if(isDisplayingRoute) {
            miFramework.displayRoute(false);

        }
        if(isNavigating){
            miFramework.endNavigation();
        }
        deleteRouteStart();
        deleteRouteEnd();

        miFramework.deleteObjects(ID_CURRENT_LOCATION, ID_CURRENT_LOCATION);
        miFramework.deleteObjects(ID_SELECTED_POINT, ID_SELECTED_POINT);
        currentPlace = null;
        lastPlace = null;
        getMap();
        invalidate();
    }

    /**
     * Establece el contenedor de esta vista.
     * @param contentManager
     */
    public void setContentManager(ContentManager contentManager) {
        miContent = contentManager;
    }

    /**
     * Buscador retorna el centro del edificio a buscar si existe, busca la coincidencia exacta al nombre.
     * @param toFind Palabra a buscar.
     * @param method Método utilizado, puede ser: @FrameWork.EXACT_STRING_MATCH_METHOD
     *               Framework.FUZZY_STRING_MATCH_FLAG
     *               Framework.PREFIX_STRING_MATCH_FLAG
     * @param maxObjects Maximo de resultados.
     */
    public ArrayList<String> finder(String toFind, int method, int maxObjects) {
        ArrayList<String> results = new ArrayList<>();
        MapObject[] result = miFramework.find(toFind, method, maxObjects);
        if(result != null && result.length != 0) {
            int i = 0;
            for( MapObject temp : result) {
                if(temp.getLabel().length() != 0) {
                    results.add(i, temp.getLabel());
                }
            }
        }
        return results;
    }

    /**
     * Encuentra un lugar específico del mapa y utiliza @setSelectedPoint para marcarlo.
     * @param toFocus label del lugar.
     */
    public void foundFocus(String toFocus) {
        double[] center = new double[2];
        MapObject[] result = miFramework.find(toFocus, Framework.FUZZY_STRING_MATCH_METHOD, 1);
        if(result != null && result.length != 0) {
            for( MapObject temp : result) {
                center = getCenterNearby(temp);
                Log.v(TAG, "Centro a: " +toFocus+ " en: ( "+center[0]+", "+center[1]+")");
            }
        }
        setSelectedPoint(center[0], center[1]);
        miFramework.setViewCenterLatLong(center[0], center[1]);
        getMap();
        invalidate();
    }

    // **********************
    // Methods from SuperClass
    // **********************

    /**
     * Override del método @OnTouchListener.onTouch para determinar los gestos del usuario al pulsar la pantalla, si se activa
     * la UI para invidentes, desactiva los gestos tactiles.
     *
     * @param v
     * @param event
     * @return
     * @MotionEvent.ACTION_DOWN registra cuando se toca la pantalla.
     * @MotionEvent.ACTION_MOVE registra el movimiento mientras se está tocando la pantalla.
     * @MotionEvent.ACTION_UP registra cuando se deja de tocar la pantalla.
     * <p/>
     * Al finalizar esta acción de eventos se verifíca la si diferencia entre @MotionEvent.ACTION_DOWN y MotionEvent.ACTION_UP
     * es menor a @MIN_DIST_TO_POINT se pone un marcador en la pantalla mediante @setSelectedPoint. Si la diferencia es mayor, se hace un
     * desplazamiento del mapa en la pantalla.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (miScaleGestureDetector == null || miFramework == null || UIpreferences.getBoolean(EYESIGHT_ASSISTANT, false)) {
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

                if (miXOffset > -MIN_DIST_TO_POINT && miXOffset < MIN_DIST_TO_POINT && miYOffset > -MIN_DIST_TO_POINT && miYOffset < MIN_DIST_TO_POINT) {
                    double arrayOfDouble[] = new double[2];
                    arrayOfDouble[0] = event.getX();
                    arrayOfDouble[1] = event.getY();
                    //notifyMessage("(" + event.getX() + ", " + event.getY() +")");
                    miFramework.convertCoords(arrayOfDouble, Framework.SCREEN_COORDS, Framework.DEGREE_COORDS);
                    setSelectedPoint(arrayOfDouble[0], arrayOfDouble[1]);
                    miTouchCount = 0;
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

    public boolean isDisplayingRoute() {
        return isDisplayingRoute;
    }

    public boolean isHasAccurancy() {
        return hasAccurancy;
    }

    public boolean isNavigating() {
        return isNavigating;
    }

    /**
     * Override del método @View.OnDraw dibuja la vista del mapa obtenida del @Framework
     * de CartoType al iniciar la aplicación, durante el desplazamiento, el acercamiento y la navegación.
     *
     * @param canvas the canvas on which the background will be drawn
     */
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

    /**
     * Override el método @onScale, Responde a los eventos de escalamiento, que es cuando el usuario hace un doble toque y separa o junta ambos toques.
     * Lo cual se toma para efectos de realizar acercamiento o alejar la vista del mapa.
     *
     * @param detector es el gesto del usuario.
     * @return
     */
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

    /**
     * Override del método @onScaleBegin, que se ejecuta cuando el usuario inicia a ejecutar el gesto de escalamiento.
     * Determina el punto de inicio del gesto.
     *
     * @param detector
     * @return
     */
    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        miScaleGestureDetector = detector;
        miPrevScaleFocusX = miScaleGestureDetector.getFocusX();
        miPrevScaleFocusY = miScaleGestureDetector.getFocusY();
        miTouchCount = 2;
        return true;
    }

    /**
     * Override del método @onScaleEnd, que se ejecuta cuando el usuario termina de ejecutar el gesto de escalamiento.
     * Aplicando el efecto de acercamiento o alejamiento a la vista del mapa.
     *
     * @param detector
     */
    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        miScaleGestureDetector = detector;
        if (miScale != 1) {
            miFramework.zoom(miScale);
        }
        miScale = 1;
        if (miXOffset != 0 || miYOffset != 0) {
            miFramework.pan((int) Math.floor(-miXOffset + 0.5), (int) Math.floor(-miYOffset + 0.5));
        }
        miXOffset = 0;
        miYOffset = 0;
        miTouchCount = 0;
        Log.v(TAG, "Escala: "+ miFramework.getScale());
        getMap();
        invalidate();
    }

    /**
     * Override del método @LocationListener.onLocationChanged, el cual es llamado cuando cambia la ubicación del usuario según los criterios definidos de @MIN_DISTANCE y @MIN_TIME
     * al objeto de @LocationManager.
     *
     * @param cLocation
     */
    @Override
    public void onLocationChanged(Location cLocation) {
        if (cLocation.hasAccuracy() && !hasAccurancy) {
            progressDialog.hide();
            progressDialog.dismiss();
            hasAccurancy = cLocation.hasAccuracy();
        }
        Log.d(TAG, "Location update " + miCurrentLon + ", " + miCurrentLat + " Accurracy: "+cLocation.getAccuracy());
        miCurrentLon = cLocation.getLongitude() + 0.00000895;
        miCurrentLat = cLocation.getLatitude() - 0.00004458;
        miCurrentAccurancy = cLocation.getAccuracy();
        if (iWantNavigate) {
            navigateStart();
            iWantNavigate = false;
        }
        if (isNavigateStarted) {
            int validity = Framework.POSITION_VALID | Framework.TIME_VALID;
            if (cLocation.hasSpeed()) {
                validity |= Framework.SPEED_VALID;
            }
            if (cLocation.hasBearing()) {
                validity |= Framework.COURSE_VALID;
            }
            if (cLocation.hasAltitude()) {
                validity |= Framework.HEIGHT_VALID;
            }
            double time = (cLocation.getTime()) / 1000;
            double speed = cLocation.getSpeed() * 3.6;
            Log.v(TAG,"setUpNavigation("+validity+", "+time+ ", "+ speed +", ");
            setUpNavigation(validity, time, miCurrentLon, miCurrentLat, speed, cLocation.getBearing(), cLocation.getAltitude());
            miFramework.setViewCenterLatLong(miCurrentLon, miCurrentLat);
            miFramework.setRotation(0);
            displayCurrentLocation();
        } else {
            //locateMe();
            displayCurrentLocation();
        }

    }

    //Estos métodos de la superclase @LocationListener no se utilizaron pero son requisito de la interface.
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.v(TAG,"Conectado a: " + provider);
        miContent.setStatusLocationBtn(status);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.v(TAG, "Conectado a: " + provider + " activado.");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.v(TAG, "Conectado a: " + provider + " Desactivado.");
    }

    public void setVoiceManager(VoiceManager voiceManager) {
        this.miVoice = voiceManager;
    }
}
