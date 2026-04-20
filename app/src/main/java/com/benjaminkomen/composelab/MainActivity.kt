package com.benjaminkomen.composelab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.benjaminkomen.composelab.ui.theme.ComposeLabTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

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

/**
 * Custom swipe-to-delete using detectHorizontalDragGestures + Animatable.
 *
 * We bypass SwipeToDismissBox entirely because positionalThreshold is broken
 * in Material 3 >= 1.4.0 (https://issuetracker.google.com/issues/471021165).
 * This approach gives us full control over the dismiss threshold.
 */
@Composable
fun TodoItemRow(
    item: TodoItem,
    onRemove: (TodoItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isRemoved by remember { mutableStateOf(false) }
    var containerWidth by remember { mutableIntStateOf(0) }
    val animationDuration = 500
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }

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
        Box(
            modifier = modifier
                .onSizeChanged { containerWidth = it.width }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (containerWidth > 0 && abs(offsetX.value) >= containerWidth * 0.8f) {
                                // Past 80% threshold — animate off-screen, then remove
                                scope.launch {
                                    offsetX.animateTo(
                                        -containerWidth.toFloat(),
                                        animationSpec = tween(200)
                                    )
                                    isRemoved = true
                                }
                            } else {
                                // Not far enough — spring back
                                scope.launch {
                                    offsetX.animateTo(
                                        0f,
                                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                    )
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch { offsetX.animateTo(0f, spring()) }
                        },
                    ) { change, dragAmount ->
                        change.consume()
                        // Only allow dragging left (negative offset)
                        val newValue = (offsetX.value + dragAmount).coerceAtMost(0f)
                        scope.launch { offsetX.snapTo(newValue) }
                    }
                }
        ) {
            // Foreground: the actual list item, sliding with the drag.
            // Listed first so it determines the Box height.
            ListItem(
                headlineContent = { Text(item.text) },
                modifier = Modifier
                    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                    .background(Color(0xFFF5F5F5)),
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

            // Threshold markers at every 10% — visible behind the sliding item
            ThresholdMarkers(
                containerWidth = containerWidth,
                modifier = Modifier.matchParentSize()
            )

            // Background: delete pill revealed as item slides left.
            // Uses matchParentSize so it doesn't affect the Box's own size.
            DeleteBackground(
                offsetPx = offsetX.value,
                modifier = Modifier.matchParentSize()
            )
        }
    }
}

@Composable
fun ThresholdMarkers(containerWidth: Int, modifier: Modifier = Modifier) {
    val textMeasurer = rememberTextMeasurer()
    val markerColor = Color.LightGray
    val highlightColor = Color(0xFFFF9800) // orange for 80%

    Canvas(modifier = modifier) {
        if (containerWidth <= 0) return@Canvas

        for (pct in 1..9) {
            val x = containerWidth * (1f - pct / 10f) // markers from right edge
            val isThreshold = pct == 8 // 80% marker
            val color = if (isThreshold) highlightColor else markerColor
            val strokeWidth = if (isThreshold) 3f else 1.5f
            val lineHeight = if (isThreshold) size.height else size.height * 0.4f
            val yStart = (size.height - lineHeight) / 2f

            drawLine(
                color = color,
                start = Offset(x, yStart),
                end = Offset(x, yStart + lineHeight),
                strokeWidth = strokeWidth,
            )

            // Label at 80%
            if (isThreshold) {
                val label = textMeasurer.measure(
                    "80%",
                    style = TextStyle(fontSize = 9.sp, color = highlightColor)
                )
                drawText(label, topLeft = Offset(x - label.size.width / 2f, 2f))
            }
        }
    }
}

@Composable
fun DeleteBackground(offsetPx: Float, modifier: Modifier = Modifier) {
    // offsetPx is negative when swiping left
    val absOffsetDp: Dp = with(LocalDensity.current) { abs(offsetPx).toDp() }
    // Subtract horizontal padding so the pill fits within the revealed area
    val pillWidth: Dp = (absOffsetDp - 24.dp).coerceAtLeast(0.dp)

    if (absOffsetDp > 4.dp) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .height(40.dp)
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
