package com.proyecto.uis.uismaps;

/**
 * Created by cheloreyes on 9/03/15.
 * Esta clase contiene las constantes de los ajustes de la app
 */
public interface UISMapsSettingsValues {

    public static final int END_ROUTE_BUTTON_ID = 3;
    public static final int FIND_ME_BUTTON_ID = 1;
    public static final int START_ROUTE_BUTTON_ID = 2;

    /**
     * Id para los valores auto generados al dejar y usados al retomar la aplicación.
     */
    public static final String ACT_OPTIONS = "UisMapPreferencias";
    public static final String MAP_ESCALE = "last_scale";
    public static final String MAP_LAT = "last_lat";
    public static final String MAP_LONG = "last_lon";
    public static final String MAP_ROTATION = "last_rotation";
    public static final String MAP_UNIT = "display_units";

    /**
     * Id para los valores modificados por el usuario en los ajustes de la aplicación.
     */
    public static final String EYESIGHT_ASSISTANT= "voice_capabilities";
}
