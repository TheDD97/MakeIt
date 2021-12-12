package com.domslab.makeit.model;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import com.domslab.makeit.ManualFirebaseCallBack;
import com.domslab.makeit.adapters.ManualAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class ManualFlyweight {
    private static ManualFlyweight instance = null;
    private static HashMap<String, ManualCard> myManual;
    private static HashMap<String, ManualCard> favourite;
    private static HashMap<String, ManualCard> newest;
    private static ManualFactory manualFactory;

    private ManualFlyweight() {
        myManual = new HashMap<>();
        favourite = new HashMap<>();
        newest = new HashMap<>();
        manualFactory = new ManualFactory();
    }

    public static ManualFlyweight getInstance() {
        if (instance == null)
            instance = new ManualFlyweight();
        return instance;
    }

    public void setMyManualContent(String s, ArrayList<ManualCard> manualCards, RecyclerView recyclerView, ManualAdapter.OnManualListener onManualListener, Context context) {

        if (s.equals("my manual") && myManual.isEmpty())
            manualFactory.createMyManualList(myManual, new ManualFirebaseCallBack() {
                @Override
                public void onCallBack(HashMap<String, ManualCard> manualCardHashMap) {
                    setContent(manualCards, myManual, false);
                }
            }, recyclerView, context, onManualListener);
        else if (!myManual.isEmpty()) {
            setContent(manualCards, myManual, false);
            for (ManualCard card : manualCards)
                System.out.println(card.getKey());
            ManualAdapter tmp = new ManualAdapter(context, manualCards, onManualListener);
            recyclerView.setAdapter(tmp);
        }

    }


    public void setNewestContent(String s, ArrayList<ManualCard> manualCards, RecyclerView recyclerView, ManualAdapter.OnManualListener onManualListener, Context context) {

        if (s.equals("newest") && newest.isEmpty())
            manualFactory.createNewestList(newest, new ManualFirebaseCallBack() {
                @Override
                public void onCallBack(HashMap<String, ManualCard> manualCardHashMap) {
                    setContent(manualCards, newest, true);
                }
            }, recyclerView, context, onManualListener);
        else if (!newest.isEmpty()) {
            setContent(manualCards, newest, true);
            ManualAdapter tmp = new ManualAdapter(context, manualCards, onManualListener);
            recyclerView.setAdapter(tmp);
        }

    }

    public void setFavouriteContent(String s, ArrayList<ManualCard> manualCards, RecyclerView recyclerView, ManualAdapter.OnManualListener onManualListener, Context context) {

        if (s.equals("favourite") && favourite.isEmpty())
            manualFactory.createFavouriteList(favourite, new ManualFirebaseCallBack() {
                @Override
                public void onCallBack(HashMap<String, ManualCard> manualCardHashMap) {
                    setContent(manualCards, favourite, true);
                }
            }, recyclerView, context, onManualListener);
        else if (!favourite.isEmpty()) {
            setContent(manualCards, favourite, true);
            ManualAdapter tmp = new ManualAdapter(context, manualCards, onManualListener);
            recyclerView.setAdapter(tmp);
        }

    }

    private void setContent(ArrayList<ManualCard> manualCards, HashMap<String, ManualCard> myManual, boolean descending) {
        for (String key : myManual.keySet())
            manualCards.add(myManual.get(key));
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
}