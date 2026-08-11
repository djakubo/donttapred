package com.mintedtech.dont_tap_red.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mintedtech.dont_tap_red.R;
import com.mintedtech.dont_tap_red.classes.CardViewImageAdapter;
import com.mintedtech.dont_tap_red.classes.Utils;
import com.mintedtech.dont_tap_red.models.DontTapRed;
import com.mintedtech.dont_tap_red.interfaces.OnItemClickCustomListener;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_GAME_JSON = "game_json";
    private static final String KEY_GAME_STARTED = "game_started";
    private static final String KEY_CURRENT_STAY = "current_stay";
    private static final String KEY_TURN_START_TIME = "turn_start_time";

    private DontTapRed mGame;
    private CardViewImageAdapter mAdapter;
    private TextView mScoreView;
    private RecyclerView mRecyclerView;
    
    private TextView mTimerView;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Runnable mGameLoop = this::handleGameOver;
    private long mCurrentStay;
    private long mTurnStartTime;
    private boolean mGameStarted;

    private final Runnable mUpdateTimerRunnable = new Runnable() {
        @Override
        public void run() {
            long remaining = mCurrentStay - (System.currentTimeMillis() - mTurnStartTime);
            if (remaining < 0) remaining = 0;
            if (mTimerView != null) {
                mTimerView.setText(getString(R.string.timer_format, (int) Math.ceil(remaining / 1000.0)));
            }
            if (remaining > 0) {
                mHandler.postDelayed(this, 100);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mScoreView = findViewById(R.id.tv_status);
        mTimerView = findViewById(R.id.tv_timer);
        mRecyclerView = findViewById(R.id.rv_board);

        if (savedInstanceState != null) {
            String gameJson = savedInstanceState.getString(KEY_GAME_JSON);
            mGame = DontTapRed.getGameFromJSON(gameJson);
            mGameStarted = savedInstanceState.getBoolean(KEY_GAME_STARTED);
            mCurrentStay = savedInstanceState.getLong(KEY_CURRENT_STAY);
            mTurnStartTime = savedInstanceState.getLong(KEY_TURN_START_TIME);
            
            mAdapter = new CardViewImageAdapter(mGame);
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.setOnItemClickListener((position, v) -> handleItemClick(position));
            
            updateUI();
            if (mTimerView != null) {
                long remaining = mCurrentStay - (System.currentTimeMillis() - mTurnStartTime);
                if (remaining < 0) remaining = 0;
                mTimerView.setText(getString(R.string.timer_format, (int) Math.ceil(remaining / 1000.0)));
            }
        } else {
            setupGame();
        }

        findViewById(R.id.fab).setOnClickListener(view -> showRulesDialog());
    }

    private void setupGame() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean oneGreenTile = prefs.getBoolean(getString(R.string.key_one_green_tile), false);
        
        mGame = new DontTapRed(3, 3, oneGreenTile);
        mAdapter = new CardViewImageAdapter(mGame);
        mGameStarted = false;
        
        mRecyclerView.setAdapter(mAdapter);
        adjustSpeed();
        mAdapter.setOnItemClickListener((position, v) -> handleItemClick(position));

        mScoreView.setText(getString(R.string.score_format, mGame.getScore()));
        if (mTimerView != null) {
            mTimerView.setText(getString(R.string.timer_format, (int) (mCurrentStay / 1000)));
        }
    }

    private void handleItemClick(int position) {
        if (mGame.isGameOver()) return;

        if (mGame.getTileType(position) == 1) {
            handleGreenClick();
        } else {
            handleRedClick();
        }
    }

    private void handleGreenClick(){
        mHandler.removeCallbacks(mGameLoop);
        mHandler.removeCallbacks(mUpdateTimerRunnable);
        mGameStarted = true;
        mGame.addScore();
        mScoreView.setText(getString(R.string.score_format, mGame.getScore()));
        mGame.shiftTiles();
        adjustSpeed();
        updateUI();
        mTurnStartTime = System.currentTimeMillis();
        mHandler.postDelayed(mGameLoop, mCurrentStay);
        mHandler.post(mUpdateTimerRunnable);
    }
        
    private void handleRedClick(){
        handleGameOver();
    }    
    private void updateUI() {
        mAdapter.notifyDataSetChanged();
        if (mScoreView != null) {
            mScoreView.setText(getString(R.string.score_format, mGame.getScore()));
        }
    }

    private void adjustSpeed() {
        switch (mGame.getScore()) {
            case 0: //first three tries user gets ten seconds
            case 1:
            case 2:
                mCurrentStay = 10000;
                break;
            case 3: //next three tries user gets five seconds
            case 4:
            case 5:
                mCurrentStay = 5000;
                break;
            case 6:
            case 7:
            case 8: //first three tries user gets ten seconds
                mCurrentStay = 2000;
                break;
            default://after nine tries user gets one second and on
                mCurrentStay=1000; 
        }
    }

    private void handleGameOver() {
        mHandler.removeCallbacks(mGameLoop);
        mHandler.removeCallbacks(mUpdateTimerRunnable);
        new AlertDialog.Builder(this)
                .setTitle(R.string.game_over)
                .setMessage(getString(R.string.score_format, mGame.getScore()))
                .show();
        setupGame();
    }
    
    private void showRulesDialog() {
        Utils.showInfoDialog(this, R.string.aboutDialogTitle, R.string.game_rules);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mGameLoop);
        mHandler.removeCallbacks(mUpdateTimerRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGameStarted && !mGame.isGameOver()) {
            long elapsed = System.currentTimeMillis() - mTurnStartTime;
            long remaining = mCurrentStay - elapsed;
            if (remaining > 0) {
                mHandler.postDelayed(mGameLoop, remaining);
                mHandler.post(mUpdateTimerRunnable);
            } else {
                handleGameOver();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Set the state of the "One Green Tile" menu item from preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean oneGreenTile = prefs.getBoolean(getString(R.string.key_one_green_tile), false);
        MenuItem oneGreenTileItem = menu.findItem(R.id.action_one_green_tile);
        if (oneGreenTileItem != null) {
            oneGreenTileItem.setChecked(oneGreenTile);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_one_green_tile) {
            // Toggle the preference
            boolean newValue = !item.isChecked();
            item.setChecked(newValue);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putBoolean(getString(R.string.key_one_green_tile), newValue).apply();

            // Reset the game to apply the new setting
            setupGame();
            return true;
        } else if (id == R.id.action_about) {
            showRulesDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mGame != null) {
            outState.putString(KEY_GAME_JSON, mGame.getJSONFromCurrentGame());
            outState.putBoolean(KEY_GAME_STARTED, mGameStarted);
            outState.putLong(KEY_CURRENT_STAY, mCurrentStay);
            outState.putLong(KEY_TURN_START_TIME, mTurnStartTime);
        }
    }
}