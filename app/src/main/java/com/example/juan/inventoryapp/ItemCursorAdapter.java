package com.example.juan.inventoryapp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.juan.inventoryapp.data.ItemContract;
import com.example.juan.inventoryapp.data.ItemContract.ItemEntry;
import com.squareup.picasso.Picasso;

public class ItemCursorAdapter extends CursorAdapter {

    public ItemCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        ImageView iv_item_image_lv = (ImageView) view.findViewById(R.id.item_image_listView);
        TextView iv_item_name_lv = (TextView) view.findViewById(R.id.item_name_listView);
        TextView tv_item_price_lv = (TextView) view.findViewById(R.id.item_price_listView);
        TextView tv_item_brand_lv = (TextView) view.findViewById(R.id.item_brand_listView);
        TextView tv_item_quantity_lv = (TextView) view.findViewById(R.id.item_quantity_listView);
        final TextView tv_item_sales_lv = (TextView) view.findViewById(R.id.item_sales_listView);
        ImageView iv_item_sold_lv = (ImageView) view.findViewById(R.id.item_sold_listView);

        int image_column_index = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_IMAGE);
        int item_name_column_index = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_NAME);
        final int price_column_index = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_PRICE);
        int brand_column_index = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_BRAND);
        int quantity_column_index = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY);
        int sales_column_index = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_SALES);

        int id = cursor.getInt(cursor.getColumnIndex(ItemContract.ItemEntry._ID));
        Uri item_image = Uri.parse(cursor.getString(image_column_index));
        final String item_name = cursor.getString(item_name_column_index);
        String item_brand = cursor.getString(brand_column_index);
        String item_price = "Price: " + cursor.getString(price_column_index) + "$";
        String item_quantity = "Stock: " + cursor.getString(quantity_column_index);
        String item_sales = "Sales: " + cursor.getString(sales_column_index) + "$";
        final double current_item_price = cursor.getDouble(price_column_index);
        final int current_item_quantity = cursor.getInt(quantity_column_index);
        final double item_total_sales = cursor.getDouble(sales_column_index);
        final Uri currentProductUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id);

        iv_item_name_lv.setText(item_name);
        tv_item_price_lv.setText(item_price);
        tv_item_brand_lv.setText(item_brand);
        tv_item_quantity_lv.setText(item_quantity);
        tv_item_sales_lv.setText(item_sales);

        Picasso.with(context).load(item_image)
                .placeholder(R.drawable.no_image)
                .fit()
                .into(iv_item_image_lv);

        iv_item_sold_lv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ContentResolver resolver = v.getContext().getContentResolver();
                ContentValues values = new ContentValues();
                if (current_item_quantity > 0) {
                    int actualStock = current_item_quantity;
                    double actualPrice = current_item_price;
                    double salesSum = item_total_sales + actualPrice;
                    values.put(ItemEntry.COLUMN_ITEM_SALES, salesSum);
                    values.put(ItemEntry.COLUMN_ITEM_QUANTITY, --actualStock);
                    resolver.update(
                            currentProductUri,
                            values,
                            null,
                            null
                    );
                    context.getContentResolver().notifyChange(currentProductUri, null);
                } else {
                    Toast.makeText(context, R.string.no_stock_left, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
