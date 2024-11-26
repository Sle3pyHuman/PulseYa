package com.examen.pulseya.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BaseDatosHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "pulseya.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLA_USUARIO = "usuario";
    private static final String COL_ID = "id";
    private static final String COL_NOMBRE = "nombre";
    private static final String COL_CORREO = "correo";
    private static final String COL_CONTRASENA = "contrasena";
    private static final String COL_TIPO_CUENTA = "tipo_cuenta";

    public BaseDatosHelper (Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQLUsaurio = "CREATE TABLE " + TABLA_USUARIO + "("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_NOMBRE + " TEXT,"
                + COL_CORREO + " TEXT,"
                + COL_CONTRASENA + " TEXT,"
                + COL_TIPO_CUENTA + " TEXT"
                + ")";
        db.execSQL(SQLUsaurio);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_USUARIO);
        onCreate(db);
    }

    public void guardarUsuario(String nombre, String correo, String contrasena, String tipoCuenta) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put(COL_NOMBRE, nombre);
        valores.put(COL_CORREO, correo);
        valores.put(COL_CONTRASENA, contrasena);
        valores.put(COL_TIPO_CUENTA, tipoCuenta);
        db.insert(TABLA_USUARIO, null, valores);
        db.close();
    }

    public Cursor getUsuario() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLA_USUARIO, null);
    }

    public void eliminarUsuario() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLA_USUARIO, null, null);
        db.close();
    }
}
