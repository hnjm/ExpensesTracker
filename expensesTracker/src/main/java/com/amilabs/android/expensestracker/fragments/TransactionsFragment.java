package com.amilabs.android.expensestracker.fragments;

import java.util.ArrayList;

import com.amilabs.android.expensestracker.R;
import com.amilabs.android.expensestracker.fragments.adapters.TransactionListAdapter;
import com.amilabs.android.expensestracker.database.Data;
import com.amilabs.android.expensestracker.database.DatabaseHandler;
import com.amilabs.android.expensestracker.interfaces.Constants;
import com.amilabs.android.expensestracker.interfaces.OnUpdateFragmentInterface;
import com.amilabs.android.expensestracker.utils.SharedPref;
import com.amilabs.android.expensestracker.utils.Utils;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class TransactionsFragment extends Fragment implements Constants {

    private static final String TAG = "TransactionsFragment";
    
    private static DatabaseHandler mDb;
    private AppCompatActivity mContext;
    OnUpdateFragmentInterface mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_transactions, container, false);
        ListView mListView = (ListView) rootView.findViewById(android.R.id.list);
        TextView mTvTotalView = (TextView) rootView.findViewById(R.id.tv_total);
        
        Bundle args = getArguments();
        String category = args.getString("category");
        boolean isOtherCategories = args.getBoolean("is_other");
        boolean isFromDistribution = args.getBoolean("isFromDistribution");
        ArrayList<String> otherCategories = args.getStringArrayList("other_categories");
        
        mContext = (AppCompatActivity) getActivity();
        mDb = DatabaseHandler.getInstance(mContext);
        
        long dateFrom = SharedPref.getDateFrom(mContext, isFromDistribution ? TrackerType.DISTRIBUTION : TrackerType.PLANNER);
        dateFrom = Utils.getFreshDate(mContext, dateFrom);
        long dateTo = SharedPref.getDateTo(mContext, isFromDistribution ? TrackerType.DISTRIBUTION : TrackerType.PLANNER);
        dateTo = Utils.getFreshDate(mContext, dateTo);
        Cursor c = null;
        if (isOtherCategories && otherCategories != null && otherCategories.size() > 0)
        	c = mDb.getExpensesByCategories(otherCategories, dateFrom, dateTo);
        else
        	c = mDb.getExpensesByCategory(category, dateFrom, dateTo);
        //Log.d(TAG, "onCreate: count = " + c.getCount() + " category="+category+" dateFrom="+Utils.getStringDate(dateFrom)+" dateTo="+Utils.getStringDate(dateTo));
        TransactionListAdapter adapter = new TransactionListAdapter(mContext, c, 
                new String[] { Data.Expenses.DATE, Data.Expenses.DETAILS, Data.Expenses.EXPENSE }, 
                new int[] { R.id.entry_date, R.id.entry_details, R.id.entry_value });
        mListView.setAdapter(adapter);

        mTvTotalView.setText(getString(R.string.total) + ": " + 
                Utils.getFormatted(mDb.getTotalExpensesByCategory(category, dateFrom, dateTo)) + " " + SharedPref.getCurrency(mContext));
        mContext.setTitle(category);
        setHasOptionsMenu(true);
        
        return rootView;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.transactions_options, menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help_transactions:
                HelpDialogFragment.newInstance(getString(R.string.transactions), getString(R.string.help_transactions))
                        .show(getFragmentManager(), HelpDialogFragment.TAG);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnUpdateFragmentInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnUpdateFragmentInterface");
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mListener != null)
            mListener.onUpdateFragment();
    }
}
