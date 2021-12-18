package com.domslab.makeit.model;

public class UidManualPair {
    private String Uid;
    private String idManual;

    public UidManualPair(String uid, String idManual) {
        Uid = uid;
        this.idManual = idManual;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public String getIdManual() {
        return idManual;
    }

    public void setIdManual(String idManual) {
        this.idManual = idManual;
    }
}
