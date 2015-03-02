package com.customlbs.android.presentation;

import java.util.ArrayList;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils.TruncateAt;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.customlbs.android.R;
import com.customlbs.android.presentation.BuildingSelectionFragment.OnBuildingSelectedListener;
import com.customlbs.android.presentation.FloorSelectionFragment.OnFloorSelectedListener;
import com.customlbs.android.presentation.ZoneSelectionFragment.ZoneSelectionListener;
import com.customlbs.android.presentation.legal.LegalNotesActivity;
import com.customlbs.android.util.IndoorsCrittercism;
import com.customlbs.coordinates.GeoCoordinate;
import com.customlbs.library.IndoorsFactory;
import com.customlbs.library.LocalizationParameters;
import com.customlbs.library.model.Building;
import com.customlbs.library.model.Floor;
import com.customlbs.library.model.Zone;
import com.customlbs.service.Configuration;
import com.customlbs.shared.Coordinate;
import com.customlbs.surface.library.IndoorsSurface;
import com.customlbs.surface.library.IndoorsSurfaceFactory;
import com.customlbs.surface.library.IndoorsSurfaceFragment;
import com.customlbs.surface.library.IndoorsSurfaceQuickAction;
import com.customlbs.surface.library.SurfaceState;
import com.customlbs.surface.library.ViewMode;
import com.example.android.actionbarcompat.ActionBarActivity;

import de.keyboardsurfer.android.widget.crouton.Crouton;

/**
 * IndoorsViewer is the main Activity of the IndoorsViewer application.
 * 
 * @author customLBS
 * 
 */
