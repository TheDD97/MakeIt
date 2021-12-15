package com.domslab.makeit.model;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import com.domslab.makeit.adapters.ManualAdapter;

public class RecyclerListener {
    private RecyclerView recyclerView;
    private ManualAdapter.OnManualListener listener;

    public RecyclerListener(RecyclerView recyclerView, ManualAdapter.OnManualListener listener) {
        this.recyclerView = recyclerView;
        this.listener = listener;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public ManualAdapter.OnManualListener getListener() {
        return listener;
    }

    public void setListener(ManualAdapter.OnManualListener listener) {
        this.listener = listener;
    }
}
