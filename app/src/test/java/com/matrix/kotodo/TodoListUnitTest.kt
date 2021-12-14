package com.matrix.kotodo

import com.matrix.kotodo.todotxt.TodoList
import junit.framework.Assert.assertEquals
import org.junit.Test

class TodoListUnitTest {

    private var itemList = TodoList()
    private val initStrings = listOf(
        "x go home",
        "2020-06-13 make lasagna +cooking +food @health",
        "(A) do +the_locomotion",
        "x x out other items +organization+++@@",
        "(B) 2021-12-08 buy presents @christmas",
        "x 2021-12-23 2021-12-08 buy presents @christmas",
    )

    init {
        this.itemList.listFromLines(initStrings)
    }

    @Test
    fun runAllTests() {
        sortsCorrectly()
    }

    @Test
    fun sortsCorrectly() {
        val tempList = TodoList()
        for (initString: String in initStrings.shuffled()) {
            tempList.addItem(initString)
        }
        assertEquals(tempList, itemList)
        println(itemList)
        println(tempList)
    }
}