package com.sjsu.se195.irom;

import java.util.Date;

/**
 * Created by Krystle on 10/28/2016.
 */

public class Listing {
    public String listID;
    public Date dateCreated;
    public String creator;
    public com.sjsu.se195.irom.Item item;
    public String description;
    public Boolean isLive;
    public Double price;
    //maybe have tags as an array list of strings?
    public String tag;
}
