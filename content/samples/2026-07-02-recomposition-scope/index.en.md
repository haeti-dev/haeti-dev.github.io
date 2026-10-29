---
title: Where recomposition stops, and where it starts again
summary: Change one piece of state and some of your composables run again. Following the recomposition scopes tells you exactly which ones.
source: 135b5c70b9af
---

## Where you read state decides the scope

What recomposes comes down to **where you read** the state. Read the same state inside a lambda, and only that lambda runs again.

Take this counter:

```kotlin
@Composable
fun Counter(count: State<Int>) {
    Column {
        Header()
        Text("${count.value}")
    }
}
```

## When skipping kicks in

If every parameter is stable and equal to its previous value, Compose skips the function body entirely. One unstable type in the mix is enough to break that.

> [!tip] Checking stability
> Turn on the Compose compiler reports and you'll see, function by function, which ones are skippable.

## The takeaway

The further down you read state, the less gets redrawn. That one habit takes care of half your performance problems.
