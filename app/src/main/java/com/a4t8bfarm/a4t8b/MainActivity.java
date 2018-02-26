package com.a4t8bfarm.a4t8b;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.a4t8bfarm.a4t8b.Fragments.AboutFragment;
import com.a4t8bfarm.a4t8b.Fragments.CartFragment;
import com.a4t8bfarm.a4t8b.Fragments.LocalCartFragment;
import com.a4t8bfarm.a4t8b.Fragments.OrderHistoryFragment;
import com.a4t8bfarm.a4t8b.Fragments.ProductsFragment;
import com.a4t8bfarm.a4t8b.Fragments.UserDetailsFragment;
import com.a4t8bfarm.a4t8b.Interfaces.Cart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private boolean exit = false;
    SessionManagement session;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkIntent();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        session = new SessionManagement(getApplicationContext());

        // get user data from session
        HashMap<String, String> user = session.getUserDetails();

        // name
        String customer = user.get(SessionManagement.KEY_NAME);
        String firstName = user.get(SessionManagement.KEY_FNAME);
        String lastName = user.get(SessionManagement.KEY_LNAME);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SQLiteDBHelper sqLiteDBHelper = new SQLiteDBHelper(MainActivity.this);
                sqLiteDBHelper.deleteAllRecords();
                Toast.makeText(MainActivity.this, "All records deleted", Toast.LENGTH_SHORT).show();
                *//*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*//*
            }
        });*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /*Toast.makeText(getApplicationContext(), "User Login Status: " + session.isLoggedIn(), Toast.LENGTH_LONG).show();*/
        /*boolean check = preferences.getBoolean(IS_LOGIN, false);*/
        View headerView = navigationView.getHeaderView(0);
        Menu navMenu = navigationView.getMenu();
        TextView username = (TextView) navigationView.getHeaderView(0).findViewById(R.id.tvUsername);

        //calling products fragment
        ProductsFragment productsFragment = new ProductsFragment();
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.content_main, productsFragment,
                "products_fragment")
                .addToBackStack("products_fragment")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        navigationView.getMenu().getItem(1).setChecked(true);

        //modify navigation drawer view based on logged in status
        if (session.isLoggedIn()){
            username.setText("Hi, " + firstName + " " + lastName);
            headerView.setVisibility(View.VISIBLE);
            navMenu.findItem(R.id.nav_login).setVisible(false);
            navMenu.findItem(R.id.nav_logout).setVisible(true);
            navMenu.findItem(R.id.nav_order_history).setVisible(true);
        }
        else {
            headerView.setVisibility(View.GONE);
            navMenu.findItem(R.id.nav_login).setVisible(true);
            navMenu.findItem(R.id.nav_logout).setVisible(false);
            navMenu.findItem(R.id.nav_account_settings).setVisible(false);
            navMenu.findItem(R.id.nav_order_history).setVisible(false);
        }

    }

    @Override
    public void onBackPressed() {
        String homeTag = "products_fragment";
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

        else {
            FragmentManager fragmentManager = getSupportFragmentManager();

            String tag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
            Fragment currentFragment = fragmentManager.findFragmentByTag(tag);
                if(tag.equals(homeTag)){
                    final AlertDialog.Builder exit = new AlertDialog.Builder(this);
                    exit.setTitle("Confirm Exit");
                    exit.setMessage("Do you really want to exit the app?");
                    exit.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    exit.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    exit.show();
                }
                else {
                    /*ProductsFragment productsFragment = new ProductsFragment();*/
                    fragmentManager.beginTransaction().hide(currentFragment).show(fragmentManager.findFragmentByTag(homeTag))
                            .addToBackStack(homeTag)
                            .commit();
                    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                    navigationView.getMenu().getItem(1).setChecked(true);
                    setTitle("4T8B");
                    /*Toast.makeText(this, tag, Toast.LENGTH_SHORT).show();*/
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if(id == R.id.action_shopping_cart)
        {
            Intent intent = new Intent(MainActivity.this, CartActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FragmentManager manager = getSupportFragmentManager();

        if (id == R.id.nav_login) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.putExtra("from","MainActivity");
            startActivity(intent);
        } else if (id == R.id.nav_shop_now) {
            String thisTag = "products_fragment";
            ProductsFragment productsFragment = new ProductsFragment();
            manager.executePendingTransactions();
            /*boolean fragmentPopped = manager.popBackStackImmediate("products_fragment", 0);*/
            Fragment fragment = manager.findFragmentByTag(thisTag);
            String tag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
            Fragment currentFragment = manager.findFragmentByTag(tag);
            if (fragment == null){

                manager.beginTransaction().hide(currentFragment)
                        .add(R.id.content_main, productsFragment,
                                thisTag)
                        .addToBackStack(thisTag)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
            }
            else {

                manager.beginTransaction().hide(currentFragment)
                        .show(manager.findFragmentByTag(thisTag))
                        .addToBackStack(thisTag)
                        .commit();

            }


            setTitle("4T8B");
        } else if (id == R.id.nav_order_history) {
            String thisTag = "order_history_fragment";
            OrderHistoryFragment orderHistoryFragment = new OrderHistoryFragment();
            manager.executePendingTransactions();
            /*boolean fragmentPopped = manager.popBackStackImmediate("order_history_fragment", 0);*/
            Fragment fragment = manager.findFragmentByTag(thisTag);
            String tag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
            Fragment currentFragment = manager.findFragmentByTag(tag);
            if (fragment == null){

                manager.beginTransaction().hide(currentFragment)
                        .add(R.id.content_main, orderHistoryFragment,
                        thisTag)
                        .addToBackStack(thisTag)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
                      }
            else {

                manager.beginTransaction().hide(currentFragment)
                        .show(manager.findFragmentByTag(thisTag))
                        .addToBackStack(thisTag)
                        .commit();

            }


        } else if (id == R.id.nav_about) {
            String thisTag = "about_fragment";
            AboutFragment aboutFragment = new AboutFragment();
            manager.executePendingTransactions();
            /*boolean fragmentPopped = manager.popBackStackImmediate("about_fragment", 0);*/
            Fragment fragment = manager.findFragmentByTag(thisTag);
            String tag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
            Fragment currentFragment = manager.findFragmentByTag(tag);
            if (fragment == null){

                manager.beginTransaction().hide(currentFragment)
                        .add(R.id.content_main, aboutFragment,
                        thisTag)
                        .addToBackStack(thisTag)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
            }
            else {

                manager.beginTransaction().hide(currentFragment)
                        .show(manager.findFragmentByTag(thisTag))
                        .addToBackStack(thisTag)
                        .commit();

            }

        } else if (id == R.id.nav_deals) {


        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_account_settings) {
            String thisTag = "user_details";
            UserDetailsFragment userDetailsFragment = new UserDetailsFragment();
            manager.executePendingTransactions();
            /*boolean fragmentPopped = manager.popBackStackImmediate("about_fragment", 0);*/
            Fragment fragment = manager.findFragmentByTag(thisTag);
            String tag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
            Fragment currentFragment = manager.findFragmentByTag(tag);
            if (fragment == null){

                manager.beginTransaction().hide(currentFragment)
                        .add(R.id.content_main, userDetailsFragment,
                                thisTag)
                        .addToBackStack(thisTag)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
            }
            else {

                manager.beginTransaction().hide(currentFragment)
                        .show(manager.findFragmentByTag(thisTag))
                        .addToBackStack(thisTag)
                        .commit();

            }

        } else if (id == R.id.nav_logout) {
            session.logoutUser();
            Toast.makeText(getApplicationContext(), "You have logged out.", Toast.LENGTH_SHORT).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);


        item.setChecked(true);
        if (id != R.id.nav_shop_now){
            if(id != R.id.nav_login){
                setTitle(item.getTitle());
            }
        }

        return true;
    }

    public void checkIntent() {
        try {
            Intent i = getIntent();
            String activity = i.getStringExtra("from");// is firing an error if there is no intent call
            if (activity.equals("CartActivity")) {
                setContentView(R.layout.activity_main);
                View parentLayout = findViewById(android.R.id.content);
                Snackbar.make(parentLayout, "We have added your order to your online cart.", Snackbar.LENGTH_LONG)
                        .setAction("Show Cart", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent i = new Intent(MainActivity.this, CartActivity.class);
                                startActivity(i);
                            }
                        })
                        .setActionTextColor(getResources().getColor(R.color.colorButton))
                        .setDuration(10000).show();
            }
        } catch (Exception e) {
            setContentView(R.layout.activity_main); // that shows just my Content
        }

    }

    public Fragment getActiveFragment() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            return null;
        }
        String tag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
        return getSupportFragmentManager().findFragmentByTag(tag);
    }
}
