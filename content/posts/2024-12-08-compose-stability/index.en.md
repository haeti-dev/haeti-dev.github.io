---
title: Compose stability and a look inside kotlinx.collections.immutable
summary: How stability lets Compose skip recomposition, three ways to get it right, and how persistent collections avoid copying on every change.
source: a438efa8dab4
---

Jetpack Compose is a declarative UI framework: you describe **what** to draw, not **how** to draw it. That "what" is usually **state**. When state changes, Compose re-runs the UI that depends on it. That's **recomposition**.

Because recomposition happens automatically, you rarely have to write UI update logic yourself. But if every state change redrew the whole screen, Compose would start over from composition, the very first of the [Compose phases](https://developer.android.com/develop/ui/compose/phases?hl=ko), every time. That means slower UI and wasted work. Compose avoids this with a concept called **stability**, which lets it skip recompositions it doesn't need.

In this post I'll go through the ways to keep your code stable, and then dig into the source of a library that provides immutable collections.

## 1️⃣ What is stability in Compose?

Stability answers one question: **can Compose trust that this object hasn't changed?** A `@Composable` function that takes stable objects can skip recomposition when it gets the same arguments again, which is good for performance. If an object is unstable, Compose has to assume it might have changed internally, so it can't skip, and you lose that benefit.

Put another way, stability is what lets Compose redraw the UI efficiently instead of burning CPU on work it doesn't need. The more of your data is stable, the more Compose's smart recomposition can do, and that shows up in the app's overall performance.

#### Stable vs. unstable

Compose sorts types into two broad groups.

1. **Stable objects**
   - Immutable objects made only of properties that never change, or data structures Compose can be sure are immutable.
   - Examples: a `data class` whose fields are all immutable types, a class explicitly marked with `@Stable`, or types whose stability Compose already knows (primitives, immutable lists, and so on).
   - When you pass a stable object to a composable and later pass the same instance again, that composable can skip recomposition. Compose decides that the value hasn't changed, so there's nothing to redraw.
2. **Unstable objects**
   - Objects with properties that can change at any time, or objects Compose can't prove are immutable.
   - Typical examples are mutable properties declared with `var`, objects holding references that aren't thread-safe, and complex types from external libraries that Compose knows nothing about.
   - Even if the reference hasn't changed, Compose can't rule out that the internal state has. So even when it gets the same instance again, it plays it safe and redraws, which makes it hard to keep recompositions down.

#### Smart recomposition

With **smart recomposition**, Compose redraws only the parts affected by a state change. To get there, it asks two questions:

- Is the object passed to this composable the same one as last time?
- I got the same reference again; can I be sure its internal state hasn't changed?

If the object is stable, Compose concludes that it was promised not to change and doesn't need redrawing. Stability lets Compose cut unnecessary render passes, and that's where the performance win comes from. If the object is unstable, Compose redraws even when the reference is the same, because it can't rule out internal changes.

#### Skippable: when Compose can skip recomposition

When a `@Composable` function gets the same parameters as before, Compose tries to reuse the previous result. A function that allows this is called "skippable". To be skippable, the function has to be pure and its parameters have to be stable, so Compose can be sure the values inside haven't moved. Stable parameters are the key condition here.

- **With stable objects**: same object passed -\> no internal change, guaranteed -\> recomposition is skippable
- **With unstable objects**: same object passed, but internal changes can't be detected -\> recomposition required

#### How do you check whether your code is stable?

While developing, it's worth checking which composables and objects are stable and fixing the ones that aren't. This is usually done with the Compose compiler, and as far as I know there are several ways to do it. I used Compose Compiler Metrics to check runtime stability.

![](images/01.webp){width=50 align=center}

You can also use Compose Compiler Reports to see the results of stability inference:

[https://developer.android.com/develop/ui/compose/performance/stability/diagnose?hl=ko](https://developer.android.com/develop/ui/compose/performance/stability/diagnose?hl=ko)

## 2️⃣ How do you keep things stable?

Section 1 showed why stability matters: it cuts slowdowns and wasted work, and it's what smart recomposition relies on. Now for how to actually get it.

### 1. Use the annotations

Compose provides two annotations to help with stability inference: `@Stable` and `@Immutable`. Both explicitly tell Compose that a type or return value is stable. The difference between them is subtle, so you need to pick the right one for the situation.

#### `@Immutable`

- **Meaning**: tells Compose that the class or interface is immutable.
- **Conditions for immutability**:
  - Every field must be an immutable type.
  - The structure must not allow any further state changes.
  - In other words, once the object is created, its fields never change.
- **Effect**: since nothing inside an `@Immutable` object will ever change, Compose treats it as fully immutable and can skip recomposition when the same reference comes back.

![](images/02.webp){width=60 align=center}

Every field of `User` in this example is an immutable type (`Int`, `String`), and its state can't change, so it's marked `@Immutable` to declare that it's fully immutable.

`@Immutable` can improve performance by skipping recompositions you don't need. Use it on the wrong type, though, and Compose will skip recompositions you did need, and the screen won't update.

#### `@Stable`

- **Meaning**: `@Stable` says that an instance of this type is **stable as long as it keeps the same reference**. Its conditions are looser than `@Immutable`'s, and the fields don't have to be fully immutable. It still can't be so unstable that Compose has no way to reason about changes to the type.
- **When to use it**:
  - The object isn't fully immutable, but you can guarantee it won't change under certain conditions.
  - Or the object can change internally, but Compose can't easily tell from change tracking whether its state changed.
- **Effect**: Compose assumes an object marked `@Stable` won't change easily. **If it gets the same object again, it assumes nothing changed inside and can skip recomposition.** But because `@Stable` is less strict than `@Immutable`, keeping the object stable is your responsibility.

For example, in Compose's own code, `Dp`, `Modifier`, and others are declared `@Stable`. That signals they're very unlikely to change, and that if they do, Compose can easily tell.

![](images/03.webp){width=100 align=center}

In this snippet of Compose's internal code (it may differ slightly from the actual implementation), `Dp` uses `@Stable` rather than `@Immutable`. `Dp` can effectively be treated as immutable, but Compose manages it as `@Stable`.

#### `@Immutable` vs. `@Stable`

When I first saw these two annotations, I couldn't really feel the difference. Once I understood the key point, telling them apart got easy. The key is **mutability**: whether the object might change at all.

Here's how I'd sum up each one and when to reach for it:

- `@Immutable`
  - A fully immutable object
  - Use it when the object never changes after it's created
  - Compose trusts 100% that it won't change
  - The surest way to improve stability
- `@Stable`
  - Not fully immutable, but safe to treat as stable
  - Works even when the type isn't built only from immutable types
  - Use it when internal state rarely changes, or when you can guarantee stability yourself
  - A weaker guarantee than `@Immutable`, but it still helps keep recompositions down

So: for data models that never change at all (`User`, `Color`, `Point`, and so on), use `@Immutable`. When you want some flexibility but still want to signal stability, use `@Stable`. Default to `@Immutable` where you can, and fall back to `@Stable` when the situation calls for it.

### 2. Keep your lambdas stable

Lambdas are everywhere in Compose, for handling UI events and passing down business logic. The thing to watch is **what the lambda captures**. That changes how Compose optimizes the lambda and how it judges its stability. Get it wrong and you end up with extra recompositions and slower UI.

#### How the Compose compiler treats lambdas

In regular Kotlin, a lambda that **captures an outer variable forms a closure**. It reads that variable every time it runs, so its result can change depending on the situation. The Compose compiler handles these lambdas specially during IR (intermediate representation) lowering. It picks an optimization strategy based on **whether the lambda captures outer values**, and on whether those captured values are stable and immutable.

- **Lambdas that capture nothing**

  A lambda that doesn't reference any outer variable is treated as a singleton, so **no new instance is created on recomposition**. That means the lambda stays **stable**.

```kotlin
// A simple lambda that references no outer variables at all
@Composable
fun StaticContent(showMessage: () -> Unit) {
    showMessage() // Always does the same thing
}

// This lambda captures no variables
StaticContent {
    println("This lambda does not capture any external values.")
}
```

The lambda passed to `StaticContent` doesn't depend on any outer state, so it can be reused safely.

- **Lambdas that capture values**

When a lambda references an outer variable, its result can change as that variable changes. The Compose compiler gives these lambdas special treatment during IR lowering.

Consider a lambda that references an outer variable:

```kotlin
val factor = 2
val numbers = listOf(1, 2, 3)

// This lambda captures the outer variable factor
val multipliedValues = numbers.map {
    factor * it
}
```

Here `it` is each list element and `factor` is the outer variable. If `factor` could take a different value across recompositions, Compose decides that this lambda could produce a different result on each recomposition. So even if the lambda itself looks stable, the state of what it captured can cause recompositions you don't need.

#### What stable and unstable captures do

Compose sees a lambda as a function type and tries to classify the function itself as fairly stable. But **if the captured value is unstable, it gets hard to skip recomposition at runtime**. Even if the lambda is marked stable, **it can't skip recomposition when it references an unstable value inside**.

Say you have a function that returns an unstable value:

```kotlin
// An unstable function that returns a different value on every call
fun unstableValueProvider(): Any? {
    return System.currentTimeMillis()
}
```

If a lambda captures this function, it can produce a different result every time even if the lambda is stable, so Compose has a hard time avoiding recomposition.

```kotlin
@Composable
fun DisplayData(getData: () -> Any?) {
    Text(text = (getData() ?: "No Data").toString())
}

// A lambda that captures an unstable value
DisplayData {
    // Returns a different value every time, so recomposition is needed
    unstableValueProvider()
}
```

The `getData` lambda might be marked stable because it's a function type, but its value actually changes on every call, so Compose can't skip recomposition.

#### The fix: `remember` and keys

The main tool here is stabilizing the lambda with `remember`. With `remember`, you reuse the same lambda instance as long as certain inputs don't change.

```kotlin
@Composable
fun UseStableLambda(name: String, fetchData: (String) -> String) {
    val stableFetcher = remember(name) { { fetchData(name) } }
    // stableFetcher creates a new lambda only when name changes
    // Otherwise the same lambda instance is reused
    Text(stableFetcher())
}

// The outer function can be stable
fun stableDataFetcher(input: String): String = "Data for $input"

// Example of capturing a stable value
UseStableLambda(name = "User") { input ->
    stableDataFetcher(input) // stableDataFetcher always produces a predictable result
}
```

Here `remember(name)` keeps the `stableFetcher` lambda stable. A new lambda is created only when `name` changes; otherwise the previous one is reused. That lets Compose conclude this lambda won't trigger unnecessary recompositions.

But what if you're still capturing an unstable function?

```kotlin
@Composable
fun UseUnstableLambda(id: Int, fetchData: (Int) -> Any?) {
    val dynamicFetcher = remember(id) { { fetchData(id) } }
    Text(dynamicFetcher()?.toString() ?: "No Data")
}

// Returns an unstable value (different every time)
fun unstableDataFetcher(input: Int): Any? = System.nanoTime()

UseUnstableLambda(id = 42) { input ->
    unstableDataFetcher(input)
}
```

Even with `remember(id)`, if `unstableDataFetcher` returns a different value every time, recomposition can't be avoided. `remember` keeps the lambda instance stable, but if the lambda's result keeps changing, Compose still has to recompose to reflect the new UI state.

#### In short

- **Lambdas that capture nothing** are stable without any extra work. The compiler treats them as singletons, which keeps recompositions to a minimum.
- **Lambdas that capture stable values** recompose much less if you reuse the instance with `remember` or similar.
- **Lambdas that capture unstable values** can't fully avoid unnecessary recompositions if the captured value itself changes, even wrapped in `remember`. Consider switching to immutable data structures or stable data sources, or using `@Immutable`.

### 3. Use immutable collections

Another important way to improve stability is to use immutable collections. Kotlin's standard collections are read-only, but they don't guarantee full immutability. An immutable collection's internal state never changes once it's defined, so when the collection is reused, Compose can be sure nothing changed inside and keep recompositions to a minimum. Returning a new collection every time to preserve immutability, a basic idea of functional programming, matters for Compose performance too.

I'll cover this in more detail in the next section. For now, just keep in mind that immutable collections improve stability.

## 3️⃣ A closer look at kotlinx.collections.immutable

Earlier I mentioned immutable collections as one way to get stability in Compose. In this section I'll dig into what data structures the `kotlinx.collections.immutable` library provides and how they work.

#### 1. Why are Kotlin collections unstable?

![](images/04.webp){width=100 align=center}

[https://developer.android.com/develop/ui/compose/performance/stability/fix?hl=ko#immutable-collections](https://developer.android.com/develop/ui/compose/performance/stability/fix?hl=ko#immutable-collections)

This is from the official Android docs. As it says, Kotlin collections like `List`, `Map`, and `Set` expose `read-only` interfaces. But you don't know how they're implemented, and the implementation can be mutable, so immutability and persistence aren't guaranteed.

The answer is to use collections that can't be modified. Let's look at Kotlinx Immutable Collections, which the docs recommend.

#### 2. Two kinds of collections in kotlinx.collections.immutable

[https://github.com/Kotlin/kotlinx.collections.immutable](https://github.com/Kotlin/kotlinx.collections.immutable)

The `kotlinx.collections.immutable` library mentioned in the docs provides two broad types of collections. Let's start with what each one means:

- **Immutable**: once created, the object's internal state never changes. For example, `listOf(1, 2, 3)` returns a read-only list that can't be modified from the outside. But with standard Kotlin collections, that isn't really true immutability. When you need a whole new list from one of these, it can take a copy or other extra work internally, and there's no guarantee of structural immutability where the new version shares the original.
- **Persistent**: this is about persistence. A persistent collection is an immutable collection with "versions". Any modifying operation (`add`, `remove`) leaves the existing collection untouched and returns a new, modified one, while the previous version stays valid and usable. To keep this efficient, it uses **structural sharing**: instead of copying all the existing data, it creates new nodes only for the parts that changed and links them in, which keeps memory use and computation down.

So a persistent collection is an immutable collection that can manage its history of changes efficiently, while an immutable one is an object whose state can't change once set. A persistent collection stays immutable internally and still produces new immutable instances efficiently when you modify it.

![](images/05.webp){width=100 align=center}

As you can see, `ImmutableCollection` extends Kotlin's `Collection`, and `PersistentCollection` extends `ImmutableCollection`.

#### 3. Inside the immutable collections

I once wrote my own immutable linked list to learn about immutability in functional programming. It was easier than I expected, so I wondered whether I could build the immutable and persistent collections from this post myself too, and went digging through the internals.

As I mentioned, the heart of this library is **structural sharing**: it always keeps the previous versions of a collection around while producing new versions for each change efficiently.

That's not simple to implement. Unlike Kotlin's standard collections, persistent data structures use complex **trie-based** structures or particular array-chunk layouts internally, so they **avoid a full copy and create a new version of only the parts that changed**.

[https://github.com/Kotlin/kotlinx.collections.immutable/blob/master/core/commonMain/src/implementations/immutableList/PersistentVector.kt](https://github.com/Kotlin/kotlinx.collections.immutable/blob/master/core/commonMain/src/implementations/immutableList/PersistentVector.kt)

You can see the trie structure in the library's `PersistentVector.kt`.

![](images/06.webp){width=100 align=center}

The KDoc comment in that file mentions structural terms like trie, leaf buffer, and tail. Here are the two core ideas behind it.

**1. Indexing split across a multi-level array structure**

Instead of a single flat array, a trie-based structure stores elements in arrays spread across several levels. The `root` field points to the top-level array, and each of its elements points to another array, forming a tree. Each node (array) holds at most `MAX_BUFFER_SIZE` elements or child-array references.

**2. Bit-mask indexing with `LOG_MAX_BUFFER_SIZE`**

The `rootShift` variable encodes the height as `rootShift = (height - 1) * LOG_MAX_BUFFER_SIZE`. Calling `indexSegment(index, shift)` splits the index into bit ranges to work out which level and node of the trie it belongs to, which finds the element at a given index in **O(log n)** time.

- A larger `shift` means a higher level.
- `indexSegment(index, shift)` extracts a particular bit range from `index` to decide which array element to follow at that level.

With this approach, looking up a single element stays close to **O(1)**, like plain array indexing, while appending at the end or inserting in the middle stays efficient.

#### Structural sharing and the trie

To be persistent, `PersistentVector` shares existing nodes on modification instead of copying everything. Here's what happens when you add an element:

- The tail goes first. If the tail has room, the new element goes straight into the tail array.
- When the tail is full, it's treated as a full leaf node and inserted into the `root` trie through `pushTail`.
- `pushTail` reuses most of the existing trie and allocates only the few nodes it needs. The old nodes are left untouched, so the previous version of the collection stays intact.

Every call returns a new `PersistentVector` instance, but most of the nodes are reused internally, which saves a lot of memory and work.

To sum up, `PersistentVector`:

- uses a trie-based, hierarchical array structure,
- breaks an index down by level with `rootShift` and `indexSegment` to reach an element,
- and creates new versions by pushing the tail up into the trie with functions like `pushTail`.

As a result, it:

- handles large lists efficiently,
- creates new versions on modification without a full copy,
- and keeps previous versions around while staying fast, which is what makes it persistent.

Trie-based structures like this are a key way to handle large lists efficiently while staying fully immutable, and that's exactly why `kotlinx.collections.immutable` goes with a complex trie-based implementation.

#### Covariance and generics

![](images/07.webp){width=100 align=center}

In the library code you'll see the `out` keyword and the `@UnsafeVariance` annotation. Here's what they mean and what they do.

- **Covariance that fits immutability**

  `out` means the list can produce values of type `E`, but is restricted in what it can accept.

  Since an immutable data structure's internal state can't be changed from outside, it's safe to upcast it to a supertype. For example, you can't replace the elements of a `PersistentList<Cat>`, so treating it as a `PersistentList<Animal>` is fine. That only works because the collection is immutable.
- **The complexity inside**:

  But a persistent collection returns a new collection every time you add an element. Since the collection is immutable, covariance should apply without any trouble, yet once an operation that adds elements comes in, keeping type safety isn't easy.

  To handle this, the library's internals use `@UnsafeVariance` and similar annotations to deal with adding elements to a type parameter declared `out`. That lets the library authors keep as much generic type safety as possible while still supporting the operations a persistent collection needs.

### Wrapping up

I started from what stability means in Jetpack Compose, went through how to get it (the stability annotations, lambda stability, and immutable data structures), and ended with the structure of `kotlinx.collections.immutable`, one of the libraries that makes immutability practical.

Getting stability right in Compose goes beyond UI performance; it also makes the whole app easier to maintain and extend. Fewer unnecessary recompositions means better performance, and predictable state management keeps UI logic simple. To get there, I'm convinced it pays to really understand immutable and persistent collections and use them where they fit.
