---
title: Adding a Baseline Profile to the Droid Knights app
summary: My first open source contribution: adding a Baseline Profile to the Droid Knights conference app and measuring what it did to startup time.
source: 51350ea5baac
---

![](images/01.webp){width=100 align=center}

Droid Knights, Korea's largest Android conference, recently put out a call for contributors to its app. I'd been following the project closely for a while and wanted some open source experience, so I went through the open issues and picked the one I most wanted to work on.

[https://github.com/droidknights/DroidKnightsApp/issues/299](https://github.com/droidknights/DroidKnightsApp/issues/299)

![](images/02.webp){width=100 align=center}

I'd heard about Baseline Profiles plenty of times but never seen how they're set up or how much they actually help, so I took this one. I'd never written test code before and was nervous about it, but I got some advice that it was worth a shot, so I went for it.

## What's a Baseline Profile?

After an app is installed or updated, its code runs in interpreted mode on the first launch until it gets [JIT](https://source.android.com/devices/tech/dalvik/jit-compiler)-compiled. The Java and Kotlin code in an APK is compiled to dex bytecode, but (since Android 6) it isn't fully compiled to machine code, because storing and loading a fully compiled app is expensive. The classes and methods the app uses often, along with the ones it uses at startup, get recorded in a profile file. When the device goes idle, ART compiles the app based on those profiles, so the next launch is faster.

If you ship a Baseline Profile with your app or library, the [Android Runtime (ART)](https://source.android.com/docs/core/runtime?hl=ko) uses Ahead-Of-Time (AOT) compilation to optimize the code paths it lists, so every new user and every app update gets the faster version. This profile-guided optimization (PGO) lets an app optimize its startup, cut interaction jank, and improve overall runtime performance from the very first launch.

That's straight from the official docs. In short, a Baseline Profile hands ART metadata about your critical code paths so they're AOT-compiled first, which **makes code run 30% faster from the first launch**.

With a Baseline Profile, every user interaction (app startup, navigating between screens, scrolling content) is smoother from the first run. A faster, more responsive app can in turn mean more daily active users and a higher return rate.

![](images/03.webp){width=100 align=center}

The Droid Knights app has a min SDK of 28, so it falls into the bottom row of that table (Android 9 and up: Baseline Profile plus Cloud Profiles). Since the profile already ships with the release, users get the baseline optimizations even when the Play Store doesn't have a Cloud Profile for the app yet.

### Adding the module and dependencies

![](images/04.webp){width=100 align=center}

First I created a baselineprofile module with the **Baseline Profile Generator** template. To my surprise, Android Studio added the dependency between the app module and the new baselineprofile module in version catalog form, and it also generated a basic `BaselineProfileGenerator` and `StartupBenchmarks` for me.

![](images/05.webp){width=100 align=center}

Next I cleaned up the entries in `libs.versions.toml` to follow the project's existing conventions, then added the profileinstaller and baselineprofile dependencies and plugin to the app module.

### Adding a build type

![](images/06.webp){width=100 align=center}

I also added a benchmark build variant and configured it. I didn't turn on obfuscation for it.

### Setting up the test device

![](images/07.webp){width=100 align=center}

Then I added the test emulator config to the baseline profile module. I went with API 34 to measure performance on the newest version, but a physical device would give you more accurate numbers.

### Generating the Baseline Profile

![](images/08.webp){width=100 align=center}

![](images/09.webp){width=100 align=center}

I didn't write any custom scenarios and only measured app startup, using the default classes the `Baseline Profile Generator` created.

Then I generated the Baseline Profile from the terminal:

`./gradlew :app:generateBaselineProfile`

![](images/10.webp){width=100 align=center}

That produced two txt files under `app/src/release/generated/baselineProfiles`, as shown above. A Baseline Profile lists the classes and methods relevant to an app or library, and the Android runtime uses that information to optimize execution.

### Running the benchmark

![](images/11.webp){width=100 align=center}

Finally, I hit the run button to the left of the `StartupBenchmarks` class to start the test.

![](images/12.webp){width=100 align=center}

```
StartupBenchmarks_startupCompilationBaselineProfiles 
timeToInitialDisplayMs min 957.5, median 1,128.3, max 1,780.6 
Traces: Iteration 0 1 2 3 4 5 6 7 8 9 

StartupBenchmarks_startupCompilationNone 
timeToInitialDisplayMs min 1,003.5, median 1,176.3, max 1,788.9 
Traces: Iteration 0 1 2 3 4 5 6 7 8 9
```

The first run showed almost no improvement. I'd read that a 12% to 20% speedup is typical, so I ran it again.

![](images/13.webp){width=100 align=center}

```
StartupBenchmarks_startupCompilationBaselineProfiles
timeToInitialDisplayMs   min   948.7,   median 1,036.8,   max 1,166.4
Traces: Iteration 0 1 2 3 4 5 6 7 8 9

StartupBenchmarks_startupCompilationNone
timeToInitialDisplayMs   min 1,257.4,   median 1,574.7,   max 2,295.3
Traces: Iteration 0 1 2 3 4 5 6 7 8 9
```

This time startup was about 35% faster on average. That's a meaningful cut in how long users wait for the app to load, and it can affect library compile speed too.

#### Pull request

[https://github.com/droidknights/DroidKnightsApp/pull/318](https://github.com/droidknights/DroidKnightsApp/pull/318)

## What's next, and what I took away

The Droid Knights app is full of scrolling and screen transitions, so I'd like to write scenarios for those and measure them. I'd also like to try automating Baseline Profile generation for each release with GitHub Actions.

It's a pretty heavy app, so builds took a long time. The main branch didn't build, so I patched a bunch of files locally just to get a build going, and then hit an error about `"/META-INF/LICENSE.md"` on top of that. I was really happy when that first build finally went through.

This was my first open source contribution, on a project a lot of people are watching, and a chance to collaborate a little with some impressive people, so I went about it extra carefully. I'm cautious by nature, so it took me a long time, and fixing the build wore me out. Adding a feature I'd never built before (Baseline Profiles) inside a multi-module, build-logic setup I'm still not used to was hard, but I think that's exactly why it'll stay with me as a solid, valuable experience.

From here on I want to contribute to open source more, and more often, for the motivation and for the experience.

### References

[https://codelabs.developers.google.com/android-baseline-profiles-improve#0](https://codelabs.developers.google.com/android-baseline-profiles-improve#0)

[https://github.com/skydoves/pokedex-compose](https://github.com/skydoves/pokedex-compose)

[https://developer.android.com/topic/performance/baselineprofiles/overview](https://developer.android.com/topic/performance/baselineprofiles/overview)
