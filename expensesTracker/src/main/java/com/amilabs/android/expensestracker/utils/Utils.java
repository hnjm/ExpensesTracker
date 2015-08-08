package com.amilabs.android.expensestracker.utils;

import com.amilabs.android.expensestracker.database.DatabaseHandler;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Utils {

    public static final int NEW_ENTRY_ID = -1;
    public static final int INVALID_ID = -2;
    
    public static SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
    
    public static long getFreshDate(Context ctx, long date) {
        if (date == 0)
            date = System.currentTimeMillis();
        else
            date = getLongDate(formatter.format(new Date(date)));
        return date;
    }
    
    public static long getLongDate(String date) {
        long res = 0;
        try {
            res = formatter.parse(date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return res;
    }
    
    public static String getStringDate(long date) {
        return formatter.format(new Date(date)).toString();
    }

    public static Date getDate(String date) {
        Date res = null;
        try {  
            res = formatter.parse(date);  
        } catch (ParseException e) {  
            e.printStackTrace();  
        }
        return res;
    }

    public static Date getDate(long date) {
        return getDate(getStringDate(date));
    }

    public static int get(long time, int id) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(time);
        return date.get(id);
    }

    public static String getFormatted(double value) {
        return new DecimalFormat("#.#").format(value);
    }
    
    public static String getFormattedPercent(double value) {
        return new DecimalFormat("#").format(value);
    }
    
    /*public static float getScreenWidthInDP(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        int width;
        try {
            display.getSize(size);
            width = size.x;
        } catch (java.lang.NoSuchMethodError ignore) { // Older device
            width = display.getWidth();
        }
        return width / Resources.getSystem().getDisplayMetrics().density;
    }
    
    public static float getTextViewWidthInDP(TextView tv) {
        Rect bounds = new Rect();
        Paint textPaint = tv.getPaint();
        textPaint.getTextBounds(tv.getText().toString(), 0, tv.getText().length(), bounds);
        return bounds.width() / Resources.getSystem().getDisplayMetrics().density;
    }

    public static int getPixels(float dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }*/
}
