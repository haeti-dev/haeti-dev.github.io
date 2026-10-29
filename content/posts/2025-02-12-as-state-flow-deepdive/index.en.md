---
title: Do you really need asStateFlow()?
summary: A backing property already hides your MutableStateFlow, so I traced what asStateFlow() adds on top, from downcast protection to flow fusion.
source: bfcd59eabf9f
---

### The backing property pattern

If you write Kotlin, you've seen the **backing property** pattern more times than you can count:

```kotlin
private val _counter = MutableStateFlow(0) 
val counter : StateFlow<Int> = _counter 
```

`_counter` is a `MutableStateFlow` that only the class itself can modify, and `counter` exposes it to everyone else as a read-only `StateFlow`. Android code uses the observer pattern constantly, usually through data streams like LiveData, Rx, or Flow, so the UI or some piece of logic updates automatically whenever something changes.

The reason for the backing property is to stop outside code from changing the value. A lot of developers think of that as immutability, but look one step closer and it isn't. What you actually get is a **read-only** view. The public interface doesn't let anyone modify it, but the value behind it can keep changing from the inside. The "Limit mutability" section of my earlier post, [Applying Effective Kotlin to Compose](post/effective-kotlin), goes into this.

(Side note: I didn't use `get()` in the example because data streams like these almost never get reassigned. Still, a `get()` getter instead of an assignment (`=`) saves you one final property, and if you put a backing property in front of a primitive or a binding, you need `get()` so changes show up in real time.)

So why expose a read-only type at all? Two reasons come to mind:

- **Encapsulation.** Internal logic can update the value freely, while outside code can't modify it by mistake.
- **Stating intent.** The code says plainly that nobody outside should change this.

#### Isn't asStateFlow() redundant?

If the backing property already gives you a read-only view, why bother with `asStateFlow()`? And if you should use it, when and why?

```kotlin
private val _counter = MutableStateFlow(0) 
val counter = _counter.asStateFlow()
```

Both just use a `MutableStateFlow` as if it were a `StateFlow`, so functionally they look about the same. Plenty of developers skip the `asStateFlow()` extension and never run into trouble.

But a teammate, or any other developer, can downcast it by accident (or on purpose):

```kotlin
val counterAttack = (MViewModel.counter as? MutableStateFlow<Int>)
counterAttack?.value = -1
```

Sure, nobody writes code like this casually. The problem is that the door is always open.

That's why `asStateFlow()` exists: to make the read-only part explicit.

The other big reason is that `asStateFlow()` has **optimizations built in**, so it does more than just change the type.

Before getting into the internals, here's an analogy for anyone who doesn't have a feel yet for what `asStateFlow()` is for.

I was a part lead at SOPT (a Korean collegiate IT startup club), and I ran my seminars in Notion. The seminar pages lived in a workspace everyone had been invited to, so even with Notion's page lock on, **anyone could unlock the page and edit the material**. And someone did: a slip during one seminar damaged the content (I restored it right away). It's a bit of a stretch, but that's a lot like changing a `StateFlow`'s value through a downcast. I locked the page to make it read-only, but the setup still allowed edits, so it was never truly read-only.

Now say I'd written the material in my personal Notion and shared it with **Notion's publish feature** instead. Attendees could read it but not edit it, which makes it properly read-only. (I can still edit it as the author.) Set up that way, nothing gets damaged mid-seminar.

I think the same idea is pretty useful in Android code. Let's see how `asStateFlow()` actually works by reading its source.

## Taking asStateFlow() apart

### asStateFlow()

![](images/01.webp){width=100 align=center}

The `asStateFlow()` extension turns a `MutableStateFlow<T>` instance into the read-only `StateFlow<T>` interface.

Under the hood, it creates and returns a wrapper class called `ReadonlyStateFlow`. Let's look inside that next.

### ReadonlyStateFlow

![](images/02.webp){width=100 align=center}

When you wrap a `MutableStateFlow` with `asStateFlow()`, `ReadonlyStateFlow` is the class you actually get back. As the name says, it's a wrapper that makes mutable state on the inside look read-only from the outside.

#### Delegation

With `StateFlow<T> by flow`, `ReadonlyStateFlow` **delegates every `StateFlow` capability to the wrapped `flow`**. That's why you can use a `ReadonlyStateFlow` exactly like a `StateFlow`.

#### The Job parameter

The constructor can take a `Job`. It's `null` in this code path, but if the flow were managed by some coroutine context, the class would hold that job with a strong reference so it could handle cancellation and lifetime.

#### CancellableFlow

Implementing this interface lets the flow respond when the coroutine collecting it might get cancelled. I'll come back to it in the next section.

#### FusibleFlow

This one may be less familiar. It's an interface that lets the flow fuse with downstream operators such as `flowOn` and `buffer`. When you attach operators like these (`flowOn` to run the flow in a different thread context, `buffer` to change the buffer size along the way), fusion lets the library **drop or optimize away intermediate steps it doesn't need**. I'll walk through that implementation later too.

#### fuse()

`fuse` is a `FusibleFlow<T>` method that defines how this flow should optimize itself if `flowOn` or `buffer` shows up downstream. Here it hands the real work to `fuseStateFlow()`, which we'll also get to later.

So this class **delegates to the inner flow to provide the read-only interface**, and by implementing `CancellableFlow` and `FusibleFlow`, it **supports coroutine cancellation and fusion with downstream `buffer`/`flowOn` operators**, which is where the optimization comes from.

### CancellableFlow, CancellableFlowImpl

![](images/03.webp){width=100 align=center}

`CancellableFlow` is an **internal marker interface** for, literally, a flow that can be cancelled. How cancellation actually works (stopping `collect` when the coroutine is cancelled) lives in `CancellableFlowImpl`.

Its `collect` method calls `currentCoroutineContext().ensureActive()` to check that the current coroutine is still alive (not cancelled), and throws a `CancellationException` if it isn't.

So when `ReadonlyStateFlow` implements `CancellableFlow`, read it as: if a cancellation comes in while something downstream is collecting, the flow stops cleanly.

### FusibleFlow

![](images/04.webp){width=100 align=center}

`FusibleFlow` is the interface that decides **how this flow fuses with a downstream `buffer` or `flowOn` operator**.

- **`buffer`**: adjusts the flow's buffer size, e.g. `.buffer(capacity = 64)`
- **`flowOn`**: runs the flow on a different coroutine dispatcher, e.g. `flowOn(Dispatchers.IO)`

With a plain flow, every `buffer` or `flowOn` you add can create another intermediate operator. The Flow library leaves room to optimize this internally (fusion) so it doesn't create operators it doesn't need. For example, adding `conflate` to a flow that's already conflated (keeping only the latest value) does nothing, so the library can drop it.

`ReadonlyStateFlow` implements this so it can effectively say, "I'm already a conflated `StateFlow`, so if a similar setting comes in from downstream, there's no need to handle it twice."

### fuseStateFlow(), fuseSharedFlow()

![](images/05.webp){width=100 align=center}

This is the function we saw inside `ReadonlyStateFlow` earlier. It decides what happens when `buffer` or `flowOn` gets attached to a `StateFlow`.

The logic goes like this. A `StateFlow` by nature always **keeps only the latest value (it's conflated)**. So if `capacity` is `0~1` and `onBufferOverflow` is `DROP_OLDEST`, the request matches what it already does. There's no need for a new intermediate operator, so it **just returns `this`**.

As the comment in the source points out, conflating a `StateFlow` that's already conflated would be redundant, so an `assertion` rules out `Channel.CONFLATED`. That check usually only runs in debug mode.

If `capacity` isn't in the `0~1` range and `onBufferOverflow` isn't `DROP_OLDEST` either, it falls through to `fuseSharedFlow()`.

![](images/06.webp){width=100 align=center}

`fuseSharedFlow()` handles `SharedFlow<T>`: under some conditions it returns the flow as is, and otherwise it wraps it in `ChannelFlowOperatorImpl` so buffer policies and the like get applied properly.

Its comment notes that **context doesn't mean much for a `SharedFlow`**, and that adding a rendezvous channel to a flow that's already shareable buys you very little.

### ChannelFlowOperatorImpl

![](images/07.webp){width=100 align=center}

This is where the library finally builds a **channel-based flow** that can apply the buffer (`capacity`), the context (`context`), and the overflow policy (`onBufferOverflow`).

`ChannelFlow` is an abstraction layer that Kotlin coroutines use internally to optimize operators like `buffer` and `flowOn`. It receives data from a single channel and takes care of whatever buffering or context switching is needed.

`flowCollect` ultimately collects the original flow and performs (or tacks on) whatever operations are needed in between.

In short, a class like `ChannelFlowOperatorImpl` does the channel work at the very end, so things behave correctly when there's a very slow subscriber or the buffer size needs adjusting.

## Wrapping up

We started with read-only properties and ended up deep in optimization internals, so here's the whole picture in one place:

1. `ReadonlyStateFlow` wraps the `MutableStateFlow` so that from the outside it looks like a genuinely **read-only `StateFlow`**.
2. It implements `CancellableFlow`, so **collection stops cleanly if a cancellation happens mid-`collect`**.
3. Through `FusibleFlow`, it **fuses with downstream operators like `flowOn` and `buffer`**.
4. `fuseStateFlow`, `fuseSharedFlow`, and `ChannelFlowOperatorImpl` implement rules like "conflating something that's already conflated is pointless" and "add extra buffer or context settings when they're needed."

Put together, `asStateFlow()` does more than change the type. It's built to guarantee **optimization and safety** for coroutine flows, and that's the key point.

Even shorter:

`asStateFlow()` → `ReadonlyStateFlow` → (`CancellableFlow` + `FusibleFlow`) → **internal optimization and guaranteed cancellation**

That's the chain to keep in mind.

You won't think about any of this in day-to-day Android work. But the next time you wonder why anyone bothered writing an `asStateFlow()` extension, remember that **all this optimization and safety is baked into it**.
