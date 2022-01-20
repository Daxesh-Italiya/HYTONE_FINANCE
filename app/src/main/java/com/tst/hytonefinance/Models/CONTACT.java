package com.tst.hytonefinance.Models;

import java.util.ArrayList;

public class CONTACT {
    String name,location,company,number;
    ArrayList<String> email;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
