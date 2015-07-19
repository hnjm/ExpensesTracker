package com.amilabs.android.expensestracker.fragments.adapters;

import com.amilabs.android.expensestracker.R;
import com.amilabs.android.expensestracker.database.Data;
import com.amilabs.android.expensestracker.database.DatabaseHandler;
import com.amilabs.android.expensestracker.utils.SharedPref;
import com.amilabs.android.expensestracker.utils.Utils;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class ExpensesListAdapter extends CursorAdapter {

    private static final String TAG = "ExpensesListAdapter";
    
    private Context mContext;
    private DatabaseHandler mDb;
    private int mPeriod;
    public List<ExpenseItem> mItems = new ArrayList<ExpenseItem>();
    private boolean mIsCheckable;
    private boolean mIsSelectAll;

    public ExpensesListAdapter(Context context, Cursor c, int period) {
        super(context, c, 0);
        mContext = context;
        mPeriod = period;
        mDb = DatabaseHandler.getInstance(context);
        rebuildItemsList();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.fragment_expenses_details_list_item, parent, false);
        ExpensesViewHolder.setTag(v);
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ExpensesViewHolder holder = (ExpensesViewHolder) view.getTag();

        holder.tvExpenseDate.setText(Utils.formatter.format(new Date(cursor.getLong(cursor.getColumnIndex(Data.Expenses.DATE)))).toString());
        holder.tvExpenseValue.setText(Utils.getFormatted(cursor.getFloat(cursor.getColumnIndex(Data.Expenses.EXPENSE)))
                + " " + SharedPref.getCurrency(context));
        if (holder.tvExpenseCategory != null)
            holder.tvExpenseCategory.setText(cursor.getString(cursor.getColumnIndex(Data.Categories.NAME)));
        
        //Log.d("ExpensesDetailsFragment", "bindView: pos="+cursor.getPosition() + " size="+mItems.size()+" mPeriod="+mPeriod);
        boolean isChecked = mItems.get(cursor.getPosition()).isChecked;
        if (mIsCheckable) {
            holder.ivArrow.setVisibility(View.GONE);
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(mIsSelectAll || isChecked);
        } else {
            holder.ivArrow.setVisibility(View.VISIBLE);
            holder.checkBox.setVisibility(View.INVISIBLE);
            holder.checkBox.setChecked(false);
        }
        ((RelativeLayout)holder.tvExpenseDate.getParent()).setBackgroundResource(
                cursor.getPosition() % 2 == 0 ? R.drawable.list_item_selector1 : R.drawable.list_item_selector2);
    }
    
    public void rebuildItemsList() {
        //Log.d("ExpensesDetailsFragment", "rebuildItemsList start: size="+mItems.size());
        mItems.clear();
        Cursor c = null;
        try {
            c = mDb.getExpensesData(mPeriod);
            if (c != null) {
                if (c.moveToFirst()) {
                    do {
                        int id = c.getInt(c.getColumnIndex(Data.Expenses.ID));
                        float expense = c.getInt(c.getColumnIndex(Data.Expenses.EXPENSE));
                        mItems.add(new ExpenseItem(id, expense, false));
                    } while (c.moveToNext());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in rebuildItemsList.", e);
        } finally {
            if (c != null)
                c.close();
        }
        //Log.d("ExpensesDetailsFragment", "rebuildItemsList end: size="+mItems.size());
    }
    
    public void setCheckable(boolean flag) {
        mIsCheckable = flag;
    }
    
    public boolean isSelectAll() {
        return mIsSelectAll;
    }

    public void setAllItemsChecked(boolean status) {
        mIsSelectAll = status;
        Iterator<ExpenseItem> it = mItems.iterator();
        while (it.hasNext()) {
            ExpenseItem item = it.next();
            item.isChecked = status;
        }
    }
    
    public List<Integer> getItemsCheckedIds() {
        List<Integer> checkedItems = new ArrayList<Integer>();
        Iterator<ExpenseItem> it = mItems.iterator();
        while (it.hasNext()) {
            ExpenseItem item = it.next();
            if (item.isChecked)
                checkedItems.add(item.id);
        }
        return checkedItems;
    }
    
    public void setItemChecked(int position, boolean value) {
        ExpenseItem item = mItems.get(position);
        item.isChecked = value;
        mItems.set(position, item);
        checkAndSetSelectAll();
    }
    
    private void checkAndSetSelectAll() {
        mIsSelectAll = isAllSelected();
    }
    
    public boolean isAllSelected() {
        Iterator<ExpenseItem> it = mItems.iterator();
        while (it.hasNext()) {
            ExpenseItem item = it.next();
            if (!item.isChecked)
                return false;
        }
        return true;
    }
    
    public boolean isNoneSelected() {
        Iterator<ExpenseItem> it = mItems.iterator();
        while (it.hasNext()) {
            ExpenseItem item = it.next();
            if (item.isChecked)
                return false;
        }
        return true;
    }

    public boolean[] getCheckedArray() {
        boolean[] arr = new boolean[mItems.size()];
        int i = 0;
        Iterator<ExpenseItem> it = mItems.iterator();
        while (it.hasNext()) {
            ExpenseItem item = it.next();
            arr[i++] = item.isChecked;
        }
        return arr;
    }
    
    public void setCheckedItemsArray(boolean[] arr) {
        int i = 0;
        Iterator<ExpenseItem> it = mItems.iterator();
        while (it.hasNext()) {
            ExpenseItem item = it.next();
            item.isChecked = arr[i++];
        }
    }
    
    public static class ExpensesViewHolder {
        
        private CheckBox checkBox;
        private TextView tvExpenseDate, tvExpenseValue, tvExpenseCategory;
        private ImageView ivArrow;

        public ExpensesViewHolder(View v) {
            tvExpenseDate = (TextView) v.findViewById(R.id.entry_date);
            tvExpenseValue = (TextView) v.findViewById(R.id.entry_value);
            tvExpenseCategory = (TextView) v.findViewById(R.id.entry_category);
            ivArrow = (ImageView) v.findViewById(R.id.entry_icon);
            checkBox = (CheckBox) v.findViewById(R.id.entry_check);
        }

        static void setTag(View v) {
            ExpensesViewHolder tag = new ExpensesViewHolder(v);
            v.setTag(tag);
        }

        public CheckBox getCheckBox() {
            return checkBox;
        }
    }
    
    public static class ExpenseItem {
        
        public int id;
        public float expense;
        private boolean isChecked;
        
        public ExpenseItem(int id, float expense, boolean isChecked) {
            this.id = id;
            this.expense = expense;
            this.isChecked = isChecked;
        }
    }

}
