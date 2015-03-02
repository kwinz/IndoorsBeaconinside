package com.customlbs.android.presentation;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.customlbs.android.R;
import com.customlbs.library.IndoorsException;
import com.customlbs.library.IndoorsFactory;
import com.customlbs.library.IndoorsLocationAdapter;
import com.customlbs.library.callbacks.IndoorsServiceCallback;
import com.customlbs.library.model.Building;
import com.customlbs.library.model.Floor;
import com.customlbs.surface.library.IndoorsSurfaceFragment;
import com.customlbs.surface.library.SurfaceState;
import com.customlbs.surface.library.SurfaceState.FloorChangeListener;

/**
 * ListFragment displaying available floors in current building.
 * 
 * 
 * @author customLBS | Philipp Koenig
 * 
 */
public class FloorSelectionFragment extends ListFragment {

    private OnFloorSelectedListener mFloorSelectListener;
    private final ArrayList<FloorListWrapper> data = new ArrayList<FloorListWrapper>();
    private FloorListAdapter arrayAdapter;
    private boolean connected;
    private IndoorsSurfaceFragment indoorsSurfaceFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	View view = inflater.inflate(R.layout.fragment_floors, container, false);

	TextView title = (TextView) view.findViewById(R.id.generic_list_title);
	if (title != null)
	    title.setText(getString(R.string.select_floor_cond));

	FragmentManager fragmentManager = ((FragmentActivity) getActivity()).getSupportFragmentManager();
	indoorsSurfaceFragment = (IndoorsSurfaceFragment) fragmentManager.findFragmentByTag("indoors");
	indoorsSurfaceFragment.getSurfaceState().registerFloorChangeListener(floorChangeListener);

	view.findViewById(R.id.close_button).setOnClickListener(
		(OnClickListener) new FragmentCloseListener(this));

