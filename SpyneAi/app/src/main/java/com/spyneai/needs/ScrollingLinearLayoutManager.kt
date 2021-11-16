package com.spyneai.needs

import android.content.Context
import android.graphics.PointF
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView


class ScrollingLinearLayoutManager(context: Context?, orientation: Int, reverseLayout: Boolean) :
    LinearLayoutManager(context, orientation, reverseLayout) {


    override fun smoothScrollToPosition(
        recyclerView: RecyclerView, state: RecyclerView.State?,
        position: Int
    ) {
        val firstVisibleChild: View = recyclerView.getChildAt(0)
        val itemHeight: Int = firstVisibleChild.getHeight()
        val currentPosition = recyclerView.getChildLayoutPosition(firstVisibleChild)
        var distanceInPixels = Math.abs((currentPosition - position) * itemHeight)
        if (distanceInPixels == 0) {
            distanceInPixels = Math.abs(firstVisibleChild.getY()) as Int
        }
        val smoothScroller = SmoothScroller(recyclerView.context, distanceInPixels, 5000)
        smoothScroller.targetPosition = position
        startSmoothScroll(smoothScroller)
    }

    private class SmoothScroller(context: Context?, distanceInPixels: Int, duration: Int) :
        LinearSmoothScroller(context) {
        private val distanceInPixels: Float
        private val duration: Float
        override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
            return this.computeScrollVectorForPosition(targetPosition)
        }

        override fun calculateTimeForScrolling(dx: Int): Int {
            val proportion = dx.toFloat() / distanceInPixels
            return (duration * proportion).toInt()
        }

        init {
            this.distanceInPixels = distanceInPixels.toFloat()
            this.duration = duration.toFloat()
        }
    }
}