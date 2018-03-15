package com.hellowo.colosseum.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

class PagingControlableViewPager : ViewPager {

    private var isPagingEnabled: Boolean = false

    constructor(context: Context) : super(context) {
        this.isPagingEnabled = true
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        this.isPagingEnabled = true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (this.isPagingEnabled) {
            super.onTouchEvent(event)
        } else false
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (this.isPagingEnabled) {
            try {
                return super.onInterceptTouchEvent(event)
            } catch (e: Exception) {
            }

        }
        return false
    }

    fun setPagingEnabled(b: Boolean) {
        this.isPagingEnabled = b
    }
}