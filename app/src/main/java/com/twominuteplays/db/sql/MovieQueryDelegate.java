package com.twominuteplays.db.sql;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

public class MovieQueryDelegate {
    private static final Map<String,String> moviesProjectionMap;
    static {
        moviesProjectionMap = new HashMap<>();
        for(String column : MovieHelper.READ_MOVIE_PROJECTION) {
            moviesProjectionMap.put(column,column);
        }
    }



    private MovieContentProvider movieContentProvider;
    private MovieHelper movieHelper;

    public MovieQueryDelegate(MovieContentProvider movieContentProvider, MovieHelper movieHelper) {
        this.movieContentProvider = movieContentProvider;
        this.movieHelper = movieHelper;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String orderBy;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(MovieContract.Movie.TABLE_NAME);
        qb.setProjectionMap(moviesProjectionMap);
        switch (MovieContentProvider.uriMatcher.match(uri)) {
            case MovieContentProvider.ALL_MOVIES:
                orderBy = TextUtils.isEmpty(sortOrder) ? MovieContract.Movie._ID : sortOrder;
                break;
            case MovieContentProvider.A_MOVIE:
                qb.appendWhere(movieContentProvider.getMovieIdWhere(uri));
                orderBy = sortOrder;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase db = movieHelper.getReadableDatabase();
        Cursor c = qb.query(
                db,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                orderBy);

        if (movieContentProvider.getContext() != null) {
            c.setNotificationUri(movieContentProvider.getContext().getContentResolver(), uri);
        }

        return c;
    }
}
