package com.amilabs.android.expensestracker.interfaces;

public interface Constants {

    public static final String TAG_FRAGMENT_HELP = "help";
    public static final String TAG_FRAGMENT_CATEGORY = "category";
    public static final String TAG_FRAGMENT_EXPENSES = "expenses";
    public static final String TAG_FRAGMENT_PLANNER = "planner";
    public static final String TAG_FRAGMENT_DISTRIBUTION = "distribution";
    public static final String TAG_FRAGMENT_TRANSACTIONS = "transactions";
    public static final String TAG_FRAGMENT_SETTINGS = "settings";
    public static final String TAG_FRAGMENT_ABOUT = "about";
    public static final String TAG_FRAGMENT_UNLOCK = "unlock";

    public static final int ID_FRAGMENT_EXPENSES = 0;
    public static final int ID_FRAGMENT_CATEGORY = 1;
    public static final int ID_FRAGMENT_DISTRIBUTION = 2;
    public static final int ID_FRAGMENT_PLANNER = 3;
    public static final int ID_FRAGMENT_SETTINGS = 4;
    public static final int ID_FRAGMENT_ABOUT = 5;

    public static enum TrackerType { NONE, DISTRIBUTION, PLANNER };

    public final static int DAY_IN_MSEC = 24 * 60 * 60 * 1000;
    public final static int WEEK_IN_MSEC = 6 * DAY_IN_MSEC;
    public final static String DEFAULT_CURRENCY = "USD";
    public final static String WEEK = "Week";
    public final static String MONTH = "Month";
    public final static String YEAR = "Year";

    public static final String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqf9QnbdGxTYmgNYFiH1LEsrcz4ZNbKCBqFG56wUSKilTNTi0D6cHAMN3osskL2wNItd1or+ufAfzNBF3oVUEnCMioOHVe4Z/e2mJuBoiyHEsacPx1iLh5PkqIN6mHN95/8LQorKgEZ9JRZN1hjbTeptncnb/tvIa8omVXLcDJ4ACMn9+0M54Kcykmvn8VNk6S+oFE6vNprBjxVhaI6hKTFHBWsEt0gWuiuhDrjufnI2ewQbYhWu5ZWpmrsNcbYubSJxObH9hrRcPnPo/CVODaY5/R5VpdAYD86cYAEBwLUiOpUY+UR5sWIZlkk2hV76BwbgpV9pAGMUr+Mqv7HO13QIDAQAB";
    public static final String DEVELOPER_PAYLOAD = "myPurchasePayload";
    public static final String PREMIUM_SKU = "amilabs.expensestracker.adsfree";
    public static final String INTERSTITIAL_AD_ID = "ca-app-pub-4169047675530402/5722739377";

}
