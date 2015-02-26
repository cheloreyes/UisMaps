package com.proyecto.uis.uismaps;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.ResourceProxyImpl;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MinimapOverlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;

/**
 * Created by cheloreyes on 19/02/15.
 */
public class MapController extends Fragment implements OpenStreetMapTileProviderConstants {

    // **********************
    // Constants

    private String TAG = "MapController";
    private static final int DEFAULT_RESOLUTION = 256;
    private static final int DEFAULT_ZOOM = 18;
    private static final int MIN_ZOOM = 17;
    private static final int MAX_ZOOM = 19;
    private static final int BACKGROUND_COLOR = 15592162; // R:237 G: 234 B: 226
    private static final BoundingBoxE6 BOUNDING_BOX = new BoundingBoxE6(7.13927299476594,
            -73.1231647642, 7.14257310135635, -73.1161113000236);
    private static final GeoPoint CAMPUS_CENTER = new GeoPoint(7.1410242928925083, -73.11925510433737);

    //TODO: estas las constantes serían mejor en una interfaz? public?

    private static final String IPREF_NAME = "com.proyecto.uis.prefs";
    private static final String IPREF_SCROLL_X = "scrollX";
    private static final String IPREF_SCROLL_Y = "scrollY";
    private static final String IPREF_ZOOM = "zoomLevel";
    private static final String IPREF_SHOW_LOCATION = "showLocation";
    private static final String IPREF_SHOW_COMPASS = "showCompass";
    private static final String IPREF_MINIMAP = "showMiniMap";

    // **********************
    // Fields
    // **********************

    private Context iContext;
    private SharedPreferences iPreferences;
    private MapView iMapview;
    private ResourceProxy ireResourceProxy;
    private DisplayMetrics dm;
    private XYTileSource tile;
    private CompassOverlay iCompassOverlay;
    private MinimapOverlay iMinimapOverlay;
    private ScaleBarOverlay iScaleBarOverlay;
    private MyLocationNewOverlay iMyLocationOverlay;
    private IMapController iMapController;

    // **********************
    // Constructors
    // **********************
    public static MapController newInstance() {
        MapController controller = new MapController();
        return controller;
    }

