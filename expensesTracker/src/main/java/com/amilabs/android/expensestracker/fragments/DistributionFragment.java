package com.amilabs.android.expensestracker.fragments;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
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
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

import com.amilabs.android.expensestracker.R;
import com.amilabs.android.expensestracker.fragments.adapters.DistributionListAdapter;
import com.amilabs.android.expensestracker.fragments.adapters.DistributionListRowItem;
import com.amilabs.android.expensestracker.database.Data;
import com.amilabs.android.expensestracker.database.DatabaseHandler;
import com.amilabs.android.expensestracker.interfaces.Constants;
import com.amilabs.android.expensestracker.interfaces.OnDateSelectedListener;
import com.amilabs.android.expensestracker.interfaces.OnUpdateFragmentInterface;
import com.amilabs.android.expensestracker.utils.SharedPref;
import com.amilabs.android.expensestracker.utils.Utils;
import com.echo.holographlibrary.PieGraph;
import com.echo.holographlibrary.PieSlice;
import com.echo.holographlibrary.PieGraph.OnSliceClickedListener;

public class DistributionFragment extends Fragment implements LoaderCallbacks<List<DistributionListRowItem>>,
	OnUpdateFragmentInterface, OnDateSelectedListener, Constants {

    private static final String TAG = "DistributionFragment";
    
    private static DatabaseHandler mDb;
    private AppCompatActivity mContext;
    private DistributionListAdapter mAdapter;
    private final static int mLoaderId = -3; 
    
    private static ArrayList<String> mOtherCategories = new ArrayList<String>();
    private TransactionsFragment mTransactionsFragment;
    private ListView mListView;
    private RelativeLayout mStartPeriodLayout;
    private RelativeLayout mFinishPeriodLayout;
    private TextView mEmptyView;
    private TextView mTvStartDate, mTvFinishDate;
    private PieGraph mPieGraph;
    // data for supporting orientation change
    private boolean mIsDateDialogShown;
    private boolean mIsTransactionsFragmentShown;
    private static boolean mIsFromBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_distribution, container, false);
        mListView = (ListView) rootView.findViewById(android.R.id.list);
        mEmptyView = (TextView) rootView.findViewById(R.id.empty_view);
        mStartPeriodLayout = (RelativeLayout) rootView.findViewById(R.id.start_period_layout);
        mFinishPeriodLayout = (RelativeLayout) rootView.findViewById(R.id.finish_period_layout);
        mTvStartDate = (TextView) rootView.findViewById(R.id.tv_start_period);
        mTvFinishDate = (TextView) rootView.findViewById(R.id.tv_finish_period);
        mPieGraph = (PieGraph) rootView.findViewById(R.id.piegraph);
        
        mContext = (AppCompatActivity) getActivity();
        mDb = DatabaseHandler.getInstance(mContext);

        mAdapter = new DistributionListAdapter(mContext);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	switchToTransactionsFragment(position);
            }
        });

        initPeriods();
        updateView();
        setHasOptionsMenu(true);

        if (mContext.getSupportLoaderManager().getLoader(mLoaderId) == null)
            mContext.getSupportLoaderManager().initLoader(mLoaderId, null, this);
        else
            mContext.getSupportLoaderManager().restartLoader(mLoaderId, null, this);

        if (savedInstanceState != null) {
            mIsTransactionsFragmentShown = savedInstanceState.getBoolean("is_transactions_fragment_shown");
            //Log.d(TAG, "onCreateView: restore [" + mLoaderId + "], mIsTransactionsFragmentShown = " + mIsTransactionsFragmentShown);
            if (mIsTransactionsFragmentShown) {
                mListView.setVisibility(View.GONE);
                mStartPeriodLayout.setVisibility(View.GONE);
                mFinishPeriodLayout.setVisibility(View.GONE);
            } else {
                mIsFromBtn = savedInstanceState.getBoolean("is_from_btn");
                mIsDateDialogShown = savedInstanceState.getBoolean("is_date_dialog_shown");
                if (mIsDateDialogShown)
                    showDateDialog(mIsFromBtn ? Utils.getStringDate(SharedPref.getDateFrom(mContext, TrackerType.DISTRIBUTION)) :
                            Utils.getStringDate(SharedPref.getDateTo(mContext, TrackerType.DISTRIBUTION)));
            }
        }
        return rootView;
    }

    private void initPeriods() {
        long dateFrom = SharedPref.getDateFrom(mContext, TrackerType.DISTRIBUTION);
        if (dateFrom == 0) {
            dateFrom = System.currentTimeMillis() - DatabaseHandler.WEEK_IN_MSEC;
            SharedPref.saveDateFrom(mContext, dateFrom, TrackerType.DISTRIBUTION);
        }
        long dateTo = SharedPref.getDateTo(mContext, TrackerType.DISTRIBUTION);
        if (dateTo == 0) {
            dateTo = System.currentTimeMillis();
            SharedPref.saveDateTo(mContext, dateTo, TrackerType.DISTRIBUTION);
        }
        mTvStartDate.setText(Utils.getStringDate(dateFrom));
        mTvFinishDate.setText(Utils.getStringDate(dateTo));
        mStartPeriodLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsFromBtn = true;
                showDateDialog(Utils.getStringDate(SharedPref.getDateFrom(mContext, TrackerType.DISTRIBUTION)));
            }
        });
        mFinishPeriodLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsFromBtn = false;
                showDateDialog(Utils.getStringDate(SharedPref.getDateTo(mContext, TrackerType.DISTRIBUTION)));
            }
        });
    }
    
    private void updateView() {
        if (isAdded()) {
            if (isFetchedDataEmpty()) {
            	mPieGraph.setVisibility(View.GONE);
                mListView.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.VISIBLE);
            } else {
            	mPieGraph.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.VISIBLE);
                mEmptyView.setVisibility(View.GONE);
            }
            mStartPeriodLayout.setVisibility(View.VISIBLE);
            mFinishPeriodLayout.setVisibility(View.VISIBLE);
            mContext.setTitle(getString(R.string.distribution));
        }
    }
    
    private boolean isFetchedDataEmpty() {
    	return mDb.getSpentOnCategories(mContext, TrackerType.DISTRIBUTION).getCount() == 0;
    }

    private void showDateDialog(String dateToString) {
        DialogFragment dialogFragment = new DatePickerFragment();
        Bundle bundle = new Bundle();
        bundle.putString("date", dateToString);
        bundle.putString("trackerType", "DISTRIBUTION");
        bundle.putBoolean("isFromBtn", mIsFromBtn);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(mContext.getSupportFragmentManager(), "datePicker");
        mIsDateDialogShown = true;
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Log.d(TAG, "onSaveInstanceState [" + mLoaderId + "], mIsTransactionsFragmentShown = " + mIsTransactionsFragmentShown);
        outState.putBoolean("is_from_btn", mIsFromBtn);
        outState.putBoolean("is_date_dialog_shown", mIsDateDialogShown);
        outState.putBoolean("is_transactions_fragment_shown", mIsTransactionsFragmentShown);
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.distribution_options, menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help_distribution:
                HelpDialogFragment.newInstance(getString(R.string.distribution), getString(R.string.help_distribution))
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
            SharedPref.saveDateFrom(mContext, date, TrackerType.DISTRIBUTION);
        } else {
            mTvFinishDate.setText(Utils.getStringDate(date));
            SharedPref.saveDateTo(mContext, date, TrackerType.DISTRIBUTION);
        }
        mContext.getSupportLoaderManager().restartLoader(mLoaderId, null, this);
        updateView();
        mIsDateDialogShown = false;
    }
    
    @Override
    public void onDialogDestroyed() {
        //Log.d(TAG, "onDialogDestroyed [" + mLoaderId + "], mIsDateDialogShown = false");
        mIsDateDialogShown = false;
    }
    
    @Override
    public void onUpdateFragment() {
        updateView();
        mIsTransactionsFragmentShown = false;
        //Log.d(TAG, "onUpdateFragment [" + mLoaderId + "], mIsTransactionsFragmentShown = false");
    }
    
    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView");
        mOtherCategories.clear();
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
    public Loader<List<DistributionListRowItem>> onCreateLoader(int i, Bundle bundle) {
        return new DistributionDataLoader(mContext, this);
    }
     
    @Override
    public void onLoadFinished(Loader<List<DistributionListRowItem>> loader, List<DistributionListRowItem> data) {
        mAdapter.setData(data);
        fillPieChart();
    }
     
    @Override
    public void onLoaderReset(Loader<List<DistributionListRowItem>> loader) {
        mAdapter.setData(null);
    }
    
    private static class DistributionDataLoader extends AsyncTaskLoader<List<DistributionListRowItem>> {
     
        private DistributionFragment mFragment;
        
        public DistributionDataLoader(Context context, DistributionFragment instance) {
            super(context);
            mFragment = instance;
            onContentChanged();
        }

        private float filter(float v) {
            if (v < 1)
            	v = 0.5f;
            if (v > 100)
                v = 100;
            return v;
        }
        
        @Override
        public List<DistributionListRowItem> loadInBackground() {
            List<DistributionListRowItem> list = new ArrayList<DistributionListRowItem>();
            Cursor cursor = mDb.getSpentOnCategories(mFragment.mContext, TrackerType.DISTRIBUTION);
            if (cursor != null) {
                int len = cursor.getCount();
                if (len > 0) {
                    float totalExpenses = mDb.getTotalExpenses(
                            SharedPref.getDateFrom(mFragment.mContext, TrackerType.DISTRIBUTION),
                            SharedPref.getDateTo(mFragment.mContext, TrackerType.DISTRIBUTION));
                    float maxSpentByCategory = 0, lastSpentOnCategory = 0;
                    for (int i = 0; i < len; i++) {
                        if (cursor.moveToNext()) {
                            String categoryName = cursor.getString(cursor.getColumnIndex(Data.Categories.NAME));
                            if (i >= DistributionListRowItem.MAX_COUNT - 1) {
                                if (len > DistributionListRowItem.MAX_COUNT) {
                                    if (i == len - 1) {
                                        float spentByCategory = cursor.getFloat(4);
                                        lastSpentOnCategory += spentByCategory;
                                        float realProgress = lastSpentOnCategory * 100 / totalExpenses;
                                        realProgress = filter(realProgress);
                                        float barProgress = lastSpentOnCategory * 100 / maxSpentByCategory;
                                        barProgress = filter(barProgress);
                                        DistributionListRowItem row = new DistributionListRowItem(mFragment.getResources().getString(R.string.other), lastSpentOnCategory, realProgress, barProgress);
                                        list.add(row);
                                        mOtherCategories.add(categoryName);
                                    } else {
                                        float spentByCategory = cursor.getFloat(4);
                                        lastSpentOnCategory += spentByCategory;
                                        mOtherCategories.add(categoryName);
                                    }
                                    continue;
                                }
                            }
                            float spentByCategory = cursor.getFloat(4);
                            if (i == 0)
                                maxSpentByCategory = spentByCategory;
                            float realProgress = spentByCategory * 100 / totalExpenses;
                            realProgress = filter(realProgress);
                            float barProgress = spentByCategory * 100 / maxSpentByCategory;
                            barProgress = filter(barProgress);
                            DistributionListRowItem row = new DistributionListRowItem(categoryName, spentByCategory, realProgress, barProgress);
                            list.add(row);
                        }
                    }
                }
                cursor.close();
            }
            return list.size() == 0 ? null : list;
        }
        
        @Override
        protected void onStartLoading() {
            if (takeContentChanged())
                forceLoad();
        }

        @Override
        protected void onStopLoading() {
            cancelLoad();
        }
    }
    
    private void switchToTransactionsFragment(int position) {
        mListView.setVisibility(View.GONE);
        mStartPeriodLayout.setVisibility(View.GONE);
        mFinishPeriodLayout.setVisibility(View.GONE);
        mPieGraph.setVisibility(View.GONE);
        mTransactionsFragment = new TransactionsFragment();
        Bundle args = new Bundle();
        args.putString(TAG_FRAGMENT_CATEGORY, mAdapter.getItem(position).getCategoryName());
        args.putBoolean("is_other", position == DistributionListRowItem.MAX_COUNT - 1 && mOtherCategories.size() != 0);
        args.putBoolean("isFromDistribution", true);
        args.putStringArrayList("other_categories", mOtherCategories);
        mTransactionsFragment.setArguments(args);
        mContext.getSupportFragmentManager().beginTransaction().replace(R.id.distribution_layout, 
                mTransactionsFragment, TAG_FRAGMENT_TRANSACTIONS).addToBackStack(TAG_FRAGMENT_DISTRIBUTION).commit();
        mIsTransactionsFragmentShown = true;
    }
    
    private void fillPieChart() {
    	if (isFetchedDataEmpty())
    		return;
        final Resources resources = getResources();
        // get pie colors
        TypedArray imgs = resources.obtainTypedArray(R.array.colors);
        int[] colors = new int[imgs.length()];
        for (int i = 0; i < imgs.length(); i++)
        	colors[i] = imgs.getResourceId(i, -1);
        imgs.recycle();
        // get pie values
        int len = mAdapter.getCount();
        float[] values = new float[len];
        for (int i = 0; i < len; i++)
            values[i] = mAdapter.getSpentOnCategory(i);
        // fill pie chart
        mPieGraph.removeSlices();
        for (int i = 0; i < len; i++) {
            PieSlice slice = new PieSlice();
            slice.setColor(resources.getColor(colors[i]));
            slice.setValue(values[i]);
            slice.setTitle(Utils.getFormattedPercent(values[i] * 100 /
                    mDb.getTotalExpenses(SharedPref.getDateFrom(mContext, TrackerType.DISTRIBUTION),
                            SharedPref.getDateTo(mContext, TrackerType.DISTRIBUTION))) + "%");
            mPieGraph.addSlice(slice);
        }
        mPieGraph.setOnSliceClickedListener(new OnSliceClickedListener() {
            @Override
            public void onClick(int index) {
            	if (index >= 0)
            		switchToTransactionsFragment(index);
            }
        });

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
        	int i = 0;
	        for (PieSlice s: mPieGraph.getSlices()) {
        		s.setValue((float) Math.random() * 10);
        		s.setGoalValue(values[i++]);
	        }
	        mPieGraph.setDuration(1000);//default if unspecified is 300 ms
	        mPieGraph.setInterpolator(new AccelerateDecelerateInterpolator());//default if unspecified is linear
	        //pg.setAnimationListener(getAnimationListener());
	        mPieGraph.animateToGoalValues();
        }
    }
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public Animator.AnimatorListener getAnimationListener() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
	        return new Animator.AnimatorListener() {
	            @Override
	            public void onAnimationStart(Animator animation) {
	            }
	
	            @Override
	            public void onAnimationEnd(Animator animation) {
	                // you might want to call slice.setvalue(slice.getGoalValue)
	            }
	
	            @Override
	            public void onAnimationCancel(Animator animation) {
	            }
	
	            @Override
	            public void onAnimationRepeat(Animator animation) {
	            }
	        };
        else
        	return null;
    }

}
