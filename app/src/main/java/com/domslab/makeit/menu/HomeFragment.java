package com.domslab.makeit.menu;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.domslab.makeit.Utilities;
import com.domslab.makeit.home.PagerAdapter;
import com.domslab.makeit.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Utilities.showProgressDialog(getContext(), true);
        ViewPager viewPager = this.getView().findViewById(R.id.vpPager);
        SharedPreferences preferences = this.getActivity().getSharedPreferences(Utilities.sharedPreferencesName, Context.MODE_PRIVATE);
        viewPager.setAdapter(new PagerAdapter(getChildFragmentManager(), preferences));
        viewPager.setCurrentItem(1);
        Utilities.closeProgressDialog();
    }
}