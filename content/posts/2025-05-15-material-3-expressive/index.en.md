---
title: A first look at Material 3 Expressive
summary: A tour of Material 3 Expressive, Google's design update for Android 16, with hands-on Compose code for each new component.
source: 85b69e0ebe8f
---

Early this morning (May 13, 2025, as I write this), Google announced **Material 3 Expressive**, its new design system. It's supposed to ship **starting with Android 16, which lands later this year**. This post covers the big picture, and I'll also try the new and updated components myself, in Figma and in code.

(Google I/O 2025 is next week and should go into more detail, so I'll update this post after the talks.)

## What is Material 3 Expressive?

Material 3 Expressive is the latest version of Material Design, and Google pitches it as a new way to build products that are more engaging, easier to use, and more desirable. The goal is an expressive interface: visual design and interaction that evoke a feeling or a mood, so the product has an emotional impact and builds a connection with the people using it. It's an evolution of the Material 3 design system. Google says M3 isn't being deprecated and this isn't M4, so it's less a hit to existing developers than an update that opens up new options.

Google lists four effects of Material 3 Expressive:

1. People of all ages prefer it.
2. It scored high on playfulness, energy, creativity, and friendliness.
3. Users are more likely to switch to products that use M3 Expressive components and techniques.
4. Expressive design is easy to use: people spot the key UI elements on a screen 4x faster.

### Why it matters

According to the Google Design team, Material 3 Expressive didn't come out of a **41 shades of blue** approach, where design decisions are handed over to the data. It came out of joint work spanning research, design, and engineering.

41 shades of blue was an episode from 2009, when Google pushed its data-driven approach to the extreme. To decide which blue worked best, Google tested 41 different shades, ran A/B tests on the Google homepage, Gmail, and other sites, and picked the one that got the most clicks. It was one of the reasons Douglas Bowman, Google's head of design at the time, left the company, and it set off a big debate in the design community about how to balance data-driven decisions against creative intuition. The lesson Google took from the whole episode: **good design needs a balance of data analysis, creative intuition, and a deep understanding of the user experience**.

I'd call the launch of Material 3 Expressive a milestone that shows how much Google's design philosophy has matured. About 15 years after 41 shades of blue, Google is striking a more refined balance between data and creativity. Material 3 Expressive was developed through 46 separate studies with more than 18,000 participants around the world, and it doesn't chase efficiency alone. It puts weight on users' emotional experience and on an app's personality.

That matters for Android developers too. We can now build apps that aren't just functionally solid but also connect with users on an emotional level. Material 3 Expressive started from the questions "Why do all these apps look the same?" and "Why are they so boring?", and it grew into a design system that balances technology and emotion. It shows Google moving away from leaving everything to the data and toward research, design, and engineering working together to open up new ground in user experience.

The update has three parts: **Expressive Components, Expressive Styles, and Expressive Tactics**. This post focuses on the components, and I'll fill in the rest once more details are announced.

## Expressive components

![](images/01.webp){width=100 align=center}

Fifteen components are new or updated, with more features and shape options, emphasized text, and other expressive touches.

![](images/02.webp){width=100 align=center}

I'll focus on the newly added components. For each one, I wrote the code myself based on the Material 3 Design Kit and the samples in the official docs, and I'll walk through how to use it and what stands out. A few components get **deprecated** in this update, so keep an eye out for those.

### 1️⃣ Button groups

![](images/03.webp){width=100 align=center caption="Button Groups"}

- There are two types: standard and connected.
- Buttons can change shape when they're pressed or selected.
- **Connected button groups replace the existing segmented button.**
- They work with every button size: XS, S, M, L, and XL.
- They support single-select, multi-select, and selection-required.
- They can include icon buttons too.

#### Standard button group with overflow

```kotlin
val numButtons = 10

ButtonGroup(
    // ★ Set up the overflow menu indicator
    overflowIndicator = { menuState ->
        IconButton(
            onClick = {
                // Toggle the menu state
                if (menuState.isExpanded) {
                    menuState.dismiss()
                } else {
                    menuState.show()
                }
            }
        ) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "Localized description"
            )
        }
    }
) {
    // Add a bunch of buttons to the group
    for (i in 0 until numButtons) {
        clickableItem(onClick = {}, label = "$i")
    }
}
```

![](images/04.webp){width=100 align=center caption="Standard Button Group preview"}

- The `ButtonGroup` component groups several buttons together.
- `overflowIndicator` creates a menu button that signals there are more buttons when they can't all fit on screen.
- `menuState` manages the overflow state.
- `clickableItem()`, which `ButtonGroupScope` provides, creates the buttons dynamically.

![](images/05.webp){width=100 align=center caption="clickableItem"}

#### Connected toggle button group

```kotlin
val options = listOf("Add", "Call", "Account")
    val unCheckedIcons = listOf(Icons.Outlined.Add, Icons.Outlined.Call, Icons.Outlined.AccountBox)
    val checkedIcons = listOf(Icons.Filled.Add, Icons.Filled.Call, Icons.Filled.AccountBox)
    // Track each button's checked state
    val checked = remember { mutableStateListOf(false, false, false) }

    Row(
        Modifier.padding(horizontal = 8.dp),
        // ★ Spacing for a connected button group
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        // Set each button's weight (widths as a ratio)
        val modifiers = listOf(Modifier.weight(1f), Modifier.weight(1f), Modifier.weight(1.3f))

        options.forEachIndexed { index, label ->
            // ★ Use a toggle button
            ToggleButton(
                checked = checked[index],
                onCheckedChange = { checked[index] = it },
                modifier = modifiers[index],
                // ★ Different shape depending on position (first, middle, last)
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
            ) {
                // Show a different icon depending on the checked state
                Icon(
                    if (checked[index]) checkedIcons[index] else unCheckedIcons[index],
                    contentDescription = "Localized description",
                )
                Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                Text(label)
            }
        }
    }
```

![](images/06.webp){width=80 align=center caption="Connected Toggle Button Group preview"}

- `ToggleButton` gives each button a checked and unchecked state.
- `connectedXXXButtonShapes()` applies a different shape to each position.
- The `weight` modifier gives each button its own width.

### 2️⃣ Floating action button menu (FAB menu)

![](images/07.webp){width=100 align=center caption="FAB Menu"}

- **You no longer have to build a FAB menu yourself.** Use it when a FAB opens up to show 2 to 6 related actions.
- **It isn't used with extended FABs.**
- It works with the existing primary, secondary, and tertiary color sets.
- It can replace the old speed dial and the stacked-small-FABs approach.
- It has a single menu size that works with every kind of FAB.
- Menu items use a color that contrasts with the close button.

#### Example

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FABMenu() {
    val listState = rememberLazyListState()
    // Show or hide the FAB based on the scroll position
    val fabVisible by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }

    Box {
        // Regular list content (omitted)...

        // Items shown in the FAB menu
        val items = listOf(
            Icons.AutoMirrored.Filled.ExitToApp to "Exit",
            Icons.Filled.Add to "Add",
            Icons.Filled.Call to "Call",
        )

        // FAB menu expanded state
        var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }

        // Handle back: close the menu while it's expanded
        BackHandler(fabMenuExpanded) { fabMenuExpanded = false }

        // ★ The new FAB Menu component
        FloatingActionButtonMenu(
            modifier = Modifier.align(Alignment.BottomEnd),
            expanded = fabMenuExpanded,
            button = {
                // ★ A toggleable FAB
                ToggleFloatingActionButton(
                    modifier = Modifier
                        .semantics { traversalIndex = -1f }
                        // ★ Animate the FAB as the list scrolls
                        .animateFloatingActionButton(
                            visible = fabVisible || fabMenuExpanded,
                            alignment = Alignment.BottomEnd,
                        ),
                    checked = fabMenuExpanded,
                    onCheckedChange = { fabMenuExpanded = !fabMenuExpanded },
                ) {
                    // ★ Swap the icon by state (+ → X) and animate it
                    val imageVector by remember {
                        derivedStateOf {
                            if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add
                        }
                    }
                    Icon(
                        painter = rememberVectorPainter(imageVector),
                        contentDescription = null,
                        // ★ Apply the icon animation
                        modifier = Modifier.animateIcon({ checkedProgress }),
                    )
                }
            },
        ) {
            // Build the menu items
            items.forEachIndexed { i, item ->
                // ★ Component for each menu item
                FloatingActionButtonMenuItem(
                    // Accessibility
                    modifier = Modifier.semantics { /*...*/ },
                    onClick = { fabMenuExpanded = false },
                    icon = { Icon(item.first, contentDescription = null) },
                    text = { Text(text = item.second) },
                )
            }
        }
    }
}
```

What to notice:

- The FAB menu holds a main button and several menu items.
- `ToggleFloatingActionButton` is a new component that, unlike a regular FAB, has a toggle state. It can change its shape or content based on `checked`.
- `animateFloatingActionButton()` animates the FAB in and out.
- `BackHandler` closes the menu when the user presses back while it's expanded.

### 3️⃣ Loading indicator

![](images/08.gif){width=100 align=center}

- **Google recommends it as a replacement for the circular progress indicator.**
- It's tuned for loads that take less than 5 seconds.
- It reflects actual progress, not just decoration.
- It's used for pull-to-refresh.
- There are two types, **Loading Indicator** and **Contained Loading Indicator**. The difference is whether there's a background container.
- It can morph its shape as loading progresses, and it scales to a range of sizes.
- Don't use it when progress switches from indeterminate to determinate.

#### Example

```kotlin
// Progress statevar progress by remember { mutableFloatStateOf(0f) }

