package com.mintedtech.dont_tap_red.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mintedtech.dont_tap_red.R;
import com.mintedtech.dont_tap_red.classes.CardViewImageAdapter;
import com.mintedtech.dont_tap_red.classes.Utils;
import com.mintedtech.dont_tap_red.enums.PlayerTurn;
import com.mintedtech.dont_tap_red.interfaces.OnItemClickCustomListener;
import com.mintedtech.dont_tap_red.models.TicTacToe;

import java.util.ArrayList;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity
{
    // named constants (finals)
    private final int mEMPTY_SPACE = R.drawable.tile_empty,
            mINVALID_ICON_VALUE_FLAG = -99;

    // primitives and Strings
    private boolean mPrefUseAutoSave, mPrefComputerOpponent, mPrefComputerStarts;

    // These values are coded here rather than in strings.xml because they are not used elsewhere
    // If these keys might be read in another Activity then the values should instead be put in xml
    // Keys reference in both Java and XML - values stored in strings.xml
    private String mKEY_USE_AUTO_SAVE, mKEY_COMPUTER_OPPONENT, mKEY_COMPUTER_STARTS;

    // Keys referenced only in Java - values stored here
    private final String mKEY_BOARD = "BOARD";
    private final String mKEY_TINTS = "TINTS";
    private final String mKEY_GAME = "GAME";
    private final String mPREFS = "PREFS";

    private TicTacToe mCurrentGame;

    // Reference to our custom Adapter used to create and maintain a board in our GridView here
    private CardViewImageAdapter mAdapter;

    // References to various Views
    private TextView mStatusBar;
    private SwipeRefreshLayout mSwipeRefreshLayout;


    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);
        setupToolbar ();
        setupViews ();
        setupRV ();
        initializePreferenceKeys ();
        initializeViewAndModel (savedInstanceState);
    }

    private void setupToolbar ()
    {
        Toolbar toolbar = findViewById (R.id.toolbar);
        setSupportActionBar (toolbar);
    }

    private void setupViews ()
    {
        initializeStatusItems ();
        initializeSwipeRefreshLayout ();
        findViewById (R.id.fab).setOnClickListener (
                v -> {
                    Utils.showInfoDialog (this, R.string.information, R.string.game_rules);
                });
    }

    /**
     * Initializes the members that are relevant to the status bar, like the x and o indicator icons
     * and the status bar (TextView)
     */
    private void initializeStatusItems ()
    {
        mStatusBar = findViewById (R.id.tv_status);
        if (mStatusBar != null) {
            mStatusBar.setText("text goes here");
        }
    }

    private void initializeViewAndModel (Bundle savedInstanceState)
    {
        // If we are starting a fresh Activity (meaning, not after rotation), then do initial setup
        if (savedInstanceState == null) {
            mCurrentGame = new TicTacToe ((int) Math.sqrt (mAdapter.getItemCount ()));
            setupInitialSession ();
        }
        // If we're in the middle of a game then onRestoreInstanceState will restore the App's state
    }

    /**
     * This has what to do when the user swipes to refresh, including the anonymous inner-class
     */
    private void initializeSwipeRefreshLayout ()
    {
        mSwipeRefreshLayout = findViewById (R.id.swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener (() -> {
            prepareForNewGame ();
            mCurrentGame.startGame ();
            startNewOrResumeGameState ();
        });
    }

    /**
     * Creates an unfilled board, which includes creating the array of winning spaces (9/3 elements)
     * and the RecyclerView grid, including layout... and an instance of out custom adapter class.
     */
    private void setupRV ()
    {
        final int totalSpaces = 9;

        // Create the adapter for later use in the RecyclerView
        mAdapter = new CardViewImageAdapter (totalSpaces, R.drawable.tile_empty);

        // set the listener which will listen to the clicks in the RecyclerView
        mAdapter.setOnItemClickListener (listener);

        // get a reference to the RecyclerView
        RecyclerView rvBoard = findViewById (R.id.rv_board);

        // Create a new LayoutManager object to be used in the RecyclerView
        int rvColumnCount = (int) Math.sqrt (mAdapter.getItemCount ());
        RecyclerView.LayoutManager layoutManager =
                new GridLayoutManager (this, rvColumnCount);/*
                {
                    @Override public boolean checkLayoutParams (RecyclerView.LayoutParams lp)
                    {
                        // Desired height seems to be slightly less than 1/3rd, due to padding/etc.?
                        // But the precise number is slightly different for portrait and landscape
                        double offset = getWidth () > getHeight () ? .13 : .18;

                        // Size the height of each to have the entire RV fill the screen
                        // Since this is a symmetrical grid, column count == row count
                        lp.height = (int) (getHeight () / (rvColumnCount + offset));
                        return true; // instead of super.checkLayoutParams (lp);
                    }
                };*/

        // apply the Layout Manager object just created to the RecyclerView
        rvBoard.setLayoutManager (layoutManager);

        // set the adapter as the data source (model) for the RecyclerView
        rvBoard.setHasFixedSize (true);
        rvBoard.setAdapter (mAdapter);
    }

    /**
     * These variables will hold the user's choices regarding auto-save and computer player/start
     */
    private void initializePreferenceKeys ()
    {
        mKEY_USE_AUTO_SAVE = getString (R.string.key_use_auto_save);
        mKEY_COMPUTER_OPPONENT = getString (R.string.key_computer_opponent);
        mKEY_COMPUTER_STARTS = getString (R.string.key_computer_starts);
    }

    /**
     * Called from onCreate() only if the user just entered the app from the home screen
     * as opposed to when destroying/recreating from e.g. an orientation change
     * <p/>
     * Steps:
     * 1. Done in SplashActivity - In case this is the first run ever, set the default values in shared prefs
     * 2. Then, set the last Game results message to first game of session
     * Next:
     * 3. Prepare for new game (but no initial computer turn)
     * 4. Restore all data from Shared Preferences
     * 5. Start or resume new game, meaning turn off animation
     * and conditionally take initial/next computer turn
     */
    private void setupInitialSession ()
    {
        prepareForNewGame ();
        mCurrentGame.startGame ();
        restoreAllDataFromPrefs ();
        startNewOrResumeGameState ();
    }

    private void prepareForNewGame ()
    {
        mAdapter.resetAllImagesAndTints ();
    }

    private void updateUIWithCurrentPlayer ()
    {
        // Turn-based UI logic removed for overhaul
    }

    /**
     * If the user chose to Auto-Save and was in the middle of a game then board will be restored
     * Otherwise, since we just started a new game, the user will simply start a new game
     */
    private void restoreAllDataFromPrefs ()
    {
        restoreGameTypeAndAutoSaveStatus ();
        restoreLastStateIfAutoSaveIsOn ();
    }

    /**
     * This method essentially takes what was saved above and restores it.
     */
    private void restoreGameTypeAndAutoSaveStatus ()
    {
        // Since this is for reading only, no editor is needed unlike in saveRestoreState
        SharedPreferences preferences = getSharedPreferences (mPREFS, MODE_PRIVATE);

        // restore AutoSave preference value
        mPrefUseAutoSave = preferences.getBoolean (mKEY_USE_AUTO_SAVE, true);

        // restore user's opponent and start preferences
        mPrefComputerOpponent = preferences.getBoolean (mKEY_COMPUTER_OPPONENT, true);
        mPrefComputerStarts = preferences.getBoolean (mKEY_COMPUTER_STARTS, false);
    }

    private void restoreLastStateIfAutoSaveIsOn ()
    {
        SharedPreferences preferences = getSharedPreferences (mPREFS, MODE_PRIVATE);

        if (mPrefUseAutoSave) {
            // restore Model
            String restoredGame = preferences.getString (mKEY_GAME, null);

            if (restoredGame != null) {
                restoreAllBoardData (preferences);

                mCurrentGame = TicTacToe.getGameFromJSON (restoredGame);

                // restore current player
                updateUIWithCurrentPlayer ();

                doPostPlayerTurn ();
            }
        }
    }

    private void restoreAllBoardData (SharedPreferences preferences)
    {
        // restore the board from SharedPreferences
        restoreBoard (preferences);

        // restore the tints from SharedPreferences
        restoreTintsIfGameOver (preferences);
    }

    private void restoreBoard (SharedPreferences preferences)
    {
        // restore the board one square at a time
        for (int i = 0; i < mAdapter.getItemCount (); i++) {
            String currentKeyName = mKEY_BOARD + i;
            int currentSpace = (int) preferences.getLong (currentKeyName, mEMPTY_SPACE);
            currentSpace = getValidCurrentSpace (currentSpace);
            mAdapter.setImage (i, currentSpace);
        }
    }

    private int getValidCurrentSpace (int currentSpace)
    {
        final int[] CURRENT_ICONS =
                new int[] {R.drawable.tile_empty, R.drawable.tile_green, R.drawable.tile_red};

        for (int icon : CURRENT_ICONS) {
            if (currentSpace == icon) {
                return currentSpace;
            }
        }

        return R.drawable.tile_empty;
    }


    private void restoreTintsIfGameOver (SharedPreferences preferences)
    {
        // tints are not used unless the game is over
        if (mCurrentGame.isGameOver ()) {
            // restore the tints one square at a time
            for (int i = 0; i < mAdapter.getItemCount (); i++) {
                String currentKeyName = mKEY_TINTS + i;
                int currentSpaceTint =
                        (int) preferences.getLong (currentKeyName, mINVALID_ICON_VALUE_FLAG);
                currentSpaceTint = getValidCurrentSpaceTint (currentSpaceTint);
                mAdapter.setImageTint (i, currentSpaceTint);
            }
        }
    }

    /**
     * Returns a valid tint, meaning either an actual tint color ID or else the invalid flag value
     *
     * @param currentSpaceTint tint read in from SP
     * @return either the non-changing mInvalid_Icon_Value_Flag, which is -99 or else the color_yes
     */
    private int getValidCurrentSpaceTint (int currentSpaceTint)
    {
        return currentSpaceTint == mINVALID_ICON_VALUE_FLAG
               ? mINVALID_ICON_VALUE_FLAG
               : R.color.color_yes;
    }


    /**
     * Called as the last step in the new game process and when starting the app even with auto-save
     * 1. Always turns off the swipe-to-refresh animation
     * 2. If the game is not over (from last run, then closed/reopened) and it's the computer's turn
     */
    private void startNewOrResumeGameState ()
    {
        // regardless of how we got here (via listener, MenuItem click, etc), turn off animation
        mSwipeRefreshLayout.setRefreshing (false);

        // If the user chooses to have a computer opponent and that the computer should start (X)
        // and it is currently turn x (always first player)
        if (!mCurrentGame.isGameOver () &&
                mPrefComputerOpponent && mPrefComputerStarts &&
                mCurrentGame.getCurrentPlayer () == PlayerTurn.X) {
            doComputerTurnCycle ();
        }
    }

/*
    // ------------------------------------------------------------------------------------------
    // Handle Android life-cycle destroy/recreate so that user doesn't even know it was recreated
    // ------------------------------------------------------------------------------------------
*/


    /**
     * This method's Superclass implementation saves to its "Bundle" argument the values of Views
     * We will add to that bundle the variables that we need to persist across destroy/create cycles
     *
     * @param outState bundle containing the saved instance state
     */
    @Override
    protected void onSaveInstanceState (@NonNull Bundle outState)
    {
        // save contents of views, etc. automatically
        super.onSaveInstanceState (outState);

        // save the game Model
        outState.putString (mKEY_GAME, mCurrentGame.getJSONFromCurrentGame ());

        // save the user's choice of opponent and start
        outState.putBoolean (mKEY_COMPUTER_OPPONENT, mPrefComputerOpponent);
        outState.putBoolean (mKEY_COMPUTER_STARTS, mPrefComputerStarts);

        // save the current autoSave boolean
        outState.putBoolean (mKEY_USE_AUTO_SAVE, mPrefUseAutoSave);

        // save the board layout as a whole to the bundle to be saved
        outState.putIntArray (mKEY_BOARD, mAdapter.getAllImages ());

        // save the tinting data model
        outState.putIntArray (mKEY_TINTS, mAdapter.getAllImageTints ());
    }

    /**
     * This method's Superclass implementation restore the values of Views, as above.
     * We will add to that the functionality to have it restore our variables that we saved above.
     *
     * @param savedInstanceState the bundle containing the saved instance state
     */
    @Override
    protected void onRestoreInstanceState (@NonNull Bundle savedInstanceState)
    {
        // Restore contents of views, etc. automatically
        super.onRestoreInstanceState (savedInstanceState);

        // restore game
        mCurrentGame = TicTacToe.getGameFromJSON (savedInstanceState.getString (mKEY_GAME));

        // restore autoSave
        mPrefUseAutoSave = savedInstanceState.getBoolean (mKEY_USE_AUTO_SAVE);

        // restore the user's choice of opponent and start
        mPrefComputerOpponent = savedInstanceState.getBoolean (mKEY_COMPUTER_OPPONENT);
        mPrefComputerStarts = savedInstanceState.getBoolean (mKEY_COMPUTER_STARTS);

        // restore the current player
        updateUIWithCurrentPlayer ();

        // restore the game board one space at a time
        restoreBoardFromSavedState (savedInstanceState);
    }

    private void restoreBoardFromSavedState (Bundle savedInstanceState)
    {
        restoreBoardImageData (savedInstanceState);
        restoreBoardTintData (savedInstanceState);
    }


    private void restoreBoardImageData (Bundle savedInstanceState)
    {
        // This is essentially a copy of the board as it was before the rotation, etc.
        int[] existingBoard = savedInstanceState.getIntArray (mKEY_BOARD);

        if (existingBoard != null) {
            for (int i = 0; i < existingBoard.length; i++) {
                // since the Adapter's array is private, call the public setImage for each image
                mAdapter.setImage (i, existingBoard[i]);
            }
        }
    }


    private void restoreBoardTintData (Bundle savedInstanceState)
    {
        int[] existingTint = savedInstanceState.getIntArray (mKEY_TINTS);

        if (existingTint != null) {
            for (int i = 0; i < existingTint.length; i++) {
                if (existingTint[i] != mINVALID_ICON_VALUE_FLAG) {
                    mAdapter.setImageTint (i, R.color.color_yes);
                }
            }
        }
    }


    /**
     * In addition to the super-class's onPause, save the board to shared prefs now, just in case...
     */
    @Override
    protected void onPause ()
    {
        super.onPause ();
        savePrefAndBoardToSharedPref ();
    }

    private void savePrefAndBoardToSharedPref ()
    {
        // Create a SP object that (creates if needed and) uses the value of mPREFS as the file name
        SharedPreferences preferences = getSharedPreferences (mPREFS, MODE_PRIVATE);

        // Create an Editor object to write changes to the preferences object above
        SharedPreferences.Editor editor = preferences.edit ();

        // clear whatever was set last time
        editor.clear ();

        // save autoSave preference
        editor.putBoolean (mKEY_USE_AUTO_SAVE, mPrefUseAutoSave);

        // save opponent type and start preferences
        editor.putBoolean (mKEY_COMPUTER_OPPONENT, mPrefComputerOpponent);
        editor.putBoolean (mKEY_COMPUTER_STARTS, mPrefComputerStarts);


        // if autoSave is on then save the board
        saveBoardToSharedPrefsIfAutoSaveIsOn (editor);

        // apply the changes to the XML file in the device's storage
        editor.apply ();

    }

    private void saveBoardToSharedPrefsIfAutoSaveIsOn (SharedPreferences.Editor editor)
    {
        // (Only) if autoSave is enabled, then save the board and current player to the SP file
        if (mPrefUseAutoSave) {
            // save model
            editor.putString (mKEY_GAME, mCurrentGame.getJSONFromCurrentGame ());

            // save "game over" state
            editor.putString (mKEY_GAME, mCurrentGame.getJSONFromCurrentGame ());

            saveAllBoardData (editor);
        }
    }

    private void saveAllBoardData (SharedPreferences.Editor editor)
    {
        // save the board to SharedPreferences
        saveBoard (editor);

        // save the tints to SharedPreferences (if gameOver)
        saveTints (editor);
    }

    private void saveBoard (SharedPreferences.Editor editor)
    {
        // save board one square at a time
        for (int i = 0; i < mAdapter.getItemCount (); i++) {
            String currentKeyName = mKEY_BOARD + i;
            editor.putLong (currentKeyName, mAdapter.getItemId (i));
        }
    }

    private void saveTints (SharedPreferences.Editor editor)
    {

        // There are no tints unless the game has ended
        if (mCurrentGame.isGameOver ()) {
            int[] currentTint = mAdapter.getAllImageTints ();
            for (int i = 0; i < currentTint.length; i++) {
                editor.putLong (mKEY_TINTS + i, currentTint[i]);
            }
        }
    }

/*
    // ------------------------------------------------------------------------------------------
    // Handle Android menu framework for this application
    // Specifically, set each menu checkbox to the value matching their respective boolean's value
    // ------------------------------------------------------------------------------------------
*/

    @Override
    public boolean onPrepareOptionsMenu (Menu menu)
    {
        super.onPrepareOptionsMenu (menu);

        // prepares the value for the mPrefUseAutoSave checked item in the menu
        // meaning: check or remove the check in the menu to match the user value for this pref.
        menu.findItem (R.id.action_autoSave).setChecked (mPrefUseAutoSave);
        menu.findItem (R.id.action_computerOpponent).setChecked (mPrefComputerOpponent);
        menu.findItem (R.id.action_computerStarts).setChecked (mPrefComputerStarts);
        return super.onPrepareOptionsMenu (menu);
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater ().inflate (R.menu.menu_main, menu);
        return super.onCreateOptionsMenu (menu);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int itemId = item.getItemId ();

        if (itemId == R.id.action_newGame) {
            startNewGame ();
            return true;
        }
        else if (itemId == R.id.action_autoSave) {
            toggleItemCheck (item);
            mPrefUseAutoSave = item.isChecked ();
            return true;
        }
        else if (itemId == R.id.action_computerOpponent) {
            toggleItemCheck (item);
            mPrefComputerOpponent = item.isChecked ();
            doComputerTurnCycleIfCheckedAndNotGameOverAndIsComputerTurn ();
            return true;
        }
        else if (itemId == R.id.action_computerStarts) {
            toggleItemCheck (item);
            mPrefComputerStarts = item.isChecked ();
            doComputerTurnCycleIfCheckedAndNotGameOverAndIsComputerTurn ();
            return true;
        }
        else if (itemId == R.id.action_statistics) {
            showStatistics ();
            return true;
        }
        else if (itemId == R.id.action_reset_stats) {
            mCurrentGame.resetStatistics ();
            return true;
        }
        else if (itemId == R.id.action_about) {
            showTTTDialog (getString (R.string.aboutDialogTitle),
                           getString (R.string.aboutDialog_banner)
            );
            return true;
        }
        return super.onOptionsItemSelected (item);
    }

    private void showStatistics ()
    {
        Intent intent = new Intent (getApplicationContext (), StatisticsActivity.class);
        intent.putExtra ("GAME", mCurrentGame.getJSONFromCurrentGame ());
        startActivity (intent);
    }

    private void startNewGame ()
    {
        // start animation
        mSwipeRefreshLayout.setRefreshing (true);
        prepareForNewGame ();
        mCurrentGame.startGame ();
        startNewOrResumeGameState ();
    }


    private void toggleItemCheck (MenuItem item)
    {
        item.setChecked (!item.isChecked ());
    }

    private void doComputerTurnCycleIfCheckedAndNotGameOverAndIsComputerTurn ()
    {
        boolean isNowTurnX = mCurrentGame.getCurrentPlayer () == PlayerTurn.X;

        if (mPrefComputerOpponent && !mCurrentGame.isGameOver () &&
                (mPrefComputerStarts && isNowTurnX || !mPrefComputerStarts && !isNowTurnX)) {
            doComputerTurnCycle ();
        }
    }

    // ---------------------------------------------------------------------------------------------
    // This method is called from showAbout (MenuItem item), specified in the item's onClick in XML.
    // It is also called from the Game Over code sequence
    //
    // Note: To use the new "Material Design"-look AlertDialog, the AlertDialog's import should be:
    // import android.support.v7.app.AlertDialog;
    // ---------------------------------------------------------------------------------------------
    private void showTTTDialog (String title, String message)
    {
        // Create listener for use with dialog window (could also be created anonymously below...
        DialogInterface.OnClickListener dialogListener = (dialog, which) -> dialog.dismiss ();


        // Create dialog window
        AlertDialog TTTAlertDialog = initDialog (title, message, dialogListener);

        // Show the dialog window
        TTTAlertDialog.show ();

    }

    private AlertDialog initDialog (String title, String message,
                                    DialogInterface.OnClickListener dialogOnClickListener)
    {
        AlertDialog TTTAlertDialog;
        TTTAlertDialog = new AlertDialog.Builder (MainActivity.this).create ();
        TTTAlertDialog.setTitle (title);
        TTTAlertDialog.setIcon (R.mipmap.ic_launcher);
        TTTAlertDialog.setMessage (message);
        TTTAlertDialog.setButton (DialogInterface.BUTTON_NEUTRAL,
                                  getString (R.string.OK), dialogOnClickListener);
        return TTTAlertDialog;
    }

    /**
     * This object of our custom Listener class handles events in the adapter; created here anon.
     * This leaves the Adapter to handle the Model part of MVC, not View or Controller
     */
    private final OnItemClickCustomListener listener = new OnItemClickCustomListener ()
    {
        public void onItemClick (int position, View view)
        {
            // if the game is already over then there is nothing more to do here
            if (mCurrentGame.isGameOver ()) {
                // Game already over, silent
            }
            // If the current space is empty and, therefore available and thus a valid space
            else if (isSpaceEmpty (position)) {
                processClickOnValidSpace (position);
            }
            else {
                // Invalid space, silent
            }
        }
    };

    private boolean isSpaceEmpty (int position)
    {
        return mAdapter.getItemId (position) == mEMPTY_SPACE;
    }

    private void processClickOnValidSpace (int position)
    {
        doHumanTurnCycle (position);

        boolean isCurrentTurnX = mCurrentGame.getCurrentPlayer () == PlayerTurn.X;

        if (mPrefComputerOpponent && !mCurrentGame.isGameOver ()) {
            if (mPrefComputerStarts && isCurrentTurnX || !mPrefComputerStarts && !isCurrentTurnX) {
                doComputerTurnCycle ();
            }
            else {
                Toast.makeText (getApplicationContext (),
                                R.string.info_computer_goes_next, Toast.LENGTH_SHORT).show ();
            }
        }
    }

    private void doHumanTurnCycle (int position)
    {
        // process this turn/move
        doPlayerTurn (position);

        // check for win and/or full board
        doPostPlayerTurn ();
    }

    /**
     * Switches changes the board's space icon in that space to match the X or O
     * and updates the current and recent positions to match the just-chosen space
     *
     * @param position The position on the board to change to X or O
     */
    private void doPlayerTurn (final int position)
    {
        int rvColumnCount = (int) Math.sqrt (mAdapter.getItemCount ());
        int row = position / rvColumnCount;
        int col = position % rvColumnCount;

        // update the model
        mCurrentGame.attemptTurn (row, col);

        // change the icon at that position from empty to either X or O as appropriate
        // icon must be for prior player because model already moved on to next player
        mAdapter.setImage (position, getIconForPriorPlayer ());
    }


    private int getIconForPriorPlayer ()
    {
        // Reference to Green or Red, depending on value of mTurnX (which player's turn it is)
        PlayerTurn currentPlayer = mCurrentGame.isGameOver () ?
                                   mCurrentGame.getCurrentPlayer () :
                                   mCurrentGame.getPriorPlayer ();
        return currentPlayer == PlayerTurn.X ? R.drawable.tile_green : R.drawable.tile_red;
    }

    private void doPostPlayerTurn ()
    {
        if (mCurrentGame.isGameOver ()) {
            doGameOverTasks ();
        }
        else {
            updateUIWithCurrentPlayer ();
        }
    }

    private void doGameOverTasks ()
    {
        tintWinningSpacesIfNotDraw ();
    }

    private void tintWinningSpacesIfNotDraw ()
    {
        if (mCurrentGame.isWinner ()) {

            boolean[][] winningSpaces = mCurrentGame.getWinningSpaces ();

            int idx = 0;
            for (boolean[] arrWinningSpace : winningSpaces) {
                for (boolean isWinningSpace : arrWinningSpace) {
                    if (isWinningSpace) {
                        mAdapter.setImageTint (idx, R.color.color_yes);
                    }

                    idx++;
                }

            }
        }
    }

    private int[] getAvailableSpaces ()
    {
        ArrayList<Integer> availableSpacesList = new ArrayList<> ();
        int[] availableSpacesArray;

        for (int i = 0; i < mAdapter.getItemCount (); i++) {
            if (isSpaceEmpty (i)) {
                availableSpacesList.add (i);
            }
        }

        availableSpacesArray = new int[availableSpacesList.size ()];
        for (int i = 0; i < availableSpacesArray.length; i++) {
            availableSpacesArray[i] = availableSpacesList.get (i);
        }

        return availableSpacesArray;
    }

    private void doComputerTurnCycle ()
    {
        doComputerTurn ();
        doPostPlayerTurn ();
    }

    private int doComputerTurn ()
    {
        Random generator = new Random ();
        int[] spaces = getAvailableSpaces ();
        int length = spaces.length;
        int element = generator.nextInt (length);
        int position = spaces[element];

        doPlayerTurn (position);
        return position;
    }
}
