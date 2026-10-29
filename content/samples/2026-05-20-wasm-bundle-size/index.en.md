---
title: Why is my Kotlin/Wasm bundle 12MB?
summary: I took the bundle apart, from skiko.wasm to the app wasm and the fonts, to see what can shrink and what can't.
source: 1c22295ef040
---

## What's in the bundle

Open up a production build and you'll find three chunks: skiko (the rendering engine), the app code, and the fonts.

| File | Size |
|---|---|
| skiko.wasm | 8.2MB |
| app.wasm | 3.7MB |

## What you can shrink

Fonts can be subset. Keep only the 2,350 common Hangul syllables and each font file drops under 500KB.

## What you can't

skiko is the drawing engine itself, so there's no way to drop it or split it up. Give it long cache headers, though, and repeat visits get it almost for free.
