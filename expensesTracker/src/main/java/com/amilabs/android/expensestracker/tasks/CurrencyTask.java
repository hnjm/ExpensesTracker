package com.amilabs.android.expensestracker.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.amilabs.android.expensestracker.R;
import com.amilabs.android.expensestracker.database.DatabaseHandler;
import com.amilabs.android.expensestracker.utils.SharedPref;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

public class CurrencyTask extends AsyncTask<Void, Void, List<CharSequence>> {

    private static final String TAG = "CurrencyTask";

    private Context context;
    private Spinner spinner;
    private ArrayAdapter<CharSequence> adapter;
    private static DatabaseHandler db;
    private final ProgressDialog dialog;

    public CurrencyTask(Context context, Spinner spinner, ArrayAdapter<CharSequence> adapter) {
        this.context = context;
        this.spinner = spinner;
        this.adapter = adapter;
        db = DatabaseHandler.getInstance(context);
        dialog = new ProgressDialog(context);
    }

    @Override
    protected void onPreExecute() {
        this.dialog.setMessage("Retrieving available currency list...");
        this.dialog.show();
    }

    @Override
    protected List<CharSequence> doInBackground(final Void... args) {
        List<CharSequence> retList = new ArrayList<CharSequence>();
        if (DatabaseHandler.getInstance(context).isCurrencyListEmpty()) {
            List<String> list = new ArrayList<String>();
            Locale[] locs = Locale.getAvailableLocales();
            for (Locale loc: locs) {
                try {
                    String currency = Currency.getInstance(loc).toString();
                    if (!list.contains(currency))
                        list.add(currency);
                } catch(Exception e) {
                }
            }
            Collections.sort(list);
            DatabaseHandler.getInstance(context).setCurrencyList(list);
            for (String s: list)
                retList.add(s);
        } else {
            retList = DatabaseHandler.getInstance(context).getCurrencyList();
        }
        return retList;
    }

    @Override
    protected void onPostExecute(final List<CharSequence> currencyList) {
        if (dialog.isShowing())
            dialog.dismiss();
        if (currencyList == null || currencyList.size() == 0)
            Toast.makeText(context, R.string.currency_failed, Toast.LENGTH_SHORT).show();
        else {
            adapter.addAll(currencyList);
            spinner.setSelection(adapter.getPosition(SharedPref.getCurrency(context)), false);
        }
    }

}
