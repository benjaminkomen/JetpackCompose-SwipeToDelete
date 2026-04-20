# ComposeLab

A playground for learning Jetpack Compose and Material 3 on Android by building a TODO app.

## What this demonstrates

### Compose fundamentals
- `@Composable` functions, `remember`, `mutableStateOf`, `Modifier` chains
- Material 3 theming with `MaterialTheme`, `Scaffold`, `ListItem`
- `LazyColumn` with `itemsIndexed` and `key` for efficient list rendering
- `ViewModel` with `mutableStateListOf` for observable state management
- `OutlinedTextField` with trailing icon for text input
- Keyboard dismissal via `detectTapGestures` + `LocalFocusManager`

### Custom swipe-to-delete gesture
Material 3's `SwipeToDismissBox` has a [known bug](https://issuetracker.google.com/issues/471021165) in versions >= 1.4.0 where `positionalThreshold` is ignored and `confirmValueChange` may not be called reliably. This project works around the bug by implementing swipe-to-delete from scratch using:

- `detectHorizontalDragGestures` for the swipe gesture (full control over thresholds)
- `Animatable` for smooth offset tracking and spring bounce-back
- `AnimatedVisibility` with `shrinkVertically` + `fadeOut` for removal animation
- Custom 80% positional threshold — items only delete when dragged past 80% of width
- Growing red pill delete indicator using `RoundedCornerShape(50)`

Real-world apps like [EhViewer](https://github.com/FooIbar/EhViewer) and [Firefox Android](https://github.com/mozilla-firefox/firefox) use similar workarounds.

## Running

Open in Android Studio and run on a physical device or emulator (min SDK 26).
