package com.benjaminkomen.composelab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.benjaminkomen.composelab.ui.theme.ComposeLabTheme
import kotlinx.coroutines.delay
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    private val viewModel: TodoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeLabTheme {
                TodoScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun TodoScreen(viewModel: TodoViewModel) {
    val focusManager = LocalFocusManager.current

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                },
        ) {
            AddItemRow(onAddItem = { viewModel.addTodo(it) })

            Spacer(modifier = Modifier.height(16.dp))

            TodoList(viewModel = viewModel)
        }
    }
}

@Composable
fun AddItemRow(onAddItem: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    Row(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("Enter todo text") },
            modifier = Modifier.weight(1f),
            singleLine = true,
            trailingIcon = {
                AddItemIconButton(onClick = {
                    if (text.isNotBlank()) {
                        onAddItem(text.trim())
                        text = ""
                    }
                })
            }
        )
    }
}

@Composable
fun AddItemIconButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(Icons.Default.Add, contentDescription = "Add")
    }
}

@Composable
fun TodoList(viewModel: TodoViewModel) {
    if (viewModel.todos.isEmpty()) {
        Text(
            text = "No todos yet. Add one above!",
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        LazyColumn(
            modifier = Modifier.border(
                1.dp,
                MaterialTheme.colorScheme.onSurfaceVariant,
                shape = MaterialTheme.shapes.small
            )
        ) {
            itemsIndexed(items = viewModel.todos, key = { _, item -> item.id }) { index, item ->
                TodoItemRow(
                    item = item,
                    onToggleImportant = { viewModel.toggleImportant(item) },
                    onRemove = { viewModel.removeTodo(item) }
                )
                if (index < viewModel.todos.size - 1) {
                    // Add Divider between items, except after the last one
                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                }
            }
        }
    }
}

@Composable
fun TodoItemRow(
    item: TodoItem,
    onToggleImportant: (TodoItem) -> Unit,
    onRemove: (TodoItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isRemoved by remember { mutableStateOf(false) }
    val animationDuration = 500

    val swipeToDismissBoxState = rememberSwipeToDismissBoxState(
        positionalThreshold = { totalDistance -> totalDistance * 0.8f },
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                isRemoved = true
                true
            } else {
                false
            }
        }
    )

    LaunchedEffect(isRemoved) {
        if (isRemoved) {
            delay(animationDuration.toLong())
            onRemove(item)
        }
    }

    AnimatedVisibility(
        visible = !isRemoved,
        exit = shrinkVertically(
            animationSpec = tween(durationMillis = animationDuration),
            shrinkTowards = Alignment.Top
        ) + fadeOut()
    ) {
        SwipeToDismissBox(
            state = swipeToDismissBoxState,
            modifier = modifier,
            enableDismissFromStartToEnd = false,
            backgroundContent = {
                DeleteBackground(swipeToDismissBoxState = swipeToDismissBoxState)
            }
        ) {
            ListItem(
                headlineContent = { Text(item.text) },
                modifier = Modifier.background(Color(0xFFF5F5F5)),
                leadingContent = if (item.isImportant) {
                    {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                } else null
            )
        }
    }
}

@Composable
fun DeleteBackground(
    swipeToDismissBoxState: androidx.compose.material3.SwipeToDismissBoxState
) {
    val offset = try {
        swipeToDismissBoxState.requireOffset()
    } catch (_: IllegalStateException) {
        0f
    }
    // offset is negative when swiping left
    val absOffsetDp: Dp = with(LocalDensity.current) { abs(offset).toDp() }
    val pillHeight = 40.dp
    // Grows from 0 → circle (when width = height) → wider pill
    val pillWidth: Dp = (absOffsetDp * 0.5f).coerceIn(0.dp, 120.dp)

    if (absOffsetDp > 4.dp) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .height(pillHeight)
                    .width(pillWidth)
                    .background(
                        color = Color.Red,
                        shape = RoundedCornerShape(50)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (pillWidth > 36.dp) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}
