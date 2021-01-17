package com.jasen.kimjaeseung.morningbees.setting.beemember.formanager

import android.graphics.Canvas
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_bee_member_for_manager.view.*
import kotlin.math.max
import kotlin.math.min

class BeeMemberSwipeHelperCallback : ItemTouchHelper.Callback(){

    // MARK:~ Properties

    private var currentPosition: Int? = null
    private var previousPosition: Int? = null
    private var currentDx = 0f
    private var clamp = 0f

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return makeMovementFlags(0, LEFT or RIGHT)
    }

    // MARK:~ Drag (not used)

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    // MARK:~ Swipe

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }

    // MARK:~ ItemTouchUIUtil for Swiping itemBeeMemberSwipeView

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        currentDx = 0f
        getDefaultUIUtil().clearView(getView(viewHolder))
        previousPosition = viewHolder.adapterPosition
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        viewHolder?.let {
            currentPosition = viewHolder.adapterPosition
            getDefaultUIUtil().onSelected(getView(it))
        }
    }

    // MARK:~ Block Escape from View

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return defaultValue * 10
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        val isClamped = getTag(viewHolder)
        // 현재 View가 고정되어있지 않고 사용자가 -clamp 이상 swipe시 isClamped true로 변경, 아닐시 false로 변경

        setTag(viewHolder, !isClamped && currentDx <= -clamp)
        return 2f
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if(actionState == ACTION_STATE_SWIPE){
            val view = getView(viewHolder)
            val isClamped = getTag(viewHolder)
            val x = clampViewPositionHorizontal(view, dX, isClamped, isCurrentlyActive)

            currentDx = x
            getDefaultUIUtil().onDraw(
                c, recyclerView, view, x, dY, actionState, isCurrentlyActive
            )
        }
    }

    private fun clampViewPositionHorizontal(
        view: View,
        dX: Float,
        isClamped: Boolean,
        isCurrentlyActive: Boolean
    ) : Float {
        val min = -view.width.toFloat()/3
        val max = 0f    // Right 방향으로 swipe 막음

        val x = if (isClamped) {
            if (isCurrentlyActive) dX - clamp else -clamp
        } else {
            dX
        }

        return min(max(min, x), max)
    }

    private fun getView(viewHolder: RecyclerView.ViewHolder): View {
        return (viewHolder as BeeMemberForManagerAdapter.BeeMemberViewHolderForManager).itemView.itemBeeMemberSwipeView
    }

    private fun setTag(viewHolder: RecyclerView.ViewHolder, isClamped: Boolean){
        viewHolder.itemView.tag = isClamped
    }

    private fun getTag(viewHolder: RecyclerView.ViewHolder) : Boolean {
        return viewHolder.itemView.tag as? Boolean ?: false
    }

    fun setClamp(clamp: Float) {
        this.clamp = clamp
    }

    // MARK:~ Take Off

    fun removePreviousClamp(recyclerView: RecyclerView) {
        Log.d(TAG,"-------removePreviousClamp-------")
        Log.d(TAG, "currentPosition $currentPosition")
        Log.d(TAG, "previousPosition $previousPosition")

        if (currentPosition == previousPosition){
            return
        }

        previousPosition?.let {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(it) ?: return
            Log.d(TAG, "removePreviousClamp/it: $it")
            getView(viewHolder).translationX = 0f
            setTag(viewHolder, false)
            previousPosition = null
        }
    }

    companion object {
        private const val TAG = "BeeMemberCallback"
    }
}