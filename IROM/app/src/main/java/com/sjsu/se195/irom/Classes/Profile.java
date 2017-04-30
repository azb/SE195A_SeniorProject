package com.sjsu.se195.irom.Classes;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Krystle on 10/28/2016.
 */

public class Profile implements Parcelable {
    public String uID;
    public String firstName;
    public String lastName;
    //public String currency;

    // TODO: add timezone, location

    public Profile(){

    }
    public Profile(String uid, String first, String last, String curr){
        uID = uid;
        firstName = first;
        lastName = last;
    }
    public Profile(String uid, String first, String last){
        uID = uid;
        firstName = first;
        lastName = last;
    }

    // Parcelable constructor and methods
    public Profile(Parcel in) {
        this.uID = in.readString();
        this.firstName = in.readString();
        this.lastName = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.uID);
        parcel.writeString(this.firstName);
        parcel.writeString(this.lastName);
    }

    public static final Parcelable.Creator<Profile> CREATOR = new Parcelable.Creator<Profile>() {
        public Profile createFromParcel(Parcel in) {
            return new Profile(in);
        }
        public Profile[] newArray(int size) {
            return new Profile[size];
        }
    };

    @Exclude
    public Map<String,Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("uID", uID);
        result.put("first name", firstName);
        result.put("last name",lastName);

        return result;
    }

    // Getters/Setters
    public String getuID() {
        return uID;
    }

    public void setuID(String uID) {
        this.uID = uID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
