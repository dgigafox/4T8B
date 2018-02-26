package com.a4t8bfarm.a4t8b.Interfaces;

import java.util.ArrayList;

/**
 * Created by Darren Gegantino on 10/4/2017.
 */

public interface OnDataPass {
    public void onDataPass(ArrayList<Cart> itemList, String checkoutSum, Integer itemCount);
}
