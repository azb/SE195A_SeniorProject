package com.sjsu.se195.irom.Classes;

import com.sjsu.se195.irom.Classes.Item;

import java.util.Date;

/**
 * Created by Krystle on 10/28/2016.
 */

public class Listing {
    public String listID;
    public Date dateCreated;
    public String creator;
    public Item item;
    public String description;
    public Boolean isLive;
    public Double price;
    //maybe have tags as an array list of strings?
    public String tag;

    public Listing(){

    }

    public Listing(String c, Item i, String d, Double p){
        creator = c;
        item = i;
        description = d;
        price = p;
        isLive=true;
    }

    @Override
    public String toString() {
        return "Listing{" +
                "creator='" + creator + '\'' +
                ", listID='" + listID + '\'' +
                ", dateCreated=" + dateCreated +
                ", item=" + item.toString() +
                ", description='" + description + '\'' +
                ", isLive=" + isLive +
                ", price=" + price +
                ", tag='" + tag + '\'' +
                '}';
    }
}