// Animated progress
val animatedProgress by
    animateFloatAsState(
        targetValue = progress,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessVeryLow,
                visibilityThreshold = 1 / 1000f
            )
    )

Column(horizontalAlignment = Alignment.CenterHorizontally) {
    // ★ Use the loading indicator
    LoadingIndicator(progress = { animatedProgress })
    
    Spacer(Modifier.requiredHeight(30.dp))
    
    // Slider to set the progress for the demo
    Text("Set loading progress:")
    Slider(
        modifier = Modifier.width(300.dp),
        value = progress,
        valueRange = 0f..1f,
        onValueChange = { progress = it },
    )
}
```

![](images/09.gif){width=70 align=center caption="Loading Indicator running on the emulator"}

There's a bit of lag, but you can see the indicator change shape as the progress changes.

### 4️⃣ Split button

![](images/10.webp){width=100 align=center}

- It's a compound button that pairs a primary action with a menu of related actions.
- **It combines a primary action button on the left (the leading button) with a menu icon button (the trailing button).**
- It comes in four styles: elevated, filled, tonal, and outlined.

#### Example

```kotlin
    // Menu visibility state
    var checked by remember { mutableStateOf(false) }

    SplitButtonLayout(
        // Primary action button on the left
        leadingButton = {
            SplitButtonDefaults.LeadingButton(
                onClick = { /* Handle the primary action */ },
            ) {
                // Icon + text
                Icon(
                    Icons.Filled.Edit,
                    modifier = Modifier.size(SplitButtonDefaults.LeadingIconSize),
                    contentDescription = "Localized description",
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("My Button")
            }
        },
        // Menu button on the right
        trailingButton = {
            SplitButtonDefaults.TrailingButton(
                checked = checked,  // Menu visibility state
                onCheckedChange = { checked = it },  // Handle state changes
                modifier = Modifier.semantics {
                    stateDescription = if (checked) "Expanded" else "Collapsed"
                },
            ) {
                // ★ Apply a rotation animation
                val rotation: Float by animateFloatAsState(
                    targetValue = if (checked) 180f else 0f,
                    label = "Trailing Icon Rotation"
                )
                // Menu icon (rotated)
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    modifier = Modifier
                        .size(SplitButtonDefaults.TrailingIconSize)
                        .graphicsLayer { this.rotationZ = rotation },
                    contentDescription = "Localized description"
                )
            }
        }
    )
