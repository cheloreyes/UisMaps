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
    public static final int MAX_PLACES = 5;
    // **********************
    // Constants
    // **********************
    public static final int MIN_DIST_TO_POINT = 13;
    public static final int RADIO = 20;
    private static final String ACT_OPTIONS = "UisMapPreferencias";
    private static final int DEFAULT_PROFILE = RouteProfile.WALKING_PROFILE;
    private static final int DPI_MIN = 160;
    private static final int GRAY_COLOR = 0xFFCCCCCC;
    private static final int ID_CURRENT_LOCATION = 100;
    private static final int ID_ROUTE_END = 103;
    private static final int ID_ROUTE_START = 102;
    private static final int ID_SELECTED_POINT = 101;
    private static final String MAP_ESCALE = "last_scale";
    private static final String MAP_LAT = "last_lat";
    private static final String MAP_LONG = "last_lon";
    private static final String MAP_ROTATION = "last_rotation";
    private static final String MAP_UNIT = "display_units";
    private static final double MAX_XA = -73.1233; //-73.12329138853613
    private static final double MAX_XB = -73.11553; //-73.1155327517166
    private static final double MAX_YA = 7.144; //7.143398173714375
    private static final double MAX_YB = 7.136; //7.1382743178385315
    private static final int MIN_DISTANCE = 3;
    private static final int MIN_TIME = 2000;
    private static final int PRED_SCALE = 2500000;
    private static final String TAG = "MapView";
    private static final String UIS_MAPS_FOLDER = Environment.getExternalStorageDirectory().getPath() + "/UISMaps";
    private static final String CAMPUS_MAP = UIS_MAPS_FOLDER + "/mapa/mapa.ctm1";
    private static final String FILE_STYLE = UIS_MAPS_FOLDER + "/estilos/osm-style.xml";
    private static final String FILE_FONT = UIS_MAPS_FOLDER + "/fuentes/DejaVuSans.ttf";

    // **********************
    // Fields
    // **********************

    private final Context miContext;
    private int dpiScreen;
    private String iNavTimeLeft;
    private boolean iWantNavigate = false;
    private double interestingPointLat;
    private double interestingPointLong;
    private boolean isDisplayingRoute;
    private boolean isGPSon = false;
    private boolean hasAccurancy = false;
    private boolean isNavigateStarted = false;
    private boolean isNavigating = false;
    private Bitmap miBitmap;
    private byte[] miBitmapData;
    private ByteBuffer miBuffer;
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
    private boolean miPerspective;
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
     *
     * @param iContext
     * @param iDpi     densidad de pixeles de la pantalla del dispositivo.
     */

    public MapView(Context iContext, int iDpi) {
        super(iContext);
        setOnTouchListener(this);
        dpiScreen = iDpi;
        miContext = iContext;
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
        miFramework.setNavigatorMinimumFixDistance(5);
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
     * Obtiene el angulo de rotación del mapa.
     *
     * @return el angulo de rotación del mapa, 0 si no se a inicializado.
     */
    private double getMapRotation() {
        try {
            return miFramework.getRotation();
        } catch (NullPointerException e) {
            return 0.0;
        }
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
        editor.putLong(MAP_ROTATION, Double.doubleToLongBits((getMapRotation())));
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
            miFramework.setRotation(map_rotation);
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
            Log.e("Error al leer carpeta assets", e.toString());
            e.printStackTrace();
        }

        for (int i = 0; i < files.length; i++) {
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
                Log.e("Error al copiar de asset", e.toString());
                e.printStackTrace();
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
        miFramework.deleteObjects(ID_ROUTE_START, ID_ROUTE_START);
        miFramework.addPointObject("route_start", miRouteStartLon, miRouteStartLat,
                                          Framework.DEGREE_COORDS, "Punto de inicio", 0, ID_ROUTE_START, 0);
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
     * Establece el punto marcado por el usuario como punto de destino de la ruta, si existe un punto inical, inicia la navegación.
     */
    public void setRouteEnd() {
        miRouteEndLon = interestingPointLong;
        miRouteEndLat = interestingPointLat;
        deleteSelectedPoint();
        miFramework.deleteObjects(ID_ROUTE_END, ID_ROUTE_END);
        miFramework.addPointObject("route_end", miRouteEndLon, miRouteEndLat, Framework.DEGREE_COORDS, "", 0, ID_ROUTE_END, 0);
        if (isDisplayingRoute) {
            miFramework.endNavigation();
            showRoute();
        } else {
            if (miRouteEndLon != 0 && miRouteEndLat != 0 && miRouteStartLon != 0 && miRouteStartLat != 0) {
                showRoute();
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
     * dibuja el punto sobre el mapa y lo guarda con una latitud y longitud asociada.
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
        notifyMessage(getNearbyPlaces());
        //notifyMessage(aLongitude +"," + aLatitude);
        MainActivity.showFloatingMenu(true);
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
        if (!miDetailsView.isActivated()) {
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
        setRouteProfile(DEFAULT_PROFILE);
        if (routeEndLon == 0 && routeEndLat == 0) {
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
     * Ubica al usuario en el mapa.
     */
    public void locateMe() {
        if (!isGPSon) {
            toggleGPS(true);
        }
        if (miCurrentLon != 0 && miCurrentLat != 0) {
            if (miCurrentLon > MAX_XA && miCurrentLon < MAX_XB) {
                if (miCurrentLat < MAX_YA && miCurrentLat > MAX_YB) {
                    miFramework.setScale(1700);
                    miFramework.setViewCenterLatLong(miCurrentLon, miCurrentLat);
                    displayCurrentLocation();
                }
            } else {
                notifyMessage(miContext.getString(R.string.not_inside_campus));
                toggleGPS(false);
            }
        }
    }

    /**
     * Referencia la ubicacion del usuario como un icono en la pantalla.
     */
    private void displayCurrentLocation() {
        if (miCurrentLon != 0 && miCurrentLat != 0 && miFramework != null) {
            miFramework.deleteObjects(ID_CURRENT_LOCATION, ID_CURRENT_LOCATION);
            miFramework.addPointObject("route-position", miCurrentLon, miCurrentLat,
                                              Framework.DEGREE_COORDS,
                                              "", 0, ID_CURRENT_LOCATION, 0);
            MainActivity.setInformationText(miContext.getString(R.string.current_accuracy) + miCurrentAccurancy + " " + miContext.getString(R.string.meters));
            getMap();
            invalidate();
        }
    }

    /**
     * Busca los lugares en un radio @RADIO del punto establecido por @setSelectedPoint obteniendo un maximo de elementos @MAX_PLACES.
     *
     * @return un @String con el nombre del lugar si toca un edifico o informa si no se encuentran lugares.
     */
    public String getNearbyPlaces() {
        String places = null;
        double[] point = new double[2];
        point[0] = interestingPointLong;
        point[1] = interestingPointLat;
        miFramework.convertCoords(point, Framework.DEGREE_COORDS, Framework.SCREEN_COORDS);
        MapObject[] nearby = miFramework.findInDisplay(point[0], point[1], RADIO, MAX_PLACES);
        if (nearby != null && nearby.length > 0) {
            int i = 0;
            for (MapObject iNearby : nearby) {
                if (iNearby.getLabel().length() != 0) {
                    places = iNearby.getLabel();
                    i++;

                }
            }
            if (i == 0) {
                places = miContext.getString(R.string.no_places_nearby);
            }
        }
        return places;
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
        }
        getMap();
        invalidate();
    }

    public void removeMapObjects() {
        if(isGPSon) {
           toggleGPS(false);
        }
        if(isDisplayingRoute) {

        }
        deleteRouteStart();
        deleteRouteEnd();
        miFramework.displayRoute(false);
        miFramework.endNavigation();
        miFramework.deleteObjects(ID_CURRENT_LOCATION, ID_CURRENT_LOCATION);
        miFramework.deleteObjects(ID_SELECTED_POINT, ID_SELECTED_POINT);

        getMap();
        invalidate();
    }

    // **********************
    // Methods from SuperClass
    // **********************

    /**
     * Override del método @OnTouchListener.onTouch para determinar los gestos del usuario al pulsar la pantalla.
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

                if (miXOffset > -MIN_DIST_TO_POINT && miXOffset < MIN_DIST_TO_POINT && miYOffset > -MIN_DIST_TO_POINT && miYOffset < MIN_DIST_TO_POINT) {
                    double arrayOfDouble[] = new double[2];
                    arrayOfDouble[0] = event.getX();
                    arrayOfDouble[1] = event.getY();
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
        notifyMessage(miFramework.getScale() + "");
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
        if (cLocation.hasAccuracy()) {
            progressDialog.hide();
            hasAccurancy = true;
            locateMe();
        }
        Log.d(TAG, "Location update " + miCurrentLon + ", " + miCurrentLat);
        miCurrentLon = cLocation.getLongitude();
        miCurrentLat = cLocation.getLatitude();
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
            setUpNavigation(validity, time, miCurrentLon, miCurrentLat, speed, cLocation.getBearing(), cLocation.getAltitude());
            miFramework.setViewCenterLatLong(miCurrentLon, miCurrentLat);
            displayCurrentLocation();
        } else {
            displayCurrentLocation();
        }

    }

    //Estos métodos de la superclase @LocationListener no se utilizaron pero son requisito de la interfaz.
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
