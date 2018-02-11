package com.example.android.inventoryapp;


import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.information.InventoryContract.ProductEntry;


public class ProductCursorAdapter extends CursorAdapter {


    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }


    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        ImageView itemImage = view.findViewById(R.id.item_image);
        TextView itemName = view.findViewById(R.id.item_name);
        TextView itemPrice = view.findViewById(R.id.price);
        final TextView itemQuantity = view.findViewById(R.id.quantity);
        ImageView sale = view.findViewById(R.id.sell);

        sale.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {
                int quantityFromTextView = Integer.parseInt(itemQuantity.getText().toString());
                if (quantityFromTextView == 0) {
                    Toast.makeText(context, "This product is out of stock", Toast.LENGTH_SHORT).show();

                    return;
                }

                if (quantityFromTextView > 0) {
                    quantityFromTextView = quantityFromTextView - 1;
                    itemQuantity.setText(String.valueOf(quantityFromTextView));
                }
                if (quantityFromTextView < 0) {
                    quantityFromTextView = 0;
                    itemQuantity.setText(String.valueOf(quantityFromTextView));
                }

            }
        });

        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);

        String productName = cursor.getString(nameColumnIndex);
        int productPrice = cursor.getInt(priceColumnIndex);
        int productQuantity = cursor.getInt(quantityColumnIndex);
        byte[] currentImage = cursor.getBlob(imageColumnIndex);

        if (!(currentImage == null)) {
            itemImage.setImageBitmap(convertToBitmap(currentImage));
        } else {
            itemImage.setImageResource(R.drawable.cart_add_icon);
        }

        itemName.setText(productName);
        itemPrice.setText(Integer.toString(productPrice));
        itemQuantity.setText(Integer.toString(productQuantity));

    }

    //get bitmap image from currentImage byte array
    private Bitmap convertToBitmap(byte[] currentImage) {
        return BitmapFactory.decodeByteArray(currentImage, 0, currentImage.length);
    }
}

