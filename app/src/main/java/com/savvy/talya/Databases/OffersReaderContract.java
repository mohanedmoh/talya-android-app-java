package com.savvy.talya.Databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class OffersReaderContract {
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                    FeedEntry._ID + " INTEGER PRIMARY KEY," +
                    FeedEntry.GId + " TEXT," +
                    FeedEntry.start_date + " TEXT," +
                    FeedEntry.end_date + " TEXT," +
                    FeedEntry.currency + " TEXT," +
                    FeedEntry.currency_name + " TEXT," +
                    FeedEntry.advance + " TEXT," +
                    FeedEntry.added_value + " TEXT," +
                    FeedEntry.month_no + " TEXT," +
                    FeedEntry.plan_id + " TEXT," +
                    FeedEntry.type + " TEXT," +
                    FeedEntry.plan_name + " TEXT)";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private OffersReaderContract() {
    }

    /* Inner class that defines the table contents */
    public static class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "Offers";
        public static final String GId = "id";
        public static final String start_date = "start_date";
        public static final String end_date = "end_date";
        public static final String currency = "currency";
        public static final String currency_name = "currency_name";
        public static final String advance = "advance";
        public static final String added_value = "added_value";
        public static final String month_no = "month_no";
        public static final String plan_id = "plan_id";
        public static final String plan_name = "plan_name";
        public static final String type = "type";
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
