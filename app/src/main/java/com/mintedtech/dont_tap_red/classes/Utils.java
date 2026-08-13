package com.mintedtech.dont_tap_red.classes;

import android.content.Context;

import com.mintedtech.dont_tap_red.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

public class Utils {
    /**
     * Shows an Android equivalent to JOptionPane
     *
     * @param strTitle Title of the Dialog box
     * @param strMsg   Message (body) of the Dialog box
     */
    private static void showAlertDialog(Context context, String strTitle, String strMsg) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(strTitle);
        alertDialogBuilder.setMessage(strMsg);
        alertDialogBuilder.setIcon(ContextCompat.getDrawable(context, R.mipmap.ic_launcher));
        alertDialogBuilder.setCancelable(true);
        alertDialogBuilder.setNeutralButton(context.getString(android.R.string.ok), (dialog, which) -> {
        });
        alertDialogBuilder.show();
    }

    public static void showInfoDialog(Context context, int titleID, int msgID) {
        showInfoDialog(context, context.getString(titleID), context.getString(msgID));
    }

    public static void showInfoDialog(Context context, String strTitle, String strMsg) {
        showAlertDialog(context, strTitle, strMsg);
    }
}