package com.customlbs.android.presentation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.customlbs.android.R;
import com.customlbs.library.DebugSettings;
import com.customlbs.library.callbacks.DebugInfoCallback;
import com.customlbs.library.model.DebugInfo;
import com.customlbs.service.Configuration;
import com.customlbs.service.Version;
import com.customlbs.surface.library.IndoorsSurfaceFragment;

public class DebugFragment extends Fragment implements OnClickListener, DebugInfoCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(new Object() {}.getClass()
	    .getEnclosingClass());

    private static final File cacheDirectory = new File(Environment.getExternalStorageDirectory(),
	    "/Android/data/" + "com.customlbs.library");
    private static final File settingsFile = new File(cacheDirectory, "settings.properties");

    private boolean apiKeyReset = false;
    private boolean serverSettingDeleted = false;
    private View rootView;
    private TabHost tabHost;

    private IndoorsSurfaceFragment indoorsFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	rootView = inflater.inflate(R.layout.fragment_debug, container, false);

	String serverUrl = DebugSettings.getServerUrl();
	if (serverUrl == null) {
	    serverUrl = Configuration.SERVER_URL;
	}

	tabHost = (TabHost) rootView.findViewById(R.id.tabhost);
	tabHost.setup();

	TabSpec settingsTabSpec = tabHost.newTabSpec("Settings");
	settingsTabSpec.setContent(R.id.settingsTab);
	settingsTabSpec.setIndicator("Settings");

	TabSpec surfaceStateTabSpec = tabHost.newTabSpec("Surface State");
	surfaceStateTabSpec.setIndicator("Surface");
	surfaceStateTabSpec.setContent(R.id.surfaceStateTab);

	TabSpec coreTabSpec = tabHost.newTabSpec("Core");
	coreTabSpec.setIndicator("Core");
	coreTabSpec.setContent(R.id.coreTab);

	tabHost.addTab(settingsTabSpec);
	tabHost.addTab(surfaceStateTabSpec);
	tabHost.addTab(coreTabSpec);

	((TextView) rootView.findViewById(R.id.overviewHeader).findViewById(R.id.headerText))
		.setText("OVERVIEW");
	((TextView) rootView.findViewById(R.id.apikeySettingsHeader).findViewById(R.id.headerText))
		.setText("API KEY SETTINGS");
	((TextView) rootView.findViewById(R.id.cacheSettingsHeader).findViewById(R.id.headerText))
		.setText("CACHE SETTINGS");
	((TextView) rootView.findViewById(R.id.serverSettingsHeader).findViewById(R.id.headerText))
		.setText("SERVER SETTINGS");
	((TextView) rootView.findViewById(R.id.restartAppHeader).findViewById(R.id.headerText))
	.setText("RESTART APP");

	View apiKeyResetView = rootView.findViewById(R.id.apiKeyReset);
	apiKeyResetView.setOnClickListener(this);
	((TextView) apiKeyResetView.findViewById(R.id.headerText)).setText("Reset API key");
	((TextView) apiKeyResetView.findViewById(R.id.description))
		.setText("Use to change the API Key");

	((TextView) rootView.findViewById(R.id.apiKeyText)).setText("API Key: "
		+ LoadScreenActivity.getApiKey(getActivity()));
	((TextView) rootView.findViewById(R.id.serverText)).setText("Server: " + serverUrl);
	((TextView) rootView.findViewById(R.id.versionText)).setText("Version: "
		+ Version.VERSION_STRING);

	View clearCacheView = rootView.findViewById(R.id.clearCache);
	clearCacheView.setOnClickListener(this);
	((TextView) clearCacheView.findViewById(R.id.headerText)).setText("Clear cache");
	((TextView) clearCacheView.findViewById(R.id.description))
		.setText("Use to clear the app cache");

	View useProductionServerView = rootView.findViewById(R.id.useProductionServer);
	useProductionServerView.setOnClickListener(this);
	((TextView) useProductionServerView.findViewById(R.id.headerText))
		.setText("Use production server");
	((TextView) useProductionServerView.findViewById(R.id.description))
		.setText("Changes the server to production server");

	View useTestingServerView = rootView.findViewById(R.id.useTestingServer);
	useTestingServerView.setOnClickListener(this);
	((TextView) useTestingServerView.findViewById(R.id.headerText))
		.setText("Use testing server");
	((TextView) useTestingServerView.findViewById(R.id.description))
		.setText("Changes the server to testing server");

	View deleteServerSettingsView = rootView.findViewById(R.id.deleteServerSettings);
	deleteServerSettingsView.setOnClickListener(this);
	((TextView) deleteServerSettingsView.findViewById(R.id.headerText))
		.setText("Delete server settings");
	((TextView) deleteServerSettingsView.findViewById(R.id.description))
		.setText("Use to delete server settings");
	
	View restartAppView = rootView.findViewById(R.id.restartApp);
	restartAppView.setOnClickListener(this);
	((TextView) restartAppView.findViewById(R.id.headerText))
		.setText("Restart app");
	((TextView) restartAppView.findViewById(R.id.description))
		.setText("Use to restart indoo.rs app");

	return rootView;
    }

    @Override
    public void onResume() {
	super.onResume();

	rootView.postDelayed(new Runnable() {

	    @Override
	    public void run() {
		if (isVisible()) {
		    refreshData();

		    rootView.postDelayed(this, 1000);
		}
	    }
	}, 1000);
    }

    @Override
    public void onAttach(Activity activity) {
	super.onAttach(activity);

	FragmentManager fragmentManager = ((FragmentActivity) activity).getSupportFragmentManager();
	indoorsFragment = (IndoorsSurfaceFragment) fragmentManager.findFragmentByTag("indoors");
    }

    @Override
    public void onClick(View v) {
	switch (v.getId()) {
	case R.id.useProductionServer:
	    writeSettings(true);

	    break;
	case R.id.useTestingServer:
	    writeSettings(false);

	    break;
	case R.id.deleteServerSettings:
	    deleteSettings();

	    break;
	case R.id.clearCache:
	    clearCache();

	    break;
	case R.id.apiKeyReset:
	    resetApiKey();

	    break;
	case R.id.restartApp:
	    restartApp();
	    
	    break;
	}
    }
    
    private void restartApp(){
	feedback("open the app again!");
	getActivity().finish();
    }

    private void refreshData() {
	((TextView) rootView.findViewById(R.id.stateText)).setText(indoorsFragment
		.getSurfaceState().toString());

	indoorsFragment.getIndoors().getDebugInfo(this);
    }

    @Override
    public void setDebugInfo(DebugInfo arg0) {
	((TextView) rootView.findViewById(R.id.coreText)).setText(arg0.toString());
    }

    private void feedback(String message) {
	Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    private void resetApiKey() {
	if (apiKeyReset) {
	    feedback("already reset. restart the app!");
	} else {
	    apiKeyReset = true;
	    LoadScreenActivity.setApiKey(getActivity(), null);
	    feedback("api key reset. restart the app!");
	}
    }

    private void writeSettings(boolean useProductionServer) {
	String settingsContent = "rest.urlprefix=https\\://";

	if (useProductionServer) {
	    settingsContent += "api";
	} else {
	    settingsContent += "testing";
	}
	settingsContent += ".indoo.rs";

	try {
	    FileWriter fileWriter = new FileWriter(settingsFile);
	    fileWriter.write(settingsContent);
	    fileWriter.close();

	    feedback("settings written. restart the app!");
	} catch (IOException e) {
	    LOGGER.error("could not write settings.properties", e);

	    feedback("could not write settings");
	}
    }

    private void deleteSettings() {
	if (serverSettingDeleted) {
	    feedback("already deleted. restart the app!");
	} else {
	    serverSettingDeleted = true;
	    settingsFile.delete();
	    feedback("settings deleted. restart the app!");
	}
    }

    private void clearCache() {
	boolean cacheCleared = deleteDir(cacheDirectory);

	if (cacheCleared) {
	    feedback("cache cleared. restart the app!");
	    getActivity().finish();
	} else {
	    feedback("could not clear cache.");
	}
    }

    private boolean deleteDir(File dir) {
	if (dir != null && dir.isDirectory()) {
	    String[] children = dir.list();
	    for (int i = 0; i < children.length; i++) {
		boolean success = deleteDir(new File(dir, children[i]));
		if (!success) {
		    return false;
		}
	    }
	}

	// The directory is now empty so delete it
	return dir.delete();
    }
}
