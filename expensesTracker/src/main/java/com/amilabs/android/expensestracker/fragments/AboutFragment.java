package com.amilabs.android.expensestracker.fragments;

import com.amilabs.android.expensestracker.R;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class AboutFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);
        Button btn = ((Button) rootView.findViewById(R.id.btn_rate_it));
        btn.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                startActivity(Intent.createChooser(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.amilabs.android.expensestracker")), "Select"));
            }
        });
        getActivity().setTitle(getString(R.string.about));
        return rootView;
    }

}
