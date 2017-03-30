package com.sjsu.se195.irom.Classes;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Krystle on 10/28/2016.
 */

public class Item implements Parcelable{
    public String uID;
    public Date dateAdded;
    public String name;
    //TODO add picture
    public Integer quantity;
    public String note;
    //maybe have tag as an arraylist of strings?
    //public String tag;
    public boolean forSale;
    private static final int ITEM_ATTRIBUTE_SIZE = 6;

    public String getuID() {
        return uID;
    }

    public void setuID(String uID) {
        this.uID = uID;
    }

    public Item(String uid, Date d, String n, Integer q, String  note){
        this.uID = uid;
        this.dateAdded = d;
        this.name = n;
        this.quantity = q;
        this.note = note;
    }

    public Item(){
    }

    //for parcelable
    public Item(Parcel in){
        Object[] data = new Object[ITEM_ATTRIBUTE_SIZE];
        this.uID = in.readString();
        this.name = in.readString();
        this.dateAdded = (Date) in.readValue(getClass().getClassLoader());
        this.forSale = (Boolean) in.readValue(getClass().getClassLoader());
        this.note = in.readString();
        this.quantity = in.readInt();



    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

//    public String getTag() {
//        return tag;
//    }
//
//    public void setTag(String tag) {
//        this.tag = tag;
//    }

    public Boolean getForSale() {
        return forSale;
    }

    public void setForSale(boolean fs) {
        forSale = fs;
    }

//    @Exclude
//    public Map<String,Object> toMap(){
//        HashMap<String, Object> result = new HashMap<>();
//        result.put("uID", uID);
//        result.put("dateAdded", dateAdded);
//        result.put("name",name);
//        result.put("notes",note);
//        result.put("tags",tag);
//        result.put("forSale",forSale);
//
//        return result;
//    }

    @Override
    public String toString() {
        return name;
    }

    public String toAllString(){
        return "Item{" +
                "uID='" + uID + '\'' +
                ", dateAdded=" + dateAdded +
                ", name='" + name + '\'' +
                ", quantity=" + quantity +
                ", note='" + note + '\'' +
                //", tag='" + tag + '\'' +
                ", forSale=" + forSale +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.uID);
        parcel.writeString(this.name);
        parcel.writeValue(this.dateAdded);
        parcel.writeValue(this.forSale);
        parcel.writeString(this.note);
        parcel.writeInt(this.quantity);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator(){
        public Item createFromParcel(Parcel in){
            return new Item(in);
        }
        public Item[] newArray(int size){
            return new Item[size];
        }
    };
}
