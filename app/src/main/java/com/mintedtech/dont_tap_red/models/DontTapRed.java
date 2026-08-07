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

        // Rule: Only tap the bottom-most active target tile first.
        // We find the index of the bottom-most row that is NOT cleared.
        int bottomMostActiveRow = -1;
        for (int i = mRows - 1; i >= 0; i--) {
            if (!mRowCleared.get(i)) {
                bottomMostActiveRow = i;
                break;
            }
        }

        // If user tapped a row other than the bottom-most active one, we ignore or penalize.
        // Sequential gameplay enforcement:
        if (row != bottomMostActiveRow) {
            // Optional: User said "Ensure players can only tap... first".
            // Let's treat tapping a higher row as a miss/game over if it's a target, 
            // OR just ignore it. Piano Tiles usually ignores it or treats as miss.
            // Let's treat it as Game Over for simplicity and strictness if it's a target or red.
            // Actually, let's just ignore taps on already cleared rows or rows too far up.
            if (row > bottomMostActiveRow) return false; // Already cleared
            
            // If they tap a target that is NOT the bottom-most one:
            // "Ensure players can only tap the bottom-most active target tile first"
            mGameOver = true;
            return false;
        }

        if (col == mTargetColumns.get(row)) {
            // Success!
            mRowCleared.set(row, true);
            mScore++;
            return true;
        } else {
            // Tapped a RED tile (since 1 target, 3 red per row)
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

        // Check if the bottom row target was cleared.
        // "The game ends immediately if you... let a non-red tile slide off the screen without tapping it."
        if (!mRowCleared.get(mRows - 1)) {
            mGameOver = true;
            return false;
        }

        // Move all tiles down
        for (int i = mRows - 1; i > 0; i--) {
            mTargetColumns.set(i, mTargetColumns.get(i - 1));
            mRowCleared.set(i, mRowCleared.get(i - 1));
        }

        // New row at top
        mTargetColumns.set(0, mRandom.nextInt(mColumns));
        mRowCleared.set(0, false);

        return true;
    }

    public int getTileType(int position) {
        int row = position / mColumns;
        int col = position % mColumns;
        
        if (mRowCleared.get(row) && col == mTargetColumns.get(row)) {
            return TILE_CLEARED;
        }
        
        if (col == mTargetColumns.get(row)) {
            return TILE_TARGET;
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