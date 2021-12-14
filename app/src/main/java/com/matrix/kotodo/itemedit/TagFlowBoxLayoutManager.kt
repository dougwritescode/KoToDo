package com.matrix.kotodo.itemedit

import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

class TagFlowBoxLayoutManager(thisContext: android.content.Context?): FlexboxLayoutManager(thisContext) {

    init {
        this.flexDirection = FlexDirection.ROW
        this.justifyContent = JustifyContent.FLEX_START
    }
}