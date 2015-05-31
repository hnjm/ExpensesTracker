package com.amilabs.android.expensestracker.fragments;

import com.amilabs.android.expensestracker.interfaces.Constants;
import com.amilabs.android.expensestracker.interfaces.OnDateSelectedListener;
import com.amilabs.android.expensestracker.utils.SharedPref;
import com.amilabs.android.expensestracker.utils.Utils;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.Toast;

public class DatePickerFragment extends DialogFragment implements OnDateSetListener, Constants {

    OnDateSelectedListener mListener;
    private int year;
    private int month;
    private int day;
    private boolean isFromBtn;
    private TrackerType trackerType;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnDateSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnDateSelectedListener");
        }
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true);
        final Calendar c = Calendar.getInstance();
        Bundle bundle = getArguments();
        c.setTime(Utils.getDate(bundle.getString("date")));
        String type = bundle.getString("trackerType");
        if (type.equals("NONE"))
            trackerType = TrackerType.NONE;
        else if (type.equals("PLANNER"))
            trackerType = TrackerType.PLANNER;
        else if (type.equals("DISTRIBUTION"))
            trackerType = TrackerType.DISTRIBUTION;
        isFromBtn = bundle.getBoolean("isFromBtn");
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_NEGATIVE) {
                    mListener.onDialogDestroyed();
                    dialog.dismiss();
                }
            }
        });
        return dialog;
    }
    
    @Override
    public void onCancel(DialogInterface dialog) {
        mListener.onDialogDestroyed();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
    	this.year = year;
    	this.month = month;
    	this.day = day;
    	Calendar c = Calendar.getInstance();
    	c.set(Calendar.DAY_OF_MONTH, day);
    	c.set(Calendar.MONTH, month);
    	c.set(Calendar.YEAR, this.year);
    	long date = c.getTime().getTime();
        if (trackerType != TrackerType.NONE) {
            if (isFromBtn) {
                long dateTo = SharedPref.getDateTo(view.getContext(), trackerType);
                if (date > dateTo) {
                    date = SharedPref.getDateFrom(view.getContext(), trackerType);
                    Toast.makeText(view.getContext(), "Start date should be less than end date", Toast.LENGTH_SHORT).show();
                }
            } else {
                long dateFrom = SharedPref.getDateFrom(view.getContext(), trackerType);
                if (date <= dateFrom) {
                    date = SharedPref.getDateTo(view.getContext(), trackerType);
                    Toast.makeText(view.getContext(), "End date should be bigger than start date", Toast.LENGTH_SHORT).show();
                }
            }
        }
    	mListener.onDateSelected(date);
        mListener.onDialogDestroyed();
    }
}
