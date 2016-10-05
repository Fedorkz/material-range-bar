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

/*
 * Copyright 2015, Appyvet, Inc.
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

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

import java.io.Serializable
import java.util.ArrayList
import java.util.HashMap

/**
 * The MaterialRangeBar is a single or double-sided version of a [android.widget.SeekBar]
 * with discrete values. Whereas the thumb for the SeekBar can be dragged to any
 * position in the bar, the RangeBar only allows its thumbs to be dragged to
 * discrete positions (denoted by tick marks) in the bar. When released, a
 * RangeBar thumb will snap to the nearest tick mark.
 * This version is forked from edmodo range bar
 * https://github.com/edmodo/range-bar.git
 * Clients of the RangeBar can attach a
 * [com.appyvet.rangebar.RangeBar.OnRangeBarChangeListener] to be notified when the pins
 * have
 * been moved.
 */
class RangeBar : View {

    // Instance variables for all of the customizable attributes

    private var mTickStart = DEFAULT_TICK_START

    private var mTickEnd = DEFAULT_TICK_END

    private var mTickInterval = DEFAULT_TICK_INTERVAL

    private var mTextColor = DEFAULT_TEXT_COLOR

    private var mThumbRadiusDP = DEFAULT_EXPANDED_PIN_RADIUS_DP

    private var mExpandedPinRadius = DEFAULT_EXPANDED_PIN_RADIUS_DP

    private var mMinPinFont = DEFAULT_MIN_PIN_FONT_SP

    private var mMaxPinFont = DEFAULT_MAX_PIN_FONT_SP

    // setTickCount only resets indices before a thumb has been pressed or a
    // setThumbIndices() is called, to correspond with intended usage
    private var mFirstSetTickCount = true

    private val mDefaultWidth = 500

    private val mDefaultHeight = 150

    private var mMinDistance = 0

    /**
     * Gets the tick count.

     * @return the tick count
     */
    var tickCount = ((mTickEnd - mTickStart) / mTickInterval).toInt() + 1
        private set

    private lateinit var mLeftThumb: PinView

    private lateinit var mRightThumb: PinView

    private var mBar: Bar? = null

    private var mPinDrawable: Drawable? = null

    private var mConnectingLine: ConnectingLine? = null

    private val mExtraLines = ArrayList<Range>()
    private var mAvailableRange: Range? = null

    private var mListener: OnRangeBarChangeListener? = null

    private var mPinTextListener: OnRangeBarTextListener? = null

    private var mTickMap: HashMap<Float, String>? = null

    /**
     * Gets the index of the left-most pin.

     * @return the 0-based index of the left pin
     */
    var leftIndex: Int = 0

    /**
     * Gets the index of the right-most pin.

     * @return the 0-based index of the right pin
     */
    var rightIndex: Int = 0

    /**
     * Gets the type of the bar.

     * @return true if rangebar, false if seekbar.
     */
    var isRangeBar = true
        private set

    private var mPinPadding = DEFAULT_PIN_PADDING_DP

    private var mBarPaddingBottom = DEFAULT_BAR_PADDING_BOTTOM_DP

    private var mTickNotAvailRadius = DEFAULT_TICK_WIDTH
    private var mTickRadius = DEFAULT_TICK_WIDTH
    private var mTickSelectedRadius = DEFAULT_TICK_WIDTH
    private var mTickSelectorRadius = DEFAULT_TICK_WIDTH

    private var mTickNotAvailColor = DEFAULT_TICK_COLOR
    private var mTickColor = DEFAULT_TICK_COLOR
    private var mTickSelectedColor = DEFAULT_TICK_COLOR
    private var mTickSelectorColor = DEFAULT_TICK_COLOR

    private var mBarNotAvailWidth = DEFAULT_TICK_WIDTH
    private var mBarWidth = DEFAULT_TICK_WIDTH
    private var mBarSelectedWidth = DEFAULT_TICK_WIDTH

    private var mBarNotAvailColor = DEFAULT_TICK_COLOR
    private var mBarColor = DEFAULT_TICK_COLOR
    private var mBarSelectedColor = DEFAULT_TICK_COLOR


    //Used for ignoring vertical moves
    private var mDiffX: Int = 0
    private var mDiffY: Int = 0
    private var mLastX: Float = 0.toFloat()
    private var mLastY: Float = 0.toFloat()
    private var mFormatter: IRangeBarFormatter? = null
    private var drawTicks = true
    private var mArePinsTemporary = true

    private var mPinTextFormatter: PinTextFormatter = object : PinTextFormatter {
        override fun getText(value: String): String {
            if (value.length > 4) {
                return value.substring(0, 4)
            } else {
                return value
            }
        }
    }

    private var mStartAvailTick: Int = 0
    private var mEndAvailTick: Int = 0
    private var mEnabled: Boolean = false

    // Constructors ////////////////////////////////////////////////////////////

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        rangeBarInit(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        rangeBarInit(context, attrs)
    }

    // View Methods ////////////////////////////////////////////////////////////

    public override fun onSaveInstanceState(): Parcelable {

        val bundle = Bundle()

        bundle.putParcelable("instanceState", super.onSaveInstanceState())

        bundle.putInt("TICK_COUNT", tickCount)
        bundle.putFloat("TICK_START", mTickStart)
        bundle.putFloat("TICK_END", mTickEnd)
        bundle.putFloat("TICK_INTERVAL", mTickInterval)
        bundle.putInt("TICK_COLOR", mTickColor)

        bundle.putInt("BAR_COLOR", mBarColor)

        bundle.putFloat("THUMB_RADIUS_DP", mThumbRadiusDP)
        bundle.putFloat("EXPANDED_PIN_RADIUS_DP", mExpandedPinRadius)
        bundle.putFloat("PIN_PADDING", mPinPadding)
        bundle.putFloat("BAR_PADDING_BOTTOM", mBarPaddingBottom)
        bundle.putBoolean("IS_RANGE_BAR", isRangeBar)
        bundle.putBoolean("ARE_PINS_TEMPORARY", mArePinsTemporary)
        bundle.putInt("LEFT_INDEX", leftIndex)
        bundle.putInt("RIGHT_INDEX", rightIndex)

        bundle.putBoolean("FIRST_SET_TICK_COUNT", mFirstSetTickCount)

        bundle.putFloat("MIN_PIN_FONT", mMinPinFont)
        bundle.putFloat("MAX_PIN_FONT", mMaxPinFont)

//        bundle.putSerializable("AVAIL_RANGE", mAvailableRange)

        return bundle
    }