```

![](images/11.webp){width=70 align=center caption="Split Button preview"}

- `SplitButtonDefaults.LeadingButton` and `SplitButtonDefaults.TrailingButton` create the two buttons.
- The filled, tonal, elevated, and outlined properties in `SplitButtonDefaults` give the button the matching style.

### 5️⃣ Toolbars

![](images/12.webp){width=100 align=center}

- **This component replaces the bottom app bar, which is going to be deprecated.**
- There are two main types: the **docked toolbar** and the **floating toolbar**.
- According to Google, the docked toolbar is shorter and more flexible, while the floating toolbar is built for more features, more actions, and more placement options.
- Use a `Docked Toolbar` at the bottom of the screen when you want to replace the old bottom app bar and save space. Use a `Floating Toolbar` when you need to show more actions, place the toolbar at a specific spot on screen, or want a fluid UI that reacts to scrolling.
- You can pair it with a FAB to highlight a specific action.
- Don't use it together with a navigation bar.

#### Horizontal floating toolbar example

```kotlin
    // Toolbar expanded state
    var expanded by rememberSaveable { mutableStateOf(true) }

    Scaffold(
        content = { innerPadding ->
            Box(Modifier.padding(innerPadding)) {
                // Main content (LazyColumn)
                LazyColumn(
                    // ★ Scroll modifier wired to the floating toolbar
                    modifier = Modifier.floatingToolbarVerticalNestedScroll(
                        expanded = expanded,
                        onExpand = { expanded = true },
                        onCollapse = { expanded = false },
                    ),
                    state = rememberLazyListState(),
                    contentPadding = innerPadding,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // List items - omitted
                }

                // ★ The horizontal floating toolbar
                HorizontalFloatingToolbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = -ScreenOffset),  // Lift it slightly off the bottom of the screen
                    expanded = expanded,             // Hook up the expanded state
                    leadingContent = {
		                    // Left content
                        FilledIconButton(
                            modifier = Modifier.width(64.dp),
                            onClick = { /* doSomething() */ },
                        ) {
                            Icon(
                                Icons.Filled.Call,
                                contentDescription = "Localized description",
                            )
                        }
                    },   
                    trailingContent = {
                       // Right content - omitted
                    }, 
                    content = {
                        // Center content - omitted
                    }, 
                )
            }
        },
    )
