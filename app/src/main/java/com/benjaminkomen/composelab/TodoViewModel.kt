package com.benjaminkomen.composelab

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class TodoViewModel : ViewModel() {
    val todos = mutableStateListOf<TodoItem>()

    fun addTodo(text: String) {
        todos.add(TodoItem(text = text))
    }

    fun removeTodo(item: TodoItem) {
        todos.remove(item)
    }

    fun toggleImportant(item: TodoItem) {
        val index = todos.indexOf(item)
        if (index != -1) todos[index] = item.copy(isImportant = !item.isImportant)
    }
}
