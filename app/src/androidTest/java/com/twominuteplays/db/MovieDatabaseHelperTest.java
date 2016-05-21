package com.twominuteplays.db;

import android.test.AndroidTestCase;

public class MovieDatabaseHelperTest extends AndroidTestCase {

    public void testOnCreate() throws Exception {
        MovieDatabaseHelper movieDatabaseHelper = new MovieDatabaseHelper(mContext);
        movieDatabaseHelper.onCreate(movieDatabaseHelper.getWritableDatabase());
    }
}