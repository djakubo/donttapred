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
    private final List<Integer> mTargetColumns; // Column of the target tile in each row
    private final List<Boolean> mRowCleared;    // Whether the target in this row has been tapped
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
        // Initialize with rows. The first row (index 0) is top, last row (index mRows-1) is bottom.
        for (int i = 0; i < mRows; i++) {
            mTargetColumns.add(mRandom.nextInt(mColumns));
            mRowCleared.add(false);
        }
    }

    /**
     * @param position 0-based position in the grid (row * columns + col)
     * @return true if it was a successful tap on a target
     */
    public boolean attemptTurn(int position) {
        if (mGameOver) return false;

        int row = position / mColumns;
        int col = position % mColumns;

        // Find the index of the bottom-most row that is NOT cleared.
        int bottomMostActiveRow = -1;
        for (int i = mRows - 1; i >= 0; i--) {
            if (!mRowCleared.get(i)) {
                bottomMostActiveRow = i;
                break;
            }
        }

        // Rule enforcement: Only allow tapping the bottom-most target.
        if (row != bottomMostActiveRow) {
            // Ignore taps on rows already cleared
            if (row > bottomMostActiveRow) return false;
            
            // Tapping a row higher than the current target results in Game Over
            mGameOver = true;
            return false;
        }

        if (col == mTargetColumns.get(row)) {
            // Success!
            mRowCleared.set(row, true);
            mScore++;
            return true;
        } else {
            // Tapped a RED tile (or just not the target)
            mGameOver = true;
            return false;
        }
    }

    /**
     * Shifts tiles down. Called by a timer.
     * @return false if the game ends because a target was missed
     */
    public boolean shiftTiles() {
        if (mGameOver) return false;

        // If the bottom row was not cleared before shifting, it's a miss -> Game Over.
        if (!mRowCleared.get(mRows - 1)) {
            mGameOver = true;
            return false;
        }

        // Move all tiles down (from bottom to top)
        for (int i = mRows - 1; i > 0; i--) {
            mTargetColumns.set(i, mTargetColumns.get(i - 1));
            mRowCleared.set(i, mRowCleared.get(i - 1));
        }

        // Add a fresh new row at the top
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