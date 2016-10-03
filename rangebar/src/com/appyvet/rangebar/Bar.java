/*
 * Copyright 2013, Edmodo, Inc. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file, or at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" 
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License. 
 */

package com.appyvet.rangebar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.TypedValue;

/**
 * This class represents the underlying gray bar in the RangeBar (without the
 * thumbs).
 */
public class Bar {

    // Member Variables ////////////////////////////////////////////////////////

    private final Paint mBarPaint;

    private final Paint mTickPaint;
    private final Paint mSelectedTickPaint;
    private final Paint mTickNotAvailPaint;

    // Left-coordinate of the horizontal bar.
    private final float mLeftX;

    private final float mRightX;

    private final float mY;

    private int mNumSegments;

    private float mTickDistance;

    private final float mTickHeight;
    private final float mTickNotAvailHeight;

    // Constructor /////////////////////////////////////////////////////////////


    /**
     * Bar constructor
     *
     * @param ctx          the context
     * @param x            the start x co-ordinate
     * @param y            the y co-ordinate
     * @param length       the length of the bar in px
     * @param tickCount    the number of ticks on the bar
     * @param tickColor    the color of each tick
     * @param barColor     the color of the bar
     */
    public Bar(Context ctx,
            float x,
            float y,
            float length,
            int tickCount,
            float tickWidth,
            float tickNotAvailWidth,
            float tickSelectedWidth,

            int tickColor,
            int tickNotAvailColor,
            int tickSelectedColor,

            float barWidth,
            float barNotAvailWidth,
            float barSelectedWidth,
            int barColor,
            int barNotAvailColor,
            int barSelectedColor) {

        mLeftX = x;
        mRightX = x + length;
        mY = y;

        mNumSegments = tickCount - 1;
        mTickDistance = length / mNumSegments;
//        mTickHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
//                tickHeightDP,
//                ctx.getResources().getDisplayMetrics());
        mTickHeight = tickWidth;

        mTickNotAvailHeight = tickNotAvailWidth;

        // Initialize the paint.
        mBarPaint = new Paint();
        mBarPaint.setColor(barColor);
        mBarPaint.setStrokeWidth(barWidth);
        mBarPaint.setAntiAlias(true);

        mTickPaint = new Paint();
        mTickPaint.setColor(tickColor);
        mTickPaint.setStrokeWidth(tickColor);
        mTickPaint.setAntiAlias(true);

        mSelectedTickPaint = new Paint();
        mSelectedTickPaint.setColor(tickSelectedColor);
        mSelectedTickPaint.setStrokeWidth(tickSelectedWidth);
        mSelectedTickPaint.setAntiAlias(true);

        mTickNotAvailPaint = new Paint();
        mTickNotAvailPaint.setColor(tickNotAvailColor);
        mTickNotAvailPaint.setStrokeWidth(tickNotAvailWidth);
        mTickNotAvailPaint.setAntiAlias(true);

    }

    // Package-Private Methods /////////////////////////////////////////////////

    /**
     * Draws the bar on the given Canvas.
     *
     * @param canvas Canvas to draw on; should be the Canvas passed into {#link
     *               View#onDraw()}
     */
    public void draw(Canvas canvas) {

        canvas.drawLine(mLeftX, mY, mRightX, mY, mBarPaint);
    }

    /**
     * Get the x-coordinate of the left edge of the bar.
     *
     * @return x-coordinate of the left edge of the bar
     */
    public float getLeftX() {
        return mLeftX;
    }

    /**
     * Get the x-coordinate of the right edge of the bar.
     *
     * @return x-coordinate of the right edge of the bar
     */
    public float getRightX() {
        return mRightX;
    }

    /**
     * Gets the x-coordinate of the nearest tick to the given x-coordinate.
     *
     * @param thumb the thumb to find the nearest tick for
     * @return the x-coordinate of the nearest tick
     */
    public float getNearestTickCoordinate(PinView thumb) {

        final int nearestTickIndex = getNearestTickIndex(thumb);

        return mLeftX + (nearestTickIndex * mTickDistance);
    }

//    /**
//     * Gets the nearest tick to the given x-coordinate.
//     *
//     * @param thumb the thumb to find the nearest tick for
//     * @return the x-coordinate of the nearest tick
//     */
//    public float getNearestTick(PinView thumb) {
//
//        final int nearestTickIndex = getNearestTickIndex(thumb);
//
//        return mLeftX + (nearestTickIndex * mTickDistance);
//    }

    /**
     * Gets the zero-based index of the nearest tick to the given thumb.
     *
     * @param thumb the Thumb to find the nearest tick for
     * @return the zero-based index of the nearest tick
     */
    public int getNearestTickIndex(PinView thumb) {

        return (int) ((thumb.getX() - mLeftX + mTickDistance / 2f) / mTickDistance);
    }


    /**
     * Set the number of ticks that will appear in the RangeBar.
     *
     * @param tickCount the number of ticks
     */
    public void setTickCount(int tickCount) {

        final float barLength = mRightX - mLeftX;

        mNumSegments = tickCount - 1;
        mTickDistance = barLength / mNumSegments;
    }

    // Private Methods /////////////////////////////////////////////////////////

    /**
     * Draws the tick marks on the bar.
     *
     * @param canvas Canvas to draw on; should be the Canvas passed into {#link
     *               View#onDraw()}
     */
    public void drawTicks(Canvas canvas) {
        drawTicks(canvas, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public void drawTicks(Canvas canvas, int minAvailRange, int maxAvailRange, int minIdx, int maxIdx) {

        // Loop through and draw each tick (except final tick).
        for (int i = 0; i < mNumSegments; i++) {
            final float x = calcTickX(i);
            if (i >= minIdx && i <= maxIdx) {
                canvas.drawCircle(x, mY, mTickHeight, mSelectedTickPaint);
            } else if (i >= minAvailRange && i<= maxAvailRange)
                canvas.drawCircle(x, mY, mTickHeight, mTickPaint);
            else
                canvas.drawCircle(x, mY, mTickNotAvailHeight, mTickNotAvailPaint);
        }
        // Draw final tick. We draw the final tick outside the loop to avoid any
        // rounding discrepancies.
        canvas.drawCircle(mRightX, mY, mTickHeight, mTickPaint);
    }

    public float calcTickX(int v){
        return v * mTickDistance + mLeftX;
    }
}
