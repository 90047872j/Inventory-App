package com.example.juan.inventoryapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.juan.inventoryapp.data.ItemContract;
import com.example.juan.inventoryapp.data.ItemContract.ItemEntry;
import com.squareup.picasso.Picasso;

import java.io.File;

public class EditorActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {


    public static final int PHOTO_REQUEST_CODE = 20;
    public static final int EXTERNAL_STORAGE_REQUEST_PERMISSION_CODE = 21;
    private static final int EXISTING_PRODUCT_LOADER = 0;
    private Uri currentProductUri;
    private ImageView iv_item_image;
    private EditText et_item_name;
    private EditText et_item_price;
    private EditText et_item_brand;
    private EditText et_quantity;
    private TextView tv_quantity_label;
    private TextView tv_place_order_reminder;
    private TextView tv_sales;
    private EditText et_modStock;
    private ImageView iv_requestStock;
    private int deletedRows = 0;
    private boolean isItemUpdated = false;
    private int current_item_quantity = 0;
    private String currentPhotoUri = "no image";

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            isItemUpdated = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);


        ScrollView scroll_view = (ScrollView) findViewById(R.id.scrollView);
        LinearLayout ll_addStock = (LinearLayout) findViewById(R.id.new_stock_container_layout);
        LinearLayout ll_sales = (LinearLayout) findViewById(R.id.sales_container_layout);
        iv_item_image = (ImageView) findViewById(R.id.item_image);
        et_item_name = (EditText) findViewById(R.id.item_name);
        et_item_price = (EditText) findViewById(R.id.item_price);
        et_item_brand = (EditText) findViewById(R.id.item_brand);
        et_quantity = (EditText) findViewById(R.id.item_quantity);
        et_modStock = (EditText) findViewById(R.id.item_to_mod_quantity);
        tv_place_order_reminder = (TextView) findViewById(R.id.place_order_reminder_label);
        tv_sales = (TextView) findViewById(R.id.item_sales);
        iv_requestStock = (ImageView) findViewById(R.id.stock_request);
        tv_quantity_label = (TextView) findViewById(R.id.quantity_label_textView);
        Button b_minus = (Button) findViewById(R.id.button_subtraction);
        Button b_plus = (Button) findViewById(R.id.button_addition);

        scroll_view.fullScroll(ScrollView.FOCUS_UP);

        iv_item_image.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                isItemUpdated = true;
                return false;
            }
        });

        et_item_name.setOnTouchListener(mTouchListener);
        et_item_brand.setOnTouchListener(mTouchListener);
        et_item_price.setOnTouchListener(mTouchListener);
        et_quantity.setOnTouchListener(mTouchListener);

        iv_item_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateItemImage(v);
            }
        });

        Intent intent = getIntent();
        currentProductUri = intent.getData();

        if (currentProductUri == null) {
            setTitle(getString(R.string.activity_label_new_item));
            ll_addStock.setVisibility(ll_addStock.GONE);
            ll_sales.setVisibility(ll_sales.GONE);
            invalidateOptionsMenu();
        } else {

            setTitle(getString(R.string.activity_label_modify_item));
            tv_place_order_reminder.setVisibility(View.GONE);
            et_quantity.setKeyListener(null);
            et_quantity.setTextColor(getResources().getColor(R.color.colorNotEditable));
            et_quantity.setBackgroundColor(getResources().getColor(R.color.colorTransparent));
            tv_quantity_label.setText(R.string.item_current_stock_label);
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        b_minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reduceNewStock();
            }
        });

        b_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increaseStock();
            }
        });

    }

    private void updateItemStock() {

        String newStock = et_modStock.getText().toString();

        ContentValues values = new ContentValues();
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY, newStock);

        if (currentProductUri != null) {
            int rowUpdated = getContentResolver().update(currentProductUri, values, null, null);

            if (rowUpdated == 0) {
                Toast.makeText(this, R.string.no_stock_updated, Toast.LENGTH_LONG).show();
            } else {
                et_quantity.setText(newStock);
                isItemUpdated = true;

            }
        }
    }


    private void increaseStock() {
        current_item_quantity = Integer.parseInt(et_quantity.getText().toString());
        int newStock = Integer.parseInt(et_modStock.getText().toString());
        int result = newStock + 1;
        String addition = String.valueOf(result);
        et_modStock.setText(addition);
        if (result > current_item_quantity) {
            tv_place_order_reminder.setVisibility(View.VISIBLE);
        } else {
            tv_place_order_reminder.setVisibility(View.GONE);
        }
    }

    private void reduceNewStock() {
        current_item_quantity = Integer.parseInt(et_quantity.getText().toString());
        int newStock = Integer.parseInt(et_modStock.getText().toString());
        if (newStock > 0) {
            int result = newStock - 1;
            String subtraction = String.valueOf(result);
            et_modStock.setText(subtraction);
            if (result > current_item_quantity) {
                tv_place_order_reminder.setVisibility(View.VISIBLE);
            } else {
                tv_place_order_reminder.setVisibility(View.GONE);
            }
        }
    }

    private String createOrderSummary(String item, String stock) {
        String message = "Stock request:\n" + item + " units: " + stock;
        return message;
    }

    private void requestStock() {
        String message = createOrderSummary(et_item_name.getText().toString(), et_modStock.getText().toString());
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_SUBJECT,
                getString(R.string.order_summary_email_subject, et_item_name.getText().toString()));
        intent.putExtra(Intent.EXTRA_TEXT, message);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_message_delete);
        builder.setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.button_cancel, null);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteItem() {
        if (currentProductUri != null) {
            getContentResolver().delete(currentProductUri, null, null);
            if (deletedRows == 0) {
                Toast.makeText(this, R.string.item_deleted, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(EditorActivity.this, CatalogActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, R.string.no_item_deleted, Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    private void showDiscardChanges(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_message_discard_changes_label);
        builder.setPositiveButton(R.string.button_discard_changes, discardButtonClickListener);
        builder.setNegativeButton(R.string.button_keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!isItemUpdated) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showDiscardChanges(discardButtonClickListener);
    }

    public void updateItemImage(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
                chooseItemImage();
            } else {
                String[] permisionRequest = {Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(permisionRequest, EXTERNAL_STORAGE_REQUEST_PERMISSION_CODE);
            }
        } else {
            chooseItemImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == EXTERNAL_STORAGE_REQUEST_PERMISSION_CODE && grantResults[0] ==
                PackageManager.PERMISSION_GRANTED) {
            chooseItemImage();
        } else {
            Toast.makeText(this, R.string.permission_image, Toast.LENGTH_LONG).show();
        }
    }

    private void chooseItemImage() {
        Intent selectImageIntent = new Intent(Intent.ACTION_PICK);

        File imageDirectory = Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = imageDirectory.getPath();
        Uri data = Uri.parse(pictureDirectoryPath);
        selectImageIntent.setDataAndType(data, "image/*");
        startActivityForResult(selectImageIntent, PHOTO_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
            }
            Uri mProductPhotoUri = data.getData();
            currentPhotoUri = mProductPhotoUri.toString();

            Picasso.with(this).load(mProductPhotoUri)
                    .placeholder(R.drawable.no_image)
                    .fit()
                    .into(iv_item_image);
        }
    }

    private void AddNewProduct() {
        String item_name = et_item_name.getText().toString();
        String item_brand = et_item_brand.getText().toString();
        String item_price = et_item_price.getText().toString();
        String item_quantity = et_quantity.getText().toString();

        if (item_name.isEmpty() || item_price.isEmpty() || item_brand.isEmpty() || item_quantity.isEmpty()) {
            Toast.makeText(this, R.string.missing_fields, Toast.LENGTH_LONG).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_IMAGE, currentPhotoUri);
        values.put(ItemEntry.COLUMN_ITEM_NAME, item_name);
        values.put(ItemEntry.COLUMN_ITEM_PRICE, item_price);
        values.put(ItemEntry.COLUMN_ITEM_BRAND, item_brand);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY, item_quantity);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_SALES, 0.0);

        if (currentProductUri == null) {

            Uri insertedRow = getContentResolver().insert(ItemEntry.CONTENT_URI, values);

            if (insertedRow == null) {
                Toast.makeText(this, R.string.no_item_added, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.item_added, Toast.LENGTH_LONG).show();
            }
        } else {
            int rowUpdated = getContentResolver().update(currentProductUri, values, null, null);

            if (rowUpdated > 0 && isItemUpdated) {
                Toast.makeText(this, R.string.item_updated, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, CatalogActivity.class);
                startActivity(intent);
            } else if (rowUpdated == 0) {
                Toast.makeText(this, R.string.no_item_updated, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.no_changes, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (currentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.delete_product);
            menuItem.setVisible(false);
        }
        if (currentProductUri != null) {
            menu.findItem(R.id.saved_product);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.saved_product:
                updateItemStock();
                AddNewProduct();
                finish();
                return true;
            case R.id.delete_product:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ItemEntry._ID,
                ItemContract.ItemEntry.COLUMN_ITEM_IMAGE,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_PRICE,
                ItemEntry.COLUMN_ITEM_BRAND,
                ItemEntry.COLUMN_ITEM_QUANTITY,
                ItemContract.ItemEntry.COLUMN_ITEM_SALES};

        return new CursorLoader(this,
                currentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int image_column_index = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_IMAGE);
            int item_name_column_index = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
            int price_column_index = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
            int brand_column_index = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_BRAND);
            int quantity_column_index = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
            int sales_column_index = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_SALES);

            String name = cursor.getString(item_name_column_index);
            float price = cursor.getFloat(price_column_index);
            String brand = cursor.getString(brand_column_index);
            int quantity = cursor.getInt(quantity_column_index);
            int sales = cursor.getInt(sales_column_index);
            currentPhotoUri = cursor.getString(image_column_index);

            et_item_name.setText(name);
            et_item_price.setText(String.valueOf(price));
            et_item_brand.setText(brand);
            et_quantity.setText(String.valueOf(quantity));
            et_modStock.setText(String.valueOf(quantity));
            tv_sales.setText(String.valueOf(sales));

            Picasso.with(this).load(currentPhotoUri)
                    .placeholder(R.drawable.no_image)
                    .fit()
                    .into(iv_item_image);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        et_item_name.setText("");
        et_item_brand.setText("");
        et_quantity.setText("");
        et_item_price.setText("");
    }

    private void showDialogRequestMerchandise() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_send_order);
        builder.setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                requestStock();

            }
        });
        builder.setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setTitle(getString(R.string.dialog_stock_request_label));
        alertDialog.show();
    }
}
