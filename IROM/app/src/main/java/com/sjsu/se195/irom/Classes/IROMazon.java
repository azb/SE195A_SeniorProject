package com.sjsu.se195.irom.Classes;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Overlord Rick Oliver on 3/27/2017.
 */

public class IROMazon implements Parcelable {
    public String name;
    public ArrayList<String> entity = new ArrayList<>();
    public ArrayList<String> text = new ArrayList<>();
    public ArrayList<String> logo = new ArrayList<>();
    public ArrayList<String> label = new ArrayList<>();
    public double price;
    public String description;

    /*public double entityScore;
    public double textScore;
    public double logoScore;
    public double labelScore;*/

    public String key;

    // Keeping old constructor for the sake of not breaking stuff at the moment
    public IROMazon(String name, ArrayList<String> text, ArrayList<String> logo, ArrayList<String> label, Double price) {
        this.name = name;
        this.price = price;
        for (int i=0;i<text.size();i++)
            this.text.add(text.get(i));
        for (int i=0;i<logo.size();i++)
            this.logo.add(logo.get(i));
        for (int i=0;i<label.size();i++)
            this.label.add(label.get(i));
    }

    public IROMazon(String name, ArrayList<String> entity, ArrayList<String> text, ArrayList<String> logo, ArrayList<String> label, Double price){
        this.name = name;
        this.price = price;
        for (String current : entity)
            this.entity.add(current);
        for (int i=0;i<text.size();i++)
            this.text.add(text.get(i));
        for (int i=0;i<logo.size();i++)
            this.logo.add(logo.get(i));
        for (int i=0;i<label.size();i++)
            this.label.add(label.get(i));
    }

    public IROMazon(){
    }

    // Parcelable constructor and methods
    public IROMazon(Parcel in) {
        this.name = in.readString();
        this.entity = (ArrayList<String>) in.readSerializable();
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
        parcel.writeSerializable(this.entity);
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

    // Override equals() method for IROMazon for merging ArrayLists in the SearchActivity
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IROMazon) {
            IROMazon temp = (IROMazon) obj;
            return this.name == temp.name && this.price == temp.price;
        } else {
            return false;
        }
    }


    // Getters/Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getEntity() {
        return entity;
    }

    public void setEntity(ArrayList<String> entity) {
        this.entity = entity;
    }

    public ArrayList<String> getText() {
        return text;
    }

    public void setText(ArrayList<String> text) {
        this.text = text;
    }

    public ArrayList<String> getLogo() {
        return logo;
    }

    public void setLogo(ArrayList<String> logo) {
        this.logo = logo;
    }

    public ArrayList<String> getLabel() {
        return label;
    }

    public void setLabel(ArrayList<String> label) {
        this.label = label;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
