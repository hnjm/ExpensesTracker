package com.amilabs.android.expensestracker.database;

public class Data {
    
    public static final String DB_NAME = "expenses_tracker.db";
    public static final int DB_VERSION = 2;

    public static class Expenses {
        public static final String TABLE_NAME = "EXPENSES";
        public static final String ID = "_id";
        public static final String DATE = "date";
        public static final String EXPENSE = "expense";
        public static final String CATEGORY_ID = "category_id";
        public static final String DETAILS = "details";
    }
    
    public static class Categories {
        public static final String TABLE_NAME = "CATEGORIES";
        public static final String ID = "_id";
        public static final String NAME = "name";
        public static final String LIMIT = "amount_limit";
        public static final String PERIOD = "period";
    }
    
    public static class Currencies {
        public static final String TABLE_NAME = "CURRENCIES";
        public static final String ID = "_id";
        public static final String NAME = "name";
    }

}
