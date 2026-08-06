package com.mintedtech.dont_tap_red.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DontTapRed {
    private final int mColumns;
    private final int mRows;
    private final List<Integer> mBlackTilePositions;
    private int mScore;
    private boolean mGameOver;
    private final Random mRandom;

    public DontTapRed(int rows, int columns) {
        mRows = rows;
        mColumns = columns;
        mBlackTilePositions = new ArrayList<>();
        mRandom = new Random();
        startGame();
    }

    public void startGame() {
        mScore = 0;
        mGameOver = false;
        mBlackTilePositions.clear();
        for (int i = 0; i < mRows; i++) {
            mBlackTilePositions.add(mRandom.nextInt(mColumns));
        }
    }

    public boolean attemptTurn(int position) {
        if (mGameOver) return false;

        int row = position / mColumns;
        int col = position % mColumns;

        // Check if the tapped tile is the black one in the bottom row
        if (row == mRows - 1 && col == mBlackTilePositions.get(row)) {
            shiftTiles();
            mScore++;
            return true;
        } else {
            mGameOver = true;
            return false;
        }
    }

    public boolean shiftTiles() {
        if (mGameOver) return false;
        
        // Move all black tiles down one row
        for (int i = mRows - 1; i > 0; i--) {
            mBlackTilePositions.set(i, mBlackTilePositions.get(i - 1));
        }
        // Generate new black tile for the top row
        mBlackTilePositions.set(0, mRandom.nextInt(mColumns));
        return true;
    }

    public int getTileType(int position) {
        int row = position / mColumns;
        int col = position % mColumns;
        if (row >= 0 && row < mBlackTilePositions.size()) {
            if (col == mBlackTilePositions.get(row)) {
                return 1; // Green (Safe/Target)
            }
        }
        return 0; // Red (Deadly/Background)
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