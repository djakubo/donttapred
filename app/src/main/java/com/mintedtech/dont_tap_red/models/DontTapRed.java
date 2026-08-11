package com.mintedtech.dont_tap_red.models;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DontTapRed {
    private final int mColumns;
    private final int mRows;
    private final List<Integer> mTilePositions;
    private int mScore;
    private boolean mGameOver;
    private final Random mRandom;
    
    // Statistics
    private int mGamesPlayed;
    private int mWins;
    private int mLosses;

    public DontTapRed(int rows, int columns) {
        mRows = rows;
        mColumns = columns;
        mTilePositions = new ArrayList<>();
        mRandom = new Random();
        mGamesPlayed = 0;
        mWins = 0;
        mLosses = 0;
        startGame();
    }

    public void startGame() {
        mScore = 0;
        mGameOver = false;
        mTilePositions.clear();
        for (int i = 0; i < mRows; i++) {
            mTilePositions.add(mRandom.nextInt(mColumns));
        }
    }
    
    public void endGame() {
        mGameOver = true;
        mGamesPlayed++;
        if (mScore >= 10) {
            mWins++;
        } else {
            mLosses++;
        }
    }

    public boolean shiftTiles() {
        if (mGameOver) return false;
        
        // Move all tiles down one row
        for (int i = mRows - 1; i > 0; i--) {
            mTilePositions.set(i, mTilePositions.get(i - 1));
        }
        // Generate new black tile for the top row
        mTilePositions.set(0, mRandom.nextInt(mColumns));
        return true;
    }

    public int getTileType(int position) {
        int row = position / mColumns;
        int col = position % mColumns;
        if (row >= 0 && row < mTilePositions.size()) {
            if (col == mTilePositions.get(row)) {
                return 1; // Green (Safe/Target)
            }
        }
        return 0; // Red (Deadly/Background)
    }
    public void addScore(){
        mScore++;
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
    
    public int getGamesPlayed() {
        return mGamesPlayed;
    }
    
    public int getWins() {
        return mWins;
    }
    
    public int getLosses() {
        return mLosses;
    }
    
    public void resetStatistics() {
        mGamesPlayed = 0;
        mWins = 0;
        mLosses = 0;
    }

    public static String getJSONFromGame(DontTapRed game) {
        return new Gson().toJson(game);
    }

    public static DontTapRed getGameFromJSON(String json) {
        return new Gson().fromJson(json, DontTapRed.class);
    }
}