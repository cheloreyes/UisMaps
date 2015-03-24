package com.proyecto.uis.uismaps.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.proyecto.uis.uismaps.Constants;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;

/**
 * La clase DBHelper realiza
 * Created by CheloReyes on 24/03/15.
 */
public class DBHelper extends SQLiteOpenHelper implements Constants {
    // **********************
    // Constants
    // **********************
    private static int DB_VERSION = 1;
    private static String DB_PATH = UIS_MAPS_FOLDER + "/database";
    private static String DB_NAME = "uis_maps";

    // **********************
    // Fields
    // **********************
    private Context iContext;
    private SQLiteDatabase iDataBase;


    // **********************
    // Constructor
    // **********************

    /**
     * Toma y guarda una referencia del contexto utilizado con el fin de acceder a los recursos de la aplicación.
     * @param context
     */
    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        iContext = context;
    }

    // **********************
    // Methods
    // **********************

    /**
     * Crea una base de datos vacía en el sistema y la reescribe con nuestra base de datos.
     * @throws IOException
     */
    public void createDataBase() throws IOException {
        boolean dbExist = checkDataBase();
        if(!dbExist) {
            getReadableDatabase();
            try {
                copyDatabase();
            } catch (IOException e) {
                Log.v("DBHelper", "Error copiando la base de datos");
            }
        }
    }

    /**
     * Comprueba si la base de datos ya existe para evitar copiar el archivo cada vez que se inicie la app.
     * @return true si existe ya la base de datos.
     */
    private boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        try {
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            //No existe la BD aún.
        }
        if(checkDB != null) {
            checkDB.close();
        }
        return checkDB != null ? true : false;
    }

    /**
     * Copia nuestra base de datos desde la carpeta "assets" del paquete de aplicación a la base de datos creada
     * por @createDataBase en la carpeta del sistema, desde donde se puede acceder y manipular.
     * @throws IOException
     */
    private void copyDatabase() throws IOException{
        InputStream inputStream = iContext.getAssets().open("database/" + DB_NAME);
        String outFile = DB_PATH + DB_NAME;
        OutputStream outputStream = new FileOutputStream(outFile);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0){
            outputStream.write(buffer, 0, length);
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();
    }

    public void openDataBase() throws SQLException {
        String myPath = DB_PATH + DB_NAME;
        iDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    }

    // **********************
    // Methods from super class
    // **********************
    @Override
    public synchronized void close() {
        if(iDataBase != null){
            iDataBase.close();
        }
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
