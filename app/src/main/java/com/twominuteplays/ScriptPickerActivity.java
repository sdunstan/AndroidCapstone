package com.twominuteplays;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.twominuteplays.firebase.ClickableMovieTemplateAdapter;

public class ScriptPickerActivity extends BaseActivity {

    private ClickableMovieTemplateAdapter moviesAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_script_picker);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Script Picker");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.allScripts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void postLoginCreate() {
        moviesAdapter = new ClickableMovieTemplateAdapter(mFirebaseRef.child("templates"));
        recyclerView.setAdapter(moviesAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (moviesAdapter != null) {
            moviesAdapter.cleanup();
        }
    }

}