    public override fun onRestoreInstanceState(state: Parcelable) {

        if (state is Bundle) {

            tickCount = state.getInt("TICK_COUNT")
            mTickStart = state.getFloat("TICK_START")
            mTickEnd = state.getFloat("TICK_END")
            mTickInterval = state.getFloat("TICK_INTERVAL")
            mTickColor = state.getInt("TICK_COLOR")
            mBarColor = state.getInt("BAR_COLOR")

            mThumbRadiusDP = state.getFloat("THUMB_RADIUS_DP")
            mExpandedPinRadius = state.getFloat("EXPANDED_PIN_RADIUS_DP")
            mPinPadding = state.getFloat("PIN_PADDING")
            mBarPaddingBottom = state.getFloat("BAR_PADDING_BOTTOM")
            isRangeBar = state.getBoolean("IS_RANGE_BAR")
            mArePinsTemporary = state.getBoolean("ARE_PINS_TEMPORARY")

            leftIndex = state.getInt("LEFT_INDEX")
            rightIndex = state.getInt("RIGHT_INDEX")
            mFirstSetTickCount = state.getBoolean("FIRST_SET_TICK_COUNT")

            mMinPinFont = state.getFloat("MIN_PIN_FONT")
            mMaxPinFont = state.getFloat("MAX_PIN_FONT")

//            mAvailableRange = state.getSerializable("AVAIL_RANGE") as Range

            setRangePinsByIndices(leftIndex, rightIndex)

            super.onRestoreInstanceState(state.getParcelable<Parcelable>("instanceState"))

        } else {
            super.onRestoreInstanceState(state)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val width: Int
        val height: Int

        // Get measureSpec mode and size values.
        val measureWidthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val measureHeightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val measureWidth = View.MeasureSpec.getSize(widthMeasureSpec)
        val measureHeight = View.MeasureSpec.getSize(heightMeasureSpec)

        // The RangeBar width should be as large as possible.
        if (measureWidthMode == View.MeasureSpec.AT_MOST) {
            width = measureWidth
        } else if (measureWidthMode == View.MeasureSpec.EXACTLY) {
            width = measureWidth
        } else {
            width = mDefaultWidth
        }

        // The RangeBar height should be as small as possible.
        if (measureHeightMode == View.MeasureSpec.AT_MOST) {
            height = Math.min(mDefaultHeight, measureHeight)
        } else if (measureHeightMode == View.MeasureSpec.EXACTLY) {
            height = measureHeight
        } else {
            height = mDefaultHeight
        }

        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {

        super.onSizeChanged(w, h, oldw, oldh)

        val ctx = context

        // This is the initial point at which we know the size of the View.

        // Create the two thumb objects and position line in view
        val density = resources.displayMetrics.density
        val expandedPinRadius = mExpandedPinRadius / density

        val yPos = h - mBarPaddingBottom
        if (isRangeBar) {
            mLeftThumb = PinView(ctx)
            if (mLeftThumb != null) {
                mLeftThumb.setFormatter(mFormatter)
                mLeftThumb.init(ctx, yPos, expandedPinRadius, mTickSelectorColor, mTextColor, mTickSelectorRadius,
                        mTickSelectorColor, mMinPinFont, mMaxPinFont, mArePinsTemporary, mPinDrawable)
            }
        }
        mRightThumb = PinView(ctx)
        mRightThumb.setFormatter(mFormatter)
        mRightThumb.init(ctx, yPos, expandedPinRadius, mTickSelectorColor, mTextColor, mTickSelectorRadius,
                mTickSelectorColor, mMinPinFont, mMaxPinFont, mArePinsTemporary, mPinDrawable)

        // Create the underlying bar.
        val marginLeft = Math.max(mExpandedPinRadius, mTickSelectorRadius)

        val barLength = w - 2 * marginLeft
        //        mBar = new Bar(ctx, marginLeft, yPos, barLength, mTickCount, mTickRadius, mTickNotAvailRadius, mTickColor, mTickSelectedColor,
        //                mTickNotAvailColor, mBarWidth, mBarNotAvailWidth, mBarColor);
        mBar = Bar(ctx, marginLeft, yPos, barLength, tickCount, mTickRadius, mTickNotAvailRadius, mTickSelectedRadius, mTickColor, mTickNotAvailColor, mTickSelectedColor,
                mBarWidth, mBarNotAvailWidth, mBarSelectedWidth, mBarColor, mBarNotAvailColor, mBarSelectedColor)

        // Initialize thumbs to the desired indices
        if (isRangeBar) {
            mLeftThumb.x = marginLeft + leftIndex / (tickCount - 1).toFloat() * barLength
            mLeftThumb.setXValue(getPinValue(leftIndex))
        }
        mRightThumb.x = marginLeft + rightIndex / (tickCount - 1).toFloat() * barLength
        mRightThumb.setXValue(getPinValue(rightIndex))

        // Set the thumb indices.
        val newLeftIndex = if (isRangeBar) mBar!!.getNearestTickIndex(mLeftThumb) else 0
        val newRightIndex = mBar!!.getNearestTickIndex(mRightThumb)

        // Call the listener.
        if (newLeftIndex != leftIndex || newRightIndex != rightIndex) {
            if (mListener != null) {
                mListener!!.onRangeChangeListener(this, leftIndex, rightIndex,
                        getPinValue(leftIndex),
                        getPinValue(rightIndex))
            }
        }

        // Create the line connecting the two thumbs.
        mConnectingLine = ConnectingLine(ctx, yPos, mBarSelectedWidth,
                mBarSelectedColor)

    }

    override fun onDraw(canvas: Canvas) {

        super.onDraw(canvas)

        mBar!!.draw(canvas)
        if (isRangeBar) {
            if (mExtraLines != null) {
                for (line in mExtraLines) {
                    val st = mBar!!.calcTickX(line.startTick)
                    val et = mBar!!.calcTickX(line.endTick)

                    mConnectingLine!!.drawLine(canvas, st, et, line.color, line.width)
                }
            }

            if (mAvailableRange != null) {
                val st = mBar?.calcTickX(mAvailableRange?.startTick ?: 0) ?: Float.MIN_VALUE
                val et = mBar?.calcTickX(mAvailableRange?.endTick ?: 0) ?: Float.MAX_VALUE
                mConnectingLine?.drawLine(canvas, st, et, mBarColor, mBarWidth)
            }

            if (mConnectingLine != null)
                mConnectingLine?.draw(canvas, mLeftThumb, mRightThumb)

            if (drawTicks) {
                if (mAvailableRange != null)
                    mBar?.drawTicks(canvas, mAvailableRange!!.startTick, mAvailableRange!!.endTick, leftIndex, rightIndex)
                else
                    mBar?.drawTicks(canvas)
            }
            mLeftThumb.draw(canvas)
        } else {
            mConnectingLine?.draw(canvas, marginLeft, mRightThumb)
            if (drawTicks) {
                if (mAvailableRange != null)
                    mBar?.drawTicks(canvas, mAvailableRange!!.startTick, mAvailableRange!!.endTick, leftIndex, rightIndex)
                else
                    mBar?.drawTicks(canvas)
            }
        }
        mRightThumb.draw(canvas)

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        // If this View is not enabled, don't allow for touch interactions.
        if (!isEnabled) {
            return false
        }

        when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                mDiffX = 0
                mDiffY = 0

                mLastX = event.x
                mLastY = event.y
                onActionDown(event.x, event.y)
                return true
            }

            MotionEvent.ACTION_UP -> {
                this.parent.requestDisallowInterceptTouchEvent(false)
                onActionUp(event.x, event.y)
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                this.parent.requestDisallowInterceptTouchEvent(false)
                onActionUp(event.x, event.y)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                onActionMove(event.x)
                this.parent.requestDisallowInterceptTouchEvent(true)
                val curX = event.x
                val curY = event.y
                mDiffX += Math.abs(curX - mLastX).toInt()
                mDiffY += Math.abs(curY - mLastY).toInt()
                mLastX = curX
                mLastY = curY

                if (mDiffX < mDiffY) {
                    //vertical touch
                    parent.requestDisallowInterceptTouchEvent(false)
                    return false
                } else {
                    //horizontal touch (do nothing as it is needed for RangeBar)
                }
                return true
            }

            else -> return false
        }
    }

