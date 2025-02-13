package com.example.si_t3;

import android.content.Context;
import android.graphics.*;
import android.media.MediaMetadataRetriever;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class FrameSeekBar extends View {
    private List<Bitmap> frameThumbnails = new ArrayList<>();
    private Paint paint = new Paint();
    private Path handlePath = new Path();
    private int handleWidth = 25; // Width of the handles
    private int spacing = 4; // Spacing between frames
    private float leftHandlePos = 0; // Position of the left handle
    private float rightHandlePos = 0; // Position of the right handle
    private boolean movingLeftHandle = false; // Track if the left handle is being moved
    private boolean movingRightHandle = false; // Track if the right handle is being moved
    private long videoDurationMs; // Total duration of the video in milliseconds
    private RectF selectedRect = new RectF(); // Rectangle for the selected region
    private TimeUpdateListener timeListener; // Listener for time updates
    private float lastTouchX; // Last touch position for smooth dragging

    public interface TimeUpdateListener {
        void onTimeUpdated(long startMs, long endMs);
    }

    public FrameSeekBar(Context context) {
        super(context);
        init();
    }

    public FrameSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        setLayerType(LAYER_TYPE_HARDWARE, null); // Enable hardware acceleration for smoother rendering
    }

    public void setVideoData(String videoPath, int frameCount) {
        new Thread(() -> {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                retriever.setDataSource(videoPath);
                String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                videoDurationMs = Long.parseLong(duration);

                for (int i = 0; i < frameCount; i++) {
                    long timeUs = (videoDurationMs * i * 1000) / frameCount;
                    Bitmap frame = retriever.getFrameAtTime(timeUs);
                    if (frame != null) {
                        frameThumbnails.add(Bitmap.createScaledBitmap(frame, 80, 80, false));
                    }
                }
                post(() -> {
                    rightHandlePos = getWidth(); // Initialize the right handle to the end
                    invalidate();
                });
            } catch (Exception e) {
                post(() -> {
                    paint.setColor(Color.RED);
                    invalidate();
                });
            }
        }).start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw thumbnails
        int xPos = 0;
        for (Bitmap frame : frameThumbnails) {
            canvas.drawBitmap(frame, xPos, 20, null);
            xPos += frame.getWidth() + spacing;
        }

        // Draw selected region highlight
        paint.setColor(Color.argb(100, 255, 64, 129));
        selectedRect.set(leftHandlePos, 0, rightHandlePos, getHeight());
        canvas.drawRoundRect(selectedRect, 8, 8, paint);

        // Draw handles with shadow
        paint.setColor(Color.parseColor("#FF4081"));
        paint.setShadowLayer(8f, 0, 4, Color.argb(100, 0, 0, 0));

        // Left handle
        handlePath.reset();
        handlePath.addRoundRect(
                leftHandlePos - handleWidth / 2f, 0,
                leftHandlePos + handleWidth / 2f, getHeight(),
                8f, 8f, Path.Direction.CW
        );
        canvas.drawPath(handlePath, paint);

        // Right handle
        handlePath.reset();
        handlePath.addRoundRect(
                rightHandlePos - handleWidth / 2f, 0,
                rightHandlePos + handleWidth / 2f, getHeight(),
                8f, 8f, Path.Direction.CW
        );
        canvas.drawPath(handlePath, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float layoutWidth = getWidth(); // Total width of the seekbar layout

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = x;
                movingLeftHandle = Math.abs(x - leftHandlePos) <= handleWidth;
                movingRightHandle = Math.abs(x - rightHandlePos) <= handleWidth;
                break;

            case MotionEvent.ACTION_MOVE:
                if (movingLeftHandle) {
                    leftHandlePos = Math.max(0, Math.min(x, rightHandlePos - handleWidth * 2));
                }
                else if (movingRightHandle) {
                    rightHandlePos = Math.min(layoutWidth, Math.max(x, leftHandlePos + handleWidth * 2));
                }

                // 🔹 Convert Handle Position to Percentage
                float leftPercentage = leftHandlePos / layoutWidth;
                float rightPercentage = rightHandlePos / layoutWidth;

                // 🔹 Calculate Start & End Time Based on Percentage
                long startTime = (long) (leftPercentage * videoDurationMs);
                long endTime = (long) (rightPercentage * videoDurationMs);

                if (timeListener != null) {
                    timeListener.onTimeUpdated(startTime, endTime);
                }

                postInvalidate(); // 🔹 Update UI Smoothly
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                movingLeftHandle = false;
                movingRightHandle = false;
                break;
        }
        return true;
    }





    private void updateTimeDisplay() {
        if (timeListener != null) {
            long startTime = getApproxTime(leftHandlePos);
            long endTime = getApproxTime(rightHandlePos);
            timeListener.onTimeUpdated(startTime, endTime);
        }
    }

    private long getApproxTime(float handlePos) {
        // Calculate the approximate time based on the handle position
        float progress = handlePos / getWidth();
        return (long) (progress * videoDurationMs);
    }

    public void setTimeListener(TimeUpdateListener listener) {
        this.timeListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredHeight = 120;
        int width = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, resolveSize(desiredHeight, heightMeasureSpec));
    }
}