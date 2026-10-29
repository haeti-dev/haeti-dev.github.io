---
title: Thinking through MVI for Jetpack Compose
summary: What MVI buys you in Compose, how Redux, SwiftUI, and TCA handle state, and what I learned building MVI without a library.
source: c623c67eca67
---

I'd already built MVI screens with the [Orbit library](https://github.com/orbit-mvi/orbit-mvi). Then I tried to build MVI without any library, and that raised a lot of questions. This post is me working through them, in three parts. Fair warning: there's a lot of personal opinion in here.

1. MVVM or MVI: which one suits Compose, and how do their strengths, weaknesses, and differences compare?
2. How do SwiftUI and React manage state, and what do they have in common with MVI? (Feat. TCA)
3. What's the cleanest way to implement MVI on Android without a library?

I wrote this while studying MVI to understand it better, so again, some of it may not be accurate. The React and Swift parts in particular come from a fairly quick look, so there may be plenty of mistakes 😢

## 1. MVVM, MVI, and where Compose fits

#### What MVVM and MVI have in common

First, MVVM and MVI are both architecture patterns for separating the **presentation layer**. They don't define the architecture of the whole app. They focus on **managing the UI side** and **separating business logic from UI rendering logic**. Using them makes code **easier to maintain**, **easier to test**, and **easier to read**.

As the names suggest, both patterns include a **Model** and a **View**.

- **Model**: owns the app's data and business logic.
- **View**: owns the UI the user sees and renders state on screen.

Where they differ a lot is in **how they manage state** and **how data flows**.

#### MVVM (Model - View - ViewModel)

In MVVM, the View and the ViewModel interact with each other. The **ViewModel** provides the data the **View** needs, takes user input, and updates the **Model**. **Data binding** is often what connects the **ViewModel** and the **View**.

![](images/01.webp){width=100 align=center}

As the diagram shows, the View and the ViewModel are tightly coupled: changes in the View are reflected in the ViewModel, and changes in the ViewModel show up in the View automatically.

I loved data binding when I first got into Android. A little code was enough to update the UI, and with a BindingAdapter I could layer more complex logic on top. But the more I learned and the more varied my code got, the more complicated the UI state became, and the more often screens ended up in states I never intended.

I also once joined a project partway through, after the initial build was done, to maintain it and develop new features. The code that updated the UI was spread across the ViewModel, XML layout files, BindingAdapters, custom views, and more. It was complicated and hard to read, and maintaining it was really painful.

What got me out of that was a pattern with **Unidirectional Data Flow** (UDF).

![](images/02.webp){width=100 align=center}

Whether you use Compose or XML, this approach depends on the observer pattern. In Compose, you collect the data stream with `collectAsState()`, which literally collects it and turns it into State. In XML, you collect it the same way. There are lifecycle pitfalls to watch for, but all of them have workarounds.

I used this approach a lot. My personal preference was to get UDF by defining state through a UiState interface made covariant with the `out` keyword, which widens type compatibility (it keeps the data from being consumed).

But the project grew, and I was working with quite a few developers. I started to worry that event handling wasn't consistent and that nothing really controlled how state changed. Compose asks you to manage state even more carefully, and I kept wondering: what if a small human error does serious damage to the app?

As far as I know, there are plenty of ways to address that worry. Here I'll look at one of them: MVI.

#### MVI (Model - View - Intent)

The **MVI** pattern has these components:

- **Model**: represents the data and business logic.
- **View**: renders the UI based on state.
- **Intent**: represents a user action or event.

MVI emphasizes **unidirectional data flow** and **immutable state**, and data flows like this:

![](images/03.webp){width=100 align=center}

1. **Intent**: A user action is captured as an Intent.
2. **ViewModel** (optional): Handles the Intent and does the work needed to update the Model. Personally, I think the Intent can be handled even without a ViewModel at this step.
3. **Model**: Produces a new immutable State from the updated state.
4. **View**: Observes the new State and updates the UI.

As the flow shows, MVI is reactive and follows the core idea of MVC. The Intent observes the user, the Model observes the Intent, the View observes the Model, and the user observes the View, which makes it highly reactive. Each component is expressed as a referentially transparent function over streams, and the View and the Intent each bridge the gap between the user and the Model in one direction, which matches what MVC originally set out to do.

Those traits give MVI these advantages:

