package com.a4t8bfarm.a4t8b.Fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.a4t8bfarm.a4t8b.Interfaces.Orders;
import com.a4t8bfarm.a4t8b.OrderDetailActivity;
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

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class OrderHistoryFragment extends Fragment implements AsyncResponse {
    final static String url = "https://androidshopping.000webhostapp.com/history_order.php";

    private ArrayList<Orders> orderList;
    private ListView lv;
    FunDapter<Orders> adapter;
    SessionManagement session;
    View view;

    public OrderHistoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_order_history, container, false);

        session = new SessionManagement(getContext());
        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        // name
        String customer = user.get(SessionManagement.KEY_NAME);

        HashMap postData = new HashMap();
        postData.put("txtUsername", customer);
        PostResponseAsyncTask taskRead = new PostResponseAsyncTask(OrderHistoryFragment.this.getActivity(), postData, this);
        taskRead.execute(url);

        return view;
    }


    @Override
    public void processFinish(String s) {

        orderList = new JsonConverter<Orders>().toArrayList(s, Orders.class);

        BindDictionary dic = new BindDictionary();

        dic.addStringField(R.id.tvOrderNumber, new StringExtractor<Orders>() {
            @Override
            public String getStringValue(Orders item, int position) {
                return item.OrderNumber;
            }
        });

        dic.addStringField(R.id.tvOrderDate, new StringExtractor<Orders>() {
            @Override
            public String getStringValue(Orders item, int position) {
                return item.OrderDate;
            }
        });

        dic.addStringField(R.id.tvOrderTotalPrice, new StringExtractor<Orders>() {
            @Override
            public String getStringValue(Orders item, int position) {
                double price = item.TotalCheckoutPrice;
                String s = String.format("%.2f",price);
                return s;
            }
        });

        dic.addBaseField(R.id.btnShowOrderDetail).onClick(new ItemClickListener() {
            @Override
            public void onClick(Object item, int position, View view) {
                Orders selectedOrder = orderList.get(position);
                Fragment detailFragment = new OrderDetailFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("orderList", selectedOrder);
                /*detailFragment.setArguments(bundle);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.hide(getFragmentManager().findFragmentByTag("order_history_fragment"));
                fragmentTransaction.add(R.id.content_main, detailFragment, "order_detail_fragment")
                        .addToBackStack("order_detail_fragment")
                        .commit();*/
                Intent intent = new Intent(OrderHistoryFragment.this.getActivity(), OrderDetailActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);

            }
        });



        adapter = new FunDapter<>(OrderHistoryFragment.this.getActivity(), orderList, R.layout.fragment_order_history_row, dic);
        lv = (ListView)view.findViewById(R.id.lvOrderHistory);
        lv.setAdapter(adapter);

        if(lv.getCount() == 0)
        {
            TextView empty = (TextView)view.findViewById(R.id.tvEmptyOrder);
            empty.setVisibility(View.VISIBLE);
        }


    }

    /*@Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Products selectedProduct = productList.get(position);

        Fragment detailFragment = new HomeDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("productList", selectedProduct);
        detailFragment.setArguments(bundle);
        FragmentManager manager = getFragmentManager();
        manager.beginTransaction().replace(R.id.content_main, detailFragment)
                .addToBackStack("HomeDetail").commit();
    }*/
}
