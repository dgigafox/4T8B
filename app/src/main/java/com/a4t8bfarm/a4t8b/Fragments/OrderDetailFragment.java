package com.a4t8bfarm.a4t8b.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.a4t8bfarm.a4t8b.ConfirmOrderActivity;
import com.a4t8bfarm.a4t8b.Interfaces.Cart;
import com.a4t8bfarm.a4t8b.Interfaces.OrderDetails;
import com.a4t8bfarm.a4t8b.Interfaces.Orders;
import com.a4t8bfarm.a4t8b.R;
import com.a4t8bfarm.a4t8b.SessionManagement;
import com.amigold.fundapter.BindDictionary;
import com.amigold.fundapter.FunDapter;
import com.amigold.fundapter.extractors.StringExtractor;
import com.amigold.fundapter.interfaces.ItemClickListener;
import com.kosalgeek.android.json.JsonConverter;
import com.kosalgeek.genasync12.AsyncResponse;
import com.kosalgeek.genasync12.PostResponseAsyncTask;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class OrderDetailFragment extends Fragment implements AsyncResponse {
    final static String url = "https://androidshopping.000webhostapp.com/detail_order.php";

    private ArrayList<OrderDetails> orderDetailList;
    private ListView lv;
    FunDapter<OrderDetails> adapter;
    SessionManagement session;
    View view;

    public OrderDetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_order_detail, container, false);

        final Orders order = (Orders) getArguments().getSerializable("orderList");

        session = new SessionManagement(getContext());
        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        // name
        String customer = user.get(SessionManagement.KEY_NAME);

        TextView OrderNumber = (TextView)view.findViewById(R.id.tvOrderNumber);
        TextView OrderDate = (TextView)view.findViewById(R.id.tvOrderDate);
        TextView OrderTotalPrice = (TextView)view.findViewById(R.id.tvOrderTotalPrice);

        OrderNumber.setText(order.OrderNumber);
        OrderDate.setText(order.OrderDate);
        OrderTotalPrice.setText("Php " + order.TotalCheckoutPrice);

        HashMap postData = new HashMap();
        postData.put("txtUsername", customer);
        postData.put("txtOrderNumber",order.OrderNumber);
        PostResponseAsyncTask taskRead = new PostResponseAsyncTask(OrderDetailFragment.this.getActivity(), postData, this);
        taskRead.execute(url);

        return view;
    }


    @Override
    public void processFinish(String s) {

        orderDetailList = new JsonConverter<OrderDetails>().toArrayList(s, OrderDetails.class);

        BindDictionary dic = new BindDictionary();

        dic.addStringField(R.id.orderDetailName, new StringExtractor<OrderDetails>() {
            @Override
            public String getStringValue(OrderDetails item, int position) {
                return item.orderName;
            }
        });

        dic.addStringField(R.id.orderDetailUnit, new StringExtractor<OrderDetails>() {
            @Override
            public String getStringValue(OrderDetails item, int position) {
                return item.orderUnit;
            }
        }).visibilityIfNull(View.GONE);

        dic.addStringField(R.id.orderDetailQty, new StringExtractor<OrderDetails>() {
            @Override
            public String getStringValue(OrderDetails item, int position) {
                return ""+ item.orderQty;
            }
        });

        dic.addStringField(R.id.orderDetailPrice, new StringExtractor<OrderDetails>() {
            @Override
            public String getStringValue(OrderDetails item, int position) {
                double price = item.orderPrice;
                String s = String.format("%.2f",price);
                return  s;
            }
        });

        dic.addStringField(R.id.orderDetailInstruction, new StringExtractor<OrderDetails>() {
            @Override
            public String getStringValue(OrderDetails item, int position) {
                if (!item.orderInstruction.isEmpty()) {
                    return "Show";
                }
                else {
                    return null;
                }
            }
        }).visibilityIfNull(View.GONE).onClick(new ItemClickListener() {
            @Override
            public void onClick(Object item, int position, View view) {
                final OrderDetails selectedItem = orderDetailList.get(position);
                AlertDialog.Builder dialog = new AlertDialog.Builder(OrderDetailFragment.this.getActivity());
                dialog.setTitle("Special instruction for " + selectedItem.orderQty + " " + selectedItem.orderUnit + " of "
                        + selectedItem.orderName);
                dialog.setMessage(selectedItem.orderInstruction);
                dialog.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        adapter = new FunDapter<>(OrderDetailFragment.this.getActivity(), orderDetailList, R.layout.fragment_order_detail_row, dic);
        lv = (ListView)view.findViewById(R.id.lvOrderDetail);
        lv.setAdapter(adapter);


    }
}
