package com.amilabs.android.expensestracker.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.amilabs.android.expensestracker.interfaces.Constants;

public class SharedPref implements Constants {

    private static final String PREF_NAME = "Preferences";
    private static final String PREF_CURRENCY = "Currency";
    private static final String PREF_DISTRIBUTION_DATE_FROM = "DistributionDateFrom";
    private static final String PREF_DISTRIBUTION_DATE_TO = "DistributionDateTo";
    private static final String PREF_PLANNER_DATE_FROM = "PlannerDateFrom";
    private static final String PREF_PLANNER_DATE_TO = "PlannerDateTo";
    private static final String PREF_PERIODS = "Period";
    private static final String PREF_PREMIUM = "Premium";
    private static final String PREF_DEFAULT_VALUES = "DefaultValues";
    private static final String PREF_SMS_FLAG = "isSmsEnabled";
    private static final String PREF_SMS_PHONE_NUMBER = "PhoneNumber";
    
    public static String getCurrency(Context ctx) {
        SharedPreferences pref = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(PREF_CURRENCY, "USD");
    }
    
    public static void saveCurrency(Context ctx, String currency) {
        SharedPreferences settings = ctx.getSharedPreferences(PREF_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREF_CURRENCY, currency);
        editor.commit();
    }

    public static long getDateFrom(Context ctx, TrackerType type) {
        SharedPreferences pref = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getLong(type == TrackerType.DISTRIBUTION ? PREF_DISTRIBUTION_DATE_FROM : PREF_PLANNER_DATE_FROM, 0);
    }

    public static void saveDateFrom(Context ctx, long date, TrackerType type) {
        SharedPreferences settings = ctx.getSharedPreferences(PREF_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(type == TrackerType.DISTRIBUTION ? PREF_DISTRIBUTION_DATE_FROM : PREF_PLANNER_DATE_FROM, date);
        editor.commit();
    }

    public static long getDateTo(Context ctx, TrackerType type) {
        SharedPreferences pref = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getLong(type == TrackerType.DISTRIBUTION ? PREF_DISTRIBUTION_DATE_TO : PREF_PLANNER_DATE_TO, 0);
    }

    public static void saveDateTo(Context ctx, long date, TrackerType type) {
        SharedPreferences settings = ctx.getSharedPreferences(PREF_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(type == TrackerType.DISTRIBUTION ? PREF_DISTRIBUTION_DATE_TO : PREF_PLANNER_DATE_TO, date);
        editor.commit();
    }

    public static boolean[] getPeriods(Context ctx) {
        boolean[] values = new boolean[9];
        SharedPreferences pref = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        for (int i = 0; i < values.length; i++)
            values[i] = pref.getBoolean(PREF_PERIODS + i, false);
        return values;
    }
    
    public static void savePeriods(Context ctx, boolean[] values) {
        SharedPreferences settings = ctx.getSharedPreferences(PREF_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        for (int i = 0; i < values.length; i++)
            editor.putBoolean(PREF_PERIODS + i, values[i]);
        editor.commit();
    }

    public static boolean isPremium(Context ctx) {
        SharedPreferences pref = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(PREF_PREMIUM, false);
    }

    public static void setPremium(Context ctx, boolean value) {
        SharedPreferences settings = ctx.getSharedPreferences(PREF_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(PREF_PREMIUM, value);
        editor.commit();
    }

    public static boolean isDefaultValuesUsed(Context ctx) {
        SharedPreferences pref = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(PREF_DEFAULT_VALUES, false);
    }

    public static void setDefaultValuesUsed(Context ctx, boolean value) {
        SharedPreferences settings = ctx.getSharedPreferences(PREF_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(PREF_DEFAULT_VALUES, value);
        editor.commit();
    }

    public static boolean isSmsEnabled(Context ctx) {
        SharedPreferences pref = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(PREF_SMS_FLAG, false);
    }
    
    public static void setSmsEnabledFlag(Context ctx, boolean value) {
        SharedPreferences settings = ctx.getSharedPreferences(PREF_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(PREF_SMS_FLAG, value);
        editor.commit();
    }
    
    public static String getPhoneNumber(Context ctx) {
        SharedPreferences pref = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(PREF_SMS_PHONE_NUMBER, null);
    }
    
    public static void setPhoneNumber(Context ctx, String value) {
        SharedPreferences settings = ctx.getSharedPreferences(PREF_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREF_SMS_PHONE_NUMBER, value);
        editor.commit();
    }
    
}