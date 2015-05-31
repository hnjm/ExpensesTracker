package com.amilabs.android.expensestracker.adapter;

import android.graphics.drawable.Drawable;

public class DrawerRowItem {
    
    public String drawerMenuText;
    public Drawable drawerMenuImage;
    
    public DrawerRowItem(String text, Drawable image) {
        drawerMenuText = text;
        drawerMenuImage = image;
    }
}
