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
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.util.TypedValue

/**
 * Class representing the blue connecting line between the two thumbs.
 */
class ConnectingLine
// Constructor /////////////////////////////////////////////////////////////

/**
 * Constructor for connecting line

 * @param ctx                  the context for the line
 * *
 * @param y                    the y co-ordinate for the line
 * *
 * @param connectingLineWeight the weight of the line
 * *
 * @param connectingLineColor  the color of the line
 */
(ctx: Context, private val mY: Float, connectingLineWeight: Float,
 connectingLineColor: Int) {

    // Member Variables ////////////////////////////////////////////////////////

    private val mPaint: Paint
    private val mCustomPaint: Paint

    init {

        val res = ctx.resources

        // Initialize the paint, set values
        mPaint = Paint()
        mPaint.color = connectingLineColor
        mPaint.strokeWidth = connectingLineWeight
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.isAntiAlias = true

        mCustomPaint = Paint()
        mCustomPaint.color = connectingLineColor
        mCustomPaint.strokeWidth = connectingLineWeight
        mCustomPaint.strokeCap = Paint.Cap.ROUND
        mCustomPaint.isAntiAlias = true
    }

    // Package-Private Methods /////////////////////////////////////////////////

    /**
     * Draw the connecting line between the two thumbs in rangebar.

     * @param canvas     the Canvas to draw to
     * *
     * @param leftThumb  the left thumb
     * *
     * @param rightThumb the right thumb
     */
    fun draw(canvas: Canvas, leftThumb: PinView, rightThumb: PinView) {
        canvas.drawLine(leftThumb.x, mY, rightThumb.x, mY, mPaint)
    }

    fun drawLine(canvas: Canvas, left: Float, right: Float, color: Int, width: Float) {
        mCustomPaint.color = color
        mCustomPaint.strokeWidth = width

        canvas.drawLine(left, mY, right, mY, mCustomPaint)
    }

    /**
     * Draw the connecting line between for single slider.

     * @param canvas     the Canvas to draw to
     * *
     * @param rightThumb the right thumb
     * *
     * @param leftMargin the left margin
     */
    fun draw(canvas: Canvas, leftMargin: Float, rightThumb: PinView) {
        canvas.drawLine(leftMargin, mY, rightThumb.x, mY, mPaint)
    }
}
