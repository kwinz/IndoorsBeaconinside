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
import android.widget.Toast;

import com.customlbs.android.R;
import com.customlbs.surface.library.IndoorsSurfaceFragment;

/**
 * Fragment displaying the current position button.
 * 
 * 
 * @author Thomas Wagner
 * 
 */
public class CurrentPositionButtonFragment extends Fragment implements OnClickListener {
    private IndoorsSurfaceFragment indoorsFragment;
    private View buttonCurrentPosition;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	View view = inflater.inflate(R.layout.fragment_currentpositionbutton, container, false);

	buttonCurrentPosition = view.findViewById(R.id.buttonCurrentPosition);
	buttonCurrentPosition.setOnClickListener(this);

	return view;
    }

    @Override
    public void onAttach(Activity activity) {
	super.onAttach(activity);

	FragmentManager fragmentManager = ((FragmentActivity) activity).getSupportFragmentManager();
	indoorsFragment = (IndoorsSurfaceFragment) fragmentManager.findFragmentByTag("indoors");
	
	if (indoorsFragment == null){
	    throw new RuntimeException("Couldn't find indoorsFragment in host activity.");
	}
    }

    @Override
    public void onClick(View v) {
	if (!indoorsFragment.centerUserPosition()) {
	    Toast.makeText(getActivity(), "No position", Toast.LENGTH_SHORT).show();
	}
    }

}
