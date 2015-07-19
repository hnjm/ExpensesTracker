package com.amilabs.android.expensestracker.fragments.adapters;

import com.amilabs.android.expensestracker.R;
import com.amilabs.android.expensestracker.database.Data;
import com.amilabs.android.expensestracker.interfaces.Constants;
import com.amilabs.android.expensestracker.utils.SharedPref;
import com.amilabs.android.expensestracker.utils.Utils;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Date;

public class PlannerListAdapter extends CursorAdapter implements Constants {

    private static final String TAG = "PlannerListAdapter";

    //private Map<ProgressBar, ProgressBarAnimation> mProgressBarAnimationSet = new HashMap<ProgressBar, ProgressBarAnimation>();

    public PlannerListAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.fragment_planner_list_item, parent, false);
        PlannerViewHolder.setTag(v);
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        PlannerViewHolder holder = (PlannerViewHolder) view.getTag();
        float limit = cursor.getFloat(cursor.getColumnIndex(Data.Categories.LIMIT));
        String period = cursor.getString(cursor.getColumnIndex(Data.Categories.PERIOD));
        float spent = cursor.getFloat(4);
        float calculatedLimit = calculateLimit(context, period, limit);
        int percent = (int) (spent * 100 / calculatedLimit);
        holder.tvCategory.setTextColor(context.getResources().getColor(percent > 100 ? R.color.holo_red_dark : R.color.black));
        holder.tvCategory.setText(cursor.getString(cursor.getColumnIndex(Data.Categories.NAME)));
        holder.tvCategoryPercent.setTextColor(context.getResources().getColor(percent > 100 ? R.color.holo_red_dark : R.color.black));
        holder.tvLimit.setText(Utils.getFormatted(calculatedLimit) + " " + SharedPref.getCurrency(context));
        holder.tvSpent.setText(Utils.getFormatted(spent) + " " + SharedPref.getCurrency(context));
        int progress = (int) (spent * 100 / calculatedLimit);
        if (progress > 100)
            progress = 100;
        holder.bar.setProgress(progress);
        holder.tvCategoryPercent.setText("(" + Utils.getFormattedPercent(progress) + "%)");
        
        /*if (!mProgressBarAnimationSet.containsKey(holder.bar)) {
            ProgressBarAnimation anim = new ProgressBarAnimation(holder, 0, progress);
            mProgressBarAnimationSet.put(holder.bar, anim);
            anim.setDuration(1000);
            holder.bar.startAnimation(anim);
        } else
            holder.bar.setProgress(progress);*/
    }

    public String getCategory(int position) {
        return getCursor().getString(1);
    }
    
    private float calculateLimit(Context context, String period, float limit) {
        Date dateFrom = Utils.getDate(SharedPref.getDateFrom(context, TrackerType.PLANNER));
        Date dateTo = Utils.getDate(SharedPref.getDateTo(context, TrackerType.PLANNER));
        long timeFrom = dateFrom.getTime();
        long timeTo = dateTo.getTime();
        long oneDay = 1000 * 60 * 60 * 24;
        int days = (int) (Math.abs(timeTo - timeFrom) / oneDay) + 1;
        int daysInPeriod = 0;
        if (WEEK.equalsIgnoreCase(period))
            daysInPeriod = 7;
        else if (MONTH.equalsIgnoreCase(period))
            daysInPeriod = 30;
        else if (YEAR.equalsIgnoreCase(period))
            daysInPeriod = 365;
        return ((float) limit * days / daysInPeriod);
    }

    public static class PlannerViewHolder {
        
        private ProgressBar bar;
        private TextView tvCategory, tvCategoryPercent, tvLimit, tvSpent;

        public PlannerViewHolder(View v) {
            tvCategory = (TextView) v.findViewById(R.id.category_name);
            tvCategoryPercent = (TextView) v.findViewById(R.id.category_percent);
            tvLimit = (TextView) v.findViewById(R.id.category_limit_value);
            tvSpent = (TextView) v.findViewById(R.id.category_spent_value);
            bar = (ProgressBar) v.findViewById(R.id.category_bar);
        }

        static void setTag(View v) {
            PlannerViewHolder tag = new PlannerViewHolder(v);
            v.setTag(tag);
        }
    }

    public static class ProgressBarAnimation extends Animation {
        
        private PlannerViewHolder holder;
        private float from;
        private float to;

        public ProgressBarAnimation(PlannerViewHolder holder, float from, float to) {
            super();
            this.holder = holder;
            this.from = from;
            this.to = to;
        }

        public void setProgress(PlannerViewHolder holder, int value) {
            holder.tvCategoryPercent.setText("(" + value + "%)");
        }
        
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            float value = from + (to - from) * interpolatedTime;
            holder.bar.setProgress((int) value);
            holder.tvCategoryPercent.setText("(" + Utils.getFormattedPercent(value) + "%)");
        }

    }
}
