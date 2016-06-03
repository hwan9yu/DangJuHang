package com.example.tazo.sqltest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Tazo on 2016-06-02.
 */
public class MyDBHandler extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "productDB.db";
    public static final String DATABASE_TABLE = "products";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PRODUCTNAME = "productname";
    public static final String COLUMN_QUANTITY = "quantity";

    public MyDBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "create table if not exists " +
                DATABASE_TABLE +"(" +COLUMN_ID+" integer primary key autoincrement, " +
                COLUMN_PRODUCTNAME+" text, " + COLUMN_QUANTITY +" integer "+" )";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + DATABASE_TABLE);
        onCreate(db);
    }

    public void addProduct(Product product){
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRODUCTNAME,product.getProductName());
        values.put(COLUMN_QUANTITY,product.getQuantity());

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(DATABASE_TABLE, null, values);
        db.close();
    }
    public boolean deleteProduct(String productname){
        boolean result = false;
        String query = "select * from " + DATABASE_TABLE +" where "
                + COLUMN_PRODUCTNAME + "= \'" + productname + "\'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        Product product = new Product();
        if(cursor.moveToFirst()){
            product.setID(Integer.parseInt(cursor.getString(0)));
            db.delete(DATABASE_TABLE, COLUMN_ID + "=?", new String[]{String.valueOf(product.getID())});
            cursor.close();
            db.close();
            return true;
        }

        db.close();
        return result;
    }
    public Product findProduct(String productname){
        String query = "select * from " + DATABASE_TABLE + " where " +
                COLUMN_PRODUCTNAME + "=\'" + productname + "\'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        Product product = new Product();
        if(cursor.moveToFirst()){
            product.setID(Integer.parseInt(cursor.getString(0)));
            product.setProductname(cursor.getString(1));
            product.setQuantity(Integer.parseInt(cursor.getString(2)));
            cursor.close();
        }else{
            product = null;
        }
        db.close();
        return product;
    }
}
