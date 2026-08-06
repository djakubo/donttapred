package com.mintedtech.dont_tap_red.classes;

import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;
import com.mintedtech.dont_tap_red.R;

public class CardImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    final ImageView mCurrentImageView;

    public CardImageViewHolder(View itemLayoutView) {
        super(itemLayoutView);
        mCurrentImageView = itemLayoutView.findViewById(R.id.rv_image_item);
        itemLayoutView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (CardViewImageAdapter.sOnItemClickListener != null) {
            CardViewImageAdapter.sOnItemClickListener.onItemClick(getAdapterPosition(), v);
        }
    }
}