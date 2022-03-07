package com.tst.hytonefinance.Models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class CONTACT {
    @SerializedName("user_name")
    String  name;

    @SerializedName("user_name")
    String  location;

    @SerializedName("user_name")
    String  company;

    @SerializedName("user_name")
    String  number;
    ArrayList<String> email;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CONTACT() {

    }

    public CONTACT(String name, String location, String company, String number, ArrayList<String> email) {
        this.name = name;
        this.location = location;
        this.company = company;
        this.number = number;
        this.email = email;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public ArrayList<String> getEmail() {
        return email;
    }

    public void setEmail(ArrayList<String> email) {
        this.email = email;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }
}