public final class IndoorsViewer extends ActionBarActivity implements OnBuildingSelectedListener,
	OnFloorSelectedListener, ZoneSelectionListener {

    protected static final boolean DEBUG = !Configuration.RELEASE_BUILD;

    private static final boolean ENABLE_EVALUATION_MODE = false;
    // if you want to load a map from assets/ folder set this to its ID
    // set to null if you don't want this at all (e.g. in production)!
    private static final Long LOAD_BUILDING_ID = null;

    // activity result request codes
    private static final int REQUEST_CODE_BUILDING = 0;
    private static final int REQUEST_CODE_ZONE = 1;

    // shared preferences identifier
    private static final String PREFERENCE_DEVICENAME = "indoors.devicename";

    private IndoorsSurfaceFragment indoorsSurfaceFragment;
    private IndoorsSurface indoorsSurface;
    private SurfaceState surfaceState;

    private BuildingSelectionFragment buildingSelectionFragment;
    private FloorSelectionFragment floorSelectionFragment;
    private ZoneSelectionFragment zoneSelectionFragment;
    private ZoomButtonsFragment zoomButtonsFragment;
    private CurrentPositionButtonFragment currentPositionFragment;
    private DebugFragment debugFragment;

    private LocalizationParameters parameters;
    private String username;

    // Menu as instance Variable for editing it when problems occur or similar
    private Menu menu;

    private IndoorsSurfaceQuickAction routingQuickAction;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	IndoorsCrittercism.start(this);

	setContentView(R.layout.activity_viewer);
	setTitle("");

	parameters = new LocalizationParameters();

	FragmentManager fragmentManager = getSupportFragmentManager();
	if (savedInstanceState == null) {
	    IndoorsFactory.Builder indoorsBuilder = new IndoorsFactory.Builder();
	    IndoorsSurfaceFactory.Builder surfaceBuilder = IndoorsSurfaceResourceFactory
		    .fromXml(getResources());

	    surfaceBuilder.setIndoorsBuilder(indoorsBuilder);

	    // disable debug in LoadScreenActivity too - won't work otherwise
	    indoorsBuilder.setDebug(DEBUG);
	    indoorsBuilder.setContext(this);
	    indoorsBuilder.setUsername(getUserName());
	    indoorsBuilder.setMapDirectory("indoors");
	    indoorsBuilder.setBuildingId(LOAD_BUILDING_ID);
	    indoorsBuilder.setLocalizationParameters(parameters);
	    indoorsBuilder.setEvaluationMode(ENABLE_EVALUATION_MODE);
	    indoorsBuilder.setApiKey(LoadScreenActivity.getApiKey(this));
	    indoorsBuilder.setDirectNet(getResources()
		    .getBoolean(R.bool.default_direct_net_enabled));
	    indoorsBuilder.setUserInteractionListener(new MyUserInteractionListener(this));

	    indoorsSurfaceFragment = surfaceBuilder.build();

	    FragmentTransaction transaction = fragmentManager.beginTransaction();
	    transaction.add(R.id.surfaceview_fragment, indoorsSurfaceFragment, "indoors");
	    transaction.commit();

	    if (getResources().getBoolean(R.bool.default_show_all_zones)) {
		indoorsSurfaceFragment.setViewMode(ViewMode.HIGHLIGHT_ALL_ZONES);
	    }
	    if (getResources().getBoolean(R.bool.default_zone_fencing_enabled)) {
		indoorsSurfaceFragment.setViewMode(ViewMode.HIGHLIGHT_CURRENT_ZONE);
	    }

	    // Set the default mode of the surfaceState like defined in
	    // display_options.xml
	    String defaultMode = getResources().getString(R.string.default_mode);
	    if ("DEBUG".equals(defaultMode)) {
		indoorsSurfaceFragment.setViewMode(ViewMode.DEBUG);
	    } else if ("OVERVIEW".equals(defaultMode)) {
		indoorsSurfaceFragment.setViewMode(ViewMode.OVERVIEW);
	    } else if ("LOCK_ON_ME".equals(defaultMode)) {
		indoorsSurfaceFragment.setViewMode(ViewMode.LOCK_ON_ME);
	    }

	    currentPositionFragment = new CurrentPositionButtonFragment();
	    transaction = fragmentManager.beginTransaction();
	    transaction.add(R.id.currentpositionbutton_fragment, currentPositionFragment,
		    "indoorsCurrentPositionButton");

	    floorSelectionFragment = new FloorSelectionFragment();
	    transaction.add(R.id.floors_fragment, floorSelectionFragment, "indoorsFloors");

	    zoomButtonsFragment = new ZoomButtonsFragment();
	    transaction.add(R.id.zoombuttons_fragment, zoomButtonsFragment, "indoorsZoomButtons");
	    transaction.commit();
	} else {
	    indoorsSurfaceFragment = (IndoorsSurfaceFragment) getSupportFragmentManager()
		    .findFragmentById(R.id.surfaceview_fragment);
	    floorSelectionFragment = (FloorSelectionFragment) getSupportFragmentManager()
		    .findFragmentById(R.id.floors_fragment);
	    zoomButtonsFragment = (ZoomButtonsFragment) getSupportFragmentManager()
		    .findFragmentById(R.id.zoombuttons_fragment);
	    currentPositionFragment = (CurrentPositionButtonFragment) getSupportFragmentManager()
		    .findFragmentById(R.id.currentpositionbutton_fragment);
	}

	// TODO: dead code? they are never added to the activity
	buildingSelectionFragment = (BuildingSelectionFragment) getSupportFragmentManager()
		.findFragmentById(R.id.list_buildings_fragment);
	zoneSelectionFragment = (ZoneSelectionFragment) getSupportFragmentManager()
		.findFragmentById(R.id.select_zone_fragment);

	indoorsSurface = indoorsSurfaceFragment.getIndoorsSurface();
	surfaceState = indoorsSurfaceFragment.getSurfaceState();

	// Setting the fragments on the tablet invisible if necessary
	if ((!getResources().getBoolean(R.bool.display_select_building))
		&& buildingSelectionFragment != null) {
	    buildingSelectionFragment.getView().setVisibility(View.GONE);
	}
	if ((!getResources().getBoolean(R.bool.display_select_floor))
		&& floorSelectionFragment != null) {
	    floorSelectionFragment.getView().setVisibility(View.GONE);
	}

	if (buildingSelectionFragment != null) {
	    // set listener for close button
	    buildingSelectionFragment.getView().findViewById(R.id.close_button)
		    .setOnClickListener(new FragmentCloseListener(buildingSelectionFragment));

	    // if building list fragment is visible update it
	    buildingSelectionFragment.updateView();
	}

	if (zoneSelectionFragment != null) {
	    zoneSelectionFragment.getView().setVisibility(View.GONE);
	    // set listener for close button
	    zoneSelectionFragment.getView().findViewById(R.id.close_button)
		    .setOnClickListener(new FragmentCloseListener(zoneSelectionFragment));
	}
    }

    @Override
    protected void onStart() {
	super.onStart();

	routingQuickAction = new IndoorsSurfaceQuickAction(this, indoorsSurfaceFragment);
	routingQuickAction.initialize("Route here",
		new IndoorsSurfaceQuickAction.QuickActionOnClickListener() {

		    @Override
		    public void onClick(Coordinate c) {
			startRoutingFragment(c);
		    }
		}, R.layout.quickaction, R.id.quick_text, true);
    }

    @Override
    protected void onStop() {
	super.onStop();

	routingQuickAction.remove();
    }

    @Override
    protected void onDestroy() {
	super.onDestroy();

	// the developer of the library recommends to do this at onDestroy
	Crouton.cancelAllCroutons();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
	getMenuInflater().inflate(R.menu.main_menu, menu);

	this.menu = menu;

	checkVisibilityOfMenuItems();
	return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
	switch (item.getItemId()) {
	case R.id.menu_select_building:
	    // if list fragment is not visible (see different layouts) start a
	    // new activity and show the list fragment
	    if (buildingSelectionFragment == null) {
		startActivityForResult(new Intent(this, BuildingSelectionActivity.class),
			REQUEST_CODE_BUILDING);
	    } else {
		// Setting the Fragment visible
		buildingSelectionFragment.getView().setVisibility(View.VISIBLE);
	    }
	    return true;
	case R.id.menu_direct_net:
	    startActivity(new Intent(this, MeasurementActivity.class));

	    return true;
	case R.id.menu_debug_mode:
	    item.setChecked(!item.isChecked());
	    refreshViewMode();

	    checkVisibilityOfMenuItems();

	    return true;
	case R.id.menu_legalnotes:
	case android.R.id.home:
	    startActivity(new Intent(this, LegalNotesActivity.class));
	    return true;
	case R.id.menu_select_route:
	    startRoutingFragment(null);
	    return true;
	case R.id.menu_overview_map:
	    boolean overViewEnabled = !item.isChecked();
	    item.setChecked(overViewEnabled);

	    if (overViewEnabled) {
		zoomButtonsFragment.getView().setVisibility(View.GONE);
	    } else {
		zoomButtonsFragment.getView().setVisibility(View.VISIBLE);
	    }

	    refreshViewMode();
	    return true;
	case R.id.menu_lock_on_me:
	    item.setChecked(!item.isChecked());
	    refreshViewMode();
	    return true;
	case R.id.menu_search_invoke:
	    AlertDialog.Builder searchAlert = new AlertDialog.Builder(this);

	    final EditText searchText = new EditText(this);
	    searchText.setEllipsize(TruncateAt.END);
	    searchText.setSingleLine();
	    searchText.requestFocus();

	    searchAlert.setView(searchText);
	    searchAlert.setTitle("Zone name:");
	    searchAlert.setPositiveButton(android.R.string.ok,
		    new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			    searchZones(searchText.getText().toString());
			}
		    });

	    searchAlert.setNegativeButton(R.string.cancel, null);
	    final AlertDialog dialog = searchAlert.create();

	    searchText.setOnEditorActionListener(new OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		    if (actionId == EditorInfo.IME_ACTION_DONE) {
			searchZones(searchText.getText().toString());
			dialog.dismiss();
		    }
		    return false;
		}
	    });
	    dialog.show();
	    return true;
	case R.id.menu_show_all_zones:
	    item.setChecked(!item.isChecked());
	    refreshViewMode();
	    return true;
	case R.id.menu_zone_fencing:
	    item.setChecked(!item.isChecked());
	    refreshViewMode();
	    return true;
	case R.id.menu_googlemaps:
	    GeoCoordinate currentUserGpsPosition = indoorsSurfaceFragment
		    .getCurrentUserGpsPosition();

	    if (currentUserGpsPosition != null) {
		startActivity(new Intent(Intent.ACTION_VIEW,
			Uri.parse("http://maps.google.com/maps?q=loc:"
				+ currentUserGpsPosition.getLatitude() + ","
				+ currentUserGpsPosition.getLongitude() + " (" + "You" + ")")));
	    }
	    return true;
	case R.id.menu_settings: {
	    FragmentManager fragmentManager = getSupportFragmentManager();
	    FragmentTransaction transaction = fragmentManager.beginTransaction();
	    transaction.hide(indoorsSurfaceFragment);
	    if (currentPositionFragment != null) {
		transaction.hide(currentPositionFragment);
	    }
	    if (floorSelectionFragment != null) {
		transaction.hide(floorSelectionFragment);
	    }
	    if (zoneSelectionFragment != null) {
		transaction.hide(zoneSelectionFragment);
	    }
	    if (buildingSelectionFragment != null) {
		transaction.hide(buildingSelectionFragment);
	    }
	    if (zoomButtonsFragment != null) {
		transaction.hide(zoomButtonsFragment);
	    }
	    if (fragmentManager.findFragmentByTag("indoors_debug") == null) {
		debugFragment = new DebugFragment();
		transaction.add(R.id.surfaceview_fragment, debugFragment, "indoors_debug");
		transaction.addToBackStack("indoors_debug");
	    }
	    transaction.commit();

	    return true;
	}
	default:
	    return super.onOptionsItemSelected(item);
	}
    }

    private boolean isDebugEnabled() {
	MenuItem debugItem = menu.findItem(R.id.menu_debug_mode);
	return debugItem.isChecked();
    }

    private void refreshViewMode() {
	indoorsSurfaceFragment.setViewMode(ViewMode.DEFAULT);

	if (isDebugEnabled()) {
	    indoorsSurfaceFragment.setViewMode(ViewMode.DEBUG);
	}

	MenuItem zoneFencingItem = menu.findItem(R.id.menu_zone_fencing);
	if (zoneFencingItem.isChecked()) {
	    indoorsSurfaceFragment.setViewMode(ViewMode.HIGHLIGHT_CURRENT_ZONE);
	}
	MenuItem allZonesItem = menu.findItem(R.id.menu_show_all_zones);
	if (allZonesItem.isChecked()) {
	    indoorsSurfaceFragment.setViewMode(ViewMode.HIGHLIGHT_ALL_ZONES);
	}
	MenuItem overviewItem = menu.findItem(R.id.menu_overview_map);
	if (overviewItem.isChecked()) {
	    indoorsSurfaceFragment.setViewMode(ViewMode.OVERVIEW);
	}
	MenuItem lockOnItem = menu.findItem(R.id.menu_lock_on_me);
	if (lockOnItem.isChecked()) {
	    indoorsSurfaceFragment.setViewMode(ViewMode.LOCK_ON_ME);
	}
    }

    private void searchZones(String query) {
	ArrayList<Zone> zones = new ArrayList<Zone>();
	// Get all zones that starts with the text entered by the
	// user
	for (Zone zone : indoorsSurfaceFragment.getZones()) {
	    if (zone.getName().toLowerCase(Locale.US).indexOf(query.toLowerCase(Locale.US)) != -1) {
		zones.add(zone);
	    }
	}
	// Only one zone found -> scroll directly to this zone
	if (zones.size() == 1) {
	    zoneSelected(zones.get(0));
	} else if (zones.size() == 0) {
	    // No zone found -> Error Message
	    Toast.makeText(IndoorsViewer.this, getString(R.string.no_zones), Toast.LENGTH_SHORT)
		    .show();
	} else {
	    // Multiple Zones found -> Display a list/fragment with
	    // all the zones, the user can choose one.
	    Building building = indoorsSurfaceFragment.getBuilding();
	    if (zoneSelectionFragment == null) {
		// On Smartphone: New Activity
		Intent intent = new Intent(IndoorsViewer.this, ZoneSelectionActivity.class);
		intent.putParcelableArrayListExtra(ZoneSelectionFragment.ZONES, zones);
		intent.putParcelableArrayListExtra(ZoneSelectionFragment.FLOORS,
			building.getFloors());
		startActivityForResult(intent, REQUEST_CODE_ZONE);
	    } else {
		// On Tablet: Fragment
		zoneSelectionFragment.getView().setVisibility(View.VISIBLE);
		zoneSelectionFragment.setDisplayedZones(zones, building.getFloors());
	    }
	}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	switch (requestCode) {
	// if list fragment is shown in new activity handle activity result
	case REQUEST_CODE_BUILDING:
	    if (resultCode != RESULT_CANCELED)
		onBuildingSelected((Building) data
			.getParcelableExtra(BuildingSelectionActivity.BUILDING_DATA));
	    break;
	case REQUEST_CODE_ZONE:
	    if (resultCode != RESULT_CANCELED) {
		Zone selectedZone = (Zone) data
			.getParcelableExtra(ZoneSelectionActivity.SELECTED_ZONE);
		zoneSelected(selectedZone);
	    }
	}
    }

    @Override
    public void onBuildingSelected(Building building) {
	if (building == null) {
	    // Setting the Floor Selection Fragment invisible if it is loaded
	    if (this.floorSelectionFragment != null)
		floorSelectionFragment.getView().setVisibility(View.GONE);
	    Toast.makeText(this, getText(R.string.automatic_building_selected), Toast.LENGTH_SHORT)
		    .show();
	    surfaceState.setBuilding(null);

	    indoorsSurface.updatePainter();
	    checkVisibilityOfMenuItems();
	} else {
	    IndoorsCrittercism.sendMetadata(LoadScreenActivity.getApiKey(this), building.getId());

	    IndoorsFactory.getInstance().setLocatedBuilding(building, parameters);
	}
    }

    @Override
    public void onFloorSelected(Floor floor) {
	String message = null;
	if (floor == null) {
	    message = getText(R.string.automatic_floor_selected).toString();
	    surfaceState.autoSelect = true;
	    if (surfaceState.lastFloorLevelSelectedByLibrary != SurfaceState.NO_FLOOR_SELECTED_BY_LIBRARY) {
		surfaceState.setFloor(surfaceState.building
			.getFloorByLevel(surfaceState.lastFloorLevelSelectedByLibrary));
	    }
	} else {
	    message = new StringBuilder(getString(R.string.floor)).append(" '")
		    .append(floor.getName()).append("' ").append(getString(R.string.selected))
		    .toString();
	    surfaceState.autoSelect = false;
	    surfaceState.setFloor(floor);
	    checkVisibilityOfMenuItems();
	}
	// Print selection to screen.
	Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void updateFloorList(Building building) {
	// update floor list if fragment is visible
	if (floorSelectionFragment != null)
	    floorSelectionFragment.updateFloors(building);
    }

    public IndoorsSurfaceFragment getIndoorsSurfaceFragment() {
	return indoorsSurfaceFragment;
    }

    private String getUserName() {
	if (username == null) {
	    username = getPreferences(MODE_PRIVATE).getString(PREFERENCE_DEVICENAME, null);
	}

	return username;
    }

    /**
     * Checks which Fragments are visible and sets the visibility of the
     * MenuItems in the ActionBar in order to reactivate the invisible Fragment
     * 
     * not supported on Android versions before 3.0
     */
    protected void checkVisibilityOfMenuItems() {
	if (menu == null) {
	    return;
	}

	boolean debugEnabled = isDebugEnabled();
	menu.findItem(R.id.menu_settings).setVisible(debugEnabled);

	if (indoorsSurfaceFragment.getBuilding() == null
		|| (debugFragment != null && debugFragment.isVisible())) {
	    if (floorSelectionFragment != null && floorSelectionFragment.getView() != null) {
		floorSelectionFragment.getView().setVisibility(View.GONE);
	    }
	    if (zoneSelectionFragment != null) {
		zoneSelectionFragment.getView().setVisibility(View.GONE);
	    }
	    if (zoomButtonsFragment != null) {
		zoomButtonsFragment.getView().setVisibility(View.GONE);
	    }
	    if (currentPositionFragment != null) {
		currentPositionFragment.getView().setVisibility(View.GONE);
	    }
	} else {
	    if (getResources().getBoolean(R.bool.display_select_floor)) {
		if (floorSelectionFragment != null && floorSelectionFragment.getView() != null) {
		    floorSelectionFragment.getView().setVisibility(View.VISIBLE);
		}
	    }
	    if (getResources().getBoolean(R.bool.display_route_button)) {
		menu.findItem(R.id.menu_select_route).setVisible(true);
	    }
	    if (getResources().getBoolean(R.bool.display_search_zone)) {
		menu.findItem(R.id.menu_search_invoke).setVisible(true);
	    }
	    if (zoomButtonsFragment != null) {
		zoomButtonsFragment.getView().setVisibility(View.VISIBLE);
	    }
	    if (currentPositionFragment != null) {
		currentPositionFragment.getView().setVisibility(View.VISIBLE);
	    }
	}
    }

    /**
     * If the user selects a zone (on tablet in the zone fragment, on mobile in
     * the zone activity) then paint this zone, and let user decide whether to
     * scroll or route to this zone.
     */
    @Override
    public void zoneSelected(final Zone zone) {
	surfaceState.clearPermanentlyHighlightedZones();

	// paint this zone
	surfaceState.addPermanentlyHighlightedZone(zone);

	final CharSequence[] items = { "Scroll to zone", "Route to zone" };
	new AlertDialog.Builder(this).setTitle(zone.getName())
		.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
			// scroll to zone
			if (item == 0) {
			    indoorsSurface.scrollToZone(zone);
			}
			// route to zone
			else if (item == 1) {
			    int middleX = 0;
			    int middleY = 0;

			    for (Coordinate c : zone.getZonePoints()) {
				middleX += c.x;
				middleY += c.y;
			    }
			    // Calculate the center of the polygon
			    middleX /= zone.getZonePoints().size();
			    middleY /= zone.getZonePoints().size();

			    startRoutingFragment(new Coordinate(middleX, middleY, zone
				    .getFloorLevel()));
			}
		    }
		}).show();
    }

    /**
     * Delete all zones in the zone fragment and make it invisible.
     */
    public void clearZoneFragment() {
	if (zoneSelectionFragment != null) {
	    zoneSelectionFragment.setDisplayedZones(null, null);
	    zoneSelectionFragment.getView().setVisibility(View.GONE);
	}
    }

    /**
     * start routing fragment triggered by options menu or long click on surface
     */
    protected void startRoutingFragment(Coordinate mapPoint) {
	// check if any routing fragment is active
	final DialogFragment routingFragment = (DialogFragment) getSupportFragmentManager()
		.findFragmentByTag(RoutingViewFragment.ROUTING_FRAGMENT_TAG);

	if (routingFragment == null) {
	    // check if any old route is displayed
	    if (surfaceState.getRoutingPath() == null) {
		FragmentTransaction fragmentTransaction = getSupportFragmentManager()
			.beginTransaction();
		RoutingViewFragment fragment = null;
		if (mapPoint == null) {
		    fragment = new RoutingViewFragment();
		} else {
		    fragment = new RoutingViewFragment(null, null, mapPoint, mapPoint.x + " / "
			    + mapPoint.y + " / " + mapPoint.z);
		}
		fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		fragment.show(fragmentTransaction, RoutingViewFragment.ROUTING_FRAGMENT_TAG);
	    } else {
		new AlertDialog.Builder(this).setMessage("Routing already active.")
			.setPositiveButton("Stop routing", new DialogInterface.OnClickListener() {

			    @Override
			    public void onClick(DialogInterface dialog, int which) {
				surfaceState.setRoutingPath(null, true);
				// immediately repaint to delete route
				indoorsSurface.updatePainter();
			    }
			}).setNegativeButton("Cancel", null).create().show();
	    }
	}
    }

    public IndoorsSurfaceQuickAction getRoutingAction() {
	return routingQuickAction;
    }
}