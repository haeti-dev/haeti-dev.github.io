---
title: What happens when this function fails? Kotlin's Rich Errors
summary: My KotlinConf Extended talk, expanded: why Kotlin developers spent 15 years hand-rolling failure types, and what the proposed Rich Errors fix and leave open.
source: a9a58ae9fe78
---

## The question behind the talk

In August I gave a talk at KotlinConf Extended South Korea 2026 called "What happens when this function fails?" It was 40 minutes on how Kotlin has expressed failure over the last 15 years, and on which problems Rich Errors, the design currently in the works, is trying to solve.

The syntax wasn't what I spent the most time on while preparing. After a few years of Kotlin, you notice that even a single project handles errors in several different ways. Some functions return `null`, others throw. Another module uses `Result`, and the UI wraps it all in a sealed class again. Each choice has its reasons, but as a caller you have to check "how does this function tell me it failed?" every time you call something. I've done it too. So when I first saw Rich Errors, I cared less about what the new symbols looked like and more about how much of what we keep building by hand would finally move into the language.

This post follows the talk, but it goes much deeper into the background and the code I had to rush through on stage. I've filled in the context so you can follow along even if you didn't see the talk, or if terms like KEEP, checked exceptions, and Arrow are new to you. At the end, I also read an error-handling post that went up on the JetBrains Kotlin blog a few days after the talk. It uses different tools from mine but points in the same direction, so it confirmed what I wanted to say from a different angle.

> This post is based on the KEEP documents as of September 6, 2026. KEEP-0462, the Rich Errors design spec, is still in `Public discussion`, and the `error`, `|`, `|.`, and `ifError` you'll see here are all proposed syntax. This isn't a tutorial that compiles on current stable Kotlin. Where the screenshots from the talk differ from what I checked now, I call it out in the text.

### What's a KEEP?

Before getting into it, one term that'll come up a lot. KEEP stands for Kotlin Evolution and Enhancement Process. It plays the same role as Java's JEPs and Python's PEPs: for new syntax to land in Kotlin, someone writes a public design document, and it goes through community review in GitHub Discussions. The documents and the discussions are all public, so anyone can read them and comment. Most of the quotes in this post come from there.

There are three documents about Rich Errors:

