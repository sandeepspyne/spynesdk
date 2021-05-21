package com.spyneai.dashboard

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class DotsIndicatorDecoration: RecyclerView.ItemDecoration() {
    private var indicatorHeight = 0
    private var indicatorItemPadding = 0
    private var radius = 0

    private val inactivePaint: Paint = Paint()
    private val activePaint: Paint = Paint()


    fun DotsIndicatorDecorations(
        radius: Int,
        padding: Int,
        indicatorHeight: Int,
        @ColorInt colorInactive: Int,
        @ColorInt colorActive: Int
    ) {
        val strokeWidth: Float = Resources.getSystem().getDisplayMetrics().density * 1
        this.radius = radius
        inactivePaint.setStrokeCap(Paint.Cap.ROUND)
        inactivePaint.setStrokeWidth(strokeWidth)
        inactivePaint.setStyle(Paint.Style.STROKE)
        inactivePaint.setAntiAlias(true)
        inactivePaint.setColor(colorInactive)
        activePaint.setStrokeCap(Paint.Cap.ROUND)
        activePaint.setStrokeWidth(strokeWidth)
        activePaint.setStyle(Paint.Style.FILL)
        activePaint.setAntiAlias(true)
        activePaint.setColor(colorActive)
        indicatorItemPadding = padding
        this.indicatorHeight = indicatorHeight
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        val adapter = parent.adapter ?: return
        val itemCount = adapter.itemCount

        // center horizontally, calculate width and subtract half from center
        val totalLength = (radius * 2 * itemCount).toFloat()
        val paddingBetweenItems = (Math.max(0, itemCount - 1) * indicatorItemPadding).toFloat()
        val indicatorTotalWidth = totalLength + paddingBetweenItems
        val indicatorStartX = (parent.width - indicatorTotalWidth) / 2f

        // center vertically in the allotted space
        val indicatorPosY = parent.height - indicatorHeight / 2f
        drawInactiveDots(c, indicatorStartX, indicatorPosY, itemCount)
        val activePosition: Int
        activePosition = if (parent.layoutManager is GridLayoutManager) {
            (parent.layoutManager as GridLayoutManager?)!!.findFirstVisibleItemPosition()
        } else if (parent.layoutManager is LinearLayoutManager) {
            (parent.layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()
        } else {
            // not supported layout manager
            return
        }
        if (activePosition == RecyclerView.NO_POSITION) {
            return
        }

        // find offset of active page if the user is scrolling
        val activeChild = parent.layoutManager!!.findViewByPosition(activePosition) ?: return
        drawActiveDot(c, indicatorStartX, indicatorPosY, activePosition)
    }

    private fun drawInactiveDots(
        c: Canvas,
        indicatorStartX: Float,
        indicatorPosY: Float,
        itemCount: Int
    ) {
        // width of item indicator including padding
        val itemWidth = (radius * 2 + indicatorItemPadding).toFloat()
        var start = indicatorStartX + radius
        for (i in 0 until itemCount) {
            c.drawCircle(start, indicatorPosY, radius.toFloat(), inactivePaint)
            start += itemWidth
        }
    }

    private fun drawActiveDot(
        c: Canvas, indicatorStartX: Float, indicatorPosY: Float,
        highlightPosition: Int
    ) {
        // width of item indicator including padding
        val itemWidth = (radius * 2 + indicatorItemPadding).toFloat()
        val highlightStart = indicatorStartX + radius + itemWidth * highlightPosition
        c.drawCircle(highlightStart, indicatorPosY, radius.toFloat(), activePaint)
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.bottom = indicatorHeight
    }
}