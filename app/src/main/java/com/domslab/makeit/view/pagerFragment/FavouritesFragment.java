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
import com.domslab.makeit.R;
import com.domslab.makeit.adapters.ManualAdapter;
import com.domslab.makeit.model.ManualCard;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FavouritesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FavouritesFragment extends Fragment implements ManualAdapter.OnManualListener {
    private RecyclerView recyclerView;
    private ArrayList<ManualCard> manualCards;
    private ManualAdapter manualAdapter;
    private boolean hasLoaded = false;
    private LinearLayoutManager layoutManager;
    // TODO: Rename and change types of parameters

    public FavouritesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FavouritesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FavouritesFragment newInstance() {
        FavouritesFragment fragment = new FavouritesFragment();
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
        return inflater.inflate(R.layout.fragment_favourites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        recyclerView = getView().findViewById(R.id.favourites_manual_list);
        manualCards = new ArrayList<>();
        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        if (manualCards.isEmpty())
            loadManual(new FirebaseCallBack() {
                @Override
                public void onCallBack(List<String> list, boolean business, boolean wait) {
                    Collections.sort(manualCards, new Comparator<ManualCard>() {
                        @Override
                        public int compare(ManualCard o1, ManualCard o2) {
                            if (Integer.parseInt(o1.getKey()) < Integer.parseInt(o2.getKey()))
                                return 0;
                            else if (Integer.parseInt(o1.getKey()) > Integer.parseInt(o2.getKey()))
                                return 1;
                            else return -1;
                        }
                    });
                    System.out.println("FAV CREATED");
                    hasLoaded = true;
                }
            });

    }


    private void loadManual(FirebaseCallBack callBack) {
        FirebaseDatabase rootNode = FirebaseDatabase.getInstance(Utilities.path);
        DatabaseReference reference = rootNode.getReference();
        StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://makeit-27047.appspot.com/");
        ArrayList<StorageReference> references = new ArrayList<>();
        Query checkUser = reference.child("manual").limitToLast(1);
        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    for (DataSnapshot o : dataSnapshot.getChildren()) {
                        ManualCard card = new ManualCard();
                        card.setKey(o.getKey());
                        if (o.hasChild("name"))
                            card.setName(o.child("name").getValue().toString());
                        manualCards.add(card);
                        if (o.hasChild("cover")) {
                            gsReference.child(o.getKey() + "/cover").getBytes(Utilities.MAX_FILE_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    String decodedString = new String(bytes);
                                    byte[] coded = android.util.Base64.decode(decodedString, Base64.DEFAULT);
                                    card.setCover(BitmapFactory.decodeByteArray(coded, 0, coded.length));
                                    manualAdapter = new ManualAdapter(getContext(), manualCards, FavouritesFragment.this::onManualClick);
                                    recyclerView.setAdapter(manualAdapter);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    }
                    manualAdapter = new ManualAdapter(getContext(), manualCards, FavouritesFragment.this::onManualClick);
                    recyclerView.setAdapter(manualAdapter);
                }
                callBack.onCallBack(null, false, false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    @Override
    public void onManualClick(int position) {
        Intent intent = new Intent(getContext(), HomeManual.class);
        intent.putExtra("manualId", manualCards.get(position).getKey());
        // intent.putExtra("manualCover", manualCards.get(position).getCover());
        System.out.println(manualCards.get(position).getKey());
        startActivity(intent);
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (menuVisible && !hasLoaded) {

        }
    }
}