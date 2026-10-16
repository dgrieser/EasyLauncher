package com.github.droidworksstudio.launcher.listener

import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/*
 * Feeds the touches on a RecyclerView, including those starting on an app label, into a swipe
 * listener. The label keeps receiving taps and long presses until a swipe is recognized; from then
 * on the gesture is taken over so the label gets cancelled instead of launching the app. Taps and
 * long presses beside the labels go to the swipe listener, just like outside the list.
 */
internal class SwipeItemTouchListener(
    private val swipeListener: OnSwipeTouchListener,
) : RecyclerView.SimpleOnItemTouchListener() {

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        if (e.actionMasked == MotionEvent.ACTION_DOWN)
            swipeListener.tapsEnabled = !isOnClickableView(rv, e)
        swipeListener.onTouch(rv, e)
        return swipeListener.swipeHandled
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
        swipeListener.onTouch(rv, e)
    }

    private fun isOnClickableView(rv: RecyclerView, e: MotionEvent): Boolean {
        val child = rv.findChildViewUnder(e.x, e.y) ?: return false
        return findClickableView(child, rv, e.x.toInt(), e.y.toInt())
    }

    private fun findClickableView(view: View, rv: RecyclerView, x: Int, y: Int): Boolean {
        if (view.visibility != View.VISIBLE) return false
        val bounds = Rect()
        view.getDrawingRect(bounds)
        rv.offsetDescendantRectToMyCoords(view, bounds)
        if (!bounds.contains(x, y)) return false
        if (view.isClickable || view.isLongClickable) return true
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                if (findClickableView(view.getChildAt(i), rv, x, y)) return true
            }
        }
        return false
    }
}
