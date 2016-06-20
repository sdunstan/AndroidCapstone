package com.twominuteplays;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import java.io.File;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link MyMovieWidgetConfigureActivity MyMovieWidgetConfigureActivity}
 */
public class MyMovieWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        String backgroundImagePath = MyMovieWidgetConfigureActivity.loadImagePref(context, appWidgetId);
        String movieId = MyMovieWidgetConfigureActivity.loadMovieIdPref(context, appWidgetId);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.my_movie_widget);

        if(backgroundImagePath != null) {
            Uri imageUri = Uri.fromFile(new File(backgroundImagePath));
            views.setImageViewUri(R.id.imageButton, imageUri);
        }
        else {
            views.setImageViewResource(R.id.imageButton, R.mipmap.card_bg);
        }

        if (movieId != null) {
            Intent intent = new Intent(context, PlayViewActivity.class);
            intent.putExtra("MOVIE_ID", movieId);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.imageButton, pendingIntent);
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            MyMovieWidgetConfigureActivity.deleteMovieIdPref(context, appWidgetId);
            MyMovieWidgetConfigureActivity.deleteImagePref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

