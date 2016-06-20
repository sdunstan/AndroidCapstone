package com.twominuteplays;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.twominuteplays.db.ShareUtility;
import com.twominuteplays.db.sql.MovieContract;
import com.twominuteplays.db.sql.MovieHelper;
import com.twominuteplays.model.Movie;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = MainActivity.class.getName();
    private static final int MOVIE_LOADER = 31415;
    private MyMoviesAdapter moviesAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent scriptPickerIntent = new Intent(MainActivity.this, ScriptPickerActivity.class);
                    startActivity(scriptPickerIntent);
                }
            });
        }

        recyclerView = (RecyclerView) findViewById(R.id.myScripts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        moviesAdapter = new MyMoviesAdapter();
        recyclerView.setAdapter(moviesAdapter);

        getLoaderManager().initLoader(MOVIE_LOADER, null, this);

        IntentFilter shareIntentFilter = new IntentFilter(Movie.MOVIE_SHARE);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        MainActivity.this.onShare((Movie) intent.getParcelableExtra(Movie.MOVIE_EXTRA));
                    }
                },
                shareIntentFilter
        );
    }

    @Override
    public void postLoginCreate() {
        ShareUtility.registerShareListeners(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (moviesAdapter != null) {
            moviesAdapter.cleanup();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Log.i(TAG, "Clicked option item " + id + " logout is " + R.id.action_logout);
        if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onShare(Movie movie) {
        if (movie == null)
            return;
        Log.i(TAG, "Clicked share for " + movie.getTitle());
        ShareUtility.shareMovie(this, movie);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        if(id == MOVIE_LOADER) {
            return new CursorLoader(
                    MainActivity.this,
                    MovieContract.Movie.MOVIE_URI,
                    MovieHelper.READ_MOVIE_PROJECTION,
                    null,
                    null,
                    null
            );
        }
        else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        moviesAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        moviesAdapter.swapCursor(null);
    }

}
