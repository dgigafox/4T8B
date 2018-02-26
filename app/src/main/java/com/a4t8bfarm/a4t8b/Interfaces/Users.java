package com.a4t8bfarm.a4t8b.Interfaces;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Justice on 17/02/2017.
 */

public class Users implements Serializable {

    @SerializedName("CustomerID")
    public int id;

    @SerializedName("FName")
    public String firstName;

    @SerializedName("LName")
    public String lastName;

    @SerializedName("PhoneNumber")
    public String phoneNumber;

    @SerializedName("UserName")
    public String username;

    @SerializedName("Email")
    public String email;

    @SerializedName("Address")
    public String address;

    @SerializedName("ZipCode")
    public String zipCode;

    @SerializedName("Active")
    public String emailVerified;

}
