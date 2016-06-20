package com.twominuteplays.db.sql;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class MovieUpdateDelegate {
    private MovieContentProvider movieContentProvider;
    private MovieHelper movieHelper;

    public MovieUpdateDelegate(MovieContentProvider movieContentProvider, MovieHelper movieHelper) {
        this.movieContentProvider = movieContentProvider;
        this.movieHelper = movieHelper;
    }

    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db  = movieHelper.getWritableDatabase();
        int count;
        switch (MovieContentProvider.uriMatcher.match(uri)) {
            case MovieContentProvider.ALL_MOVIES:
                count = db.update(
                        MovieContract.Movie.TABLE_NAME,
                        values,
                        where,
                        whereArgs
                );
                break;
            case MovieContentProvider.A_MOVIE:
                count = db.update(
                        MovieContract.Movie.TABLE_NAME,
                        values,
                        movieContentProvider.getMovieIdWhere(uri),
                        null
                );
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (movieContentProvider.getContext() != null) {
            movieContentProvider.getContext().getContentResolver().notifyChange(uri, null);
        }

        return count;

    }
}
