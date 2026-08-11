package com.mintedtech.dont_tap_red.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mintedtech.dont_tap_red.R;
import com.mintedtech.dont_tap_red.models.DontTapRed;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class StatisticsActivity extends AppCompatActivity {

    private TextView tvDataGamesPlayed,
            tvDataWins, tvDataWinsPercent,
            tvDataLosses, tvDataLossesPercent;

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
        tvDataWins = findViewById(R.id.tv_data_won_count);
        tvDataWinsPercent = findViewById(R.id.tv_data_won_percent);
        tvDataLosses = findViewById(R.id.tv_data_lost_count);
        tvDataLossesPercent = findViewById(R.id.tv_data_lost_percent);
    }

    private void getIncomingData() {
        Intent intent = getIntent();
        String gameJSON = intent.getStringExtra("GAME");
        mCurrentGame = DontTapRed.getGameFromJSON(gameJSON);
    }

    private void processAndOutputIncomingData() {
        final String FORMAT_STRING = "%2.1f%%", N_A = "N/A";
        int numberOfGamesPlayed = mCurrentGame.getGamesPlayed();
        int wins = mCurrentGame.getWins();
        int losses = mCurrentGame.getLosses();
        
        String winPct = numberOfGamesPlayed == 0 ? N_A :
                String.format(Locale.US, FORMAT_STRING, (wins / (double) numberOfGamesPlayed) * 100);
        String lossPct = numberOfGamesPlayed == 0 ? N_A :
                String.format(Locale.US, FORMAT_STRING, (losses / (double) numberOfGamesPlayed) * 100);
        
        tvDataGamesPlayed.setText(String.valueOf(numberOfGamesPlayed));
        tvDataWins.setText(String.valueOf(wins));
        tvDataLosses.setText(String.valueOf(losses));
        tvDataWinsPercent.setText(winPct);
        tvDataLossesPercent.setText(lossPct);
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