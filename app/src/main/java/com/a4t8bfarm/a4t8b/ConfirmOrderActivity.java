package com.a4t8bfarm.a4t8b;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.a4t8bfarm.a4t8b.Interfaces.Cart;
import com.a4t8bfarm.a4t8b.Interfaces.Orders;
import com.amigold.fundapter.BindDictionary;
import com.amigold.fundapter.FunDapter;
import com.amigold.fundapter.extractors.BooleanExtractor;
import com.amigold.fundapter.extractors.StringExtractor;
import com.amigold.fundapter.interfaces.ItemClickListener;
import com.amigold.fundapter.interfaces.StaticImageLoader;
import com.kosalgeek.android.json.JsonConverter;
import com.kosalgeek.genasync12.AsyncResponse;
import com.kosalgeek.genasync12.PostResponseAsyncTask;

import java.util.ArrayList;
import java.util.HashMap;

public class ConfirmOrderActivity extends AppCompatActivity implements AsyncResponse {

    public static ArrayList<Orders> orders = new ArrayList<>();
    public static String customer, firstName, lastName, address, zipCode, email, phoneNumber;

    SessionManagement session;
    View view;
    ListView lv;
    FunDapter<Cart> adapter;
    Button confirmOrder;
    Button cancelOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_order);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //session initialization
        session = new SessionManagement(getApplicationContext());
        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        // name
        customer = user.get(SessionManagement.KEY_NAME);
        firstName = user.get(SessionManagement.KEY_FNAME);
        lastName = user.get(SessionManagement.KEY_LNAME);
        address = user.get(SessionManagement.KEY_ADDRESS);
        zipCode = user.get(SessionManagement.KEY_ZIPCODE);
        email = user.get(SessionManagement.KEY_EMAIL);
        phoneNumber = user.get(SessionManagement.KEY_PHONENUMBER);

        TextView introMsg = (TextView) findViewById(R.id.helloCustomer);
        TextView total = (TextView) findViewById(R.id.totalOrderPrice);
        confirmOrder = (Button)findViewById(R.id.confirmOrder);
        cancelOrder = (Button)findViewById(R.id.cancelOrder);


        introMsg.setText("Summary of Order");

        Intent receivedIntent = getIntent();

        final String totalCheckout = getIntent().getStringExtra("total");

        final ArrayList<Cart> itemList = (ArrayList<Cart>) receivedIntent.getSerializableExtra("item_list");

        BindDictionary dic = new BindDictionary();

        dic.addStringField(R.id.orderName, new StringExtractor<Cart>() {
            @Override
            public String getStringValue(Cart item, int position) {
                return item.name;
            }
        });

        dic.addStringField(R.id.orderUnit, new StringExtractor<Cart>() {
            @Override
            public String getStringValue(Cart item, int position) {
                return item.unit;
            }
        }).visibilityIfNull(View.GONE);

        dic.addStringField(R.id.orderQty, new StringExtractor<Cart>() {
            @Override
            public String getStringValue(Cart item, int position) {
                return ""+ item.quantity;
            }
        });

        dic.addStringField(R.id.orderPrice, new StringExtractor<Cart>() {
            @Override
            public String getStringValue(Cart item, int position) {
                double price = item.price;
                double qty = item.quantity;
                price = price * qty;
                String s = String.format("%.2f",price);
                return  s;
            }
        });

        dic.addStringField(R.id.showInstruction, new StringExtractor<Cart>() {
            @Override
            public String getStringValue(Cart item, int position) {
                if (item.instruction != null) {
                    return "Show";
                }
                else {
                    return null;
                }
            }
        }).visibilityIfNull(View.GONE).onClick(new ItemClickListener() {
            @Override
            public void onClick(Object item, int position, View view) {
                final Cart selectedItem = itemList.get(position);
                AlertDialog.Builder dialog = new AlertDialog.Builder(ConfirmOrderActivity.this);
                dialog.setTitle("Special instruction for " + selectedItem.quantity + " " + selectedItem.unit + " of "
                        + selectedItem.name);
                dialog.setMessage(selectedItem.instruction);
                dialog.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        /*dic.addBaseField(R.id.showInstruction).onClick(new ItemClickListener() {
            @Override
            public void onClick(Object item, int position, View view) {
                final Cart selectedItem = itemList.get(position);
                AlertDialog.Builder dialog = new AlertDialog.Builder(ConfirmOrderActivity.this);
                dialog.setTitle("Special instruction for " + selectedItem.quantity + " " + selectedItem.unit + " of "
                + selectedItem.name);
                dialog.setMessage(selectedItem.instruction);
                dialog.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });*/


        adapter = new FunDapter<>(ConfirmOrderActivity.this, itemList, R.layout.activity_confirm_order_row, dic);
        lv = (ListView) findViewById(R.id.lvOrder);
        lv.setAdapter(adapter);

        total.setText("Your total checkout price is: Php " + totalCheckout);

        //CONFIRM ORDER BUTTON
        confirmOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*OrderFragment orderFragment = new OrderFragment();
                FragmentManager manager = getFragmentManager();
                manager.beginTransaction().replace(R.id.content_main, orderFragment)
                        .addToBackStack("Home").commit();*/
                final AlertDialog.Builder notifyOrder = new AlertDialog.Builder(ConfirmOrderActivity.this);
                notifyOrder.setTitle("Order Confirmation");
                notifyOrder.setMessage(
                        "After you have reviewed your orders and pressed proceed we will be delivering our products " +
                                "to this address: \n" + address + " " + zipCode + ".\n\n" +
                                "We may also contact you through your email " + email + " or through" +
                                " your mobile number " + phoneNumber + ".\n\n" +
                                "If the following details are correct kindly press proceed to get your order number. If you want to edit" +
                                " these details kindly cancel then go to Main Menu > Account settings."
                );
                notifyOrder.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String url = "https://androidshopping.000webhostapp.com/ordernumber_test_2.php";
                        HashMap postData = new HashMap();
                        postData.put("txtUsername", customer);
                        postData.put("txtTotalCheckoutPrice", totalCheckout);
                        PostResponseAsyncTask taskConfirm = new PostResponseAsyncTask(ConfirmOrderActivity.this, postData, new AsyncResponse() {
                            @Override
                            public void processFinish(final String s) {
                                ArrayList<Orders> orders = new JsonConverter<Orders>().toArrayList(s, Orders.class);
                                String output = "";
                                for(Orders o: orders){
                                    output=o.OrderNumber;
                                }
                                Toast.makeText(ConfirmOrderActivity.this, "Order Confirmed", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(ConfirmOrderActivity.this, ThanksActivity.class);
                                intent.putExtra("oNumber", output);
                                startActivity(intent);
                            }
                        });
                        taskConfirm.execute(url);
                    }
                });
                notifyOrder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                notifyOrder.show();
            }
        });

        //CANCEL ORDER BUTTON
        cancelOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    public void processFinish(String s){

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent h = NavUtils.getParentActivityIntent(this);
                h.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, h);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}


