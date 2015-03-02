package com.customlbs.android.presentation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.customlbs.android.R;
import com.customlbs.library.IndoorsException;
import com.customlbs.library.IndoorsFactory;
import com.customlbs.library.callbacks.ImportedBuildingCallback;
import com.customlbs.library.callbacks.IndoorsServiceCallback;
import com.customlbs.library.callbacks.LoadingBuildingCallback;
import com.customlbs.library.callbacks.OnlineBuildingCallback;
import com.customlbs.library.model.Building;

/**
 * ListFragment displaying imported buildings by the client and buildings from
 * indoo.rs server.
 * 
 * @author customLBS | Philipp Koenig
 * 
 */
public class BuildingSelectionFragment extends ListFragment {

    private OnBuildingSelectedListener mListener;
    private MatrixCursor buildingsCursor;
    private SimpleCursorAdapter cursorAdapter;
    private Map<Long, Building> uniqueBuildings = new HashMap<Long, Building>();

    private boolean connected;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	View view = inflater.inflate(R.layout.fragment_generic_list, container, false);

	TextView title = (TextView) view.findViewById(R.id.generic_list_title);
	if (title != null)
	    title.setText(getString(R.string.select_building_cond));

	return view;
    }

    @Override
    public void onStart() {
	super.onStart();
	IndoorsFactory.createInstance(getActivity(), LoadScreenActivity.getApiKey(getActivity()),
		serviceCallback, IndoorsViewer.DEBUG);
    }

    @Override
    public void onStop() {
	super.onStop();
	IndoorsFactory.releaseInstance(serviceCallback);
    }

    private IndoorsServiceCallback serviceCallback = new IndoorsServiceCallback() {

	@Override
	public void onError(IndoorsException arg0) {
	}

	@Override
	public void connected() {
	    IndoorsFactory.getInstance().addMapDirectory(
		    new File(Environment.getExternalStorageDirectory(), "indoors"));

	    connected = true;
	    updateView();
	}
    };

    /**
     * sends requests to the service to get imported buildings and buildings
     * from server
     */
    public void updateView() {
	if (!connected) {
	    return;
	}

	getView().findViewById(R.id.progressbar).setVisibility(View.VISIBLE);

	buildingsCursor = new MatrixCursor(new String[] { "_id", "name", "desc" });

	cursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.custom_list_row_2_items,
		buildingsCursor, new String[] { "name", "desc" }, new int[] { android.R.id.text1,
			android.R.id.text2 });

	getListView().setTextFilterEnabled(true);
	getListView().setOnItemClickListener(new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		buildingsCursor.moveToPosition(position);
		long buildingId = buildingsCursor.getLong(0);
		Building selBuilding = uniqueBuildings.get(buildingId);

		// start loading progress dialog
		final ProgressDialog progressDialog;
		progressDialog = new ProgressDialog(getActivity());
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setMessage("Downloading " + selBuilding.getName());
		progressDialog.setCancelable(true);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.show();
		// download map
		IndoorsFactory.getInstance().getBuilding(selBuilding,
			new LoadingBuildingCallback() {

			    public void loadingBuilding(int progress) {
				progressDialog.setProgress(progress);
			    }

			    public void buildingLoaded(Building building) {
				// this may be called if the fragment is
				// already destroyed (user pressed back key)

				if (!isAdded())
				    return;

				if (progressDialog.isShowing()) {
				    progressDialog.dismiss();
				}
				mListener.onBuildingSelected(building);
				// on tablet update list, on mobile the
				// activity is already in progress being
				// finished
				if (!getActivity().isFinishing())
				    updateView();
			    };

			    @Override
			    public void onError(IndoorsException error) {
				// this may be called if the fragment is
				// already destroyed (user pressed back key)
				if (!isAdded())
				    return;

				// remove progress dialog
				if (progressDialog.isShowing() && !getActivity().isFinishing()) {
				    progressDialog.dismiss();
				}
				// show error message
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setMessage(error.getMessage() + ". Please try again!")
					.setTitle("Download failed").setNeutralButton("Ok", null);
				AlertDialog alert = builder.create();
				alert.show();
			    }
			});
	    }
	});

	setListAdapter(cursorAdapter);

	// downloading list of buildings on the server is only triggered AFTER
	// loading imported buildings, because cached buildings override
	// buildings on the server with the same id
	IndoorsFactory.getInstance().getImportedBuildings(importedListener);
    }

    /**
     * Callback implemented by activity holding this fragment to handle building
     * switch.
     * 
     * @author customLBS | Philipp Koenig
     * 
     */
    public interface OnBuildingSelectedListener {
	/**
	 * if null, autoselect was selected
	 * 
	 * @param building
	 */
	public void onBuildingSelected(Building building);
    }

    @Override
    public void onAttach(Activity activity) {
	super.onAttach(activity);
	try {
	    mListener = (OnBuildingSelectedListener) activity;
	} catch (ClassCastException e) {
	    throw new ClassCastException(activity.toString()
		    + " must implement OnBuildingSelectedListener");
	}
    }

    /** indoors listener waiting for buildings sent by service */
    private ImportedBuildingCallback importedListener = new ImportedBuildingCallback() {
	@Override
	public void setImportedBuildings(ArrayList<Building> buildings) {
	    // this may be called if the fragment is already
	    // destroyed (user pressed back key)
	    if (!isAdded())
		return;

	    for (Building building : buildings) {
		buildingsCursor.addRow(new Object[] { building.getId(), building.getName(),
			"Cached - " + building.getId() });

		uniqueBuildings.put(building.getId(), building);
	    }
	    cursorAdapter.notifyDataSetChanged();

	    IndoorsFactory.getInstance().getOnlineBuildings(onlineListener);
	}
    };
    private OnlineBuildingCallback onlineListener = new OnlineBuildingCallback() {
	@Override
	public void setOnlineBuildings(ArrayList<Building> buildings) {
	    // this may be called if the fragment is already
	    // destroyed (user pressed back key)
	    if (!isAdded()) {
		return;
	    }

	    getView().findViewById(R.id.progressbar).setVisibility(View.GONE);

	    for (Building building : buildings) {
		if (uniqueBuildings.containsKey(building.getId())) {
		    continue;
		}

		uniqueBuildings.put(building.getId(), building);

		buildingsCursor.addRow(new Object[] { building.getId(), building.getName(),
			"Online - " + building.getId() });
	    }
	    cursorAdapter.notifyDataSetChanged();
	};
    };

}
