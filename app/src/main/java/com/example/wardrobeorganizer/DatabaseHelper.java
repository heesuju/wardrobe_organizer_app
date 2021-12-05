package com.example.wardrobeorganizer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "wardrobe.db";
    private static final int DATABASE_VERSION = 3;

    public DatabaseHelper (Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE wardrobe (_id INTEGER PRIMARY KEY" +
                " Autoincrement, category TEXT, material TEXT, brand TEXT, state TEXT, image TEXT, records TEXT);");
    }

    public void onUpgrade(SQLiteDatabase db, int ondVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS wardrobe");
        onCreate(db);
    }
}