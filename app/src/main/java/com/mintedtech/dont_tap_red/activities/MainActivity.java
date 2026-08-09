package com.mintedtech.dont_tap_red.activities;

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
import androidx.recyclerview.widget.RecyclerView;

import com.mintedtech.dont_tap_red.R;
import com.mintedtech.dont_tap_red.classes.CardViewImageAdapter;
import com.mintedtech.dont_tap_red.models.DontTapRed;
import com.mintedtech.dont_tap_red.interfaces.OnItemClickCustomListener;

public class MainActivity extends AppCompatActivity {

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

        setupGame();

        findViewById(R.id.fab).setOnClickListener(view -> showRulesDialog());
    }

    private void setupGame() {
        mGame = new DontTapRed(3, 3);
        mAdapter = new CardViewImageAdapter(mGame);
        mGameStarted = false;
        
        mRecyclerView.setAdapter(mAdapter);
        adjustSpeed();
        mAdapter.setOnItemClickListener((position, v) -> {
            if (mGame.isGameOver()) return;

            if (mGame.getTileType(position) == 1) {
                handleGreenClick();
            } else {
                handleRedClick();
            }
        });

        mScoreView.setText(getString(R.string.score_format, mGame.getScore()));
        if (mTimerView != null) {
            mTimerView.setText(getString(R.string.timer_format, (int) (mCurrentStay / 1000)));
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
        new AlertDialog.Builder(this)
                .setTitle(R.string.aboutDialogTitle)
                .setMessage(R.string.game_rules)
                .setPositiveButton(android.R.string.ok, null)
                .show();
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}