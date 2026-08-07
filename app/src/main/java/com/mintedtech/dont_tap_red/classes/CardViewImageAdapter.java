package com.mintedtech.dont_tap_red.classes;

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

        return new CardImageViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull CardImageViewHolder holder, int position) {
        ImageView currentImageView = holder.mCurrentImageView;
        int tileType = mGame.getTileType(position);
        
        switch (tileType) {
            case DontTapRed.TILE_SAFE:
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