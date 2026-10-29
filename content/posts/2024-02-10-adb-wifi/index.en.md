---
title: Getting the ADB Wi-Fi plugin to find adb
summary: The ADB Wi-Fi plugin said adb wasn't found; the fix was the adb path in the plugin's own settings, not my PATH.
source: e1021b7fcac0
---

## The problem

I wanted to debug wirelessly over Wi-Fi without the hassle, using this plugin:

[ADB Wi-Fi - IntelliJ IDEs Plugin | Marketplace](https://plugins.jetbrains.com/plugin/14969-adb-wi-fi)

> **Getting Started**

1. Connect an Android device via USB cable.
2. Open the "ADB Wi-Fi" Tool Window (in the right-bottom corner).
3. Click the "Connect" button.
4. After a successful connection, you can unplug the USB cable.

I followed those steps, but when I opened the ADB Wi-Fi tool window, my device didn't show up. All I got was this message:

```kotlin
adb executable not found at the specified path
```

So I did a lot of googling.

From what I found, there seemed to be three possible causes:

1. ADB isn't installed.
2. ADB isn't on the `Path` environment variable.
3. The path where adb is installed isn't being picked up.

## Tracking it down

I use the emulator all the time, so there was no way ADB wasn't installed. I ruled out the first one, looked up how to fix the second, and tried it.

![](images/01.webp){width=100 align=center}

I edited the environment variable, restarted, and tried again. Same error.

![](images/02.webp){width=100 align=center}

It turned out the adb path in the plugin's own settings was wrong. Once I pointed it at the right location, the plugin worked.

![](images/03.webp){width=90 align=center}

Now, as long as my device is on the same Wi-Fi network, I can connect to it wirelessly without plugging anything in!
