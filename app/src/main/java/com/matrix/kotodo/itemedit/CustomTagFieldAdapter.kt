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
import com.matrix.kotodo.databinding.CustomTagFragmentBinding
import com.matrix.kotodo.itemedit.CustomTagFieldAdapter.CustomTagViewHolder
import java.util.*

class CustomTagFieldAdapter(
    private val tapListener: (Int) -> Unit,
    private val tagColor: Int
) : ListAdapter<Pair<String, String>, CustomTagViewHolder>(DiffCallBack) {

//    init {
//        Log.d("CustomTagFieldAdapter", "Adapter Initialized!")
//    }

    companion object DiffCallBack : DiffUtil.ItemCallback<Pair<String, String>>() {

        override fun areItemsTheSame(
            oldItem: Pair<String, String>,
            newItem: Pair<String, String>
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: Pair<String, String>,
            newItem: Pair<String, String>
        ): Boolean {
            return oldItem == newItem
        }

        @JvmStatic
        @BindingAdapter("customTagsFlow")
        fun customTagsFlow(recyclerView: RecyclerView, tags: LiveData<SortedMap<String, String>>) {
            val tempList = tags.value?.entries?.toList()?.map { it.toPair() }
            val adapter = recyclerView.adapter as CustomTagFieldAdapter
            adapter.submitList(tempList)
        }

        @JvmStatic
        @BindingAdapter("customTagFragmentBackground")
        fun customTagFragmentBackground(parent: ConstraintLayout, color: LiveData<Int>) {
            parent.findViewById<ImageView>(R.id.tag_front).setColorFilter(color.value!!)
            parent.findViewById<TextView>(R.id.tag_title).setBackgroundColor(color.value!!)
            parent.findViewById<ImageView>(R.id.tag_back).setColorFilter(color.value!!)
        }
    }

    class CustomTagViewHolder(
        private val binding: CustomTagFragmentBinding
    ): RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(
            pair: Pair<String, String>,
            position: Int,
            listener: (Int) -> Unit,
            newColor: Int
        ) {
            val tagFragment = CustomTagFragment(pair.toList().joinToString(separator = ":"))
            tagFragment.setColor(newColor)
            binding.model = tagFragment
            binding.root.setOnClickListener{ listener(position) }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): CustomTagViewHolder {
        return CustomTagViewHolder(
            CustomTagFragmentBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun onBindViewHolder(
        holder: CustomTagViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position), position, tapListener, tagColor)
    }

    /**
     * Adds a new tag view to the view
     *
     * @param newKey before-colon custom tag part
     * @param newValue after-colon custom tag part
     */
    fun addItem(
        newKey: String,
        newValue: String
    ) {
        val oldList = currentList.toMutableList()
        oldList.add(Pair(newKey, newValue))
        oldList.sortedBy { it.first }
        this.submitList(oldList.toList())
        this.notifyDataSetChanged()
    }
}

