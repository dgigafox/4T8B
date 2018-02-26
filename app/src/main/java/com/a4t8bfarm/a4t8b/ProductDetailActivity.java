package com.a4t8bfarm.a4t8b;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.a4t8bfarm.a4t8b.Fragments.ProductDetailFragment;
import com.a4t8bfarm.a4t8b.Fragments.VegetablesFragment;
import com.a4t8bfarm.a4t8b.Interfaces.PassToDetail;
import com.a4t8bfarm.a4t8b.Interfaces.Products;

import java.util.ArrayList;

public class ProductDetailActivity extends AppCompatActivity{

    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    ProductDetailFragment productDetailFragment = new ProductDetailFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Products selectedProducts = (Products) this.getIntent().getSerializableExtra("productList");
        String from = this.getIntent().getStringExtra("from");
        Bundle bundle = new Bundle();
        bundle.putSerializable("productList",selectedProducts);
        bundle.putString("fromFragment",from);
        productDetailFragment.setArguments(bundle);
        transaction.add(R.id.frame_product_detail,productDetailFragment).commit();

        /*String name = bundle.getString("ProductName");
        Toast.makeText(ProductDetailActivity.this, name, Toast.LENGTH_SHORT).show();*/


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                /*Intent h = NavUtils.getParentActivityIntent(this);
                h.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, h);*/
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
