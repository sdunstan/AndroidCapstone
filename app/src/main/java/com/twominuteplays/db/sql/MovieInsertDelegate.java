package com.twominuteplays.db.sql;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class MovieInsertDelegate {
    private MovieContentProvider movieContentProvider;
    private MovieHelper movieHelper;

    public MovieInsertDelegate(MovieContentProvider movieContentProvider, MovieHelper movieHelper) {
        this.movieContentProvider = movieContentProvider;
        this.movieHelper = movieHelper;
    }

    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db  = movieHelper.getWritableDatabase();
        if(MovieContentProvider.uriMatcher.match(uri) != MovieContentProvider.ALL_MOVIES) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        long rowId = db.insert(
                MovieContract.Movie.TABLE_NAME,
                MovieContract.Movie.COLUMN_NAME_TITLE,
                values
        );

        if (rowId > 0) {
            Uri movieUri = ContentUris.withAppendedId(MovieContract.Movie.MOVIE_URI, rowId);
            if (movieContentProvider.getContext() != null) {
                movieContentProvider.getContext()
                        .getContentResolver()
                        .notifyChange(movieUri, null);
            }
            return movieUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }
}
