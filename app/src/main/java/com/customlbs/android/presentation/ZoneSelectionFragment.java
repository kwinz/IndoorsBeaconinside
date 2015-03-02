package com.customlbs.android.presentation;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.customlbs.android.R;
import com.customlbs.android.presentation.section.EntryAdapter;
import com.customlbs.android.presentation.section.Item;
import com.customlbs.android.presentation.section.SectionItem;
import com.customlbs.android.presentation.section.ZoneItem;
import com.customlbs.library.model.Floor;
import com.customlbs.library.model.Zone;

/**
 * ListFragment showing Zones of a map. Has a listener which is notified when
 * the user selects a zone.
 * 
 * @author Martin Kamleithner
 * 
 */
public class ZoneSelectionFragment extends ListFragment {

    private ZoneSelectionListener mListener;

    public static final String ZONES = "zones";
    public static final String FLOORS = "floors";

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
	Intent intent = getActivity().getIntent();
	List<Zone> zones = intent.getParcelableArrayListExtra(ZONES);
	ArrayList<Floor> floors = intent.getParcelableArrayListExtra(FLOORS);

	if (zones != null && floors != null)
	    setDisplayedZones(zones, floors);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	View view = inflater.inflate(R.layout.fragment_generic_list, container, false);

	TextView title = (TextView) view.findViewById(R.id.generic_list_title);
	if (title != null)
	    title.setText(getString(R.string.select_zone_cond));

	return view;
    }

    public void setDisplayedZones(List<Zone> zones, ArrayList<Floor> floors) {
	// level is key, floor is value
	SparseArray<Floor> floorz = new SparseArray<Floor>();
	if (floors != null) {
	    for (Floor floor : floors) {
		floorz.put(floor.getLevel(), floor);
	    }
	}

	// level is key, zone is level
	SparseArray<List<Zone>> zonez = new SparseArray<List<Zone>>();
	if (zones != null) {
	    for (Zone zone : zones) {
		List<Zone> temp = zonez.get(zone.getFloorLevel());
		if (temp == null) {
		    temp = new ArrayList<Zone>();

		    zonez.put(zone.getFloorLevel(), temp);
		}

		temp.add(zone);
	    }
	}

	final ArrayList<Item> items = new ArrayList<Item>();
	for (int i = 0; i < zonez.size(); i++) {
	    List<Zone> temp = zonez.valueAt(i);
	    Floor floor = floorz.get(temp.get(0).getFloorLevel());
	    String name = Integer.toString(zonez.keyAt(i));
	    if (floor.getName() != null) {
		name += " - " + floor.getName();
	    }

	    // create category
	    items.add(new SectionItem(name));
	    // add entries to category
	    for (Zone zone : temp) {
		items.add(new ZoneItem(zone));
	    }
	}

	setListAdapter(new EntryAdapter(getActivity(), items));

	getListView().setOnItemClickListener(new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Item item = items.get(position);
		if (item instanceof ZoneItem) {
		    mListener.zoneSelected(((ZoneItem) item).getZone());
		}
	    }
	});
    }

    public interface ZoneSelectionListener {
	void zoneSelected(Zone zone);
    }

    @Override
    public void onAttach(Activity activity) {
	super.onAttach(activity);

	try {
	    mListener = (ZoneSelectionListener) activity;
	} catch (ClassCastException e) {
	    throw new ClassCastException(activity.toString()
		    + " must implement ZoneSelectionListener");
	}
    }

}