1. **It enforces UDF**: People often say MVI enforces it, but I don't fully agree. I'll come back to this at the end of the post.
2. **Immutable state**: All state is an immutable object, so state changes are easy to track and side effects stay to a minimum.
3. **One way to handle events**: Every event is wrapped in an **Intent**, so event handling stays consistent.
4. **It fits declarative UI**: Compose is a declarative UI framework that favors UDF. MVI **makes state management simpler and predictable**, which suits Compose, where the UI is rebuilt from state.

Looking only at that list, there's no reason not to use it with Compose. But it has downsides too:

- **More boilerplate**: You define State, Intent, SideEffect, and so on for every screen, so there's more code.
- **Complexity**: It's overkill for simple apps and can add complexity you don't need.
- **Possible performance overhead**: It always creates new state objects, which could cause performance problems. (I haven't run into this myself yet.)
- **Learning curve**: I personally don't agree with this one. Learning it through a library, I picked it up faster than MVVM. (I'll get to libraries in part 3.)

MVI suited me, so I used it a lot. But the more conferences I went to and the more I built, the more one question nagged at me:

> Compose is the latecomer among declarative UI frameworks. How does everyone else do it?

Going to conferences often, I kept hearing that the way Compose manages state is something other ecosystems have abandoned or rarely use. I wanted to know whether that was true, and if so, how those other ecosystems handle state, so I studied up.

## 2. How do the older declarative UI frameworks manage state?

#### **The rise of declarative UI**

Traditional imperative UI programming made developers spell out, step by step, how to draw the UI. As UIs got more complex and had more state to manage, that approach hit its limits on maintainability and readability.

That's where the **declarative UI** paradigm came in. Declarative UI focuses on "**what to draw**" rather than how, and builds the UI declaratively from **state**. Developers design things so that the UI updates automatically when state changes, which cuts complexity and makes code easier to maintain. But because state decides the UI, managing state efficiently became much more important, and most of the approaches in this space appeared in the 2010s.

### **1. Redux**

![](images/04.webp){width=100 align=center}

**Redux** is a state management library widely used in JavaScript apps, especially with declarative UI libraries like **React**. Redux manages the app's state centrally and handles state changes in a consistent way, which makes code easier to read and maintain.

#### **Redux's core principles**

1. **SSOT (Single Source of Truth)**

   The entire app state lives as an object tree in a single **Store**. That keeps state management consistent and state easy to track.
2. **State is read-only**

   You can't modify state directly. To change it, you dispatch an **Action**. That preserves immutability and makes state changes more predictable.
3. **Changes happen only through pure functions (reducers)**

   State changes go through a **Reducer**, which is a pure function. A reducer takes the previous state and an action and returns a new state, managing state without side effects.

#### **How data flows in Redux**

> Data in Redux flows in **one direction**.

1. **Create an action**

   User input and external events are represented as action objects.

   ```jsx
   const incrementAction = { type: 'INCREMENT' }
   ```
2. **Dispatch**

   The action is dispatched to the Store and passed to the reducer.

   ```jsx
   store.dispatch(incrementAction);
   ```
3. **Reducer**

   The reducer takes the current state and the action and returns a new state.

   ```jsx
   function counterReducer(state = 0, action) {
     switch (action.type) {
       case 'INCREMENT':
         return state + 1;
       case 'DECREMENT':
         return state - 1;
       default:
         return state;
     }
   }
   ```
4. **The Store updates**

   The new state is saved in the Store, and the Store notifies the views subscribed to state changes.
5. **The View updates**

   The view updates the UI from the new state.

#### **What Redux does well**

- **Predictable state management**: Unidirectional data flow and immutability make state changes predictable.
- **Easy debugging and testing**: State and actions are handled by pure functions, so debugging is reproducible and unit testing is straightforward.
- **Extensibility**: Middleware handles async work and side effects, so it extends well.

#### **Where Redux falls short**

- **More boilerplate**: Actions, reducers, and store setup can make the initial setup complicated.
- **Overkill for small apps**: In a simple project, it can add more complexity than it removes.

As far as I know, MVI was inspired by Redux. Those strengths and weaknesses are strikingly similar to MVI's. I think a ViewModel could play roughly the same role as a Redux reducer.

### **2. SwiftUI**

![](images/05.webp){width=100 align=center}

#### **What SwiftUI is**

**SwiftUI** is Apple's declarative UI framework, where you write UI declaratively in Swift. The UI updates automatically based on state, and it runs on iOS, macOS, watchOS, and other platforms.

#### **How SwiftUI manages state**

SwiftUI manages state through a set of property wrappers.

#### **1**. `@State`

