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

    private float minTime = 0;
    private float maxTime = 30;
    private float rangeWidth;
    private OnRangeChangedListener listener;

    private boolean isStartThumbSelected = false;
    private boolean isEndThumbSelected = false;
    private static final float MIN_MOVEMENT_THRESHOLD = 5f; // Threshold for smoother movement

    public CustomRangeBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paintLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintLine.setColor(Color.GRAY);
        paintLine.setStrokeWidth(barHeight);
        paintLine.setStrokeCap(Paint.Cap.ROUND);

        paintThumb = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintThumb.setColor(Color.rgb(249, 168, 37));

        paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setColor(Color.BLACK);
        paintText.setTextSize(40f);
        paintText.setTextAlign(Paint.Align.CENTER);

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

        startThumbX = minBound;
        endThumbX = maxBound;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw smooth range bar
        canvas.drawLine(minBound, getHeight() / 2f, maxBound, getHeight() / 2f, paintLine);

        // Draw thumbs
        canvas.drawCircle(startThumbX, getHeight() / 2f, thumbRadius, paintThumb);
        canvas.drawCircle(endThumbX, getHeight() / 2f, thumbRadius, paintThumb);

        // Draw time labels
        int startTime = getTimeFromPosition(startThumbX);
        int endTime = getTimeFromPosition(endThumbX);
        float textY = (getHeight() / 2f) - thumbRadius - 20;

        canvas.drawText(formatTime(startTime), startThumbX, textY, paintText);
        canvas.drawText(formatTime(endTime), endThumbX, textY, paintText);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Check which thumb is closer
                if (Math.abs(x - startThumbX) < thumbRadius) {
                    isStartThumbSelected = true;
                } else if (Math.abs(x - endThumbX) < thumbRadius) {
                    isEndThumbSelected = true;
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                if (isStartThumbSelected) {
                    // Update start thumb position, ensuring it stays within bounds
                    startThumbX = Math.max(minBound, Math.min(x, endThumbX - thumbRadius));
                } else if (isEndThumbSelected) {
                    // Update end thumb position, ensuring it stays within bounds
                    endThumbX = Math.min(maxBound, Math.max(x, startThumbX + thumbRadius));
                }

                // Notify listener if set
                if (listener != null) {
                    listener.onRangeChanged(getTimeFromPosition(startThumbX), getTimeFromPosition(endThumbX));
                }

                postInvalidate(); // Request UI update
                return true;

            case MotionEvent.ACTION_UP:
                isStartThumbSelected = false;
                isEndThumbSelected = false;
                return true;
        }

        return super.onTouchEvent(event);
    }

    private int getTimeFromPosition(float position) {
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

        startThumbX = minBound + ((start - minTime) / (float) (maxTime - minTime)) * rangeWidth;
        endThumbX = minBound + ((end - minTime) / (float) (maxTime - minTime)) * rangeWidth;

        postInvalidate();
    }

    public void setOnRangeChangedListener(OnRangeChangedListener listener) {
        this.listener = listener;
    }

    public interface OnRangeChangedListener {
        void onRangeChanged(int start, int end);
    }
}