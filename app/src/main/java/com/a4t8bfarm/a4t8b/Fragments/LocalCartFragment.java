package com.a4t8bfarm.a4t8b.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.a4t8bfarm.a4t8b.ConfirmOrderActivity;
import com.a4t8bfarm.a4t8b.Interfaces.Cart;
import com.a4t8bfarm.a4t8b.Interfaces.DecimalToFraction;
import com.a4t8bfarm.a4t8b.Interfaces.OnDataPass;
import com.a4t8bfarm.a4t8b.Interfaces.UILConfig;
import com.a4t8bfarm.a4t8b.R;
import com.a4t8bfarm.a4t8b.SQLiteDBHelper;
import com.a4t8bfarm.a4t8b.SessionManagement;
import com.amigold.fundapter.BindDictionary;
import com.amigold.fundapter.FunDapter;
import com.amigold.fundapter.extractors.StringExtractor;
import com.amigold.fundapter.interfaces.DynamicImageLoader;
import com.amigold.fundapter.interfaces.ItemClickListener;
import com.kosalgeek.android.json.JsonConverter;
import com.kosalgeek.genasync12.AsyncResponse;
import com.kosalgeek.genasync12.PostResponseAsyncTask;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class LocalCartFragment extends Fragment implements AdapterView.OnItemClickListener, AsyncResponse{

    SessionManagement session;
    View view;
    FunDapter<Cart> adapter;
    OnDataPass dataPasser;

    public ArrayList<Cart> itemList;
    public String checkoutSum;

    final String LOG = "CartFragment";
    final static String url = "https://androidshopping.000webhostapp.com/cart_retrieve.php";
    private ListView lv;


    public LocalCartFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_cart, container, false);


        Toast.makeText(LocalCartFragment.this.getActivity(), "View created", Toast.LENGTH_LONG).show();
        // Image loader initialization
        ImageLoader.getInstance().init(UILConfig.config(LocalCartFragment.this.getActivity()));

        /*btnCheckout = (Button)view.findViewById(R.id.btnCheckout);*/

        //session initialization
        session = new SessionManagement(getContext());
        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        // name
        String customer = user.get(SessionManagement.KEY_NAME);

        final SQLiteDBHelper sqLiteDBHelper = new SQLiteDBHelper(LocalCartFragment.this.getActivity());
        itemList = sqLiteDBHelper.getAllRecords();

        BindDictionary<Cart> dic = new BindDictionary<Cart>();
        adapter = new FunDapter<>(LocalCartFragment.this.getActivity(), itemList, R.layout.cart_row_v2, dic);

        dic.addStringField(R.id.tvName, new StringExtractor<Cart>() {
            @Override
            public String getStringValue(Cart item, int position) {
                return item.getName();
            }
        });

        dic.addStringField(R.id.tvInstruction, new StringExtractor<Cart>() {
            @Override
            public String getStringValue(Cart item, int position) {
                return item.getInstruction();
            }
        }).visibilityIfNull(View.GONE);

        dic.addStringField(R.id.tvUnit, new StringExtractor<Cart>() {
            @Override
            public String getStringValue(Cart item, int position) {
                return item.getUnit();
            }
        });

        dic.addStringField(R.id.edQty, new StringExtractor<Cart>() {
            @Override
            public String getStringValue(Cart item, int position) {
                String qty="0";
                if (item.getUnit().contains("kilo")){
                    DecimalToFraction decimalToFraction = new DecimalToFraction(item.getQty());
                    qty = decimalToFraction.getFraction();
                }
                else {
                    DecimalFormat qtyDecFormat = new DecimalFormat("0");
                    qty = qtyDecFormat.format(item.getQty());
                }

                return qty;
            }
        });


        dic.addStringField(R.id.tvPrice, new StringExtractor<Cart>() {
            @Override
            public String getStringValue(Cart item, int position) {
                double price = item.getPrice();
                double qty = item.getQty();
                price = price * qty;
                String s = String.format("%.2f",price);
                return  s;
            }
        });


        dic.addDynamicImageField(R.id.ivImage, new StringExtractor<Cart>() {
            @Override
            public String getStringValue(Cart item, int position) {
                return item.getImg();
            }
        }, new DynamicImageLoader() {
            @Override
            public void loadImage(String url, ImageView img) {
                //Set image
                ImageLoader.getInstance().displayImage(url, img);
            }
        });


        //Remove Item from the basket
        dic.addBaseField(R.id.btnRemove).onClick(new ItemClickListener() {

            @Override
            public void onClick(Object item, int position, View view) {

                Cart selectedItem = itemList.get(position);
                sqLiteDBHelper.deleteRecord(selectedItem);
                /*lv.refreshDrawableState();

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.detach(LocalCartFragment.this).attach(LocalCartFragment.this).commit();

                adapter.notifyDataSetChanged();*/
                itemList.remove(selectedItem);
                adapter.notifyDataSetChanged();
                updateCartActivity();
                if (adapter.getCount() == 0){
                    showEmptyCart();
                }
                Toast.makeText(LocalCartFragment.this.getActivity(), "Item Removed", Toast.LENGTH_SHORT).show();
            }
        });

        //increase the item quantity
        dic.addBaseField(R.id.qty_increase).onClick(new ItemClickListener() {
            @Override
            public void onClick(Object item, int position, final View view) {
                Cart selectedItem = itemList.get(position);
                double qty = selectedItem.quantity;

                if (qty < 99){
                    sqLiteDBHelper.incQty(selectedItem);
                    /*lv.refreshDrawableState();

                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.detach(LocalCartFragment.this).attach(LocalCartFragment.this).commit();*/
                    selectedItem.quantity = Double.parseDouble(sqLiteDBHelper.showQty(selectedItem));
                    adapter.notifyDataSetChanged();
                    updateCartActivity();
                }

            }
        });

        //decrease the item quantity
        dic.addBaseField(R.id.qty_decrease).onClick(new ItemClickListener() {

            @Override
            public void onClick(Object item, int position, final View view) {
                Cart selectedItem = itemList.get(position);
                double qty = selectedItem.quantity;
                /*ImageButton dec = (ImageButton)view.findViewById(R.id.qty_decrease);*/
                double limit = 0;
                if (selectedItem.getUnit().contains("kilo")){
                    limit = 0.25;
                }
                else {
                    limit = 1;
                }

                if (qty>limit){

                    sqLiteDBHelper.decQty(selectedItem);
                    /*lv.refreshDrawableState();

                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.detach(LocalCartFragment.this).attach(LocalCartFragment.this).commit();*/
                    selectedItem.quantity = Double.parseDouble(sqLiteDBHelper.showQty(selectedItem));
                    adapter.notifyDataSetChanged();
                    updateCartActivity();
                }
            }
        });

        dic.addBaseField(R.id.editQty).onClick(new ItemClickListener<Cart>() {
            @Override
            public void onClick(Cart item, int position, View view) {
                final Cart selectedItem = itemList.get(position);
                LinearLayout linearLayout = new LinearLayout(LocalCartFragment.this.getActivity());
                linearLayout.setOrientation(linearLayout.VERTICAL);
                linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
                final EditText input = new EditText(LocalCartFragment.this.getActivity());
                input.setGravity(Gravity.CENTER_HORIZONTAL);
                input.setCursorVisible(true);
                input.setSingleLine(true);

                switch(selectedItem.unit){
                    case "kilo":
                        input.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(5)});
                        break;
                    default:
                        input.setInputType(InputType.TYPE_CLASS_NUMBER);
                        input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(2)});
                        break;
                }
                linearLayout.setPadding(350, 0, 350, 0);
                linearLayout.addView(input);

                final AlertDialog.Builder enterQty = new AlertDialog.Builder(LocalCartFragment.this.getActivity());

                enterQty.setTitle("Edit quantity of " + selectedItem.unit + " " + selectedItem.name + ": 1-99");
                enterQty.setMessage("");
                enterQty.setView(linearLayout);

                enterQty.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });

                final AlertDialog dialog = enterQty.create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        String updateQty = input.getText().toString();

                        if (!updateQty.equals("")){
                            Double updateQtyDouble = Double.parseDouble(updateQty);
                            if (updateQtyDouble > 0.0 && updateQtyDouble <= 99.0){
                                if(updateQtyDouble%0.25 == 0.0){
                                    SQLiteDBHelper sqLiteDBHelper = new SQLiteDBHelper(LocalCartFragment.this.getActivity());
                                    sqLiteDBHelper.updateQty(selectedItem,updateQtyDouble);
                                    /*lv.refreshDrawableState();

                                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                                    ft.detach(LocalCartFragment.this).attach(LocalCartFragment.this).commit();*/
                                    selectedItem.quantity = Double.parseDouble(sqLiteDBHelper.showQty(selectedItem));
                                    adapter.notifyDataSetChanged();
                                    dialog.dismiss();
                                }
                                else{
                                    Animation shake = AnimationUtils.loadAnimation(LocalCartFragment.this.getActivity(), R.anim.shake);
                                    input.startAnimation(shake);
                                    Toast.makeText(LocalCartFragment.this.getActivity(),"Please enter a valid quantity for kilo.",Toast.LENGTH_SHORT).show();
                                }
                            }


                            else{
                                Animation shake = AnimationUtils.loadAnimation(LocalCartFragment.this.getActivity(), R.anim.shake);
                                input.startAnimation(shake);
                                Toast.makeText(LocalCartFragment.this.getActivity(),"Please enter values from 1 to 99 only.",Toast.LENGTH_SHORT).show();
                            }
                        }

                        else {
                            dialog.dismiss();
                        }
                    }
                });
            }
        });

        lv = (ListView)view.findViewById(R.id.lvCart);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(this);

        if (adapter.getCount() == 0){
            showEmptyCart();
        }
        updateCartActivity();

        return view;
    }

    @Override
    public void processFinish(final String s) {

    }

    public void updateCartActivity() {
        Integer itemCount = lv.getCount();

        totalPrice(lv);

        double sum = totalPrice(lv);
        checkoutSum = String.format("%.2f",sum);

        dataPasser.onDataPass(itemList, checkoutSum, itemCount);
    }

    public void showEmptyCart() {
        TextView total = (TextView)view.findViewById(R.id.tvTotal);
        TextView empty = (TextView)view.findViewById(R.id.tvEmpty);

        Button btn = (Button) getActivity().findViewById(R.id.btnCheckout);
        btn.setVisibility(View.GONE);
        total.setVisibility(View.GONE);
        empty.setVisibility(View.VISIBLE);
    }

    public double totalPrice(ListView listView)
    {
        double sum = 0;
        TextView total = (TextView)view.findViewById(R.id.tvTotal);
        int count = listView.getCount();
        for(int i = 0; i < count; i++)
        {
            View v = listView.getAdapter().getView(i, null, null);
            TextView tv = (TextView) v.findViewById(R.id.tvPrice);
            sum = sum + Double.parseDouble(tv.getText().toString());
        }

        String checkoutSum = String.format("%.2f",sum);

        total.setText("Sub Total: Php " +checkoutSum);
        return sum;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        final Cart selectedItem = itemList.get(position);
        final AlertDialog.Builder noteDialog = new AlertDialog.Builder(LocalCartFragment.this.getActivity());
        noteDialog.setTitle("Note from 4T8B");
        noteDialog.setMessage("You need to be logged in to write special instruction for this item.");
        noteDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        noteDialog.show();
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        dataPasser = (OnDataPass) context;
    }



}
