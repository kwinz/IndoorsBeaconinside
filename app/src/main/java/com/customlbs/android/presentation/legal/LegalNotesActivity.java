package com.customlbs.android.presentation.legal;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.customlbs.android.presentation.legal.LicenseListFragment.LicenseCallback;

/**
 * Activity for a textview with legal notices.
 */
public class LegalNotesActivity extends FragmentActivity implements LicenseCallback {

    protected static final Map<String, String> LICENSES;

    static {
	LICENSES = new HashMap<String, String>();
	LICENSES.put("indoo.rs EULA", "EULA.txt");
	LICENSES.put("Core Dependencies", "NOTICE_SHARED.txt");
	LICENSES.put("Android Dependencies", "NOTICE_ANDROID.txt");
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	LicenseListFragment listFragment = new LicenseListFragment();

	FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
	transaction.add(android.R.id.content, listFragment);
	transaction.commit();
    }

    @Override
    public void onLicenseClicked(String licenseName, String licensePath) {
	Bundle arguments = new Bundle();
	arguments.putString(LicenseDetailFragment.EXTRA_LICENSE_NAME, licenseName);
	arguments.putString(LicenseDetailFragment.EXTRA_LICENSE_PATH, licensePath);

	LicenseDetailFragment detailFragment = new LicenseDetailFragment();
	detailFragment.setArguments(arguments);

	FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
	transaction.replace(android.R.id.content, detailFragment);
	transaction.addToBackStack(null);
	transaction.commit();
    }
}
