package com.domslab.makeit;

public class UserHelperClass {
    private String name, surname, email, password;
    private Boolean business;


    public UserHelperClass(String name, String surname, String email, String password, Boolean business) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.business = business;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean isBusiness() {
        return business;
    }

    public void setBusiness(Boolean business) {
        this.business = business;
    }
}
