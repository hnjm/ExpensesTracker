package com.amilabs.android.expensestracker.fragments.adapters;

public class DistributionListRowItem {

	public static final int MAX_COUNT = 10;
	
	private String mCategoryName;
	private float mSpentOnCategory;
	private float mRealProgress;
	private float mBarProgress;
	
	public DistributionListRowItem(String categoryName, float spentOnCategory, float realProgress, float barProgress) {
	    mCategoryName = categoryName;
	    mSpentOnCategory = spentOnCategory;
	    mRealProgress = realProgress;
	    mBarProgress = barProgress;
	}
	
	public String getCategoryName() {
	    return mCategoryName;
	}

    public float getSpentOnCategory() {
        return mSpentOnCategory;
    }

    public float getRealProgress() {
        return mRealProgress;
    }

    public float getBarProgress() {
        return mBarProgress;
    }
}
