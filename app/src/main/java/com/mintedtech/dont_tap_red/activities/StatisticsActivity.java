package com.mintedtech.dont_tap_red.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mintedtech.dont_tap_red.R;
import com.mintedtech.dont_tap_red.models.DontTapRed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class StatisticsActivity extends AppCompatActivity {

    private TextView tvDataGamesPlayed, tvDataHighestScore, tvDataLowestScore;
    private DontTapRed mCurrentGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        setupToolbar();
        setupFAB();
        setupViews();
        getIncomingData();
        processAndOutputIncomingData();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupFAB() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> onBackPressed());
    }

    private void setupViews() {
        tvDataGamesPlayed = findViewById(R.id.tv_data_games_played);
        tvDataHighestScore = findViewById(R.id.tv_data_highest_score);
        tvDataLowestScore = findViewById(R.id.tv_data_lowest_score);
    }

    private void getIncomingData() {
        Intent intent = getIntent();
        String gameJSON = intent.getStringExtra("GAME");
        mCurrentGame = DontTapRed.getGameFromJSON(gameJSON);
    }

    private void processAndOutputIncomingData() {
        tvDataGamesPlayed.setText(String.valueOf(mCurrentGame.getGamesPlayed()));
        tvDataHighestScore.setText(String.valueOf(mCurrentGame.getHighestScore()));
        tvDataLowestScore.setText(String.valueOf(mCurrentGame.getLowestScore()));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }
}