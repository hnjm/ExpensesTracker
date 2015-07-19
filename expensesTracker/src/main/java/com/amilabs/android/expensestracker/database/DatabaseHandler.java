package com.amilabs.android.expensestracker.database;

import com.amilabs.android.expensestracker.R;
import com.amilabs.android.expensestracker.interfaces.Constants;
import com.amilabs.android.expensestracker.utils.SharedPref;
import com.amilabs.android.expensestracker.utils.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler implements Constants {

    private static final String TAG = "DatabaseHandler";

    private Context context;
    private SQLiteDatabase database;
    private DbOpenHelper dbHelper;
    private static DatabaseHandler db;

    public static DatabaseHandler getInstance(Context ctx) {
        if (db == null)
            db = new DatabaseHandler(ctx);
        return db;
    }

    private DatabaseHandler(Context ctx) {
        context = ctx;
    }

    public void init() {
        dbHelper = new DbOpenHelper(context);
        database = dbHelper.getWritableDatabase();
        initDB();
    }

    private void initDB() {
        // init default time ranges
        boolean[] values = SharedPref.getPeriods(context);
        int counter = 0;
        for (boolean value: values)
            if (!value)
                counter++;
        if (counter == values.length) {
            for (int i = 0; i < values.length; i++)
                values[i] = i == 0 || i == 3 || i == 7;
            SharedPref.savePeriods(context, values);
        }
        // init default categories
        if (getCategoriesCount() == 0) {
            String[] defaultCategories = context.getResources().getStringArray(R.array.category);
            for (int i = 0; i < defaultCategories.length - 1; i++) {
                addCategory(defaultCategories[i], 100, WEEK);
            }
        }
    }
    
    public void close() {
        database.close();
        dbHelper.close();
        db = null;
    }

    public int getExpensesDataCount() {
        int res = 0;
        String[] projection = new String[] { Data.Expenses.ID };
        Cursor c = database.query(Data.Expenses.TABLE_NAME, projection, null, null, null, null, null);
        if (c != null)
            res = c.getCount();
        return res;
    }

    public int getExpensesDataCount(int days) {
        int res = 0;
        long currentDate = System.currentTimeMillis();
        long oldDate = currentDate - (long)days * DAY_IN_MSEC;
        String[] projection = new String[] { Data.Expenses.ID };
        String selection = Data.Expenses.DATE + " >= " + oldDate + " AND " + Data.Expenses.DATE +
                " <= " + currentDate;
        Cursor c = database.query(Data.Expenses.TABLE_NAME, projection, selection, null, null, null, null);
        if (c != null)
            res = c.getCount();
        return res;
    }

    public Cursor getExpensesData(int days) {
        long currentDate = System.currentTimeMillis();
        long oldDate = currentDate - (long) days * DAY_IN_MSEC;
        //String[] projection = new String[] { Data.Expenses.ID, Data.Expenses.DATE, Data.Expenses.EXPENSE, Data.Expenses.CATEGORY };
        //String selection = Data.Expenses.DATE + " >= " + oldDate + " AND " + Data.Expenses.DATE + " <= " + currentDate;
        //return database.query(Data.Expenses.TABLE_NAME, projection, selection, null, null, null, Data.Expenses.DATE);
        return database.rawQuery("select e._id, e.date, e.expense, c.name, e.details from EXPENSES e, CATEGORIES c " +
        		"where c._id = e.category_id and e.date >= " + oldDate + " and e.date <= " +
                currentDate + " order by e.date;", null);
    }
    
    public long getExpenseDate(int id) {
        long res = -1;
        String[] projection = new String[] { Data.Expenses.DATE };
        String selection = Data.Expenses.ID + " = " + id;
        Cursor c = database.query(Data.Expenses.TABLE_NAME, projection, selection, null, null, null, null);
        if (c != null) {
            if (c.moveToFirst())
                res = c.getLong(0);
            c.close();
        }
        return res;
    }
    
    public float getExpenseValue(int id) {
        float res = -1;
        String[] projection = new String[] { Data.Expenses.EXPENSE };
        String selection = Data.Expenses.ID + " = " + id;
        Cursor c = database.query(Data.Expenses.TABLE_NAME, projection, selection, null, null, null, null);
        if (c != null) {
            if (c.moveToFirst())
                res = c.getFloat(0);
            c.close();
        }
        return res;
    }
    
    public String getExpenseDetails(int id) {
        String res = null;
        String[] projection = new String[] { Data.Expenses.DETAILS };
        String selection = Data.Expenses.ID + " = " + id;
        Cursor c = database.query(Data.Expenses.TABLE_NAME, projection, selection, null, null, null, null);
        if (c != null) {
            if (c.moveToFirst())
                res = c.getString(0);
            c.close();
        }
        return res == null ? "" : res;
    }
    
    public String getExpenseCategory(int id) {
        String res = null;
        Cursor c = database.rawQuery("select c.name from EXPENSES e, CATEGORIES c " +
                "where c._id = e.category_id and e._id = " + id + ";", null);
        if (c != null) {
            if (c.moveToFirst())
                res = c.getString(0);
            c.close();
        }
        return res;
    }
    
    public Cursor getExpensesByCategory(String category, long dateFrom, long dateTo) {
        return database.rawQuery("select e._id, e.date, c.name, e.expense, e.details from EXPENSES e, CATEGORIES c " +
                "where c._id = e.category_id and e.date >= " + dateFrom + " and e.date <= " + dateTo +
                " and c.name = '" + category + "' order by e.date;", null);
    }
    
    public Cursor getExpensesByCategories(ArrayList<String> categories, long dateFrom, long dateTo) {
    	String names = "(";
    	for (int i = 0; i < categories.size(); i++) {
    		if (i == categories.size() - 1)
    			names += "'" + categories.get(i) + "')";
    		else
    			names += "'" + categories.get(i) + "',";
    	}
        return database.rawQuery("select e._id, e.date, c.name, e.expense, e.details from EXPENSES e, CATEGORIES c " +
                "where c._id = e.category_id and e.date >= " + dateFrom + " and e.date <= " + dateTo +
                " and c.name in " + names + " order by e.date;", null);
    }
    
    public float getTotalExpensesByCategory(String category, long dateFrom, long dateTo) {
        float res = 0;
        Cursor c = database.rawQuery("select e.expense from EXPENSES e, CATEGORIES c " +
                "where c._id = e.category_id and e.date >= " + dateFrom + " and e.date <= " + dateTo +
                " and c.name = '" + category + "';", null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    res += c.getFloat(0);
                } while (c.moveToNext());
            }
            c.close();
        }
        return res;
    }
    
    public float getTotalExpenses(int days) {
        float res = 0;
        long currentDate = System.currentTimeMillis();
        long oldDate = currentDate - (long)days * DAY_IN_MSEC;
        String[] projection = new String[] { Data.Expenses.EXPENSE };
        String selection = Data.Expenses.DATE + " >= " + oldDate + " AND " + Data.Expenses.DATE +
                " <= " + currentDate;
        Cursor c = database.query(Data.Expenses.TABLE_NAME, projection, selection, null, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    res += c.getFloat(0);
                } while (c.moveToNext());
            }
            c.close();
        }
        return res;
    }
    
    public float getTotalExpenses(long dateFrom, long dateTo) {
        float res = 0;
        String[] projection = new String[] { Data.Expenses.EXPENSE };
        String selection = Data.Expenses.DATE + " >= " + dateFrom + " AND " + Data.Expenses.DATE +
                " <= " + dateTo;
        Cursor c = database.query(Data.Expenses.TABLE_NAME, projection, selection, null, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    res += c.getFloat(0);
                } while (c.moveToNext());
            }
            c.close();
        }
        return res;
    }

    public synchronized Cursor getSpentOnCategories(Context ctx, TrackerType type) {
        long dateFrom = SharedPref.getDateFrom(ctx, type);
        if (dateFrom == 0)
            dateFrom = System.currentTimeMillis() - DatabaseHandler.DAY_IN_MSEC;
        dateFrom = Utils.getFreshDate(ctx, dateFrom);
        long dateTo = Utils.getFreshDate(ctx, SharedPref.getDateTo(ctx, type));
        return database.rawQuery("select c._id, c.name, c.amount_limit, c.period, sum(e.expense) from CATEGORIES c " +
                "inner join EXPENSES e on c._id = e.category_id and e.date >= " + dateFrom +
                " and e.date <= " + dateTo + " group by c.name, c.amount_limit order by sum(e.expense) desc;", null);
    }

    public void addExpense(long date, float expense, String category, String details) {
        ContentValues values = new ContentValues();
        values.put(Data.Expenses.DATE, date);
        values.put(Data.Expenses.EXPENSE, expense);
        values.put(Data.Expenses.CATEGORY_ID, getCategoryId(category));
        values.put(Data.Expenses.DETAILS, details);
        long insertId = database.insert(Data.Expenses.TABLE_NAME, null, values);
        if (insertId == -1)
            Log.e(TAG, "Error occurred during inserting data (expense) into DB");
    }

    public void updateExpense(int id, long date, float expense, String category, String details) {
        ContentValues values = new ContentValues();
        values.put(Data.Expenses.DATE, date);
        values.put(Data.Expenses.EXPENSE, expense);
        values.put(Data.Expenses.CATEGORY_ID, getCategoryId(category));
        values.put(Data.Expenses.DETAILS, details);
        String where = Data.Expenses.ID + " = " + id;
        int rows = database.update(Data.Expenses.TABLE_NAME, values, where, null);
        if (rows == 0)
            Log.e(TAG, "No rows updated");
    }

    public void updateAllWithNewRate(double rate) {
        Cursor c = database.query(Data.Expenses.TABLE_NAME,
                new String[] { Data.Expenses.ID, Data.Expenses.EXPENSE }, null, null, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    int id = c.getInt(c.getColumnIndex(Data.Expenses.ID));
                    double expense = c.getDouble(c.getColumnIndex(Data.Expenses.EXPENSE));
                    expense *= rate;
                    updateExpense(id, expense);
                } while (c.moveToNext());
            }
            c.close();
        }
        c = database.query(Data.Categories.TABLE_NAME,
                new String[] { Data.Categories.ID, Data.Categories.LIMIT }, null, null, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    int id = c.getInt(c.getColumnIndex(Data.Categories.ID));
                    double limit = c.getDouble(c.getColumnIndex(Data.Categories.LIMIT));
                    limit *= rate;
                    updateCategoryLimit(id, limit);
                } while (c.moveToNext());
            }
            c.close();
        }
    }
    
    private void updateExpense(int id, double expense) {
        ContentValues values = new ContentValues();
        values.put(Data.Expenses.EXPENSE, expense);
        String where = Data.Expenses.ID + " = " + id;
        int rows = database.update(Data.Expenses.TABLE_NAME, values, where, null);
        if (rows == 0)
            Log.e(TAG, "No rows updated");
    }
    
    private void updateCategoryLimit(int id, double limit) {
        ContentValues values = new ContentValues();
        values.put(Data.Categories.LIMIT, limit);
        String where = Data.Categories.ID + " = " + id;
        int rows = database.update(Data.Categories.TABLE_NAME, values, where, null);
        if (rows == 0)
            Log.e(TAG, "No rows updated");
    }
    
    public void deleteExpense(List<Integer> ids) {
        String selection = Data.Expenses.ID + " in (";
        for (int i = 0; i < ids.size(); i++) {
            if (i != ids.size() - 1)
                selection += ids.get(i) + ", ";
            else
                selection += ids.get(i) + ")";
        }
        int rows = database.delete(Data.Expenses.TABLE_NAME, selection, null);
        if (rows == 0)
            Log.e(TAG, "No rows deleted");
    }

    public int getCategoriesCount() {
        int res = 0;
        String[] projection = new String[] { Data.Categories.ID };
        Cursor c = database.query(Data.Categories.TABLE_NAME, projection, null, null, null, null, null);
        if (c != null)
            res = c.getCount();
        return res;
    }
    
    public Cursor getCategoriesData() {
        String[] projection = new String[] { Data.Categories.ID, Data.Categories.NAME,
                Data.Categories.LIMIT,  Data.Categories.PERIOD };
        return database.query(Data.Categories.TABLE_NAME, projection, null, null, null, null, Data.Categories.NAME);
    }
    
    public ArrayList<String> getCategoriesNames() {
        ArrayList<String> categoriesNames = new ArrayList<String>();
        String[] projection = new String[] { Data.Categories.NAME };
        Cursor c = database.query(Data.Categories.TABLE_NAME, projection, null, null, null, null, Data.Categories.NAME);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    categoriesNames.add(c.getString(0));
                } while (c.moveToNext());
            }
            c.close();
        }
        return categoriesNames;
    }
    
    public float getCategoryLimit(String name) {
        float limit = 0;
        String[] projection = new String[] { Data.Categories.LIMIT };
        Cursor c = database.query(Data.Categories.TABLE_NAME, projection, Data.Categories.NAME +
                " = '" + name + "'", null, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                limit = c.getFloat(0);
            }
            c.close();
        }
        return limit;
    }
    
    public int getCategoryId(String name) {
        int id = 0;
        String[] projection = new String[] { Data.Categories.ID };
        Cursor c = database.query(Data.Categories.TABLE_NAME, projection, Data.Categories.NAME +
                " = '" + name + "'", null, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                id = c.getInt(0);
            }
            c.close();
        }
        return id;
    }
    
    public String getCategoryPeriod(String name) {
        String limit = null;
        String[] projection = new String[] { Data.Categories.PERIOD };
        Cursor c = database.query(Data.Categories.TABLE_NAME, projection, Data.Categories.NAME +
                " = '" + name + "'", null, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                limit = c.getString(0);
            }
            c.close();
        }
        return limit;
    }
    
    public void addCategory(String name, float limit, String period) {
        ContentValues values = new ContentValues();
        values.put(Data.Categories.NAME, name);
        values.put(Data.Categories.LIMIT, limit);
        values.put(Data.Categories.PERIOD, period);
        long insertId = database.insert(Data.Categories.TABLE_NAME, null, values);
        if (insertId == -1)
            Log.e(TAG, "Error occurred during inserting data (category) into DB "+name);
    }
    
    public void updateCategory(int id, String name, float limit, String period) {
        ContentValues values = new ContentValues();
        values.put(Data.Categories.NAME, name);
        values.put(Data.Categories.LIMIT, limit);
        values.put(Data.Categories.PERIOD, period);
        long insertId = database.update(Data.Categories.TABLE_NAME, values, Data.Categories.ID +
                " = " + id, null);
        if (insertId == -1)
            Log.e(TAG, "Error occurred during updating data (category) into DB "+name);
    }
    
    public void deleteCategory(List<Integer> ids) {
        String selection = Data.Categories.ID + " in (";
        for (int i = 0; i < ids.size(); i++) {
            if (i != ids.size() - 1)
                selection += ids.get(i) + ", ";
            else
                selection += ids.get(i) + ")";
        }
        int rows = database.delete(Data.Categories.TABLE_NAME, selection, null);
        if (rows == 0)
            Log.e(TAG, "No rows deleted");
    }
    
    public boolean isCurrencyListEmpty() {
        boolean res = false;
        Cursor c = database.query(Data.Currencies.TABLE_NAME, null, null, null, null, null, null);
        if (c != null) {
            res = c.getCount() == 0;
            c.close();
        }
        return res;
    }
    
    public void setCurrencyList(List<String> list) {
        ContentValues values = new ContentValues();
        for (String s: list) {
            values.put(Data.Currencies.NAME, s);
            long insertId = database.insert(Data.Currencies.TABLE_NAME, null, values);
            if (insertId == -1)
                Log.e(TAG, "Error occurred during inserting data (currencies) into DB "+s);
        }
    }
    
    public List<CharSequence> getCurrencyList() {
        List<CharSequence> list = new ArrayList<CharSequence>();
        String[] projection = new String[] { Data.Currencies.NAME };
        Cursor c = database.query(Data.Currencies.TABLE_NAME, projection, null, null, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    list.add(c.getString(c.getColumnIndex(Data.Currencies.NAME)));
                } while (c.moveToNext());
            }
            c.close();
        }
        return list;
    }

    public void setLimit(List<Integer> ids, int limit) {
        String selection = Data.Categories.ID + " in (";
        for (int i = 0; i < ids.size(); i++) {
            if (i != ids.size() - 1)
                selection += ids.get(i) + ", ";
            else
                selection += ids.get(i) + ")";
        }
        ContentValues cv = new ContentValues();
        cv.put(Data.Categories.LIMIT, limit);
        int rows = database.update(Data.Categories.TABLE_NAME, cv, selection, null);
        if (rows == 0)
            Log.e(TAG, "No rows updated");
    }
}
