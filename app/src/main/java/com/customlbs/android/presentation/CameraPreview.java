package com.customlbs.android.presentation;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Preview view of the camera with auto focus capability. Adopted from the ZBar
 * example code.
 * 
 * @author Darius Malecki
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final Logger LOGGER = LoggerFactory.getLogger(new Object() {}.getClass()
	    .getEnclosingClass());

    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private PreviewCallback previewCallback;
    private AutoFocusCallback autoFocusCallback;

    public CameraPreview(Context context, Camera camera, PreviewCallback previewCallback,
	    AutoFocusCallback autoFocusCallback) {
	super(context);
	this.camera = camera;
	this.previewCallback = previewCallback;
	this.autoFocusCallback = autoFocusCallback;

	// a holder gets notified when the surface of the camera is created and
	// destroyed
	surfaceHolder = getHolder();
	surfaceHolder.addCallback(this);

	// deprecated setting, but required on Android versions prior to 3.0
	surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
	// The Surface has been created, now tell the camera where to draw the
	// preview.
	try {
	    camera.setPreviewDisplay(holder);
	} catch (IOException e) {
	    LOGGER.error("Error setting camera preview", e);
	}
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
	// Camera preview released in activity
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	/*
	 * If your preview can change or rotate, take care of those events here.
	 * Make sure to stop the preview before resizing or reformatting it.
	 */
	if (surfaceHolder.getSurface() == null) {
	    // preview surface does not exist
	    return;
	}

	// stop preview before making changes
	try {
	    camera.stopPreview();
	} catch (Exception e) {
	    // ignore: tried to stop a non-existent preview
	}

	try {
	    // Hard code camera surface rotation 90 degs to match Activity view
	    // in portrait
	    camera.setDisplayOrientation(90);

	    camera.setPreviewDisplay(surfaceHolder);
	    camera.setPreviewCallback(previewCallback);
	    camera.startPreview();
	    camera.autoFocus(autoFocusCallback);
	} catch (Exception e) {
	    LOGGER.error("Error starting camera preview", e);
	}
    }
}
