package com.domslab.makeit.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.domslab.makeit.ReloadFirebaseCallBack;
import com.domslab.makeit.FirebaseCallBack;
import com.domslab.makeit.R;
import com.domslab.makeit.model.ManualCard;
import com.domslab.makeit.model.ManualFlyweight;
import com.domslab.makeit.model.Utilities;
import com.domslab.makeit.view.MainActivity;
import com.domslab.makeit.view.menu.HomeContainer;

import java.util.ArrayList;
import java.util.List;

public class ManualAdapter extends RecyclerView.Adapter<ManualAdapter.ViewHolder> {
    ArrayList<ManualCard> manualCards;
    Context context;
    private OnManualListener mOnManualListener;
    private boolean visible = true;
    private boolean deletable = false;

    public ManualAdapter(Context context, ArrayList<ManualCard> manualCards, OnManualListener onManualListener) {
        this.manualCards = manualCards;
        this.context = context;
        mOnManualListener = onManualListener;
    }

    public ManualAdapter(Context context, ArrayList<ManualCard> manualCards, OnManualListener onManualListener, boolean visible, boolean deletable) {
        this.manualCards = manualCards;
        this.context = context;
        mOnManualListener = onManualListener;
        this.visible = visible;
        this.deletable = deletable;
    }


    @NonNull
    @Override
    public ManualAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item, parent, false);
        return new ViewHolder(view, mOnManualListener);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int currentPosition = position;
        holder.imageView.setImageBitmap(manualCards.get(position).getCover());
        holder.textView.setText(manualCards.get(position).getName());
        if(!deletable)
            holder.delete.setVisibility(View.GONE);
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(context,R.style.MyAlertDialogTheme);
                    builder.setTitle("Attenzione!");
                    builder.setMessage("Vuoi Eliminare il seguente manuale?\n"+holder.textView.getText().toString());
                    builder.setCancelable(true);
                    builder.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.setPositiveButton("Conferma", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ManualFlyweight.getInstance().deleteManual(manualCards.get(currentPosition).getKey(),view.getContext());

                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
        });
        if (visible) {
            if (!ManualFlyweight.getInstance().isFavourite(manualCards.get(currentPosition).getKey())) {
                holder.favourite.setImageResource(R.drawable.ic_heart_off);
            } else {
                holder.favourite.setImageResource(R.drawable.ic_heart_on);
            }
            holder.favourite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utilities.showProgressDialog(v.getContext(), false);

                    ManualFlyweight.getInstance().updateManual(manualCards.get(currentPosition).getKey(), v.getContext(),
                            new ReloadFirebaseCallBack() {
                                @Override
                                public void reload(ArrayList<String> ids) {
                                    if (ids.contains(manualCards.get(currentPosition).getKey())) {
                                        holder.favourite.setImageResource(R.drawable.ic_heart_on);
                                    } else {
                                        holder.favourite.setImageResource(R.drawable.ic_heart_off);
                                    }
                                    //ManualFlyweight.getInstance().reloadContent(context);
                                    Utilities.closeProgressDialog();
                                }
                            });
                }
            });
        } else holder.favourite.setVisibility(View.INVISIBLE);
    }


    @Override
    public int getItemCount() {
        return manualCards.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView imageView;
        private TextView textView;
        private ImageButton favourite;
        private ImageButton delete;
        private OnManualListener onManualListener;

        public ViewHolder(@NonNull View itemView, OnManualListener onSongListener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            textView = itemView.findViewById(R.id.manual_name);
            favourite = itemView.findViewById(R.id.fav_btn);
            delete = itemView.findViewById(R.id.delete_manual);
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
