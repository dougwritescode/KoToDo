package com.matrix.kotodo.itemedit

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.matrix.kotodo.databinding.TagFragmentBinding

class TagFragment(tagTitle: String, tagStatus: Boolean) : Fragment() {

    private var _title = MutableLiveData(tagTitle)
    val title: LiveData<String> = _title
    private var _status = MutableLiveData(false)
    val status: LiveData<Boolean> = _status
    private var _color = MutableLiveData(0)
    val color: LiveData<Int> = _color
    var binding: TagFragmentBinding? = null

    init {
        this._title.value = tagTitle
        this._status.value = tagStatus
        this._color.value = 0
    }

    fun setColor(newColor: Int) {
        this._color.value = newColor
    }
}