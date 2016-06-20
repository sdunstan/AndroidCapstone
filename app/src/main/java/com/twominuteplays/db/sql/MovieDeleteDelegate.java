package com.twominuteplays.db.sql;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class MovieDeleteDelegate {
    private MovieContentProvider movieContentProvider;
    private MovieHelper movieHelper;

    public MovieDeleteDelegate(MovieContentProvider movieContentProvider, MovieHelper movieHelper) {
        this.movieContentProvider = movieContentProvider;
        this.movieHelper = movieHelper;
    }

    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db  = movieHelper.getWritableDatabase();
        int count;
        switch (MovieContentProvider.uriMatcher.match(uri)) {
            case MovieContentProvider.ALL_MOVIES:
                count = db.delete(
                        MovieContract.Movie.TABLE_NAME,
                        where,
                        whereArgs
                );
                break;
            case MovieContentProvider.A_MOVIE:
                count = db.delete(
                        MovieContract.Movie.TABLE_NAME,
                        movieContentProvider.getMovieIdWhere(uri),
                        null
                );
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if(movieContentProvider.getContext() != null) {
            movieContentProvider.getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }
}
