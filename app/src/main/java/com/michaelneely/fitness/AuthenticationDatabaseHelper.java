package com.michaelneely.fitness;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AuthenticationDatabaseHelper extends SQLiteOpenHelper {
    public static final String USERNAME_COLUMN = "USERNAME";
    public static final String PASSWORD_COLUMN = "PASSWORD";
    public static final String DATABASE_NAME = "user.db";
    public static final String TABLE_NAME = "user_table";


    public AuthenticationDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " (USERNAME TEXT PRIMARY KEY, PASSWORD TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean createAccount(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(USERNAME_COLUMN, username);
        cv.put(PASSWORD_COLUMN, password);
        long result = db.insert(TABLE_NAME, null, cv);
        if (result == -1) {
            return false;
        }
        return true;
    }

    public boolean checkAccount(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor check = db.rawQuery("select * from " + TABLE_NAME + " where USERNAME = ?", new String[]{username});
        check.moveToFirst();
        if (check.getCount() == 0) {
            return false;
        } else if (!check.getString(1).equals(password)) {
            return false;
        }
        return true;
    }

    public boolean isAccountFoundInTheDatabase(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor check = db.rawQuery("select * from " + TABLE_NAME + " where USERNAME = ?", new String[]{username});
        if (check.getCount() == 0) {
            return false;
        }
        return true;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME, null);
        return res;
    }
}
