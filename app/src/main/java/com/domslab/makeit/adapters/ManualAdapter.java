package com.domslab.makeit.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.domslab.makeit.R;
import com.domslab.makeit.model.ManualCard;

import java.util.ArrayList;

public class ManualAdapter extends RecyclerView.Adapter<ManualAdapter.ViewHolder> {
    ArrayList<ManualCard> manualCards;
    Context context;
    private OnManualListener mOnManualListener;

    public ManualAdapter(Context context, ArrayList<ManualCard> manualCards, OnManualListener onManualListener) {
        this.manualCards = manualCards;
        this.context = context;
        mOnManualListener = onManualListener;
    }

    @NonNull
    @Override
    public ManualAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item, parent, false);
        return new ViewHolder(view, mOnManualListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.imageView.setImageBitmap(manualCards.get(position).getCover());
        //set name to textview
        holder.textView.setText(manualCards.get(position).getName());

    }

    @Override
    public int getItemCount() {
        return manualCards.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView;
        TextView textView;

        OnManualListener onManualListener;

        public ViewHolder(@NonNull View itemView, OnManualListener onSongListener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            textView = itemView.findViewById(R.id.manual_name);
            this.onManualListener = onSongListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onManualListener.onManualClick(getAdapterPosition());
        }
    }

    public interface OnManualListener {
        public void onManualClick(int position);
    }
}
