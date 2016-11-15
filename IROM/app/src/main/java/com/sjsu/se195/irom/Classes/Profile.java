package com.sjsu.se195.irom;

/**
 * Created by Krystle on 10/28/2016.
 */

public class Profile {
    public  String uID;
    public String firstName;
    public  String lastName;
    public String currency;

    // TODO: add timezone, location

    public Profile(){

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
}
