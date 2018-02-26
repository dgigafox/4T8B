package com.a4t8bfarm.a4t8b.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import com.a4t8bfarm.a4t8b.ConfirmOrderActivity;
import com.a4t8bfarm.a4t8b.Interfaces.Cart;
import com.a4t8bfarm.a4t8b.Interfaces.DecimalToFraction;
import com.a4t8bfarm.a4t8b.Interfaces.OnDataPass;
import com.a4t8bfarm.a4t8b.Interfaces.UILConfig;
import com.a4t8bfarm.a4t8b.R;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class CartFragment extends Fragment implements AdapterView.OnItemClickListener, AsyncResponse{

    SessionManagement session;
    OnDataPass dataPasser;

    final String LOG = "CartFragment";
    final static String url = "https://androidshopping.000webhostapp.com/cart_retrieve.php";

    View view;

    public static ArrayList<Cart> itemList = new ArrayList<>();
    public String checkoutSum;
    private ListView lv;
    FunDapter<Cart> adapter;



    public CartFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_cart, container, false);

        ImageLoader.getInstance().init(UILConfig.config(CartFragment.this.getActivity()));

        /*btnCheckout = (Button)view.findViewById(R.id.btnCheckout);*/

        //session initialization
        session = new SessionManagement(getContext());
        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        // name
        String customer = user.get(SessionManagement.KEY_NAME);

        HashMap postData = new HashMap();
        postData.put("txtUsername", customer);
        PostResponseAsyncTask taskRead = new PostResponseAsyncTask(CartFragment.this.getActivity(), postData, this);
        taskRead.execute(url);

        return view;
    }

    @Override
    public void processFinish(final String s) {
        itemList = new JsonConverter<Cart>().toArrayList(s, Cart.class);

        BindDictionary dic = new BindDictionary();

        dic.addStringField(R.id.tvName, new StringExtractor<Cart>() {
            @Override
            public String getStringValue(Cart item, int position) {
                return item.name;
            }
        });

        dic.addStringField(R.id.tvInstruction, new StringExtractor<Cart>() {
            @Override
            public String getStringValue(Cart item, int position) {
                return item.instruction;
            }
        }).visibilityIfNull(View.GONE);

        dic.addStringField(R.id.tvUnit, new StringExtractor<Cart>() {
            @Override
            public String getStringValue(Cart item, int position) {
                return item.unit;
            }
        });

        dic.addStringField(R.id.edQty, new StringExtractor<Cart>() {
            @Override
            public String getStringValue(Cart item, int position) {
                String qty="0";
                if (item.unit.contains("kilo")){
                    DecimalToFraction decimalToFraction = new DecimalToFraction(item.quantity);
                    qty = decimalToFraction.getFraction();
                }
                else {
                    DecimalFormat qtyDecFormat = new DecimalFormat("0");
                    qty = qtyDecFormat.format(item.quantity);
                }

                return qty;
            }

        });


        dic.addStringField(R.id.tvPrice, new StringExtractor<Cart>() {
            @Override
            public String getStringValue(Cart item, int position) {
                double price = item.price;
                double qty = item.quantity;
                price = price * qty;
                String s = String.format("%.2f",price);
                return  s;
            }
        });


        dic.addDynamicImageField(R.id.ivImage, new StringExtractor<Cart>() {
            @Override
            public String getStringValue(Cart item, int position) {
                return item.img_url;
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

                final Cart selectedItem = itemList.get(position);
                HashMap postData = new HashMap();
                postData.put("pid", ""+ selectedItem.id);

                PostResponseAsyncTask taskRemove = new PostResponseAsyncTask(CartFragment.this.getActivity(),
                        postData, new AsyncResponse() {
                    @Override
                    public void processFinish(String s) {
                        Log.d(LOG, s);
                        if(s.contains("success"))
                        {
                            itemList.remove(selectedItem);
                            /*lv.refreshDrawableState();

                            FragmentTransaction ft = getFragmentManager().beginTransaction();
                            ft.detach(CartFragment.this).attach(CartFragment.this).commit();*/
                            if(adapter.getCount() == 0){
                                showEmptyCart();
                            }
                            adapter.notifyDataSetChanged();
                            updateCartActivity();
                            Toast.makeText(CartFragment.this.getActivity(), "Item Removed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                taskRemove.execute("https://androidshopping.000webhostapp.com/cart_remove.php");
            }
        });

        //increase the item quantity
        dic.addBaseField(R.id.qty_increase).onClick(new ItemClickListener() {
            @Override
            public void onClick(Object item, int position, final View view) {
                final Cart selectedItem = itemList.get(position);
                final double qty = selectedItem.quantity;
                HashMap postData = new HashMap();
                postData.put("txtPid", ""+ selectedItem.id);
                postData.put("mobile", "android");
                postData.put("unit", selectedItem.unit);


                if (qty < 99){
                    final PostResponseAsyncTask incTask = new PostResponseAsyncTask(CartFragment.this.getActivity(),
                            postData, new AsyncResponse() {
                        @Override
                        public void processFinish(String s) {

                            if(s.contains("success"))
                            {
                                /*FragmentTransaction ft = getFragmentManager().beginTransaction();
                                ft.detach(CartFragment.this).attach(CartFragment.this).commit();*/
                                switch(selectedItem.unit){
                                    case "kilo":
                                        selectedItem.quantity = selectedItem.getQty() + 0.25;
                                        break;
                                    default:
                                        selectedItem.quantity = selectedItem.getQty() + 1.0;
                                        break;
                                }
                                adapter.notifyDataSetChanged();
                                updateCartActivity();
                            }
                            else if (s.contains("failed")){
                                Toast.makeText(CartFragment.this.getActivity(),s,Toast.LENGTH_SHORT).show();
                            }
                        }

                    });
                    incTask.execute("https://androidshopping.000webhostapp.com/cart_qty_inc.php");
                }

            }
        });

        //Decrease the item quantity
        dic.addBaseField(R.id.qty_decrease).onClick(new ItemClickListener() {
            @Override
            public void onClick(Object item, int position, final View view) {
                final Cart selectedItem = itemList.get(position);
                HashMap postData = new HashMap();
                double qty = selectedItem.quantity;
                double limit = 0;

                postData.put("txtPid", ""+ selectedItem.id);
                postData.put("mobile", "android");
                postData.put("unit", selectedItem.unit);

                if (selectedItem.unit.contains("kilo")){
                    limit = 0.25;
                }
                else {
                    limit = 1.00;
                }


                if(qty > limit)
                {
                    final PostResponseAsyncTask incTask = new PostResponseAsyncTask(CartFragment.this.getActivity(),
                            postData, new AsyncResponse() {
                        @Override
                        public void processFinish(String s) {

                            if(s.contains("success"))
                            {
                                /*FragmentTransaction ft = getFragmentManager().beginTransaction();
                                ft.detach(CartFragment.this).attach(CartFragment.this).commit();*/
                                switch(selectedItem.unit){
                                    case "kilo":
                                        selectedItem.quantity = selectedItem.getQty() - 0.25;
                                        break;
                                    default:
                                        selectedItem.quantity = selectedItem.getQty() - 1.0;
                                        break;
                                }
                                adapter.notifyDataSetChanged();
                                updateCartActivity();
                            }
                            else if (s.contains("failed")){
                                Toast.makeText(CartFragment.this.getActivity(),s,Toast.LENGTH_SHORT).show();
                            }
                        }

                    });
                    incTask.execute("https://androidshopping.000webhostapp.com/cart_qty_dec.php");
                }
            }
        });

        dic.addBaseField(R.id.editQty).onClick(new ItemClickListener() {
            @Override
            public void onClick(Object item, int position, View view) {
                final Cart selectedItem = itemList.get(position);
                LinearLayout linearLayout = new LinearLayout(CartFragment.this.getActivity());
                linearLayout.setOrientation(linearLayout.VERTICAL);
                linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
                final EditText input = new EditText(CartFragment.this.getActivity());
                input.setGravity(Gravity.CENTER_HORIZONTAL);
                input.setCursorVisible(true);
                input.setSingleLine(true);
                switch (selectedItem.unit){
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

                final AlertDialog.Builder enterQty = new AlertDialog.Builder(CartFragment.this.getActivity());

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
                        final String updateQty = input.getText().toString();

                        if (!updateQty.equals("")){
                            final Double updateQtyDouble = Double.parseDouble(updateQty);
                            if (updateQtyDouble > 0.0 && updateQtyDouble <= 99.0){
                                if (updateQtyDouble%0.25 == 0.0){
                                    HashMap postData = new HashMap();
                                    postData.put("txtPid", ""+ selectedItem.id);
                                    postData.put("mobile", "android");
                                    postData.put("txtQty", updateQty);



                                    final PostResponseAsyncTask incTask = new PostResponseAsyncTask(CartFragment.this.getActivity(),
                                            postData, new AsyncResponse() {
                                        @Override
                                        public void processFinish(String s) {

                                            if(s.contains("success"))
                                            {
                                                /*FragmentTransaction ft = getFragmentManager().beginTransaction();
                                                ft.detach(CartFragment.this).attach(CartFragment.this).commit();*/
                                                selectedItem.quantity = updateQtyDouble;
                                                adapter.notifyDataSetChanged();
                                                updateCartActivity();
                                                dialog.dismiss();
                                            }
                                        }

                                    });
                                    incTask.execute("https://androidshopping.000webhostapp.com/cart_qty_update.php");
                                }
                                else{
                                    Animation shake = AnimationUtils.loadAnimation(CartFragment.this.getActivity(), R.anim.shake);
                                    input.startAnimation(shake);
                                    Toast.makeText(CartFragment.this.getActivity(),"Please enter a valid quantity for kilo.",Toast.LENGTH_SHORT).show();
                                }
                            }
                            else{
                                Animation shake = AnimationUtils.loadAnimation(CartFragment.this.getActivity(), R.anim.shake);
                                input.startAnimation(shake);
                                Toast.makeText(CartFragment.this.getActivity(),"Please enter values from 1 to 99 only.",Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {
                            dialog.dismiss();
                        }
                    }
                });
            }
        });

        adapter = new FunDapter<>(CartFragment.this.getActivity(), itemList, R.layout.cart_row_v2, dic);
        lv = (ListView)view.findViewById(R.id.lvCart);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(this);

        if (adapter.getCount() == 0){
            showEmptyCart();
        }

        updateCartActivity();

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
        LinearLayout linearLayout = new LinearLayout(CartFragment.this.getActivity());
        linearLayout.setOrientation(linearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        final EditText input = new EditText(CartFragment.this.getActivity());
        input.setGravity(Gravity.START);
        input.setCursorVisible(true);
        input.setSingleLine(false);
        input.setScroller(new Scroller(getContext()));
        input.setVerticalScrollBarEnabled(true);
        input.setMinLines(1);
        input.setMaxLines(5);
        input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(320)});
        input.setHint("Add/edit special instruction");

        linearLayout.setPadding(10, 0, 10, 0);
        linearLayout.addView(input);

        final AlertDialog.Builder enterInstruction = new AlertDialog.Builder(CartFragment.this.getActivity());
        enterInstruction.setTitle("Special instruction for " + selectedItem.quantity + " " + selectedItem.unit +
                "/s of " + selectedItem.name);
        if (selectedItem.instruction == null){
            enterInstruction.setMessage("You did not set any instructions on this item yet.");
        }
        else{
            enterInstruction.setMessage("Your current instruction for this item: " + selectedItem.instruction);
        }
        enterInstruction.setView(linearLayout);

        enterInstruction.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        if(selectedItem.instruction != null){
            enterInstruction.setNeutralButton("Delete Instruction", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        }



        final AlertDialog dialog = enterInstruction.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap postData = new HashMap();
                postData.put("txtPId",""+selectedItem.id);
                PostResponseAsyncTask sendInstruction = new PostResponseAsyncTask(CartFragment.this.getActivity(), postData, new AsyncResponse() {
                    @Override
                    public void processFinish(String s) {
                        if (s.contains("success")){
                            Toast.makeText(CartFragment.this.getActivity(),"Instruction removed successfully", Toast.LENGTH_LONG).show();
                            /*FragmentTransaction ft = getFragmentManager().beginTransaction();
                            ft.detach(CartFragment.this).attach(CartFragment.this).commit();*/
                            selectedItem.instruction = null;
                            adapter.notifyDataSetChanged();
                            dialog.dismiss();
                        }
                        else{
                            Toast.makeText(CartFragment.this.getActivity(),"Instruction remove failed. Please try again.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                sendInstruction.execute("https://androidshopping.000webhostapp.com/remove_instruction.php");
            }
        });
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                final String txtInstruction = input.getText().toString().trim();
                HashMap postData = new HashMap();
                postData.put("txtInstruction",txtInstruction);
                postData.put("txtPId",""+selectedItem.id);

                if (!txtInstruction.equals("")){
                    PostResponseAsyncTask sendInstruction = new PostResponseAsyncTask(CartFragment.this.getActivity(), postData, new AsyncResponse() {
                        @Override
                        public void processFinish(String s) {
                            if (s.contains("success")){
                                /*FragmentTransaction ft = getFragmentManager().beginTransaction();
                                ft.detach(CartFragment.this).attach(CartFragment.this).commit();*/
                                selectedItem.instruction = txtInstruction;
                                adapter.notifyDataSetChanged();
                                Toast.makeText(CartFragment.this.getActivity(),"Instruction updated successfully", Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                            }
                            else{
                                Toast.makeText(CartFragment.this.getActivity(), s, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    sendInstruction.execute("https://androidshopping.000webhostapp.com/update_instruction.php");
                }
                else {
                    dialog.dismiss();
                }
            }
        });
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        dataPasser = (OnDataPass) context;
    }
}
