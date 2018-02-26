package com.a4t8bfarm.a4t8b;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.a4t8bfarm.a4t8b.Interfaces.Users;

public class SessionManagement{
    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "prefFile";

    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";

    // User name (make variable public to access from outside)
    public static final String KEY_NAME = "username";

    public static final String KEY_FNAME = "firstName";

    public static final String KEY_LNAME = "lastName";

    public static final String KEY_PHONENUMBER = "0";

    public static final String KEY_EMAIL = "email";

    public static final String KEY_ADDRESS = "address";

    public static final String KEY_ZIPCODE = "zipCode";

    public static final String KEY_VERIFIED = "false";

    public SessionManagement(Context applicationContext) {
        this._context = applicationContext;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }


    /**
     * Create login session
     * */
    public void createLoginSession(Users users){
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);

        // Storing name in pref
        editor.putString(KEY_NAME, users.username);
        editor.putString(KEY_FNAME, users.firstName);
        editor.putString(KEY_LNAME, users.lastName);
        editor.putString(KEY_PHONENUMBER, users.phoneNumber);
        editor.putString(KEY_EMAIL, users.email);
        editor.putString(KEY_ADDRESS, users.address);
        editor.putString(KEY_ZIPCODE, users.zipCode);
        editor.putString(KEY_VERIFIED, users.emailVerified);


        // commit changes
        editor.commit();
    }

    public void editUserDetail(String userDetailName, String userDetail){
        switch (userDetailName){
            case "Email Address":
                editor.putString(KEY_EMAIL, userDetail);
                break;
            case "Mobile Number":
                editor.putString(KEY_PHONENUMBER, userDetail);
                break;
            case "Delivery Address":
                editor.putString(KEY_ADDRESS, userDetail);
                break;
            case "Zip Code":
                editor.putString(KEY_ZIPCODE, userDetail);
                break;
            case "Email Verification":
                editor.putString(KEY_VERIFIED, userDetail);
                break;
        }
        editor.commit();
    }

    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     * */
    public void checkLogin(){
        // Check login status
        if(!this.isLoggedIn()){
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, LoginActivity.class);
            i.putExtra("from","CartActivity");
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            _context.startActivity(i);
        }

    }





    /**
     * Get stored session data
     * */
    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<String, String>();
        // user name
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));
        user.put(KEY_FNAME, pref.getString(KEY_FNAME, null));
        user.put(KEY_LNAME, pref.getString(KEY_LNAME, null));
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));
        user.put(KEY_PHONENUMBER, pref.getString(KEY_PHONENUMBER, null));
        user.put(KEY_ADDRESS, pref.getString(KEY_ADDRESS, null));
        user.put(KEY_ZIPCODE, pref.getString(KEY_ZIPCODE, null));
        user.put(KEY_VERIFIED, pref.getString(KEY_VERIFIED, null));

        // return user
        return user;
    }

    /**
     * Clear session details
     * */
    public void logoutUser(){
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Login Activity
        Intent i = new Intent(_context, MainActivity.class);
        /*Intent i = new Intent(this, MainActivity.class);*/
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Staring Login Activity
        _context.startActivity(i);
    }

    /**
     * Quick check for login
     * **/
    // Get Login State
    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }
}
