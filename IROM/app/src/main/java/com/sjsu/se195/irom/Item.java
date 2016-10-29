package com.sjsu.se195.irom;

import java.util.Date;

/**
 * Created by Krystle on 10/28/2016.
 */

public class Item {
    public String itemID;
    public String uID;
    public Date dateAdded;
    public String name;
    //TODO add picture
    public Integer quantity;
    public String note;
    //maybe have tag as an arraylist of strings?
    public String tag;
    public Boolean isForSale;

    public Item(String iid, String uid, Date d, String n, Integer q, String  note, String tag, Boolean forSale){
        this.itemID = iid;
        this.uID = uid;
        this.dateAdded = d;
        this.name = n;
        this.quantity = q;
        this.note = note;
        this.tag = tag;
        this.isForSale = forSale;
    }

    public Item(){

    }
}
