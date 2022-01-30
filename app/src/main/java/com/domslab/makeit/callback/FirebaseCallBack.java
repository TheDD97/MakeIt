package com.domslab.makeit.callback;


import java.util.List;

public interface FirebaseCallBack {
    void onCallBack(List<String> list, boolean business, boolean wait);
}
