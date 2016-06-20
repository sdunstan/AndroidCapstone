package com.twominuteplays;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.twominuteplays.firebase.MovieListAdapter;
import com.twominuteplays.model.Movie;

/**
 * The configuration screen for the {@link MyMovieWidget MyMovieWidget} AppWidget.
 */
public class MyMovieWidgetConfigureActivity extends BaseActivity {

    private static final String PREFS_NAME = "com.twominuteplays.MyMovieWidget";
    private static final String PREF_MOVIE_PREFIX_KEY = "appwidget_movie_";
    private static final String PREF_IMAGE_PREFIX_KEY = "appwidget_image_";

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private GridView myMoviesCollectionView;

    public MyMovieWidgetConfigureActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.my_movie_widget_configure);
        myMoviesCollectionView = (GridView) findViewById(R.id.myScripts);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }
    }

    @Override
    public void postLoginCreate() {
        ListAdapter adapter = new MovieListAdapter(this, android.R.layout.simple_list_item_1) {
            @Override
            protected void populateView(View view, Movie movie, int position) {
                ((TextView)view.findViewById(android.R.id.text1)).setText(movie.getTitle());
            }
        };
        myMoviesCollectionView.setAdapter(adapter);
        myMoviesCollectionView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Movie movie = (Movie) adapterView.getItemAtPosition(i);
                final Context context = MyMovieWidgetConfigureActivity.this;

                saveImagePref(context, mAppWidgetId, movie.getImageUrl());
                saveMovieIdPref(context, mAppWidgetId, movie.getId());

                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                MyMovieWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

                // Make sure we pass back the original appWidgetId
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }
        });
    }

    private void saveMovieIdPref(Context context, int appWidgetId, String movieId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_MOVIE_PREFIX_KEY + appWidgetId, movieId);
        prefs.apply();
    }


    public static String loadMovieIdPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString(PREF_MOVIE_PREFIX_KEY + appWidgetId, null);
    }

    static void deleteMovieIdPref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_MOVIE_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    private void saveImagePref(Context context, int appWidgetId, String imageUrl) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_IMAGE_PREFIX_KEY + appWidgetId, imageUrl);
        prefs.apply();
    }

    public static String loadImagePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString(PREF_IMAGE_PREFIX_KEY + appWidgetId, null); // TODO: load default image
    }

    static void deleteImagePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_IMAGE_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }


}

