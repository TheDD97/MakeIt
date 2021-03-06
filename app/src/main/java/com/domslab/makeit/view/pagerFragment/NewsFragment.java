package com.domslab.makeit.view.pagerFragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.domslab.makeit.R;
import com.domslab.makeit.adapters.ManualAdapter;
import com.domslab.makeit.model.ManualCard;
import com.domslab.makeit.model.ManualFlyweight;
import com.domslab.makeit.view.HomeManual;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NewsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewsFragment extends Fragment implements ManualAdapter.OnManualListener {
    private RecyclerView recyclerView;
    private ArrayList<ManualCard> manualCards;
    private ManualAdapter manualAdapter;
    private boolean hasLoaded = false;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    public NewsFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static NewsFragment newInstance() {
        NewsFragment fragment = new NewsFragment();
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
        return inflater.inflate(R.layout.fragment_news, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = getView().findViewById(R.id.news_list);
        manualCards = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

    }

    @Override
    public void onManualClick(int position) {
        Intent intent = new Intent(getContext(), HomeManual.class);
        getActivity().finish();
        intent.putExtra("manualId", manualCards.get(position).getKey());
        startActivity(intent);
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (menuVisible) {
            manualCards.clear();
            ManualFlyweight.getInstance().setNewestContent("newest", manualCards, recyclerView, NewsFragment.this::onManualClick, getContext());
        }
    }
}