package com.example.cs360modulethreeassignment_zachmendiola;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "users.db";
    public static final int DB_VERSION = 1;
    public static final String USER_TABLE = "users";
    public static final String COL_USERNAME = "email";
    public static final String COL_PASSWORD = "password";
    public static final String EVENT_TABLE = "event_table";
    public static final String COL_ID = "ID";
    public static final String COL_EVENT_NAME = "event_name";
    public static final String COL_EVENT_DATE = "event_date";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users table
        db.execSQL("CREATE TABLE " + USER_TABLE + " (" +
                COL_USERNAME + " TEXT PRIMARY KEY, " +
                COL_PASSWORD + " TEXT)");

        // Create event table
        db.execSQL("CREATE TABLE " + EVENT_TABLE + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_EVENT_NAME + " TEXT, " +
                COL_EVENT_DATE + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + EVENT_TABLE);
        onCreate(db);
    }

    public boolean registerUser(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, email);
        values.put(COL_PASSWORD, password);
        long result = db.insert(USER_TABLE, null, values);
        return result != -1;
    }
    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + USER_TABLE +
                " WHERE email = ? AND password = ?", new String[]{email, password});
        return cursor.getCount() > 0;
    }
    public boolean userExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + USER_TABLE +
                " WHERE email = ?", new String[]{email});
        return cursor.getCount() > 0;
    }

    public boolean insertEvent(String name, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_EVENT_NAME, name);
        cv.put(COL_EVENT_DATE, date);
        long result = db.insert(EVENT_TABLE, null, cv);
        return result != -1;
    }
    public Cursor getAllEvents() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + EVENT_TABLE, null);
    }
    public boolean deleteEvent(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(EVENT_TABLE, COL_ID + "=?", new String[]{String.valueOf(id)});
        return rows > 0;
    }
    public boolean updateEvent(int id, String name, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_EVENT_NAME, name);
        cv.put(COL_EVENT_DATE, date);
        int result = db.update(EVENT_TABLE, cv, COL_ID + "=?", new String[]{String.valueOf(id)});
        return result > 0;
    }
}
