package com.domslab.makeit.home;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.domslab.makeit.FirebaseCallBack;
import com.domslab.makeit.R;
import com.domslab.makeit.Utilities;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class PagerAdapter extends FragmentPagerAdapter {
    private static int NUM_PAGES = 3;
    private SharedPreferences preferences;
    private LinkedHashMap<String, Fragment> fragments;
    private ArrayList<String> keys;

    public PagerAdapter(@NonNull FragmentManager fm, SharedPreferences preferences) {
        super(fm);
        fragments = new LinkedHashMap<>();
        this.preferences = preferences;
        fragments.put("news", NewsFragment.newInstance());
        fragments.put("favourites", FavouritesFragment.newInstance());
        if (!preferences.getBoolean("advanced", false))
            NUM_PAGES = 2;
        else {
            fragments.put("My Manual", MyManualFragment.newInstance());
            NUM_PAGES = 3;
        }
        keys = new ArrayList(fragments.keySet());

    }


    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragments.get(keys.get(position));


    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return keys.get(position);
    }


}
