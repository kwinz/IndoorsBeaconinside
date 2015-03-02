package com.customlbs.android.presentation.legal;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class LicenseDetailFragment extends Fragment {

    protected static final String EXTRA_LICENSE_NAME = "extra_license_name";
    protected static final String EXTRA_LICENSE_PATH = "extra_license_path";

    private String licenseName;
    private String licensePath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	Bundle arguments = getArguments();
	licenseName = arguments.getString(EXTRA_LICENSE_NAME);
	licensePath = arguments.getString(EXTRA_LICENSE_PATH);

	getActivity().setTitle(licenseName);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	WebView webView = new WebView(getActivity());
	webView.loadUrl("file:///android_asset/" + licensePath);
	webView.getSettings().setSupportZoom(true);
	webView.getSettings().setBuiltInZoomControls(true);

	return webView;
    }
}
