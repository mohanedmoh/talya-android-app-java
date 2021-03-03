package com.savvy.talya.Databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class PlotsReaderContract {
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                    FeedEntry._ID + " INTEGER PRIMARY KEY," +
                    FeedEntry.GId + " TEXT," +
                    FeedEntry.plot_no + " TEXT," +
                    FeedEntry.plot_area + " TEXT," +
                    FeedEntry.cat + " TEXT," +
                    FeedEntry.meter_price + " TEXT," +
                    FeedEntry.price_sdg + " TEXT," +
                    FeedEntry.price_usd + " TEXT," +
                    FeedEntry.status + " TEXT," +
                    FeedEntry.status_id + " TEXT," +
                    FeedEntry.status_description + " TEXT," +
                    FeedEntry.allowOffer + " TEXT," +
                    FeedEntry.isRegistered + " TEXT," +
                    FeedEntry.basic + " TEXT," +
                    FeedEntry.total + " TEXT," +
                    FeedEntry.extra_amount + " TEXT," +
                    FeedEntry.extra_quantity + " TEXT," +

                    FeedEntry.blueprint_id + " TEXT)";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private PlotsReaderContract() {
    }

    /* Inner class that defines the table contents */
    public static class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "plots";
        public static final String GId = "id";
        public static final String plot_no = "plot_no";
        public static final String plot_area = "plot_area";
        public static final String cat = "cat";
        public static final String blueprint_id = "blueprint_id";
        public static final String meter_price = "meter_price";
        public static final String price_sdg = "price_sdg";
        public static final String price_usd = "price_usd";
        public static final String status = "status";
        public static final String status_id = "status_id";
        public static final String status_description = "status_description";
        public static final String allowOffer = "allowOffer";
        public static final String basic = "basic";
        public static final String isRegistered = "isRegistered";
        public static final String total = "total";
        public static final String extra_amount = "extra_amount";
        public static final String extra_quantity = "extra_quantity";


    }

    public static class FeedReaderDbHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "FeedReader.db";

        public FeedReaderDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }
}
