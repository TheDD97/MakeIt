package com.domslab.makeit.model;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.domslab.makeit.FavouriteFirebaseCallBack;
import com.domslab.makeit.FirebaseCallBack;
import com.domslab.makeit.ManualFirebaseCallBack;
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
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class ManualFlyweight {
    private static ManualFlyweight instance = null;
    private static HashMap<String, ManualCard> myManual;
    private static HashMap<String, ManualCard> favourite;
    private static HashMap<String, ManualCard> newest;
    private static HashMap<String, ManualCard> loaded;
    private static ManualFactory manualFactory;
    private Context context;
    private static HashMap<String, RecyclerListener> recyclerListener;

    private ManualFlyweight() {
        myManual = new HashMap<>();
        favourite = new HashMap<>();
        newest = new HashMap<>();
        loaded = new HashMap<>();
        manualFactory = new ManualFactory();
        recyclerListener = new HashMap<>();
    }

    public static ManualFlyweight getInstance() {
        if (instance == null)
            instance = new ManualFlyweight();
        return instance;
    }

    public void setMyManualContent(String s, ArrayList<ManualCard> manualCards, RecyclerView recyclerView, ManualAdapter.OnManualListener onManualListener, Context context) {

        if (this.context != context)
            this.context = context;
        RecyclerListener recyclerListener = new RecyclerListener(recyclerView, onManualListener);
        this.recyclerListener.put("myManual", recyclerListener);
        if (s.equals("myManual") && myManual.isEmpty()) {
            Utilities.showProgressDialog(context, true);
            manualFactory.createMyManualList(myManual, loaded, new ManualFirebaseCallBack() {
                @Override
                public void onCallBack(HashMap<String, ManualCard> manualCardHashMap) {
                    setContent(manualCards, myManual, false);
                    ManualAdapter tmp = new ManualAdapter(context, manualCards, onManualListener,true,true);
                    recyclerView.setAdapter(tmp);
                    Utilities.closeProgressDialog();
                }
            }, recyclerView, context, onManualListener);
        } else if (!myManual.isEmpty()) {
            setContent(manualCards, myManual, false);
            ManualAdapter tmp = new ManualAdapter(context, manualCards, onManualListener,true,true);
            recyclerView.setAdapter(tmp);
            Utilities.closeProgressDialog();
        }

    }


    public void setNewestContent(String s, ArrayList<ManualCard> manualCards, RecyclerView recyclerView, ManualAdapter.OnManualListener onManualListener, Context context) {
        if (this.context != context)
            this.context = context;
        Utilities.showProgressDialog(context, true);
        RecyclerListener recyclerListener = new RecyclerListener(recyclerView, onManualListener);
        this.recyclerListener.put("newest", recyclerListener);
        manualCards.clear();
        if (s.equals("newest") && newest.isEmpty())
            manualFactory.createNewestList(newest, loaded, new ManualFirebaseCallBack() {
                @Override
                public void onCallBack(HashMap<String, ManualCard> manualCardHashMap) {
                    setContent(manualCards, newest, true);
                    Utilities.closeProgressDialog();
                }
            }, recyclerView, context, onManualListener);
        else if (!newest.isEmpty()) {
            setContent(manualCards, newest, true);
            ManualAdapter tmp = new ManualAdapter(context, manualCards, onManualListener);
            recyclerView.setAdapter(tmp);
            Utilities.closeProgressDialog();
        }

    }

    public void setFavouriteContent(String s, ArrayList<ManualCard> manualCards, RecyclerView recyclerView, ManualAdapter.OnManualListener onManualListener, Context context) {
        if (this.context != context)
            this.context = context;
        //favourite.clear();
        if (s.equals("favourite") && favourite.isEmpty()){
            manualFactory.createFavouriteList(favourite, new ManualFirebaseCallBack() {
                @Override
                public void onCallBack(HashMap<String, ManualCard> manualCardHashMap) {
                    setContent(manualCards, favourite, true);
                    Utilities.closeProgressDialog();
                }
            }, recyclerView, context, onManualListener);
        }
        else if (!favourite.isEmpty()) {
            setContent(manualCards, favourite, true);
            ManualAdapter tmp = new ManualAdapter(context, manualCards, onManualListener, false, false);
            recyclerView.setAdapter(tmp);
            RecyclerListener recyclerListener = new RecyclerListener(recyclerView, onManualListener);
            this.recyclerListener.put("favourite", recyclerListener);
            Utilities.closeProgressDialog();
        }

    }

    private void setContent(ArrayList<ManualCard> manualCards, HashMap<String, ManualCard> manual, boolean descending) {

        for (String key : manual.keySet())
            manualCards.add(manual.get(key));
        if (descending)
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
        else
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
    }

    public void addManual(String id, Manual manual) {
        ManualCard card = new ManualCard();
        card.setName(manual.getName());
        byte[] coded = Base64.getDecoder().decode(manual.getCover());
        card.setCover(BitmapFactory.decodeByteArray(coded, 0, coded.length));
        card.setKey(id);
        if (!myManual.containsKey(id)) {
            myManual.put(id, card);
            ArrayList<ManualCard> manualCards = new ArrayList<>();
            setContent(manualCards, myManual, false);
            if (recyclerListener.containsKey("myManual")) {
                ManualAdapter tmp = new ManualAdapter(context, manualCards, recyclerListener.get("myManual").getListener(),true,true);
                recyclerListener.get("myManual").getRecyclerView().setAdapter(tmp);
            }

        }
        if (!newest.containsKey(id)) {
            newest.put(id, card);
            ArrayList<ManualCard> manualCards = new ArrayList<>();
            setContent(manualCards, newest, true);
            if (recyclerListener.containsKey("newest")) {
                ManualAdapter tmp = new ManualAdapter(context, manualCards, recyclerListener.get("newest").getListener());
                recyclerListener.get("newest").getRecyclerView().setAdapter(tmp);
            }
        }
        if (!loaded.containsKey(id)) {
            loaded.put(id, card);
        }
    }

    public void updateManual(String id, Context context, FavouriteFirebaseCallBack callBack) {
        ArrayList<String> tmpId = new ArrayList<>();
        ArrayList<ManualCard> cd = new ArrayList<>();
        setContent(cd, favourite, true);
        for (ManualCard c : cd)
            tmpId.add(c.getKey());
        if (!favourite.containsKey(id)) {
            manualFactory.updateFavourite(id, false, context, tmpId, new FavouriteFirebaseCallBack() {
                @Override
                public void loadFavourite(ArrayList<String> ids) {
                    for (String k : ids) {
                        if (!favourite.containsKey(k)) {
                            if (myManual.containsKey(k))
                                favourite.put(k, myManual.get(k));
                            else if (newest.containsKey(k))
                                favourite.put(k, newest.get(k));
                            else if (loaded.containsKey(k))
                                favourite.put(k, loaded.get(k));
                        }
                    }

                    ArrayList<ManualCard> manualCardsFavourite = new ArrayList<>();
                    setContent(manualCardsFavourite, favourite, true);

                    if (recyclerListener.containsKey("favourite")) {
                        ManualAdapter tmp = new ManualAdapter(context, manualCardsFavourite, recyclerListener.get("favourite").getListener(), false,false);
                        recyclerListener.get("favourite").getRecyclerView().setAdapter(tmp);
                    }

                    callBack.loadFavourite(ids);
                }

            });
        } else {
            favourite.remove(id);
            ArrayList<ManualCard> manualCardsFavourite = new ArrayList<>();
            setContent(manualCardsFavourite, favourite, true);

            if (recyclerListener.containsKey("favourite")) {
                ManualAdapter tmp = new ManualAdapter(context, manualCardsFavourite, recyclerListener.get("favourite").getListener(), false, false);
                recyclerListener.get("favourite").getRecyclerView().setAdapter(tmp);
            }

            manualFactory.updateFavourite(id, true, context, tmpId, new FavouriteFirebaseCallBack() {
                @Override
                public void loadFavourite(ArrayList<String> ids) {
                    callBack.loadFavourite(ids);
                }
            });
        }

    }

    public boolean isFavourite(String key) {
        return favourite.containsKey(key);
    }

    public void reloadContent(Context context) {
        ArrayList<ManualCard> myTmp = new ArrayList<>();
        ArrayList<ManualCard> newTmp = new ArrayList<>();
        ArrayList<ManualCard> fav = new ArrayList<>();
        setContent(fav, favourite, true);
        for (ManualCard card : fav) {
            if (myManual.containsKey(card.getKey())) {
                myManual.get(card.getKey()).setFavourite(card.getFavourite());
                // myTmp.clear();
                setContent(myTmp, myManual, false);
                ManualAdapter tmp = new ManualAdapter(context, myTmp, recyclerListener.get("myManual").getListener(),true,true);
                recyclerListener.get("myManual").getRecyclerView().setAdapter(tmp);
            }

            if (newest.containsKey(card.getKey())) {
                newest.get(card.getKey()).setFavourite(card.getFavourite());
                newTmp.clear();
                setContent(newTmp, newest, true);
                ManualAdapter tmp = new ManualAdapter(context, newTmp, recyclerListener.get("newest").getListener());
                recyclerListener.get("newest").getRecyclerView().setAdapter(tmp);
            }
        }
    }

    public void getLoaded(Context c, ManualAdapter.OnManualListener listener, RecyclerView recyclerView, ManualFirebaseCallBack callBack) {
        Utilities.showProgressDialog(c, true);
        for (String k : myManual.keySet())
            if (!loaded.containsKey(k))
                loaded.put(k, myManual.get(k));
        for (String k : favourite.keySet())
            if (!loaded.containsKey(k))
                loaded.put(k, favourite.get(k));
        for (String k : newest.keySet())
            if (!loaded.containsKey(k))
                loaded.put(k, newest.get(k));
        manualFactory.loadAllManual(loaded, c, listener, recyclerView, new ManualFirebaseCallBack() {
            @Override
            public void onCallBack(HashMap<String, ManualCard> manual) {
                callBack.onCallBack(loaded);
                Utilities.closeProgressDialog();

            }
        });
    }
}