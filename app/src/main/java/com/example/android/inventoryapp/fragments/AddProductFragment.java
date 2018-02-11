package com.example.android.inventoryapp.fragments;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.support.v4.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.R;
import com.example.android.inventoryapp.information.InventoryContract.ProductEntry;

import java.io.ByteArrayOutputStream;

import static android.app.Activity.RESULT_OK;

public class AddProductFragment extends Fragment {

    private EditText itemName;
    private EditText itemPrice;
    private EditText itemQuantity;
    private EditText supplierName;
    private EditText supplierPhone;
    private EditText supplierEmail;
    private Button plusButton;
    private Button minusButton;
    private FloatingActionButton addImageButton;
    private ImageView productImage;
    private Bitmap mBitmap;
    private byte[] mPhoto;
    private boolean itemHasChanged = false;


    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            itemHasChanged = true;
            return false;
        }
    };

    private OnFragmentInteractionListener mListener;

    public AddProductFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.add_fragment_layout, container, false);
        setHasOptionsMenu(true);

        itemName = view.findViewById(R.id.product_name);
        itemPrice = view.findViewById(R.id.product_price);
        itemQuantity = view.findViewById(R.id.item_quantity);
        supplierName = view.findViewById(R.id.supplier_name);
        supplierPhone = view.findViewById(R.id.supplier_phone);
        supplierEmail = view.findViewById(R.id.supplier_email);
        plusButton = view.findViewById(R.id.plus);
        minusButton = view.findViewById(R.id.minus);
        addImageButton = view.findViewById(R.id.add_image_button);
        productImage = view.findViewById(R.id.product_image);

        itemName.setOnTouchListener(mTouchListener);
        itemPrice.setOnTouchListener(mTouchListener);
        itemQuantity.setOnTouchListener(mTouchListener);
        supplierName.setOnTouchListener(mTouchListener);
        supplierPhone.setOnTouchListener(mTouchListener);
        supplierEmail.setOnTouchListener(mTouchListener);
        plusButton.setOnTouchListener(mTouchListener);
        minusButton.setOnTouchListener(mTouchListener);
        addImageButton.setOnTouchListener(mTouchListener);

        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        //when item quantity field inside EditText is changed (for input validation in real time)
        itemQuantity.addTextChangedListener(new TextValidator(itemQuantity) {
            @Override
            public void validate(TextView textView, String text) {
                // Validation code here
                if (!text.isEmpty()) {
                    if (Integer.parseInt(text) > 100 || Integer.parseInt(text) < 0) {
                        itemQuantity.setError("Quantity should be between 0-100");
                        itemQuantity.setText("0");
                    }
                }
            }
        });

        //handle clicks on +, - and add image button
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemQuantity.setError(null);
                String currentQuantity = itemQuantity.getText().toString().trim();

                if (currentQuantity.isEmpty()) {
                    itemQuantity.setText("1");
                } else {
                    int currentItemQuantity = Integer.parseInt(currentQuantity);
                    itemQuantity.setText(String.valueOf(currentItemQuantity + 1));
                }
            }
        });

        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemQuantity.setError(null);
                String currentQuantity = itemQuantity.getText().toString().trim();

                if (!currentQuantity.isEmpty()) {
                    int currentItemQuantity = Integer.parseInt(currentQuantity);
                    itemQuantity.setText(String.valueOf(currentItemQuantity - 1));
                } else
                    itemQuantity.setText("0");
            }
        });


        return view;
    }

    public void selectImage() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, 2);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 2:
                if (resultCode == RESULT_OK) {
                    Uri choosenImage = data.getData();

                    if (choosenImage != null) {

                        mBitmap = decodeUri(choosenImage, 400);
                        productImage.setImageBitmap(mBitmap);
                    }
                }
        }
    }


    //COnvert and resize our image to 400dp for faster uploading our images to DB
    protected Bitmap decodeUri(Uri selectedImage, int REQUIRED_SIZE) {

        try {

            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(getContext().getContentResolver().openInputStream(selectedImage), null, o);

            // The new size we want to scale to
            // final int REQUIRED_SIZE =  size;

            // Find the correct scale value. It should be the power of 2.
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE
                        || height_tmp / 2 < REQUIRED_SIZE) {
                    break;
                }
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(getContext().getContentResolver().openInputStream(selectedImage), null, o2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //Convert bitmap to bytes
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private byte[] imageToDB(Bitmap b) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 0, bos);
        return bos.toByteArray();
    }

    private void saveItem() {
        //read from input fields
        String itemNameString = itemName.getText().toString().trim();
        String itemPriceString = itemPrice.getText().toString().trim();
        String itemQuantityString = itemQuantity.getText().toString().trim();
        String supplierNameString = supplierName.getText().toString().trim();
        String supplierPhoneString = supplierPhone.getText().toString().trim();
        String supplierEmailString = supplierEmail.getText().toString().trim();
        if (!(mBitmap == null)) {
            mPhoto = imageToDB(mBitmap);
        } else {
            mPhoto = null;
        }


        boolean status = formValidate(itemNameString, itemPriceString, itemQuantityString, supplierNameString,
                supplierPhoneString, supplierEmailString);

        if (!status)
            return;

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, itemNameString);

        int itemPriceInt = Integer.parseInt(itemPriceString);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, itemPriceInt);

        int itemQuantityInt = Integer.parseInt(itemQuantityString);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, itemQuantityInt);

        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME, supplierNameString);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE, supplierPhoneString);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL, supplierEmailString);
        values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, mPhoto);


        //if (currentItemUri == null) {//new Item
        Uri newUri = getContext().getContentResolver().insert(ProductEntry.CONTENT_URI, values);

        if (newUri == null) {
            Toast.makeText(getContext(), "Error while saving new Item", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "New Item added", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.add_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save new readymix product to database
                saveItem();

                return true;

        }
        return super.onOptionsItemSelected(item);
    }


    //to validate the item quantity inside the EditText
    public abstract static class TextValidator implements TextWatcher {
        private final TextView textView; //This will work as every EditText is a TextView as well

        public TextValidator(TextView textView) {
            this.textView = textView;
        }

        public abstract void validate(TextView textView, String text);

        @Override
        final public void afterTextChanged(Editable s) {
            String text = textView.getText().toString().trim();
            validate(textView, text);
        }

        @Override
        final public void beforeTextChanged(CharSequence s, int start, int count, int after) { /* Don't care */ }

        @Override
        final public void onTextChanged(CharSequence s, int start, int before, int count) { /* Don't care */ }
    }

    //for Form Validation
    boolean formValidate(String itemNameString, String itemPriceString, String itemQuantityString,
                         String supplierNameString, String supplierPhoneString, String supplierEmailString) {

        boolean status = true;

        if (itemNameString.equals("")) {
            status = false;
            itemName.setError("Item Name required");
        }

        if (itemPriceString.equals("")) {
            status = false;
            itemPrice.setError("Item Price required");
        }

        if (itemQuantityString.equals("")) {
            status = false;
            itemQuantity.setError("Item Quantity required");
        }

        if (supplierNameString.equals("")) {
            status = false;
            supplierName.setError("Supplier Name required");
        }

        if (supplierPhoneString.equals("")) {
            status = false;
            supplierPhone.setError("Supplier Phone required");
        }

        if (supplierEmailString.equals("")) {
            status = false;
            supplierEmail.setError("Supplier Email required");
        }

        return status;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        //onBackPressed();
    }

    public interface OnFragmentInteractionListener {

        void onFragmentInteraction(Uri uri);
    }

}





