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
    private RecyclerView mRecyclerView;
    
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mGameLoop;
    private long mCurrentDelay = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mScoreView = findViewById(R.id.tv_status);
        mRecyclerView = findViewById(R.id.rv_board);

        setupGame();

        findViewById(R.id.fab).setOnClickListener(view -> showRulesDialog());
    }

    private void setupGame() {
        mGame = new DontTapRed(4, 4);
        mAdapter = new CardViewImageAdapter(mGame);
        
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        mRecyclerView.setAdapter(mAdapter);
        
        mAdapter.setOnItemClickListener((position, v) -> {
            if (mGame.isGameOver()) return;
            
            boolean success = mGame.attemptTurn(position);
            if (success) {
                updateUI();
                adjustSpeed();
            } else {
                handleGameOver();
            }
        });

        setupGameLoop();
    }

    private void setupGameLoop() {
        mGameLoop = new Runnable() {
            @Override
            public void run() {
                if (!mGame.isGameOver()) {
                    mGame.shiftTiles();
                    updateUI();
                    mHandler.postDelayed(this, mCurrentDelay);
                } else {
                    handleGameOver();
                }
            }
        };
        mHandler.postDelayed(mGameLoop, mCurrentDelay);
    }

    private void updateUI() {
        mAdapter.notifyDataSetChanged();
        if (mScoreView != null) {
            mScoreView.setText(getString(R.string.score_format, mGame.getScore()));
        }
    }

    private void adjustSpeed() {
        mCurrentDelay = Math.max(200, 1000 - (mGame.getScore() * 10L));
    }

    private void handleGameOver() {
        mHandler.removeCallbacks(mGameLoop);
        new AlertDialog.Builder(this)
                .setTitle(R.string.game_over)
                .setMessage(getString(R.string.score_format, mGame.getScore()))
                .setPositiveButton(R.string.play_again, (dialog, which) -> {
                    mGame.startGame();
                    mCurrentDelay = 1000;
                    updateUI();
                    mHandler.postDelayed(mGameLoop, mCurrentDelay);
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
        mHandler.removeCallbacks(mGameLoop);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGame != null && !mGame.isGameOver() && mGameLoop != null) {
            mHandler.postDelayed(mGameLoop, mCurrentDelay);
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
        }
        return super.onOptionsItemSelected(item);
    }
}