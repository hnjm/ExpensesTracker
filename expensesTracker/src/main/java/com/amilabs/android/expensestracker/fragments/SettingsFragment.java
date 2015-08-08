package com.amilabs.android.expensestracker.fragments;

import com.amilabs.android.expensestracker.R;
import com.amilabs.android.expensestracker.database.DatabaseHandler;
import com.amilabs.android.expensestracker.fragments.adapters.CurrencyAdapter;
import com.amilabs.android.expensestracker.interfaces.Constants;
import com.amilabs.android.expensestracker.tasks.CurrencyTask;
import com.amilabs.android.expensestracker.tasks.ExchangeRateTask;
import com.amilabs.android.expensestracker.tasks.ExportDBToXLSTask;
import com.amilabs.android.expensestracker.utils.SharedPref;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment implements Constants {

    private AppCompatActivity mContext;
    private static DatabaseHandler mDb;
    private List<CheckBox> mPeriods = new ArrayList<CheckBox>();
    private boolean isFirst = true;
    //private EditText etPhoneNumber;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = (AppCompatActivity) getActivity();
        mDb = DatabaseHandler.getInstance(mContext);
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.coordinator_layout);

        // time ranges
        mPeriods.add((CheckBox) rootView.findViewById(R.id.cb_1week));
        mPeriods.add((CheckBox) rootView.findViewById(R.id.cb_2weeks));
        mPeriods.add((CheckBox) rootView.findViewById(R.id.cb_3weeks));
        mPeriods.add((CheckBox) rootView.findViewById(R.id.cb_1month));
        mPeriods.add((CheckBox) rootView.findViewById(R.id.cb_2months));
        mPeriods.add((CheckBox) rootView.findViewById(R.id.cb_3months));
        mPeriods.add((CheckBox) rootView.findViewById(R.id.cb_6months));
        mPeriods.add((CheckBox) rootView.findViewById(R.id.cb_1year));
        mPeriods.add((CheckBox) rootView.findViewById(R.id.cb_2years));
        boolean[] periods = SharedPref.getPeriods(mContext);
        for (int i = 0; i < periods.length; i++)
            mPeriods.get(i).setChecked(periods[i]);

        // currency spinner
        final Spinner spinnerCurrency = (Spinner) rootView.findViewById(R.id.spinner_currency);
        final CurrencyAdapter<CharSequence> adapter = new CurrencyAdapter<CharSequence>(mContext,
                R.layout.spinner, new ArrayList<CharSequence>());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(adapter);
        spinnerCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (isFirst) {
                    isFirst = false;
                    return;
                }
                ExchangeRateTask exchangeRate = new ExchangeRateTask(mContext, spinnerCurrency,
                        coordinatorLayout, adapter.getPosition(SharedPref.getCurrency(mContext)));
                exchangeRate.execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        // fill the currency spinner
        CurrencyTask currencyTask = new CurrencyTask(mContext, spinnerCurrency, coordinatorLayout, adapter);
        currencyTask.execute();

        // export to CSV
        final Spinner spinnerPeriod = (Spinner) rootView.findViewById(R.id.spinner_time_range);
        ArrayAdapter<String> adapterPeriod = new ArrayAdapter<String>(mContext, R.layout.spinner, getResources().getStringArray(R.array.period));
        adapterPeriod.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPeriod.setAdapter(adapterPeriod);
        Button btnExport = (Button) rootView.findViewById(R.id.btn_export);
        btnExport.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int days = 0;
                String period = spinnerPeriod.getSelectedItem().toString();
                if (period.equals(WEEK))
                    days = 7;
                else if (period.equals(MONTH))
                    days = 31;
                else if (period.equals(YEAR))
                    days = 365;
                ExportDBToXLSTask exportTask = new ExportDBToXLSTask(mContext, coordinatorLayout);
                exportTask.execute(days);
            }
        });
        /*if (!SharedPref.isPremium(mContext)) {
            TextView tv = (TextView) rootView.findViewById(R.id.tv_export);
            tv.setText(getResources().getString(R.string.export_text) + 
        			" " + getResources().getString(R.string.available_in_full_version));
            tv.setEnabled(false);
            spinnerPeriod.setEnabled(false);
            btnExport.setEnabled(false);
        }*/
        /*final CheckBox cbSmsNotif = (CheckBox) rootView.findViewById(R.id.cb_enable_sms_notif);
        final TextView tvSmsNotifHelp = (TextView) rootView.findViewById(R.id.tv_enable_sms_notif);
        final TextView tvPhoneNumber = (TextView) rootView.findViewById(R.id.tv_phone_number);
        etPhoneNumber = (EditText) rootView.findViewById(R.id.et_phone_number);
        String phoneNumber = SharedPref.getPhoneNumber(mContext);
        if (phoneNumber != null)
            etPhoneNumber.setText(phoneNumber);
        boolean isSmsEnabled = SharedPref.isSmsEnabled(mContext);
        cbSmsNotif.setChecked(isSmsEnabled);
        tvSmsNotifHelp.setEnabled(isSmsEnabled);
        tvPhoneNumber.setEnabled(isSmsEnabled);
        etPhoneNumber.setEnabled(isSmsEnabled);
        cbSmsNotif.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                tvSmsNotifHelp.setEnabled(isChecked);
                tvPhoneNumber.setEnabled(isChecked);
                etPhoneNumber.setEnabled(isChecked);
                SharedPref.setSmsEnabledFlag(mContext, isChecked);
            }
         });*/
        setHasOptionsMenu(true);
        mContext.setTitle(getString(R.string.settings));

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.help_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help:
                HelpDialogFragment.newInstance(getString(R.string.settings), getString(R.string.help_settings))
                        .show(getFragmentManager(), HelpDialogFragment.TAG);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //SharedPref.setPhoneNumber(mContext, etPhoneNumber.getText().toString());
        boolean[] values = new boolean[9];
        for (int i = 0; i < values.length; i++)
            values[i] = mPeriods.get(i).isChecked();
        SharedPref.savePeriods(mContext, values);
    }

}
