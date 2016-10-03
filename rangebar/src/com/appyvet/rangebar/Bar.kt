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

package com.appyvet.rangebar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.TypedValue

/**
 * This class represents the underlying gray bar in the RangeBar (without the
 * thumbs).
 */
class Bar
// Constructor /////////////////////////////////////////////////////////////
/**
 * Bar constructor

 * @param ctx          the context
 * *
 * @param x            the start x co-ordinate
 * *
 * @param y            the y co-ordinate
 * *
 * @param length       the length of the bar in px
 * *
 * @param tickCount    the number of ticks on the bar
 * *
 * @param tickColor    the color of each tick
 * *
 * @param barColor     the color of the bar
 */
(ctx: Context,
        // Left-coordinate of the horizontal bar.
 /**
  * Get the x-coordinate of the left edge of the bar.

  * @return x-coordinate of the left edge of the bar
  */
 val leftX: Float,
 private val mY: Float,
 length: Float,
 tickCount: Int,
 private val mTickHeight: Float,
 private val mTickNotAvailHeight: Float,
 tickSelectedWidth: Float,

 tickColor: Int,
 tickNotAvailColor: Int,
 tickSelectedColor: Int,

 barWidth: Float,
 barNotAvailWidth: Float,
 barSelectedWidth: Float,
 barColor: Int,
 barNotAvailColor: Int,
 barSelectedColor: Int) {

    // Member Variables ////////////////////////////////////////////////////////

    private val mBarPaint: Paint
    private val mBarSelectedPaint: Paint

    private val mTickPaint: Paint
    private val mSelectedTickPaint: Paint
    private val mTickNotAvailPaint: Paint

    /**
     * Get the x-coordinate of the right edge of the bar.

     * @return x-coordinate of the right edge of the bar
     */
    val rightX: Float

    private var mNumSegments: Int = 0

    private var mTickDistance: Float = 0.toFloat()

    init {
        rightX = leftX + length

        mNumSegments = tickCount - 1
        mTickDistance = length / mNumSegments

        // Initialize the paint.
        mBarPaint = Paint()
        mBarPaint.color = barNotAvailColor
        mBarPaint.strokeWidth = barNotAvailWidth
        mBarPaint.isAntiAlias = true

        mBarSelectedPaint = Paint()
        mBarSelectedPaint.color = barSelectedColor
        mBarSelectedPaint.strokeWidth = barSelectedWidth
        mBarSelectedPaint.isAntiAlias = true

        mTickPaint = Paint()
        mTickPaint.color = tickColor
        mTickPaint.strokeWidth = tickColor.toFloat()
        mTickPaint.isAntiAlias = true

        mSelectedTickPaint = Paint()
        mSelectedTickPaint.color = tickSelectedColor
        mSelectedTickPaint.strokeWidth = tickSelectedWidth
        mSelectedTickPaint.isAntiAlias = true

        mTickNotAvailPaint = Paint()
        mTickNotAvailPaint.color = tickNotAvailColor
        mTickNotAvailPaint.strokeWidth = mTickNotAvailHeight
        mTickNotAvailPaint.isAntiAlias = true

    }//        mTickHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
    //                tickHeightDP,
    //                ctx.getResources().getDisplayMetrics());

    // Package-Private Methods /////////////////////////////////////////////////

    /**
     * Draws the bar on the given Canvas.

     * @param canvas Canvas to draw on; should be the Canvas passed into {#link
     * *               View#onDraw()}
     */
    fun draw(canvas: Canvas) {

        canvas.drawLine(leftX, mY, rightX, mY, mBarPaint)
    }

    /**
     * Gets the x-coordinate of the nearest tick to the given x-coordinate.

     * @param thumb the thumb to find the nearest tick for
     * *
     * @return the x-coordinate of the nearest tick
     */
    fun getNearestTickCoordinate(thumb: PinView): Float {

        val nearestTickIndex = getNearestTickIndex(thumb)

        return leftX + nearestTickIndex * mTickDistance
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

     * @param thumb the Thumb to find the nearest tick for
     * *
     * @return the zero-based index of the nearest tick
     */
    fun getNearestTickIndex(thumb: PinView): Int {

        return ((thumb.x - leftX + mTickDistance / 2f) / mTickDistance).toInt()
    }


    /**
     * Set the number of ticks that will appear in the RangeBar.

     * @param tickCount the number of ticks
     */
    fun setTickCount(tickCount: Int) {

        val barLength = rightX - leftX

        mNumSegments = tickCount - 1
        mTickDistance = barLength / mNumSegments
    }

    @JvmOverloads fun drawTicks(canvas: Canvas, minAvailRange: Int = Integer.MIN_VALUE, maxAvailRange: Int = Integer.MAX_VALUE, minIdx: Int = Integer.MIN_VALUE, maxIdx: Int = Integer.MAX_VALUE) {

        // Loop through and draw each tick (except final tick).
        for (i in 0..mNumSegments - 1) {
            drawTick(canvas, i, maxAvailRange, maxIdx, minAvailRange, minIdx, calcTickX(i))
        }
        // Draw final tick. We draw the final tick outside the loop to avoid any
        // rounding discrepancies.
        drawTick(canvas, mNumSegments - 1, maxAvailRange, maxIdx, minAvailRange, minIdx, rightX)
    }

    private fun drawTick(canvas: Canvas, i: Int, maxAvailRange: Int, maxIdx: Int, minAvailRange: Int, minIdx: Int, x: Float) {
        if (i >= minIdx && i <= maxIdx) {
            canvas.drawCircle(x, mY, mTickHeight, mSelectedTickPaint)
        } else if (i >= minAvailRange && i <= maxAvailRange) {
            canvas.drawCircle(x, mY, mTickHeight, mTickPaint)
        } else {
            canvas.drawCircle(x, mY, mTickNotAvailHeight, mTickNotAvailPaint)
        }
    }

    fun calcTickX(v: Int): Float {
        return v * mTickDistance + leftX
    }
}// Private Methods /////////////////////////////////////////////////////////
/**
 * Draws the tick marks on the bar.

 * @param canvas Canvas to draw on; should be the Canvas passed into {#link
 * *               View#onDraw()}
 */
