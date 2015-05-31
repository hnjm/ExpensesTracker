package com.amilabs.android.expensestracker.adapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

public class CategoryListAdapter extends CursorAdapter {

    private static final String TAG = "CategoryListAdapter";
    
    private Context mContext;
    private DatabaseHandler mDb;
    public List<CategoryItem> mItems = new ArrayList<CategoryItem>();
    private boolean mIsCheckable;
    private boolean mIsSelectAll;

    public CategoryListAdapter(Context context, Cursor c) {
        super(context, c, 0);
        mContext = context;
        mDb = DatabaseHandler.getInstance(context);
        rebuildItemsList();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.fragment_category_list_item, parent, false);
        CategoryViewHolder.setTag(v);
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        CategoryViewHolder holder = (CategoryViewHolder) view.getTag();

        holder.tvCategory.setText(cursor.getString(cursor.getColumnIndex(Data.Categories.NAME)));
        holder.tvLimit.setText(Utils.getFormatted(cursor.getFloat(cursor.getColumnIndex(Data.Categories.LIMIT)))
                + " " + SharedPref.getCurrency(context));
        if (holder.tvPeriod != null)
            holder.tvPeriod.setText(cursor.getString(cursor.getColumnIndex(Data.Categories.PERIOD)));
        
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
        
        ((RelativeLayout)holder.tvCategory.getParent()).setBackgroundResource(
                cursor.getPosition() % 2 == 0 ? R.drawable.list_item_selector1 : R.drawable.list_item_selector2);
        
        /*float tvWidth = Utils.getTextViewWidthInDP(holder.tvCategory);
        float screenWidth = Utils.getScreenWidthInDP(context);
        if (screenWidth > 400) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.tvLimit.getLayoutParams();
            float left = (screenWidth - 55 - 170) / 3;
            params.setMargins(Utils.getPixels(left), Utils.getPixels(5), 0, 0);
            holder.tvLimit.setLayoutParams(params);
            Log.d(TAG, "limit left margin: "+left);
            if (holder.tvPeriod != null) {
                params = (RelativeLayout.LayoutParams) holder.tvPeriod.getLayoutParams();
                left = (screenWidth - 55 - 170) * 2 / 3;
                params.setMargins(Utils.getPixels(left), Utils.getPixels(5), 0, 0);
                holder.tvPeriod.setLayoutParams(params);
                Log.d(TAG, "period left margin: "+left);
            }
        }
        Log.d(TAG, holder.tvCategory.getText() + ", length in dp: "+ tvWidth + 
            ", dpi="+Resources.getSystem().getDisplayMetrics().density + ", screenWidth in dp: " + screenWidth);
        Log.d(TAG, ", screenWidth in dp: " + Utils.getScreenWidthInDP(context));*/
    }
    
    public void rebuildItemsList() {
        mItems.clear();
        Cursor c = null;
        try {
            c = mDb.getCategoriesData();
            if (c != null) {
                if (c.moveToFirst()) {
                    do {
                        int id = c.getInt(c.getColumnIndex(Data.Categories.ID));
                        String name = c.getString(c.getColumnIndex(Data.Categories.NAME));
                        mItems.add(new CategoryItem(id, name, false));
                    } while (c.moveToNext());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in rebuildItemsList.", e);
        } finally {
            if (c != null)
                c.close();
        }
        //Log.d(TAG, "rebuildItemsList: size="+mItems.size());
    }
    
    public void setCheckable(boolean flag) {
        mIsCheckable = flag;
    }
    
    public boolean isSelectAll() {
        return mIsSelectAll;
    }

    public void setAllItemsChecked(boolean status) {
        mIsSelectAll = status;
        Iterator<CategoryItem> it = mItems.iterator();
        while (it.hasNext()) {
            CategoryItem item = it.next();
            item.isChecked = status;
        }
    }
    
    public List<Integer> getItemsCheckedIds() {
        List<Integer> checkedItems = new ArrayList<Integer>();
        Iterator<CategoryItem> it = mItems.iterator();
        while (it.hasNext()) {
            CategoryItem item = it.next();
            if (item.isChecked)
                checkedItems.add(item.id);
        }
        return checkedItems;
    }
    
    public void setItemChecked(int position, boolean value) {
        CategoryItem item = mItems.get(position);
        item.isChecked = value;
        mItems.set(position, item);
        checkAndSetSelectAll();
    }
    
    private void checkAndSetSelectAll() {
        mIsSelectAll = isAllSelected();
    }
    
    public boolean isAllSelected() {
        Iterator<CategoryItem> it = mItems.iterator();
        while (it.hasNext()) {
            CategoryItem item = it.next();
            if (!item.isChecked)
                return false;
        }
        return true;
    }
    
    public boolean isNoneSelected() {
        Iterator<CategoryItem> it = mItems.iterator();
        while (it.hasNext()) {
            CategoryItem item = it.next();
            if (item.isChecked)
                return false;
        }
        return true;
    }

    public boolean[] getCheckedArray() {
        boolean[] arr = new boolean[mItems.size()];
        int i = 0;
        Iterator<CategoryItem> it = mItems.iterator();
        while (it.hasNext()) {
            CategoryItem item = it.next();
            arr[i++] = item.isChecked;
        }
        return arr;
    }
    
    public void setCheckedItemsArray(boolean[] arr) {
        int i = 0;
        Iterator<CategoryItem> it = mItems.iterator();
        while (it.hasNext()) {
            CategoryItem item = it.next();
            item.isChecked = arr[i++];
        }
    }
    
    public static class CategoryViewHolder {
        
        private CheckBox checkBox;
        private TextView tvCategory, tvLimit, tvPeriod;
        private ImageView ivArrow;

        public CategoryViewHolder(View v) {
            tvCategory = (TextView) v.findViewById(R.id.category_name);
            tvLimit = (TextView) v.findViewById(R.id.category_limit);
            tvPeriod = (TextView) v.findViewById(R.id.category_period);
            ivArrow = (ImageView) v.findViewById(R.id.category_icon);
            checkBox = (CheckBox) v.findViewById(R.id.category_check);
        }

        static void setTag(View v) {
            CategoryViewHolder tag = new CategoryViewHolder(v);
            v.setTag(tag);
        }

        public CheckBox getCheckBox() {
            return checkBox;
        }
    }
    
    public static class CategoryItem {
        
        public int id;
        public String name;
        private boolean isChecked;
        
        public CategoryItem(int id, String name, boolean isChecked) {
            this.id = id;
            this.name = name;
            this.isChecked = isChecked;
        }
    }

}
