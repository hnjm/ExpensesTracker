package com.amilabs.android.expensestracker.fragments.adapters;

import com.amilabs.android.expensestracker.R;
import com.amilabs.android.expensestracker.utils.SharedPref;
import com.amilabs.android.expensestracker.utils.Utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

public class DistributionListAdapter extends ArrayAdapter<DistributionListRowItem> {

    private Context mContext;
    private int[] mPieColors;
    //private Map<Integer, ProgressBarAnimation> mProgressBarAnimationSet = new HashMap<Integer, ProgressBarAnimation>();
    //private long startAnimTime;

    public DistributionListAdapter(Context context/*, List<RowItem> list*/) {
        super(context, R.layout.fragment_distribution_list_item);
        mContext = context;
        // init pie colors
        TypedArray imgs = context.getResources().obtainTypedArray(R.array.pie_colors);
        mPieColors = new int[imgs.length()];
        for (int i = 0; i < imgs.length(); i++)
        	mPieColors[i] = imgs.getResourceId(i, -1);
        imgs.recycle();
        //startAnimTime = System.currentTimeMillis();
    }

    public void setData(List<DistributionListRowItem> data) {
        clear();
        if (data != null)
            for (DistributionListRowItem item: data)
                add(item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final DistributionViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.fragment_distribution_list_item, parent, false);
            TextView tvName = (TextView) convertView.findViewById(R.id.tv_name);
            TextView tvPercent = (TextView) convertView.findViewById(R.id.tv_percent);
            TextView tvSpent = (TextView) convertView.findViewById(R.id.tv_spent_value);
            ProgressBar pbBar = (ProgressBar) convertView.findViewById(R.id.pb_bar);
            holder = new DistributionViewHolder(tvName, tvPercent, tvSpent, pbBar);
            convertView.setTag(holder);
        } else
            holder = (DistributionViewHolder) convertView.getTag();
        
        holder.tvName.setTextColor(mContext.getResources().getColor(R.color.black));
        holder.tvName.setText(getItem(position).getCategoryName());
        holder.tvPercent.setTextColor(mContext.getResources().getColor(R.color.black));
        holder.tvPercent.setText((getItem(position).getRealProgress() < 1 ? 
        		Utils.getFormatted(getItem(position).getRealProgress()) : 
        		Utils.getFormattedPercent(getItem(position).getRealProgress())) + "%");
        holder.tvSpent.setText("(" + Utils.getFormatted(getItem(position).getSpentOnCategory()) + " " + SharedPref.getCurrency(mContext) + ")");
        holder.pbBar.setProgressDrawable(mContext.getResources().getDrawable(mPieColors[position]));
        holder.pbBar.setProgress(getItem(position).getBarProgress() < 1 ?
        		1 : (int) getItem(position).getBarProgress());

        /*Log.d("MNIK", "- " + mProgressBarAnimationSet.containsKey(position));
        if (System.currentTimeMillis() - startAnimTime < 1000) {
            if (!mProgressBarAnimationSet.containsKey(position)) {
                ProgressBarAnimation anim = new ProgressBarAnimation(holder, mListItems.get(position).getRealProgress(), mListItems.get(position).getBarProgress());
                mProgressBarAnimationSet.put(position, anim);
                anim.setDuration(1000);
                holder.pbBar.startAnimation(anim);
                Log.e("MNIK", "start anim " + mListItems.get(position).getCategoryName() + " " + position);
            } else
                holder.pbBar.setProgress(mListItems.get(position).getBarProgress());
        } else
            holder.pbBar.setProgress(mListItems.get(position).getBarProgress());*/

        return convertView;
    }

    public float getSpentOnCategory(int pos) {
        return getItem(pos).getSpentOnCategory();
    }
    
    public static class DistributionViewHolder {
        
        private TextView tvName, tvPercent, tvSpent;
        private ProgressBar pbBar;

        public DistributionViewHolder(TextView tvName, TextView tvPercent, TextView tvSpent, ProgressBar pbBar) {
            this.tvName = tvName;
            this.tvPercent = tvPercent;
            this.tvSpent = tvSpent;
            this.pbBar = pbBar;
        }

    }

    public static class ProgressBarAnimation extends Animation {
        
        private DistributionViewHolder holder;
        private float realProgress;
        private float barProgress;

        public ProgressBarAnimation(DistributionViewHolder holder, float realProgress, float barProgress) {
            super();
            this.holder = holder;
            this.realProgress = realProgress;
            this.barProgress = barProgress;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            holder.pbBar.setProgress((int) (barProgress * interpolatedTime));
            holder.tvPercent.setText(Utils.getFormattedPercent(realProgress * interpolatedTime) + "%");
        }

    }
}
