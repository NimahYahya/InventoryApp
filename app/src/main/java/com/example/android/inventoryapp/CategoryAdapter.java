package com.example.android.inventoryapp;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.android.inventoryapp.fragments.AddProductFragment;
import com.example.android.inventoryapp.fragments.ProductsFragment;

public class CategoryAdapter extends FragmentPagerAdapter {


    /**
     * Context of the app
     */
    private Context mContext;

    public CategoryAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }


    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new ProductsFragment();
        } else {
            return new AddProductFragment();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return mContext.getString(R.string.category_InventoryList);
        } else {
            return mContext.getString(R.string.category_AddInventory);
        }
    }
}

