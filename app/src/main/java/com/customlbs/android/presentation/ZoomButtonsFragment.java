package com.customlbs.android.presentation;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.customlbs.android.R;
import com.customlbs.surface.library.IndoorsSurface;
import com.customlbs.surface.library.IndoorsSurfaceFragment;

/**
 * Fragment displaying zoombuttons.
 * 
 * 
 * @author Thomas Wagner
 * 
 */
public class ZoomButtonsFragment extends Fragment implements OnClickListener {
    private IndoorsSurfaceFragment indoorsFragment;
    private IndoorsSurface indoorsSurface;
    private View buttonZoomIn;
    private View buttonZoomOut;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	View view = inflater.inflate(R.layout.fragment_zoombuttons, container, false);

	buttonZoomIn = view.findViewById(R.id.button_zoom_in);
	buttonZoomIn.setOnClickListener(this);
	buttonZoomOut = view.findViewById(R.id.button_zoom_out);
	buttonZoomOut.setOnClickListener(this);

	return view;
    }

    @Override
    public void onAttach(Activity activity) {
	super.onAttach(activity);

	FragmentManager fragmentManager = ((FragmentActivity) activity).getSupportFragmentManager();
	indoorsFragment = (IndoorsSurfaceFragment) fragmentManager.findFragmentByTag("indoors");
	
	if (indoorsFragment != null){
	    indoorsSurface = indoorsFragment.getIndoorsSurface();
	}
	else{
	    throw new RuntimeException("Couldn't find indoorsFragment in host activity.");
	}
    }

    @Override
    public void onClick(View v) {
	switch (v.getId()) {
	case R.id.button_zoom_in:
	    indoorsSurface.zoomIn();
	    break;
	case R.id.button_zoom_out:
	    indoorsSurface.zoomOut();
	    break;
	}
    }

}
