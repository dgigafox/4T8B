package com.a4t8bfarm.a4t8b;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.a4t8bfarm.a4t8b.Fragments.LocalCartFragment;
import com.a4t8bfarm.a4t8b.Fragments.VegetablesFragment;
import com.a4t8bfarm.a4t8b.Interfaces.Cart;
import com.a4t8bfarm.a4t8b.Interfaces.Products;
import com.kosalgeek.genasync12.AsyncResponse;
import com.kosalgeek.genasync12.PostResponseAsyncTask;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Darren Gegantino on 9/22/2017.
 */

public class SQLiteDBHelper extends SQLiteOpenHelper{
    private static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "local_database.db";
    public static final String CART_TABLE_NAME = "cart";
    public static final String CART_COLUMN_ID = "_id";
    public static final String CART_COLUMN_NAME = "name";
    public static final String CART_COLUMN_DESCRIPTION = "description";
    public static final String CART_COLUMN_UNIT = "unit";
    public static final String CART_COLUMN_QUANTITY = "quantity";
    public static final String CART_COLUMN_PRICE = "price";
    public static final String CART_COLUMN_IMG_URL = "img_url";

    private SQLiteDatabase database;

    public SQLiteDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + CART_TABLE_NAME + " (" +
                CART_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CART_COLUMN_NAME + " VARCHAR(80), " +
                CART_COLUMN_DESCRIPTION + " VARCHAR(200), " +
                CART_COLUMN_UNIT + " VARCHAR(80), " +
                CART_COLUMN_QUANTITY + " DOUBLE, " +
                CART_COLUMN_PRICE + " DOUBLE, " +
                CART_COLUMN_IMG_URL + " VARCHAR(200)" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CART_TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public void insertRecords(String name, Products selectedItem,String qty, String selectedUnit, Double price){

        database = this.getReadableDatabase();

        long count = DatabaseUtils.queryNumEntries(database, CART_TABLE_NAME, CART_COLUMN_NAME + "=? AND " + CART_COLUMN_UNIT + "=?",
                        new String[] {name, selectedUnit});

        if(count>0){

            String currentQtyString = getCurrentQty(name, selectedUnit);
            Double currentQty = Double.parseDouble(currentQtyString);
            Double qtyDouble = Double.parseDouble(qty);

            if (currentQty + qtyDouble > 99){
                database.execSQL("update " + CART_TABLE_NAME + " set " + CART_COLUMN_QUANTITY + " = 99 where "+ CART_COLUMN_NAME + " = '" + name + "'" + " AND " + CART_COLUMN_UNIT + " = '"
                        + selectedUnit + "'" + " AND " + CART_COLUMN_QUANTITY + " < 99");
            }
            else{
                database.execSQL("update " + CART_TABLE_NAME + " set " + CART_COLUMN_QUANTITY + " = " + CART_COLUMN_QUANTITY + "+'" + qty
                        + "' where " + CART_COLUMN_NAME + " = '" + name + "'" + " AND " + CART_COLUMN_UNIT + " = '"
                        + selectedUnit + "'" + " AND " + CART_COLUMN_QUANTITY + " < 99");
            }

            /*database.execSQL("update " + CART_TABLE_NAME + " set " + CART_COLUMN_QUANTITY + " = " + CART_COLUMN_QUANTITY + "+'" + qty
                    + "' where " + CART_COLUMN_NAME + " = '" + name + "'" + " AND " + CART_COLUMN_UNIT + " = '"
                    + selectedUnit + "'" + " AND " + CART_COLUMN_QUANTITY + " < 99");
*/
            database.close();
        }
        else {
            ContentValues values = new ContentValues();
            values.put(SQLiteDBHelper.CART_COLUMN_NAME, name);
            values.put(SQLiteDBHelper.CART_COLUMN_DESCRIPTION, selectedItem.description);
            values.put(SQLiteDBHelper.CART_COLUMN_UNIT, selectedUnit);
            values.put(SQLiteDBHelper.CART_COLUMN_QUANTITY, qty);
            values.put(SQLiteDBHelper.CART_COLUMN_PRICE, price);
            values.put(SQLiteDBHelper.CART_COLUMN_IMG_URL, selectedItem.img_url);
            database.insert(SQLiteDBHelper.CART_TABLE_NAME, null, values);

            database.close();
        }



    }

