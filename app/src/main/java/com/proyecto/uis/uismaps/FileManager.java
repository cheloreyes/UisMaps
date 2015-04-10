package com.proyecto.uis.uismaps;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by CheloReyes on 30/03/15.
 */
public class FileManager implements Constants{

    private static final String TAG = "FileManager";
    private Context iContext;

    public FileManager(Context context) {
        iContext = context;
    }

    /**
     * Comprueba que existan las carpetas y archivos en la memoria SD del dispositivo.
     */
    public void folderCheck() {
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
        else {
            if(needUpdate(f)) {
                assetCopy("mapa");
                Log.i(TAG, "Se actualiza Mapa");
            }
            else Log.i(TAG, "No necesita actualizar mapa");
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
        f = new File(DB_PATH + DB_NAME);
        if (!f.exists()) {
            assetCopy("database");
            Log.i(TAG, "archivo de database no encontrado, se compia de asset");
        }
        else {
            if(needUpdate(f)) {
                assetCopy("database");
                Log.i(TAG, "Se actualiza base de datos");
            }
            else Log.i(TAG, "No necesita actualizar base de datos");
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
        AssetManager assetManager = iContext.getResources().getAssets();
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

    private boolean needUpdate(File file) {
        SharedPreferences preferences = iContext.getSharedPreferences(ACT_OPTIONS, Context.MODE_PRIVATE);
        boolean update = false;
        Log.i(TAG, "Version mapa actual: "+ preferences.getFloat(MAP_VERSION, 0.0f) + " Nueva:" + iContext.getString(R.string.map_version));
        if(Float.parseFloat(iContext.getString(R.string.map_version)) > preferences.getFloat(MAP_VERSION, 0.0f)) {
            update = true;
            file.delete();
            Log.i(TAG, "Necesita actualizar " + CAMPUS_MAP);
        }
        if(Float.parseFloat(iContext.getString(R.string.db_version)) > preferences.getFloat(DB_VERSION, 0.0f)) {
            update = true;
            Log.i(TAG, "Necesita actualizar " + DB_PATH + DB_NAME);
            file.delete();
        }
        return update;
    }
}
