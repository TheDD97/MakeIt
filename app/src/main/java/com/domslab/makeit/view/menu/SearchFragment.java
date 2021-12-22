package com.domslab.makeit.view.menu;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.domslab.makeit.ManualFirebaseCallBack;
import com.domslab.makeit.R;
import com.domslab.makeit.adapters.ManualAdapter;
import com.domslab.makeit.model.ManualCard;
import com.domslab.makeit.model.ManualFlyweight;
import com.domslab.makeit.view.HomeManual;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        System.out.println("HEREEEEEEEEE");
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
        RadioGroup group = getView().findViewById(R.id.group);
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
        group.check(R.id.no_filter);

        ManualFlyweight.getInstance().getLoaded(getContext(), SearchFragment.this::onManualClick, recyclerView, new ManualFirebaseCallBack() {
            @Override
            public void onCallBack(HashMap<String, ManualCard> manual) {
                for (String key : manual.keySet())
                    allCard.add(manual.get(key));
               Collections.sort(allCard, new Comparator<ManualCard>() {
                   @Override
                   public int compare(ManualCard o1, ManualCard o2) {
                       return o1.getName().compareTo(o2.getName());
                   }
               });
                ManualAdapter adapter = new ManualAdapter(getContext(), allCard, SearchFragment.this::onManualClick);
                recyclerView.setAdapter(adapter);
                initSearch();
                filter();
            }
        });

    }


    @Override
    public void onManualClick(int position) {
        Intent intent = new Intent(getContext(), HomeManual.class);
        intent.putExtra("manualId", filteredCard.get(position).getKey());
        System.out.println(filteredCard.get(position).getKey());
        startActivity(intent);
    }

    private void initSearch() {

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search = newText;
                filter();

                /*if (filter == null) {
                    ArrayList<ManualCard> tmp = new ArrayList<>();
                    for (ManualCard c : ManualFlyweight.getInstance().getLoaded())
                        if (c.getName().toLowerCase().contains(newText.toLowerCase()))
                            tmp.add(c);
                    filteredCard = tmp;
                    ManualAdapter adapter = new ManualAdapter(getContext(), tmp, SearchFragment.this::onManualClick);
                    recyclerView.setAdapter(adapter);
                }*/
                return false;
            }
        });
    }

    public void noFilter() {
        filter = null;
        //initSearch();
        filter();
        //filteredCard.clear();
    }

    private void foodFilter() {
        filter = "Food";
        filter();

    }


    private void homeFilter() {
        filter = "Home";
        filter();

        //   filteredCard.clear();
    }

    private void toysFilter() {
        filter = "Toy";
        filter();

        //filteredCard.clear();
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