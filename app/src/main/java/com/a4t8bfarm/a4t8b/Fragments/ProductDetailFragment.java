package com.a4t8bfarm.a4t8b.Fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.a4t8bfarm.a4t8b.Interfaces.Products;
import com.a4t8bfarm.a4t8b.Interfaces.UILConfig;
import com.a4t8bfarm.a4t8b.R;
import com.a4t8bfarm.a4t8b.SQLiteDBHelper;
import com.a4t8bfarm.a4t8b.SessionManagement;
import com.kosalgeek.genasync12.AsyncResponse;
import com.kosalgeek.genasync12.PostResponseAsyncTask;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProductDetailFragment extends Fragment {
    SessionManagement session;
    SQLiteDBHelper sqLiteDBHelper;
    final String LOG = "ProductDetailFragment";

    TextView tvName, tvDesc, tvPricePerPiece, tvPricePerKilo, tvPricePerBundle, tvPrice125g, tvPrice500g;
    String img_url;
    Spinner SpinnerCoffeeType;
    ImageView imageView;
    Button btnAddToCart;

    public ProductDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_product_detailv2, container, false);

        ImageLoader.getInstance().init(UILConfig.config(ProductDetailFragment.this.getActivity()));

        final Products product = (Products) getArguments().getSerializable("productList");
        String from = getArguments().getString("fromFragment");

        tvName = (TextView) v.findViewById(R.id.tvName);

        SpinnerCoffeeType = (Spinner) v.findViewById(R.id.spinnerCoffeeType);
        tvDesc = (TextView) v.findViewById(R.id.tvDesc);
        tvPricePerPiece = (TextView) v.findViewById(R.id.tvPricePerPiece);
        tvPricePerKilo = (TextView) v.findViewById(R.id.tvPricePerKilo);
        tvPricePerBundle = (TextView) v.findViewById(R.id.tvPricePerBundle);
        tvPrice125g = (TextView) v.findViewById(R.id.tvPrice125g);
        tvPrice500g = (TextView) v.findViewById(R.id.tvPrice500g);

        img_url = product.img_url;
        imageView = (ImageView) v.findViewById(R.id.ivImage);
        btnAddToCart = (Button) v.findViewById(R.id.btnAddToCart);


        final TextView name, desc, price, pricePerKilo;
        final String qty, imurl;

        if(product !=null)
        {
            tvName.setText(product.name);

            if (from.contains("coffee")){
                String[] coffeeTypes = new String[]{"ground","beans"};
                List<String> coffeeType = new ArrayList<String>(Arrays.asList(coffeeTypes));

                ArrayAdapter<String> coffeeTypeAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, coffeeType);
                coffeeTypeAdapter.setDropDownViewResource(R.layout.spinner_textview_align);
                SpinnerCoffeeType.setAdapter(coffeeTypeAdapter);
            }
            else{
                SpinnerCoffeeType.setVisibility(View.GONE);
            }

            tvDesc.setText(product.description);
            if(product.price != 0){
                tvPricePerPiece.setText("Php" + product.price + "\n" + "each");
            }
            else {
                tvPricePerPiece.setVisibility(View.GONE);
            }
            if(product.pricePerKilo != 0){
                tvPricePerKilo.setText("Php" + product.pricePerKilo  + "\n" + "per kilo");
            }
            else {
                tvPricePerKilo.setVisibility(View.GONE);
            }
            if(product.pricePerBundle != 0){
                tvPricePerBundle.setText("Php" + product.pricePerBundle  + "\n" + "per bundle");
            }
            else {
                tvPricePerBundle.setVisibility(View.GONE);
            }
            if(product.price125Grams != 0){
                tvPrice125g.setText("Php" + product.price125Grams  + "\n" + "per 125g");
            }
            else {
                tvPrice125g.setVisibility(View.GONE);
            }
            if(product.price250Grams != 0){
                tvPrice500g.setText("Php" + product.price250Grams + "\n" + "per 250g");
            }
            else {
                tvPrice500g.setVisibility(View.GONE);
            }
            ImageLoader.getInstance().displayImage(img_url, imageView);
        }

        imurl = img_url;
        qty = "1";

        session = new SessionManagement(getContext());

        // get user data from session
        HashMap<String, String> user = session.getUserDetails();

        // name
        final String customer = user.get(SessionManagement.KEY_NAME);


        btnAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final HashMap postData = new HashMap();

                final String name;
                String coffeeType = (String) SpinnerCoffeeType.getSelectedItem();
                if (coffeeType != null){
                    name = product.name + " (" + coffeeType + ")";
                }
                else {
                    name = product.name;
                }


                Toast.makeText(ProductDetailFragment.this.getActivity(), name, Toast.LENGTH_SHORT).show();

                postData.put("txtName", name);
                postData.put("txtDesc", product.description);
                postData.put("txtImageUrl", imurl);
                postData.put("txtCustomer", customer);

                List<String> unitSelection = new ArrayList<String>();

                if (product.price != 0){
                    unitSelection.add("piece");
                }
                if (product.pricePerBundle != 0){
                    unitSelection.add("bundle");
                }
                if (product.pricePerKilo != 0){
                    unitSelection.add("kilo");
                }
                if (product.price125Grams != 0){
                    unitSelection.add("125 grams");
                }
                if (product.price250Grams != 0){
                    unitSelection.add("250 grams");
                }

                final CharSequence[] unit = unitSelection.toArray(new CharSequence[unitSelection.size()]);
                AlertDialog.Builder selectUnitDialog= new AlertDialog.Builder(ProductDetailFragment.this.getActivity());
                selectUnitDialog.setSingleChoiceItems(unit, 0, null);
                selectUnitDialog.setTitle("Select the unit of your order.");
                selectUnitDialog.setCancelable(true);
                selectUnitDialog.setPositiveButton("OK", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, final int whichbutton) {
                        int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                        final String selectedUnit = unit[selectedPosition].toString();
                        Double selectedPrice = 0.0;
                        String unitQty = "1";
                        switch (selectedUnit) {
                            default:
                                switch (selectedUnit){
                                    case("piece"):
                                        selectedPrice = product.price;
                                        break;
                                    case("bundle"):
                                        selectedPrice = product.pricePerBundle;
                                        break;
                                    case("125 grams"):
                                        selectedPrice = product.price125Grams;
                                        break;
                                    case("250 grams"):
                                        selectedPrice = product.price250Grams;
                                        break;
                                }
                                postData.put("txtUnit", selectedUnit);
                                postData.put("txtPrice", "" + selectedPrice);
                                openSelectQtyDialog("How many " + selectedUnit + "/s of " + name + " do you want? (1-99 only)", new Qty() {
                                    @Override
                                    public void returnQty(Double qty) {
                                        postData.put("txtQty", "" + qty);
                                        addToCart(postData, product);
                                    }
                                });

                                break;

                            case("kilo"):
                                postData.put("txtPrice", "" + product.pricePerKilo);
                                postData.put("txtUnit", selectedUnit);

                                final CharSequence weight[] = new CharSequence[]{"1/4 kilo", "1/2 kilo", "3/4 kilo", "(?) kilo"};
                                AlertDialog.Builder selectWeightDialog = new AlertDialog.Builder(ProductDetailFragment.this.getActivity());
                                selectWeightDialog.setSingleChoiceItems(weight, 0, null);
                                selectWeightDialog.setTitle("Select the weight of your order.");
                                selectWeightDialog.setCancelable(true);
                                selectWeightDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                                        String selectedWeight = weight[selectedPosition].toString();
                                        String unitQtyPerKilo = "0";
                                        switch (selectedPosition) {
                                            default:
                                                switch (selectedPosition){
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
                                                addToCart(postData, product);
                                                break;

                                            case 3:
                                                openSelectQtyDialog("How many " + selectedUnit + "/s of " + name + " do you want? (1-99 only)", new Qty() {
                                                    @Override
                                                    public void returnQty(Double qty) {
                                                        postData.put("txtQty", "" + qty);
                                                        addToCart(postData, product);
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

        return v;
    }

    private class WriteToDB extends AsyncTask<Void, Void, Void>{
        String name;
        Products selectedItem;
        String qty;
        String selectedUnit;
        Double price;
        ProgressDialog progressDialog = new ProgressDialog(ProductDetailFragment.this.getActivity());

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
            sqLiteDBHelper = new SQLiteDBHelper(ProductDetailFragment.this.getActivity());
            sqLiteDBHelper.insertRecords(name, selectedItem, qty, selectedUnit, price);
            return null;
        }

        @Override
        protected void onPostExecute(final Void unused) {

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    progressDialog.dismiss();
                    Toast.makeText(ProductDetailFragment.this.getActivity(), "Added to Cart: "+selectedItem.name, Toast.LENGTH_SHORT).show();
                }
            }, 1000); // 1000 milliseconds delay

        }
    }

    interface Qty {
        void returnQty (Double qty);
    }

    private void openSelectQtyDialog(String title, final Qty qty){
        LinearLayout linearLayout = new LinearLayout(ProductDetailFragment.this.getActivity());
        linearLayout.setOrientation(linearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        final EditText input = new EditText(ProductDetailFragment.this.getActivity());
        input.setGravity(Gravity.CENTER_HORIZONTAL);
        input.setCursorVisible(true);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("1");
        linearLayout.setPadding(400, 0, 400, 0);
        linearLayout.addView(input);

        final AlertDialog.Builder selectQtyDialog = new AlertDialog.Builder(ProductDetailFragment.this.getActivity());
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
                        Animation shake = AnimationUtils.loadAnimation(ProductDetailFragment.this.getActivity(), R.anim.shake);
                        input.startAnimation(shake);
                        Toast.makeText(ProductDetailFragment.this.getActivity(),"Please enter values of 1 to 99 only.",Toast.LENGTH_SHORT).show();
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
                    ProductDetailFragment.this.getActivity(), postData, new AsyncResponse() {
                @Override
                public void processFinish(String s) {
                    Log.d(LOG, s);
                    if (s.contains("success")) {
                        Toast.makeText(ProductDetailFragment.this.getActivity(), "Added to Cart", Toast.LENGTH_SHORT).show();
                    } else if (s.contains("limit reached")) {
                        Toast.makeText(ProductDetailFragment.this.getActivity(), "We limit orders to 99 per item only.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            insertTask.execute("https://androidshopping.000webhostapp.com/cart_insertv3.php");
        } else {

            new WriteToDB(name, selectedItem, qty, selectedUnit, price).execute();

        }
    }
}
