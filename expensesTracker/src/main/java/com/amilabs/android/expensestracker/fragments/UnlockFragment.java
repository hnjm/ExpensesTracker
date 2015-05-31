package com.amilabs.android.expensestracker.fragments;

import com.amilabs.android.expensestracker.MainActivity;
import com.amilabs.android.expensestracker.R;
import com.amilabs.android.expensestracker.interfaces.OnUpdateFragmentInterface;
import com.amilabs.android.expensestracker.utils.SharedPref;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;

public class UnlockFragment extends Fragment implements OnUpdateFragmentInterface {

    private MainActivity mActivity;
    private Button mButton;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_unlock, container, false);
        mButton = ((Button) rootView.findViewById(R.id.btn_pay));
        mButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.purchasePremium();
            }
        });
        getActivity().setTitle(getString(R.string.pro));
        if (SharedPref.isPremium(mActivity))
            disableButton();
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mActivity = (MainActivity) activity;
        } catch (ClassCastException e) {
        }
    }
    
    @Override
    public void onUpdateFragment() {
        disableButton();
    }
    
    private void disableButton() {
        mButton.setEnabled(false);
        mButton.setTextColor(getResources().getColor(android.R.color.darker_gray));
    }
}
