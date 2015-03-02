package com.customlbs.android.presentation;

import android.support.v4.app.Fragment;
import android.view.View;

public class FragmentCloseListener implements View.OnClickListener {
    private final Fragment fragment;

    public FragmentCloseListener(Fragment fragment) {
	this.fragment = fragment;
    }

    @Override
    public void onClick(View v) {
	fragment.getView().setVisibility(View.GONE);
    }
}
