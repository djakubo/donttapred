package com.mintedtech.dont_tap_red.classes;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mintedtech.dont_tap_red.R;
import com.mintedtech.dont_tap_red.interfaces.OnItemClickCustomListener;
import com.mintedtech.dont_tap_red.models.DontTapRed;

public class CardViewImageAdapter extends RecyclerView.Adapter<CardImageViewHolder> {
    public static OnItemClickCustomListener sOnItemClickListener;
    private final DontTapRed mGame;

    public CardViewImageAdapter(DontTapRed game) {
        mGame = game;
    }

    @NonNull
    @Override
    public CardImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rv_card_image_item, parent, false);

        CardImageViewHolder cardImageViewHolder = new CardImageViewHolder(itemLayoutView);
        adjustScaling(cardImageViewHolder, parent);

        return cardImageViewHolder;
    }

    private void adjustScaling(CardImageViewHolder cardImageViewHolder, ViewGroup parent) {
        ImageView imageInNewlyInflatedView = cardImageViewHolder.mCurrentImageView;
        ViewGroup.LayoutParams currentLayoutParams = imageInNewlyInflatedView.getLayoutParams();
        currentLayoutParams.height = calcHeightSize(parent);
        imageInNewlyInflatedView.setLayoutParams(currentLayoutParams);
    }

    private int calcHeightSize(ViewGroup parent) {
        // Try to get the actual height from the RecyclerView (parent)
        int parentHeight = parent.getHeight();
        
        // If the RecyclerView has been measured, use its height to fit 3 rows exactly
        if (parentHeight > 0) {
            Resources resources = parent.getContext().getResources();
            float density = resources.getDisplayMetrics().density;
            // Subtract the total vertical margins for the item (4dp top + 4dp bottom = 8dp)
            int itemMargins = (int) (8 * density);
            return (parentHeight / 3) - itemMargins;
        }

        // Fallback to DisplayMetrics if parent height isn't available yet
        Context context = parent.getContext();
        Resources resources = context.getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        int orientation = resources.getConfiguration().orientation;

        float density = displayMetrics.density;
        float barsHeight;

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Very aggressive estimate to ensure we don't exceed screen height
            barsHeight = 220 * density;
        } else {
            // In portrait, we account for instructions and bars
            barsHeight = 260 * density;
        }
        
        double availableHeight = displayMetrics.heightPixels - barsHeight;
        return (int) (availableHeight / 3.0);
    }

    @Override
    public void onBindViewHolder(@NonNull CardImageViewHolder holder, int position) {
        ImageView currentImageView = holder.mCurrentImageView;
        int tileType = mGame.getTileType(position);
        
        if (tileType == 1) {
            currentImageView.setImageResource(R.drawable.tile_green);
        } else {
            currentImageView.setImageResource(R.drawable.tile_red);
        }
    }

    @Override
    public int getItemCount() {
        return mGame.getRows() * mGame.getColumns();
    }

    public void setOnItemClickListener(OnItemClickCustomListener onItemClickListener) {
        sOnItemClickListener = onItemClickListener;
    }
}