- [KEEP-0441](https://github.com/Kotlin/KEEP/blob/main/proposals/KEEP-0441-rich-errors-motivation.md) (August 2025): the motivation document, explaining why it's needed
- [KEEP-0462](https://github.com/Kotlin/KEEP/blob/main/proposals/KEEP-0462-rich-errors.md) (first published May 2026, last updated July 31): the design spec, defining the syntax and typing rules
- [notes/0009](https://github.com/Kotlin/KEEP/blob/main/notes/0009-rich-errors-use-cases.md) (August 2026): a validation document that tries it on the code of three real Android apps

Keep the motivation document and the design spec apart when you read them. The type hierarchy and the operators changed between the 2025 talk and motivation document and the 2026 design spec. This post follows the latest spec, KEEP-0462.

![KEEP-0462 metadata](images/01.webp){width=100 align=center caption="KEEP-0462's metadata, captured at the time of the talk. The Status on the bottom line is Public discussion, which is the very first step in the status sequence the KEEP repo defines. Not In progress, and not Experimental."}

## One null, two failures

I opened the talk with this code. If you use coroutines, it'll look familiar.

![withTimeoutOrNull code](images/02.webp){width=100 align=center}

It fetches a user, but gives up and returns `null` if that takes longer than a second. You run it and get `null`. What happened?

This is where I went quiet on stage for about three seconds. There are two possible answers:

1. `findUser` didn't find the user and returned `null`.
2. It didn't finish within a second, so `withTimeoutOrNull` returned `null`.

Say the lookup function itself looks like this. It returns `null` when there's no such user. For a contract that simple, `User?` seems like plenty.

![loadUser signature](images/03.webp){width=100 align=center}

But the moment the caller adds a timeout, things change.

![withTimeoutOrNull call](images/04.webp){width=100 align=center}

[withTimeoutOrNull](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/with-timeout-or-null.html) returns the block's result if it finishes in time, and `null` if the timeout cancels it. The types make this clearer. The function returns `T?`, and here `T` is already `User?`. You'd expect `User??`, but nullable types in Kotlin don't nest: add another `?` to `User?` and you still have `User?`. Two kinds of "nothing" collapse into one type.

The first is a confirmed absence; the second is a failure to check at all. In an Android app, one is an empty "User not found" screen and the other is a retry prompt. On a server, it's 404 versus 504. Same `null`, completely different next step. And that difference was already gone the moment both results merged into a single `null`. No amount of careful `if (user == null)` checks can bring back information that no longer exists.

One caveat. `withTimeoutOrNull` doesn't turn every failure into `null`. Other exceptions thrown inside the block still propagate, and cancellation of the outer coroutine is a separate matter. What I'm after here is the ambiguity you get when two normal return paths are represented by the same value.

By now you're probably thinking, "Just use a sealed class." You're right. That works, and I've done it plenty. But let me change the question: how many of those hierarchies are in your codebase? Didn't you make one per screen, one per API? The first half of this post is about why, for 15 years, we've each had to build that ourselves.

These days, this kind of code also arrives by a different route. The `suspend fun loadUser(id: String): User?` you got from an AI works, and the tests pass. But nothing anywhere says what that `null` means. In the past, someone would ask in code review: "What's this null?" Now we don't read AI-written code line by line like that. I'll come back to this later in the post.

## Why has everyone built their own failure types for 15 years?

Kotlin was announced in 2011, and 1.0 shipped in 2016. That makes it 15 years old this year. Here's what happened with error handling over those 15 years, on a single line:

![15-year timeline](images/05.webp){width=100 align=center caption="The community built Arrow in 2017, Result landed in the standard library in 2018, and sealed interfaces didn't arrive until 2021. The two on the far right are what this post is about."}

Don't read this as a story of old tools being retired and replaced by new ones. All five approaches are still in use, often side by side in the same project. And the explanations below don't follow release order. They follow how each approach tried to fill the gap the previous one left: exceptions, null, sealed, Result, Arrow. That's also the order KEEP-0441 uses to lay out the problem.

### The first decision: dropping checked exceptions

Kotlin's first decision wasn't to build something. It was to throw something out: Java's checked exceptions.

If you started with Kotlin, this might be unfamiliar, so briefly: in Java, when you call a method that throws certain exceptions, you have to either handle them with `catch` or redeclare them in your own `throws` clause. Otherwise your code doesn't compile. Exceptions whose handling the compiler checks this way are called checked exceptions.

![Java throws declaration](images/06.webp){width=100 align=center}

This does two things for you. The possibility of failure is written in the signature, and handling it is enforced. If a repository method declares `IOException` and `SQLException`, the caller knows about both failures without opening the implementation.

The second part was the problem. Because you were forced to catch even exceptions the current layer couldn't recover from, code piled up that opened a `catch` block just to log. The repository reads a file, the service calls the repository, the screen calls the service. If the service in the middle can't recover from an I/O failure, it has to pass the exception up or rewrap it in its own layer's exception. The handling policy hasn't changed one bit, yet the declarations and conversion code keep growing. It didn't play well with lambdas either. Java's `Function.apply` doesn't declare any checked exceptions, so you can't pass a method that throws one straight into `Stream.map`. Exception-forwarding code gets wedged into what was supposed to be "just plug in a function."

So Kotlin got rid of them. Exceptions and `try-catch` stayed, but [every exception is treated as unchecked](https://kotlinlang.org/docs/exceptions.html). You can add `@Throws` when Java callers need to know, but that doesn't introduce any checking at Kotlin call sites. I think this was the right call. It did come with a price, though. The enforcement was what people hated, but writing failures in the signature went away along with it. Today, a Kotlin function declared as `fun loadUser(): User` gives you no way to tell whether it throws. **There's no longer anywhere to write down that something can fail.** That sentence is where this whole part starts. You can read the four approaches that follow as separate attempts to build that "somewhere."

![KT-7128 multi-catch issue](images/07.webp){width=100 align=center caption="KT-7128 has been open for 11 years. It asks for multi-catch, catching several exceptions in one clause. It was filed in 2015, has the second-most votes of any unresolved issue, and has never had an assignee. That's because the language team doesn't recommend using exceptions as an error model in the first place, but a rejected request that refuses to go away for 11 years also tells you there's a real need."}

### null: the first tool within reach

The first material people reached for was `null`. Kotlin had put nullability into the type system, so it was the natural choice. `toIntOrNull()` returns `null` instead of throwing when parsing fails, and the caller either picks a default with `?:` or chains the next step with `?.`. Chaining is easy, and it meshes nicely with smart casts.

![null idioms](images/08.png){width=100 align=center}

When the reason doesn't matter, `null` is still a good way to express failure. If you've decided to fall back to a default when a config value isn't a number, encoding in the type exactly which character broke the parse would be overkill.

But it cracks in two places.

![Where null cracks](images/09.webp){width=100 align=center}

First, it falls apart with collections. `listOf(null, 42).firstOrNull()` returns `null`, and you can't tell whether that's because the first element is `null` or because the list is empty. In one case you got the first element; in the other, you didn't. Second, chains lose the location. If `fetch()?.charge()` gives you `null`, you can't tell whether the user wasn't found or the charge failed. That's the opening problem all over again.

You could object that `firstOrNull` is a lookup API, not an error-handling API. Fair enough. But in practice it gets used as a failure signal without anyone making that distinction, and KEEP-0441 uses this exact example. `null` isn't bad. **You run out of information when one kind of absence has to stand in for several different decisions.**

### Sealed classes: just make more types

Since `null` wasn't enough, the next thing people did was make more types.

![sealed interface UserResult](images/10.webp){width=100 align=center}

With a sealed type, the compiler knows the full list of possible subtypes. That means a `when` can be checked for covering every case, which is what exhaustiveness checking is. When you add a new `PermissionDenied`, any `when` that lists all the subtypes without an `else` stops compiling, and the branch you forgot shows up. When "the user might not exist" lives only in a comment, a reviewer has to remember it. When it's in the type, the compiler checks it. That's something `null` couldn't do.

It comes with two costs, though.

First, you have to wrap the success too. `User` is usually a domain model, not a class that extends `UserResult`. A sealed type only accepts its own children, so to fit an existing type in, you make a `Success(val user: User)` box, and then unwrap it again at the call site.

Second, the hierarchy belongs to this one API. The next screen over needs its own. So your codebase fills up with `UserResult`, `LoginResult`, `PaymentResult`, `ProfileResult`. You've probably got a few of these yourself. The KEEP describes this approach as powerful, but verbose and repetitive.

To be fair, no language rule says you need a new one for every function. You can reuse a generic wrapper. But then the project has to design everything itself: which results to group, which failures to share, how to get the success value out. Because sealed types are a general-purpose tool, each team has to invent its own error-handling idioms. Side note: sealed interfaces only arrived in Kotlin 1.5, in 2021. Before that, subclasses had to live in the same file, and building a hierarchy cost even more than it does now.

### kotlin.Result: half an answer from the standard library

Repeat those hierarchies enough and you start wanting a generic version. But the `Result` that Kotlin actually gave us wasn't born for that job.

![Continuation.resumeWith](images/11.webp){width=100 align=center}

[kotlin.Result](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-result/) came in with coroutines in Kotlin 1.3. It was an internal part for telling `Continuation.resumeWith` whether a suspended computation finished with a value or with an exception. So it holds either a success value `T` or a single `Throwable` for failure. With only one type parameter, there's nowhere to say what the error is. Looking at `Result<User>`, you know you'll get a `User` on success, but you don't know whether on failure you need to handle "user not found," "permission denied," or "rate limited." `exceptionOrNull()` just returns `Throwable?`.

![Result plumbing code](images/12.webp){width=100 align=center}

Chain a few `Result` functions together and you get code like this. The actual work is two function calls. Everything else is plumbing that unwraps values and wraps them up again. The standard `map` is fine for transforming a success value, but if the transform itself returns a `Result`, you have to deal with nesting. You can write your own extension functions or flatten it with early returns, but that turns into yet another team convention. JetBrains itself quotes this code as a problem case in its official documents.

Performance is a separate question. `Result` is a `@JvmInline value class`, so it doesn't allocate a wrapper object on the success path. "Result always creates an extra object" isn't accurate. The more direct difference is the API surface, not allocation. Open autocomplete on a `Result<User>` and you'll see `getOrNull`, `map`, and `fold`, but not a single method of `User`. You have to unwrap it first.

### Arrow: the community got there first

With the standard `Result` being only half an answer, the community built its own. Arrow is a functional programming library for Kotlin. It got its current name when funKTionale (from 2013) and Kategory (from 2017) merged, because their authors worried the community would split.

![Result vs. Either](images/13.webp){width=100 align=center}

Arrow's `Either<E, A>` spells out both the failure type `E` and the success type `A`. By convention, `Left` is failure and `Right` is success. That's the decisive difference from `Result`: you can specify the error type as a generic parameter. Given `Either<UserError, User>`, the caller can open `UserError` to see the expected failures, and if `UserError` is sealed, you get exhaustiveness checking too. In other words, you don't have to wait for Rich Errors to put failures in your types.

Arrow also handles chaining functions. Inside an `either { }` block, `bind()` pulls out the success value so you can use it on the next line, and stops the computation right there on failure. More recently there's also [Raise](https://arrow-kt.io/learn/typed-errors/from-either-to-raise/), a context-based approach. The word "functional" makes it sound like you have to learn theory first, but the starting point is a single flow: keep going on success, and on failure, return in the way you've defined.

Technically, Arrow was a precise answer to `Result`'s limits. So why didn't everyone use it? Here's something you actually hear in the community:

> I get that Either is good. But I can't spend six months teaching functional programming to teammates who still find generics unfamiliar. That's not Arrow's fault.

Even when the technology is right, adoption stalls at the team's learning cost. And there's one more thing: code from teams that use Arrow and teams that don't starts to look like two different languages. **The library created a dialect, not a common language.** I'm not calling Arrow a failure. For teams already using it well, it's still a perfectly practical answer. It's more accurate to say the community came up with an answer first, and the language is now learning from it.

### So where did real-world code end up?

I want to share one piece of real code that came up in the KEEP discussion. It was a function that deducts from a balance, and its return type was a single `Boolean?`. `true` meant success, `false` meant insufficient funds, and `null` meant the state wasn't what it expected. Three meanings in one type, and which was which was written only in a comment. The author explained why, too: there was no way to express it otherwise, and a sealed class was way too much boilerplate for a function used in one place. The comment was saying what the type couldn't. Honestly, that one hit close to home.

### Recap: there was never dedicated syntax

![History recap table](images/14.webp){width=100 align=center}

Each of the five solved something and left something behind. Exceptions handled unrecoverable situations, but there was no longer anywhere to write that something could fail. `null` was the most concise, but it loses the reason and the location. Sealed types gave us exhaustiveness checking, but you have to wrap the success and repeat the whole thing for every API. `Result` gave us a standard wrapper, but it can't name the error type. Arrow gave errors real types, but the team had to learn it.

The five are different, but they have one thing in common. **They're all combinations we each assembled from general-purpose parts the language gave us. There was never dedicated syntax for recoverable failure.** That's the answer to "just use a sealed class" from the opening. You can. But for 15 years, everyone's been building their own.

## Rich Errors: failure goes in the return type

So what's Kotlin's answer? At KotlinConf in May 2025, Kotlin's lead language designer, Michail Zarečenskij, unveiled it in a 45-minute solo session. Not a quick mention in the keynote, but an entire session of its own, which tells you how seriously they take it. He's also the author of KEEP-0441 and 0462.

![KotlinConf 2025 session](images/15.webp){width=100 align=center caption="The KotlinConf 2025 session. At the time, the number 2.4 was splashed across the slide, but the feature didn't make it into either 2.4.0 or 2.4.20. The issue tracker now targets 2.5.0."}

### Just two pieces of syntax

![The error keyword and union return types](images/16.webp){width=100 align=center}

There are really only two new things to learn.

First, a new keyword: `error`. You declare an `error object` when there's no data, or an `error class` when it needs to carry some. Here, `NetworkError` holds a status code.

Second, a pipe in the return type. You write the success and the failures side by side. Read `User | NotFound | NetworkError` exactly as it looks: a User, or NotFound, or NetworkError. Any single call returns one of them.

The important part is that an error is a **returned value**, not something that climbs the stack like an exception. You can store it in a variable, branch on it, and pass it to another function. `return NotFound` and `throw SomeException()` are completely different control flow.

### Back to the opening function

![Answering the opening question](images/17.webp){width=100 align=center}

This is the function from the opening. Whether the user is missing or the call timed out, the signature now tells you itself. No comment needed, and nobody has to ask in review.

Here's the calling side:

![when and smart casts](images/18.png){width=100 align=center}

That's all there is to it: `when` and smart casts. Inside the `is User` branch, `user` is already a `User`, so there's nothing to unwrap or cast. If you don't handle all three branches, the compiler catches it. The only new things are the `error` keyword and the pipe; the rest is Kotlin you already know. Remember Arrow's learning cost? That's exactly why this had to be part of the language rather than a library.

That said, changing the return type won't make the existing `withTimeoutOrNull` tell the two failures apart for you. Giving a name to a result that's already been collapsed into `null` can't tell you which case it was. You have to change the implementation so that the point where the lookup produces "not found" and the point where the timeout expires each return a different error value. **The type defines the shape of the information to keep; producing that information correctly is still the implementation's job.**

### You can't union just any types

![Allowed and disallowed unions](images/19.webp){width=100 align=center}

Let me head off a misconception. You can't join arbitrary types with a pipe. `String | NotFound` is fine, but `String | Int` is a compile error. Only types declared with `error` can take part in a pipe, there can be at most one non-error type, and it goes on the far left.

You don't even need a success type. You can write just `TimeoutError | ServerError`, for example as the parameter of a shared error-handling function. Going the other way, you can put a nullable type on the success side, as in `User? | NetworkError`. Here, `null` is a result the contract allows and the network failure is a separate value, so the two never mix. One of the spec's Goals is to resolve the ambiguity when null is used both as an error and as a value. That's precisely the opening problem.

### Why so restrictive?

![No inheritance, no generics](images/20.webp){width=100 align=center}

There are more restrictions. Error types can't use inheritance, and they can't be generic. Every error is final and can't have type parameters. They can't have user-defined superclasses or interfaces either, so grouping them under a common parent like `HttpError` is impossible. Instead, the spec tells you to group them with a typealias.

![Composing with typealias](images/21.webp){width=100 align=center}

A typealias doesn't create a new parent type; it gives a name to an existing set of types. What matters is that the same `TimeoutError` can go into both the user-lookup error set and the file-download error set. Inheritance can't do that.

If you're used to common parent types, this is a pretty frustrating restriction. Why lock it down this hard? There are two reasons.

First, if you can union anything, there's nothing you can actually do with the resulting value.

![Methods available on A | B](images/22.webp){width=100 align=center}

![Methods available on User | NotFound](images/23.webp){width=100 align=center}

Say you receive a `String | Int`. It's neither a `String` nor an `Int`, so all you can call are `Any`'s methods: `toString`, `equals`, `hashCode`. With Rich Errors, on the other hand, there's exactly one value type and everything else is an error. Filter out the errors and what's left is the value itself. `Result<User>` didn't work like that. Whether or not there's an unwrapping step: that's the difference.

Second, the compiler couldn't keep up. Leave unions wide open and the cost of type inference gets out of hand. The spec's Goals say the type inference algorithm should stay polynomial and unambiguous.

The spec spells out the reasoning behind these restrictions in a single paragraph. It's the passage I highlighted in three places during the talk.

![The reasoning, as written in the spec](images/24.webp){width=100 align=center}

The three highlighted bits form a single chain of cause and effect. The design builds a structure where errors and values never overlap, which the spec calls a disjoint union. Nothing can be both a value and an error at once; no value is both a `User` and a `NotFound`. Because they never overlap, an ordinary type check is enough to tell them apart, and a single `is` decides success or failure. And so nothing needs wrapping. `Either` puts a success or a failure inside a `Left` or `Right` box, but with `User | NotFound`, success is the `User` itself and failure is the `NotFound` itself.

[On the JVM](https://github.com/Kotlin/KEEP/blob/main/proposals/KEEP-0462-rich-errors.md#compilation-scheme-for-jvm), a union is represented as `Object`, and the actual object is passed through as-is. The catch is that primitives like `Int` still need boxing. So the accurate claim isn't "error handling is free" but "the extra wrapper for the union goes away." The spec itself says "almost" overhead-free.

![The KEEP's type hierarchy](images/25.webp){width=100 align=center caption="The type hierarchy diagram from the KEEP. A new name, Value, appears as the common supertype of every non-error type. Value and Error both sit under Any, so they meet at the top, but no single object can belong to both. Value isn't a real class but an interface the compiler treats specially, and it has nothing to do with value classes. The spec admits the name is confusing and lists finding a better one as an open question."}

**They traded away expressiveness to buy those two things.** Once you see that trade, every restriction above makes sense.

### Exceptions don't go away; the line between them gets sharper

So do exceptions go away? No. A line gets drawn between the two.

A failing `require(name.isNotBlank())` is an exception. There's nothing the caller can do about it; the code needs fixing. A missing user or a dropped network connection, on the other hand, is an error. The caller has to act differently, whether that's drawing an empty screen or retrying. There's one test: **can the caller know about this and act differently?** If yes, it's an error. If no, it's an exception. That's the last item in the spec's Goals: a clear distinction between exceptions and rich, predictable errors.

Here's the design in four lines. Treat errors like values. Use them only for recoverable failures. `when` has to be exhaustive. Don't slow down compilation. Every restriction we just saw comes from that fourth line.

## Chaining with `|.` piles up errors

### Your `?.` stays the same

First, the reassuring part. The existing `?.` doesn't change one bit. Instead, there's a new operator, `|.`, called the error-safe call. It works on the same principle as `?.`: if the left side is an error, it stops and passes the error through.

![The pipe-dot operator](images/26.webp){width=100 align=center}

Say `loadModel()` returns `Model | NoData` and `Model.computeTask()` returns `Task | FeatureDisabled`. Chain them, and it reads like this:

- If `loadModel()` returns `NoData`, `computeTask()` never runs and that failure becomes the result.
- If you got a `Model`, the next function is called and returns either a `Task` or `FeatureDisabled`.

The final type is `Task | NoData | FeatureDisabled`. Here's the fun part. Earlier we saw what goes wrong with `?.` chains: in `fetch()?.charge()`, whichever side failed gets mashed into a single `null`. The pipe-dot goes the other way. **Errors don't get mashed together; they pile up.** And I didn't write that list. The compiler put it together by looking at the two functions.

Let me be precise about "errors pile up." It means the lists of possible error types get merged. It doesn't mean multiple errors that happened at runtime get collected into a list. If an earlier step fails, the later ones don't run, so the computation has exactly one result.

So why is that good? Insert another function in the middle, and the type grows by itself. Anywhere that doesn't handle the new failure turns into a compile error. However long the pipeline gets, the list of failures stays up to date on its own. You can't do that with sealed classes, where every new step means fixing the hierarchy by hand. Of course, if you lump everything into an `else` or widen the type to `Any`, you give up this feedback.

### Propagating through a chain isn't returning early

One distinction to keep in mind. `|.` skips the next call in the chain; it doesn't end the current function. Even if the chain's result is an error, the next line still runs. There's no syntax like Rust's `?` that propagates the error out of the function. Instead, the spec proposes standard functions like `ifError { return it }`. On failure, it hands the error to the caller and the function ends. On success, you're left with the success value after it. We'll see this difference again in a real call site later on.

### Why not reuse `?.`?

![Discussion of alternative operators](images/27.webp){width=100 align=center caption="The record of the discussion about alternative syntax for error propagation. The first idea was to reuse ?., and after that !. was a candidate. The last sentence is the key one: the goal is to avoid a jumble of question marks and exclamation marks."}

`?.` was ruled out because of the type hierarchy. In the current design, Error is a subtype of `Any`, so an error can end up inside a value you're handling as `Any?`. If `?.` were changed to skip errors as well, a different function would run without a single character of source code changing. So [the spec](https://github.com/Kotlin/KEEP/blob/main/proposals/KEEP-0462-rich-errors.md#why-existing-nullability-operators-cannot-be-reused) is explicit that it won't extend the existing nullability operators to error handling, to avoid ambiguity and compatibility problems.

The reason `!.` wasn't picked is in a different section of the spec. In Kotlin, the exclamation mark already signals dangerous operations, as in `!!`, so the designers didn't want the same symbol on an operation that propagates failure safely. Instead, they tied it to `|`, the union symbol. Even choosing a single symbol involves compatibility and consistency.

### When the same error comes from two places

![The traceability rule](images/28.webp){width=100 align=center}

Giving a failure a type doesn't mean you keep track of where it came from. If `resolveUser()` and `medicalRecord()` both return `AccessDenied`, the `AccessDenied` in the chained result can't tell you whether you were rejected because the user couldn't be resolved or because access to the medical record was denied. It's the opening `null` ambiguity again, this time inside a single error type. The spec uses this exact example and says that when the same error from different sources gets merged in a `|.` chain, the compiler emits a [traceability warning](https://github.com/Kotlin/KEEP/blob/main/proposals/KEEP-0462-rich-errors.md#error-traceability). If handling needs to differ by source, you split the calls and handle each one, or convert to a more specific error at the boundary.

![The most-upvoted objection](images/29.webp){width=100 align=center}

But this was the most-upvoted objection in design discussion #487. The gist: merging errors from different sources is often perfectly reasonable. If two calls use the same underlying service, there's no reason to tell apart the same error coming out of it. And whether errors need to be told apart is a design decision for each codebase, not something the language can or should enforce.

![The language team's reply](images/30.webp){width=100 align=center}

The language team replied that they sympathized with the concern, but wanted to know how often it actually happens in practice and how hard it is to work around. It's a trade-off between safety and flexibility, they said, and they'd evaluate whether it's worth keeping during the experimental phase. The last sentence shows a principle of language design: a restriction is easy to relax later, but nearly impossible to add later. So you start strict.

Then two months later, on July 31, this restriction really was downgraded from a compile error to a warning. I don't know whether the objection was the reason; the commit message doesn't say. But it's exactly the "easy to relax" they'd talked about, put into practice. The gray quote block in the objection screenshot above says it's a compile error, but that's the spec's wording at the time of the screenshot, and it's no longer true. Warnings can be ignored, and turning on `-Werror` in CI makes them errors again, so it's more accurate to say the responsibility for the decision moved from the language to the developer.

Personally, I think this discussion captures the heart of Rich Errors. What a type can decide for us is different from what someone who knows the domain has to decide. The compiler can tell you "these are the same type, so they can't be told apart." It can't know that "these two failures must be handled differently."

### The whole thing on one slide

![Rich Errors on one slide](images/31.webp){width=100 align=center}

The new syntax is just three things: `error`, `|`, and `|.`. The rest is Kotlin you already know. In exchange, you give up inheritance and generics. That's the trade we just looked at.

## Trying it on three real Android apps

Let me be clear up front: you can't use this syntax today. It's not in Kotlin 2.4.0 or 2.4.20. But on August 12, four days before my talk, the language team published a document in which they took apart three real open-source Android apps themselves to check how Rich Errors would play out in real code. That's [Design Notes 0009](https://github.com/Kotlin/KEEP/blob/main/notes/0009-rich-errors-use-cases.md).

![notes/0009 commit history](images/32.webp){width=100 align=center caption="The commit history of notes/0009. Every example links to a pinned commit of the original code, so you can check exactly when it was analyzed."}

The apps are DuckDuckGo, the privacy browser (about 100 Gradle modules); AnkiDroid, a flashcard app; and Signal, the end-to-end encrypted messenger. They weren't picked for being famous. They were picked because their code is public, so things could actually be counted.

When you read this document, check the premise first. It's not a report on three apps that migrated to Rich Errors. It's a design review that compares **what happens when code that already treated failures as values, through null, sealed types, and callbacks, re-expresses the same intent**. It isn't a verdict that the existing architecture is wrong, and it isn't a performance measurement either. For the talk, I picked three patterns and one call site from it. You don't need to know any of these apps to understand them; they're shapes we run into every day in repositories and ViewModels.

### 1. Wrapping the success just to describe one failure

The first example is a DuckDuckGo function that decides whether a custom header may be added to a browser request. If it's allowed, it has to return which config to use along with the "yes." So a `Boolean` won't cut it.

![CustomHeaderAllowedChecker, original code](images/33.webp){width=100 align=center}

The existing code puts `Allowed` and `NotAllowed` in a sealed class. The success data actually needed is a `TrafficQualityAppVersion`, but what gets returned is the `Allowed` that wraps it. A sealed type only accepts its own children, and a config class that's already used elsewhere can't be made a child of this hierarchy, so the code builds a box and puts it inside. And since there's a box, the call site has to open it with `result.config` every time.

Here's the chain of cause and effect. The success carries data, so `Boolean` isn't enough. Both outcomes have to fit in one type, so it's a sealed class. A sealed class only takes its own children, so you need the `Allowed` box.

![CustomHeaderAllowedChecker, proposed syntax](images/34.webp){width=100 align=center}

In the proposed version, the declaration is a single line: `error object NotAllowed`. And `TrafficQualityAppVersion` stays as it is, unwrapped. **A sealed type only lets in its own children; a union just puts things side by side.** That makes three things disappear at once: the box, the unwrapping code at the call site, and the name `Result`, which clashed with the standard library. The generic parameter, added in the hope of reuse but only ever used for one type, goes away too. Read the signature as written and you get the contract: you get a config, or it's not allowed.

That doesn't mean every wrapper is unnecessary. If the success needs extra meaning attached, like when it was created or where it came from, a separate type is still useful. What goes away here are wrappers that only existed to fit a result hierarchy.

### 2. The value is returned, the error goes out through a callback

The second example is an AnkiDroid function that registers media from the clipboard into a note. On success, it returns a string pointing to the registered media. On failure, it has to tell the user.

![onPaste, original signature](images/35.webp){width=100 align=center}

This is a really common shape on Android. The value goes out through the return type, and the error goes out separately through a `showError` callback. It takes a callback because it needs to show a snackbar. The call site puts the snackbar code in the callback and then uses control flow like `?: return` after the call.

The document makes a sharp point here: the type system can't check that the two channels agree. Returning `null` without calling the callback compiles. So does calling the callback and then returning a value. Every helper that can fail has to do both, every time, and if it misses one, nothing catches it. A state that should never be allowed is left wide open in the signature.

![onPaste, proposed syntax](images/36.png){width=100 align=center}

The callback parameter is gone. The value and the error both come out through a single return type. Now the function can only give you a value or an error, never both. Calling the callback and returning a value can't even be expressed anymore. **What the developer used to have to remember is now enforced by the type.**

There's one more benefit. Originally, how to display the error was baked into this function, since it took a callback to show a snackbar. Now the caller decides. Imagine reusing the same registration logic on a screen and in a background job: the screen shows a message, and the background job logs it or updates the job status. Presentation policy the registration function never needed to know about goes back to the caller. The reading order falls into place, too. Before, the error handling sat inside the argument list and the control flow came after the call, so everything read inside out. Now you call, handle the failure, and move on.

It's the same problem as `loadUser(id): User?` from the opening. The return type couldn't carry the failure, so the code punched a side channel through.

### 3. Same name, different type

The third example is a single registration API file in Signal. It has about twenty endpoints and seventeen sealed error hierarchies, one per endpoint.

![CreateSessionError](images/37.webp){width=100 align=center}

![UpdateSessionError](images/38.webp){width=100 align=center}

You'll see `RateLimited` twice. Same name, different types, because a sealed subclass belongs only to its own parent. In this one file, `RateLimited` is declared separately nine times, and `InvalidRequest` eleven times.

![The question mark in handleRateLimited](images/39.webp){width=100 align=center}

So you can't write "a function that handles any `RateLimited`." There's no type to put in the parameter. You might say, "Just take the parent type." In the real code, both sealed classes do implement `BadRequestError`. But take that, and you can't tell `RateLimited` from `InvalidRequest`, and you can't read `retryAfter` either. Even an existing parent type is useless if it doesn't match the grouping you need right now. You could solve it by designing a separate interface or mapping layer, but that's one more composition mechanism to maintain.

With Rich Errors, you declare each error once and compose them with typealiases. Then `handleRateLimited(e: RateLimited)` becomes possible, and the question mark gets filled in. Write the retry logic once, and all twenty endpoints share it. Adding or removing an error turns into changing a single token in a typealias.

But it shouldn't end at "same name, so merge them." In the real code, `RateLimited` carries extra data depending on the endpoint, and whether it's safe to retry depends on the nature of the operation. Rich Errors make sharing easy to express. They don't magically make different meanings the same. I'll come back to this later.

### 4. So what happens at the call site?

Looking only at declarations doesn't answer "so what gets better in my code?" Let's look at a call site. This is a trimmed-down piece of ViewModel code that restores a master key and then continues with registration.

![restoreMasterKey, original call site](images/40.webp){width=100 align=center}

There's a `when` inside a `when`. The outer one strips off the `RequestResult` wrapper, and the inner one picks out the actual domain error. Two layers of branching. To follow the success path, you read `Success`, then the `result` inside it, then the `masterKey` inside that. The failures are split across layers too: domain failures on one, transport failures on the other.

![restoreMasterKey, proposed syntax](images/41.webp){width=100 align=center}

The version below has a single layer. One `ifError` filters out the error, and after it the value is smart-cast, so the unwrapping code disappears. The key point is that the `ifError` block never finishes by returning a value normally. It `return`s from inside, so that path has type `Nothing`, and the compiler can use the fact that only the success value remains afterward. That's different from logging and moving on with `ifError { log(it) }`. Logging a failure doesn't turn it into success data.

And both steps share transport errors like `NetworkFailure`. That's what we just covered: since they're declared once as a shared vocabulary, they land in the same branch no matter which step they came from. Here's where the real difference shows. Insert one more call in the middle. The top version grows another four-way `when` pyramid. The bottom one just gets one more error in its type, and that's it. For reference, the original function is about 120 lines long.

A word of caution. `NetworkFailure` and `UnexpectedFailure` stay as values in this example because the language team kept the intent of the existing architecture when making the comparison. Don't read it as official guidance to turn every exception into an error value. Improving how failures are returned and deciding how to classify them are separate calls.

### Limits written down by the people who built it

One last thing. Signal knew about this duplication. So it built its own generic wrapper: `NetworkResult.kt`, 469 lines and twenty-nine functions. But the KDoc in that file says something to this effect: if you have a complex request with lots of different responses depending on the error, this isn't for you, and you're better off writing your own sealed class.

They documented the limits of their own tool. So in the same codebase, one generic wrapper runs alongside 125 hand-written sealed result, error, and failure types. The history from earlier in this post replayed itself inside a single company. I don't read that number as "the amount of bad code that needs fixing" but as **the cost people were already paying to express failure accurately**. What Rich Errors is trying to reduce is the cost of expression that pushes people to give up that accuracy.

## What to keep in mind

### You can't use it yet, and it might change

[KT-68296](https://youtrack.jetbrains.com/issue/KT-68296) lists Experimental in 2.5.0 as the target. But the language team has never committed to a date. All they've said in the design discussions is "we're not rushing" and "there's a long way to go." The design spec is still in Public discussion. The accurate way to put it isn't "it's coming in 2.5.0" but "it's being validated with 2.5.0 as the target." As I said in the talk, half of this post could be out of date in a few months. It's a living design. Even the spec itself says some features, like negative smart casts, might ship later than error unions because they're complex to implement.

### Declaring failures doesn't make exceptions disappear

Even with a `User | NotFound` return type, implementation bugs and other exceptions still happen. An error union describes the set of failures a function explicitly returns. It isn't proof of every runtime event that might occur in it. You still need exception propagation and your framework's exception-handling boundaries.

People also ask, "Isn't this just checked exceptions again?" The goal of writing failures into the contract and making callers deal with them is similar. There are three differences. Propagation is lightweight: errors flow as values instead of climbing the stack like exceptions. Because they're values, they compose with lambdas, and checked exceptions being terrible with lambdas is exactly why Kotlin dropped them. And they're only for recoverable failures; exceptions stay as they are. That said, this objection has come up again and again in the discussion and hasn't been fully put to rest.

### Composing error sets isn't accumulating validation errors

Say you want a sign-up form to show name, email, and password errors all at once. Chain the checks with `|.` and everything after the first failure gets skipped. "There are three possible failures" and "collect the three failures that actually happened" are different requirements. A union is a structure where one of several errors comes back, so it can't accumulate anything. You can model it with an error class that holds a list of errors, but the union and `|.` won't do that for you. This is where [tools](https://arrow-kt.io/learn/typed-errors/working-with-typed-errors/) like Arrow's `zipOrAccumulate` and `mapOrAccumulate` still matter. Rich Errors solve the problem of surfacing a single failure in the signature. They don't replace error handling as a whole.

### Stack traces and types carry different information

Unlike an exception, an error value doesn't automatically get a stack trace from where it was created. That's not unique to Rich Errors; it's true of the whole errors-as-values approach. Same with `Result`, and same with `Either`. These tools are meant for expected failures in the first place, so if you need a stack trace, you're probably looking at something that should be thrown as an exception. If you need to trace something, put the information you need in the error yourself, as in `error class NetworkError(val code: Int)`. And if you converted an existing exception, you can keep the original `Throwable` as a field to preserve its stack. I'd steer clear of saying "treating errors as values loses all your stack traces."

### Adding an error to a public API is a change too

Inside an app, having call sites stop compiling when you add an error is useful feedback. In a published library, the same thing turns into a compatibility problem, because existing users wrote their code against the old list of failures.

The same goes for Java. From Java's point of view, a union return type is just `Object`, so there's no way to know what to handle, and you could pass in any object at all, which is dangerous. So the current [JVM design](https://github.com/Kotlin/KEEP/blob/main/proposals/KEEP-0462-rich-errors.md#interoperability-with-java) mangles the names of functions that have error unions so Java can't call them directly, and exposes them through an adapter plugin that maps error unions to exceptions instead. That's a big deal for library authors and for teams with legacy Java code. Under the current mangling scheme, changing an error set also affects binary compatibility, so hiding it behind a typealias doesn't make the problem go away.

## AI writes all my sealed classes now. Do I still need this?

Some of you have probably been thinking this while reading. I did too. Earlier, the problem with sealed types was that they're verbose and repetitive. If AI writes them in a few seconds, isn't that cost gone?

Honestly, the only cost that's gone is the cost of writing them. The cost of reading and understanding them is exactly the same. And one thing got worse. Think back to Signal. People wrote that code by hand, and `RateLimited` still showed up nine times. AI doesn't write code with the whole project in view. It creates whatever error type this particular function needs, on the spot. If you hand off implementation one function at a time without passing along your existing error vocabulary and boundaries, you end up with several result types that each look reasonable on their own. That doesn't mean AI always creates duplicates. It means that if the context we give it is narrow, the result gets optimized within that narrow scope. And the faster generation gets, the bigger the cleanup of those differences after the fact. **The cost of writing went away, but the cost of meaning didn't.**

### Spec first, then let it run

Anthropic's [Opus 5 prompting guide](https://platform.claude.com/docs/en/build-with-claude/prompt-engineering/prompting-claude-opus-5) has this line:

> \[Opus 5\] performs best when given the complete task specification up front and left to run.

The guide even tells you to remove the re-verification instructions you added for older models. The model already verifies on its own, so asking it again just adds over-verification and cost.

But the important part of that sentence is the front half, not the back. "Left to run" comes with a condition attached: a complete task specification. You can say less about how to split up the work or in what order to implement it. But you have to define up front what counts as success and what counts as failure. **What we're stripping out is procedure, not the contract.** I'm not claiming the guide proves anything about Rich Errors; this is my own reading, a connection I wanted to make in the talk. So what goes into a complete spec? Success conditions alone only get you halfway. It also has to say what to do when things fail.

### Split failures by what happens next, not by cause

We need to change the question. Not "what error is this?" but "when this happens, what should the caller do?"

The same 404 can mean different things. A 404 because a user searched and found nothing is a normal domain branch: you draw a "No results" screen. But a 404 on a required config file means the deployment is broken. A timeout on a read can simply be retried, but on a payment, you first have to check whether the previous request went through. The word "timeout" alone isn't enough to decide on a retry. **A failure isn't a technical event; it's a domain decision.**

- **Expected failures**: the caller has a meaningful choice to make, whether to recover, inform the user, or retry. Return them as values.
- **Broken assumptions**: this layer can't decide on a recovery policy. Throw an exception to stop, and log and report it at the right boundary.
- **Cancellation**: a control signal that says not to go any further. Propagate it upward immediately, and never swallow it by turning it into a failure value.

The third is the one people get wrong most often. `runCatching { api.getUser(id) }`. Don't casually write this inside a coroutine. [runCatching](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/run-catching.html) catches every `Throwable`, `CancellationException` included. Cancellation gets disguised as a failure and becomes `Result.failure`, and if you then turn that into a default value or treat it as an ordinary error, the current code carries on to the next step even after it's been cancelled. The parent Job's cancelled state doesn't go away, but the problem is that this stops [the cancellation exception from propagating](https://kotlinlang.org/docs/cancellation-and-timeouts.html) and lets the code keep running as if it were on the normal path. I'm not saying ban `runCatching` in coroutines outright. I'm saying avoid using it in ways that don't tell cancellation apart from ordinary failure. If you catch exceptions broadly and convert them, make sure you have a policy of rethrowing `CancellationException`. It has the same root as the opening problem: lump failures together and you can't decide what to do next.

### Define before you catch

![Define before you catch](images/42.webp){width=100 align=center}

Usually we follow the order on top. Ask the AI, run it, test it, and catch the missing failures in review. But review can't keep up with how fast code gets generated. We have to flip the order. Decide first which failures exist, write them into the types, and the AI implements within those bounds. The compiler stops being the final auditor and becomes a feedback loop that's part of generation. **Being good at error handling isn't about catching errors well. It's about defining which failures exist before you implement anything.**

### What was missing from my rules files

While preparing the talk, I reopened the AI rules files I use. There were rules for MVI architecture, rules for Compose, rules for the design system, and rules for code style. The only thing missing was error handling.

![Rules directory](images/43.webp){width=40 align=center}

I'd set up conventions for architecture and none for error handling. The same story from the history section was right there in my own repo. That's what stuck with me the longest. This wasn't a problem caused only by missing syntax. The meaning of `NotFound`, the boundary where exceptions become values, how to handle cancellation: I could have written at least that much into docs and types already.

So write the rules down. You can do it today, and it pays off right away. But if it stops at documentation, it's weak. Docs might not get read, or they get read and implemented differently anyway. A signature is a contract the AI is guaranteed to run into while implementing. If docs are the map, types are the road itself, and the compiler tells you right there and then.

- **Rules docs** (AGENTS.md, CLAUDE.md): make things discoverable.
- **Type signatures** (`User | NotFound`): constrain which way things can go.
- **The compiler** (exhaustive `when`, must-use): tells you immediately.

They play different roles, so none of them replaces another. I'm not saying you don't need AGENTS.md.

### A good type is compressed domain knowledge

![Domain contract example](images/44.webp){width=100 align=center}

That last line gives the AI everything: what counts as success, how many ways it can fail, and even that on `RateLimited` it should wait for `retryAfter`. There's nothing left to guess. Signal's problem of declaring `RateLimited` nine times doesn't happen here either. Declare it once as a shared vocabulary, and the AI uses it too.

Of course, this signature doesn't prove everything. Having a `retryAfter` doesn't guarantee that automatic retries are safe, and a person still has to decide how widely errors with the same name should be shared. An implementation that returns `PermissionDenied` when it should return `NotFound` compiles just fine, as long as both are in the contract. That's why you keep rules docs, types, and tests together: the docs explain why you split the failures the way you did, the types say what results the caller needs to know about, and the tests check that real situations lead to the right results. The compiler finds gaps within the world we've defined. It won't find the requirements we left out of that world.

Flip the order and your implementation requests get more specific, too. "Distinguish a user that doesn't exist from one you can't access, pass along the wait time when rate-limited, and propagate cancellation" carries far more decisions than "build a user lookup." I wrote these principles in the proposed syntax, but you can apply them today with sealed types or `Either`.

## After the talk: JetBrains on honest signatures

On August 20, four days after my talk, a post called [Signatures, be true: domain errors and functional handling in Kotlin](https://blog.jetbrains.com/kotlin/2026/08/signatures-be-true-domain-errors-and-functional-handling-in-kotlin/) went up on the JetBrains Kotlin blog. I read it as soon as I saw the title. The author is Sergey Chernov, Lead Software Engineer at Salmon, a fintech company building banking and lending products in Southeast Asia. He works on authentication and verification systems, and he writes that in that role, telling two failures apart wrongly is the difference between letting the right person in and letting the wrong one in.

It ran on the official JetBrains blog, but keep in mind that it's a guest post from a practitioner, not guidance from the language team or the Rich Errors spec. That's actually what made it more interesting to me. The words "Rich Errors" never appear once, yet it walks almost the same path as my talk, using tools you can use today.

### Same starting point: a function that returns Unit and secretly throws

The post opens with a function that signs a document.

![signDocument signature](images/45.webp){width=100 align=center}

He asks you to look at the signature on top and say what can go wrong. You can't. Yet the signing code could be wrong, the signing window could have closed, the document could already be signed, or the database could be down. To find any of that out, you'd have to open the implementation, then the services it calls, and read through the exception handlers, the route mappings, the tests, and the OpenAPI spec. Everything except the signature, which is the one thing that should have told you. It's exactly the same problem as `loadUser(id): User?` from the opening. The signature he wants is the one at the bottom: inputs on the left, the expected failure types and the success type on the right.

He also points out that the phrase "functional error handling" scares people off. They brace for a lecture on monads and category theory, and that's not what this is. There's one goal: the signature alone should tell you how to call the function and which results you need to handle, with nothing hidden in the body. It's the same thing I said earlier about Arrow, that the word "functional" makes it sound like you need to learn theory first.

### Three kinds of failure, and only one goes in the signature

This was the most useful part of the post for me. He splits failures into three groups:

1. **API client errors.** The caller used the API wrong: broken JSON, a missing header, requests in the wrong order. A well-behaved client almost never hits these, so instead of listing each one in the domain model, he lumps them into coarse HTTP responses like 400, 403, and 404.
2. **Unexpected exceptions.** The database is down, a dependency timed out, an NPE happened. These aren't business outcomes. Nobody designs a user flow for "when Postgres falls over." They become operational signals: a 500, logs, metrics, and a page to whoever's on call.
3. **Domain errors.** The client did everything right, and the operation still can't succeed. The signing code is wrong, the window has closed, the document's already signed. These are failures real users hit through no fault of their own, and the designer has a screen for each one.

Only the third group belongs in the signature. He says he often sees people drag the second group into the first or third, like putting `DatabaseUnavailable` in an error union. Let it throw, and let the global handler catch it. HTTP 400 isn't a domain concept, but "signing window closed" is.

Put this next to the three-row table from my talk and it lines up almost exactly. His domain errors are my "expected failures," and his unexpected exceptions are my "broken assumptions." When he writes that splitting these three groups correctly is most of the design work, that's the same conclusion as "a failure isn't a technical event; it's a domain decision" from the talk. Since it's a server-side post, though, it doesn't cover coroutine cancellation. On mobile, that third row is where people slip up most often.

There's one thing I don't think carries over as is. He puts a dropped network connection in the second group. For someone running a server, a database outage really is an incident that calls for alerts and investigation. But in a mobile app, being offline is part of normal use. There are screens that need to show cached data or wait for the user to retry. File it under "technical cause, so always an exception," and that screen's requirements disappear. The language team keeping `NetworkFailure` as a value in the Signal example and this post treating infrastructure failures as exceptions aren't in contradiction. They're contracts for different consumers at different layers. The test isn't the name of the cause. It's **which distinctions the code consuming this particular contract needs**.

### Starting with a sealed interface, ending up at Either

The solution is a familiar one: model the domain errors as a sealed interface and handle them with `when`.

![DocumentSignError sealed interface](images/46.webp){width=100 align=center}

Add a fourth failure and this `when` stops compiling. That's what he calls the whole game: the compiler knows what can fail and won't let you forget it.

Next, you need a way to say "this function returns that error or a success." You could write a wrapper by hand, and people really do write one for every result type, but it gets verbose fast. So you arrive at the generic version, `Either`: left is failure, right is success. It's a two-way sealed type you can understand without any library, plus a handful of helpers like `map`, `flatMap`, `fold`, and `getOrElse`. I also liked his analogy that Java's `Optional` is just an `Either` with nothing on the left.

Reading it felt like rewriting the history part of my talk. `null` isn't enough, so you move to sealed types; you repeat sealed types until you reach for a generic wrapper. The path he titles "You just reinvented Either" is the one the Kotlin community has walked for 15 years. And he reaches the same verdict on `Result` as I did: if the left side is left open, you gain nothing.

### Two unions people get wrong

This is the practical core of the post. When a team adopts this style, he says, two anti-patterns almost always show up, and both throw away most of the benefits.

The first is `Either<Throwable, Unit>`. It looks typed, but all it says is "something might fail." `Throwable` is an open type, so every `when` needs an `else`, and you're back to not knowing. He writes that this is essentially the same as throwing an exception, and that it's why Kotlin's `Result<T>` isn't recommended for domain modeling. Same diagnosis as "`Result` can't name its error type" from earlier.

The second is a single wide union shared across a whole class to cut down on repetition.

![The wide-union anti-pattern](images/47.webp){width=100 align=center}

The compiler's happy, but now every method looks like it can return every error. `signDocument` can never produce `TemplateNotFound`, yet every caller has to write that branch. You get exhaustive handling full of impossible branches; in his words, catch-all programming wearing a type. The fix is to define one narrow union per public method: `DocumentSignError`, `PrepareSigningError`, `ExportError`. It's a bit more typing, but he says it pays for itself every time you read a signature.

This shows once more why Rich Errors went with typealias composition. Remember the Signal example. The problem was repetition, and the easiest way to get rid of repetition is exactly the second anti-pattern this post warns about: unify everything into one global `AppError`. The declarations get simpler, but now a function that reads a user has payment authorization failures in its result, and callers go back to checking the implementation instead of trusting the signature. There's a type, but the contract is still vague.

![The same contract with Rich Errors](images/48.webp){width=100 align=center}

Rich Errors' answer is to have it both ways. You declare each error once and share it, but compose a separate failure list for each function. Profile lookup is `NotFound | PermissionDenied`, profile update is `PermissionDenied | InvalidNickname`. What's shared is `PermissionDenied`, used with the same meaning, while each operation's list of failures is different. Doing this with sealed interfaces means carving out new classes for every hierarchy, which is how Signal ended up with `RateLimited` nine times. The discipline this post asks for, one narrow union per method, is something Rich Errors gives you for the price of a one-line typealias.

On the flip side, a rule like "share every error across the whole project" goes too far. A missing account and a missing temp file could both be named `NotFound`, but they may change for different reasons and need different handling. Only share within a scope where you've confirmed it's the same knowledge, with the same meaning, changing together. Preserving what the contract means comes before cutting down on duplicate types.

### Composing without plumbing, and converting once at the boundary

Real flows chain several steps, and any of them can fail. Write that naively with `flatMap` and the lambdas nest one level deeper with every step. He shows two ways out: early returns in plain Kotlin, and Arrow's `either { }` block.

![either block and bind](images/49.webp){width=100 align=center}

`bind()` pulls out the `Right` value and short-circuits on the first `Left`. Scala's for-comprehensions have had this idea built into the language for a long time. Then he adds: use Arrow if it helps your team, but "I'd rather you adopt the discipline than the dependency." That's a practitioner's answer to the cost of adopting Arrow we looked at earlier. It also becomes clear that the `|.` and `ifError` we saw before do exactly what this `bind()` does. Something that only worked inside a library's DSL block is moving into the language as a single operator.

He's clear about boundaries, too. Services and repositories return domain errors, and the conversion to HTTP happens in exactly one place, at the route boundary. It's a single line: `mapLeft { it.toHttpResponse() }`. Only the controller knows about HTTP, and everything below it speaks in business outcomes. And if you ship an API client alongside the service, he says to ship the error types with it. That way the client handles failures with the same sealed union as the server, and the two stay consistent for free. For a mobile team, that's a pretty attractive idea. You no longer have to guess in the app whether the server's 404 means "no such user" or "no such endpoint."

### Review the contract, not the implementation

The last section overlaps with the AI part of my talk. When failures are in the signature, reviewers can start from the contract instead of doing archaeology in the implementation. Did the error union change? Is API misuse being dressed up as a domain error? Does the new failure map to an HTTP response? You can answer all of these from the interface alone, before opening the function body.

He writes that this agility matters even more now that agents draft a large share of the code. When a model writes the implementation, an explicit contract is the cheapest way to check that it's correct. You read the type, not the 200 lines underneath. If you write "don't throw expected failures; return a typed error union" in your agent instruction file, the model will mostly follow it, but the way to verify that is to read the contract, not to trust the prose. On his team, he says, this isn't personal taste but a shared default. The contract is the unit of review, and the bar doesn't drop just because the implementation was generated. Deciding which failures a function can produce is a judgment call, and the signature is where that judgment gets written down, so the next person, or the next agent, has to respect it. As he puts it, the signature is where ownership lives.

In the talk I'd said "docs are the map, types are the contract," and here was a server-side practitioner writing nearly the same sentence. Writing something in a rules file makes it easier to discover; a signature enforces it. He doesn't hide the honest trade-offs either: more types, more mapping code, longer signatures. But that complexity was always there. The signing window could always close, and the code could always be wrong. This approach just moves that complexity out of the implementation, where it was hiding, and into types that are named, tested, and visible. It's the same conclusion I reached looking at Signal's 125 sealed types while preparing the talk: it's a cost they were already paying.

What I took away from this post wasn't a conviction that we have to adopt Rich Errors. It was **a standard for making contracts more precise even before the new syntax arrives**, along with a clear sense of exactly where the cost of meeting that standard comes from today. My talk was about how much the language can lower that cost. This post was the practical link that says not to put off that judgment just because today's tools are what we have. Its last line, roughly: a signature that returns `Unit` while secretly throwing is lying about what it does, so make your signatures tell the truth.

## Wrapping up

Before I prepared this talk, I saw Rich Errors as a convenience feature that trims success wrappers and repeated declarations. After reading the spec, the discussions, and real app code side by side, I think what matters more isn't how many lines it saves but **how much easier it gets to keep an accurate failure contract**.

`null` concisely expresses an absence when the reason doesn't matter. Sealed types and Arrow can model failure clearly today. `Result` is useful for carrying either a success or an exception. Rich Errors builds on the problems these tools have already solved and tries to bring the expression and composition of failure into Kotlin's syntax and type system. In return come constraints around inheritance, type inference, and compatibility. There are also open disagreements, like traceability, over how far safety should be enforced. So for now, I think it's better to look at the expected benefits and the open problems together than to rush into being for or against it. That's why concrete examples from real code help so much in these discussions. And once it's done, there'll be a hundred tutorials, but now is the only time you can take part in the design.

For me personally, the most practical homework I came away with was finding that my rules files had no standard for error handling. Even if you can't use Rich Errors yet, starting with the next function you write, you can write down "when this fails, what should the caller do?" first. You don't need a new keyword to answer that.

![The function returns](images/50.webp){width=100 align=center}

Back to the original question: what happens when this function fails? For 15 years we've answered with `null`, with `Result`, and with a sealed class for every screen. On the last line, nobody has to answer, because the signature already does. And it's no longer only people reading that line.

The more implementation AI takes on, the clearer it becomes what developers need to define: what counts as success, which failures to tell apart, where to stop, and where to retry. Before a good type is a constraint for the compiler, it's compressed domain knowledge that people and AI read together.

> AI can only implement success once you've defined failure.

### TL;DR

- After dropping checked exceptions, Kotlin went 15 years without dedicated syntax for putting failures in the return type. `null`, sealed types, `Result`, and Arrow were all workarounds each of us pieced together.
- Rich Errors puts failures in the return type with three pieces of syntax: `error`, `|`, and `|.`. Giving up inheritance and generics buys wrapper-free representation and polynomial-time type inference.
- You can't use it yet. The issue tracker targets 2.5.0, but the spec is still in Public discussion, and it changed significantly just two months ago.
- Before any new syntax, the first job is to split failures by what the caller does next and write that into the contract. The practitioner's post on the JetBrains blog reaches the same conclusion using sealed types and `Either`.

## References

- [KEEP-0441 · Rich Errors Motivation and Rationale](https://github.com/Kotlin/KEEP/blob/main/proposals/KEEP-0441-rich-errors-motivation.md)
- [KEEP-0462 · Rich Errors Design Proposal](https://github.com/Kotlin/KEEP/blob/main/proposals/KEEP-0462-rich-errors.md)
- [KEEP Discussion #487](https://github.com/Kotlin/KEEP/discussions/487)
- [Design Notes 0009 · Evaluating Rich Errors in the Wild](https://github.com/Kotlin/KEEP/blob/main/notes/0009-rich-errors-use-cases.md)
- [KT-68296 · Union Types for Errors](https://youtrack.jetbrains.com/issue/KT-68296)
- [KT-7128 · Multi-catch](https://youtrack.jetbrains.com/issue/KT-7128)
- [JetBrains Kotlin Blog · Signatures, be true: domain errors and functional handling in Kotlin (Sergey Chernov)](https://blog.jetbrains.com/kotlin/2026/08/signatures-be-true-domain-errors-and-functional-handling-in-kotlin/)
- [Kotlin · Exception and error handling](https://kotlinlang.org/docs/exceptions.html)
- [Kotlin · Result API](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-result/)
- [Kotlin · runCatching API](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/run-catching.html)
- [kotlinx.coroutines · withTimeoutOrNull](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/with-timeout-or-null.html)
- [Kotlin · Cancellation and timeouts](https://kotlinlang.org/docs/cancellation-and-timeouts.html)
- [Arrow · From Either to Raise](https://arrow-kt.io/learn/typed-errors/from-either-to-raise/)
- [Arrow · Working with typed errors](https://arrow-kt.io/learn/typed-errors/working-with-typed-errors/)
- [Anthropic · Prompting Claude Opus 5](https://platform.claude.com/docs/en/build-with-claude/prompt-engineering/prompting-claude-opus-5)
