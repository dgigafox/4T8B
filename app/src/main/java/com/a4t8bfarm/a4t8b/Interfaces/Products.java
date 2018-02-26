package com.a4t8bfarm.a4t8b.Interfaces;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Justice on 22/01/2017.
 */

public class Products implements Serializable {

    @SerializedName("id")
    public int id;

    @SerializedName("product_name")
    public String name;

    @SerializedName("description")
    public String description;

    @SerializedName("img_url")
    public String img_url;

    @SerializedName("price_per_piece")
    public double price;

    @SerializedName("price_per_bundle")
    public double pricePerBundle;

    @SerializedName("price_per_kilo")
    public double pricePerKilo;

    @SerializedName("coffee_category")
    public String coffeeCategory;

    @SerializedName("125_grams")
    public double price125Grams;

    @SerializedName("250_grams")
    public double price250Grams;
}
