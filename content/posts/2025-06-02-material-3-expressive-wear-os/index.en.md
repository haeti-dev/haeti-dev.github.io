---
title: A closer look at Material 3 Expressive on Wear OS
summary: What Material 3 Expressive changes on Wear OS 6, from round-screen components and new tiles to variable typography.
source: d68715319fb9
---

In my [earlier look at Material 3 Expressive](post/material-3-expressive), I covered what's changing on phones. Google's design team talked about the Wear OS side of Material 3 Expressive more than I expected, and it clearly matters to them, so this time I'm looking at Material 3 Expressive on Wear OS.

![](images/01.webp){width=100 align=center}

## Fluid design for round displays

![](images/02.gif){width=70 align=center}

On the watch, Material 3 Expressive is designed around the **round display**. The design is fluid at its core, with **scroll animations that follow the curve of the screen**. **Lists animate as you scroll**, which adds a sense of depth and makes information easier to get to. Google also says it reworked everyday experiences like using a PIN pad and controlling media, with motion and responsive feedback. On top of that, elements change shape to fit the watch's small screen, so transitions on the watch feel smooth.

## Richer color schemes for personal themes

![](images/03.webp){width=100 align=center}

Material always seems to push this one, and sure enough, the update brings a **consistent experience to Wear OS too**. The Wear Compose Material3 and Wear Protolayout Material 3 libraries were updated with an **expanded color scheme, typography, and shapes**, and the goal seems to be giving designs both **depth and variety**. I really like that tiles now align with the system font by default.

The end result is that **dynamic color theming** now works fully on watches too, with themes generated automatically.

## New tile components that read at a glance

![](images/04.webp){width=100 align=center}

On a watch, a **tile** is something you reach by swiping from the home screen, without opening the app. Tiles exist **to give you quick access to an app's core features**, and I use them all the time: when I'm working out, when I'm checking when the bus will arrive, and so on.

Tiles now support a new framework and a set of components built for the watch's round form factor. These components make tiles **more consistent and easier to read at a glance**, so users can act on the information in a tile **more easily and quickly**.

To make the tile carousel more visually consistent, Google introduced a **3-slot tile layout**. It has a **title slot, a main content slot, and a bottom slot**, and it's designed to work across screen sizes. Personally, I don't think the 3-slot layout simply cuts the screen into thirds. It's **a smart design that puts the [Gestalt principle of proximity](https://brunch.co.kr/@jiyuhan/87) (in Korean) to work**. It seems to make the most of the way our brains naturally group information, inside the cramped space of a wearable. On a smartwatch, where a quick glance is everything, I think this kind of psychology-driven approach has a direct effect on usability.

## Components built for round screens

The new Wear OS Material 3 components **adjust automatically to larger screens**, building on the large-display support added in Wear OS 5. Components like buttons and lists also support shape morphing in apps. Here's one example:

![](images/05.webp){width=100 align=center}

There's a new `EdgeButton` for apps and tiles, a signature design pattern that hugs the edge of the screen to make the most of the round form factor. It comes in four standard sizes. Look at the container with the "One time" text and you'll see its shape is cut to match the curve of the watch.

![](images/06.gif){width=100 align=center}

Items also change shape as they enter and leave at the edges of the screen (the top and bottom), which makes for a really natural animation. Wear OS devices have round screens, so list items near the top and bottom are hard to see, and that's why this layout exists.

![](images/07.gif){width=60 align=center}

Concretely, this is what happens when you scroll. Items morph very naturally as they fade in and out, and apparently you can build this with `TransformingLazyColumn`. Here's a snippet from the [official docs](https://developer.android.com/training/wearables/compose/lists?hl=ko&version=3):

```kotlin
val columnState = rememberTransformingLazyColumnState()
val contentPadding = rememberResponsiveColumnPadding(
    first = ColumnItemType.ListHeader,
    last = ColumnItemType.Button,
)
val transformationSpec = rememberTransformationSpec()
ScreenScaffold(
    scrollState = columnState,
    contentPadding = contentPadding
) { contentPadding ->
    TransformingLazyColumn(
        state = columnState,
        contentPadding = contentPadding
    ) {
        item {
            ListHeader(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec)
            ) {
                Text(text = "Header")
            }
        }
        // ... other items
        item {
            Button(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
                onClick = { /* ... */ },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = "build",
                    )
                },
            ) {
                Text(
                    text = "Build",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
```

To show the scroll indicator, share `columnState` between `ScreenScaffold` and `TransformingLazyColumn`. The rest doesn't look much different from the `LazyColumn` code you already know.

## Variable typography

![](images/08.webp){width=100 align=center}

Roboto is the default font on watches. This update adds Roboto Flex, which brings variable font support. Google also says variable font axes can make motion feedback and interactions more expressive, in cases like these:

![](images/09.webp){width=100 align=center}

- Dynamic font weight
- Dynamic font width
- Dynamic font weight and width

## Containers that fit the edge

![](images/10.webp){width=100 align=center}

The first time I saw this design, it threw me a little. I'd never seen anything like it, so it felt odd, but now there's a concept of shaped containers that make the most of a tiny space, and I think they'll come in handy. Think about [**Fitts's law**](https://brunch.co.kr/@doyeong/14) (in Korean): on a screen as small as a watch's, this design should help **take as much stress as possible out of the moment a user taps**.

> **Fitts's law**: the time it takes to reach a target depends on the target's size and its distance.

## Adaptive shapes

![](images/11.webp){width=100 align=center}

Google showed off expressive designs that use corner radii and distinctive shapes as containers. It'd be fun to try building UI that gets a lot of mileage out of distinctive shapes on a small screen.

## Final thoughts

That's Material 3 Expressive, which lands on Android 16 and Wear OS 6 devices at the same time. I expect to see apps using it toward the end of this year, and I'm looking forward to that. Personally, I think it's a really pretty, bold change, and as someone who loves Wear OS, I'm relieved to see Google paying this much attention to watches. Google also says Wear OS 6 improves battery life by more than 10%, which is another thing I'm excited about.

I think the biggest change Material 3 Expressive brings to watches is \<turning the limits of screen size into a design language\>. Material Design used to be phone-first. This time, what impressed me is that it treats the traits of wearables (round, small screens) **as character instead of constraints**. `EdgeButton` in particular doesn't just save space. It creates something unique that only a round display can have.

Google also highlighted the release of **Watch Face Format 4**. A **watch face** is roughly the watch equivalent of **the components you put on a phone's lock screen**. The blog post below covers that update, and if I end up building a watch face myself, I'll write a follow-up.

Watch Face 4 : [https://android-developers.googleblog.com/2025/05/whats-new-in-watch-faces.html](https://android-developers.googleblog.com/2025/05/whats-new-in-watch-faces.html)

### References

[https://developer.android.com/design/ui/wear/guides/get-started/apply?hl=ko](https://developer.android.com/design/ui/wear/guides/get-started/apply?hl=ko)
