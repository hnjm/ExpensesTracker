package com.amilabs.android.expensestracker.fragments;

import com.amilabs.android.expensestracker.R;
import com.amilabs.android.expensestracker.interfaces.OnActionModeCallbackInterface;
import com.amilabs.android.expensestracker.interfaces.OnDateSelectedListener;
import com.amilabs.android.expensestracker.utils.SharedPref;
import com.amilabs.android.expensestracker.utils.Utils;
import com.getbase.floatingactionbutton.FloatingActionButton;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

//import java.io.IOException;
//import java.io.Serializable;

public class ExpensesFragment extends Fragment implements
        OnDateSelectedListener, OnActionModeCallbackInterface {

    private static final String TAG = "ExpensesFragment";
    
    private AppCompatActivity mContext;
    private ViewPager mViewPager;
    private ExpensesPagerAdapter mAdapter;
    private FloatingActionButton mFAB;
    
    private String[] mPeriods;
    private List<String> mTabs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_expenses, container, false);
        TextView tvNoTabs = (TextView) rootView.findViewById(R.id.empty_view);
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        mFAB = (FloatingActionButton) rootView.findViewById(R.id.fab);

        mContext = (AppCompatActivity) getActivity();
        mContext.setTitle(getString(R.string.expenses));
        boolean[] periods = SharedPref.getPeriods(mContext);

        if (areAllFalse(periods)) {
            mViewPager.setVisibility(View.GONE);
            tvNoTabs.setVisibility(View.VISIBLE);
            mFAB.setVisibility(View.GONE);
        } else {
            mViewPager.setVisibility(View.VISIBLE);
            tvNoTabs.setVisibility(View.GONE);
            mPeriods = getResources().getStringArray(R.array.periods);
            mTabs = new ArrayList<String>();
            for (int i = 0; i < periods.length; i++) {
                if (periods[i])
                    mTabs.add(mPeriods[i].toUpperCase());
            }
            mAdapter = new ExpensesPagerAdapter(mContext.getSupportFragmentManager(), this);
            if (savedInstanceState != null) {
                //SerializableSparseArrayContainer deserializedSparseArray = (SerializableSparseArrayContainer) savedInstanceState.getSerializable("fragments");
                //mAdapter.mFragments = deserializedSparseArray.getSparseArray();
                mAdapter.currentPosition = savedInstanceState.getInt("current_position");
                //mAdapter.mFragments.put(mAdapter.currentPosition, (ExpensesDetailsFragment) findFragmentByPosition(mAdapter.currentPosition));
                for (int i = 0; i < mTabs.size(); i++)
                    mAdapter.fragments.put(i, (ExpensesDetailsFragment) findFragmentByPosition(i));
            }
            mViewPager.setAdapter(mAdapter);
            mViewPager.setOffscreenPageLimit(2);
            mViewPager.setCurrentItem(mAdapter.currentPosition);
            mViewPager.setOnPageChangeListener(mAdapter);
            //setRetainInstance(true);
            mFAB.setVisibility(View.VISIBLE);
            mFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAdapter.fragments.get(mAdapter.currentPosition).showDialog(Utils.NEW_ENTRY_ID);
                }
            });
        }
        return rootView;
    }
    
    private boolean areAllFalse(boolean[] array) {
        for(boolean b: array)
            if (b)
                return false;
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        //SerializableSparseArrayContainer sparseArraySerializable = new SerializableSparseArrayContainer(mAdapter.mFragments);
        //outState.putSerializable("fragments", sparseArraySerializable);
        if (mAdapter != null)
            outState.putInt("current_position", mAdapter.currentPosition);
    }

    @Override
    public void onActionModeCallback(boolean isVisible) {
        mFAB.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDateSelected(long date) {
        mAdapter.fragments.get(mAdapter.currentPosition).setDate(date);
    }

    @Override
    public void onDialogDestroyed() {
        mAdapter.fragments.get(mAdapter.currentPosition).onDialogDestroyed();
    }
    
    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView");
        if (mAdapter != null && mAdapter.fragments != null)
            for (int i = 0; i < mAdapter.fragments.size(); i++) {
            	if (mAdapter.fragments.get(i) != null) {
	                try {
	                    FragmentTransaction transaction = mContext.getSupportFragmentManager().beginTransaction();
	                    transaction.remove(mAdapter.fragments.get(i));
	                    transaction.commit();
	                } catch (Exception e) {
	                }
            	}
            }
        super.onDestroyView();
    }

    private Fragment findFragmentByPosition(int position) {
        return mContext.getSupportFragmentManager().findFragmentByTag("android:switcher:" + mViewPager.getId() + ":" + mAdapter.getItemId(position));
    }
    
    public static class ExpensesPagerAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener {

        private ExpensesFragment outerInstance;
        private SparseArray<ExpensesDetailsFragment> fragments;
        private int currentPosition;
        
        public ExpensesPagerAdapter(FragmentManager fm, ExpensesFragment outerInstance) {
            super(fm);
            fragments = new SparseArray<ExpensesDetailsFragment>();
            this.outerInstance = outerInstance;
        }

        @Override
        public void onPageSelected(int position) {
            //Log.d(TAG, "onPageSelected: pos = " + position);
            ExpensesDetailsFragment currentFragment = fragments.get(currentPosition);
            if (currentFragment != null)
                currentFragment.resetActionMode();
            ExpensesDetailsFragment newFragment = fragments.get(position);
            if (newFragment != null)
                newFragment.onResumeFragment();
            currentPosition = position;
        }
        
        @Override
        public Fragment getItem(int i) {
            //Log.d(TAG, "getItem: pos = " + i);
            Fragment fragment = new ExpensesDetailsFragment();
            Bundle args = new Bundle();
            args.putInt(ExpensesDetailsFragment.PERIOD, i);
            fragment.setArguments(args);
            fragments.put(i, (ExpensesDetailsFragment) fragment);
            return fragment;
        }

        @Override
        public int getCount() {
            return outerInstance.mTabs.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return outerInstance.mTabs.get(position);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }

    }

    /*public static class SerializableSparseArrayContainer implements Serializable {

        private static final long serialVersionUID = 393662066105575556L;
        private SparseArray<ExpensesDetailsFragment> mSparseArray;

        public SerializableSparseArrayContainer(SparseArray<ExpensesDetailsFragment> mDataArray) {
            this.mSparseArray = mDataArray;
        }

        public SparseArray<ExpensesDetailsFragment> getSparseArray() {
            return mSparseArray;
        }

        public void setSparseArray(SparseArray<ExpensesDetailsFragment> sparseArray) {
            this.mSparseArray = sparseArray;
        }

        private void writeObject(java.io.ObjectOutputStream out) throws IOException {
            out.writeLong(serialVersionUID);
            int sparseArraySize = mSparseArray.size();
            out.write(sparseArraySize);
            for (int i = 0 ; i < sparseArraySize; i++){
                int key = mSparseArray.keyAt(i);
                out.writeInt(key);
                ExpensesDetailsFragment value = mSparseArray.get(key);
                out.writeObject(value);
            }
        }

        private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
            long readSerialVersion = in.readLong();
            if (readSerialVersion != serialVersionUID) {
                throw new IOException("serial version mismatch");
            }
            int sparseArraySize = in.read();
            mSparseArray = new SparseArray<ExpensesDetailsFragment>(sparseArraySize);
            for (int i = 0 ; i < sparseArraySize; i++) {
                int key = in.readInt();
                ExpensesDetailsFragment value = (ExpensesDetailsFragment) in.readObject();
                mSparseArray.put(key, value);
            }
        }
    }*/
}
