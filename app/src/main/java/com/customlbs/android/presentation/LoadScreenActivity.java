package com.customlbs.android.presentation;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.customlbs.android.R;
import com.customlbs.android.util.IndoorsCrittercism;
import com.customlbs.library.DebugSettings;
import com.customlbs.library.IndoorsException;
import com.customlbs.library.IndoorsFactory;
import com.customlbs.library.callbacks.IndoorsServiceCallback;

/**
 * Loading screen with indoors logo. Also binding to service happens in
 * background.
 * 
 * @author customLBS | Philipp Koenig
 * 
 */
public final class LoadScreenActivity extends Activity {

    /**
     * leave this null for release build, but you can set it while coding for
     * convenience
     */
    private static String apiKey = null; // "xxx-xxx-xxx-xxx";

    private final static String PREFERENCE_APIKEY = "indoors.apikey";
    private final static int CAMERA_QR_REQUEST = 1;

    private EditText keyInput;
    private String enteredApiKey = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	IndoorsCrittercism.start(this);

	setContentView(R.layout.activity_launcher);
	enteredApiKey = null;

	if (getApiKey(this) == null) {
	    final LinearLayout dialogLayout = new LinearLayout(this);
	    keyInput = new EditText(this);

	    dialogLayout.setOrientation(LinearLayout.VERTICAL);
	    dialogLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
		    LayoutParams.WRAP_CONTENT));
	    dialogLayout.addView(keyInput);

	    Builder builder = new AlertDialog.Builder(this).setTitle("Enter API Key")
		    .setView(dialogLayout)
		    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			    enteredApiKey = keyInput.getText().toString();

			    // try to authenticate with entered API key
			    executeServiceRequest(enteredApiKey);
			    return;
			}
		    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			    finish();
			}
		    });

	    // TODO: also support front-camera (e.g. nexus7 2012)
	    if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
		builder.setNeutralButton(R.string.scan_qr, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int whichButton) {
			Toast.makeText(getApplicationContext(), getString(R.string.scan_a_qr_code),
				Toast.LENGTH_SHORT).show();
			startCameraPreview();
		    }
		});
	    }

	    builder.setCancelable(false).create().show();
	} else {
	    executeServiceRequest(getApiKey(this));
	}
    }

    protected void executeServiceRequest(String enteredApiKey) {
	IndoorsFactory.createInstance(getApplicationContext(), enteredApiKey, serviceCallback,
		IndoorsViewer.DEBUG);
    }

    @Override
    protected void onDestroy() {
	super.onDestroy();
	IndoorsFactory.releaseInstance(serviceCallback);
    }

    private IndoorsServiceCallback serviceCallback = new IndoorsServiceCallback() {

	@Override
	public void onError(IndoorsException e) {
	    new AlertDialog.Builder(LoadScreenActivity.this).setTitle("Authentication failed")
		    .setMessage(e.getErrorCode().getErrorMessage())
		    .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			    finish();
			}
		    }).create().show();
	}

	@Override
	public void connected() {
	    if (enteredApiKey != null) {
		setApiKey(LoadScreenActivity.this, enteredApiKey);
	    }
	    startActivity(new Intent(getApplicationContext(), IndoorsViewer.class));
	    finish();
	}
    };

    public static String getApiKey(Context context) {
	if (apiKey == null) {
	    apiKey = PreferenceManager.getDefaultSharedPreferences(context).getString(
		    PREFERENCE_APIKEY, null);

	    if (apiKey == null) {
		apiKey = DebugSettings.getApiKey();
	    }
	}

	return apiKey;
    }

    public static void setApiKey(Context context, String apiKey) {
	LoadScreenActivity.apiKey = apiKey;

	Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
	editor.putString(PREFERENCE_APIKEY, apiKey);
	editor.commit();
    }

    public void startCameraPreview() {
	startActivityForResult(new Intent(this, CameraPreviewActivity.class), CAMERA_QR_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	if (requestCode == CAMERA_QR_REQUEST) {
	    if (resultCode == RESULT_OK) {
		enteredApiKey = data.getStringExtra("barcode");

		executeServiceRequest(enteredApiKey);
	    }
	}
    }
}
