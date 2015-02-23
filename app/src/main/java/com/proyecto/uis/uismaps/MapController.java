package com.proyecto.uis.uismaps;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
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

/**
 * Created by cheloreyes on 19/02/15.
 */
public class MapController extends Fragment implements OpenStreetMapTileProviderConstants{

    // **********************
    // Constants

    private static final int DEFAULT_RESOLUTION = 256;
    private static final int DEFAULT_ZOOM = 18;
    private static final int MIN_ZOOM = 17;
    private static final int MAX_ZOOM = 19;
    private static final int BACKGROUND_COLOR = 15592162; // R:237 G: 234 B: 226
    private static final BoundingBoxE6 BOUNDING_BOX = new BoundingBoxE6(7.13927299476594, -73.1231647642, 7.14257310135635, -73.1161113000236);
    private static final GeoPoint CAMPUS_CENTER = new GeoPoint(7.1410242928925083, -73.11925510433737);
    // **********************
    // Fields
    // **********************

    private Context iContext;
    private MapView iMapview;
    private ResourceProxy ireResourceProxy;
    private DisplayMetrics dm;
    private XYTileSource mapSource;
    private CompassOverlay iCompassOverlay;
    private MinimapOverlay iMinimapOverlay;
    private ScaleBarOverlay iScaleBarOverlay;
    private MyLocationNewOverlay iMyLocationOverlay;


    // **********************
    // Methods
    // **********************
    public static MapController newInstance() {
        MapController controller = new MapController();
        return controller;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ireResourceProxy = new ResourceProxyImpl(inflater.getContext().getApplicationContext());
        iMapview = new MapView(inflater.getContext(), 512, ireResourceProxy);
        return iMapview;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        iContext = this.getActivity();
        init();
    }



    /**
     * Inicializa el mapa junto a los complementos, como brujula, mini mapa, barra de escala
     * y los añade a la vista del mapa.
     */
    public void init() {
        setUpTile(DEFAULT_RESOLUTION);

        dm = iContext.getResources().getDisplayMetrics();

        iCompassOverlay = new CompassOverlay(iContext, new InternalCompassOrientationProvider(iContext), iMapview);
        iCompassOverlay.enableCompass();

        iMyLocationOverlay = new MyLocationNewOverlay(iContext, new GpsMyLocationProvider(iContext), iMapview);
        iMyLocationOverlay.enableMyLocation();
//TODO: hacer que se pueda activar y desactivar el minimapa.
        iMinimapOverlay = new MinimapOverlay(iContext,iMapview.getTileRequestCompleteHandler());
        iMinimapOverlay.setWidth(dm.widthPixels / 5);
        iMinimapOverlay.setHeight(dm.heightPixels / 5);
        iMinimapOverlay.setTileSource(mapSource);
        iMinimapOverlay.setUseDataConnection(false);

        iScaleBarOverlay = new ScaleBarOverlay(iContext);
        iScaleBarOverlay.setCentred(true);
        iScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);

        iMapview.setTileSource(mapSource);
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

        IMapController mapController = iMapview.getController();
        mapController.setZoom(DEFAULT_ZOOM);
        mapController.setCenter(CAMPUS_CENTER);

    }

    /**
     * setUpTile(), crea un nuevo origen para los Tiles o cuadros espec&iacute;ficos del campus.
     *@param resolution La resolución del cuadro, este parametro se puede modificar en la configuración.
     */
    //Todo: Hacer que desde la configuración se pueda establecer la resolución del mapa.
    public void setUpTile(int resolution) {
        String[] LocalHost = new String[2];
        LocalHost[0] = "http://a.127.0.0.1/";
        LocalHost[1] = "http://b.127.0.0.1/";
        mapSource = new XYTileSource("UisMapsTiles",
                ResourceProxy.string.offline_mode,
                MIN_ZOOM,
                MAX_ZOOM,
                resolution,
                ".png",
                LocalHost);
    }



}
