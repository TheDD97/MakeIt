package com.domslab.makeit.model;

import androidx.annotation.NonNull;

import java.util.HashMap;

public class ManualPage {
    private HashMap<String, Object> pageContent;

    public ManualPage() {
        pageContent = new HashMap<>();
    }

    public ManualPage(HashMap<String, Object> pageContent) {
        this.pageContent = pageContent;
    }

    public HashMap<String, Object> getPageContent() {
        return pageContent;
    }

    public void add(String key, String value) {
        pageContent.put(key, value);
    }

    public void setPageContent(HashMap<String, Object> pageContent) {
        this.pageContent = pageContent;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder content = new StringBuilder();
        for (String k : pageContent.keySet())
            content.append(k +": "+ pageContent.get(k)+"\n");
        return content.toString();
    }
}
