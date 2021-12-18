package com.domslab.makeit.view.pagerFragment;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.domslab.makeit.FirebaseCallBack;
import com.domslab.makeit.ManualFirebaseCallBack;
import com.domslab.makeit.R;
import com.domslab.makeit.adapters.ManualAdapter;
import com.domslab.makeit.model.ManualCard;
import com.domslab.makeit.model.ManualFlyweight;
import com.domslab.makeit.model.Utilities;
import com.domslab.makeit.view.HomeManual;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyManualFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyManualFragment extends Fragment implements ManualAdapter.OnManualListener {
    private RecyclerView recyclerView;
    private ArrayList<ManualCard> manualCards;
    private ManualAdapter manualAdapter;
    private boolean hasLoaded = false;

    public MyManualFragment() {
        // Required empty public constructor
    }

    public static MyManualFragment newInstance() {
        MyManualFragment fragment = new MyManualFragment();
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
        return inflater.inflate(R.layout.fragment_my_manual, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = getView().findViewById(R.id.my_manual_list);
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
        System.out.println(manualCards.get(position).getKey());
        startActivity(intent);
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (menuVisible) {
            manualCards.clear();
            ManualFlyweight.getInstance().setMyManualContent("myManual", manualCards, recyclerView, MyManualFragment.this::onManualClick, getContext());
        }
    }
}