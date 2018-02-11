package com.example.android.inventoryapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.information.InventoryContract.ProductEntry;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


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


    private static final int EXISTING_ITEM_LOADER = 0;

    private boolean itemHasChanged = false;

    /**
     * Content URI for the existing item (null if it's a new item)
     */
    private Uri currentItemUri;


    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            itemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor_layout);

        Intent intent = getIntent();
        currentItemUri = intent.getData();

        //start the loader and fill in the EditTexts and Image in editor activity
        getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);


        itemName = findViewById(R.id.product_name);
        itemPrice = findViewById(R.id.product_price);
        itemQuantity = findViewById(R.id.item_quantity);
        supplierName = findViewById(R.id.supplier_name);
        supplierPhone = findViewById(R.id.supplier_phone);
        supplierEmail = findViewById(R.id.supplier_email);
        plusButton = findViewById(R.id.plus);
        minusButton = findViewById(R.id.minus);
        addImageButton = findViewById(R.id.add_image_button);
        productImage = findViewById(R.id.product_image);

        itemName.setOnTouchListener(mTouchListener);
        itemPrice.setOnTouchListener(mTouchListener);
        itemQuantity.setOnTouchListener(mTouchListener);
        supplierName.setOnTouchListener(mTouchListener);
        supplierPhone.setOnTouchListener(mTouchListener);
        supplierEmail.setOnTouchListener(mTouchListener);
        plusButton.setOnTouchListener(mTouchListener);
        minusButton.setOnTouchListener(mTouchListener);
        addImageButton.setOnTouchListener(mTouchListener);

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

        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
    }


    public void selectImage() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, 2);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 2:
                if (resultCode == RESULT_OK) {
                    Uri choosenImage = data.getData();

                    if (choosenImage != null) {

                        mBitmap = decodeUri(choosenImage, 400);
                        productImage.setImageBitmap(mBitmap);
                    } else {
                        productImage.setImageBitmap(convertToBitmap(mPhoto));
                    }
                }
        }


    }

    protected Bitmap decodeUri(Uri selectedImage, int REQUIRED_SIZE) {

        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

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
            return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);
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

    //get user input and save the item in DB
    private void updateProduct() {
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
        productImage.setImageBitmap(mBitmap);


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

        int rowsAffected = getContentResolver().update(currentItemUri, values, null, null);
        if (rowsAffected == 0)
            Toast.makeText(this, "Error with updating Item", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "Item Updated", Toast.LENGTH_SHORT).show();
    }


    private void deleteItem() {
        // Only perform the delete if this is an existing item.
        if (currentItemUri != null) {
            int rowsDeleted = getContentResolver().delete(currentItemUri, null, null);

            if (rowsDeleted == 0)
                Toast.makeText(this, "Error with deleting Item", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "Item Deleted", Toast.LENGTH_SHORT).show();
        }
        // Close the activity
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all Item attributes, define a projection that contains all columns from inventory table
        String[] projection = {
                // InventoryContract.InventoryEntry._ID,  //Not needed as I will not be showing ID in Editor Activity's Form
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL,
                ProductEntry.COLUMN_PRODUCT_IMAGE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                currentItemUri,        // Query the content URI for the current
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
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
        // If the loader is invalidated, clear out all the data from the input fields.
        itemName.setText("");
        itemPrice.setText("");
        itemQuantity.setText("");
        supplierName.setText("");
        supplierEmail.setText("");
        supplierPhone.setText("");
        productImage.setImageResource(R.drawable.cart_add_icon);

    }

    //get bitmap image from currentImage byte array
    private Bitmap convertToBitmap(byte[] currentImage) {
        return BitmapFactory.decodeByteArray(currentImage, 0, currentImage.length);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {

            case R.id.action_save:
                updateProduct();
                finish();
                return true;

            case R.id.action_delete_item:
                // Pop up confirmation dialog for delete( this will only be visible when Editor
                // activity is opened for updating an existing item and not during addition of a new Item)
                showDeleteConfirmationDialog();
                return true;

            case R.id.order_more:
                //will be visible only while updating and not while adding new Item
                showOrderDialog();
                return true;

            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the Item hasn't changed, continue with navigating up to parent activity
                if (!itemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void showOrderDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Choose an option to place your order");

        builder.setPositiveButton("Email", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //send Email Intent

                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{supplierEmail.getText().toString().trim()});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Need More Items");
                intent.putExtra(Intent.EXTRA_TEXT, "Please send additional" + " " + itemQuantity.getText().toString().trim() + " " + itemName.getText().toString().trim());
                if (intent.resolveActivity(getPackageManager()) != null)
                    startActivity(intent);
            }
        });

        builder.setNegativeButton("Phone", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //send Dialer Intent
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + supplierPhone.getText().toString().trim()));
                startActivity(intent);
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() { //same as R.id.home above
        // If the Item hasn't changed, continue with handling back button press
        if (!itemHasChanged) {
            super.onBackPressed();
            return;
        }
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog();
    }

    private void showUnsavedChangesDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard your changes and quit editing?");

        builder.setPositiveButton("Discard", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners for the postive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete this Item?");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteItem();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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
}



