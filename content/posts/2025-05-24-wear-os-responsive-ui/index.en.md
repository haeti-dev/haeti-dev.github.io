---
title: Making a Wear OS start screen fit every watch
summary: My watch app's start screen only looked right on my own Galaxy Watch4, so I reworked it to scale across round Wear OS watches.
source: 0fd3c9e6ce41
---

## Background

This year I gave a watch-related talk at [GDG Super.init(version=6)](https://event-us.kr/gdgandroid/event/99825) (event page in Korean). To keep the audience interested, I demoed a watch app I'd planned and built myself as a side project. It'll ship once I fix a few small issues, and an iOS version should follow in the second half of the year.

I didn't have much time to prepare, so for the demo I took a shortcut and made the app **only look right on my own Galaxy Watch4**. That meant it had to work on other watches before release. This post walks through how I did that for the start screen, which was the screen hit hardest by differences between devices.

## ⚠️ The problem

I designed the app for watches running Wear OS. Most of them (the Google Pixel Watch, the Galaxy Watch) are round, so I designed around a round screen.

![](images/01.webp){width=50 align=center}

To fit the app's concept, I went with a waveform look, like a heartbeat rippling outward, and put the app's text logo and a short prompt to tap and start in the middle. This screen may change once I add Apple Watch support.

To get it built quickly, I stacked three circular Boxes in different colors at fixed sizes. But I'd overlooked something: **the watch's bezel** was bigger than I expected, so the black ring looked almost twice as thick as intended. (What's a bezel? It's the border around a display. It holds the display panel in place and protects it, so every display needs one for structural reasons.)

![](images/02.webp){width=100 align=center caption="The black area is the bezel"}

I was really short on time, so I just adjusted the code for the Galaxy Watch4's bezel size. Here's how it looked in the preview:

![](images/03.webp){width=100 align=center}

My watch matches **WearPreviewDevices 2** in the image above, and once the bezel was accounted for, the design looked the way I'd intended. I ran the demo on my own watch during the talk, so it went off without a hitch, and as far as I could tell, people liked it.

## 🔎 What watches are people wearing?

Before doing the real work, I looked into who my users would be. From what I found, Wear OS watches mostly have 40–47mm cases and 1.2–1.5 inch screens, with bezels of around 2–4mm. In Korea, Samsung's Galaxy Watch is by far the most popular, and the 44mm model seems to be the most typical size. On average, the bezel takes up about 8% of the screen.

With that in mind, I decided to **make the bezel part of the design**. That way I keep the original design and get to use more of the screen.

## 🖌️ Drawing circles from ratios

```kotlin
BoxWithConstraints(
    modifier = Modifier
        .fillMaxSize()
        .clickable { /* …navigation… */ },
    contentAlignment = Alignment.Center
) {
    val screenWidthDp = maxWidth
    
}
```

First, I use `BoxWithConstraints` to read the maximum width and height (`maxWidth`, `maxHeight`) at runtime. It tells you the size of the current device's screen, or the size the current composable has been given.

Watch screens are round, so width and height are the same and `maxWidth` == `maxHeight`.

```kotlin
Canvas(modifier = Modifier.matchParentSize()) {
    val radiusPx = size.width / 2f
    val center = Offset(rPx, rPx)
    
    // 1) Gradient circle (red → black fade)
    val gradientBrush = Brush.radialGradient(
        colorStops = arrayOf(
            0.00f to RedTertiary,  // 0% of the radius: pure RedTertiary
            0.85f to RedTertiary,  // up to 85% of the radius: still RedTertiary
            1.00f to Color.Black   // 100% of the radius: Black
        ),
        center = center,
        radius = radiusPx
    )
    drawCircle(brush = gradientBrush, radius = radiusPx)

    // 2) Middle circle: 95% radius (RedSecondary)
    drawCircle(color = RedSecondary, radius = radiusPx * 0.95f)

    // 3) Inner circle: 75% radius (RedPrimary)
    drawCircle(color = RedPrimary, radius = radiusPx * 0.75f)
}
```

Then I draw the circles on a Canvas. The built-in `drawCircle` function draws a circle with whatever radius you give it, and the radius is just half of the size the Canvas provides. **Multiply the radius by the ratio you want, and the UI keeps the same proportions on any watch model.** (The outermost circle needed a gradient, so I colored it with a `Brush`.)

![](images/04.webp){width=60 align=center}

The circles now had the right proportions, but the logo and text sit inside the innermost circle, and if they spill past its edge it looks bad. That needed one more fix.

## Scaling the content

The simple fix is to size the inner content (the image and text) for my own watch, then **scale it to the size of the user's watch**. I wanted the text to always render at the same size, so I used `dp` instead of `sp`. `sp` grows and shrinks when the user changes the font size in system settings, which could push the text outside the inner circle I'd designed it to sit in.

First I defined a reference model: I took my own watch's screen width as the baseline and computed the ratio against it.

```kotlin
	val screenWidthDp = maxWidth

        // Based on a 44mm screen
        val baseWidthDp = 218.dp

        val scale = screenWidthDp / baseWidthDp

        val baseImageWidth  = 108.dp
        val baseImageHeight = 70.dp
        val imageWidth  = baseImageWidth  * scale
        val imageHeight = baseImageHeight * scale

        val baseFontSize  = 15.toTextDp
        val baseLineHeight = 23.toTextDp
        val fontSize   = baseFontSize * scale
        val lineHeight = baseLineHeight * scale

        val baseSpacer = 10.dp
        val spacerHeight = baseSpacer * scale
```

Because I'm using `BoxWithConstraints`, `maxWidth` gives me the size of the user's screen for free, and dividing it by the baseline width gives the scale factor.

```kotlin
    Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.img_logo),
                    contentDescription = "logo",
                    modifier = Modifier.size(width = imageWidth, height = imageHeight)
                )

                Spacer(Modifier.height(spacerHeight))

                Text(
                    text = "Tap the screen\\nto start",
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    lineHeight = lineHeight,
                    textAlign = TextAlign.Center
                )
            }
```

Plug the scaled sizes in like this, and the design keeps the proportions I wanted on a watch of any size:

![](images/05.webp){width=70 align=center}

## Takeaways

Working through this, I realized that a 4mm difference between watches works out to roughly a 10% difference in size, so supporting different devices really isn't optional. I also think **turning the bezel, a physical part of the device, into a design element** is a useful trick. Of course, if a watch with a much thinner bezel comes out soon, a design built around the bezel will fall apart. But I can detect the user's device and branch to a different design, so I can work around that to some extent. (It does mean shipping an update every time a new watch comes out...)

I also read through the [Wear OS accessibility docs](https://developer.android.com/training/wearables/accessibility?hl=ko). Much like on mobile, they cover TalkBack support, content descriptions, minimum touch target sizes, and so on. In my app, the only thing I really need to handle is font size changes, and there I plan to stick with fixed sizes to protect the design. I could truncate with overflow or let the text wrap onto more lines, but on a watch specifically, I think design matters far more than accessibility. That's just my personal take, though, and before release I plan to test font sizes with things like `autoSizeMaxTextSize` to make the app as accessible as I can.
