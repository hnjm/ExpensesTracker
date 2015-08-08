package com.amilabs.android.expensestracker.fragments;

import com.amilabs.android.expensestracker.R;
import com.amilabs.android.expensestracker.fragments.adapters.PlannerListAdapter;
import com.amilabs.android.expensestracker.database.DatabaseHandler;
import com.amilabs.android.expensestracker.interfaces.Constants;
import com.amilabs.android.expensestracker.interfaces.OnDateSelectedListener;
import com.amilabs.android.expensestracker.interfaces.OnUpdateFragmentInterface;
import com.amilabs.android.expensestracker.utils.SharedPref;
import com.amilabs.android.expensestracker.utils.Utils;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class PlannerFragment extends Fragment implements LoaderCallbacks<Cursor>,
	OnUpdateFragmentInterface, OnDateSelectedListener, Constants {

    private static final String TAG = "PlannerFragment";

    private static DatabaseHandler mDb;
    private AppCompatActivity mContext;
    private PlannerListAdapter mAdapter;
    private final static int mLoaderId = -2;

    private TransactionsFragment mTransactionsFragment;
    private CoordinatorLayout mCoordinatorLayout;
    private ListView mListView;
    private RelativeLayout mStartPeriodLayout;
    private RelativeLayout mFinishPeriodLayout;
    private TextView mEmptyView;
    private TextView mTvStartDate, mTvFinishDate;
    // data for supporting orientation change
    private boolean mIsDateDialogShown;
    private boolean mIsTransactionsFragmentShown;
    private static boolean mIsFromBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        final View rootView = inflater.inflate(R.layout.fragment_planner, container, false);
        mCoordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.coordinator_layout);
        mListView = (ListView) rootView.findViewById(android.R.id.list);
        mEmptyView = (TextView) rootView.findViewById(R.id.empty_view);
        mStartPeriodLayout = (RelativeLayout) rootView.findViewById(R.id.start_period_layout);
        mFinishPeriodLayout = (RelativeLayout) rootView.findViewById(R.id.finish_period_layout);
        mTvStartDate = (TextView) rootView.findViewById(R.id.tv_start_period);
        mTvFinishDate = (TextView) rootView.findViewById(R.id.tv_finish_period);

        mContext = (AppCompatActivity) getActivity();
        mDb = DatabaseHandler.getInstance(mContext);

        mAdapter = new PlannerListAdapter(mContext, mDb.getSpentOnCategories(mContext, TrackerType.PLANNER));
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListView.setVisibility(View.GONE);
                mStartPeriodLayout.setVisibility(View.GONE);
                mFinishPeriodLayout.setVisibility(View.GONE);
                mTransactionsFragment = new TransactionsFragment();
                Bundle args = new Bundle();
                args.putBoolean("isFromDistribution", false);
                args.putString(TAG_FRAGMENT_CATEGORY, mAdapter.getCategory(position));
                mTransactionsFragment.setArguments(args);
                mContext.getSupportFragmentManager().beginTransaction().replace(R.id.coordinator_layout,
                		mTransactionsFragment, TAG_FRAGMENT_TRANSACTIONS).addToBackStack(TAG_FRAGMENT_PLANNER).commit();
                mIsTransactionsFragmentShown = true;
            }
        });

        initLoader();
        initPeriods();
        updateView();
        restoreView(savedInstanceState);
        setHasOptionsMenu(true);

        return rootView;
    }

    private void initPeriods() {
        long dateFrom = SharedPref.getDateFrom(mContext, TrackerType.PLANNER);
        if (dateFrom == 0) {
            dateFrom = System.currentTimeMillis() - DatabaseHandler.WEEK_IN_MSEC;
            SharedPref.saveDateFrom(mContext, dateFrom, TrackerType.PLANNER);
        }
        long dateTo = SharedPref.getDateTo(mContext, TrackerType.PLANNER);
        if (dateTo == 0) {
            dateTo = System.currentTimeMillis();
            SharedPref.saveDateTo(mContext, dateTo, TrackerType.PLANNER);
        }
        mTvStartDate.setText(Utils.getStringDate(dateFrom));
        mTvFinishDate.setText(Utils.getStringDate(dateTo));
        mStartPeriodLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsFromBtn = true;
                showDateDialog(Utils.getStringDate(SharedPref.getDateFrom(mContext, TrackerType.PLANNER)));
            }
        });
        mFinishPeriodLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsFromBtn = false;
                showDateDialog(Utils.getStringDate(SharedPref.getDateTo(mContext, TrackerType.PLANNER)));
            }
        });
    }

    private void initLoader() {
        if (mContext.getSupportLoaderManager().getLoader(mLoaderId) == null)
            mContext.getSupportLoaderManager().initLoader(mLoaderId, null, this);
        else
            mContext.getSupportLoaderManager().restartLoader(mLoaderId, null, this);
    }

    private void updateView() {
        if (isAdded()) {
            if (mDb.getSpentOnCategories(mContext, TrackerType.PLANNER).getCount() == 0) {
                mListView.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.VISIBLE);
            } else {
                mListView.setVisibility(View.VISIBLE);
                mEmptyView.setVisibility(View.GONE);
            }
            mStartPeriodLayout.setVisibility(View.VISIBLE);
            mFinishPeriodLayout.setVisibility(View.VISIBLE);
            mContext.setTitle(getString(R.string.planner));
        }
    }

    private void restoreView(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mIsTransactionsFragmentShown = savedInstanceState.getBoolean("is_transactions_fragment_shown");
            if (mIsTransactionsFragmentShown) {
                mListView.setVisibility(View.GONE);
                mStartPeriodLayout.setVisibility(View.GONE);
                mFinishPeriodLayout.setVisibility(View.GONE);
            } else {
                mIsFromBtn = savedInstanceState.getBoolean("is_from_btn");
                mIsDateDialogShown = savedInstanceState.getBoolean("is_date_dialog_shown");
                if (mIsDateDialogShown)
                    showDateDialog(mIsFromBtn ? Utils.getStringDate(
                            SharedPref.getDateFrom(mContext, TrackerType.PLANNER)) :
                            Utils.getStringDate(SharedPref.getDateTo(mContext, TrackerType.PLANNER)));
            }
        }
    }

    private void showDateDialog(String dateToString) {
        DialogFragment dialogFragment = new DatePickerFragment();
        Bundle bundle = new Bundle();
        bundle.putString("date", dateToString);
        bundle.putString("trackerType", "PLANNER");
        bundle.putBoolean("isFromBtn", mIsFromBtn);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(mContext.getSupportFragmentManager(), "datePicker");
        mIsDateDialogShown = true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState [" + mLoaderId + "], mIsTransactionsFragmentShown = " + mIsTransactionsFragmentShown);
        outState.putBoolean("is_from_btn", mIsFromBtn);
        outState.putBoolean("is_date_dialog_shown", mIsDateDialogShown);
        outState.putBoolean("is_transactions_fragment_shown", mIsTransactionsFragmentShown);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.planner_options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help_planner:
                HelpDialogFragment.newInstance(getString(R.string.planner), getString(R.string.help_planner))
                        .show(getFragmentManager(), HelpDialogFragment.TAG);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDateSelected(long date) {
        if (mIsFromBtn) {
            mTvStartDate.setText(Utils.getStringDate(date));
            SharedPref.saveDateFrom(mContext, date, TrackerType.PLANNER);
        } else {
            mTvFinishDate.setText(Utils.getStringDate(date));
            SharedPref.saveDateTo(mContext, date, TrackerType.PLANNER);
        }
        mContext.getSupportLoaderManager().restartLoader(mLoaderId, null, this);
        updateView();
        mIsDateDialogShown = false;
    }

    @Override
    public void onDialogDestroyed(String error) {
        Log.d(TAG, "onDialogDestroyed [" + mLoaderId + "], mIsDateDialogShown = false");
        mIsDateDialogShown = false;
        if (error != null)
            Snackbar.make(mCoordinatorLayout, error, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onUpdateFragment() {
        updateView();
        mIsTransactionsFragmentShown = false;
        Log.d(TAG, "onUpdateFragment [" + mLoaderId + "], mIsTransactionsFragmentShown = false");
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView");
        if (mTransactionsFragment != null && mTransactionsFragment.isAdded()) {
            try {
                FragmentManager fm = mContext.getSupportFragmentManager();
                for (int i = 0; i < fm.getBackStackEntryCount(); ++i)
                    mContext.getSupportFragmentManager().popBackStack();
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.remove(mTransactionsFragment).commit();
            } catch (Exception e) {
            }
        }
        super.onDestroyView();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new PlannerDataLoader(mContext, PlannerFragment.this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }

    private static class PlannerDataLoader extends CursorLoader {

        private PlannerFragment fragment;

        public PlannerDataLoader(Context context, PlannerFragment instance) {
            super(context);
            fragment = instance;
        }

        @Override
        public Cursor loadInBackground() {
            return mDb.getSpentOnCategories(fragment.mContext, TrackerType.PLANNER);
        }
    }

}
