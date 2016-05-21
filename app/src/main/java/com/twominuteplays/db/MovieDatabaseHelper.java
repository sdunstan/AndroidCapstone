package com.twominuteplays.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.twominuteplays.db.MovieContract.*;
import static com.twominuteplays.db.MovieContract.LINE_VIDEO_URI;

public class MovieDatabaseHelper extends SQLiteOpenHelper {

    private static int DB_VERSION = 1;
    private static String DB_NAME = "movies.db";

    public MovieDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createMovieTemplate = "CREATE TABLE " + MOVIE_TEMPLATE + "(" +
                "_ID INTEGER PRIMARY KEY NOT NULL," +
                TITLE + " TEXT NOT NULL," +
                SUMMARY + " TEXT NOT NULL," +
                SCRIPT_MARKUP + " TEXT NOT NULL);";

        String createMovie = "CREATE TABLE " + MOVIE + "(" +
                "_ID INTEGER PRIMARY KEY NOT NULL," +
                TEMPLATE_ID + " INTEGER NOT NULL," +
                CREATE_DATE + " TEXT NOT NULL," +
                STATE + " TEXT NOT NULL," +
                VIDEO_URI + " NOT NULL," +
                "FOREIGN KEY (" + TEMPLATE_ID + ") REFERENCES " + MOVIE_TEMPLATE + "(_ID))";

        String part = "CREATE TABLE " + PART + "(" +
                "_ID INTEGER PRIMARY KEY NOT NULL," +
                TEMPLATE_ID + " INTEGER NOT NULL," +
                NAME + " TEXT NOT NULL," +
                DESCRIPTION + " TEXT NOT NULL," +
                "FOREIGN KEY (" + TEMPLATE_ID + ") REFERENCES " + MOVIE_TEMPLATE + "(_ID))";

        String line = "CREATE TABLE " + LINE + "(" +
                "_ID INTEGER PRIMARY KEY NOT NULL," +
                PART_ID + " INTEGER NOT NULL," +
                SORT_ORDER + " INTEGER NOT NULL," +
                LINE + " TEXT NOT NULL," +
                "FOREIGN KEY (" + PART_ID + ") REFERENCES " + PART + "(_ID))";

        String movieLine = "CREATE TABLE MOVIE_LINE(" +
                MOVIE_ID + " INTEGER NOT NULL," +
                LINE_ID + " INTEGER NOT NULL," +
                LINE_VIDEO_URI + " TEXT NOT NULL," +
                "PRIMARY KEY(" + MOVIE_ID + ", " + LINE_ID + ")," +
                "FOREIGN KEY (" + MOVIE_ID + ") REFERENCES MOVIE(_ID)," +
                "FOREIGN KEY (" + LINE_ID + ") REFERENCES LINE(_ID))";

        sqLiteDatabase.execSQL(createMovieTemplate);
        sqLiteDatabase.execSQL(createMovie);
        sqLiteDatabase.execSQL(part);
        sqLiteDatabase.execSQL(line);
        sqLiteDatabase.execSQL(movieLine);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // Do nothing, this is version 1
    }
}
