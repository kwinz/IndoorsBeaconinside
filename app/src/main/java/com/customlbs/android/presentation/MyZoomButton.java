package com.customlbs.android.presentation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Custom view to display zoom buttons.
 * 
 * @author customLBS | Philipp Koenig
 * 
 */
public class MyZoomButton extends View {

    private OnClickListener zoomOutListener;
    private OnClickListener zoomInListener;
    private Paint paintBg;
    private Paint paintBgPlus;
    private Paint paintBgMinus;
    private Paint paintBgHighlighted;
    private Paint paintBorder;
    private Paint paintBorderMiddle;
    private Paint paintSigns;

    public MyZoomButton(Context context, AttributeSet attrs) {
	super(context, attrs);

	setOnTouchListener(new OnTouchListener() {

	    @Override
	    public boolean onTouch(View arg0, MotionEvent arg1) {
		if (arg1.getY() < arg0.getHeight() / 2 && zoomInListener != null) {
		    if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
			paintBgPlus = paintBgHighlighted;
			invalidate();
		    } else if (arg1.getAction() == MotionEvent.ACTION_UP)
			zoomInListener.onClick(arg0);
		} else if (zoomOutListener != null) {
		    if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
			paintBgMinus = paintBgHighlighted;
			invalidate();
		    } else if (arg1.getAction() == MotionEvent.ACTION_UP)
			zoomOutListener.onClick(arg0);
		}
		if (arg1.getAction() == MotionEvent.ACTION_UP) {
		    paintBgMinus = paintBgPlus = null;
		    invalidate();
		}
		return true;
	    }
	});

	paintBg = new Paint();
	paintBg.setColor(Color.WHITE);
	paintBg.setStyle(Style.FILL);
	paintBg.setAlpha(150);

	paintBgHighlighted = new Paint();
	paintBgHighlighted.setColor(0xf0d244);
	paintBgHighlighted.setStyle(Style.FILL);
	paintBgHighlighted.setAlpha(150);

	paintBorder = new Paint();
	paintBorder.setColor(Color.DKGRAY);
	paintBorder.setStyle(Style.STROKE);
	paintBorder.setAntiAlias(true); // if not we have pixel problems

	paintBorderMiddle = new Paint();
	paintBorderMiddle.setColor(Color.GRAY);
	paintBorderMiddle.setStyle(Style.STROKE);
	paintBorderMiddle.setAntiAlias(true); // if not we have pixel problems

	paintSigns = new Paint();
	paintSigns.setColor(Color.DKGRAY);
	paintSigns.setStyle(Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
	// background
	RectF r = new RectF(0, 0, getWidth(), getHeight());
	canvas.drawRoundRect(r, 3, 3, paintBg);

	canvas.save();
	// background +
	if (paintBgPlus != null) {
	    canvas.clipRect(0, 0, getWidth(), getHeight() / 2);
	    RectF r1 = new RectF(0, 0, getWidth(), getHeight() / 2 + 10);
	    canvas.drawRoundRect(r1, 3, 3, paintBgPlus);
	    canvas.restore();
	}
	// background -
	if (paintBgMinus != null) {
	    canvas.clipRect(0, getHeight() / 2, getWidth(), getHeight());
	    RectF r2 = new RectF(0, getHeight() / 2 - 10, getWidth(), getHeight());
	    canvas.drawRoundRect(r2, 3, 3, paintBgMinus);
	    canvas.restore();
	}

	// border at the half
	canvas.drawRect(1, getHeight() / 2, getWidth() - 1, getHeight() / 2, paintBorderMiddle);
	// border
	canvas.drawRoundRect(r, 3, 3, paintBorder);

	// 1/10 of element width
	float thickness = getWidth() * 0.1f / 2;
	// half of element width
	float lenght = getWidth() / 2 / 2;
	// +
	canvas.drawRect(getWidth() / 2 - lenght, getHeight() / 4 - thickness, getWidth() / 2
		+ lenght, getHeight() / 4 + thickness, paintSigns);
	canvas.drawRect(getWidth() / 2 - thickness, getHeight() / 4 - lenght, getWidth() / 2
		+ thickness, getHeight() / 4 + lenght, paintSigns);
	// -
	canvas.drawRect(getWidth() / 2 - lenght, getHeight() * 3 / 4 - thickness, getWidth() / 2
		+ lenght, getHeight() * 3 / 4 + thickness, paintSigns);
    }

    public void setOnZoomOutClickListener(View.OnClickListener zoomOutListener) {
	this.zoomOutListener = zoomOutListener;
    }

    public void setOnZoomInClickListener(View.OnClickListener zoomInListener) {
	this.zoomInListener = zoomInListener;
    }
}
