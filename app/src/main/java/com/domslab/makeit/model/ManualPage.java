package com.domslab.makeit.model;

import androidx.annotation.NonNull;

import java.util.HashMap;

public class ManualPage {
    private HashMap<String, String> pageContent;

    public ManualPage() {
        pageContent = new HashMap<>();
    }

    public ManualPage(HashMap<String, String> pageContent) {
        this.pageContent = pageContent;
    }

    public HashMap<String, String> getPageContent() {
        return pageContent;
    }

    public void add(String key, String value) {
        pageContent.put(key, value);
    }

    public void setPageContent(HashMap<String, String> pageContent) {
        this.pageContent = pageContent;
    }
    public boolean hasItem(String item){
        return pageContent.containsKey(item);
    }
    public String getItem(String item){
        return pageContent.get(item);
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
