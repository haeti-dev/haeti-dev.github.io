---
title: Kotlin inline functions and crossinline
summary: What `inline` does to a function call, when it's worth using, and why `crossinline` blocks non-local returns from a lambda.
source: 496e86749699
---

## How is `inline fun` different from `fun`?

> It comes down to how each one is handled at compile time.

A regular `fun` runs its code when it's called, and each call pushes a new function frame onto the call stack.

With `inline fun`, the compiler copies the function body straight into the call site, so there's no function call overhead.

- What's overhead?

  Function call overhead is the extra processing time and memory it takes to call a function.

  When you call a function, the computer stores its parameters, local variables, return address, and so on in the stack, a dedicated region of memory. That information stays there until the function finishes and is removed from the stack when it returns. All of this costs CPU time and memory, and that cost is what we call function call overhead.

  On top of that, every call moves execution into the function, and execution has to jump back to where it came from when the function ends. This context switching takes extra time too.

`inline fun` really pays off with lambdas. A lambda normally becomes a function object, which costs extra memory and time. Pass a lambda to an `inline fun`, though, and the compiler inserts the lambda's code directly at the call site, which cuts that overhead.

### The downside

> What happens if you declare every function as `inline fun`?

Because the body gets copied into every call site, a function that's called in many places can grow your code size by that much, and that affects the app's performance and memory usage.

### Bottom line

> **Use `inline fun` mainly for small functions and for higher-order functions that take lambdas as parameters.**

## What's crossinline?

`crossinline` is a Kotlin keyword that forbids non-local returns from a lambda used inside an `inline` function.

Normally, when you use a lambda inside an `inline` function, a `return` inside that lambda is a **non-local return**. In other words, `return` exits not just the lambda but also the `inline` function that contains it.

Non-local returns can sometimes cause behavior you didn't want. For example, if the function running the lambda controls important flow, like a loop or an outer function, a `return` in the lambda can get in the way of that control flow.

That's where `crossinline` comes in: it forbids the `return` keyword inside that lambda. A `crossinline` lambda can only do a local return, which ends the lambda itself while the rest of the function keeps running.

So `crossinline` helps you use `inline` functions and lambdas more safely.

### Bottom line

> **`crossinline` makes non-local returns impossible inside the function.**
