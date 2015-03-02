package com.customlbs.android.presentation;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.customlbs.android.R;
import com.customlbs.library.IndoorsException;
import com.customlbs.library.IndoorsFactory;
import com.customlbs.library.IndoorsLocationAdapter;
import com.customlbs.library.callbacks.RoutingCallback;
import com.customlbs.shared.Coordinate;
import com.customlbs.surface.library.IndoorsSurfaceQuickAction.QuickActionOnClickListener;
import com.customlbs.surface.library.SurfaceState;

/**
 * Dialog ui to get routing coordinates.
 * 
 * @author customLBS | Philipp Koenig
 * 
 */
@SuppressLint("ValidFragment")
public class RoutingViewFragment extends DialogFragment {

    public static final String ROUTING_FRAGMENT_TAG = "routing dialog";

    private volatile EditText startText;
    private EditText endText;

    private Coordinate start;
    private String startMsg;
    private Coordinate end;
    private String endMsg;

    // convenience references (also to prevent nullpointer in some situation)
    private IndoorsViewer indoorsViewer;
    private FragmentManager fragmentManager;

    /**
     * default constructor, no start/end position selected
     */
    public RoutingViewFragment() {
    }

    /**
     * init this routing dialog with values
     * 
     * @param start
     * @param startMsg
     * @param end
     * @param endMsg
     */
    public RoutingViewFragment(Coordinate start, String startMsg, Coordinate end, String endMsg) {
	this.start = start;
	this.startMsg = startMsg;
	this.end = end;
	this.endMsg = endMsg;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	getDialog().setTitle("Where do you want to go?");

	// convenience references
	indoorsViewer = (IndoorsViewer) getActivity();
	fragmentManager = getFragmentManager();

	View routingView = inflater.inflate(R.layout.fragment_route, container, false);

	startText = ((EditText) routingView.findViewById(R.id.route_startText));
	// init with start values
	setStart(start, startMsg);
	endText = ((EditText) routingView.findViewById(R.id.route_endText));
	// init with start values
	setEnd(end, endMsg);

	// start new point choose dialog for start position
	((ImageButton) routingView.findViewById(R.id.route_startButton))
		.setOnClickListener(new OnClickListener() {

		    @Override
		    public void onClick(View v) {
			final FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
			final DialogFragment newFragment = new PointChooseDialog(Position.START);
			fragmentTransaction
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			newFragment.show(fragmentTransaction, "point choose dialog");
		    }
		});
	// start new point choose dialog for end position
	((ImageButton) routingView.findViewById(R.id.route_endButton))
		.setOnClickListener(new OnClickListener() {

		    @Override
		    public void onClick(View v) {
			final FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
			final DialogFragment newFragment = new PointChooseDialog(Position.END);
			fragmentTransaction
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			newFragment.show(fragmentTransaction, "point choose dialog");
		    }
		});

	// send routing calculation request to service
	final Button startRoutingCalculationButton = ((Button) routingView
		.findViewById(R.id.route_startCalculation));
	startRoutingCalculationButton.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View v) {
		// start only if we have start and end point
		if (start != null && end != null) {
		    // remove routing fragment
		    final DialogFragment routingFragment = (DialogFragment) fragmentManager
			    .findFragmentByTag("routing dialog");
		    routingFragment.dismiss();

		    // create modal dialog waiting for service to calculate
		    // route, but offer button to cancel
		    final ProgressDialog waitForRouteCalculationDialog;
		    waitForRouteCalculationDialog = new ProgressDialog(getActivity());
		    waitForRouteCalculationDialog.setMessage("Calculating route ...");
		    waitForRouteCalculationDialog.setCancelable(false);
		    waitForRouteCalculationDialog.setButton(Dialog.BUTTON_NEGATIVE, "Cancel",
			    new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
				    waitForRouteCalculationDialog.dismiss();
				}
			    });
		    waitForRouteCalculationDialog.show();

