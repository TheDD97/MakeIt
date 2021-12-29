package com.domslab.makeit.adapters;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.domslab.makeit.view.pagerFragment.FavouritesFragment;
import com.domslab.makeit.view.pagerFragment.MyManualFragment;
import com.domslab.makeit.view.pagerFragment.NewsFragment;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class PagerAdapter /*extends FragmentPagerAdapter*/ extends FragmentStateAdapter {
    private static int NUM_PAGES = 3;
    private SharedPreferences preferences;
    private LinkedHashMap<String, Fragment> fragments;
    private ArrayList<String> keys;

    public PagerAdapter(@NonNull FragmentActivity fragmentActivity, SharedPreferences preferences) {
        super(fragmentActivity);
        fragments = new LinkedHashMap<>();
        this.preferences = preferences;
        fragments.put("Novità", NewsFragment.newInstance());
        fragments.put("Preferiti", FavouritesFragment.newInstance());
        if (!preferences.getBoolean("advanced", false))
            NUM_PAGES = 2;
        else {
            fragments.put("I miei manuali", MyManualFragment.newInstance());
            NUM_PAGES = 3;
        }
        keys = new ArrayList(fragments.keySet());
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(keys.get(position));
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }

    public String getTitle(int position) {
        if (position < keys.size())
            return keys.get(position);
        return "";
    }
}
