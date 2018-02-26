package com.a4t8bfarm.a4t8b.Fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.a4t8bfarm.a4t8b.Interfaces.Products;
import com.a4t8bfarm.a4t8b.Interfaces.UILConfig;
import com.a4t8bfarm.a4t8b.ProductDetailActivity;
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
import com.a4t8bfarm.a4t8b.R;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FruitsFragment extends Fragment implements AsyncResponse, AdapterView.OnItemClickListener {

    SessionManagement session;
    SQLiteDBHelper sqLiteDBHelper;

    final String LOG = "FruitsFragment";

    final static String url = "https://androidshopping.000webhostapp.com/product_fruits.php";

    private ArrayList<Products> productList;
    private ListView lv;
    FunDapter<Products> adapter;

    View view;


    public FruitsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_fruits, container, false);

        ImageLoader.getInstance().init(UILConfig.config(FruitsFragment.this.getActivity()));

        PostResponseAsyncTask taskRead = new PostResponseAsyncTask(FruitsFragment.this.getActivity(), this);
        taskRead.execute(url);


        return view;
    }

    @Override
    public void processFinish(String s) {

        productList = new JsonConverter<Products>().toArrayList(s, Products.class);

        BindDictionary dic = new BindDictionary();

        dic.addStringField(R.id.tvName, new StringExtractor<Products>() {
            @Override
            public String getStringValue(Products item, int position) {
                return item.name;
            }
        });

        /*dic.addStringField(R.id.tvDesc, new StringExtractor<Products>() {
            @Override
            public String getStringValue(Products item, int position) {
                return item.description;
            }
        }).visibilityIfNull(View.GONE);*/

        dic.addStringField(R.id.tvPricePerPiece, new StringExtractor<Products>() {
            @Override
            public String getStringValue(Products item, int position) {
                DecimalFormat pricing = new DecimalFormat("0.00");
                String price = pricing.format(item.price);
                switch(price){
                    case "0.00":
                        return null;
                    default:
                        return "Php " + price + "\n" + "each";
                }
            }
        }).visibilityIfNull(View.GONE);

        dic.addStringField(R.id.tvPricePerBundle, new StringExtractor<Products>() {
            @Override
            public String getStringValue(Products item, int position) {
                DecimalFormat pricing = new DecimalFormat("0.00");
                String price = pricing.format(item.pricePerBundle);
                switch(price){
                    case "0.00":
                        return null;
                    default:
                        return "Php " + price + "\n" + "per bundle";
                }
            }
        }).visibilityIfNull(View.GONE);

        dic.addStringField(R.id.tvPricePerKilo, new StringExtractor<Products>() {
            @Override
            public String getStringValue(Products item, int position) {
                DecimalFormat pricing = new DecimalFormat("0.00");
                String price = pricing.format(item.pricePerKilo);
                switch(price){
                    case "0.00":
                        return null;
                    default:
                        return "Php " + price + "\n" + "per kilo";
                }
            }
        }).visibilityIfNull(View.GONE);

        dic.addDynamicImageField(R.id.ivImage, new StringExtractor<Products>() {
            @Override
            public String getStringValue(Products item, int position) {
                return item.img_url;
            }
        }, new DynamicImageLoader() {
            @Override
            public void loadImage(String url, ImageView img) {
                //Set image
                ImageLoader.getInstance().displayImage(url, img);
            }
        });

        dic.addBaseField(R.id.btnAddToCart).onClick(new ItemClickListener() {
            @Override
            public void onClick(Object item, final int position, View view) {
                session = new SessionManagement(getContext());

                // get user data from session
                HashMap<String, String> user = session.getUserDetails();

                // name
                final String customer = user.get(SessionManagement.KEY_NAME);

                final Products selectedItem = productList.get(position);
                final HashMap postData = new HashMap();
                postData.put("txtName", selectedItem.name);
                postData.put("txtDesc", selectedItem.description);
                postData.put("txtImageUrl", selectedItem.img_url);
                postData.put("txtCustomer", customer);

                final List<String> unitSelection = new ArrayList<String>();

                if (selectedItem.price != 0){
                    unitSelection.add("piece");
                }
                if (selectedItem.pricePerBundle != 0){
                    unitSelection.add("bundle");
                }
                if (selectedItem.pricePerKilo != 0){
                    unitSelection.add("kilo");
                }

                final CharSequence unit[] = unitSelection.toArray(new CharSequence[unitSelection.size()]);
                AlertDialog.Builder selectUnitDialog= new AlertDialog.Builder(FruitsFragment.this.getActivity());
                selectUnitDialog.setSingleChoiceItems(unit, 0, null);
                selectUnitDialog.setTitle("Select the unit of your order.");
                selectUnitDialog.setCancelable(true);
                selectUnitDialog.setPositiveButton("OK", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, final int whichbutton) {
                        int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        final String selectedUnit = unit[selectedPosition].toString();
                        switch (selectedUnit) {
                            default:
                                postData.put("txtUnit", selectedUnit);
                                final Double price = 0.0;
                                switch (selectedUnit) {
                                    case "piece":
                                        postData.put("txtPrice", "" + selectedItem.price);

                                        openSelectQtyDialog("How many " + selectedUnit + "/s of " + selectedItem.name + " do you want? (1-99 only)",selectedUnit, new Qty() {
                                            @Override
                                            public void returnQty(Double qty) {

                                                postData.put("txtQty", "" + qty);
                                                addToCart(postData, selectedItem);
                                            }
                                        });
                                        break;
                                    case "bundle":
                                        postData.put("txtPrice", "" + selectedItem.pricePerBundle);

                                        openSelectQtyDialog("How many " + selectedUnit + "/s of " + selectedItem.name + " do you want? (1-99 only)",selectedUnit, new Qty() {
                                            @Override
                                            public void returnQty(Double qty) {

                                                postData.put("txtQty", "" + qty);
                                                addToCart(postData, selectedItem);
                                            }
                                        });
                                        break;
                                }

                                break;

                            case "kilo":
                                postData.put("txtPrice", "" + selectedItem.pricePerKilo);
                                postData.put("txtUnit", selectedUnit);

                                final CharSequence weight[] = new CharSequence[]{"1/4 kilo", "1/2 kilo", "3/4 kilo", "preferred kilo"};
                                AlertDialog.Builder selectWeightDialog = new AlertDialog.Builder(FruitsFragment.this.getActivity());
                                selectWeightDialog.setSingleChoiceItems(weight, 0, null);
                                selectWeightDialog.setTitle("Select the weight of your order.");
                                selectWeightDialog.setCancelable(true);
                                selectWeightDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                                        String unitQtyPerKilo = "0";
                                        switch (selectedPosition) {
                                            default:
                                                switch(selectedPosition){
                                                    case 0:
                                                        unitQtyPerKilo = "0.25";
                                                        break;
                                                    case 1:
                                                        unitQtyPerKilo = "0.5";
                                                        break;
                                                    case 2:
                                                        unitQtyPerKilo = "0.75";
                                                        break;
                                                }
                                                postData.put("txtQty", unitQtyPerKilo);
                                                addToCart(postData, selectedItem);
                                                break;

                                            case 3:
                                                openSelectQtyDialog("How many " + selectedUnit + "/s of " + selectedItem.name + " do you want? (1-99 only)", selectedUnit, new Qty() {
                                                    @Override
                                                    public void returnQty(Double qty) {
                                                        postData.put("txtQty", "" + qty);
                                                        addToCart(postData, selectedItem);
                                                    }
                                                });
                                                break;
                                        }
                                    }
                                });
                                selectWeightDialog.show();
                                break;
                        }
                    }
                });
                selectUnitDialog.show();

            }
        });

        adapter = new FunDapter<>(FruitsFragment.this.getActivity(), productList, R.layout.produce_product_row, dic);
        lv = (ListView) view.findViewById(R.id.lvProduct);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Products selectedProduct = productList.get(position);
        Bundle bundle = new Bundle();
        bundle.putSerializable("productList", selectedProduct);
        Intent intent = new Intent(FruitsFragment.this.getActivity(), ProductDetailActivity.class);
        intent.putExtra("from","produce");
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private class WriteToDB extends AsyncTask<Void, Void, Void>{
        String name;
        Products selectedItem;
        String qty;
        String selectedUnit;
        Double price;
        ProgressDialog progressDialog = new ProgressDialog(FruitsFragment.this.getActivity());

        public WriteToDB(String name, Products selectedItem, String qty, String selectedUnit, Double price){
            this.name = name;
            this.selectedItem = selectedItem;
            this.qty = qty;
            this.selectedUnit = selectedUnit;
            this.price = price;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setMessage("Loading...");
            progressDialog.setIndeterminate(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(true);
            progressDialog.show();


        }

        @Override
        protected Void doInBackground(Void... params){
            sqLiteDBHelper = new SQLiteDBHelper(FruitsFragment.this.getActivity());
            sqLiteDBHelper.insertRecords(name, selectedItem, qty, selectedUnit, price);
            return null;
        }

        @Override
        protected void onPostExecute(final Void unused) {

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    progressDialog.dismiss();
                    Toast.makeText(FruitsFragment.this.getActivity(), "Added to Cart: "+selectedItem.name, Toast.LENGTH_SHORT).show();
                }
            }, 1000); // 1000 milliseconds delay

        }
    }

    interface Qty {
        void returnQty (Double qty);
    }

    private void openSelectQtyDialog(String title, String selectedUnit, final Qty qty){
        LinearLayout linearLayout = new LinearLayout(FruitsFragment.this.getActivity());
        linearLayout.setOrientation(linearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        final EditText input = new EditText(FruitsFragment.this.getActivity());
        input.setGravity(Gravity.CENTER_HORIZONTAL);
        input.setCursorVisible(true);
        input.setSingleLine(true);

        switch (selectedUnit){
            case "kilo":
                input.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
                input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(5)});
                break;
            default:
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(2)});
                break;
        }



        input.setHint("1");
        linearLayout.setPadding(350, 0, 350, 0);
        linearLayout.addView(input);

        final AlertDialog.Builder selectQtyDialog = new AlertDialog.Builder(FruitsFragment.this.getActivity());
        selectQtyDialog.setTitle(title);
        if (selectedUnit.equals("kilo")){
            selectQtyDialog.setMessage("You may add 0.25, 0.5, and 0.75 for 1/4, 1/2, and 3/4 kilo respectively. Eg. 3.25 = 3 and 1/4 kilo.");
        }
        selectQtyDialog.setView(linearLayout);
        selectQtyDialog.setCancelable(true);
        selectQtyDialog.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });


        final AlertDialog dialog = selectQtyDialog.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Double qtyDouble = 1.0; //default value
                String qtyString = input.getText().toString();
                if(!qtyString.equals("")){
                    qtyDouble = Double.parseDouble(qtyString);
                    if(qtyDouble > 0.0 && qtyDouble <= 99.0){
                        if(qtyDouble%0.25 == 0.0){
                            qty.returnQty(qtyDouble);
                            dialog.dismiss();
                        }
                        else{
                            Animation shake = AnimationUtils.loadAnimation(FruitsFragment.this.getActivity(), R.anim.shake);
                            input.startAnimation(shake);
                            Toast.makeText(FruitsFragment.this.getActivity(),"Please enter a valid quantity for kilo.",Toast.LENGTH_SHORT).show();
                        }
                    }

                    else{
                        Animation shake = AnimationUtils.loadAnimation(FruitsFragment.this.getActivity(), R.anim.shake);
                        input.startAnimation(shake);
                        Toast.makeText(FruitsFragment.this.getActivity(),"Please enter values from 1 to 99 only.",Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    qty.returnQty(qtyDouble);
                    dialog.dismiss();
                }
            }
        });
    }

    private void addToCart(HashMap<String, String> postData, Products selectedItem){

        String qty = postData.get("txtQty");
        String selectedUnit = postData.get("txtUnit");
        Double price = Double.parseDouble(postData.get("txtPrice"));

        if (session.isLoggedIn()) {
            PostResponseAsyncTask insertTask = new PostResponseAsyncTask(
                    FruitsFragment.this.getActivity(), postData, new AsyncResponse() {
                @Override
                public void processFinish(String s) {
                    Log.d(LOG, s);
                    if (s.contains("success")) {
                        Toast.makeText(FruitsFragment.this.getActivity(), "Added to Cart", Toast.LENGTH_SHORT).show();
                    } else if (s.contains("limit reached")) {
                        Toast.makeText(FruitsFragment.this.getActivity(), "We limit orders to 99 per item only.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            insertTask.execute("https://androidshopping.000webhostapp.com/cart_insertv3.php");
        } else {

            new WriteToDB(selectedItem.name, selectedItem, qty, selectedUnit, price).execute();

        }
    }
}
