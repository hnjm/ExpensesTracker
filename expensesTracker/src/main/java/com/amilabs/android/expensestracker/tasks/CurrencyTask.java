package com.amilabs.android.expensestracker.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.widget.Spinner;

import com.amilabs.android.expensestracker.R;
import com.amilabs.android.expensestracker.database.DatabaseHandler;
import com.amilabs.android.expensestracker.fragments.adapters.CurrencyAdapter;
import com.amilabs.android.expensestracker.utils.SharedPref;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class CurrencyTask extends AsyncTask<Void, Void, List<CharSequence>> {

    private static final String TAG = "CurrencyTask";

    private Context context;
    private Spinner spinner;
    private CoordinatorLayout coordinatorLayoutView;
    private CurrencyAdapter<CharSequence> adapter;
    private final ProgressDialog dialog;

    public CurrencyTask(Context context, Spinner spinner, CoordinatorLayout coordinatorLayoutView, CurrencyAdapter<CharSequence> adapter) {
        this.context = context;
        this.spinner = spinner;
        this.coordinatorLayoutView = coordinatorLayoutView;
        this.adapter = adapter;
        dialog = new ProgressDialog(context);
    }

    @Override
    protected void onPreExecute() {
        dialog.setMessage("Retrieving available currency list...");
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    protected List<CharSequence> doInBackground(final Void... args) {
        List<CharSequence> retList = new ArrayList<CharSequence>();
        if (DatabaseHandler.getInstance(context).isCurrencyListEmpty()) {
            Map<String, String> map = new TreeMap<>();
            Locale[] locs = Locale.getAvailableLocales();
            for (Locale loc: locs) {
                try {
                    Currency currency = Currency.getInstance(loc);
                    String curr = currency.toString();
                    String symbol = currency.getSymbol(loc);
                    String description = ", " + symbol;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        String name = currency.getDisplayName(loc);
                        if (curr.equals("USD"))
                            name = "US dollar";
                        description = ", " + symbol + " (" + name + ")";
                    }
                    if (!map.containsKey(curr))
                        map.put(curr, description);
                } catch(Exception e) {
                }
            }
            for (Map.Entry<String, String> entry: map.entrySet())
                retList.add(entry.getKey() + entry.getValue());
            DatabaseHandler.getInstance(context).setCurrencyList(retList);
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
            Snackbar.make(coordinatorLayoutView, R.string.currency_failed, Snackbar.LENGTH_LONG).show();
        else {
            adapter.setItems(currencyList);
            spinner.setSelection(adapter.getPosition(SharedPref.getCurrency(context)), false);
        }
    }

}
