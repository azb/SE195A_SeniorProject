package com.sjsu.se195.irom.Classes;

import java.util.ArrayList;

/**
 * Created by Overlord Rick Oliver on 3/27/2017.
 */

public class IROMazon {
    public String name;
    public ArrayList<String> text = new ArrayList<>();
    public ArrayList<String> logo = new ArrayList<>();
    public ArrayList<String> label = new ArrayList<>();
    public double price;

    /*public double entityScore;
    public double textScore;
    public double logoScore;
    public double labelScore;*/

    public String key;
    public String imageURL;

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
    //public String getName() {return name;}
    //public void setName(String Name) {this.name = name;}
    //public String getDescription() {return description;}
    //public void setDescription(String description) {this.description = description;}
    //public String getiID() {return iID;}
    //public void setiID(String Key) {this.iID = Key;}
    //public Integer getPrice() {return Price;}
    //public void setKey(Integer Price) {this.Price = Price;}
}
