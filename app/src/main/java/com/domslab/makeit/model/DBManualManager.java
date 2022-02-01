package com.domslab.makeit.model;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.domslab.makeit.callback.ReloadFirebaseCallBack;
import com.domslab.makeit.callback.ManualFirebaseCallBack;
import com.domslab.makeit.adapters.ManualAdapter;
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

public class DBManualManager {

    public void createMyManualList(HashMap<String, ManualCard> manualCards, HashMap<String, ManualCard> loaded, ManualFirebaseCallBack callBack, RecyclerView recyclerView, Context context, ManualAdapter.OnManualListener onManualListener) {
        FirebaseDatabase rootNode = FirebaseDatabase.getInstance(Utilities.path);
        DatabaseReference reference = rootNode.getReference();
        StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://makeit-27047.appspot.com/");
        Query checkUser = reference.child("manual").orderByChild("owner").equalTo(Utilities.getAuthorisation().getCurrentUser().getUid().toString());
        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ArrayList<ManualCard> tmpCard = new ArrayList<>();

                    for (DataSnapshot o : dataSnapshot.getChildren()) {
                        if (loaded.containsKey(o.getKey())) {
                            manualCards.put(o.getKey(), loaded.get(o.getKey()));
                            for (String k : manualCards.keySet())
                                if (!tmpCard.contains(manualCards.get(k)))
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
                            ManualAdapter tmp = new ManualAdapter(context, tmpCard, onManualListener, true, true);
                            recyclerView.setAdapter(tmp);

                        } else {
                            ManualCard card = new ManualCard();
                            card.setKey(o.getKey());
                            if (o.hasChild("name"))
                                card.setName(o.child("name").getValue().toString());
                            manualCards.put(o.getKey(), card);
                            if (o.hasChild("category"))
                                card.setCategory(o.child("category").getValue().toString());
                            if (ManualFlyweight.getInstance().isFavourite(o.getKey()))
                                card.setFavourite(true);
                            if (o.hasChild("cover")) {
                                gsReference.child(o.getKey() + "/cover").getBytes(Utilities.MAX_FILE_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        String decodedString = new String(bytes);
                                        byte[] coded = Base64.decode(decodedString, Base64.DEFAULT);
                                        card.setCover(BitmapFactory.decodeByteArray(coded, 0, coded.length));

                                        for (String k : manualCards.keySet())
                                            if (!tmpCard.contains(manualCards.get(k)))
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
                                        ManualAdapter tmp = new ManualAdapter(context, tmpCard, onManualListener, true, true);
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
                    }
                }
                callBack.onCallBack(manualCards);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void createNewestList(HashMap<String, ManualCard> manualCards, HashMap<String, ManualCard> loaded, ManualFirebaseCallBack callBack, RecyclerView recyclerView, Context context, ManualAdapter.OnManualListener onManualListener) {
        ManualFlyweight manualFlyweight = ManualFlyweight.getInstance();
        FirebaseDatabase rootNode = FirebaseDatabase.getInstance(Utilities.path);
        DatabaseReference reference = rootNode.getReference();
        StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://makeit-27047.appspot.com/");
        Query checkUser = reference.child("manual").orderByChild("date").limitToLast(5);
        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ArrayList<ManualCard> tmpCard = new ArrayList<>();

                    for (DataSnapshot o : dataSnapshot.getChildren()) {
                        if (loaded.containsKey(o.getKey())) {
                            manualCards.put(o.getKey(), loaded.get(o.getKey()));
                            for (String k : manualCards.keySet())
                                if (!tmpCard.contains(manualCards.get(k)))
                                    tmpCard.add(manualCards.get(k));
                            Collections.sort(tmpCard, new Comparator<ManualCard>() {
                                @Override
                                public int compare(ManualCard o1, ManualCard o2) {
                                    if (Integer.parseInt(o1.getKey()) > Integer.parseInt(o2.getKey()))
                                        return -1;
                                    else if (Integer.parseInt(o1.getKey()) < Integer.parseInt(o2.getKey()))
                                        return 1;
                                    return 0;
                                }
                            });
                            ManualAdapter tmp = new ManualAdapter(context, tmpCard, onManualListener);
                            recyclerView.setAdapter(tmp);
                        } else {
                            ManualCard card = new ManualCard();
                            card.setKey(o.getKey());
                            if (o.hasChild("name"))
                                card.setName(o.child("name").getValue().toString());
                            if (o.hasChild("category"))
                                card.setCategory(o.child("category").getValue().toString());
                            manualCards.put(o.getKey(), card);
                            if (manualFlyweight.isFavourite(o.getKey()))
                                card.setFavourite(true);
                            if (o.hasChild("cover")) {
                                gsReference.child(o.getKey() + "/cover").getBytes(Utilities.MAX_FILE_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        String decodedString = new String(bytes);
                                        byte[] coded = Base64.decode(decodedString, Base64.DEFAULT);
                                        card.setCover(BitmapFactory.decodeByteArray(coded, 0, coded.length));
                                        for (String k : manualCards.keySet())
                                            if (!tmpCard.contains(manualCards.get(k)))
                                                tmpCard.add(manualCards.get(k));
                                        Collections.sort(tmpCard, new Comparator<ManualCard>() {
                                            @Override
                                            public int compare(ManualCard o1, ManualCard o2) {
                                                if (Integer.parseInt(o1.getKey()) > Integer.parseInt(o2.getKey()))
                                                    return -1;
                                                else if (Integer.parseInt(o1.getKey()) < Integer.parseInt(o2.getKey()))
                                                    return 1;
                                                return 0;
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
                    }
                }
                callBack.onCallBack(manualCards);

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
        // Utilities.showProgressDialog(context, true);
        loadFavourite(new ReloadFirebaseCallBack() {
            @Override
            public void reload(ArrayList<String> favouriteIds) {
                if (!favouriteIds.isEmpty()) {
                    Utilities.showProgressDialog(context);
                    ArrayList<String> ids = favouriteIds;
                    Query checkUser = reference.child("manual");
                    checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot o : dataSnapshot.getChildren()) {
                                    if (ids.contains(o.getKey())) {
                                        ManualCard card = new ManualCard();
                                        card.setKey(o.getKey());
                                        if (o.hasChild("name"))
                                            card.setName(o.child("name").getValue().toString());
                                        if (o.hasChild("category"))
                                            card.setCategory(o.child("category").getValue().toString());
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

                                                    ManualAdapter tmp = new ManualAdapter(context, tmpcards, onManualListener, false, false);
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
                                }

                                Utilities.closeProgressDialog();
                            }
                            callBack.onCallBack(manualCards);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });
    }

    private void loadFavourite(ReloadFirebaseCallBack callBack) {
        FirebaseDatabase rootNode = FirebaseDatabase.getInstance(Utilities.path);
        DatabaseReference reference = rootNode.getReference();
        ArrayList<String> favouriteIds = new ArrayList<>();
        Query checkUser = reference.child("favourites");
        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot o : snapshot.getChildren()) {
                        if (o.child("uid").getValue().toString().equals(Utilities.getAuthorisation().getCurrentUser().getUid())) {
                            favouriteIds.add(o.child("idManual").getValue().toString());
                        }
                    }
                    callBack.reload(favouriteIds);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void updateFavourite(String id, boolean exist, Context context, ArrayList<String> ids, ReloadFirebaseCallBack callBack) {
        FirebaseDatabase rootNode = FirebaseDatabase.getInstance(Utilities.path);
        DatabaseReference reference = rootNode.getReference();

        if (exist) {
            Query checkUser = reference.child("favourites");
            checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Utilities.showProgressDialog(context);
                        for (DataSnapshot o : snapshot.getChildren()) {
                            if (o.child("uid").getValue().toString().equals(Utilities.getAuthorisation().getUid()))
                                if (o.child("idManual").getValue().toString().equals(id)) {
                                    o.getRef().removeValue(new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                            ids.remove(o.child("idManual").getValue().toString());
                                            Utilities.closeProgressDialog();
                                            callBack.reload(ids);
                                        }
                                    });
                                }
                        }
                        // Utilities.closeProgressDialog();


                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            Utilities.showProgressDialog(context);
            UidManualPair uidManualPair = new UidManualPair(Utilities.getAuthorisation().getUid(), id);
            String key = reference.child("favourites").push().getKey();
            ids.add(uidManualPair.getIdManual());
            reference.child("favourites").child(key).setValue(uidManualPair);
            Utilities.closeProgressDialog();
            callBack.reload(ids);


        }
    }

    public void loadAllManual(HashMap<String, ManualCard> loaded, Context context, ManualAdapter.OnManualListener onManualListener, RecyclerView recyclerView, ManualFirebaseCallBack manualFirebaseCallBack) {
        ManualFlyweight manualFlyweight = ManualFlyweight.getInstance();
        FirebaseDatabase rootNode = FirebaseDatabase.getInstance(Utilities.path);
        DatabaseReference reference = rootNode.getReference();
        StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://makeit-27047.appspot.com/");
        Query checkUser = reference.child("manual");
        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot o : dataSnapshot.getChildren()) {
                        if (!loaded.containsKey(o.getKey())) {
                            ManualCard card = new ManualCard();
                            card.setKey(o.getKey());
                            if (o.hasChild("name"))
                                card.setName(o.child("name").getValue().toString());
                            if (o.hasChild("category"))
                                card.setCategory(o.child("category").getValue().toString());
                            loaded.put(o.getKey(), card);
                            if (manualFlyweight.isFavourite(o.getKey()))
                                card.setFavourite(true);
                            if (o.hasChild("cover")) {
                                gsReference.child(o.getKey() + "/cover").getBytes(Utilities.MAX_FILE_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        String decodedString = new String(bytes);
                                        byte[] coded = Base64.decode(decodedString, Base64.DEFAULT);
                                        card.setCover(BitmapFactory.decodeByteArray(coded, 0, coded.length));
                                        ArrayList<ManualCard> tmpcards = new ArrayList<>();
                                        for (String k : loaded.keySet())
                                            tmpcards.add(loaded.get(k));
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
                    }
                }
                manualFirebaseCallBack.onCallBack(loaded);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void deleteManual(String id, Context context, ReloadFirebaseCallBack reloadFirebaseCallBack) {
        FirebaseDatabase rootNode = FirebaseDatabase.getInstance(Utilities.path);
        DatabaseReference reference = rootNode.getReference();
        Query checkUser = reference.child("manual");
        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Utilities.showProgressDialog(context);
                    for (DataSnapshot o : snapshot.getChildren()) {
                        if (o.getKey().equals(id))
                            if (o.child("owner").getValue().toString().equals(Utilities.getAuthorisation().getCurrentUser().getUid())) {
                                o.getRef().removeValue(new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                        reloadFirebaseCallBack.reload(null);
                                    }
                                });
                            }
                    }
                    // Utilities.closeProgressDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
