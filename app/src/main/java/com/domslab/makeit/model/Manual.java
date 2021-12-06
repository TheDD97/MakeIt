package com.domslab.makeit.model;

import java.util.HashMap;

public class Manual {

    private HashMap<String, ManualPage> content;
    private String name, description, cover, date, category;


    public Manual() {
        content = new HashMap<>();
    }

    public HashMap<String, ManualPage> getContent() {
        return content;
    }

    public void setContent(HashMap<String, ManualPage> content) {
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void addPage(String key, ManualPage content) {
        this.content.put(key, content);
    }

    public ManualPage getPage(String number) {
        if (content.containsKey(number))
            return content.get(number);
        return null;
    }

    public int size() {

        System.out.println(content.size());
        return content.size();
    }
}
