package com.twominuteplays.db.sql;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

public class MovieContentProvider extends ContentProvider {
    public static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static final int ALL_MOVIES = 1;
    public static final int A_MOVIE = 2;

    static {
        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.Movie.TABLE_NAME, ALL_MOVIES);
        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.Movie.TABLE_NAME+"/#", A_MOVIE);
    }

    private MovieHelper movieHelper;

    // Use these delegates instead of creating one giant class. Makes it
    // easier to read and test.
    private MovieDeleteDelegate movieDeleteDelegate;
    private MovieInsertDelegate movieInsertDelegate;
    private MovieUpdateDelegate movieUpdateDelegate;
    private MovieQueryDelegate movieQueryDelegate;


    public MovieContentProvider() {
    }

    @Override
    public boolean onCreate() {
        movieHelper = new MovieHelper(getContext());

        movieDeleteDelegate = new MovieDeleteDelegate(this, movieHelper);
        movieInsertDelegate = new MovieInsertDelegate(this, movieHelper);
        movieUpdateDelegate = new MovieUpdateDelegate(this, movieHelper);
        movieQueryDelegate = new MovieQueryDelegate(this, movieHelper);

        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case ALL_MOVIES:
                return MovieContract.Movie.CONTENT_TYPE;
            case A_MOVIE:
                return MovieContract.Movie.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    public String getMovieIdWhere(Uri uri) {
        return MovieContract.Movie._ID+"="+uri.getPathSegments().get(1);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        return movieDeleteDelegate.delete(uri, where, whereArgs);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return movieInsertDelegate.insert(uri, values);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        return movieQueryDelegate.query(uri, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public int update(Uri uri, ContentValues values, String where,
                      String[] whereArgs) {
        return movieUpdateDelegate.update(uri, values, where, whereArgs);
    }
}
