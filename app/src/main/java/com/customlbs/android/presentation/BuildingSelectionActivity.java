package com.customlbs.android.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.customlbs.android.R;
import com.customlbs.android.presentation.BuildingSelectionFragment.OnBuildingSelectedListener;
import com.customlbs.library.model.Building;

/**
 * Activity holding BuildingSelectionFragment used on devices where floor
 * fragment could not be displayed on main view.
 * 
 * @author customLBS | Philipp Koenig
 * 
 */
public class BuildingSelectionActivity extends FragmentActivity implements
	OnBuildingSelectedListener {

    /**
     * intents sent back to the viewer activity if building was selected
     */
    public static final String BUILDING_DATA = "com.customlbs.android.building";

    private BuildingSelectionFragment buildingSelectionFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_buildings);

	buildingSelectionFragment = (BuildingSelectionFragment) getSupportFragmentManager()
		.findFragmentById(R.id.building_fragment_in_activity);
	buildingSelectionFragment.updateView();
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
    public void onBuildingSelected(Building building) {
	Intent data = new Intent();
	data.putExtra(BUILDING_DATA, building);
	setResult(RESULT_OK, data);
	finish();
    }
}
