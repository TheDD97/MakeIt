package com.domslab.makeit.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class ManualCard {
    private Bitmap cover;
    private String name;
    private String key;
    private Boolean isFavourite;

    public ManualCard() {
        isFavourite = false;
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Boolean getFavourite() {
        return isFavourite;
    }

    public void setFavourite(Boolean favourite) {
        isFavourite = favourite;
    }
}
