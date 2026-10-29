---
title: Getting to know Jetpack Navigation 3
summary: Navigation 3 lets you own the back stack as a plain list; here's how keys, NavEntry, and NavDisplay fit together.
source: 16562ce4653a
---

There's a new version of Jetpack Navigation out. I went through the official docs to see what Navigation 3 adds, and compared it with the Navigation 2 way of doing things to figure out why the update matters.

## ⚠️ What was wrong with the old Navigation

The original Jetpack Navigation library (also called Nav2, since it's on major version 2) was first announced in 2018, before AndroidX and Compose. It met its original goals, but Google says it heard from developers that it had a few limitations when used with modern Compose patterns.

One major limitation was that you could only **observe the back stack state indirectly**. That meant there were two sources of truth, which could leave the app's state inconsistent. On top of that, Nav2's NavHost was designed to **show only a single destination (the top of the back stack)**, filling the available space. That made it hard to build adaptive layouts that show several content panes at once, like a list-detail layout on a large screen.

![](images/01.webp){width=100 align=center caption="Source: official docs"}

In short, the old Jetpack Compose Navigation library **didn't let you control the back stack directly**, so you lacked real control, and that limited multi-pane layouts. Navigation 3 was designed to get past this: it brings Compose-friendly APIs, **explicit control over the back stack**, and a structure that supports multi-pane layouts. Google says it drew ideas from libraries like Voyager, so you can think of it as an evolution to meet modern UI needs.

Let's look at what's new in Nav3.

## 🎯 A new back stack paradigm: keys

First, a quick refresher on the back stack, and then what Nav3 adds to it.

Navigation describes how users move through your app. Users usually interact by tapping or clicking UI elements, and the app responds by showing new content. To go back to the previous content, they use the back gesture or tap the back button.

A convenient way to model this is a stack of content. When the user navigates to new content, it's pushed onto the top of the stack. When they go back from it, it's **popped** off the stack and the previous content shows. In navigation terms, this stack holds the content the user can go back to, so it's usually called the **back stack**.

![](images/02.webp){width=100 align=center caption="Source: official docs"}

In Nav3, the back stack doesn't actually contain content. Instead, it contains references to content called **keys**. A key can be any type, but it's usually a simple serializable data class. Using references instead of content has a couple of benefits:

- You navigate simply by pushing a key onto the back stack.
- **As long as your keys are serializable, you can save the back stack to persistent storage, so it survives configuration changes and process death.** That matters because users expect to leave your app, come back later, and pick up the same content where they left off.

The core idea of the Navigation 3 API is that you own the back stack.

- The back stack should be a SnapshotStateList\<T\>, where `T` is the type of your back stack `keys`. You can use `Any`, or provide your own, more strongly typed keys. Whenever you see the terms push or pop, the underlying implementation is adding an item to, or removing one from, the end of the list.
- You observe the back stack and reflect its state in the UI with `NavDisplay`.

![](images/03.webp){width=100 align=center caption="Source: official docs"}

With the concepts covered, let's write an example that creates keys and a back stack, then changes the back stack in response to the user's navigation events.

### 1. Keys, not content

- Navigation 3 **doesn't store the content itself**. It only stores references called **keys**.
- Keys are usually **serializable data classes**, which is what lets you restore state after configuration changes like screen rotation.

![](images/04.webp){width=100 align=center}

### 2. Build a list-based back stack yourself

- In Navigation 3, **you create the list** that makes up the back stack.
- Every time the user navigates, you add a new key to that list.

![](images/05.webp){width=100 align=center}

### 3. Work with the back stack directly

- You navigate by pushing and popping on the back stack list you created yourself.

![](images/06.webp){width=100 align=center}

## 🎯 A new container: NavEntry (formerly NavHost)

A `NavEntry` object maps each screen's key to its content, which lets you switch and render screens dynamically. Everything is declarative.

Navigation 3 models content with `NavEntry`, a class that holds a composable function. Think of it as a destination: a single piece of content the user can navigate forward to and back from.

A `NavEntry` can also carry metadata, which is information about the content. Container objects like `NavDisplay` can read this metadata to decide how to display the `NavEntry`'s content. For example, you can use metadata to override the default animation for a specific `NavEntry`. NavEntry `metadata` maps `String` keys to `Any` values, so it can hold all kinds of data.

To turn a `key` into a `NavEntry`, you create an `entryProvider`: a function that takes a `key` and returns the `NavEntry` for that `key`. You usually define it as a lambda parameter when you create the `NavDisplay`.

There are two ways to create an `entryProvider`: write the lambda yourself, or use the `entryProvider` DSL.

![](images/07.webp){width=100 align=center}

You can take a key in a **lambda** and return a NavEntry, as above,

![](images/08.webp){width=100 align=center}

or write the same thing more cleanly with the **DSL**.

If you want to attach metadata, extend it like this:

![](images/09.webp){width=100 align=center}

Put the `entryProvider` above together with `NavDisplay`, and you end up with something like this:

![](images/10.webp){width=100 align=center}

`NavDisplay` passes the last `key` in the back stack to the `entryProvider` and shows the screen from the `NavEntry` it gets back.

NavEntry isn't only for rendering screens. It's also how you define **a ViewModel scope that's valid only for that screen**. For example, when DetailScreen is removed from the back stack, you can have the ViewModel attached to it removed automatically too.

## 🎯 Saving the back stack safely

We've already handled configuration changes safely with remember. To keep navigation state safe through process death as well, you can use what Navigation 3 provides.

### Implement the NavKey interface

**Every key in the back stack must implement the NavKey interface.** It's a marker interface that tells the library the key can be saved.

![](images/11.webp){width=100 align=center}

Make your existing keys implement **NavKey** like this and you're done. You still need serialization, though. Now let's go through two ways to save the back stack safely.

### 1️⃣ Using `rememberNavBackStack`

The `rememberNavBackStack` composable is designed to create a back stack that survives configuration changes and process death. For `rememberNavBackStack` to work correctly, every key in the back stack has to meet these requirements:

- **Implement the `NavKey` interface**: every key in the back stack must implement the `NavKey` interface.
- **Add the `@Serializable` annotation**: on top of implementing `NavKey`, your key classes and objects must be annotated with `@Serializable`.

![](images/12.webp){width=100 align=center}

### 2️⃣ Storing the back stack in a ViewModel (plus scoping)

Another way to manage the back stack is to store it in a `ViewModel`. If you use a `ViewModel` or other custom storage and want the back stack to survive process death, you need to:

- **Make your keys serializable**: just like with `rememberNavBackStack`, navigation keys have to be serializable.
- **Handle serialization and deserialization yourself**: you're responsible for saving each key's serialized form to persistent storage (e.g. SharedPreferences, a database, or a file) when the app goes to the background, and deserializing it when the app is restored.

![](images/13.webp){width=100 align=center}

- With `mutableStateListOf`, Compose UI automatically observes the list and updates the UI.

You could stop once it's stored in the ViewModel, but there's something to think about from a lifecycle point of view.

By default, a ViewModel shares its lifecycle with an Activity or Fragment.

In Navigation 3, though, you might want to manage state separately for each screen.

> For example, if you want state that only DetailScreen uses to be **cleared automatically when the user goes back**

> → you need to **scope the ViewModel to the NavEntry**.

The `androidx.lifecycle:lifecycle-viewmodel-navigation3` add-on library provides a `NavEntryDecorator` that makes this easy. The decorator gives each `NavEntry` its own `ViewModelStoreOwner`. When you create a `ViewModel` inside a `NavEntry`'s content (for example with `viewModel()` in Compose), it's automatically scoped to that `NavEntry`'s key in the back stack. **In other words, the `ViewModel` is created when the `NavEntry` is added to the back stack and cleared when it's removed.**

Scoping a ViewModel to a NavEntry in Navigation 3 is as simple as this:

![](images/14.webp){width=100 align=center}

- Add the `entryDecorators` parameter to `NavDisplay`.
- Pass it a **decorator the library provides**, such as `NavEntryViewModelDecorator()`.

⇒ Now each ViewModel belongs to its screen, and when that screen leaves the back stack, its ViewModel is cleared along with it.

## 📝 The whole flow in four steps

![](images/15.webp){width=100 align=center caption="Source: official docs"}

1. **A navigation event starts the change.** Keys are added to or removed from the back stack in response to user interaction.
2. **The back stack change triggers a content lookup.** `NavDisplay` (the composable that renders the back stack) observes the back stack. In its default configuration, it shows the top back stack entry in a single-pane layout. When the top key changes, `NavDisplay` uses that key to ask the `entryProvider` for the matching content.
3. **The entry provider supplies the content.** `entryProvider` is a function that resolves a key to a `NavEntry`. When the `entryProvider` receives a key from `NavDisplay`, it provides the associated `NavEntry`, which holds both the key and the content.
4. **The content is displayed.** `NavDisplay` receives the `NavEntry` and displays its content.

## Final thoughts

This time I looked at Nav3, which is brand new. Learning new tech always leaves me with mixed feelings: it's fun and fascinating, and it also makes me sigh at how much more there is to study. Nav3, though, was a manageable size with a lot to learn from, so it was a good experience. Along the way I found [Voyager](https://github.com/adrielcafe/voyager), an open-source navigation library for Compose Multiplatform (CMP), and I got the sense that Jetpack is pushing hard toward multiplatform. I suspect the whole point of supporting multi-pane layouts is groundwork for showing multiple screens at once on the web.

Still, being able to **manipulate the back stack explicitly** makes things far more flexible. I was also really impressed by **scoping a ViewModel** to each screen. It could be a good answer to messy situations like the difference between `hiltViewModel()` and `viewModel()`, or scope and instance-mismatch problems with shared ViewModels.

That said, I still have one doubt: is a plain mutable list really the right place to keep something as important as the back stack, along with the keys that carry each screen's information? On my next project, I want to use Nav3 for real and hit those trade-offs myself.

(This post doesn't cover `Scene` or the animation features that are also part of the Nav3 update. I'll study them and update this post if I get a chance to use them. `Scene` in particular looks pretty complex and puts me off a little, but since it seems to be mainly for the web and tablets, I probably won't need it right away.)

## References

[https://github.com/android/nav3-recipes](https://github.com/android/nav3-recipes)

[https://android-developers.googleblog.com/2025/05/announcing-jetpack-navigation-3-for-compose.html](https://android-developers.googleblog.com/2025/05/announcing-jetpack-navigation-3-for-compose.html)

[https://developer.android.com/guide/navigation/navigation-3?hl=ko&_gl=1\*fklray\*_up\*MQ.._*ga*MTk5MDIyNTE5MC4xNzQ5MzA1ODky\*_ga_6HH9YJMN9M\*czE3NDkzMDg4MTckbzIkZzAkdDE3NDkzMDg4MTckajYwJGwwJGg2MDU5MjI2NDA](https://developer.android.com/guide/navigation/navigation-3?hl=ko&_gl=1*fklray*_up*MQ.._gaMTk5MDIyNTE5MC4xNzQ5MzA1ODky*_ga_6HH9YJMN9M*czE3NDkzMDg4MTckbzIkZzAkdDE3NDkzMDg4MTckajYwJGwwJGg2MDU5MjI2NDA).
