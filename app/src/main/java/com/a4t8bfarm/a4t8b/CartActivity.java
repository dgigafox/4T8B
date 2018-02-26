package com.a4t8bfarm.a4t8b;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.BoolRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.a4t8bfarm.a4t8b.Fragments.CartFragment;
import com.a4t8bfarm.a4t8b.Fragments.LocalCartFragment;
import com.a4t8bfarm.a4t8b.Interfaces.Cart;
import com.a4t8bfarm.a4t8b.Interfaces.OnDataPass;
import com.kosalgeek.genasync12.AsyncResponse;
import com.kosalgeek.genasync12.PostResponseAsyncTask;

import java.util.ArrayList;
import java.util.HashMap;


public class CartActivity extends AppCompatActivity implements OnDataPass{
    SessionManagement session;
    public static final int REQUEST_CODE = 1;
    CartFragment cartFragment = new CartFragment();
    LocalCartFragment localCartFragment = new LocalCartFragment();
    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        session = new SessionManagement(getApplicationContext());

        if(session.isLoggedIn()){
            transaction.add(R.id.frame_container,cartFragment).commit();
        }
        else {
            transaction.add(R.id.frame_container,localCartFragment).commit();
        }



    }

    @Override
    public void onDataPass (final ArrayList<Cart> itemList, final String checkoutSum, final Integer itemCount){
        Button BtnCheckout = (Button) findViewById(R.id.btnCheckout);

        if (itemCount == 0){
            BtnCheckout.setVisibility(View.GONE);
        }
        BtnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(session.isLoggedIn()){
                    Intent intent = new Intent(CartActivity.this, ConfirmOrderActivity.class);
                    intent.putExtra("item_list", itemList);
                    intent.putExtra("total", checkoutSum);
                    startActivity(intent);
                }
                else {

                    final AlertDialog.Builder requireLogin = new AlertDialog.Builder(CartActivity.this);
                    requireLogin.setTitle("Login Required");
                    requireLogin.setMessage("Checkout requires login. Do you want to login now?");
                    requireLogin.setPositiveButton("Login", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(CartActivity.this,LoginActivity.class);
                            intent.putExtra("from","CartActivity");
                            startActivityForResult(intent,REQUEST_CODE);
                            transaction.add(R.id.frame_container, cartFragment);
                        }
                    });
                    requireLogin.setNegativeButton("Maybe later", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    requireLogin.show();



                }


            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if(requestCode==REQUEST_CODE)
        {
            if(resultCode== Activity.RESULT_OK){
                final SQLiteDBHelper sqLiteDBHelper = new SQLiteDBHelper(CartActivity.this);
                Cursor cursor = sqLiteDBHelper.getCursor();
                String customer = data.getStringExtra("customer");
                uploadLocalDB(cursor,customer);
                /*restartActivity(CartActivity.this);*/
                Intent intent = new Intent(CartActivity.this,MainActivity.class);
                intent.putExtra("from","CartActivity");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }

        }
    }

    public void uploadLocalDB(Cursor cursor, String customer){
        final String LOG = "UploadtoLocalDB";

        SQLiteDBHelper sqLiteDBHelper = new SQLiteDBHelper(CartActivity.this);
        if (cursor.getCount() > 0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                HashMap postData = new HashMap();
                postData.put("txtName", cursor.getString(cursor.getColumnIndex(SQLiteDBHelper.CART_COLUMN_NAME)));
                postData.put("txtDesc", cursor.getString(cursor.getColumnIndex(SQLiteDBHelper.CART_COLUMN_DESCRIPTION)));
                postData.put("txtUnit", cursor.getString(cursor.getColumnIndex(SQLiteDBHelper.CART_COLUMN_UNIT)));
                postData.put("txtQty", cursor.getString(cursor.getColumnIndex(SQLiteDBHelper.CART_COLUMN_QUANTITY)));
                postData.put("txtPrice", cursor.getString(cursor.getColumnIndex(SQLiteDBHelper.CART_COLUMN_PRICE)));
                postData.put("txtImageUrl", cursor.getString(cursor.getColumnIndex(SQLiteDBHelper.CART_COLUMN_IMG_URL)));
                postData.put("txtCustomer", customer);
                PostResponseAsyncTask insertTask = new PostResponseAsyncTask(
                        CartActivity.this, postData, new AsyncResponse() {
                    @Override
                    public void processFinish(String s) {
                        Log.d(LOG, s);


                        if(s.contains("success"))
                        {

                        }
                    }
                });

                insertTask.execute("https://androidshopping.000webhostapp.com/cart_insertv3.php");
            }
        }
        sqLiteDBHelper.deleteAllRecords();

    }

    public static void restartActivity(Activity activity) {
        if (Build.VERSION.SDK_INT >= 11) {
            activity.recreate();
        } else {
            activity.finish();
            activity.startActivity(activity.getIntent());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

