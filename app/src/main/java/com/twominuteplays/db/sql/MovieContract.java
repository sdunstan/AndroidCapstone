package com.twominuteplays.db.sql;

import android.net.Uri;
import android.provider.BaseColumns;

public class MovieContract {
    public static final String AUTHORITY = "com.twominuteplays.movie";

    private MovieContract() {}

    public static abstract class Movie implements BaseColumns {
        public static final String TABLE_NAME = "movie";
        public static final String COLUMN_NAME_MOVIE_ID = "movieId";
        public static final String COLUMN_NAME_TEMPLATE_ID = "templateId";
        public static final String COLUMN_NAME_SHARE_ID = "shareId";
        public static final String COLUMN_NAME_CONTRIBUTOR = "contributor";
        public static final String COLUMN_NAME_STATE = "movieState";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_SYNOPSIS = "synopsis";
        public static final String COLUMN_NAME_AUTHOR = "author";
        public static final String COLUMN_NAME_SCRIPT_MARKUP = "scriptMarkup";
        public static final String COLUMN_NAME_IMAGE_URL = "imageUrl";
        public static final String COLUMN_NAME_MOVIE_URL = "movieUrl";

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.twominuteplays.movie";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.twominuteplays.movie";

        public static final Uri MOVIE_URI = Uri.parse("content://" + AUTHORITY + "/movie/");
    }

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + Movie.TABLE_NAME + " (" +
                    Movie._ID + " INTEGER PRIMARY KEY, " +
                    Movie.COLUMN_NAME_MOVIE_ID + " TEXT NOT NULL UNIQUE, " +
                    Movie.COLUMN_NAME_TEMPLATE_ID + " TEXT NOT NULL, " +
                    Movie.COLUMN_NAME_SHARE_ID + " INTEGER, " +
                    Movie.COLUMN_NAME_CONTRIBUTOR + " TEXT, " +
                    Movie.COLUMN_NAME_STATE + " TEXT NOT NULL, " +
                    Movie.COLUMN_NAME_TITLE + " TEXT NOT NULL, " +
                    Movie.COLUMN_NAME_SYNOPSIS + " TEXT NOT NULL, " +
                    Movie.COLUMN_NAME_AUTHOR + " TEXT, " +
                    Movie.COLUMN_NAME_SCRIPT_MARKUP + " TEXT, " +
                    Movie.COLUMN_NAME_IMAGE_URL + " TEXT, " +
                    Movie.COLUMN_NAME_MOVIE_URL + " TEXT)";

    public static final String SQL_DROP_ENTRIES =
            "DROP TABLE " + Movie.TABLE_NAME + ";";

}
