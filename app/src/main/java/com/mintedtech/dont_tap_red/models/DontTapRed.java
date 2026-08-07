package com.mintedtech.dont_tap_red.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DontTapRed {
    public static final int TILE_EMPTY = 0;
    public static final int TILE_TARGET = 1;
    public static final int TILE_RED = 2;
    public static final int TILE_CLEARED = 3;

    private final int mColumns;
    private final int mRows;
    private final List<Integer> mTargetColumns;
    private final List<Boolean> mRowCleared;
    private int mScore;
    private boolean mGameOver;
    private final Random mRandom;

    public DontTapRed(int rows, int columns) {
        mRows = rows;
        mColumns = columns;
        mTargetColumns = new ArrayList<>();
        mRowCleared = new ArrayList<>();
        mRandom = new Random();
        startGame();
    }

    public void startGame() {
        mScore = 0;
        mGameOver = false;
        mTargetColumns.clear();
        mRowCleared.clear();
        for (int i = 0; i < mRows; i++) {
            mTargetColumns.add(mRandom.nextInt(mColumns));
            mRowCleared.add(false);
        }
    }

    public boolean attemptTurn(int position) {
        if (mGameOver) return false;

        int row = position / mColumns;
        int col = position % mColumns;

        int bottomMostActiveRow = -1;
        for (int i = mRows - 1; i >= 0; i--) {
            if (!mRowCleared.get(i)) {
                bottomMostActiveRow = i;
                break;
            }
        }

        if (row != bottomMostActiveRow) {
            if (row > bottomMostActiveRow) return false; // Already cleared
            mGameOver = true;
            return false;
        }

        if (col == mTargetColumns.get(row)) {
            mRowCleared.set(row, true);
            mScore++;
            return true;
        } else {
            mGameOver = true;
            return false;
        }
    }

    public boolean shiftTiles() {
        if (mGameOver) return false;

        // If the player hasn't cleared the bottom row, they missed it.
        if (!mRowCleared.get(mRows - 1)) {
            mGameOver = true;
            return false;
        }

        // Shift everything down
        for (int i = mRows - 1; i > 0; i--) {
            mTargetColumns.set(i, mTargetColumns.get(i - 1));
            mRowCleared.set(i, mRowCleared.get(i - 1));
        }

        // New top row
        mTargetColumns.set(0, mRandom.nextInt(mColumns));
        mRowCleared.set(0, false);

        return true;
    }

    public int getTileType(int position) {
        int row = position / mColumns;
        int col = position % mColumns;
        
        if (row < 0 || row >= mRows) return TILE_EMPTY;

        if (col == mTargetColumns.get(row)) {
            return mRowCleared.get(row) ? TILE_CLEARED : TILE_TARGET;
        } else {
            return TILE_RED;
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