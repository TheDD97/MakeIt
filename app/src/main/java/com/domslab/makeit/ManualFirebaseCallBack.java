package com.domslab.makeit;

import com.domslab.makeit.model.ManualCard;

import java.util.HashMap;
import java.util.List;

public interface ManualFirebaseCallBack {
    void onCallBack(HashMap<String, ManualCard> manual);
}
