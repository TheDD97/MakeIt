package com.domslab.makeit.model;

public class UserHelperClass {
    private String name, surname, email, username;
    private Boolean advanced, waiting;


    public UserHelperClass(String name, String surname, String email, Boolean business, String username,Boolean waiting) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.advanced = business;
        this.username = username;
        this.waiting = waiting;
    }

    public Boolean getWaiting() {
        return waiting;
    }

    public void setWaiting(Boolean waiting) {
        this.waiting = waiting;
    }

    public Boolean getAdvanced() {
        return advanced;
    }

    public void setAdvanced(Boolean advanced) {
        this.advanced = advanced;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