    public ArrayList<Cart> getAllRecords() {
        database = this.getReadableDatabase();
        Cursor cursor = database.query(SQLiteDBHelper.CART_TABLE_NAME, null, null, null, null, null, null);
        ArrayList<Cart> itemList = new ArrayList<Cart>();
        Cart cart;
        if (cursor.getCount() > 0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                cart = new Cart();
                cart.setId(cursor.getString(cursor.getColumnIndex(SQLiteDBHelper.CART_COLUMN_ID)));
                cart.setName(cursor.getString(cursor.getColumnIndex(SQLiteDBHelper.CART_COLUMN_NAME)));
                cart.setDesc(cursor.getString(cursor.getColumnIndex(SQLiteDBHelper.CART_COLUMN_DESCRIPTION)));
                cart.setUnit(cursor.getString(cursor.getColumnIndex(SQLiteDBHelper.CART_COLUMN_UNIT)));
                cart.setQty(cursor.getDouble(cursor.getColumnIndex(SQLiteDBHelper.CART_COLUMN_QUANTITY)));
                cart.setPrice(cursor.getDouble(cursor.getColumnIndex(SQLiteDBHelper.CART_COLUMN_PRICE)));
                cart.setImg(cursor.getString(cursor.getColumnIndex(SQLiteDBHelper.CART_COLUMN_IMG_URL)));
                itemList.add(cart);
            }
        }
        cursor.close();
        database.close();
        return itemList;
    }

    public Cursor getCursor(){
        database = this.getReadableDatabase();
        Cursor cursor = database.query(SQLiteDBHelper.CART_TABLE_NAME, null, null, null, null, null, null);
        return cursor;
    }

    public void deleteRecord(Cart selectedItem) {
        database = this.getReadableDatabase();
        database.execSQL("delete from " + CART_TABLE_NAME + " where " + CART_COLUMN_ID + " = '" + selectedItem.id + "'");
        database.close();
    }

    public void deleteAllRecords() {
        database = this.getReadableDatabase();
        database.delete(CART_TABLE_NAME, null, null);
        database.close();
    }

    public void incQty(Cart selectedItem) {
        double increment = 0;
        database = this.getReadableDatabase();
        if (selectedItem.getUnit().contains("kilo")){
            increment = 0.25;
        }
        else {
            increment = 1.0;
        }

        database.execSQL("update " + CART_TABLE_NAME + " set " + CART_COLUMN_QUANTITY + " = " + CART_COLUMN_QUANTITY + "+" + increment + " where " + CART_COLUMN_ID + " = '" + selectedItem.id + "'");
        database.close();
    }

    public void decQty(Cart selectedItem) {
        double decrement = 0;
        database = this.getReadableDatabase();
        if (selectedItem.getUnit().contains("kilo")){
            decrement = 0.25;
        }
        else {
            decrement = 1.0;
        }
        database.execSQL("update " + CART_TABLE_NAME + " set " + CART_COLUMN_QUANTITY + " = " + CART_COLUMN_QUANTITY + "-" + decrement + " where " + CART_COLUMN_ID + " = '" + selectedItem.id + "'");
        database.close();
    }

    public void updateQty(Cart selectedItem, Double updateQtyInt) {
        database = this.getReadableDatabase();
        database.execSQL("update " + CART_TABLE_NAME + " set " + CART_COLUMN_QUANTITY + " = '" + updateQtyInt + "' where " + CART_COLUMN_ID + " = '" + selectedItem.id + "'");
        database.close();
    }

    private String getCurrentQty (String name, String selectedUnit){

        String currentQty = "Error";
        Cursor cursor = database.query(CART_TABLE_NAME, new String[] {CART_COLUMN_QUANTITY}, CART_COLUMN_NAME + "=? AND "
                + CART_COLUMN_UNIT + "=?", new String[]{name, selectedUnit}, null, null, null );
        if (cursor.getCount() == 1){
            cursor.moveToFirst();
            currentQty = cursor.getString(cursor.getColumnIndex(CART_COLUMN_QUANTITY));
        }
        cursor.close();

        return currentQty;
    }

    public String showQty (Cart selectedItem) {
        String qty = "Error";
        database = this.getReadableDatabase();
        Cursor cursor = database.query(CART_TABLE_NAME, new String[] {CART_COLUMN_QUANTITY}, CART_COLUMN_ID + "=?",
                new String[]{selectedItem.getID()}, null, null, null);
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            qty = cursor.getString(cursor.getColumnIndex(CART_COLUMN_QUANTITY));
        }
        cursor.close();
        database.close();
        return  qty;
    }
}
