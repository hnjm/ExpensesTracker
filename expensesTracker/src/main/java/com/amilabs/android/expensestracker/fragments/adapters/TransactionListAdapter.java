package com.amilabs.android.expensestracker.fragments.adapters;

import com.amilabs.android.expensestracker.R;
import com.amilabs.android.expensestracker.database.Data;
import com.amilabs.android.expensestracker.utils.SharedPref;
import com.amilabs.android.expensestracker.utils.Utils;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Date;

public class TransactionListAdapter extends SimpleCursorAdapter {
    
    private Context context;
    private Cursor dataCursor;

    public TransactionListAdapter(Context context, Cursor c, String[] from, int[] to) {
        super(context, R.layout.fragment_transaction_list_item, c, from, to, 0);
        this.context = context;
        dataCursor = c;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final TransactionViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.fragment_transaction_list_item, parent, false);
            TextView tvDate = (TextView) convertView.findViewById(R.id.entry_date);
            TextView tvDetails = (TextView) convertView.findViewById(R.id.entry_details);
            TextView tvExpense = (TextView) convertView.findViewById(R.id.entry_value);
            
            holder = new TransactionViewHolder(tvDate, tvDetails, tvExpense);

            convertView.setTag(holder);
        } else
            holder = (TransactionViewHolder) convertView.getTag();

        if (dataCursor.moveToPosition(position)) {
            holder.tvDate.setText(Utils.formatter.format(new Date(dataCursor.getLong(dataCursor.getColumnIndex(Data.Expenses.DATE)))));
            holder.tvDetails.setText(dataCursor.getString(dataCursor.getColumnIndex(Data.Expenses.DETAILS)));
            holder.tvExpense.setText(Utils.getFormatted(Double.parseDouble(dataCursor.getString(dataCursor.getColumnIndex(Data.Expenses.EXPENSE)))) 
                    + " " + SharedPref.getCurrency(context));
        }
        ((RelativeLayout)holder.tvDate.getParent()).setBackgroundColor(
                context.getResources().getColor(position % 2 == 0 ? R.color.row_color : R.color.bg_list_color));

        return convertView;
    }
    
    public static class TransactionViewHolder {
        
        private TextView tvDate, tvDetails, tvExpense;

        public TransactionViewHolder(TextView tv1, TextView tv2, TextView tv3) {
            tvDate = tv1;
            tvDetails = tv2;
            tvExpense = tv3;
        }
    }

}
