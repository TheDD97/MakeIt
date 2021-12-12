package com.domslab.makeit.view.pagerFragment;

import android.app.ProgressDialog;
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
import java.util.HashMap;
import java.util.List;

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
        if (manualCards.isEmpty())
            loadManual(new FirebaseCallBack() {
                @Override
                public void onCallBack(List<String> list, boolean business, boolean wait) {

                    Collections.sort(manualCards, new Comparator<ManualCard>() {
                        @Override
                        public int compare(ManualCard o1, ManualCard o2) {
                            if (Integer.parseInt(o1.getKey()) > Integer.parseInt(o2.getKey()))
                                return -1;
                            else if (Integer.parseInt(o1.getKey()) < Integer.parseInt(o2.getKey()))
                                return 1;
                            return 0;
                        }
                    });
                    for (ManualCard c : manualCards)
                        System.out.println(c.getKey());
                    hasLoaded = true;
                }
            });
    }

    private void loadManual(FirebaseCallBack callBack) {
        FirebaseDatabase rootNode = FirebaseDatabase.getInstance(Utilities.path);
        DatabaseReference reference = rootNode.getReference();
        StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://makeit-27047.appspot.com/");
        ArrayList<StorageReference> references = new ArrayList<>();
        Query checkUser = reference.child("manual").orderByChild("date").limitToLast(3);
        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
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
                                    byte[] coded = Base64.decode(decodedString, Base64.DEFAULT);
                                    card.setCover(BitmapFactory.decodeByteArray(coded, 0, coded.length));
                                    manualAdapter = new ManualAdapter(getContext(), manualCards, NewsFragment.this::onManualClick);
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
        getActivity().finish();
        intent.putExtra("manualId", manualCards.get(position).getKey());
        // intent.putExtra("manualCover", manualCards.get(position).getCover());
        System.out.println(manualCards.get(position).getKey());
        startActivity(intent);
    }
}