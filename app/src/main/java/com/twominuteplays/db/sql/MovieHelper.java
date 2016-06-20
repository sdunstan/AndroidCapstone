package com.twominuteplays.db.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MovieHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "movie.db";

    public static final String[] READ_MOVIE_PROJECTION = new String[] {
            MovieContract.Movie._ID,
            MovieContract.Movie.COLUMN_NAME_MOVIE_ID,
            MovieContract.Movie.COLUMN_NAME_TEMPLATE_ID,
            MovieContract.Movie.COLUMN_NAME_SHARE_ID,
            MovieContract.Movie.COLUMN_NAME_CONTRIBUTOR,
            MovieContract.Movie.COLUMN_NAME_STATE,
            MovieContract.Movie.COLUMN_NAME_TITLE,
            MovieContract.Movie.COLUMN_NAME_SYNOPSIS,
            MovieContract.Movie.COLUMN_NAME_AUTHOR,
            MovieContract.Movie.COLUMN_NAME_SCRIPT_MARKUP,
            MovieContract.Movie.COLUMN_NAME_IMAGE_URL,
            MovieContract.Movie.COLUMN_NAME_MOVIE_URL
    };

    public MovieHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(MovieContract.SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(MovieContract.SQL_DROP_ENTRIES);
        db.execSQL(MovieContract.SQL_CREATE_ENTRIES);
    }
}
