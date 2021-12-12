package com.domslab.makeit.model;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.domslab.makeit.ManualFirebaseCallBack;
import com.domslab.makeit.adapters.ManualAdapter;
import com.domslab.makeit.view.pagerFragment.MyManualFragment;
import com.domslab.makeit.view.pagerFragment.NewsFragment;
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

public class ManualFactory {

    public void createMyManualList(HashMap<String, ManualCard> manualCards, ManualFirebaseCallBack callBack, RecyclerView recyclerView, Context context, ManualAdapter.OnManualListener onManualListener) {

        FirebaseDatabase rootNode = FirebaseDatabase.getInstance(Utilities.path);
        DatabaseReference reference = rootNode.getReference();
        StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://makeit-27047.appspot.com/");
        Query checkUser = reference.child("manual").orderByChild("owner").equalTo(Utilities.getAuthorisation().getUid());
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
                        manualCards.put(o.getKey(), card);
                        if (o.hasChild("cover")) {
                            gsReference.child(o.getKey() + "/cover").getBytes(Utilities.MAX_FILE_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    String decodedString = new String(bytes);
                                    byte[] coded = Base64.decode(decodedString, Base64.DEFAULT);
                                    card.setCover(BitmapFactory.decodeByteArray(coded, 0, coded.length));
                                    ArrayList<ManualCard> tmpCard = new ArrayList<>();
                                    for (String k : manualCards.keySet())
                                        tmpCard.add(manualCards.get(k));
                                    Collections.sort(tmpCard, new Comparator<ManualCard>() {
                                        @Override
                                        public int compare(ManualCard o1, ManualCard o2) {
                                            if (Integer.parseInt(o1.getKey()) < Integer.parseInt(o2.getKey()))
                                                return 0;
                                            else if (Integer.parseInt(o1.getKey()) > Integer.parseInt(o2.getKey()))
                                                return 1;
                                            return -1;
                                        }
                                    });
                                    ManualAdapter tmp = new ManualAdapter(context, tmpCard, onManualListener);
                                    recyclerView.setAdapter(tmp);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    }
                    callBack.onCallBack(manualCards);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void createNewestList(HashMap<String, ManualCard> manualCards, ManualFirebaseCallBack callBack, RecyclerView recyclerView, Context context, ManualAdapter.OnManualListener onManualListener) {
        FirebaseDatabase rootNode = FirebaseDatabase.getInstance(Utilities.path);
        DatabaseReference reference = rootNode.getReference();
        StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://makeit-27047.appspot.com/");
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
                        manualCards.put(o.getKey(), card);
                        if (o.hasChild("cover")) {
                            gsReference.child(o.getKey() + "/cover").getBytes(Utilities.MAX_FILE_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    String decodedString = new String(bytes);
                                    byte[] coded = Base64.decode(decodedString, Base64.DEFAULT);
                                    card.setCover(BitmapFactory.decodeByteArray(coded, 0, coded.length));
                                    ArrayList<ManualCard> tmpcards = new ArrayList<>();
                                    for (String k : manualCards.keySet())
                                        tmpcards.add(manualCards.get(k));
                                    Collections.sort(tmpcards, new Comparator<ManualCard>() {
                                        @Override
                                        public int compare(ManualCard o1, ManualCard o2) {
                                            if (Integer.parseInt(o1.getKey()) > Integer.parseInt(o2.getKey()))
                                                return -1;
                                            else if (Integer.parseInt(o1.getKey()) < Integer.parseInt(o2.getKey()))
                                                return 1;
                                            return 0;
                                        }
                                    });

                                    ManualAdapter tmp = new ManualAdapter(context, tmpcards, onManualListener);
                                    recyclerView.setAdapter(tmp);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    }
                    callBack.onCallBack(manualCards);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void createFavouriteList(HashMap<String, ManualCard> manualCards, ManualFirebaseCallBack callBack, RecyclerView recyclerView, Context context, ManualAdapter.OnManualListener onManualListener) {
        FirebaseDatabase rootNode = FirebaseDatabase.getInstance(Utilities.path);
        DatabaseReference reference = rootNode.getReference();
        StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://makeit-27047.appspot.com/");
        Query checkUser = reference.child("manual").limitToLast(1);
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
                        manualCards.put(o.getKey(), card);
                        if (o.hasChild("cover")) {
                            gsReference.child(o.getKey() + "/cover").getBytes(Utilities.MAX_FILE_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    String decodedString = new String(bytes);
                                    byte[] coded = Base64.decode(decodedString, Base64.DEFAULT);
                                    card.setCover(BitmapFactory.decodeByteArray(coded, 0, coded.length));
                                    ArrayList<ManualCard> tmpcards = new ArrayList<>();
                                    for (String k : manualCards.keySet())
                                        tmpcards.add(manualCards.get(k));
                                    Collections.sort(tmpcards, new Comparator<ManualCard>() {
                                        @Override
                                        public int compare(ManualCard o1, ManualCard o2) {
                                            if (Integer.parseInt(o1.getKey()) > Integer.parseInt(o2.getKey()))
                                                return -1;
                                            else if (Integer.parseInt(o1.getKey()) < Integer.parseInt(o2.getKey()))
                                                return 1;
                                            return 0;
                                        }
                                    });

                                    ManualAdapter tmp = new ManualAdapter(context, tmpcards, onManualListener);
                                    recyclerView.setAdapter(tmp);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    }
                    callBack.onCallBack(manualCards);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
