package com.example.cs360modulethreeassignment_zachmendiola;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;

// REVIEW [Structure | Databases] DatabaseHelper is the app's entire data layer: one
// SQLiteOpenHelper that creates users.db and exposes login and weight-entry CRUD.
// Every Activity calls it directly - no repository layer between UI and SQL.

// REVIEW [Documentation | Design] No Javadoc on the class or any method.
public class DatabaseHelper extends SQLiteOpenHelper {
    // REVIEW [Variables | Databases] Misleading names. 
    // "users.db" also holds weights
    // "event_table"/"event_name" store weight entries
    // COL_USERNAME maps to "email".
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
        // REVIEW [Defensive Programming | Databases] Password column holds plaintext - no hash/salt.
        // Email is case-sensitive - A@x.com and a@x.com are two accounts
        // Enhancement: integer id PK, UNIQUE email, password_hash, goal_weight column.
        db.execSQL("CREATE TABLE " + USER_TABLE + " (" +
                COL_USERNAME + " TEXT PRIMARY KEY, " +
                COL_PASSWORD + " TEXT)");

        // Create event table
        // REVIEW [Structure | Databases] No user_id foreign key - every account shares entries.
        // [Variables] Weight is TEXT, not REAL. 
        // [Defensive Programming] No NOT NULL, no CHECK weight > 0, no UNIQUE (user, date), and no index on date.

        // REVIEW [Variables | Algorithms] Dates stored as free-form TEXT sort as strings, not chronologically. 
        // Enhancement: ISO-8601 dates so ORDER BY works.
        db.execSQL("CREATE TABLE " + EVENT_TABLE + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_EVENT_NAME + " TEXT, " +
                COL_EVENT_DATE + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // REVIEW [Defensive Programming | Databases] Leaves the file in a bad state.
        // Any version bump deletes every user and entry. 
        // Enhancement: a v1 to v2 migration that copies data.
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + EVENT_TABLE);
        onCreate(db);
    }

    public boolean registerUser(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, email);
        // REVIEW [Defensive Programming | Databases] Raw password written to disk. 
        // Anyone with the db file can read every password.
        // Enhancement: PBKDF2 hash
        values.put(COL_PASSWORD, password);
        long result = db.insert(USER_TABLE, null, values);
        // REVIEW [Defensive Programming | Databases] No error trap: failure becomes false, never logged.
        return result != -1;
    }
    // REVIEW [Defensive Programming | Databases] This only ever compares plaintext passwords.
    // Enhancement: load the stored hashre-hash the input, and compare in constant time.
    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        // REVIEW [Defensive Programming | Databases] Cursor is never deallocated, leaks on every login. 
        // [Structure] SELECT * is inefficient, all that is needed is a count.
        Cursor cursor = db.rawQuery("SELECT * FROM " + USER_TABLE +
                " WHERE email = ? AND password = ?", new String[]{email, password});
        return cursor.getCount() > 0;
    }
    public boolean userExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        // REVIEW [Structure | Databases] Near-duplicate of checkUser, repeated code, same Cursor leak.
        Cursor cursor = db.rawQuery("SELECT * FROM " + USER_TABLE +
                " WHERE email = ?", new String[]{email});
        return cursor.getCount() > 0;
    }

    // REVIEW [Structure | Databases] Entry methods take no user id.
    // They can't be scoped to the signed-in user. 
    // Enhancement: every query filters by user_id from the session.
    public boolean insertEvent(String name, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_EVENT_NAME, name);
        cv.put(COL_EVENT_DATE, date);
        long result = db.insert(EVENT_TABLE, null, cv);
        return result != -1;
    }
    // REVIEW [Structure | Algorithms] No ORDER BY, so entries come back in insertion order.
    // A raw Cursor also pushes column index parsing into the UI.
    // Enhancement: return a sorted List<WeightEntry>.
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
