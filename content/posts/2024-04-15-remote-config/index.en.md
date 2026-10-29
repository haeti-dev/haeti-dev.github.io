---
title: Using Firebase Remote Config to show a maintenance dialog
summary: How I used Firebase Remote Config to show a server maintenance dialog that backend developers can turn on and reword without an app update.
source: 81597cc194c8
---

While working on a side project called \[Santa Manitto\] (manitto is the Korean take on Secret Santa), I picked up an issue: show a popup while the server is under maintenance. On Android that popup is a Dialog, and the backend developers needed to be able to change its content text on the fly.

![](images/01.webp){width=100 align=center}

The way I'd done this before was to call an API on entering SplashActivity, check whether the server was under maintenance, and act on the response. The Santa Manitto team, though, manages maintenance through Firebase Remote Config, so I dug into how it works.

## What is Firebase Remote Config?

> Firebase Remote Config is a cloud service that lets you change configuration values for your users to change how your app behaves and looks. You can use it to give different people different experiences, or to set up test groups and try out new features in your app. And when you release a new version of your app, you can use Remote Config to keep supporting older versions or to turn off specific features.
>
> \- Source: [https://firebase.google.com/docs/remote-config?hl=ko](https://firebase.google.com/docs/remote-config?hl=ko)

In other words, you can change what users see without shipping an app update. It has almost no impact on performance, and you decide exactly when changes take effect, which makes it ideal when you're short on developer time.

With Firebase Remote Config, you can:

- Change simple parameter values (layouts, themes, strings, JSON, booleans)
- Tailor settings to specific segments
- Optimize custom events with machine learning

Here's how each of these could be useful.

### Changing simple parameter values

I'll use the issue I was assigned on Santa Manitto, showing a popup during server maintenance, as the example.

In Firebase Remote Config, you can create parameters that hold strings, numbers, booleans, or JSON, as shown above. For this issue, that means one boolean parameter that says whether the server is under maintenance, and one string parameter for the text the popup should show.

When the app starts, it reads the maintenance flag. If it's true, it pulls the text from the string parameter and puts it in the popup.

Beyond that, it looks like you could use JSON and boolean parameters to tailor more complex data to each user depending on the situation.

### Tailoring settings to specific segments

[Remote Config parameters and conditions  |  Firebase Remote Config](https://firebase.google.com/docs/remote-config/parameters?hl=ko&authuser=0&_gl=1*16e4wsr*_ga*MTE2Njg4NTEyOC4xNzExODY4OTI2*_ga_CW55HF8NVT*MTcxMzE1MTUzMi4xMS4xLjE3MTMxNTQwMDguNjAuMC4w#conditions_rules_and_conditional_values)

You can serve different values to a specific group of users (a segment). For example, you can ship different strings per country, or push an urgent update only to Android users.

Before rolling a change out to everyone, you can also run an A/B test to see how it performs. An [**A/B test**](https://firebase.google.com/docs/ab-testing/abtest-config?hl=ko&_gl=1*pfzswz*_up*MQ..*_ga*NTMwOTUwODMxLjE3MTMxNjAzNjM.*_ga_CW55HF8NVT*MTcxMzE2MDM2My4xLjAuMTcxMzE2MDM2My4wLjAuMA..) is a statistical method that compares two or more variants and picks the one that works best. It's widely used to test and improve the user experience of websites and apps, for example to decide which button color gets more clicks or which homepage layout drives more engagement. The appeal is that you make decisions based on real data instead of guessing how users will behave.

In Firebase Remote Config, the Conditions tab lets you define a user segment and attach conditions to a parameter.

![](images/02.webp){width=100 align=center}

![](images/03.webp){width=100 align=center}

### Optimizing custom events with machine learning

![](images/04.webp){width=100 align=center}

According to the official docs, parameter personalization is like running a permanent A/B test that's automatically individualized and keeps improving over time. It requires Google Analytics, and it comes with these built-in metrics:

- User engagement time, which optimizes for how long users stay engaged
- Ad clicks, which optimizes for the total number of ad click events
- Ad impressions, which optimizes for the number of ad impressions

Personalization uses a machine learning algorithm to decide the best experience for each user. The docs say the algorithm balances two things: learning the best experience for different types of users, and using that knowledge to maximize the target metric.

#### Personalization vs. A/B testing

An A/B test is designed to find the single best-performing experience. Personalization, on the other hand, dynamically picks the best experience for each individual user to maximize the goal. Personalization gives the best results for most kinds of problems, but the docs recommend A/B testing for cases like these:

![](images/05.webp){width=100 align=center}

Which approach to use depends on the situation and on what your analytics show. There seem to be endless ways to use this, and I plan to try it in a lot of different places. Parameters are quick and easy to set up, so for developers who are short on resources, it's about as good as it gets.

To wrap up, here's how I built the maintenance popup on Android with Firebase Remote Config.

## How I built it

#### Adding the dependencies (build.gradle)

![](images/06.webp){width=100 align=center}

![](images/07.webp){width=100 align=center}

#### Creating the Remote Config instance as a singleton

Next, I need an object that fetches values from Firebase Remote Config. It has to be a singleton (so the whole app sees consistent values, and to avoid performance overhead), so I created it in a Kotlin companion object.

![](images/08.webp){width=100 align=center}

`minimumFetchIntervalInSeconds` sets the minimum interval between fetches. I set it to 0 so any change in Remote Config is fetched right away. If you fetch at a short interval like this too often, the server throttles you, but this issue only needs one fetch when the app starts, so 0 was fine. I also applied these settings asynchronously with `setConfigSettingsAsync`.

#### Fetching values from Remote Config and storing them

In the ViewModel, I used the backing property pattern, which separates a MutableStateFlow from a read-only StateFlow, to create the values the activity collects.

On the singleton Remote Config instance, I call `fetchAndActivate()` to fetch the config from the server and activate it, and `addOnCompleteListener` adds a callback that runs when that async work finishes. If it succeeds, the parameter values published in Firebase Remote Config get stored in the MutableStateFlows I created earlier.

#### Collecting the values in the activity and showing the dialog

![](images/09.webp){width=100 align=center}

The activity collects the ViewModel's StateFlows, which reflect the Remote Config values. If the server is under maintenance (remoteServerCheck is true), it sets the message published through Remote Config as the dialog's content text and shows the dialog.

So that the other values the activity observes can't interfere with the dialog, I added an `isDialogShown` flag and branch on it. I also used `commitAllowingStateLoss()` so the dialog can still be shown safely after the activity's state has been saved.

With this in place, when a developer changes the Remote Config value in Firebase, which is to say starts server maintenance, every user sees the maintenance dialog and can't use the app. It reacts very quickly, and as I mentioned earlier, you can set Remote Config parameters differently for each segment, which makes it really useful.