	return view;
    }

    @Override
    public void onStart() {
	super.onStart();
	IndoorsFactory.createInstance(getActivity(), LoadScreenActivity.getApiKey(getActivity()),
		serviceCallback, IndoorsViewer.DEBUG);

	arrayAdapter = new FloorListAdapter(getActivity(), data);
	setListAdapter(arrayAdapter);
	getListView().setTextFilterEnabled(true);

	getListView().setOnItemClickListener(new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		mFloorSelectListener.onFloorSelected((arrayAdapter.getItem(position)).floor);
	    }
	});
    }

    @Override
    public void onStop() {
	super.onStop();
	if (connected) {
	    IndoorsFactory.getInstance().removeLocationListener(libraryFloorChangeListener);
	}
	IndoorsFactory.releaseInstance(serviceCallback);

    }

    private IndoorsServiceCallback serviceCallback = new IndoorsServiceCallback() {

	@Override
	public void onError(IndoorsException arg0) {
	}

	@Override
	public void connected() {
	    connected = true;
	    IndoorsFactory.getInstance().registerLocationListener(libraryFloorChangeListener);
	}
    };

    @Override
    public void onDestroyView() {
	super.onDestroyView();
	indoorsSurfaceFragment.getSurfaceState().removeFloorChangeListener(floorChangeListener);
    }

    private final FloorChangeListener floorChangeListener = new FloorChangeListener() {

	@Override
	public void floorChanged(SurfaceState surfaceState) {
	    for (FloorListWrapper wrapper : data) {
		if (wrapper.floor.getLevel() == surfaceState.currentFloor.getLevel()) {
		    wrapper.displayed = true;
		} else {
		    wrapper.displayed = false;
		}
	    }
	    arrayAdapter.notifyDataSetChanged();
	}
    };

    private final IndoorsLocationAdapter libraryFloorChangeListener = new IndoorsLocationAdapter() {

	public void positionUpdated(com.customlbs.shared.Coordinate c, int accuracy) {
	    for (FloorListWrapper wrapper : data) {
		if (wrapper.floor.getLevel() == c.z && c.z != Integer.MAX_VALUE
			&& c.y != Integer.MAX_VALUE && c.x != Integer.MAX_VALUE) {
		    wrapper.located = true;
		} else {
		    wrapper.located = false;
		}
	    }
	    arrayAdapter.notifyDataSetChanged();
	};
    };

    /**
     * Update the list with floors from given building.
     * 
     * @param building
     */
    public void updateFloors(final Building building) {
	data.clear();

	if (building == null) {
	    return;
	}
	SurfaceState surfaceState = indoorsSurfaceFragment.getSurfaceState();
	for (Floor floor : building.getFloors()) {
	    FloorListWrapper tmp = new FloorListWrapper(floor, surfaceState.currentFloor != null
		    && floor.getId() == surfaceState.currentFloor.getId(),
		    floor.getLevel() == surfaceState.lastFloorLevelSelectedByLibrary);
	    data.add(tmp);
	}

	arrayAdapter.notifyDataSetChanged();
    }

    /**
     * Callback implemented by activity holding this fragment to handle floor
     * switch.
     * 
     * @author customLBS | Philipp Koenig
     * 
     */
    public interface OnFloorSelectedListener {
	/**
	 * if null, autoselect was selected
	 * 
	 * @param floor
	 */
	public void onFloorSelected(Floor floor);
    }

    @Override
    public void onAttach(Activity activity) {
	super.onAttach(activity);
	try {
	    mFloorSelectListener = (OnFloorSelectedListener) activity;
	} catch (ClassCastException e) {
	    throw new ClassCastException(activity.toString()
		    + " must implement OnFloorSelectedListener");
	}
    }

    /**
     * Very fast adapter with custom views. Checkout
     * http://www.youtube.com/watch?v=wDBM6wVEO70&t=40m45s
     * 
     * @author customLBS | Philipp Koenig
     * 
     */
    private static class FloorListAdapter extends BaseAdapter {

	enum ItemType {
	    NORMAL, HIGHLIGHTED;
	}

	final Context context;
	final ArrayList<FloorListWrapper> data;

	public FloorListAdapter(Context context, ArrayList<FloorListWrapper> data) {
	    this.context = context;
	    this.data = data;
	}

	@Override
	public int getItemViewType(int position) {
	    return data.get(position).displayed ? ItemType.HIGHLIGHTED.ordinal() : ItemType.NORMAL
		    .ordinal();
	}

	@Override
	public int getViewTypeCount() {
	    return ItemType.values().length;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    ViewHolder holder;

	    final FloorListWrapper floorWrapper = data.get(position);
	    final ItemType type = ItemType.values()[getItemViewType(position)];

	    // create new view if null or type changed
	    if (convertView == null || ((ViewHolder) convertView.getTag()).type != type) {
		int res;
		switch (type) {
		case NORMAL:
		    res = R.layout.listview_item_floors;
		    break;
		case HIGHLIGHTED:
		    res = R.layout.listview_item_floors_highlighted;
		    break;
		default:
		    throw new IllegalArgumentException();
		}

		convertView = ((Activity) context).getLayoutInflater().inflate(res, parent, false);
		holder = new ViewHolder();
		holder.textFloorLevel = (TextView) convertView.findViewById(R.id.text_floorlevel);
		holder.textFloorName = (TextView) convertView.findViewById(R.id.text_floorname);
		holder.imageLocation = (ImageView) convertView.findViewById(R.id.image_location);
		holder.type = type;

		convertView.setTag(holder);
	    } else {
		holder = (ViewHolder) convertView.getTag();
	    }

	    if (holder.textFloorName != null) {
		if ("".equals(floorWrapper.floor.getName())) {
		    holder.textFloorName.setText("Floor " + (floorWrapper.floor.getLevel()));

		} else {
		    holder.textFloorName.setText(floorWrapper.floor.getName());
		}
	    }
	    if (holder.textFloorLevel != null) {
		holder.textFloorLevel.setText(floorWrapper.floor.getLevel() + "");
	    }

	    if (holder.imageLocation != null) {
		if (floorWrapper.located) {
		    holder.imageLocation.setVisibility(View.VISIBLE);
		} else {
		    holder.imageLocation.setVisibility(View.GONE);
		}
	    }

	    return convertView;
	}

	// fast references, so no need for slow findViewById
	static class ViewHolder {
	    TextView textFloorLevel;
	    TextView textFloorName;
	    ImageView imageLocation;
	    ItemType type;
	}

	@Override
	public int getCount() {
	    return data.size();
	}

	@Override
	public FloorListWrapper getItem(int position) {
	    return data.get(position);
	}

	@Override
	public long getItemId(int position) {
	    return data.get(position).floor.getId();
	}
    }

    private static class FloorListWrapper {
	public FloorListWrapper(Floor floor, boolean displayed, boolean located) {
	    this.floor = floor;
	    this.displayed = displayed;
	    this.located = located;
	}

	Floor floor;
	boolean displayed;
	boolean located;
    }

}
