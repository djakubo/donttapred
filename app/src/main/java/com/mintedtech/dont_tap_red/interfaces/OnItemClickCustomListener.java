package com.mintedtech.dont_tap_red.interfaces;

import android.view.View;

// used to send data out of Adapter - implemented in the calling Activity/Fragment
public interface OnItemClickCustomListener
{
    void onItemClick (int position, View v);
}