- **Use**: Managing state inside a View.
- **Traits**: It's a value type and isn't exposed outside the View.

```swift
struct CounterView: View {
    @State private var count: Int = 0

    var body: some View {
        VStack {
            Text("Count: \\(count)")
            Button("Increment") {
                count += 1
            }
        }
    }
}
```

#### **2**. `@Binding`

- **Use**: Letting a child View modify state it received from its parent View.
- **Traits**: Stays in sync with the parent View's state.

```swift
struct ParentView: View {
    @State private var count: Int = 0

    var body: some View {
        ChildView(count: $count)
    }
}

struct ChildView: View {
    @Binding var count: Int

    var body: some View {
        Button("Increment") {
            count += 1
        }
    }
}
```

#### **3**. `@ObservedObject` **and** `@Published`

- **Use**: Observing a class-type object and updating the View when its state changes.
- **Traits**: Mark properties inside the object with `@Published` to announce state changes.

```swift
class CounterModel: ObservableObject {
    @Published var count: Int = 0
}

struct CounterView: View {
    @ObservedObject var model = CounterModel()

    var body: some View {
        VStack {
            Text("Count: \\(model.count)")
            Button("Increment") {
                model.count += 1
            }
        }
    }
}
```

#### **4**. `@EnvironmentObject`

- **Use**: Managing an object shared across the whole app.
- **Traits**: Passed down the View hierarchy automatically.

```swift
class UserSettings: ObservableObject {
    @Published var username: String = ""
}

struct ContentView: View {
    @EnvironmentObject var settings: UserSettings

    var body: some View {
        TextField("Username", text: $settings.username)
    }
}
```

#### **Where SwiftUI's state management falls short**

- **Scattered state**: In a large app, state management can spread across many places and get complicated.
- **Hard to handle complex state logic**: There's no structured way to handle complex state transitions or async work.
- **Hard to test**: The View and its state are tightly coupled, which can make unit tests hard to write.

### **3. The Composable Architecture (TCA)**

#### **What TCA is**

The Composable Architecture (TCA) is an open source app architecture for Swift and SwiftUI, built by Point-Free. It's based on Redux's ideas and gives you a structured way to handle complex state management and side effects.

#### **TCA's core building blocks**

1. **State**
   - A struct that represents the app's state.
   - It stays immutable, and state changes go through the reducer.

   ```swift
   struct AppState: Equatable {
       var count: Int = 0
   }
   ```
2. **Action**
   - An enum that represents the events that change state.
   - It models user input and external events.

   ```swift
   enum AppAction: Equatable {
       case increment
       case decrement
   }
   ```
3. **Environment**
   - Defines how the app talks to external systems.
   - Handles async work like API clients, database access, and timers.

   ```swift
   struct AppEnvironment {
       var mainQueue: AnySchedulerOf<DispatchQueue>
   }
   ```
4. **Reducer**
   - A pure function that takes an action, the current state, and the environment, and returns a new state and effects.
   - All the state-changing logic lives here.

   ```swift
   let appReducer = Reducer<AppState, AppAction, AppEnvironment> { state, action, environment in
       switch action {
       case .increment:
           state.count += 1
           return .none
       case .decrement:
           state.count -= 1
           return .none
       }
   }
   ```
5. **Store**
   - Holds the state and the reducer, and connects to the View.
   - The View subscribes to state and sends actions through the Store.

   ```swift
   let store = Store(
       initialState: AppState(),
       reducer: appReducer,
       environment: AppEnvironment(mainQueue: DispatchQueue.main.eraseToAnyScheduler())
   )
   ```

#### **How data flows in TCA**

1. **View**
   - Subscribes to state and renders the UI.
   - Turns user input and events into actions and sends them to the Store.
2. **Action**
   - Events are represented as actions.
3. **Reducer**
   - Handles the action, produces a new state, and returns any effects needed.
4. **Effect**
   - Handles async work and side effects.
   - When it finishes, it dispatches more actions to the Store.
5. **State**
   - The new state is saved in the Store, and the View observes it and updates the UI.

#### **What TCA does well**

- **Modularity**: State, actions, and reducers are modular, which makes them reusable and easier to maintain.
- **Better testability**: The reducer is a pure function, so state-change logic is easy to test.
- **Side effect management**: Effects keep async work and side effects clearly separated.

#### **Where TCA falls short**

- **Up-front complexity**: The setup and structure are complex, which can be overkill for small projects.
- **Learning curve and instability**: I've heard that almost all TCA learning material is paid. I'm also worried that TCA, built on composition, could end up eating a lot of resources as Swift keeps changing so quickly.

