package com.proyecto.uis.uismaps.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.proyecto.uis.uismaps.Constants;
import com.proyecto.uis.uismaps.FileManager;
import com.proyecto.uis.uismaps.finder.Spaces;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * La clase DBHelper permite consultar la base de datos local SQLite la cual contiene toda la información
 * relacionada a los edificios del campus universitario como son las oficinas, aulas, laboratorios, talleres,
 * auditorios, secretarías y demás entidades presentes en el campus principal de la universidad.
 *
 * Created by CheloReyes on 24/03/15.
 */
public class DBHelper extends SQLiteOpenHelper implements Constants {
    // **********************
    // Constants
    // **********************
    private static int DB_VERSION = 1;


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
        if(!dataBaseExist()) {
            FileManager fileManager = new FileManager(iContext);
            fileManager.folderCheck();
            getReadableDatabase();
        }
    }

    /**
     * Comprueba si la base de datos ya existe para evitar copiar el archivo cada vez que se inicie la app.
     * @return true si existe ya la base de datos.
     */
    private boolean dataBaseExist() {
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
     * Abre la base de datos para ser utilizada.
     * @throws SQLException
     */
    public void openDataBase() throws SQLException {
        String myPath = DB_PATH + DB_NAME;
        iDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    }

    /**
     * Obtiene los posibles espacios requeridos.
     * @param query sentencia a buscar.
     * @param limit máximo de resultados.
     * @return Lista de tipo @Spaces.
     */
    public List<Spaces> getSpaces(String query, int limit) {
        Cursor c = iDataBase.rawQuery("SELECT * FROM Spaces WHERE SpacesName LIKE '%" + query + "%' ORDER BY SpacesOfcNum LIMIT " + limit, null);
        Log.v("DB", "Buscando en la BD: "+query);
        return cursorToList(c);
    }

    /**
     * Obtiene las dependencias del edificio requerido.
     * @param query Nombre del edificio.
     * @return Lista de tipo @Spaces.
     */
    public List<Spaces> getDependences(String query) {
        Cursor c = iDataBase.rawQuery("SELECT * FROM Spaces WHERE EdificeName LIKE '%" + query + "%' AND SpacesOfcNum IS NOT NULL ORDER BY SpacesOfcNum", null);
        Log.v("DB", "Buscando en la BD: "+query);
        return cursorToList(c);
    }

    /**
     * Obtiene todos los edificios del campus universitario.
     * @return Arreglo de tipo String.
     */
    public String[] getBuildingsList() {
        Cursor c = iDataBase.rawQuery("SELECT EdificeName FROM Edifice", null);
        String[] toReturn = new String[c.getCount()];
       int i = 0;
        while (c.moveToNext()) {
            toReturn[i] = c.getString(0);
            i++;
        }
        return toReturn;
    }

    /**
     * Almacena el resutlado de la consulta a la base de datos que es de tipo @Cursor en una @List de tipo @Spaces
     * @param cursor Lista de tipo @Spaces.
     * @return
     */
    private List<Spaces> cursorToList(Cursor cursor) {
        List<Spaces> tempList = new ArrayList<>();
        if(cursor != null) {
            while (cursor.moveToNext()) {
                Spaces spaces = new Spaces();
                spaces.setName(cursor.getString(0));
                spaces.setBuilding(cursor.getString(1));
                spaces.setOffice(cursor.getString(2));
                spaces.setUaa(cursor.getString(3));
                tempList.add(spaces);
            }
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