    // Public Methods //////////////////////////////////////////////////////////

    /**
     * Sets a listener to receive notifications of changes to the RangeBar. This
     * will overwrite any existing set listeners.

     * @param listener the RangeBar notification listener; null to remove any
     * *                 existing listener
     */
    fun setOnRangeBarChangeListener(listener: OnRangeBarChangeListener) {
        mListener = listener
    }

    /**
     * Sets a listener to modify the text

     * @param mPinTextListener the RangeBar pin text notification listener; null to remove any
     * *                         existing listener
     */
    fun setPinTextListener(mPinTextListener: OnRangeBarTextListener) {
        this.mPinTextListener = mPinTextListener
    }


    fun setFormatter(formatter: IRangeBarFormatter) {
        if (mLeftThumb != null) {
            mLeftThumb.setFormatter(formatter)
        }

        if (mRightThumb != null) {
            mRightThumb.setFormatter(formatter)
        }

        mFormatter = formatter
    }

    fun setDrawTicks(drawTicks: Boolean) {
        this.drawTicks = drawTicks
    }

    /**
     * Sets the start tick in the RangeBar.

     * @param tickInterval Integer specifying the number of ticks.
     */
    fun setTickInterval(tickInterval: Float) {
        val tickCount = ((mTickEnd - mTickStart) / tickInterval).toInt() + 1
        if (isValidTickCount(tickCount)) {
            this.tickCount = tickCount
            mTickInterval = tickInterval

            // Prevents resetting the indices when creating new activity, but
            // allows it on the first setting.
            if (mFirstSetTickCount) {
                leftIndex = 0
                rightIndex = this.tickCount - 1

                if (mListener != null) {
                    mListener!!.onRangeChangeListener(this, leftIndex, rightIndex,
                            getPinValue(leftIndex), getPinValue(rightIndex))
                }
            }
            if (indexOutOfRange(leftIndex, rightIndex)) {
                leftIndex = 0
                rightIndex = this.tickCount - 1

                if (mListener != null) {
                    mListener!!.onRangeChangeListener(this, leftIndex, rightIndex,
                            getPinValue(leftIndex), getPinValue(rightIndex))
                }
            }

            createBar()
            createPins()
        } else {
            Log.e(TAG, "tickCount less than 2; invalid tickCount.")
            throw IllegalArgumentException("tickCount less than 2; invalid tickCount.")
        }
    }

    /**
     * Sets the height of the ticks in the range bar.

     * @param tickHeight Float specifying the height of each tick mark in dp.
     */
    fun setTickHeight(tickHeight: Float) {

        mTickRadius = tickHeight
        createBar()
    }

    /**
     * Set the weight of the bar line and the tick lines in the range bar.

     * @param barWeight Float specifying the weight of the bar and tick lines in
     * *                  px.
     */
    fun setBarWeight(barWeight: Float) {

        mBarWidth = barWeight
        createBar()
    }

    /**
     * Set the color of the bar line and the tick lines in the range bar.

     * @param barColor Integer specifying the color of the bar line.
     */
    fun setBarColor(barColor: Int) {
        mBarColor = barColor
        createBar()
    }

    /**
     * Set the color of the pins.

     * @param pinColor Integer specifying the color of the pin.
     */
    fun setPinColor(pinColor: Int) {
        mTickColor = pinColor
        createPins()
    }

    /**
     * Set the color of the text within the pin.

     * @param textColor Integer specifying the color of the text in the pin.
     */
    fun setPinTextColor(textColor: Int) {
        mTextColor = textColor
        createPins()
    }

    /**
     * Set if the view is a range bar or a seek bar.

     * @param isRangeBar Boolean - true sets it to rangebar, false to seekbar.
     */
    fun setRangeBarEnabled(isRangeBar: Boolean) {
        this.isRangeBar = isRangeBar
        invalidate()
    }


    /**
     * Set if the pins should dissapear after released

     * @param arePinsTemporary Boolean - true if pins shoudl dissapear after released, false to
     * *                         stay
     * *                         drawn
     */
    fun setTemporaryPins(arePinsTemporary: Boolean) {
        mArePinsTemporary = arePinsTemporary
        invalidate()
    }


