package com.a4t8bfarm.a4t8b.Interfaces;



import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Justice on 05/02/2017.
 */

public class Cart implements Serializable{

    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("description")
    public String description;

    @SerializedName("unit")
    public String unit;

    @SerializedName("quantity")
    public double quantity;

    @SerializedName("price")
    public double price;

    @SerializedName("img_url")
    public String img_url;

    @SerializedName("customer")
    public String customer;

    @SerializedName("total")
    public String checkoutSum;

    @SerializedName("instruction")
    public String instruction;

    public String getID() { return id; }

    public  void setId(String id) { this.id = id; }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getDesc(){
        return description;
    }

    public void setDesc(String description){
        this.description = description;
    }

    public String getUnit(){
        return unit;
    }

    public void setUnit(String unit){
        this.unit = unit;
    }

    public double getQty(){
        return quantity;
    }

    public void setQty(Double quantity){
        this.quantity = quantity;
    }

    public Double getPrice(){
        return price;
    }

    public void setPrice(Double price){
        this.price = price;
    }

    public String getImg(){
        return img_url;
    }

    public void setImg(String img_url){
        this.img_url = img_url;
    }

    public String getInstruction(){
        return instruction;
    }

    public void setInstruction(String instruction){
        this.instruction = instruction;
    }

}


