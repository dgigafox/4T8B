package com.a4t8bfarm.a4t8b.Fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.a4t8bfarm.a4t8b.R;
import com.a4t8bfarm.a4t8b.SessionManagement;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProductsFragment extends Fragment {

    public ProductsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_products, container, false);

        //Create the necessary tabs needed
        TabLayout tabLayout = (TabLayout)view.findViewById(R.id.productsTab);
        tabLayout.addTab(tabLayout.newTab().setText("Vegetables"));
        tabLayout.addTab(tabLayout.newTab().setText("Fruits"));
        tabLayout.addTab(tabLayout.newTab().setText("Coffee"));
        tabLayout.setTabTextColors(Color.parseColor("#28cd43"), Color.parseColor("#19812a"));
        tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#19812A"));

        //initialise the view pager
        final ViewPager viewPager = (ViewPager)view.findViewById(R.id.viewPager_products);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(new CustomAdapter(getChildFragmentManager(),
                tabLayout.getTabCount()));

        //Add listener to respond to touches and slides
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener(){

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return view;
    }

    private class CustomAdapter extends FragmentStatePagerAdapter {
        int numberOfTabs;

        private CustomAdapter(FragmentManager fragmentManager, int numberOfTabs) {
            super( fragmentManager);
            this.numberOfTabs = numberOfTabs;
        }

        @Override
        public Fragment getItem(int position) {

            switch (position)
            {
                case 0:
                    VegetablesFragment vegetables = new VegetablesFragment();
                    return vegetables;
                case 1:
                    FruitsFragment fruits = new FruitsFragment();
                    return fruits;
                case 2:
                    CoffeeFragment coffee = new CoffeeFragment();
                    return coffee;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {

            return numberOfTabs;
        }
    }

}
