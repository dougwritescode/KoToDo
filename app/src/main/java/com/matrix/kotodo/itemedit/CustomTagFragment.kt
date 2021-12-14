package com.matrix.kotodo.itemedit

import android.app.Activity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.matrix.kotodo.R
import com.matrix.kotodo.databinding.CustomTagFragmentBinding

class CustomTagFragment(tagTitle: String) : Fragment() {

    private var _title = MutableLiveData(tagTitle)
    val title: LiveData<String> = _title
    private var _color = MutableLiveData(0)
    val color: LiveData<Int> = _color
    private var binding: CustomTagFragmentBinding? = null

    init {
        this._title.value = tagTitle
        this._color.value = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dataBinding: CustomTagFragmentBinding = DataBindingUtil.setContentView(
            this.context as Activity,
            R.layout.custom_tag_fragment
        )
        dataBinding.lifecycleOwner = this
        binding = dataBinding
    }

    fun setColor(newColor: Int) {
        this._color.value = newColor
    }
}