package com.example.android.inventoryapp;


import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.example.android.inventoryapp.fragments.ProductsFragment;
import com.example.android.inventoryapp.fragments.AddProductFragment;


public class MainActivity extends AppCompatActivity implements
        ProductsFragment.OnFragmentInteractionListener,
        AddProductFragment.OnFragmentInteractionListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        ViewPager viewPager = findViewById(R.id.viewpager);

        CategoryAdapter adapter = new CategoryAdapter(this, getSupportFragmentManager());

        // Set the adapter onto the view pager
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tabs);

        tabLayout.setupWithViewPager(viewPager);

    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
