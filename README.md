# JetpackCompose-SwipeToDelete

A minimal Android project that tests swipe-to-delete in Jetpack Compose and provides a working solution for the broken `positionalThreshold` in Material 3's `SwipeToDismissBox`.

https://github.com/user-attachments/assets/19f5b1f0-e8e8-4981-88b1-d7be4811cdec

## The problem

Material 3's `SwipeToDismissBox` has a [known bug](https://issuetracker.google.com/issues/471021165) (status: "Not started") in versions >= 1.4.0:

- **`positionalThreshold` is ignored** — the parameter exists but is never forwarded to the underlying `AnchoredDraggableState`'s fling behavior
- **`confirmValueChange` may not be called** — the state can transition during the drag itself, bypassing the callback entirely
- **`progress` only updates after 50%** — making it unusable for driving animations from the start of a swipe

This means you cannot reliably control how far users need to swipe before an item is dismissed. Items get deleted after swiping just ~25-50% of the way.

## The solution

This project bypasses `SwipeToDismissBox` entirely and implements swipe-to-delete from scratch using lower-level Compose APIs:

- **`detectHorizontalDragGestures`** — handles the swipe gesture with full control over thresholds
- **`Animatable`** — tracks the drag offset and provides spring bounce-back animation
- **`AnimatedVisibility`** with `shrinkVertically` + `fadeOut` — smooth removal animation when an item is deleted
- **Configurable positional threshold** — set to 80% by default, trivially changeable to any value
- **Growing red pill indicator** — a `RoundedCornerShape(50)` delete indicator that grows 1:1 with the swipe distance
- **Visual threshold markers** — `Canvas`-drawn lines at every 10% interval with the active threshold highlighted in orange

The threshold check happens at finger-up time in `onDragEnd`, giving precise control:

```kotlin
onDragEnd = {
    if (containerWidth > 0 && abs(offsetX.value) >= containerWidth * 0.8f) {
        // Past threshold — animate off-screen, then remove
    } else {
        // Not far enough — spring back
    }
}
```

## Prior art

Real-world apps that work around the same bug:
- [EhViewer](https://github.com/FooIbar/EhViewer) — forks `SwipeToDismissBox` using foundation-level `AnchoredDraggableState` with explicit threshold passing via `AnchoredDraggableDefaults.flingBehavior()`
- [Firefox Android](https://github.com/mozilla-firefox/firefox) — full custom implementation using raw `pointerInput` gesture APIs

## Running

Open in Android Studio and run on a physical device or emulator (min SDK 26).