That's a quick tour of three approaches to state management. Redux and TCA in particular are very close to the MVI I covered at the top of this post. The most important keywords they share are **immutability, reducers, UDF, consistent event handling, and side effects**. Looking at Redux middleware and TCA's Effect, I started to think Compose could also keep side effects clearly separated and managed.

Now let's look at how to implement MVI on Android.

## 3. How do you implement MVI on Android without a library?

![](images/06.webp){width=60 align=center caption="Orbit-MVI"}

I first met MVI through the Orbit library. My first impression was that it looked hard and the amount of code was intimidating, but it turned out to be easier to use and more consistent than I expected, and I had a lot of fun writing code with it.

But then I wondered: should a company make its architecture depend on a library? Teams often build their own design systems to reduce their dependency on Material (granted, there are plenty of other reasons for that too). Tying the architecture to a specific library felt like a bigger risk than the benefits the library brings.

Around then I started a new project. I thought: if I design it so the team follows **one consistent way of managing state**, even teammates who don't know MVI well could get all of MVI's benefits with a low learning curve, and the project could move faster. So I wrote these base types in the core:ui module:

```kotlin
interface UiIntent
interface UiState
interface SideEffect
```

- **UiIntent**: An interface that represents user events and actions.
- **UiState**: An interface that represents UI state and stays immutable.
- **SideEffect**: An interface that represents one-off events like navigation and notifications.

With these interfaces, I wanted to keep the shared logic in one place and get type safety. I also wanted to follow the **DIP** (Dependency Inversion Principle): the higher-level module doesn't depend on the lower-level ones (each screen's State, Intent, and SideEffect), only on the abstract interfaces.

![](images/07.webp){width=100 align=center}

- **State management**: State lives in a `MutableStateFlow` and is exposed as a `StateFlow`, which keeps it immutable from the outside.
- **Side effect management**: One-off events go through a `Channel`.
- **Access modifiers**: `intent`, `postSideEffect`, and `currentState` are `protected`, so nothing outside the ViewModel can change state. (I gave them the same names as in Orbit.)
- **Mandatory onIntent**: Every ViewModel has to implement `onIntent`, which keeps event handling consistent.

I went back and forth a lot between Channel and SharedFlow, thinking about lost events. I still haven't found the answer. The goal was to prevent lost events as simply as possible, but I got a bit confused about what "losing an event" even means, so I plan to sort this out in a later post. (Can you fully prevent lost events in the first place? And when the user has left the screen, maybe skipping the side effect isn't a UX problem at all..)

Even so, here's why I went with Channel:

- **One-off delivery**: A Channel buffers events even when there's no receiver, so they aren't lost.
- **Backpressure**: When the sender can't keep up with the receiver's processing speed, a Channel suspends and applies backpressure.
- **Single subscriber**: SharedFlow supports multiple subscribers, but I designed each ViewModel to have a 1:1 relationship with its screen, so I only need one.

Let's walk through a simple example of how to use this MVI setup.

#### 1. Define the State, Intent, and SideEffect

![](images/08.webp){width=100 align=center}

Start by defining the state the UI holds, the Intents the user can trigger, and the SideEffects those can cause. Right now the app only has a simple feature: two buttons that increment and decrement a number.

#### 2. Apply the reducer and handle intents

![](images/09.webp){width=100 align=center}

The ViewModel extends the BaseViewModel from earlier. It then overrides `onIntent`, which forces developers to change state only through that function. For each Intent, it calls the `intent` function defined in BaseViewModel to run the reducer, and it emits events through `postSideEffect`.

After that, the UI just collects the data streams and handles events appropriately.

That's MVI without a library like Orbit or Mavericks. There's still unavoidable boilerplate, but you get all the benefits of a consistent code style and UDF. `update` guards against multithreading issues to some extent, but it doesn't handle them as thoroughly as a library does. That's where a library clearly wins on safety.

## Looking back

Writing this post left me pretty confused about architecture. In part 1, I mentioned that MVI is said to enforce UDF and said I didn't agree. I made `onIntent` an override in BaseViewModel and used access control so nothing else can modify state, but human error can still break the pattern.

So, setting MVI and MVVM aside: if human error or some other issue sneaks code into a project that doesn't follow its architecture pattern, can you still say that project or module follows that architecture? Is there any way to actually enforce UDF? In the end, I think the best you can do is guard against it as much as possible with access control or inheritance, plus code review and static analysis tools.
