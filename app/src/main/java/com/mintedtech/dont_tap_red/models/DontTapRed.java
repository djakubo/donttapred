package com.mintedtech.dont_tap_red.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DontTapRed {
    public static final int TILE_SAFE = 1;
    public static final int TILE_RED = 2;
    public static final int TILE_CLEARED = 3;
    public static final int TILE_NEUTRAL = 0;

    private final int mColumns;
    private final int mRows;
    private final List<Integer> mSafeTileColumns; // One safe tile per row
    private final List<Boolean> mRowCleared;      // Has the safe tile in this row been tapped?
    private int mScore;
    private boolean mGameOver;
    private final Random mRandom;

    public DontTapRed(int rows, int columns) {
        mRows = rows;
        mColumns = columns;
        mSafeTileColumns = new ArrayList<>();
        mRowCleared = new ArrayList<>();
        mRandom = new Random();
        startGame();
    }

    public void startGame() {
        mScore = 0;
        mGameOver = false;
        mSafeTileColumns.clear();
        mRowCleared.clear();
        // Initialize rows with one random safe tile per row
        for (int i = 0; i < mRows; i++) {
            mSafeTileColumns.add(mRandom.nextInt(mColumns));
            mRowCleared.add(false);
        }
    }

    public boolean attemptTurn(int position) {
        if (mGameOver) return false;

        int row = position / mColumns;
        int col = position % mColumns;

        // Find the bottom-most row that is not yet cleared
        int activeRow = -1;
        for (int i = mRows - 1; i >= 0; i--) {
            if (!mRowCleared.get(i)) {
                activeRow = i;
                break;
            }
        }

        if (activeRow == -1) return false;

        // Rules:
        // 1. Tapping an already cleared row is ignored.
        if (row > activeRow) return false;

        // 2. Tapping a row ABOVE the active row is a mistake (Sequential play).
        if (row < activeRow) {
            mGameOver = true;
            return false;
        }

        // 3. Tapping the safe tile in the active row.
        if (col == mSafeTileColumns.get(row)) {
            mRowCleared.set(row, true);
            mScore++;
            return true;
        } else {
            // Tapped a RED tile in the active row.
            mGameOver = true;
            return false;
        }
    }

    public boolean shiftTiles() {
        if (mGameOver) return false;

        // If the bottom-most row was NOT cleared, the player missed it.
        if (!mRowCleared.get(mRows - 1)) {
            mGameOver = true;
            return false;
        }

        // Move rows down
        for (int i = mRows - 1; i > 0; i--) {
            mSafeTileColumns.set(i, mSafeTileColumns.get(i - 1));
            mRowCleared.set(i, mRowCleared.get(i - 1));
        }

        // Generate new row at top
        mSafeTileColumns.set(0, mRandom.nextInt(mColumns));
        mRowCleared.set(0, false);

        return true;
    }

    public int getTileType(int position) {
        int row = position / mColumns;
        int col = position % mColumns;
        
        if (row < 0 || row >= mRows) return TILE_NEUTRAL;

        if (mRowCleared.get(row)) {
            // Row is cleared: target turns grey, others turn white/neutral.
            return (col == mSafeTileColumns.get(row)) ? TILE_CLEARED : TILE_NEUTRAL;
        } else {
            // Row is active: target is SAFE (black), others are RED.
            return (col == mSafeTileColumns.get(row)) ? TILE_SAFE : TILE_RED;
        }
    }

    public int getScore() {
        return mScore;
    }

    public boolean isGameOver() {
        return mGameOver;
    }

    public int getColumns() {
        return mColumns;
    }

    public int getRows() {
        return mRows;
    }
}