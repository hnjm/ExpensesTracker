package com.amilabs.android.expensestracker;

import com.amilabs.android.expensestracker.fragments.adapters.DrawerAdapter;
import com.amilabs.android.expensestracker.fragments.adapters.DrawerRowItem;
import com.amilabs.android.expensestracker.database.DatabaseHandler;
import com.amilabs.android.expensestracker.fragments.AboutFragment;
import com.amilabs.android.expensestracker.fragments.CategoryFragment;
import com.amilabs.android.expensestracker.fragments.DistributionFragment;
import com.amilabs.android.expensestracker.fragments.ExpensesFragment;
import com.amilabs.android.expensestracker.fragments.PlannerFragment;
import com.amilabs.android.expensestracker.fragments.SettingsFragment;
import com.amilabs.android.expensestracker.fragments.UnlockFragment;
import com.amilabs.android.expensestracker.interfaces.Constants;
import com.amilabs.android.expensestracker.interfaces.OnDateSelectedListener;
import com.amilabs.android.expensestracker.interfaces.OnUpdateFragmentInterface;
import com.amilabs.android.expensestracker.utils.SharedPref;
import com.android.vending.billing.IabHelper;
import com.android.vending.billing.IabResult;
import com.android.vending.billing.Inventory;
import com.android.vending.billing.Purchase;
import com.google.android.gms.ads.*;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
	OnDateSelectedListener, OnUpdateFragmentInterface, Constants {

    private static final String TAG = "MainActivity";

    private DrawerLayout mDrawerLayout;
    private LinearLayout mDrawerListLayout;
    private LinearLayout mUnlockLinearLayout;
    private View mViewLine;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar mToolbar;

    private List<DrawerRowItem> mDrawerMenuItems;
    private Fragment mFragment;
    private String mTag;
    private int mSelectedItem;
    private AdView mBannerAd;
    
    private DrawerAdapter mAdapter;
    private IabHelper mHelper;
    private boolean mIsBillingAvailable;
    private InterstitialAd mInterstitialAd;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer);

        DatabaseHandler.getInstance(this).init();
        initLayouts();
        initMainMenu();

        mAdapter = new DrawerAdapter(this, R.layout.drawer_list_item, mDrawerMenuItems);
        mDrawerList.setAdapter(mAdapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        if (savedInstanceState == null) {
            selectItem(ID_FRAGMENT_EXPENSES);
        } else {
            mSelectedItem = savedInstanceState.getInt("item");
            if (mSelectedItem == -1)
                mUnlockLinearLayout.setBackgroundResource(R.drawable.pressed_color);
            mAdapter.setChecked(mSelectedItem);
            mAdapter.notifyDataSetChanged();
            mTag = savedInstanceState.getString("tag");
            mFragment = getSupportFragmentManager().findFragmentByTag(mTag);
        }

        setUpAds();
        setUpGoogleAnalytics();
        setUpInAppPurchase();

        Log.d(TAG, "onCreate");
    }
    
    private void initLayouts() {
        mDrawerListLayout = (LinearLayout) findViewById(R.id.drawer_list_layout);
        mUnlockLinearLayout = (LinearLayout) findViewById(R.id.ll_unlock);
        mViewLine = findViewById(R.id.line);
        if (SharedPref.isPremium(this)) {
            mUnlockLinearLayout.setVisibility(View.GONE);
            mViewLine.setVisibility(View.GONE);
        }
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        if (mDrawerLayout != null)
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mUnlockLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUnlockLinearLayout.setBackgroundResource(R.drawable.pressed_color);
                mSelectedItem = -1;
                mAdapter.setChecked(mSelectedItem);
                mAdapter.notifyDataSetChanged();
                mFragment = new UnlockFragment();
                mTag = TAG_FRAGMENT_UNLOCK;
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, mFragment, mTag).commit();
                setTitle(getString(R.string.pro));
                if (mDrawerLayout != null)
                    mDrawerLayout.closeDrawer(mDrawerListLayout);
            }
        });

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (mDrawerLayout != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.menu, R.string.app_name) { };
            mDrawerLayout.setDrawerListener(mDrawerToggle);
        }
    }
    
    private void initMainMenu() {
        mDrawerMenuItems = new ArrayList<DrawerRowItem>();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            mDrawerMenuItems.add(new DrawerRowItem(getString(R.string.expenses), getResources().getDrawable(R.drawable.ic_main_menu_expenses, null)));
            mDrawerMenuItems.add(new DrawerRowItem(getString(R.string.categories), getResources().getDrawable(R.drawable.ic_main_menu_categories, null)));
            mDrawerMenuItems.add(new DrawerRowItem(getString(R.string.distribution), getResources().getDrawable(R.drawable.ic_main_menu_distribution, null)));
            mDrawerMenuItems.add(new DrawerRowItem(getString(R.string.planner), getResources().getDrawable(R.drawable.ic_main_menu_tracker, null)));
            mDrawerMenuItems.add(new DrawerRowItem(getString(R.string.settings), getResources().getDrawable(R.drawable.ic_main_menu_settings, null)));
            mDrawerMenuItems.add(new DrawerRowItem(getString(R.string.about), getResources().getDrawable(R.drawable.ic_main_menu_about, null)));
        } else {
            mDrawerMenuItems.add(new DrawerRowItem(getString(R.string.expenses), getResources().getDrawable(R.drawable.ic_main_menu_expenses)));
            mDrawerMenuItems.add(new DrawerRowItem(getString(R.string.categories), getResources().getDrawable(R.drawable.ic_main_menu_categories)));
            mDrawerMenuItems.add(new DrawerRowItem(getString(R.string.distribution), getResources().getDrawable(R.drawable.ic_main_menu_distribution)));
            mDrawerMenuItems.add(new DrawerRowItem(getString(R.string.planner), getResources().getDrawable(R.drawable.ic_main_menu_tracker)));
            mDrawerMenuItems.add(new DrawerRowItem(getString(R.string.settings), getResources().getDrawable(R.drawable.ic_main_menu_settings)));
            mDrawerMenuItems.add(new DrawerRowItem(getString(R.string.about), getResources().getDrawable(R.drawable.ic_main_menu_about)));
        }
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putString("tag", mTag);
        outState.putInt("item", mSelectedItem);
    }
    
    @Override
    public void onDateSelected(long date) {
        ((OnDateSelectedListener) mFragment).onDateSelected(date);
    }
    
    @Override
    public void onDialogDestroyed() {
        ((OnDateSelectedListener) mFragment).onDialogDestroyed();
    }
    
    @Override
    public void onUpdateFragment() {
        if (mTag.equals(TAG_FRAGMENT_PLANNER) || mTag.equals(TAG_FRAGMENT_DISTRIBUTION))
            ((OnUpdateFragmentInterface) mFragment).onUpdateFragment();
    }

    @Override
    public boolean onKeyUp(int keycode, KeyEvent e) {
        switch (keycode) {
            case KeyEvent.KEYCODE_MENU:
                if (getSupportActionBar() != null ) {
                    getSupportActionBar().openOptionsMenu();
                    return true;
                }
        }
        return super.onKeyUp(keycode, e);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mDrawerLayout != null) {
                if (mDrawerLayout.isDrawerOpen(mDrawerListLayout))
                    mDrawerLayout.closeDrawer(mDrawerListLayout);
                else
                    mDrawerLayout.openDrawer(mDrawerListLayout);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mDrawerLayout != null)
            mDrawerToggle.syncState();
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mBannerAd.resume();
    }

    @Override
    public void onPause() {
        mBannerAd.pause();
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }
    
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        DatabaseHandler.getInstance(this).close();
        if (mHelper != null)
            mHelper.dispose();
        mHelper = null;
        mBannerAd.destroy();
    }

    @Override
    public void onBackPressed() {
    	if (getSupportFragmentManager().getBackStackEntryCount() != 0)
    		getSupportFragmentManager().popBackStackImmediate();
    	else {
            if (mSelectedItem == ID_FRAGMENT_EXPENSES) {
                if (!SharedPref.isPremium(this) && mInterstitialAd.isLoaded())
                    mInterstitialAd.show();
                else
                    finish();
            } else
                selectItem(ID_FRAGMENT_EXPENSES);
    	}
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mHelper.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }
    
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //view.setBackgroundResource(R.drawable.pressed_color);
            //if (previousSelectedView != null && previousSelectedView != view)
            //    previousSelectedView.setBackgroundResource(R.color.drawer_bg);
            //previousSelectedView = view;
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        mSelectedItem = position;
        mUnlockLinearLayout.setBackgroundResource(R.color.drawer_bg);
        mAdapter.setChecked(position);
        mAdapter.notifyDataSetChanged();
        switch (position) {
            case ID_FRAGMENT_EXPENSES:
                mFragment = new ExpensesFragment();
                mTag = TAG_FRAGMENT_EXPENSES;
                break;
            case ID_FRAGMENT_CATEGORY:
                mFragment = new CategoryFragment();
                mTag = TAG_FRAGMENT_CATEGORY;
                break;
            case ID_FRAGMENT_DISTRIBUTION:
                mFragment = new DistributionFragment();
                mTag = TAG_FRAGMENT_DISTRIBUTION;
                break;
            case ID_FRAGMENT_PLANNER:
                mFragment = new PlannerFragment();
                mTag = TAG_FRAGMENT_PLANNER;
                break;
            case ID_FRAGMENT_SETTINGS:
                mFragment = new SettingsFragment();
                mTag = TAG_FRAGMENT_SETTINGS;
                break;
            case ID_FRAGMENT_ABOUT:
                mFragment = new AboutFragment();
                mTag = TAG_FRAGMENT_ABOUT;
                break;
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, mFragment, mTag).commit();
        //mDrawerList.setItemChecked(position, true);
        setTitle(mDrawerMenuItems.get(position).drawerMenuText);
        if (mDrawerLayout != null)
            mDrawerLayout.closeDrawer(mDrawerListLayout);
    }
    
    private void setUpGoogleAnalytics() {
        Tracker t = ((MyApp) getApplication()).getTracker(MyApp.TrackerName.APP_TRACKER);
        t.setScreenName(TAG);
        t.send(new HitBuilders.AppViewBuilder().build());
    }
    
    private void setUpAds() {
        mBannerAd = (AdView) findViewById(R.id.ad);
	    mInterstitialAd = new InterstitialAd(this);
	    mInterstitialAd.setAdUnitId(INTERSTITIAL_AD_ID);
	    mInterstitialAd.setAdListener(new AdListener() {

            @Override
            public void onAdClosed() {
            	finish();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.d(TAG, "AdMob in list failed to receive ad: " + errorCode);
            }
        });
        if (!SharedPref.isPremium(this)) {
        	AdRequest adRequest = new AdRequest.Builder().build();
            mBannerAd.loadAd(adRequest);
    	    mInterstitialAd.loadAd(adRequest);
        } else
            mBannerAd.setVisibility(View.GONE);
    }
    
    private void setUpInAppPurchase() {
        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    mIsBillingAvailable = false;
                    Log.e(TAG, "In-app Billing setup failed: " + result);
                } else {
                    mIsBillingAvailable = true;
                    Log.d(TAG, "In-app Billing is set up OK");
                    mHelper.enableDebugLogging(true, TAG);
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                }
            }
        });
    }
    
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (mHelper == null)
                return;
            if (result.isFailure()) {
                Log.d(TAG, "Failed to query inventory: " + result);
                return;
            }
            Purchase premiumPurchase = inventory.getPurchase(PREMIUM_SKU);
            boolean isPremium = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));
            Log.d(TAG, "User is " + (isPremium ? "PREMIUM" : "NOT PREMIUM"));
            if (isPremium)
                upgradeApp();
        }
    };

    private boolean verifyDeveloperPayload(Purchase p) {
        return p.getDeveloperPayload().equals(DEVELOPER_PAYLOAD);
    }

    public void purchasePremium() {
        if (mIsBillingAvailable)
            //mHelper.launchSubscriptionPurchaseFlow(this, PREMIUM_SKU, 10001, mPurchaseFinishedListener, "mypurchasetoken");
        	mHelper.launchPurchaseFlow(this, PREMIUM_SKU, 10001, mPurchaseFinishedListener, "mypurchasetoken");
        else
            Toast.makeText(MainActivity.this, "Billing is not available, please check your Google account", Toast.LENGTH_SHORT).show();
    }
    
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (result.isFailure()) {
                // handle failure
                Log.e(TAG, "Purchase failed: " + result.getMessage() + ", " + result.getResponse());
                //consumePremium();
                return;
            } else if (purchase.getSku().equals(PREMIUM_SKU)) {
                consumePremium();
                upgradeApp();
            }
        }
    };
    
    private void upgradeApp() {
        if (mTag.equals(TAG_FRAGMENT_UNLOCK))
            ((OnUpdateFragmentInterface) mFragment).onUpdateFragment();
        removeUnlockView();
    }

    private void removeUnlockView() {
        mUnlockLinearLayout.setVisibility(View.GONE);
        mViewLine.setVisibility(View.GONE);
        mBannerAd.setVisibility(View.GONE);
        SharedPref.setPremium(this, true);
    }

    public void consumePremium() {
        mHelper.queryInventoryAsync(mReceivedInventoryListener);
    }
        
    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (result.isFailure()) {
                // handle failure
            } else {
                mHelper.consumeAsync(inventory.getPurchase(PREMIUM_SKU), mConsumeFinishedListener);
            }
        }
    };
    
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            if (result.isSuccess())
                removeUnlockView();
        }
    };
}
