package com.proyecto.uis.uismaps.db;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.proyecto.uis.uismaps.Constants;
import com.proyecto.uis.uismaps.finder.Spaces;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * La clase DBHelper realiza
 * Created by CheloReyes on 24/03/15.
 */
public class DBHelper extends SQLiteOpenHelper implements Constants {
    // **********************
    // Constants
    // **********************
    private static int DB_VERSION = 1;
    private static String DB_PATH = UIS_MAPS_FOLDER + "/database/";
    private static String DB_NAME = "uis_maps.sqlite";

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
        Log.v("DBHelper", "Crea base de datos");
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
            return false;
        }
        if(checkDB != null) {
            checkDB.close();
            return true;
        }
        return checkDB != null ? true : false;
    }

    /**
     * Copia nuestra base de datos desde la carpeta "assets" del paquete de aplicación a la base de datos creada
     * por @createDataBase en la carpeta del sistema, desde donde se puede acceder y manipular.
     * @throws IOException
     */
    private void copyDatabase() throws IOException{
        File sdCardDir = new File(DB_PATH);
        if (!sdCardDir.exists()) {
            sdCardDir.mkdirs();
        }
        AssetManager am = iContext.getResources().getAssets();
        Log.v("DBHelper", "Copia la base de datos de assets");
        try{
            InputStream inputStream = am.open("database/" + DB_NAME );
            //inputStream = iContext.getAssets().open("database/" + DB_NAME);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openDataBase() throws SQLException {
        String myPath = DB_PATH + DB_NAME;
        iDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    }

    public List<Spaces> getSpaces(String query, int limit) {
        Cursor c = iDataBase.rawQuery("SELECT * FROM Spaces WHERE OfficeName LIKE '%" + query + "%' ORDER BY OfficeNumOfc LIMIT " + limit, null);
        Log.v("DB", "Buscando en la BD: "+query);
        return cursorToList(c);
    }

    public List<Spaces> getDependences(String query) {
        Cursor c = iDataBase.rawQuery("SELECT * FROM Spaces WHERE EdificeName LIKE '%" + query + "%' AND OfficeNumOfc IS NOT NULL ORDER BY OfficeNumOfc", null);
        Log.v("DB", "Buscando en la BD: "+query);
        return cursorToList(c);
    }
    public String getUrlImg(String query) {
        String url = null;
        Cursor c = iDataBase.rawQuery("SELECT ImageUrl FROM Edifice WHERE EdificeName IS '" + query +"'", null);
        try{
            url = c.getString(0);
        }
        catch (NullPointerException noUrl){
            url = "localHost";
        }
        return url;
    }

    private List<Spaces> cursorToList(Cursor cursor) {
        List<Spaces> tempList = new ArrayList<>();
        while (cursor.moveToNext()) {
            Spaces spaces = new Spaces();
            spaces.setName(cursor.getString(0));
            spaces.setBuilding(cursor.getString(1));
            spaces.setOffice(cursor.getString(2));
            spaces.setUaa(cursor.getString(3));
            tempList.add(spaces);
        }
        return tempList;
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
