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
    private transient Random mRandom;
    private final boolean mOneGreenTile;

    public DontTapRed(int rows, int columns, boolean oneGreenTile) {
        mRows = rows;
        mColumns = columns;
        mOneGreenTile = oneGreenTile;
        mTilePositions = new ArrayList<>();
        mRandom = new Random();
        mGamesPlayed = 0;
        mHighestScore = 0;
        mLowestScore = Integer.MAX_VALUE;
        startGame();
    }

    public void startGame() {
        mScore = 0;
        mGameOver = false;
        mTilePositions.clear();

        for (int i = 0; i < mRows; i++) {
            if (mOneGreenTile) {
                // If one green tile mode, only the bottom row (index mRows-1) starts with a green tile
                // The others will be empty (-1)
                if (i == mRows - 1) {
                    mTilePositions.add(mRandom.nextInt(mColumns));
                } else {
                    mTilePositions.add(-1);
                }
            } else {
                // Normal mode: every row has a green tile
                mTilePositions.add(mRandom.nextInt(mColumns));
            }
        }
    }
    
    public void endGame() {
        mGameOver = true;
        mGamesPlayed++;
        if (mScore > mHighestScore) {
            mHighestScore = mScore;
        }
        if (mScore < mLowestScore) {
            mLowestScore = mScore;
        }
    }

    public boolean shiftTiles() {
        if (mGameOver) return false;

        // Move all black tiles down one row
        for (int i = mRows - 1; i > 0; i--) {
            mTilePositions.set(i, mTilePositions.get(i - 1));
        }

        if (mOneGreenTile) {
            // If in one green tile mode, only add a new tile at the top if the board is now empty
            boolean anyGreen = false;
            for (int i = 1; i < mRows; i++) {
                if (mTilePositions.get(i) != -1) {
                    anyGreen = true;
                    break;
                }
            }
            if (!anyGreen) {
                mTilePositions.set(0, mRandom.nextInt(mColumns));
            } else {
                mTilePositions.set(0, -1);
            }
        } else {
            // Generate new black tile for the top row
            mTilePositions.set(0, mRandom.nextInt(mColumns));
        }
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

    /**
     * Reverses the game object's serialization as a String
     * back to a DontTapRed game object
     *
     * @param json The serialized String of the game object
     * @return The game object
     */
    public static DontTapRed getGameFromJSON(String json) {
        Gson gson = new Gson();
        DontTapRed game = gson.fromJson(json, DontTapRed.class);
        if (game != null && game.mRandom == null) {
            game.mRandom = new Random();
        }
        return game;
    }

    /**
     * Serializes the game object to a JSON-formatted String
     *
     * @return JSON-formatted String
     */
    public String getJSONFromCurrentGame() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}