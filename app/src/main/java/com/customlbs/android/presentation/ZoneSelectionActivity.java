package com.customlbs.android.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.customlbs.android.R;
import com.customlbs.android.presentation.ZoneSelectionFragment.ZoneSelectionListener;
import com.customlbs.library.model.Zone;

/**
 * Activity holding ZoneSelectionFragment used on devices where floor fragment
 * could not be displayed on main view.
 * 
 * @author Martin Kamleithner
 * 
 */

public class ZoneSelectionActivity extends FragmentActivity implements ZoneSelectionListener {

    public static final String SELECTED_ZONE = "selectedZone";

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_zones);
    }

    @Override
    protected void onStop() {
	super.onStop();
	// kill this activity if brought to background
	// otherwise if the process changes while in the background we get an
	// error with the indoors singleton
	finish();
    }

    @Override
    public void zoneSelected(Zone zone) {
	Intent intent = new Intent();
	intent.putExtra(SELECTED_ZONE, zone);
	setResult(RESULT_OK, intent);
	finish();
    }
}
