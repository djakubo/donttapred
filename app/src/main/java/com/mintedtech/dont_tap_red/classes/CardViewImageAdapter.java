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
        adjustScaling(cardImageViewHolder, parent.getContext());

        return cardImageViewHolder;
    }

    private void adjustScaling(CardImageViewHolder cardImageViewHolder, Context context) {
        ImageView imageInNewlyInflatedView = cardImageViewHolder.mCurrentImageView;
        ViewGroup.LayoutParams currentLayoutParams = imageInNewlyInflatedView.getLayoutParams();
        currentLayoutParams.height = calcHeightSize(context);
        imageInNewlyInflatedView.setLayoutParams(currentLayoutParams);
    }

    private int calcHeightSize(Context context) {
        Resources resources = context.getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();

        boolean isLandscape = (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE);
        double scaleVertical = isLandscape ? 4.0 : 4.0; // Show 4 rows
        double screenHeight = displayMetrics.heightPixels;

        return (int) (screenHeight / scaleVertical);
    }

    @Override
    public void onBindViewHolder(@NonNull CardImageViewHolder holder, int position) {
        ImageView currentImageView = holder.mCurrentImageView;
        int tileType = mGame.getTileType(position);
        
        switch (tileType) {
            case DontTapRed.TILE_TARGET:
                currentImageView.setImageResource(R.drawable.tile_black);
                break;
            case DontTapRed.TILE_RED:
                currentImageView.setImageResource(R.drawable.tile_red);
                break;
            case DontTapRed.TILE_CLEARED:
                currentImageView.setImageResource(R.drawable.tile_cleared);
                break;
            default:
                currentImageView.setImageResource(R.drawable.tile_empty);
                break;
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