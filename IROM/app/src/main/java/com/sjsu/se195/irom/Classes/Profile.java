package com.sjsu.se195.irom;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Krystle on 10/28/2016.
 */

public class Profile {
    public  String uID;
    public String firstName;
    public String lastName;
    public String currency;

    // TODO: add timezone, location

    public Profile(){

    }
    public Profile(String uid, String first, String last, String curr){
        uID = uid;
        firstName = first;
        lastName = last;
        currency = curr;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getCurrency() {
        return currency;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Exclude
    public Map<String,Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("uID", uID);
        result.put("first name", firstName);
        result.put("last name",lastName);
        result.put("currency",currency);

        return result;
    }
}
