package com.customlbs.android.presentation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.TextView;

import com.customlbs.android.R;
import com.customlbs.android.util.IndoorsCrittercism;
import com.customlbs.library.Indoors;
import com.customlbs.library.IndoorsException;
import com.customlbs.library.IndoorsFactory;
import com.customlbs.library.callbacks.IndoorsServiceCallback;

public class MeasurementActivity extends Activity {

    // hardcoded workaround in Worker, has no features associated
    private static String apiKey = "calibration_bridge";

    private Indoors indoors;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	IndoorsCrittercism.start(this);

	setContentView(R.layout.activity_launcher);

	TextView progressText = (TextView) findViewById(R.id.startupactivity_progresstext);
	progressText.setText("Ready for measurement...");
	progressText.setKeepScreenOn(true);

	IndoorsFactory.createInstance(this, apiKey, serviceCallback, true);
    }

    @Override
    protected void onStop() {
	super.onStop();

	if (indoors != null) {
	    indoors.stopDirectNet();
	}

	IndoorsFactory.releaseInstance(serviceCallback);
    }

    private IndoorsServiceCallback serviceCallback = new IndoorsServiceCallback() {

	@Override
	public void onError(IndoorsException e) {
	    IndoorsCrittercism.logException(e);

	    new AlertDialog.Builder(MeasurementActivity.this)
		    .setTitle("Something bad happened!")
		    .setMessage(
			    "Please contact the support and mention the following message: "
				    + e.getErrorCode().getErrorMessage())
		    .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			    finish();
			}
		    }).create().show();
	}

	@Override
	public void connected() {
	    indoors = IndoorsFactory.getInstance();
	    indoors.startDirectNet();
	}
    };
}
