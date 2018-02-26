package com.a4t8bfarm.a4t8b.Interfaces;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Darren Gegantino on 9/8/2017.
 */

public class Orders implements Serializable {
    @SerializedName("OrderNumber")
    public String OrderNumber;

    @SerializedName("OrderDate")
    public String OrderDate;

    @SerializedName("CustomerID")
    public String CustomerID;

    @SerializedName("oNumber")
    public String output;

    @SerializedName("TotalCheckoutPrice")
    public double TotalCheckoutPrice;
}
