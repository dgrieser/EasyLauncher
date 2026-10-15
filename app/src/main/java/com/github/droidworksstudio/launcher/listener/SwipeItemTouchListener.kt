package com.github.droidworksstudio.launcher.listener

import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

/*
 * Feeds the touches on a RecyclerView, including those starting on an app label, into a swipe
 * listener. The label keeps receiving taps and long presses until a swipe is recognized; from then
 * on the gesture is taken over so the label gets cancelled instead of launching the app.
 */
internal class SwipeItemTouchListener(
    private val swipeListener: OnSwipeTouchListener,
) : RecyclerView.SimpleOnItemTouchListener() {

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        swipeListener.onTouch(rv, e)
        return swipeListener.swipeHandled
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
        swipeListener.onTouch(rv, e)
    }
}
