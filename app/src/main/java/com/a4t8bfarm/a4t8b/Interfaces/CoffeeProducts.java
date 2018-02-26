package com.a4t8bfarm.a4t8b.Interfaces;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Darren Gegantino on 11/1/2017.
 */

public class CoffeeProducts implements Serializable{
    @SerializedName("ProductID")
    public int id;

    @SerializedName("ProductName")
    public String name;

    @SerializedName("Description")
    public String description;

    @SerializedName("IMG_URL")
    public String img_url;

    @SerializedName("CoffeeCategory")
    public String coffeeCategory;

    @SerializedName("125_grams")
    public double price125g;

    @SerializedName("250_grams")
    public double price500g;


}