    /**
     * Set the color of the ticks.

     * @param tickColor Integer specifying the color of the ticks.
     */
    fun setTickColor(tickColor: Int) {

        mTickColor = tickColor
        createBar()
    }

    /**
     * Set the color of the selector.

     * @param selectorColor Integer specifying the color of the ticks.
     */
    fun setSelectorColor(selectorColor: Int) {
        mTickColor = selectorColor
        createPins()
    }

    /**
     * Set the weight of the connecting line between the thumbs.

     * @param connectingLineWeight Float specifying the weight of the connecting
     * *                             line.
     */
    fun setConnectingLineWeight(connectingLineWeight: Float) {

        mBarWidth = connectingLineWeight
        createConnectingLine()
    }

    /**
     * Set the color of the connecting line between the thumbs.

     * @param connectingLineColor Integer specifying the color of the connecting
     * *                            line.
     */
    fun setConnectingLineColor(connectingLineColor: Int) {

        mBarColor = connectingLineColor
        createConnectingLine()
    }

    /**
     * If this is set, the thumb images will be replaced with a circle of the
     * specified radius. Default width = 20dp.

     * @param pinRadius Float specifying the radius of the thumbs to be drawn.
     */
    fun setPinRadius(pinRadius: Float) {
        mExpandedPinRadius = pinRadius
        createPins()
    }

    /**
     * Gets the start tick.

     * @return the start tick.
     */
    /**
     * Sets the start tick in the RangeBar.

     * @param tickStart Integer specifying the number of ticks.
     */
    // Prevents resetting the indices when creating new activity, but
    // allows it on the first setting.
    var tickStart: Float
        get() = mTickStart
        set(tickStart) {
            val tickCount = ((mTickEnd - tickStart) / mTickInterval).toInt() + 1
            if (isValidTickCount(tickCount)) {
                this.tickCount = tickCount
                mTickStart = tickStart
                if (mFirstSetTickCount) {
                    leftIndex = 0
                    rightIndex = this.tickCount - 1

                    if (mListener != null) {
                        mListener!!.onRangeChangeListener(this, leftIndex, rightIndex,
                                getPinValue(leftIndex),
                                getPinValue(rightIndex))
                    }
                }
                if (indexOutOfRange(leftIndex, rightIndex)) {
                    leftIndex = 0
                    rightIndex = this.tickCount - 1

                    if (mListener != null) {
                        mListener!!.onRangeChangeListener(this, leftIndex, rightIndex,
                                getPinValue(leftIndex),
                                getPinValue(rightIndex))
                    }
                }

                createBar()
                createPins()
            } else {
                Log.e(TAG, "tickCount less than 2; invalid tickCount.")
                throw IllegalArgumentException("tickCount less than 2; invalid tickCount.")
            }
        }

    /**
     * Gets the end tick.

     * @return the end tick.
     */
    /**
     * Sets the end tick in the RangeBar.

     * @param tickEnd Integer specifying the number of ticks.
     */
    // Prevents resetting the indices when creating new activity, but
    // allows it on the first setting.
    var tickEnd: Float
        get() = mTickEnd
        set(tickEnd) {
            val tickCount = ((tickEnd - mTickStart) / mTickInterval).toInt() + 1
            if (isValidTickCount(tickCount)) {
                this.tickCount = tickCount
                mTickEnd = tickEnd
                if (mFirstSetTickCount) {
                    leftIndex = 0
                    rightIndex = this.tickCount - 1

                    if (mListener != null) {
                        mListener!!.onRangeChangeListener(this, leftIndex, rightIndex,
                                getPinValue(leftIndex), getPinValue(rightIndex))
                    }
                }
                if (indexOutOfRange(leftIndex, rightIndex)) {
                    leftIndex = 0
                    rightIndex = this.tickCount - 1

                    if (mListener != null) {
                        mListener!!.onRangeChangeListener(this, leftIndex, rightIndex,
                                getPinValue(leftIndex), getPinValue(rightIndex))
                    }
                }

                createBar()
                createPins()
            } else {
                Log.e(TAG, "tickCount less than 2; invalid tickCount.")
                throw IllegalArgumentException("tickCount less than 2; invalid tickCount.")
            }
        }

    /**
     * Sets the location of the pins according by the supplied index.
     * Numbered from 0 to mTickCount - 1 from the left.

     * @param leftPinIndex  Integer specifying the index of the left pin
     * *
     * @param rightPinIndex Integer specifying the index of the right pin
     */
    fun setRangePinsByIndices(leftPinIndex: Int, rightPinIndex: Int) {
        if (indexOutOfRange(leftPinIndex, rightPinIndex)) {
            Log.e(TAG,
                    "Pin index left " + leftPinIndex + ", or right " + rightPinIndex
                            + " is out of bounds. Check that it is greater than the minimum ("
                            + mTickStart + ") and less than the maximum value ("
                            + mTickEnd + ")")
            throw IllegalArgumentException(
                    "Pin index left " + leftPinIndex + ", or right " + rightPinIndex
                            + " is out of bounds. Check that it is greater than the minimum ("
                            + mTickStart + ") and less than the maximum value ("
                            + mTickEnd + ")")
        } else {

            if (mFirstSetTickCount) {
                mFirstSetTickCount = false
            }
            leftIndex = leftPinIndex
            rightIndex = rightPinIndex
            createPins()

            if (mListener != null) {
                mListener!!.onRangeChangeListener(this, leftIndex, rightIndex,
                        getPinValue(leftIndex), getPinValue(rightIndex))
            }
        }

        invalidate()
        requestLayout()
    }

    /**
     * Sets the location of pin according by the supplied index.
     * Numbered from 0 to mTickCount - 1 from the left.

     * @param pinIndex Integer specifying the index of the seek pin
     */
    fun setSeekPinByIndex(pinIndex: Int) {
        if (pinIndex < 0 || pinIndex > tickCount) {
            Log.e(TAG,
                    "Pin index " + pinIndex
                            + " is out of bounds. Check that it is greater than the minimum ("
                            + 0 + ") and less than the maximum value ("
                            + tickCount + ")")
            throw IllegalArgumentException(
                    "Pin index " + pinIndex
                            + " is out of bounds. Check that it is greater than the minimum ("
                            + 0 + ") and less than the maximum value ("
                            + tickCount + ")")

        } else {

            if (mFirstSetTickCount) {
                mFirstSetTickCount = false
            }
            rightIndex = pinIndex
            createPins()

            if (mListener != null) {
                mListener!!.onRangeChangeListener(this, leftIndex, rightIndex,
                        getPinValue(leftIndex), getPinValue(rightIndex))
            }
        }
        invalidate()
        requestLayout()
    }

