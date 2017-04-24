package com.sjsu.se195.irom.Classes;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Overlord Rick Oliver on 3/27/2017.
 */

public class IROMazon implements Parcelable {
    public String name;
    public ArrayList<String> text = new ArrayList<>();
    public ArrayList<String> logo = new ArrayList<>();
    public ArrayList<String> label = new ArrayList<>();
    // TODO: Store entity data here since name will not be only storage of entity
    public double price;

    /*public double entityScore;
    public double textScore;
    public double logoScore;
    public double labelScore;*/

    public String key;

    public IROMazon(String name, ArrayList<String> text, ArrayList<String> logo, ArrayList<String> label, Double price){
        this.name = name;
        this.price = price;
        for(int i=0;i<text.size();i++)
            this.text.add(text.get(i));
        for(int i=0;i<logo.size();i++)
            this.logo.add(logo.get(i));
        for(int i=0;i<label.size();i++)
            this.label.add(label.get(i));
    }

    public IROMazon(){
    }

    // Parcelable constructor and methods
    public IROMazon(Parcel in) {
        this.name = in.readString();
        this.text = (ArrayList<String>) in.readSerializable();
        this.logo = (ArrayList<String>) in.readSerializable();
        this.label = (ArrayList<String>) in.readSerializable();
        this.price = in.readDouble();
        this.key = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.name);
        parcel.writeSerializable(this.text);
        parcel.writeSerializable(this.logo);
        parcel.writeSerializable(this.label);
        parcel.writeDouble(this.price);
        parcel.writeString(this.key);
    }

    public static final Parcelable.Creator<IROMazon> CREATOR = new Parcelable.Creator<IROMazon>() {
        public IROMazon createFromParcel(Parcel in) {
            return new IROMazon(in);
        }
        public IROMazon[] newArray(int size) {
            return new IROMazon[size];
        }
    };

    //public String getName() {return name;}
    //public void setName(String Name) {this.name = name;}
    //public String getDescription() {return description;}
    //public void setDescription(String description) {this.description = description;}
    //public String getiID() {return iID;}
    //public void setiID(String Key) {this.iID = Key;}
    //public Integer getPrice() {return Price;}
    //public void setKey(Integer Price) {this.Price = Price;}
}
