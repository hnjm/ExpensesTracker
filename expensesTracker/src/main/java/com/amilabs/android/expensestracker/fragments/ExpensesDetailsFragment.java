package com.amilabs.android.expensestracker.fragments;

import com.amilabs.android.expensestracker.R;
import com.amilabs.android.expensestracker.adapter.ExpensesListAdapter;
import com.amilabs.android.expensestracker.database.DatabaseHandler;
import com.amilabs.android.expensestracker.interfaces.OnActionModeCallbackInterface;
import com.amilabs.android.expensestracker.utils.SharedPref;
import com.amilabs.android.expensestracker.utils.Utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import java.util.Calendar;
import java.util.Date;

public class ExpensesDetailsFragment extends ListFragment implements LoaderCallbacks<Cursor> {

    private static final String TAG = "ExpensesDetailsFragment";

    public static final String PERIOD = "period";
    private static final int[] mPeriodArray = { 7, 14, 21, 31, 62, 93, 186, 365, 730 };
    private int mLoaderId;
    
    private ListView mListView;
    private TextView mEmptyView, mTvTotalView;
    private RelativeLayout mTotalLayout;

    public ExpensesListAdapter mAdapter;
    private DialogFragment mDialogFragment;
    private ActionMode mActionMode;
    private ActionModeCallback mActionModeCallback;
    private OnActionModeCallbackInterface mCallback;
    private int mPeriod;
    // data supporting orientation change
    private boolean mIsNeededToRestore;
    private boolean[] mRestoreCheckedItemsArray;
    private boolean mIsDateDialogShown;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnActionModeCallbackInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnActionModeCallbackInterface");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: pos = " + mLoaderId);
        mLoaderId = getArguments().getInt(PERIOD);
        boolean[] periods = SharedPref.getPeriods(getActivity());
        int counter = 0;
        for (int i = 0; i < periods.length; i++) {
        	if (periods[i]) {
        		if (counter == mLoaderId) {
        			mPeriod = mPeriodArray[i];
        			break;
        		} else
        			counter++;
        	}
        }
        mActionModeCallback = new ActionModeCallback();
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_expenses_details, container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated: pos = " + mLoaderId);
        mListView = getListView();
        mEmptyView = (TextView) view.findViewById(R.id.empty_view);
        mTotalLayout = (RelativeLayout) view.findViewById(R.id.total_layout);
        mTvTotalView = (TextView) view.findViewById(R.id.tv_total);

        mAdapter = new ExpensesListAdapter(getActivity(), DatabaseHandler.getInstance(getActivity()).getExpensesData(mPeriod), mPeriod);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mActionMode != null)
                	selectItem(view, position);
                else
                    showDialog(position);
            }
        });
        mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
        	@Override
	        public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long arg3) {
        		if (mActionMode == null)
        			enableActionMode();
	        	return false;
	        }
		});

        if (getActivity().getSupportLoaderManager().getLoader(mLoaderId) == null)
            getActivity().getSupportLoaderManager().initLoader(mLoaderId, null, this);
        else
            getActivity().getSupportLoaderManager().restartLoader(mLoaderId, null, this);
        
        updateView();
        
        if (savedInstanceState != null) {
            boolean isActionModeOn = savedInstanceState.getBoolean("action_mode");
            if (isActionModeOn) {
                mIsNeededToRestore = true;
                mRestoreCheckedItemsArray = savedInstanceState.getBooleanArray("items");
                mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
                mAdapter.setCheckable(true);
                mAdapter.notifyDataSetChanged();
            }
            int itemId = savedInstanceState.getInt("item_id");
            if (itemId != Utils.INVALID_ID)
                showDialog(itemId);
            mIsDateDialogShown = savedInstanceState.getBoolean("is_date_dialog_shown");
        }
        super.onViewCreated(view, savedInstanceState);
        //Log.d(TAG, "onViewCreated: pos = " + mLoaderId);
    }
    
    private void updateView() {
        //Log.d(TAG, "updateView: pos = " + mLoaderId);
        if (isAdded()) {
            if (DatabaseHandler.getInstance(getActivity()).getExpensesDataCount(mPeriod) == 0) {
                mTotalLayout.setVisibility(View.GONE);
                mListView.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.VISIBLE);
            } else {
                mTotalLayout.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.VISIBLE);
                mEmptyView.setVisibility(View.GONE);
                mTvTotalView.setText(getString(R.string.total) + ": " +
                        Utils.getFormatted(DatabaseHandler.getInstance(getActivity()).getTotalExpenses(mPeriod)) + " " +
                        SharedPref.getCurrency(getActivity()));
            }
            getActivity().supportInvalidateOptionsMenu();
        }
        //Log.d(TAG, "updateView end: pos = " + mLoaderId);
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Log.d(TAG, "onSaveInstanceState [" + mLoaderId + "], state = " + mDialogState);
        outState.putBoolean("action_mode", mActionMode != null);
        outState.putBooleanArray("items", mAdapter == null ? null : mAdapter.getCheckedArray());
        if (mDialogFragment != null && mDialogFragment.isVisible())
            outState.putInt("item_id", AddEditExpensesDialogFragment.itemId);
        else
            outState.putInt("item_id", Utils.INVALID_ID);
        outState.putBoolean("is_date_dialog_shown", mIsDateDialogShown);
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.expense_options, menu);
        if (DatabaseHandler.getInstance(getActivity()).getExpensesDataCount(mPeriod) == 0)
            menu.findItem(R.id.action_delete_expense).setEnabled(false);
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_expense:
             	enableActionMode();
                return true;
            case R.id.action_help_expense:
                HelpDialogFragment.newInstance(getString(R.string.expenses), getString(R.string.help_expenses))
                        .show(getFragmentManager(), HelpDialogFragment.TAG);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    public void onResumeFragment() {
        //Log.d(TAG, "onResumeFragment: pos = " + mLoaderId);
        resetActionMode();
        getActivity().getSupportLoaderManager().restartLoader(mLoaderId, null, this);
        updateView();
        mAdapter.notifyDataSetChanged();
    }
    
    @Override
    public void onResume(){
        //Log.d(TAG, "onResume: pos = " + mLoaderId);
        if (mDialogFragment != null && mIsDateDialogShown) {
            ((AddEditExpensesDialogFragment) mDialogFragment).hideDatePickerDialog();
            ((AddEditExpensesDialogFragment) mDialogFragment).showDatePickerDialog();
        }
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        resetActionMode();
        super.onDestroyView();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.d(TAG, "onCreateLoader");
        return new ExpenseDataLoader(getActivity(), ExpensesDetailsFragment.this);
    }
     
    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Log.d(TAG, "onLoadFinished [" + mLoaderId + "]" + cursor.getCount());
        mAdapter.swapCursor(cursor);
        mAdapter.rebuildItemsList();
        if (mIsNeededToRestore) {
            mAdapter.setCheckedItemsArray(mRestoreCheckedItemsArray);
            mIsNeededToRestore = false;
            mActionModeCallback.itemSelectAll.setEnabled(!mAdapter.isAllSelected());
            mActionModeCallback.itemDelete.setEnabled(!mAdapter.isNoneSelected());
        }
    }
     
    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        Log.d(TAG, "onLoaderReset");
        mAdapter.swapCursor(null);
    }
    
    private static class ExpenseDataLoader extends CursorLoader {

        private Context ctx;
        private ExpensesDetailsFragment fragment;
        
        public ExpenseDataLoader(Context context, ExpensesDetailsFragment instance) {
            super(context);
            ctx = context;
            fragment = instance;
        }
     
        @Override
        public Cursor loadInBackground() {
            Log.d(TAG, "loadInBackground... [" + fragment.mLoaderId + "]");
            return DatabaseHandler.getInstance(ctx).getExpensesData(fragment.mPeriod);
        }
    }
    
    private void enableActionMode() {
        mCallback.onActionModeCallback(false);
        mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
        mAdapter.setCheckable(true);
        mAdapter.notifyDataSetChanged();
    }
    
    private void selectItem(View view, int position) {
        ExpensesListAdapter.ExpensesViewHolder holder = (ExpensesListAdapter.ExpensesViewHolder) view.getTag();
        holder.getCheckBox().setChecked(!holder.getCheckBox().isChecked());
        mAdapter.setItemChecked(position, holder.getCheckBox().isChecked());
        mActionModeCallback.itemSelectAll.setEnabled(!mAdapter.isSelectAll());
        mActionModeCallback.itemDelete.setEnabled(!mAdapter.isNoneSelected());
    }
    
    public void setDate(long date) {
        String dateString = Utils.formatter.format(new Date(date));
        AddEditExpensesDialogFragment.btnDate.setText(dateString);
    }
    
    public void onDialogDestroyed() {
        //Log.d(TAG, "onDialogDestroyed [" + mLoaderId + "], FALSE");
        mIsDateDialogShown = false;
    }
    
    public void showDialog(int position) {
        //Log.i(TAG, "showDialog [" + mLoaderId + "]" + mPeriod);
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        Fragment prev = getActivity().getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            DialogFragment dF = (DialogFragment) prev;
            dF.dismiss();
            ft.remove(prev).commit();
        }
        mDialogFragment = AddEditExpensesDialogFragment.newInstance(this, position);
        mDialogFragment.show(getActivity().getSupportFragmentManager(), "dialog");
    }
    
    public static class AddEditExpensesDialogFragment extends DialogFragment {

        private EditText etExpenseAmount, etExpenseDetails;
        private Spinner category;
        private DatePickerFragment datePickerFragment;
        private static ExpensesDetailsFragment fragment;
        private static Button btnDate;
        private static int itemId; // -1 : add
                                   // >=0: edit

        public static AddEditExpensesDialogFragment newInstance(ExpensesDetailsFragment instance, int pos) {
            AddEditExpensesDialogFragment f = new AddEditExpensesDialogFragment();
            fragment = instance;
            itemId = pos;
            return f;
        }

        private void addEntry(String expense, String category, String details) {
            DatabaseHandler.getInstance(getActivity()).addExpense(Utils.getLongDate(btnDate.getText().toString()), Float.parseFloat(expense), category, details);
            getActivity().getSupportLoaderManager().restartLoader(fragment.mLoaderId, null, fragment);
            fragment.updateView();
        }
        
        private void updateEntry(String expense, String category, String details) {
            DatabaseHandler.getInstance(getActivity()).updateExpense(getDBId(itemId), Utils.getLongDate(btnDate.getText().toString()), Float.parseFloat(expense), category, details);
            getActivity().getSupportLoaderManager().restartLoader(fragment.mLoaderId, null, fragment);
        }
        
        private int getDBId(int pos) {
            return fragment.mAdapter.mItems.get(pos).id;
        }
        
        private String getDateString() {
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            String dateString;
            if (itemId >= 0)
                dateString = Utils.formatter.format(new Date(DatabaseHandler.getInstance(getActivity()).getExpenseDate(getDBId(itemId)))).toString();
            else
                dateString = new StringBuilder().append(month + 1).append("-").append(day).append("-").append(year).toString();
            return dateString;
        }
        
        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            final Activity ctx = getActivity();
            final View v = ctx.getLayoutInflater().inflate(R.layout.dialog_expense, null);
            btnDate = (Button) v.findViewById(R.id.expense_btn_date);
            btnDate.setText(getDateString());
            btnDate.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDatePickerDialog();
                }
            });

            etExpenseAmount = (EditText) v.findViewById(R.id.expense_edit);
            etExpenseDetails = (EditText) v.findViewById(R.id.expense_details);
            category = (Spinner) v.findViewById(R.id.category_spinner);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner, DatabaseHandler.getInstance(getActivity()).getCategoriesNames());
            category.setAdapter(adapter);
            if (itemId >= 0) {
                etExpenseAmount.setText(""+Utils.getFormatted(DatabaseHandler.getInstance(getActivity()).getExpenseValue(getDBId(itemId))));
                etExpenseDetails.setText(""+DatabaseHandler.getInstance(getActivity()).getExpenseDetails(getDBId(itemId)));
                category.setSelection(adapter.getPosition(DatabaseHandler.getInstance(getActivity()).getExpenseCategory(getDBId(itemId))));
            }
            etExpenseAmount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        new Handler().postDelayed(new Runnable(){
                            @Override
                            public void run() {
                                showSoftInput(etExpenseAmount);
                            }
                        }, 200);
                    }
                }
            });

            return new AlertDialog.Builder(ctx).setView(v)
                    .setTitle(itemId >= 0 ? getString(R.string.edit_expense) : getString(R.string.add_expense))
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dismiss();
                        }
                    }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            String expense = etExpenseAmount.getText().toString();
                            String details = etExpenseDetails.getText().toString();
                            if (expense.trim().equals(""))
                                Toast.makeText(getActivity(), R.string.toast_empty_amount, Toast.LENGTH_SHORT).show();
                            else {
                                if (itemId >= 0)
                                    updateEntry(expense, category.getSelectedItem().toString(), details);
                                else
                                    addEntry(expense, category.getSelectedItem().toString(), details);
                                dismiss();
                            }
                            dismiss();
                        }
                    }).create();
        }

        public void hideDatePickerDialog() {
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            Fragment prev = getActivity().getSupportFragmentManager().findFragmentByTag("datePicker");
            if (prev != null) {
                DialogFragment dF = (DialogFragment) prev;
                dF.dismiss();
                ft.remove(prev).commit();
            }
        }
        
        public void showDatePickerDialog() {
            datePickerFragment = new DatePickerFragment();
            Bundle bundle = new Bundle();
            bundle.putString("date", getDateString());
            bundle.putString("trackerType", "NONE");
            bundle.putBoolean("isFromBtn", false);
            datePickerFragment.setArguments(bundle);
            datePickerFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
            fragment.mIsDateDialogShown = true;
        }
        
        private void showSoftInput(View view) {
            if (getActivity() != null) {
                final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }

        private void hideSoftInput(View view) {
            if (getActivity() != null) {
                final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }

        @Override
        public void onDestroyView() {
            hideSoftInput(etExpenseAmount);
            super.onDestroyView();
        }
    }

    private class ActionModeCallback implements ActionMode.Callback {
        
        private MenuItem itemSelectAll;
        private MenuItem itemDelete;
        
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            getActivity().getMenuInflater().inflate(R.menu.actionbar_menu, menu);
            itemSelectAll = menu.findItem(R.id.actionbar_selectall);
            itemDelete = menu.findItem(R.id.actionbar_delete);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            mAdapter.setCheckable(true);
            itemSelectAll.setEnabled(!mAdapter.isSelectAll());
            itemDelete.setEnabled(!mAdapter.isNoneSelected());
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.actionbar_selectall:
                    item.setEnabled(false);
                    itemDelete.setEnabled(true);
                    mAdapter.setAllItemsChecked(true);
                    mAdapter.notifyDataSetChanged();
                    break;
                case R.id.actionbar_delete:
                    DatabaseHandler.getInstance(getActivity()).deleteExpense(mAdapter.getItemsCheckedIds());
                    getActivity().getSupportLoaderManager().restartLoader(mLoaderId, null, ExpensesDetailsFragment.this);
                    updateView();
                default:
                    mode.finish();
                    break;
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            cleanActionMode();
        }
    };

    public void resetActionMode() {
        //Log.d(TAG, "resetActionMode: pos = " + mLoaderId);
        if (mActionMode != null)
            mActionMode.finish();
        cleanActionMode();
    }
    
    private void cleanActionMode() {
        mActionMode = null;
        mCallback.onActionModeCallback(true);
        mAdapter.setCheckable(false);
        mAdapter.setAllItemsChecked(false);
        mAdapter.notifyDataSetChanged();
    }
}