    /**
     * Sets the location of pins according by the supplied values.

     * @param leftPinValue  Float specifying the index of the left pin
     * *
     * @param rightPinValue Float specifying the index of the right pin
     */
    fun setRangePinsByValue(leftPinValue: Float, rightPinValue: Float) {
        if (valueOutOfRange(leftPinValue, rightPinValue)) {
            Log.e(TAG,
                    "Pin value left " + leftPinValue + ", or right " + rightPinValue
                            + " is out of bounds. Check that it is greater than the minimum ("
                            + mTickStart + ") and less than the maximum value ("
                            + mTickEnd + ")")
            throw IllegalArgumentException(
                    "Pin value left " + leftPinValue + ", or right " + rightPinValue
                            + " is out of bounds. Check that it is greater than the minimum ("
                            + mTickStart + ") and less than the maximum value ("
                            + mTickEnd + ")")
        } else {
            if (mFirstSetTickCount) {
                mFirstSetTickCount = false
            }

            leftIndex = ((leftPinValue - mTickStart) / mTickInterval).toInt()
            rightIndex = ((rightPinValue - mTickStart) / mTickInterval).toInt()
            createPins()

            if (mListener != null) {
                mListener!!.onRangeChangeListener(this, leftIndex, rightIndex,
                        getPinValue(leftIndex), getPinValue(rightIndex))
            }
        }
        invalidate()
        requestLayout()
    }

    /**
     * Sets the location of pin according by the supplied value.

     * @param pinValue Float specifying the value of the pin
     */
    fun setSeekPinByValue(pinValue: Float) {
        if (pinValue > mTickEnd || pinValue < mTickStart) {
            Log.e(TAG,
                    "Pin value " + pinValue
                            + " is out of bounds. Check that it is greater than the minimum ("
                            + mTickStart + ") and less than the maximum value ("
                            + mTickEnd + ")")
            throw IllegalArgumentException(
                    "Pin value " + pinValue
                            + " is out of bounds. Check that it is greater than the minimum ("
                            + mTickStart + ") and less than the maximum value ("
                            + mTickEnd + ")")

        } else {
            if (mFirstSetTickCount) {
                mFirstSetTickCount = false
            }
            rightIndex = ((pinValue - mTickStart) / mTickInterval).toInt()
            createPins()

            if (mListener != null) {
                mListener!!.onRangeChangeListener(this, leftIndex, rightIndex,
                        getPinValue(leftIndex), getPinValue(rightIndex))
            }
        }
        invalidate()
        requestLayout()
    }

    /**
     * Gets the value of the left pin.

     * @return the string value of the left pin.
     */
    val leftPinValue: String
        get() = getPinValue(leftIndex)

    /**
     * Gets the value of the right pin.

     * @return the string value of the right pin.
     */
    val rightPinValue: String
        get() = getPinValue(rightIndex)

    /**
     * Gets the tick interval.

     * @return the tick interval
     */
    val tickInterval: Double
        get() = mTickInterval.toDouble()

    override fun setEnabled(enabled: Boolean) {
        mEnabled = enabled

        createBar()
        createPins()
        createConnectingLine()
        super.setEnabled(enabled)
    }

    fun setPinTextFormatter(pinTextFormatter: PinTextFormatter) {
        this.mPinTextFormatter = pinTextFormatter
    }

    // Private Methods /////////////////////////////////////////////////////////

