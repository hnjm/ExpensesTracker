package com.amilabs.android.expensestracker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.amilabs.android.expensestracker.utils.SharedPref;

import java.util.Calendar;

public class DbOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = "DbOpenHelper";

    private static final String TABLE_NAME_EXPENSES = Data.Expenses.TABLE_NAME;
    private static final String EXPENSE_DATE = Data.Expenses.DATE;
    private static final String EXPENSE = Data.Expenses.EXPENSE;
    private static final String CATEGORY_ID = Data.Expenses.CATEGORY_ID;
    private static final String DETAILS = Data.Expenses.DETAILS;

    private static final String TABLE_NAME_CATEGORIES = Data.Categories.TABLE_NAME;
    private static final String NAME = Data.Categories.NAME;
    private static final String PERIOD = Data.Categories.PERIOD;
    private static final String LIMIT = Data.Categories.LIMIT;

    private static final String TABLE_NAME_CURRENCIES = Data.Currencies.TABLE_NAME;
    private static final String CURRENCY_NAME = Data.Currencies.NAME;

    private static final String CREATE_TABLE_EXPENSES = "CREATE TABLE " + TABLE_NAME_EXPENSES + " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " + 
            EXPENSE_DATE + " LONG, " +
            EXPENSE + " REAL, " + 
            CATEGORY_ID + " INTEGER, " +
            DETAILS + " TEXT)";
    private static final String CREATE_TABLE_CATEGORIES = "CREATE TABLE " + TABLE_NAME_CATEGORIES + " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            NAME + " TEXT, " +
            PERIOD + " TEXT, " +
            LIMIT + " REAL)";
    private static final String CREATE_TABLE_CURRENCIES = "CREATE TABLE " + TABLE_NAME_CURRENCIES + " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            CURRENCY_NAME + " TEXT)";

    private Context mContext;

    public DbOpenHelper(Context context) {
        super(context, Data.DB_NAME, null, Data.DB_VERSION);
        mContext = context;
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_EXPENSES);
        db.execSQL(CREATE_TABLE_CATEGORIES);
        db.execSQL(CREATE_TABLE_CURRENCIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	switch (oldVersion) {
	    	case 1:
	    		db.execSQL(CREATE_TABLE_CURRENCIES);
	    		break;
    	}
    }

}
