package com.mintedtech.dont_tap_red.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mintedtech.dont_tap_red.R;
import com.mintedtech.dont_tap_red.classes.CardViewImageAdapter;
import com.mintedtech.dont_tap_red.models.DontTapRed;
import com.mintedtech.dont_tap_red.interfaces.OnItemClickCustomListener;

public class MainActivity extends AppCompatActivity {

    private DontTapRed mGame;
    private CardViewImageAdapter mAdapter;
    private TextView mScoreView;
    private TextView mTimerView;
    private RecyclerView mRecyclerView;
    
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mGameLoop;
    private Runnable mCountdownRunnable;
    
    private long mCurrentDelay = 1000;
    private int mSecondsRemaining = 30;
    private boolean mGameStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Score is in the bottom bar, Timer will be in the top bar
        mScoreView = findViewById(R.id.tv_status);
        mTimerView = findViewById(R.id.tv_status_top); // Updated layout will have this ID
        if (mTimerView == null) {
            // Fallback if layout hasn't been updated yet
            mTimerView = findViewById(R.id.tv_status); 
        }

        mRecyclerView = findViewById(R.id.rv_board);

        setupGame();

        findViewById(R.id.fab).setOnClickListener(view -> showRulesDialog());
    }

    private void setupGame() {
        mGame = new DontTapRed(4, 4);
        mAdapter = new CardViewImageAdapter(mGame);
        
        GridLayoutManager layoutManager = new GridLayoutManager(this, 4) {
            @Override
            public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
                lp.height = getHeight() / 4;
                return true;
            }
        };
        
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        
        mAdapter.setOnItemClickListener((position, v) -> {
            if (mGame.isGameOver()) return;
            
            boolean success = mGame.attemptTurn(position);
            if (success) {
                if (!mGameStarted) {
                    startRunning();
                }
                updateUI();
                adjustSpeed();
            } else if (mGame.isGameOver()) {
                handleGameOver(getString(R.string.game_over));
            }
        });

        prepareNewGame();
    }

    private void prepareNewGame() {
        mGame.startGame();
        mGameStarted = false;
        mCurrentDelay = 1000;
        mSecondsRemaining = 30;
        stopAllRunnables();
        updateUI();
    }

    private void startRunning() {
        mGameStarted = true;
        
        // Start scrolling loop
        mGameLoop = new Runnable() {
            @Override
            public void run() {
                if (!mGame.isGameOver()) {
                    boolean continued = mGame.shiftTiles();
                    updateUI();
                    if (continued) {
                        mHandler.postDelayed(this, mCurrentDelay);
                    } else {
                        handleGameOver(getString(R.string.game_over));
                    }
                }
            }
        };
        mHandler.postDelayed(mGameLoop, mCurrentDelay);

        // Start countdown timer
        mCountdownRunnable = new Runnable() {
            @Override
            public void run() {
                if (mSecondsRemaining > 0 && !mGame.isGameOver()) {
                    mSecondsRemaining--;
                    updateTimerUI();
                    mHandler.postDelayed(this, 1000);
                } else if (mSecondsRemaining <= 0) {
                    handleGameOver(getString(R.string.time_up));
                }
            }
        };
        mHandler.postDelayed(mCountdownRunnable, 1000);
    }

    private void stopAllRunnables() {
        if (mGameLoop != null) mHandler.removeCallbacks(mGameLoop);
        if (mCountdownRunnable != null) mHandler.removeCallbacks(mCountdownRunnable);
    }

    private void updateUI() {
        mAdapter.notifyDataSetChanged();
        if (mScoreView != null) {
            mScoreView.setText(getString(R.string.score_format, mGame.getScore()));
        }
        updateTimerUI();
    }

    private void updateTimerUI() {
        if (mTimerView != null) {
            mTimerView.setText(getString(R.string.timer_format, mSecondsRemaining));
        }
    }

    private void adjustSpeed() {
        // Difficulty Scaling: Increase speed slightly based on score
        mCurrentDelay = Math.max(250, 1000 - (mGame.getScore() * 10L));
    }

    private void handleGameOver(String title) {
        stopAllRunnables();
        
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(getString(R.string.score_format, mGame.getScore()))
                .setPositiveButton(R.string.play_again, (dialog, which) -> {
                    prepareNewGame();
                })
                .setNegativeButton(R.string.exit, (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
    
    private void showRulesDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.rules_title)
                .setMessage(R.string.rules_text)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAllRunnables();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGameStarted && mGame != null && !mGame.isGameOver()) {
            // Resume loops if game was already running
            if (mGameLoop != null) mHandler.postDelayed(mGameLoop, mCurrentDelay);
            if (mCountdownRunnable != null) mHandler.postDelayed(mCountdownRunnable, 1000);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_newGame) {
            prepareNewGame();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}