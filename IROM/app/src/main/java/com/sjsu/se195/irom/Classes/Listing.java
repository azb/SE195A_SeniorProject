package com.sjsu.se195.irom.Classes;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by Krystle on 10/28/2016.
 */

public class Listing implements Parcelable{
    public String listID; // Set at Listing creation
    public Date dateCreated;
    public String creator;
    public Item item;
    public String description;
    public Boolean isLive;
    public Double price;
    //maybe have tags as an array list of strings?
    //public String tag;

    public Listing(){

    }

    public Listing(String c, Item i, String d, Double p){
        creator = c;
        item = i;
        description = d;
        price = p;
        isLive=true;
    }

    public Listing(Parcel in) {
        listID = in.readString();
        dateCreated = (Date) in.readValue(getClass().getClassLoader());
        creator = in.readString();
        item = (Item) in.readValue(getClass().getClassLoader());
        description = in.readString();
        isLive = (boolean) in.readValue(getClass().getClassLoader());
        price = in.readDouble();
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
               // ", tag='" + tag + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.listID);
        parcel.writeValue(this.dateCreated);
        parcel.writeString(this.creator);
        parcel.writeValue(this.item);
        parcel.writeString(this.description);
        parcel.writeValue(this.isLive);
        parcel.writeDouble(this.price);

    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator(){
        public Listing createFromParcel(Parcel in){
            return new Listing(in);
        }
        public Listing[] newArray(int size){
            return new Listing[size];
        }
    };
}
