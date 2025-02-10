package com.example.si_t3;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CustomRangeBar extends View {
    private Paint paintLine, paintThumb, paintText;
    private float thumbRadius = 30f;
    private float minBound, maxBound;
    private float startThumbX, endThumbX;
    private float barHeight = 10f;

    private float minTime = 0; // Minimum time (0 sec)
    private float maxTime = 30; // Default max time (can be set dynamically)
    private float rangeWidth;

    private OnRangeChangedListener listener;

    public CustomRangeBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Initialize paint objects
        paintLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintLine.setColor(Color.GRAY);
        paintLine.setStrokeWidth(barHeight);

        paintThumb = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintThumb.setColor(Color.rgb(249, 168, 37));

        paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setColor(Color.BLACK);
        paintText.setTextSize(40f);
        paintText.setTextAlign(Paint.Align.CENTER);

        // Default values
        minBound = thumbRadius;
        maxBound = 600; // Will be updated in onSizeChanged
        startThumbX = minBound;
        endThumbX = maxBound;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        maxBound = w - thumbRadius;
        rangeWidth = maxBound - minBound;

        // Set default thumb positions
        startThumbX = minBound;
        endThumbX = maxBound;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw range bar
        canvas.drawLine(minBound, getHeight() / 2f, maxBound, getHeight() / 2f, paintLine);

        // Draw thumbs
        canvas.drawCircle(startThumbX, getHeight() / 2f, thumbRadius, paintThumb);
        canvas.drawCircle(endThumbX, getHeight() / 2f, thumbRadius, paintThumb);

        // Calculate time values
        int startTime = getTimeFromPosition(startThumbX);
        int endTime = getTimeFromPosition(endThumbX);
        float textY = (getHeight() / 2f) - thumbRadius - 20; // Text above thumbs

        // Draw time values above the thumbs
        canvas.drawText(formatTime(startTime), startThumbX, textY, paintText);
        canvas.drawText(formatTime(endTime), endThumbX, textY, paintText);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                // Handle thumb movement
                if (Math.abs(x - startThumbX) < thumbRadius) {
                    startThumbX = Math.max(minBound, Math.min(x, endThumbX));
                } else if (Math.abs(x - endThumbX) < thumbRadius) {
                    endThumbX = Math.min(maxBound, Math.max(x, startThumbX));
                }
                invalidate(); // Redraw the view

                // Notify listener
                if (listener != null) {
                    listener.onRangeChanged(getTimeFromPosition(startThumbX), getTimeFromPosition(endThumbX));
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

    private int getTimeFromPosition(float position) {
        // Convert thumb position to time
        return Math.round(minTime + ((position - minBound) / rangeWidth * (maxTime - minTime)));
    }

    private String formatTime(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }

    public void setTimeRange(int start, int end) {
        this.minTime = start;
        this.maxTime = end;

        // Update thumb positions
        startThumbX = minBound + ((start - minTime) / (float) (maxTime - minTime)) * rangeWidth;
        endThumbX = minBound + ((end - minTime) / (float) (maxTime - minTime)) * rangeWidth;

        invalidate(); // Redraw the view
    }

    public void setOnRangeChangedListener(OnRangeChangedListener listener) {
        this.listener = listener;
    }

    public interface OnRangeChangedListener {
        void onRangeChanged(int start, int end);
    }
}
