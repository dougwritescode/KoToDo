package com.matrix.kotodo.mainlist

import android.annotation.SuppressLint
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.matrix.kotodo.databinding.TodoListItemBinding
import com.matrix.kotodo.mainlist.TodoListItemAdapter.TodoItemViewHolder
import com.matrix.kotodo.todotxt.TodoItem
import kotlin.math.roundToInt

class TodoListItemAdapter(
    private val tapCallback: (Int) -> Unit,
    private val swipeCallback: (Int) -> Unit,
) : ListAdapter<TodoItem, TodoItemViewHolder>(DiffCallBack) {

//    init {
//        Log.d("TodoListItemAdapter", "Adapter Initialized!")
//    }

    companion object DiffCallBack : DiffUtil.ItemCallback<TodoItem>() {
        override fun areItemsTheSame(
            oldItem: TodoItem,
            newItem: TodoItem,
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: TodoItem,
            newItem: TodoItem,
        ): Boolean {
            return oldItem.initString == newItem.initString
        }

        @JvmStatic
        @BindingAdapter("todoListItems")
        fun todoListItems(recyclerView: RecyclerView, data: List<TodoItem>) {
            val adapter = recyclerView.adapter as TodoListItemAdapter
            adapter.submitList(data)
        }
    }

    class TodoItemViewHolder(
        private val binding: TodoListItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            private const val swipeMinDistance = 30
            private const val swipeMaxDistance = 90
            private var mVelocityTracker: VelocityTracker? = null
            private var swipeDistance: Float = 0.0F
            private var swipeMaxTravel = 0.0F
        }

        private var strikeThrough = false
        private var strikeThroughLock = false

        @SuppressLint("ClickableViewAccessibility", "Recycle")
        fun bind(
            item: TodoItem,
            itemIndex: Int,
            tapFunction: (Int) -> Unit,
            swipeFunction: (Int) -> Unit,
        ) {

            fun updateStrikeThrough(newState: Boolean) {
                when (newState) {
                    true -> {
                        if (binding.itemObjective.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG <= 0)
                            binding.itemObjective.paintFlags -= Paint.STRIKE_THRU_TEXT_FLAG
                        binding.priorityOrFinished.text = "x"
                    }
                    false -> {
                        if (binding.itemObjective.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG > 0)
                            binding.itemObjective.paintFlags += Paint.STRIKE_THRU_TEXT_FLAG
                        binding.priorityOrFinished.text = item.itemStateAsString()
                    }
                }
            }

            binding.task = item
            strikeThrough = item.taskState.state == 'x'
            strikeThroughLock = strikeThrough

            updateStrikeThrough(strikeThrough)

            binding.taskContainer.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
//                        Log.d("Debug", "Pressed")
                        mVelocityTracker?.clear()
                        mVelocityTracker = mVelocityTracker ?: VelocityTracker.obtain()
                        mVelocityTracker?.addMovement(event)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        mVelocityTracker!!.addMovement(event)
                        mVelocityTracker!!.computeCurrentVelocity(1000)
                        val pointerId: Int = event.getPointerId(event.actionIndex)
                        val currentVelocity = mVelocityTracker!!.getXVelocity(pointerId)
//                        Log.d("Debug", "distance: $swipeDistance")
                        swipeDistance += currentVelocity / 140.0F
                        if (swipeDistance > swipeMaxTravel) {
                            swipeMaxTravel = swipeDistance
                        }
                        if (!strikeThroughLock) {
                            updateStrikeThrough(swipeDistance >= swipeMinDistance)
                            if (swipeDistance < swipeMaxDistance) {
                                binding.taskContainer.updatePadding(swipeDistance.roundToInt(),
                                    0,
                                    0,
                                    0)
                            }
                        }
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        when {
                            swipeDistance > swipeMinDistance -> {
//                                Log.d("Debug", "swiped $itemIndex")
                                if (!strikeThroughLock)
                                    swipeFunction(itemIndex)
                            }
                            swipeMaxTravel > swipeMinDistance -> {
//                                Log.d("Debug", "released $itemIndex")
                            }
                            else -> {
//                                Log.d("Debug", "tapped $itemIndex")
                                tapFunction(itemIndex)
                            }
                        }
                        mVelocityTracker?.recycle()
                        mVelocityTracker = null
                        swipeMaxTravel = 0.0F
                        swipeDistance = 0.0F
                        binding.taskContainer.updatePadding(0, 0, 0, 0)
                    }
                }
                true
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): TodoItemViewHolder {
        return TodoItemViewHolder(
            TodoListItemBinding.inflate(
                LayoutInflater.from(parent.context)
            ),
        )
    }

    override fun onBindViewHolder(holder: TodoItemViewHolder, position: Int) {
        holder.bind(getItem(position), position, tapCallback, swipeCallback)
    }
}