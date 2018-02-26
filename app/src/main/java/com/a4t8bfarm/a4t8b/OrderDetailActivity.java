package com.a4t8bfarm.a4t8b;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.a4t8bfarm.a4t8b.Fragments.OrderDetailFragment;
import com.a4t8bfarm.a4t8b.Fragments.ProductDetailFragment;
import com.a4t8bfarm.a4t8b.Interfaces.Orders;
import com.a4t8bfarm.a4t8b.Interfaces.Products;

public class OrderDetailActivity extends AppCompatActivity {

    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    OrderDetailFragment orderDetailFragment = new OrderDetailFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Orders selectedOrder = (Orders) this.getIntent().getSerializableExtra("orderList");
        Bundle bundle = new Bundle();
        bundle.putSerializable("orderList",selectedOrder);
        orderDetailFragment.setArguments(bundle);
        transaction.add(R.id.frame_order_detail,orderDetailFragment).commit();

        /*String name = bundle.getString("ProductName");
        Toast.makeText(ProductDetailActivity.this, name, Toast.LENGTH_SHORT).show();*/


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
