package com.customlbs.android.presentation;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Toast;

import com.customlbs.android.R;
import com.customlbs.library.ErrorCode;
import com.customlbs.library.IndoorsException;
import com.customlbs.library.IndoorsLocationAdapter;
import com.customlbs.library.callbacks.RoutingCallback;
import com.customlbs.library.model.Building;
import com.customlbs.shared.Coordinate;
import com.customlbs.surface.library.IndoorsSurfaceFragment;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MyUserInteractionListener extends IndoorsLocationAdapter {

    private IndoorsViewer indoorsViewer;
    private ProgressDialog waitForBuildingLoadedDialog;

    public MyUserInteractionListener(IndoorsViewer indoorsViewer) {
	this.indoorsViewer = indoorsViewer;
    }

    @Override
    public void positionUpdated(Coordinate c, int accuracy) {
	// we are not in an inbound zone or we are in a dead zone in the
	// current floor
	if (c.x == Integer.MAX_VALUE || c.y == Integer.MAX_VALUE) {
	    showCrouton("Weak signal! We can't reliably locate you at the moment.", Style.INFO);
	}

	// updateRoute(c);
    }

    /**
     * use original routing path for PathSnapper, but show updated routing path
     * to the user every time a new position is calculated
     */
    private void updateRoute(Coordinate c) {
	List<Coordinate> routingPath = indoorsViewer.getIndoorsSurfaceFragment().getSurfaceState()
		.getRoutingPath();
	if (routingPath != null) {
	    Coordinate destination = routingPath.get(routingPath.size() - 1);

	    indoorsViewer.getIndoorsSurfaceFragment().getIndoors()
		    .getRouteAToB(c, destination, new RoutingCallback() {

			@Override
			public void onError(IndoorsException arg0) {
			    // ignore. we will try to find a new route as soon
			    // as there's a new position
			}

			@Override
			public void setRoute(ArrayList<Coordinate> arg0) {
			    IndoorsSurfaceFragment fragment = indoorsViewer
				    .getIndoorsSurfaceFragment();
			    fragment.getSurfaceState().setRoutingPath(arg0);
			    fragment.getIndoorsSurface().updatePainter();
			}
		    });
	}
    }

    private void showCrouton(String text, de.keyboardsurfer.android.widget.crouton.Style style) {
	final Crouton crouton = Crouton.makeText(indoorsViewer, text, style);
	crouton.setOnClickListener(new View.OnClickListener() {

	    @Override
	    public void onClick(View v) {
		crouton.cancel();
	    }
	});
	crouton.show();
    }

    @Override
    public void orientationUpdated(float orientation) {
    }

    @Override
    public void candidateSpread(ArrayList<Coordinate> candidateCoordinates) {
    }

    @Override
    public void changedFloor(int floorLevel, String name) {
	// on floor max value we show a dialog that we have no signal
	if (floorLevel == Integer.MAX_VALUE) {
	    showCrouton("Weak signal! We can't reliably locate you at the moment.", Style.INFO);
	}
    }

    @Override
    public void loadingBuilding(int progress) {
	waitForBuildingLoadedDialog = new ProgressDialog(indoorsViewer);
	waitForBuildingLoadedDialog.setMessage("Loading building ...");
	waitForBuildingLoadedDialog.setCancelable(false);
	try {
	    waitForBuildingLoadedDialog.show();
	} catch (Exception e) {
	    // exception if called after activity finished
	}
    }

    @Override
    public void buildingLoaded(Building building) {
	if (waitForBuildingLoadedDialog != null && waitForBuildingLoadedDialog.isShowing())
	    try {
		waitForBuildingLoadedDialog.dismiss();
	    } catch (IllegalArgumentException e) {
		// may be throw if activity already killed with back key, when
		// this listener is called
		return;
	    }

	// remove routing fragment if there is one
	final DialogFragment oldRoutingDialog = (DialogFragment) indoorsViewer
		.getSupportFragmentManager().findFragmentByTag(
			RoutingViewFragment.ROUTING_FRAGMENT_TAG);
	if (oldRoutingDialog != null) {
	    oldRoutingDialog.dismiss();
	}

	// Print selection to screen.
	String message = new StringBuilder(indoorsViewer.getString(R.string.building)).append(" '")
		.append(building.getName()).append("' ")
		.append(indoorsViewer.getString(R.string.selected)).toString();
	Toast.makeText(indoorsViewer, message, Toast.LENGTH_SHORT).show();

	indoorsViewer.updateFloorList(building);
	indoorsViewer.checkVisibilityOfMenuItems();
    }

    @Override
    public void onError(IndoorsException e) {
	if (e.getErrorCode() != null
		&& e.getErrorCode() == ErrorCode.LOCATING_DEVICE_NOT_CALIBRATED) {
	} else {
	    // since we have no precise error messages we dismiss the modal
	    // dialog
	    if (waitForBuildingLoadedDialog != null && waitForBuildingLoadedDialog.isShowing())
		waitForBuildingLoadedDialog.dismiss();
	}
    }
}
