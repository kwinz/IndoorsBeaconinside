package com.customlbs.android.presentation;

import com.customlbs.android.R;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.widget.FrameLayout;

public class CameraPreviewActivity extends Activity {
    private Camera mCamera;
    private CameraPreview mPreview;
    private Handler autoFocusHandler;

    ImageScanner scanner;

    private boolean isPreviewing = true;

    static {
	System.loadLibrary("iconv");
    }

    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	setContentView(R.layout.activity_camera_preview);

	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

	// handler for automatic continuous auto-focus execution
	autoFocusHandler = new Handler();
	mCamera = getCameraInstance();

	/* Instance of the ZBar barcode scanner */
	scanner = new ImageScanner();
	scanner.setConfig(0, Config.X_DENSITY, 3);
	scanner.setConfig(0, Config.Y_DENSITY, 3);

	mPreview = new CameraPreview(this, mCamera, previewCallback, autoFocusCallback);
	FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
	preview.addView(mPreview);
    }

    public void onPause() {
	super.onPause();
	releaseCamera();
    }

    /**
     * Get an instance of the Camera.
     */
    public static Camera getCameraInstance() {
	Camera c = null;
	try {
	    c = Camera.open();
	} catch (Exception e) {
	}
	return c;
    }

    private void releaseCamera() {
	if (mCamera != null) {
	    isPreviewing = false;
	    mCamera.setPreviewCallback(null);
	    mCamera.release();
	    mCamera = null;
	}
    }

    /**
     * Runnable for repeated auto-focus execution.
     */
    private Runnable doAutoFocus = new Runnable() {
	public void run() {
	    if (isPreviewing)
		mCamera.autoFocus(autoFocusCallback);
	}
    };

    /**
     * This callback is called for camera frames, which we then scan for QR codes
     * with the help of the ZBar library.
     */
    PreviewCallback previewCallback = new PreviewCallback() {
	public void onPreviewFrame(byte[] data, Camera camera) {
	    Camera.Parameters parameters = camera.getParameters();
	    Size size = parameters.getPreviewSize();

	    Image barcode = new Image(size.width, size.height, "Y800");
	    barcode.setData(data);

	    int result = scanner.scanImage(barcode);

	    if (result != 0) {
		isPreviewing = false;
		mCamera.setPreviewCallback(null);
		mCamera.stopPreview();

		SymbolSet syms = scanner.getResults();
		for (Symbol sym : syms) {
		    Intent barcodeIntent = new Intent();
		    barcodeIntent.putExtra("barcode", sym.getData());
		    setResult(RESULT_OK, barcodeIntent);
		    finish();
		}
	    }
	}
    };

    /**
     * Execute continuous auto-focusing every second.
     */
    AutoFocusCallback autoFocusCallback = new AutoFocusCallback() {
	public void onAutoFocus(boolean success, Camera camera) {
	    autoFocusHandler.postDelayed(doAutoFocus, 1000);
	}
    };
}