```

![](images/13.webp){width=60 align=center}

- `Alignment` lets you place it wherever you want.
- You're free to fill in the left, right, and center content.
- It can change state as you scroll. Scroll the list from the code above down a little and it changes like this, driven by the `expanded` parameter.

![](images/14.webp){width=40 align=center caption="How it changes when you scroll down"}

That covers the new components. Across the board, animation got a big boost, and the emphasis on motion and shape morphing shows how much Google cares about emotional interaction. What impressed me most is that it now ships components I used to have to build myself.

## Expressive styles

Material 3 Expressive uses four main style elements to make the experience feel more emotional and alive. Here's a quick rundown of each.

#### Motion-physics system

![](images/15.webp){width=100 align=center}

- **Built on motion springs**: a motion spring system makes interactions and transitions feel more alive, fluid, and natural
- **Two spring types**
  - **Spatial springs**: mirror the physics of real objects in motion, for clear, predictable animations
  - **Effects springs**: smooth transitions for color and opacity changes

#### Visually emphasized typography

![](images/16.webp){width=100 align=center}

- **New type styles**: new styles for variable and static fonts
- **Emotional expression**: typography designed to convey a range of emotional states
- **Automatic tuning**: font variables adjust automatically for legibility
- Support for bold editorial layouts
- Makes important information stand out, like "Start recording" or unread messages
- Typography options that express a brand's personality more strongly

#### Expanded shape library

![](images/17.webp){width=100 align=center}

- **35 new shapes**: a range of shapes for adding decorative detail to image crops, avatars, and more
- **Shape morph animation**: built-in animation that smoothly transitions from one shape to another
- **Dynamic change**: anything from a simple square-to-circle change to complex dynamic morphs

#### Vibrant color schemes

![](images/18.webp){width=100 align=center}

- **Expanded color range**: a wider palette that sharpens hierarchy and makes key actions clear
- **Rich visual styles**: colorful visual styles that support personalization and dynamic color
- **Dynamic color system**: a color system that adapts to the user's environment and preferences

## Expressive tactics

These are guidelines for steering the user's attention to the most important element on a screen. I think these seven tactics from Google can make components, layouts, and whole products more expressive.

#### 1. Use a variety of shapes

- Treat shape as a strong communication tool in the interface
- Mix different shapes and corner radii to create visual tension or cohesion
- Combine classic and abstract shapes for distinctive silhouettes or groupings

#### 2. Apply rich and nuanced colors

- Use Material's dynamic color system for a wide range of colors
- Blend color into key components and visual elements to emphasize the main message of the screen
- Build visual hierarchy with surface tones

#### 3. Guide attention with typography

- Use emphasized text styles to draw attention to important UI elements like headlines and actions
- Emphasize typography to create editorial-like moments in your app
- Use heavier weights, larger sizes, color, and spacing to guide attention and make key information more engaging

#### 4. Contain content for emphasis

- Organize content into logical groups or containers
- Give the most important content, tasks, or actions visual weight with generous space and bright surface mapping
- Use grouping principles like size, spacing, rhythm, and similarity to set important elements apart

#### 5. Add fluid and natural motion

- Make interactions lively and energetic with shape morphs and surface effects
- Apply expressive motion springs or custom micro-animations

#### 6. Leverage component flexibility

- The UI should adapt to the user's context
- Change components or controls to suit the environment so tasks are easier to finish
- Adapt content to foldables and large screens with custom adjustments or canonical layouts

#### 7. Combine tactics to create hero moments

- Combine several expressive tactics to break away from predictable or uniformly applied design ideas
- Make a bold statement, or frame essential information in a fresh, editorial way
- Hero moments work as a focusing mechanism
- Spend your time making the most important interactions stand out

**What makes a hero moment:**

- It's brief, delightful, surprising, and unexpected
- Keep it to one or two per product (too many can feel overwhelming or distracting)

**Questions for spotting a hero moment:**

1. Is this interaction emotionally impactful? (Consider whether a design choice could emphasize an emotional response or a sense of familiarity.)
2. Is this a core interaction in the product? (Think about whether details or flows could be emphasized for clarity.)

## My take

It feels like Google's design team put a lot into this, and Android 16 looks like a turning point for a lot of things. I'm excited, but also a little worried. I still have edge-to-edge work to do, and now several components are being deprecated (you can keep using them, of course) and there are new ones to learn. The wider set of options means I could try a more emotional design on my next new app, but realistically, I don't expect many apps that are already shipping to adopt Material 3 Expressive. I'll need to see what's in Android 16 and what Google announces at I/O 2025 next week before I can really judge.

Still, I think Material 3 Expressive matters when it comes to differentiating an app and expressing a brand's personality. Material 3 was light on emotional elements and leaned on static components, while Material 3 Expressive has evolved to offer distinctive experiences through variety and flexibility. As the digital world matures, users want more than functional efficiency. They want emotional satisfaction and a connection to the brand, and M3 Expressive is Google's answer to those expectations.

I'm building a watch app as a side project, so I was surprised by how much the watch design system is changing too. I'll need a Wear OS 6 device to try it, but I'm looking forward to shipping a more polished design in my own app. Google says the design changes come with battery optimizations as well, and it's nice to see Google paying this much attention to Wear OS.

This was a fun post to write: I learned about the 41 shades of blue story and got to build the new components myself. The code in this post works once you add the `androidx.compose.material3:material3:1.4.0-alpha14` library. (Some of it works on lower versions too, but check the Material3 [official docs](https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary?_gl=1*gu0bdd*_up*MQ..*_ga*MjMyMDA5NTU1LjE3NDczMTM1NTI.*_ga_6HH9YJMN9M*czE3NDczMTM1NTIkbzEkZzAkdDE3NDczMTM1NTIkajAkbDAkaDIwOTUyNzI1MDQ.#top-level-functions) for details. The docs also list the components that are going to be deprecated.)

### References

[https://android-developers.googleblog.com/2025/05/the-android-show-io-edition.html](https://android-developers.googleblog.com/2025/05/the-android-show-io-edition.html)

[https://design.google/library/expressive-material-design-google-research](https://design.google/library/expressive-material-design-google-research)

[https://m3.material.io/blog/building-with-m3-expressive](https://m3.material.io/blog/building-with-m3-expressive)

[https://blog.google/products/android/material-3-expressive-android-wearos-launch/](https://blog.google/products/android/material-3-expressive-android-wearos-launch/)

[https://www.figma.com/community/file/1035203688168086460](https://www.figma.com/community/file/1035203688168086460)

::youtube[l3yDd3CmA_Y]
