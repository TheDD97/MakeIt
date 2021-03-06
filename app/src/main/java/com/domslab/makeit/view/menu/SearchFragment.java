package com.domslab.makeit.view.menu;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.domslab.makeit.callback.ManualFirebaseCallBack;
import com.domslab.makeit.R;
import com.domslab.makeit.adapters.ManualAdapter;
import com.domslab.makeit.model.ManualCard;
import com.domslab.makeit.model.ManualFlyweight;
import com.domslab.makeit.model.Utilities;
import com.domslab.makeit.view.HomeManual;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment implements ManualAdapter.OnManualListener {
    private RecyclerView recyclerView;
    private ArrayList<ManualCard> filteredCard;
    private ArrayList<ManualCard> allCard;
    private RecyclerView.LayoutManager layoutManager;
    private RadioButton allFilter, foodFilter, toyFilter, homeFilter;
    private SearchView searchView;
    private RadioGroup group;
    private String filter = null;
    private String search = "";

    public SearchFragment() {
        // Required empty public constructor
    }


    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        allCard = new ArrayList<>();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);

    }

    @Override
    public void onResume() {
        super.onResume();
        noFilter();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = getView().findViewById(R.id.manual_list_view);
        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        filteredCard = new ArrayList<>();
        searchView = getView().findViewById(R.id.manual_list_search);
        allFilter = getView().findViewById(R.id.no_filter);
        toyFilter = getView().findViewById(R.id.toys_filter);
        foodFilter = getView().findViewById(R.id.food_filter);
        homeFilter = getView().findViewById(R.id.home_filter);
        toyFilter.setText(Utilities.ToyLabel);
        foodFilter.setText(Utilities.FoodLabel);
        homeFilter.setText(Utilities.HomeLabel);
        group = getView().findViewById(R.id.group);
        allFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noFilter();
            }
        });
        toyFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toysFilter();
            }
        });
        homeFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeFilter();
            }
        });
        foodFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                foodFilter();
            }
        });

        ManualFlyweight.getInstance().getLoaded(getContext(), SearchFragment.this::onManualClick, recyclerView, new ManualFirebaseCallBack() {
            @Override
            public void onCallBack(HashMap<String, ManualCard> manual) {
                for (String key : manual.keySet())
                    allCard.add(manual.get(key));
                group.check(R.id.no_filter);
                initSearch();
                filter();
            }
        });
    }


    @Override
    public void onManualClick(int position) {
        Intent intent = new Intent(getContext(), HomeManual.class);
        intent.putExtra("manualId", filteredCard.get(position).getKey());
        startActivity(intent);
    }

    private void initSearch() {
        ManualAdapter adapter = new ManualAdapter(getContext(), allCard, SearchFragment.this::onManualClick);
        recyclerView.setAdapter(adapter);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                search = newText;
                filter();
                return false;
            }
        });
    }

    public void noFilter() {
        filter = null;
        filter();
    }

    private void foodFilter() {
        filter = "Food";
        filter();
    }


    private void homeFilter() {
        filter = "Home";
        filter();
    }

    private void toysFilter() {
        filter = "Toy";
        filter();
    }

    private void filter() {
        ArrayList<ManualCard> filtered = new ArrayList<>();
        for (ManualCard c : allCard) {
            if (filter != null) {
                if (c.getCategory().equalsIgnoreCase(filter))
                    if (search == "")
                        filtered.add(c);
                    else if (c.getName().toLowerCase().contains(search.toLowerCase()))
                        filtered.add(c);
            } else if (c.getName().toLowerCase().contains(search.toLowerCase()))
                filtered.add(c);
        }
        filteredCard = filtered;
        ManualAdapter manualAdapter = new ManualAdapter(getContext(), filtered, SearchFragment.this::onManualClick);
        recyclerView.setAdapter(manualAdapter);
    }
}