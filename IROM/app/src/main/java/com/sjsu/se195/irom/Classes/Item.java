package com.sjsu.se195.irom;

import com.google.firebase.database.Exclude;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Krystle on 10/28/2016.
 */

public class Item {
    public String uID;
    public Date dateAdded;
    public String name;
    //TODO add picture
    public Integer quantity;
    public String note;
    //maybe have tag as an arraylist of strings?
    public String tag;
    public boolean isForSale;

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

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Boolean getForSale() {
        return isForSale;
    }

    public void setForSale(Boolean fs) {
        isForSale = fs;
    }

    @Exclude
    public Map<String,Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("uID", uID);
        result.put("dateAdded", dateAdded);
        result.put("name",name);
        result.put("notes",note);
        result.put("tags",tag);
        result.put("isForSale",isForSale);

        return result;
    }

    @Override
    public String toString() {
        return "Item{" +
                "uID='" + uID + '\'' +
                ", dateAdded=" + dateAdded +
                ", name='" + name + '\'' +
                ", quantity=" + quantity +
                ", note='" + note + '\'' +
                ", tag='" + tag + '\'' +
                ", isForSale=" + isForSale +
                '}';
    }
}
