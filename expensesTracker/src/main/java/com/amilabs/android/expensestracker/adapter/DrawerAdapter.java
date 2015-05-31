package com.amilabs.android.expensestracker.adapter;

import com.amilabs.android.expensestracker.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class DrawerAdapter extends ArrayAdapter<DrawerRowItem> {

    private Context context;
    private int currentItem;
    
    public DrawerAdapter(Context c, int resId, List<DrawerRowItem> listItems) {
        super(c, resId, listItems);
        context = c;
    }
    
    private static class ViewHolder {
        
        public TextView tvMenuName;
        public ImageView ivMenuImage;

        public ViewHolder(TextView tv, ImageView iv) {
            tvMenuName = tv;
            ivMenuImage = iv;
        }

    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.drawer_list_item, parent, false);
            TextView tvMenuName = (TextView) convertView.findViewById(R.id.main_menu_item_text);
            ImageView ivMenuImage = (ImageView) convertView.findViewById(R.id.main_menu_item_icon);
            holder = new ViewHolder(tvMenuName, ivMenuImage);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();
        DrawerRowItem rowItem = getItem(position);
        holder.tvMenuName.setText(rowItem.drawerMenuText);
        holder.ivMenuImage.setImageDrawable(rowItem.drawerMenuImage);
        if (currentItem == position)
            convertView.setBackgroundResource(R.drawable.pressed_color);
        else
            convertView.setBackgroundResource(R.color.drawer_bg);
            
        return convertView;
    }
    
    public void setChecked(int position) {
        currentItem = position;
    }
    
}
