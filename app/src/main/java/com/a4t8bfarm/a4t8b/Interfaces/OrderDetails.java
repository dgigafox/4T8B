package com.a4t8bfarm.a4t8b.Interfaces;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Darren Gegantino on 10/24/2017.
 */

public class OrderDetails implements Serializable {

    @SerializedName("OrderName")
    public String orderName;

    @SerializedName("Unit")
    public String orderUnit;

    @SerializedName("Quantity")
    public double orderQty;

    @SerializedName("TotalPrice")
    public double orderPrice;

    @SerializedName("Instruction")
    public String orderInstruction;
}
