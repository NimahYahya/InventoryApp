package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.information.InventoryContract.ProductEntry;

public class DetailsActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {


    private TextView itemName;
    private TextView itemPrice;
    private TextView itemQuantity;
    private TextView supplierName;
    private TextView supplierPhone;
    private TextView supplierEmail;
    private ImageView productImage;


    private static final int EXISTING_ITEM_LOADER = 0;

    private Uri currentItemUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_layout);

        Intent intent = getIntent();
        currentItemUri = intent.getData();

        getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);

        productImage = findViewById(R.id.d_product_image);
        itemName = findViewById(R.id.d_item_name);
        itemPrice = findViewById(R.id.d_product_price);
        itemQuantity = findViewById(R.id.d_product_quantity);
        supplierName = findViewById(R.id.d_supplier_name);
        supplierPhone = findViewById(R.id.d_supplier_phone);
        supplierEmail = findViewById(R.id.d_supplier_email);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_edit_product:

                editProductActivity();
                finish();
                return true;


            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;

            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void editProductActivity() {
        Intent editProductIntent = new Intent(DetailsActivity.this, EditorActivity.class);
        // Set the URI on the data field of the intent
        editProductIntent.setData(currentItemUri);
        startActivity(editProductIntent);
    }

    @Override
    public void onBackPressed() {
        finish();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle args) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL,
                ProductEntry.COLUMN_PRODUCT_IMAGE};

        return new CursorLoader(this,
                currentItemUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor == null || cursor.getCount() < 1)
            return;

        // Proceed with moving to the first row of the cursor and reading data from it
        // (There should be the only row in the cursor as we are in editor activity for a single Item)
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            int supplierEmailColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE);
            int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);

            String productName = cursor.getString(nameColumnIndex);
            int productPrice = cursor.getInt(priceColumnIndex);
            int productQuantity = cursor.getInt(quantityColumnIndex);
            String productSupplierName = cursor.getString(supplierNameColumnIndex);
            String productSupplierEmail = cursor.getString(supplierEmailColumnIndex);
            String productSupplierPhone = cursor.getString(supplierPhoneColumnIndex);
            byte[] currentImage = cursor.getBlob(imageColumnIndex);

            itemName.setText(productName);
            itemPrice.setText(Integer.toString(productPrice));
            itemQuantity.setText(Integer.toString(productQuantity));
            supplierName.setText(productSupplierName);
            supplierEmail.setText(productSupplierEmail);
            supplierPhone.setText(productSupplierPhone);

            if (!(currentImage == null)) {
                productImage.setImageBitmap(convertToBitmap(currentImage));
            } else {
                productImage.setImageResource(R.drawable.cart_add_icon);
            }


        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        itemName.setText("");
        itemPrice.setText("");
        itemQuantity.setText("");
        supplierName.setText("");
        supplierEmail.setText("");
        supplierPhone.setText("");
        productImage.setImageResource(R.drawable.cart_add_icon);


    }

    private Bitmap convertToBitmap(byte[] currentImage) {
        return BitmapFactory.decodeByteArray(currentImage, 0, currentImage.length);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the readymix product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the readymix product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {

        if (currentItemUri != null) {

            int rowsDeleted = getContentResolver().delete(currentItemUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.delete_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.delete_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }


        finish();
    }
}




