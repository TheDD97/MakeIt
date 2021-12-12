package com.domslab.makeit.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class ManualCard {
    private Bitmap cover;
    private String name;
    private String key;

    public ManualCard() {
    }

    public ManualCard(Bitmap cover, String name) {
        this.cover = cover;
        this.name = name;
    }

    public Bitmap getCover() {
        return cover;
    }

    public void setCover(Bitmap cover) {
        this.cover = cover;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