    // **********************
    // Methods from SuperClass
    // **********************
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ireResourceProxy = new ResourceProxyImpl(inflater.getContext().getApplicationContext());
        iMapview = new MapView(inflater.getContext(), 512, ireResourceProxy);
        //turnOffHardwareAceleration();
        return iMapview;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        iContext = this.getActivity();
        init();
    }

    /**
     * Se llama cuando el @Fragment no se encuentra en ejecución.
     * Guardando las variables de estado relacionadas con @iMapview sobre la ultima posición del mapa,
     * el nivel de zoom de la vista, y los estados de ubicación, brújula y minimapa.
     */
    @Override
    public void onPause() {
        SharedPreferences.Editor editor = iPreferences.edit();
        editor.putInt(IPREF_SCROLL_X, iMapview.getScrollX());
        editor.putInt(IPREF_SCROLL_Y, iMapview.getScrollY());
        editor.putInt(IPREF_ZOOM, iMapview.getZoomLevel());
        editor.putBoolean(IPREF_SHOW_LOCATION, iMyLocationOverlay.isMyLocationEnabled());
        editor.putBoolean(IPREF_SHOW_COMPASS, iCompassOverlay.isCompassEnabled());
        editor.putBoolean(IPREF_MINIMAP, iMinimapOverlay.isEnabled());
        editor.commit();

        iMyLocationOverlay.disableMyLocation();
        iCompassOverlay.disableCompass();

        super.onPause();
    }

    /**
     * Se llama cuando se retoma la ejecución del @Fragment.
     * Toma los estados guardados en @SharedPreferences.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (iPreferences.getBoolean(IPREF_SHOW_LOCATION, true)) {
            iMyLocationOverlay.enableMyLocation();
        }
        if (iPreferences.getBoolean(IPREF_SHOW_COMPASS, true)) {
            iCompassOverlay.enableCompass();
        }
        iMinimapOverlay.setEnabled(iPreferences.getBoolean(IPREF_MINIMAP, true));

        iMapview.scrollTo(iPreferences.getInt(IPREF_SCROLL_X, 0), iPreferences.getInt(IPREF_SCROLL_Y, 0));

        iMapController.setZoom(iPreferences.getInt(IPREF_ZOOM, DEFAULT_ZOOM));
    }

    // **********************
    // Methods
    // **********************

    /**
     * Inicializa el mapa junto a los complementos, como brujula, mini mapa, barra de escala
     * y los añade a la vista del mapa.
     */
    public void init() {
        setUpTile(DEFAULT_RESOLUTION);

        dm = iContext.getResources().getDisplayMetrics();

        iPreferences = iContext.getSharedPreferences(IPREF_NAME, Context.MODE_PRIVATE);

        iCompassOverlay = new CompassOverlay(iContext, new InternalCompassOrientationProvider(iContext), iMapview);
        iCompassOverlay.enableCompass();

        iMyLocationOverlay = new MyLocationNewOverlay(iContext, new GpsMyLocationProvider(iContext), iMapview);
        iMyLocationOverlay.enableMyLocation();
//TODO: Hacer que se pueda activar y desactivar el minimapa.
        iMinimapOverlay = new MinimapOverlay(iContext, iMapview.getTileRequestCompleteHandler());
        iMinimapOverlay.setWidth(dm.widthPixels / 5);
        iMinimapOverlay.setHeight(dm.heightPixels / 5);
        iMinimapOverlay.setTileSource(tile);
        iMinimapOverlay.setUseDataConnection(false);

        iScaleBarOverlay = new ScaleBarOverlay(iContext);
        iScaleBarOverlay.setCentred(true);
        iScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);

        iMapview.setTileSource(tile);
        iMapview.setClickable(true);
        iMapview.setBuiltInZoomControls(true);
        iMapview.setMultiTouchControls(true);
        iMapview.setUseDataConnection(false);
        iMapview.setTilesScaledToDpi(true);

        iMapview.setBackgroundColor(BACKGROUND_COLOR);
        iMapview.setScrollableAreaLimit(BOUNDING_BOX);

        iMapview.getOverlays().add(iMyLocationOverlay);
        iMapview.getOverlays().add(iCompassOverlay);
        iMapview.getOverlays().add(iMinimapOverlay);
        iMapview.getOverlays().add(iScaleBarOverlay);

        iMapController = iMapview.getController();
        iMapController.setZoom(iPreferences.getInt(IPREF_ZOOM, DEFAULT_ZOOM));
        iMapController.setCenter(CAMPUS_CENTER);

    }

    /**
     * setUpTile(), crea un nuevo origen para los Tiles o cuadros espec&iacute;ficos del campus.
     *
     * @param resolution La resolución del cuadro, este parametro se puede modificar en la configuración.
     */
//TODO: Hacer que desde la configuración se pueda establecer la resolución del mapa.
    public void setUpTile(int resolution) {
        checkTile();

        String[] LocalHost = new String[2];
        LocalHost[0] = "http://a.127.0.0.1/";
        LocalHost[1] = "http://b.127.0.0.1/";
        tile = new XYTileSource("UisMapsTiles",
                ResourceProxy.string.offline_mode,
                MIN_ZOOM,
                MAX_ZOOM,
                resolution,
                ".png",
                LocalHost);
    }

    /**
     * Desactiva la aceleración por hardware en dispositivos con version de android superior a la 3.0 (HoneyComb)
     * debido a problemas de la API
     */
//TODO: hacerlo seleccionable desde preferencias si presenta errores.
    public void turnOffHardwareAceleration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            iMapview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }


    /**
     * Verifica en @OSMDROID_PATH que exista el @tile del campus, si no existe @ lo descarga desde el servidor.
     * <p/>
     * Nota: Por defecto OsmDroid genera el directorio @OSMDROID_PATH si este no existe.
     */
    public void checkTile() {
        File path = OSMDROID_PATH;
        if (path.isDirectory() && path.length() == 0) {
            Log.i(TAG, "OSMDROID_PATH está vacío");
            //NetController.getTile();
        }
    }
}
