package com.matrix.kotodo.itemedit

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.matrix.kotodo.R
import com.matrix.kotodo.databinding.TagFragmentBinding
import com.matrix.kotodo.itemedit.TagFieldAdapter.TagViewHolder
import java.util.*

class TagFieldAdapter(
    private val tapListener: (Int) -> Unit,
    private val tagColor: Int
) : ListAdapter<Pair<String, Boolean>, TagViewHolder>(DiffCallBack) {

    companion object DiffCallBack : DiffUtil.ItemCallback<Pair<String, Boolean>>() {
        override fun areItemsTheSame(
            oldItem: Pair<String, Boolean>,
            newItem: Pair<String, Boolean>
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: Pair<String, Boolean>,
            newItem: Pair<String, Boolean>
        ): Boolean {
            return oldItem == newItem
        }

        @JvmStatic
        @BindingAdapter("tagsFlow")
        fun tagsFlow(recyclerView: RecyclerView, tags: LiveData<SortedMap<String, Boolean>>) {
            val tempList = tags.value?.entries?.toList()?.map { it.toPair() }
            val adapter = recyclerView.adapter as TagFieldAdapter
            adapter.submitList(tempList)
        }

        @JvmStatic
        @BindingAdapter("tagFragmentBackground")
        fun tagFragmentBackground(parent: ConstraintLayout, color: LiveData<Int>) {
            parent.findViewById<ImageView>(R.id.tag_front).setColorFilter(color.value!!)
            parent.findViewById<ImageView>(R.id.tag_check_box).setBackgroundColor(color.value!!)
            parent.findViewById<TextView>(R.id.tag_title).setBackgroundColor(color.value!!)
            parent.findViewById<ImageView>(R.id.tag_back).setColorFilter(color.value!!)
        }

        @JvmStatic
        @BindingAdapter("tagChecked")
        fun tagChecked(image: ImageView, status: Boolean) {
            when (status) {
                true -> image.setImageResource(R.drawable.ic_outline_check_box_24)
                false -> image.setImageResource(R.drawable.ic_baseline_check_box_outline_blank_24)
            }
        }
    }

    class TagViewHolder(
        private val binding: TagFragmentBinding
    ): RecyclerView.ViewHolder(binding.root) {

        var state: Boolean = false

        fun bind(
            tagState: Pair<String, Boolean>,
            position: Int,
            listener: (Int) -> Unit,
            newColor: Int = R.color.appPrimary
        ) {
            val tagFragment = TagFragment(tagState.first, tagState.second)
            state = tagState.second
            tagFragment.setColor(newColor)
            binding.model = tagFragment
            binding.imageHolder.setOnClickListener {
                listener(position)
                this.toggleCheckBox()
            }
        }

        private fun toggleCheckBox() {
            state = state.not()
            when (state) {
                true -> binding.tagCheckBox.setImageResource(R.drawable.ic_outline_check_box_24)
                false -> binding.tagCheckBox.setImageResource(R.drawable.ic_baseline_check_box_outline_blank_24)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TagViewHolder {
        val binding = TagFragmentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TagViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: TagViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position), position, tapListener, tagColor)
    }

    fun addItem(newPair: Pair<String, Boolean>) {
        val oldList = currentList.toMutableList()
        oldList.add(newPair)
        oldList.sortedBy { it.first }
        this.submitList(oldList.toList())
        this.notifyDataSetChanged()
    }
}