		    // send route request to service
		    IndoorsFactory.getInstance().getRouteAToB(start, end, new RoutingCallback() {
			@Override
			public void setRoute(ArrayList<Coordinate> path) {
			    // if canceled with back key do nothing
			    if (waitForRouteCalculationDialog.isShowing()) {

				SurfaceState surfaceState = indoorsViewer
					.getIndoorsSurfaceFragment().getSurfaceState();

				synchronized (surfaceState) {
				    surfaceState.setRoutingPath(path, true);

				    // change back to autoselect (start
				    // position should normally be the
				    // current position)
				    surfaceState.autoSelect = true;
				    if (surfaceState.lastFloorLevelSelectedByLibrary != -1) {
					surfaceState.setFloor(surfaceState.building
						.getFloorByLevel(surfaceState.lastFloorLevelSelectedByLibrary));
				    } else {
					surfaceState.setFloor(surfaceState.building
						.getFloorByLevel(start.z));
				    }

				    indoorsViewer.getIndoorsSurfaceFragment().updateSurface();
				}
				// dismiss on success
				waitForRouteCalculationDialog.dismiss();
			    }
			}

			@Override
			public void onError(IndoorsException e) {
			    // dismiss routing calc waiting dialog on
			    // error
			    waitForRouteCalculationDialog.dismiss();
			    // show error message
			    AlertDialog.Builder builder = new AlertDialog.Builder(indoorsViewer);
			    builder.setMessage(e.getMessage()).setTitle("Routing error")
				    .setNeutralButton("Ok", null);

			    AlertDialog alert = builder.create();
			    alert.show();
			};

		    });
		}
		// user has to provide valid start and end points
		else {
		    AlertDialog.Builder builder = new AlertDialog.Builder(RoutingViewFragment.this
			    .getActivity());
		    builder.setMessage("You have to select start and end point.").setTitle("Hint")
			    .setNeutralButton("Ok", null);
		    AlertDialog alert = builder.create();
		    alert.show();
		}
	    }
	});

	final Button cancelButton = (Button) routingView.findViewById(R.id.route_cancel);
	cancelButton.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View v) {
		dismiss();
	    }
	});

	return routingView;
    }

    @Override
    public void onStart() {
	super.onStart();

	// always immediately wait for start position, if not set already (can
	// be canceled by user)
	if (start == null)
	    waitForPosition(Position.START);
    }

    @Override
    public void onDestroy() {
	super.onDestroy();
	// remove any position listener if fragment not used anymore (called if
	// dialog is popped from stack)
	indoorsViewer.getRoutingAction().removeQuickActionListener();
    }

    /**
     * set ui text and routing point
     * 
     * @param start
     * @param msg
     */
    void setStart(final Coordinate start, String msg) {
	if (start != null && msg != null) {
	    this.start = start;
	    startText.setText(msg);
	}
    }

    /**
     * set ui text and routing point
     * 
     * @param end
     * @param msg
     */
    private void setEnd(final Coordinate end, String msg) {
	if (end != null && msg != null) {
	    this.end = end;
	    endText.setText(msg);
	}
    }

    /**
     * start dialog and wait for position from service
     * 
     * @param whichPosition
     *            start or end position
     */
    private void waitForPosition(final Position whichPosition) {
	// this may be called if the fragment is already
	// destroyed (user pressed back key)
	if (!isAdded()) {
	    return;
	}
	final ProgressDialog waitForPositionDialog;
	waitForPositionDialog = new ProgressDialog(getActivity());
	waitForPositionDialog.setMessage("Waiting for position ...");
	waitForPositionDialog.setCancelable(true);
	waitForPositionDialog.setCanceledOnTouchOutside(false);

	// indoors listener to wait for position
	final IndoorsLocationAdapter listener = new IndoorsLocationAdapter() {
	    public void positionUpdated(Coordinate c, int accuracy) {
		// remove waiting dialog
		waitForPositionDialog.cancel();
		switch (whichPosition) {
		case START:
		    start = c;
		    startMsg = "Current position";
		    startText.setText(startMsg);
		    break;
		case END:
		    end = c;
		    endMsg = "Current position";
		    endText.setText(endMsg);
		    break;
		}
	    };
	};

	// this method will always be called (if user presse
	// back key or if we get a position (.cancel)
	waitForPositionDialog.setOnCancelListener(new OnCancelListener() {

	    @Override
	    public void onCancel(DialogInterface dialog) {
		IndoorsFactory.getInstance().removeLocationListener(listener);
	    }
	});

	// first show dialog
	waitForPositionDialog.show();
	// register listener to wait for position
	IndoorsFactory.getInstance().registerLocationListener(listener);
    }

    /** types for point choose dialog */
    enum Position {
	START, END
    };

    public final class PointChooseDialog extends DialogFragment {
	Position whichPosition;

	public PointChooseDialog(Position position) {
	    this.whichPosition = position;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    final CharSequence[] items = { "Current position", "Point on the map" };
	    final CharSequence[] itemsMessage = { "Getting current position",
		    "Choose a point on the map" };

	    // create 2 item dialog: 1. wait for position from service
	    // 2. get position from map
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    builder.setTitle("Choose a point");
	    builder.setItems(items, new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int item) {
		    Toast.makeText(getActivity().getApplicationContext(), itemsMessage[item],
			    Toast.LENGTH_SHORT).show();
		    // wait for position by service
		    if (item == 0) {
			waitForPosition(whichPosition);
		    }
		    // get position from map (indoorsSurface)
		    else if (item == 1) {
			// a dialog fragment can not be hidden (because it is
			// modal, this would just hide the content, leaving the
			// screen locked) so we remove the routing fragment to
			// get back to surfaceView
			final FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
			final DialogFragment oldRoutingDialog = (DialogFragment) fragmentManager
				.findFragmentByTag(ROUTING_FRAGMENT_TAG);
			if (oldRoutingDialog != null) {
			    fragmentTransaction.remove(oldRoutingDialog);
			}
			// remember transaction, so back key will bring us back
			// to previous state
			fragmentTransaction.addToBackStack(null);
			final int commit = fragmentTransaction.commit();
			// register point choose listener
			indoorsViewer.getRoutingAction().removeQuickActionListener();
			indoorsViewer.getRoutingAction().setQuickActionOnClickListener(
				"Click to choose this position!", new QuickActionOnClickListener() {

				    @Override
				    public void onClick(final Coordinate position) {
					// save choosen point
					final String msg = position.x + " / " + position.y + " / "
						+ position.z;
					switch (whichPosition) {
					case START:
					    start = position;
					    startText.setText(msg);
					    startMsg = msg;
					    break;
					case END:
					    end = position;
					    endText.setText(msg);
					    endMsg = msg;
					    break;
					}
					// remove the remove transaction (so
					// back key
					// wont make the old dialog show again)
					fragmentManager.popBackStack(commit,
						FragmentManager.POP_BACK_STACK_INCLUSIVE);
					// remove old routing dialog and create
					// new with
					// saved values
					oldRoutingDialog.dismiss();
					final FragmentTransaction fragmentTransaction = fragmentManager
						.beginTransaction();
					final RoutingViewFragment fragment = new RoutingViewFragment(
						start, startMsg, end, endMsg);
					fragmentTransaction
						.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
					fragment.show(fragmentTransaction, ROUTING_FRAGMENT_TAG);
				    }
				}, null);
		    }
		}
	    });
	    return builder.create();
	}
    }
}
