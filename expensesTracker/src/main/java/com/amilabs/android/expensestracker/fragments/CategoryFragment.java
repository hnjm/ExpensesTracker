package com.amilabs.android.expensestracker.fragments;

import com.amilabs.android.expensestracker.R;
import com.amilabs.android.expensestracker.adapter.CategoryListAdapter;
import com.amilabs.android.expensestracker.database.DatabaseHandler;
import com.amilabs.android.expensestracker.utils.Utils;
import com.getbase.floatingactionbutton.FloatingActionButton;

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
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class CategoryFragment extends Fragment implements LoaderCallbacks<Cursor> {

    private static final String TAG = "CategoryFragment";
    
    private ListView mListView;
    private TextView mEmptyView;
    private RelativeLayout mLayoutHeaderListView;
    private FloatingActionButton mFAB;
    
    private static DatabaseHandler mDb;
    private AppCompatActivity mContext;
    private CategoryListAdapter mAdapter;
    private DialogFragment mDialogFragment;
    private ActionMode mActionMode;
    private ActionModeCallback mActionModeCallback;
    private final static int mLoaderId = -1; 
    // data supporting orientation change
    private boolean mIsNeededToRestore;
    private boolean[] mRestoreCheckedItemsArray;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_category, container, false);
        mLayoutHeaderListView = (RelativeLayout) rootView.findViewById(R.id.category_header_layout);
        mListView = (ListView) rootView.findViewById(android.R.id.list);
        mEmptyView = (TextView) rootView.findViewById(R.id.empty_view);
        mFAB = (FloatingActionButton) rootView.findViewById(R.id.fab);
        mFAB.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(Utils.NEW_ENTRY_ID);
            }
        });

        mContext = (AppCompatActivity) getActivity();
        mDb = DatabaseHandler.getInstance(mContext);

        setHasOptionsMenu(true);
        mActionModeCallback = new ActionModeCallback();
        
        mAdapter = new CategoryListAdapter(mContext, mDb.getCategoriesData());
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
        
        mContext.getSupportLoaderManager().initLoader(mLoaderId, null, this);
        if (mContext.getSupportLoaderManager().getLoader(mLoaderId) == null) {
            mContext.getSupportLoaderManager().initLoader(mLoaderId, null, this);
        } else {
            mContext.getSupportLoaderManager().restartLoader(mLoaderId, null, this);
        }
        
        updateView();
        
        if (savedInstanceState != null) {
            boolean isActionModeOn = savedInstanceState.getBoolean("action_mode");
            if (isActionModeOn) {
                mIsNeededToRestore = true;
                mRestoreCheckedItemsArray = savedInstanceState.getBooleanArray("items");
                mActionMode = mContext.startSupportActionMode(mActionModeCallback);
                mAdapter.setCheckable(true);
                mAdapter.notifyDataSetChanged();
            }
            int itemId = savedInstanceState.getInt("item_id");
            if (itemId != Utils.INVALID_ID)
                showDialog(itemId);
        }

        return rootView;
    }

    private void updateView() {
        if (isAdded()) {
            if (mDb.getCategoriesCount() == 0) {
                mLayoutHeaderListView.setVisibility(View.GONE);
                mListView.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.VISIBLE);
            } else {
                mLayoutHeaderListView.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.VISIBLE);
                mEmptyView.setVisibility(View.GONE);
            }
            mContext.setTitle(getString(R.string.categories));
            mContext.supportInvalidateOptionsMenu();
        }
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("action_mode", mActionMode != null);
        outState.putBooleanArray("items", mAdapter.getCheckedArray());
        if (mDialogFragment != null && mDialogFragment.isVisible())
            outState.putInt("item_id", AddEditCategoryDialogFragment.itemId);
        else
            outState.putInt("item_id", Utils.INVALID_ID);
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.category_options, menu);
        if (mDb.getCategoriesCount() == 0)
            menu.findItem(R.id.action_delete_category).setEnabled(false);
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_category:
            	enableActionMode();
                return true;
            case R.id.action_help_category:
                HelpDialogFragment.newInstance(getString(R.string.categories), getString(R.string.help_categories))
                        .show(getFragmentManager(), HelpDialogFragment.TAG);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void onDestroyView() {
        if (mActionMode != null)
            mActionMode.finish();
        cleanActionMode();
        super.onDestroyView();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CategoryDataLoader(mContext);
    }
     
    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Log.d(TAG, "onLoadFinished");
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
        mAdapter.swapCursor(null);
    }
    
    private static class CategoryDataLoader extends CursorLoader {
     
        public CategoryDataLoader(Context context) {
            super(context);
        }
     
        @Override
        public Cursor loadInBackground() {
            return mDb.getCategoriesData();
        }
    }
    
    private void enableActionMode() {
        mFAB.setVisibility(View.GONE);
        mActionMode = mContext.startSupportActionMode(mActionModeCallback);
        mAdapter.setCheckable(true);
        mAdapter.notifyDataSetChanged();
    }
    
    private void selectItem(View view, int position) {
        CategoryListAdapter.CategoryViewHolder holder = (CategoryListAdapter.CategoryViewHolder) view.getTag();
        holder.getCheckBox().setChecked(!holder.getCheckBox().isChecked());
        mAdapter.setItemChecked(position, holder.getCheckBox().isChecked());
        mActionModeCallback.itemSelectAll.setEnabled(!mAdapter.isSelectAll());
        mActionModeCallback.itemDelete.setEnabled(!mAdapter.isNoneSelected());
    }
    
    private void showDialog(int position) {
        FragmentTransaction ft = mContext.getSupportFragmentManager().beginTransaction();
        Fragment prev = mContext.getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            DialogFragment dF = (DialogFragment) prev;
            dF.dismiss();
            ft.remove(prev).commit();
        }
        mDialogFragment = AddEditCategoryDialogFragment.newInstance(this, position);
        mDialogFragment.show(mContext.getSupportFragmentManager(), "dialog");
    }
    
    public static class AddEditCategoryDialogFragment extends DialogFragment {

        private static CategoryFragment fragment;
        private EditText categoryName, categoryLimitEditText;
        private Spinner period;
        private static int itemId; // -1 : add
                                   // >=0: edit

        public static AddEditCategoryDialogFragment newInstance(CategoryFragment instance, int pos) {
            AddEditCategoryDialogFragment f = new AddEditCategoryDialogFragment();
            fragment = instance;
            itemId = pos;
            return f;
        }

        private void addEntry(String name, String limit, String period) {
            mDb.addCategory(name, Float.parseFloat(limit), period);
            fragment.mContext.getSupportLoaderManager().restartLoader(mLoaderId, null, fragment);
            fragment.updateView();
        }
        
        private void updateEntry(String name, String limit, String period) {
            mDb.updateCategory(fragment.mAdapter.mItems.get(itemId).id, name, Float.parseFloat(limit), period);
            fragment.mContext.getSupportLoaderManager().restartLoader(mLoaderId, null, fragment);
        }

        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            final Activity ctx = getActivity();
            final View v = ctx.getLayoutInflater().inflate(R.layout.dialog_category, null);
            categoryName = (EditText) v.findViewById(R.id.category_name_edit);
            categoryLimitEditText = (EditText) v.findViewById(R.id.category_limit_edit);
            period = (Spinner) v.findViewById(R.id.category_period_spinner);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(fragment.mContext, R.layout.spinner, getResources().getStringArray(R.array.period));
            period.setAdapter(adapter);

            if (itemId >= 0) {
                String name = fragment.mAdapter.mItems.get(itemId).name;
                categoryName.setText(name);
                categoryLimitEditText.setText(""+Utils.getFormatted(mDb.getCategoryLimit(name)));
                period.setSelection(adapter.getPosition(mDb.getCategoryPeriod(name)));
            }
            categoryName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        new Handler().postDelayed(new Runnable(){
                            @Override
                            public void run() {
                                showSoftInput(categoryName);
                            }
                        }, 200);
                    }
                }
            });
            return new AlertDialog.Builder(ctx).setView(v)
                    .setTitle(itemId >= 0 ? getString(R.string.edit_category) : getString(R.string.add_category))
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dismiss();
                        }
                    }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            String name = categoryName.getText().toString();
                            String limit = categoryLimitEditText.getText().toString();
                            if (name.trim().equals(""))
                                Toast.makeText(fragment.mContext, R.string.toast_empty_category, Toast.LENGTH_SHORT).show();
                            else if (limit.trim().equals(""))
                                Toast.makeText(fragment.mContext, R.string.toast_empty_limit, Toast.LENGTH_SHORT).show();
                            else {
                                if (itemId >= 0)
                                    updateEntry(name, limit, (String) period.getSelectedItem());
                                else
                                    addEntry(name, limit, (String) period.getSelectedItem());
                                dismiss();
                            }
                        }
                    }).create();
        }

        public void showSoftInput(View view) {
            final InputMethodManager imm = (InputMethodManager) fragment.mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }

        public void hideSoftInput(View view) {
            final InputMethodManager imm = (InputMethodManager) fragment.mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        @Override
        public void onDestroyView() {
            hideSoftInput(categoryLimitEditText);
            super.onDestroyView();
        }
    }

    private class ActionModeCallback implements ActionMode.Callback {
        
        private MenuItem itemSelectAll;
        private MenuItem itemDelete;
        
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mContext.getMenuInflater().inflate(R.menu.actionbar_menu, menu);
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
                    mDb.deleteCategory(mAdapter.getItemsCheckedIds());
                    mContext.getSupportLoaderManager().restartLoader(mLoaderId, null, CategoryFragment.this);
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

    private void cleanActionMode() {
        mFAB.setVisibility(View.VISIBLE);
        mActionMode = null;
        mAdapter.setCheckable(false);
        mAdapter.setAllItemsChecked(false);
        mAdapter.notifyDataSetChanged();
    }

}