    /**
     * Does all the functions of the constructor for RangeBar. Called by both
     * RangeBar constructors in lieu of copying the code for each constructor.

     * @param context Context from the constructor.
     * *
     * @param attrs   AttributeSet from the constructor.
     */
    private fun rangeBarInit(context: Context, attrs: AttributeSet) {
        //TODO tick value map
        if (mTickMap == null) {
            mTickMap = HashMap<Float, String>()
        }
        val ta = context.obtainStyledAttributes(attrs, R.styleable.RangeBar, 0, 0)

        try {

            // Sets the values of the user-defined attributes based on the XML
            // attributes.

            val tickStart = ta.getFloat(R.styleable.RangeBar_tickStart, DEFAULT_TICK_START)
            val tickEnd = ta.getFloat(R.styleable.RangeBar_tickEnd, DEFAULT_TICK_END)
            val tickInterval = ta.getFloat(R.styleable.RangeBar_tickInterval, DEFAULT_TICK_INTERVAL)
            val tickCount = ((tickEnd - tickStart) / tickInterval).toInt() + 1
            if (isValidTickCount(tickCount)) {

                // Similar functions performed above in setTickCount; make sure
                // you know how they interact
                this.tickCount = tickCount
                mTickStart = tickStart
                mTickEnd = tickEnd
                mTickInterval = tickInterval
                leftIndex = 0
                rightIndex = this.tickCount - 1

                if (mListener != null) {
                    mListener!!.onRangeChangeListener(this, leftIndex, rightIndex,
                            getPinValue(leftIndex),
                            getPinValue(rightIndex))
                }

            } else {

                Log.e(TAG, "tickCount less than 2; invalid tickCount. XML input ignored.")
            }

            mPinDrawable = ta.getDrawable(R.styleable.RangeBar_pin_icon);

            mTickNotAvailRadius = ta.getDimension(R.styleable.RangeBar_tickNotAvailRadius, DEFAULT_TICK_HEIGHT_DP)
            mTickRadius = ta.getDimension(R.styleable.RangeBar_tickRadius, DEFAULT_TICK_HEIGHT_DP)
            mTickSelectedRadius = ta.getDimension(R.styleable.RangeBar_tickSelectedRadius, DEFAULT_TICK_HEIGHT_DP)
            mTickSelectorRadius = ta.getDimension(R.styleable.RangeBar_tickSelectorRadius, DEFAULT_TICK_HEIGHT_DP)

            mTickNotAvailColor = ta.getColor(R.styleable.RangeBar_tickNotAvailColor, DEFAULT_BAR_COLOR)
            mTickColor = ta.getColor(R.styleable.RangeBar_tickColor, DEFAULT_BAR_COLOR)
            mTickSelectedColor = ta.getColor(R.styleable.RangeBar_tickSelectedColor, DEFAULT_BAR_COLOR)
            mTickSelectorColor = ta.getColor(R.styleable.RangeBar_tickSelectorColor, DEFAULT_BAR_COLOR)

            mBarNotAvailWidth = ta.getDimension(R.styleable.RangeBar_barNotAvailWidth, DEFAULT_TICK_HEIGHT_DP)
            mBarWidth = ta.getDimension(R.styleable.RangeBar_barWidth, DEFAULT_TICK_HEIGHT_DP)
            mBarSelectedWidth = ta.getDimension(R.styleable.RangeBar_barSelectedWidth, DEFAULT_TICK_HEIGHT_DP)

            mBarNotAvailColor = ta.getColor(R.styleable.RangeBar_barNotAvailColor, DEFAULT_BAR_COLOR)
            mBarColor = ta.getColor(R.styleable.RangeBar_barColor, DEFAULT_BAR_COLOR)
            mBarSelectedColor = ta.getColor(R.styleable.RangeBar_barSelectedColor, DEFAULT_BAR_COLOR)

            mTextColor = ta.getColor(R.styleable.RangeBar_textColor, DEFAULT_TEXT_COLOR)

            mExpandedPinRadius = ta.getDimension(R.styleable.RangeBar_pinRadius, TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    DEFAULT_EXPANDED_PIN_RADIUS_DP, resources.displayMetrics))
            mPinPadding = ta.getDimension(R.styleable.RangeBar_pinPadding, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_PIN_PADDING_DP,
                    resources.displayMetrics))
            mBarPaddingBottom = ta.getDimension(R.styleable.RangeBar_rangeBarPaddingBottom,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                            DEFAULT_BAR_PADDING_BOTTOM_DP, resources.displayMetrics))
            isRangeBar = ta.getBoolean(R.styleable.RangeBar_rangeBar, true)
            mArePinsTemporary = ta.getBoolean(R.styleable.RangeBar_temporaryPins, true)

            val density = resources.displayMetrics.density
            mMinPinFont = ta.getDimension(R.styleable.RangeBar_pinMinFont,
                    DEFAULT_MIN_PIN_FONT_SP * density)
            mMaxPinFont = ta.getDimension(R.styleable.RangeBar_pinMaxFont,
                    DEFAULT_MAX_PIN_FONT_SP * density)

            isRangeBar = ta.getBoolean(R.styleable.RangeBar_rangeBar, true)
        } finally {
            ta.recycle()
        }
    }

    /**
     * Creates a new mBar
     */
    private fun createBar() {

        mBar = Bar(context, marginLeft, yPos, barLength, tickCount, mTickRadius, mTickNotAvailRadius, mTickSelectedRadius, mTickColor, mTickNotAvailColor, mTickSelectedColor,
                mBarWidth, mBarNotAvailWidth, mBarSelectedWidth, mBarColor, mBarNotAvailColor, mBarSelectedColor)

        invalidate()
    }

    /**
     * Creates a new ConnectingLine.
     */
    private fun createConnectingLine() {

        mConnectingLine = ConnectingLine(context,
                yPos,
                mBarWidth,
                mBarColor)
        invalidate()
    }

    /**
     * Creates two new Pins.
     */
    private fun createPins() {
        val ctx = context
        val yPos = yPos
        val density = resources.displayMetrics.density
        val expandedPinRadius = mExpandedPinRadius / density

        if (isRangeBar) {
            mLeftThumb = PinView(ctx)
            mLeftThumb.init(ctx, yPos, expandedPinRadius, mTickSelectorColor, mTextColor, mTickRadius, mTickSelectorColor,
                    mMinPinFont, mMaxPinFont, false, mPinDrawable)
        }
        mRightThumb = PinView(ctx)
        mRightThumb.init(ctx, yPos, expandedPinRadius, mTickSelectorColor, mTextColor, mTickRadius, mTickSelectorColor, mMinPinFont,
                mMaxPinFont, false, mPinDrawable)

        val marginLeft = marginLeft
        val barLength = barLength

        // Initialize thumbs to the desired indices
        if (isRangeBar) {
            mLeftThumb.x = marginLeft + leftIndex / (tickCount - 1).toFloat() * barLength
            mLeftThumb.setXValue(getPinValue(leftIndex))
        }
        mRightThumb.x = marginLeft + rightIndex / (tickCount - 1).toFloat() * barLength
        mRightThumb .setXValue(getPinValue(rightIndex))

        invalidate()
    }

    /**
     * Get marginLeft in each of the public attribute methods.

     * @return float marginLeft
     */
    private val marginLeft: Float
        get() = Math.max(mExpandedPinRadius, mTickRadius)

    /**
     * Get yPos in each of the public attribute methods.

     * @return float yPos
     */
    private val yPos: Float
        get() = height - mBarPaddingBottom

    /**
     * Get barLength in each of the public attribute methods.

     * @return float barLength
     */
    private val barLength: Float
        get() = width - 2 * marginLeft

    /**
     * Returns if either index is outside the range of the tickCount.

     * @param leftThumbIndex  Integer specifying the left thumb index.
     * *
     * @param rightThumbIndex Integer specifying the right thumb index.
     * *
     * @return boolean If the index is out of range.
     */
    private fun indexOutOfRange(leftThumbIndex: Int, rightThumbIndex: Int): Boolean {
        return leftThumbIndex < 0 || leftThumbIndex >= tickCount
                || rightThumbIndex < 0
                || rightThumbIndex >= tickCount
    }

    /**
     * Returns if either value is outside the range of the tickCount.

     * @param leftThumbValue  Float specifying the left thumb value.
     * *
     * @param rightThumbValue Float specifying the right thumb value.
     * *
     * @return boolean If the index is out of range.
     */
    private fun valueOutOfRange(leftThumbValue: Float, rightThumbValue: Float): Boolean {
        return leftThumbValue < mTickStart || leftThumbValue > mTickEnd
                || rightThumbValue < mTickStart || rightThumbValue > mTickEnd
    }

    /**
     * If is invalid tickCount, rejects. TickCount must be greater than 1

     * @param tickCount Integer
     * *
     * @return boolean: whether tickCount > 1
     */
    private fun isValidTickCount(tickCount: Int): Boolean {
        return tickCount > 1
    }

    /**
     * Handles a [android.view.MotionEvent.ACTION_DOWN] event.

     * @param x the x-coordinate of the down action
     * *
     * @param y the y-coordinate of the down action
     */
    private fun onActionDown(x: Float, y: Float) {
        if (isRangeBar) {
            if (!mRightThumb!!.isPressed && mLeftThumb!!.isInTargetZone(x, y)) {

                pressPin(mLeftThumb)

            } else if (!mLeftThumb!!.isPressed && mRightThumb!!.isInTargetZone(x, y)) {

                pressPin(mRightThumb)
            }
        } else {
            if (mRightThumb!!.isInTargetZone(x, y)) {
                pressPin(mRightThumb)
            }
        }
    }

    /**
     * Handles a [android.view.MotionEvent.ACTION_UP] or
     * [android.view.MotionEvent.ACTION_CANCEL] event.

     * @param x the x-coordinate of the up action
     * *
     * @param y the y-coordinate of the up action
     */
    private fun onActionUp(x: Float, y: Float) {
        if (isRangeBar && mLeftThumb.isPressed) {
            releasePin(mLeftThumb, rightIndex)

        } else if (mRightThumb.isPressed) {

            releasePin(mRightThumb, leftIndex)

        } else {
//            var leftThumbXDistance = 0

//            if (isRangeBar) leftThumbXDistance = (Math.abs(mLeftThumb!!.x - x).toInt())
            val leftThumbXDistance = if (isRangeBar) (Math.abs(mLeftThumb.x - x)).toInt() else 0
            val rightThumbXDistance = Math.abs(mRightThumb.x - x)

            if (leftThumbXDistance < rightThumbXDistance) {
                if (isRangeBar) {
                    mLeftThumb.x = x
                    releasePin(mLeftThumb, rightIndex)
                }
            } else {
                mRightThumb.x = x
                releasePin(mRightThumb, leftIndex)
            }

            // Get the updated nearest tick marks for each thumb.
            val newLeftIndex = if (isRangeBar) mBar!!.getNearestTickIndex(mLeftThumb) else 0
            val newRightIndex = mBar!!.getNearestTickIndex(mRightThumb)
            // If either of the indices have changed, update and call the listener.
            if (newLeftIndex != leftIndex || newRightIndex != rightIndex) {

                leftIndex = newLeftIndex
                rightIndex = newRightIndex

                if (mListener != null) {
                    mListener!!.onRangeChangeListener(this, leftIndex, rightIndex,
                            getPinValue(leftIndex),
                            getPinValue(rightIndex))
                }
            }
        }
    }

    /**
     * Handles a [android.view.MotionEvent.ACTION_MOVE] event.

     * @param x the x-coordinate of the move event
     */
    private fun onActionMove(x: Float) {

        // Move the pressed thumb to the new x-position.
        if (isRangeBar && mLeftThumb?.isPressed) {
            movePin(mLeftThumb, x, rightIndex)
        } else if (mRightThumb?.isPressed) {
            movePin(mRightThumb, x, leftIndex)
        }

        // If the thumbs have switched order, fix the references.
        if (isRangeBar && mLeftThumb?.x > mRightThumb?.x) {
            val temp = mLeftThumb
            mLeftThumb = mRightThumb
            mRightThumb = temp
        }

        // Get the updated nearest tick marks for each thumb.
        var newLeftIndex = if (isRangeBar) mBar!!.getNearestTickIndex(mLeftThumb!!) else 0
        var newRightIndex = mBar!!.getNearestTickIndex(mRightThumb!!)

        val componentLeft = left + paddingLeft
        val componentRight = right - paddingRight - componentLeft

        if (x <= componentLeft) {
            newLeftIndex = 0
            movePin(mLeftThumb, mBar?.leftX ?: 0f, rightIndex)
        } else if (x >= componentRight) {
            newRightIndex = tickCount - 1
            movePin(mRightThumb, mBar?.rightX ?: 0f, leftIndex)
        }
        /// end added code
        // If either of the indices have changed, update and call the listener.
        if (newLeftIndex != leftIndex || newRightIndex != rightIndex) {

            leftIndex = newLeftIndex
            rightIndex = newRightIndex
            if (isRangeBar) {
                mLeftThumb.setXValue(getPinValue(leftIndex))
            }
            mRightThumb.setXValue(getPinValue(rightIndex))

            if (mListener != null) {
                mListener?.onRangeChangeListener(this, leftIndex, rightIndex,
                        getPinValue(leftIndex),
                        getPinValue(rightIndex))
            }
        }
    }

    /**
     * Set the thumb to be in the pressed state and calls invalidate() to redraw
     * the canvas to reflect the updated state.

     * @param thumb the thumb to press
     */
    private fun pressPin(thumb: PinView) {
        if (mFirstSetTickCount) {
            mFirstSetTickCount = false
        }
        if (mArePinsTemporary) {
            val animator = ValueAnimator.ofFloat(0f, mExpandedPinRadius)
            animator.addUpdateListener { animation ->
                mThumbRadiusDP = animation.animatedValue as Float
                thumb.setSize(mThumbRadiusDP, mPinPadding * animation.animatedFraction)
                invalidate()
            }
            animator.start()
        }

        thumb.press()
    }

    /**
     * Set the thumb to be in the normal/un-pressed state and calls invalidate()
     * to redraw the canvas to reflect the updated state.

     * @param thumb the thumb to release
     */
    private fun releasePin(thumb: PinView, ancorIdx: Int?) {
        var nearestTick = 0
        nearestTick = mBar!!.getNearestTickIndex(thumb)

        val ancorMinIdx = if (ancorIdx != null) ancorIdx-mMinDistance else Int.MAX_VALUE
        val ancorMaxIdx = if (ancorIdx != null) ancorIdx+mMinDistance else Int.MAX_VALUE

        mAvailableRange?.apply {
            if (nearestTick < startTick)
                nearestTick = startTick

            if (nearestTick > endTick)
                nearestTick = endTick
        }

        if (nearestTick <= ancorMinIdx || nearestTick >= ancorMaxIdx) {
            thumb.x = mBar!!.calcTickX(nearestTick)
            val tickIndex = mBar!!.getNearestTickIndex(thumb)
            thumb.setXValue(getPinValue(tickIndex))

            if (mArePinsTemporary) {
                val animator = ValueAnimator.ofFloat(mExpandedPinRadius, 0f)
                animator.addUpdateListener { animation ->
                    mThumbRadiusDP = animation.animatedValue as Float
                    thumb.setSize(mThumbRadiusDP,
                            mPinPadding - mPinPadding * animation.animatedFraction)
                    invalidate()
                }
                animator.start()
            } else {
                invalidate()
            }
        }
        thumb.release()
    }

    /**
     * Set the value on the thumb pin, either from map or calculated from the tick intervals
     * Integer check to format decimals as whole numbers

     * @param tickIndex the index to set the value for
     */
    private fun getPinValue(tickIndex: Int): String {
        if (mPinTextListener != null) {
            return mPinTextListener!!.getPinValue(this, tickIndex)
        }
        val tickValue = if (tickIndex == tickCount - 1)
            mTickEnd
        else
            tickIndex * mTickInterval + mTickStart
        var xValue: String? = mTickMap!![tickValue]
        if (xValue == null) {
            if (tickValue.toDouble() == Math.ceil(tickValue.toDouble())) {
                xValue = tickValue.toInt().toString()
            } else {
                xValue = tickValue.toString()
            }
        }
        return mPinTextFormatter.getText(xValue)
    }

    /**
     * Moves the thumb to the given x-coordinate.

     * @param thumb the thumb to move
     * *
     * @param x     the x-coordinate to move the thumb to
     */
    private fun movePin(thumb: PinView?, x: Float, ancorIdx: Int?) {

        // If the user has moved their finger outside the range of the bar,
        // do not move the thumbs past the edge.
        val ancorMinX = if (ancorIdx != null) mBar!!.calcTickX(ancorIdx-mMinDistance) else Float.MAX_VALUE
        val ancorMaxX = if (ancorIdx != null) mBar!!.calcTickX(ancorIdx+mMinDistance) else Float.MAX_VALUE

        if (x < mBar!!.leftX || x > mBar!!.rightX) {
            // Do nothing.
        } else if (x <= ancorMaxX && x >= ancorMinX) {
            // Do nothing. TODO: make it stick
        } else if (mAvailableRange != null && x < mBar!!.calcTickX(mAvailableRange!!.startTick)) {
            thumb!!.x = mBar!!.calcTickX(mAvailableRange!!.startTick)
            invalidate()
        } else if (mAvailableRange != null && x > mBar!!.calcTickX(mAvailableRange!!.endTick)) {
            thumb!!.x = mBar!!.calcTickX(mAvailableRange!!.endTick)
            invalidate()
        } else if (thumb != null) {
            thumb.x = x
            invalidate()
        }
    }

    fun checkRanges() {
        if (mAvailableRange != null) {
            var min = leftIndex
            var max = rightIndex
            if (min < mAvailableRange!!.startTick) {
                min = mAvailableRange!!.startTick
            }

            if (rightIndex > mAvailableRange!!.endTick) {
                max = mAvailableRange!!.endTick
            }

            setRangePinsByIndices(min, max)
        }
    }

    // Inner Classes ///////////////////////////////////////////////////////////

    /**
     * A callback that notifies clients when the RangeBar has changed. The
     * listener will only be called when either thumb's index has changed - not
     * for every movement of the thumb.
     */
    interface OnRangeBarChangeListener {

        fun onRangeChangeListener(rangeBar: RangeBar, leftPinIndex: Int,
                                  rightPinIndex: Int, leftPinValue: String, rightPinValue: String)
    }

    interface PinTextFormatter {

        fun getText(value: String): String
    }

    /**
     * @author robmunro
     * * A callback that allows getting pin text exernally
     */
    interface OnRangeBarTextListener {
        fun getPinValue(rangeBar: RangeBar, tickIndex: Int): String
    }

    fun addExtraLine(startTick: Int, endTick: Int, color: Int, width: Float) {
        mExtraLines.add(Range(startTick, endTick, color, width))
    }

    internal fun clearLines() {
        mExtraLines.clear()
    }

    fun setAvailableRange(st: Int, et: Int) {
        mStartAvailTick = st
        mEndAvailTick = et

        if (mAvailableRange == null) {
            mAvailableRange = Range(st, et, 0, 0f)
        }

        mAvailableRange?.apply {
            startTick = mStartAvailTick;
            endTick = mEndAvailTick;
        }
    }

    fun setAvailableRangeColor(color: Int) {
        mBarSelectedColor = color
    }

    fun setAvailableRangeLineWidth(lineWidth: Float) {
        mBarSelectedWidth = lineWidth
    }

    fun removeAvailableRange() {
        mAvailableRange = null
    }

    fun setMinimalDistance(d: Int){
        mMinDistance = d
    }

    inner class Range(st: Int, et: Int, c: Int, w: Float) : Serializable {
        internal var startTick = 0
        internal var endTick = 1
        internal var color = 0
        internal var width = 0f

        init {
            startTick = st
            endTick = et
            color = c
            width = w
        }
    }

    companion object {

        // Member Variables ////////////////////////////////////////////////////////

        private val TAG = "RangeBar"

        // Default values for variables
        private val DEFAULT_TICK_START = 0f

        private val DEFAULT_TICK_END = 5f

        private val DEFAULT_TICK_INTERVAL = 1f

        private val DEFAULT_TICK_HEIGHT_DP = 1f

        private val DEFAULT_PIN_PADDING_DP = 16f

        val DEFAULT_MIN_PIN_FONT_SP = 8f

        val DEFAULT_MAX_PIN_FONT_SP = 24f

        private val DEFAULT_BAR_WEIGHT_PX = 2f

        private val DEFAULT_TICK_WIDTH = 2f

        private val DEFAULT_BAR_COLOR = Color.LTGRAY

        private val DEFAULT_TEXT_COLOR = Color.WHITE

        private val DEFAULT_TICK_COLOR = Color.BLACK

        // Corresponds to material indigo 500.
        private val DEFAULT_PIN_COLOR = 0xff3f51b5.toInt()

        private val DEFAULT_CONNECTING_LINE_WEIGHT_PX = 4f

        // Corresponds to material indigo 500.
        private val DEFAULT_CONNECTING_LINE_COLOR = 0xff3f51b5.toInt()

        private val DEFAULT_EXPANDED_PIN_RADIUS_DP = 12f

        private val DEFAULT_CIRCLE_SIZE_DP = 5f

        private val DEFAULT_BAR_PADDING_BOTTOM_DP = 24f
    }

}
