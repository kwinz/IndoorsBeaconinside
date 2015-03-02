package com.customlbs.android.presentation.legal;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class LicenseListFragment extends ListFragment {

    private ArrayList<String> licenses;
    private LicenseCallback callback;

    @Override
    public void onAttach(Activity activity) {
	super.onAttach(activity);

	if (activity instanceof LicenseCallback) {
	    callback = (LicenseCallback) activity;
	} else {
	    throw new RuntimeException("Activity has to implement LicenseCallback");
	}
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	licenses = new ArrayList<String>(LegalNotesActivity.LICENSES.keySet());

	ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
		android.R.layout.simple_list_item_1, licenses);

	setListAdapter(adapter);

	getActivity().setTitle("Licenses");
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
	super.onListItemClick(l, v, position, id);

	String licenseName = licenses.get(position);
	String licensePath = LegalNotesActivity.LICENSES.get(licenseName);
	callback.onLicenseClicked(licenseName, licensePath);
    }

    interface LicenseCallback {
	public void onLicenseClicked(String licenseName, String licensePath);
    }
}
