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
import android.widget.Spinner;
import android.widget.Toast;

import com.a4t8bfarm.a4t8b.Interfaces.Products;
import com.a4t8bfarm.a4t8b.Interfaces.UILConfig;
import com.a4t8bfarm.a4t8b.ProductDetailActivity;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class CoffeeFragment extends Fragment implements AsyncResponse, AdapterView.OnItemClickListener{
    SessionManagement session;
    SQLiteDBHelper sqLiteDBHelper;

    final String LOG = "CoffeeFragment";

    final static String url = "https://androidshopping.000webhostapp.com/product_coffee.php";

    private ArrayList<Products> productList;
    private ListView lv;
    FunDapter<Products> adapter;

    View view;

    Spinner spinnerCoffeeType;


    public CoffeeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_coffee, container, false);

        ImageLoader.getInstance().init(UILConfig.config(CoffeeFragment.this.getActivity()));

        PostResponseAsyncTask taskRead = new PostResponseAsyncTask(CoffeeFragment.this.getActivity(), this);
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

        dic.addStringField(R.id.tvPrice125g, new StringExtractor<Products>() {
            @Override
            public String getStringValue(Products item, int position) {
                DecimalFormat pricing = new DecimalFormat("0.00");
                String price = pricing.format(item.price125Grams);
                String s = "Php " + price + "\n" +"per 125g";
                return s;
            }
        });

        dic.addStringField(R.id.tvPrice500g, new StringExtractor<Products>() {
            @Override
            public String getStringValue(Products item, int position) {
                DecimalFormat pricing = new DecimalFormat("0.00");
                String price = pricing.format(item.price250Grams);
                String s = "Php " + price + "\n" +"per 250g";
                return s;
            }
        });

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
            public void onClick(Object item, int position, View view) {
                session = new SessionManagement(getContext());

                // get user data from session
                HashMap<String, String> user = session.getUserDetails();

                // name
                final String customer = user.get(SessionManagement.KEY_NAME);

                final Products selectedItem = productList.get(position);
                final HashMap postData = new HashMap();
                
                postData.put("txtDesc", selectedItem.description);
                postData.put("txtImageUrl", selectedItem.img_url);
                postData.put("txtCustomer", customer);

                final List<String> weightSelection = new ArrayList<String>();

                if (selectedItem.price125Grams != 0) {
                    weightSelection.add("125 grams");
                }
                if (selectedItem.price250Grams != 0) {
                    weightSelection.add("250 grams");
                }
                
                final List<String> typeSelection = new ArrayList<String>();
                typeSelection.add("ground");
                typeSelection.add("beans");
                
                openSelectTypeDialog(typeSelection, "What type of " + selectedItem.name + " would you like to order?", new CoffeeType() {
                    @Override
                    public void returnCoffeeType(String coffeeType) {
                        final String selectedName = selectedItem.name + " (" + coffeeType + ")";
                        postData.put("txtName", selectedName);
                        openSelectWeightDialog(weightSelection, "Select unit of " + selectedName, new Weight() {
                            @Override
                            public void returnWeight(String weight) {
                                Double selectedPrice = 0.0;

                                switch (weight){
                                    case("125 grams"):
                                        selectedPrice = selectedItem.price125Grams;
                                        break;
                                    case("250 grams"):
                                        selectedPrice = selectedItem.price250Grams;
                                        break;
                                }
                                postData.put("txtPrice",""+selectedPrice);
                                postData.put("txtUnit",weight);
                                openSelectQtyDialog("How many " + weight + "/s of " + selectedName + " do you want? (1-99 only)", new Qty() {
                                    @Override
                                    public void returnQty(Double qty) {

                                        postData.put("txtQty", "" + qty);
                                        addToCart(postData, selectedItem);
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });

        adapter = new FunDapter<>(CoffeeFragment.this.getActivity(), productList, R.layout.coffee_product_row, dic);
        lv = (ListView) view.findViewById(R.id.lvCoffeeProduct);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Products selectedProduct = productList.get(position);
        Bundle bundle = new Bundle();
        bundle.putSerializable("productList", selectedProduct);
        Intent intent = new Intent(CoffeeFragment.this.getActivity(), ProductDetailActivity.class);
        intent.putExtra("from","coffee");
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private class WriteToDB extends AsyncTask<Void, Void, Void>{
        String name;
        Products selectedItem;
        String qty;
        String selectedUnit;
        Double price;
        ProgressDialog progressDialog = new ProgressDialog(CoffeeFragment.this.getActivity());

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
            sqLiteDBHelper = new SQLiteDBHelper(CoffeeFragment.this.getActivity());
            sqLiteDBHelper.insertRecords(name, selectedItem, qty, selectedUnit, price);
            return null;
        }

        @Override
        protected void onPostExecute(final Void unused) {

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    progressDialog.dismiss();
                    Toast.makeText(CoffeeFragment.this.getActivity(), "Added to Cart: "+selectedItem.name, Toast.LENGTH_SHORT).show();
                }
            }, 1000); // 1000 milliseconds delay

        }
    }

    interface Qty {
        void returnQty (Double qty);
    }
    
    interface CoffeeType {
        void returnCoffeeType (String coffeeType);
    }
    
    interface Weight {
        void returnWeight (String weight);
    }
    
    
    private void openSelectWeightDialog (List<String> weightSelection, String title, final Weight weight){
        final CharSequence unit[] = weightSelection.toArray(new CharSequence[weightSelection.size()]);
        AlertDialog.Builder selectWeightDialog = new AlertDialog.Builder(CoffeeFragment.this.getActivity());
        selectWeightDialog.setSingleChoiceItems(unit,0,null);
        selectWeightDialog.setTitle(title);
        selectWeightDialog.setCancelable(true);
        selectWeightDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                String selectedWeight = unit[selectedPosition].toString();
                weight.returnWeight(selectedWeight);
            }
        });
        selectWeightDialog.show();
    }
    private void openSelectTypeDialog (List<String> typeSelection, String title, final CoffeeType coffeeType){
        final CharSequence unit[] = typeSelection.toArray(new CharSequence[typeSelection.size()]);
        AlertDialog.Builder selectTypeDialog = new AlertDialog.Builder(CoffeeFragment.this.getActivity());
        selectTypeDialog.setSingleChoiceItems(unit,0,null);
        selectTypeDialog.setTitle(title);
        selectTypeDialog.setCancelable(true);
        selectTypeDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                String selectedType = unit[selectedPosition].toString();
                coffeeType.returnCoffeeType(selectedType);
            }
        });
        selectTypeDialog.show();
    }

    private void openSelectQtyDialog(String title, final Qty qty){
        LinearLayout linearLayout = new LinearLayout(CoffeeFragment.this.getActivity());
        linearLayout.setOrientation(linearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        final EditText input = new EditText(CoffeeFragment.this.getActivity());
        input.setGravity(Gravity.CENTER_HORIZONTAL);
        input.setCursorVisible(true);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(2)});
        input.setHint("1");
        linearLayout.setPadding(400, 0, 400, 0);
        linearLayout.addView(input);

        final AlertDialog.Builder selectQtyDialog = new AlertDialog.Builder(CoffeeFragment.this.getActivity());
        selectQtyDialog.setTitle(title);
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
                    if(qtyDouble > 0.0 && qtyDouble < 100.0){
                        qty.returnQty(qtyDouble);
                        dialog.dismiss();
                    }
                    else{
                        Animation shake = AnimationUtils.loadAnimation(CoffeeFragment.this.getActivity(), R.anim.shake);
                        input.startAnimation(shake);
                        Toast.makeText(CoffeeFragment.this.getActivity(),"Please enter values of 1 to 99 only.",Toast.LENGTH_SHORT).show();
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

        String name = postData.get("txtName");
        String qty = postData.get("txtQty");
        String selectedUnit = postData.get("txtUnit");
        Double price = Double.parseDouble(postData.get("txtPrice"));

        if (session.isLoggedIn()) {
            PostResponseAsyncTask insertTask = new PostResponseAsyncTask(
                    CoffeeFragment.this.getActivity(), postData, new AsyncResponse() {
                @Override
                public void processFinish(String s) {
                    Log.d(LOG, s);
                    if (s.contains("success")) {
                        Toast.makeText(CoffeeFragment.this.getActivity(), "Added to Cart", Toast.LENGTH_SHORT).show();
                    } else if (s.contains("limit reached")) {
                        Toast.makeText(CoffeeFragment.this.getActivity(), "We limit orders to 99 per item only.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            insertTask.execute("https://androidshopping.000webhostapp.com/cart_insertv3.php");
        } else {

            new WriteToDB(name, selectedItem, qty, selectedUnit, price).execute();

        }
    }
}
