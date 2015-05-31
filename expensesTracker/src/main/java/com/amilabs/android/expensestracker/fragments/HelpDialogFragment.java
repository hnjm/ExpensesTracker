package com.amilabs.android.expensestracker.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.amilabs.android.expensestracker.R;

public class HelpDialogFragment extends DialogFragment {

    public static String TAG = "HelpDialogFragment";

    private AlertDialog mDialog;
    private static String mTitle;
    private static String mMessage;

    public static HelpDialogFragment newInstance(String title, String msg) {
        mTitle = title;
        mMessage = msg;
        return new HelpDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Activity ctx = getActivity();
        mDialog = new AlertDialog.Builder(ctx)
                .setTitle("[" + getActivity().getString(R.string.help) +"] " + mTitle)
                .setMessage(mMessage)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                }).create();
        return mDialog;
    }

}
