package com.amilabs.android.expensestracker.fragments.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

public class CurrencyAdapter<CharSequence> extends ArrayAdapter<CharSequence> {

    private List<CharSequence> items;

    public CurrencyAdapter(Context context, int textViewResourceId, ArrayList<CharSequence> items) {
        super(context, textViewResourceId, items);
    }

    public void setItems(List<CharSequence> list) {
        items = list;
        addAll(items);
    }

    @Override
    public int getPosition(CharSequence prefix) {
        for (CharSequence item: items) {
            if (item.toString().startsWith(prefix.toString()))
                return super.getPosition(item);
        }
        return -1;
    }
}
