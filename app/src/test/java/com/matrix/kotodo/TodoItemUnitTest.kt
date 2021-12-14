package com.matrix.kotodo

import com.matrix.kotodo.todotxt.TodoItem
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class TodoItemUnitTest {

    val itemStrings: MutableList<String> = mutableListOf<String>(
        "x go home",
        "2020-06-13 make lasagna +cooking +food @health",
        "(A) do +the_locomotion",
        "x x out other items +organization+++@@",
        "(B) 2021-12-08 buy presents @christmas",
        "x 2021-12-23 2021-12-08 buy presents @christmas",
    )

    val expectedStartValues: MutableList<TodoItem.TaskState> = mutableListOf<TodoItem.TaskState>(
        TodoItem.TaskState('x'),
        TodoItem.TaskState(),
        TodoItem.TaskState('A'),
        TodoItem.TaskState('x'),
        TodoItem.TaskState('B'),
        TodoItem.TaskState('x')
    )

    val expectedStartDateValues: MutableList<LocalDate?> = mutableListOf<LocalDate?>(
        null,
        LocalDate.parse("2020-06-13"),
        null,
        null,
        LocalDate.parse("2021-12-08"),
        LocalDate.parse("2021-12-08")
    )

    @Test
    fun all_tests() {
        parses_correctly()
        toStrings_correctly()
    }

    @Test
    fun parses_correctly() {
        for (i in 0..5) {
            val tempItem = TodoItem(itemStrings[i])
            println("Current case: \"${tempItem}\"")
            // check priority
            //println("Testing priority! - #${i+1}")
            assertEquals(tempItem._itemState, expectedStartValues[i])
            // check start date value
            //println("Testing start dates! - #${i+1}")
            assertEquals(tempItem._startDate, expectedStartDateValues[i])
        }
    }

    @Test
    fun toStrings_correctly() {
        for (i in 0..5) {
            // check toString()
            assertEquals(itemStrings[i], TodoItem(itemStrings[i]).toString())
        }
    }
}