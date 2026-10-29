# English dev-blog style guide

Distilled from a close read of posts by Jake Wharton, Chris Banes, Zac Sweers, Jesse Wilson,
Cash App engineers, Manuel Vivo, Marcin Moskała, Dan Abramov,
Josh W. Comeau, Julia Evans, the Android Developers Blog, and the Kotlin Blog. Bracketed tags
like [JW-1] point to the source list at the end. Quotes illustrate patterns; don't copy them
into translations.

Korean-specific pitfalls live in `ko-en-phrasebook.md`.

## Contents

- TL;DR: the ten rules that matter most
- 1. Voice and stance (first person, opinions, humor, contractions, tense)
- 2. Openings and endings
- 3. Sentence and paragraph rhythm (headings, lists vs prose)
- 4. Technical explanation moves (code lead-ins, APIs inline, trade-offs, numbers, hedging)
- 5. Natural phrases table (OK / Sparingly / Avoid)
- 7. AI and machine-translation tells
- 8. Titles and summaries
- Sources

---

## TL;DR: the ten rules that matter most

1. **Write as a person.** Say "I" for your own experience and "we" only for a real team. Talk to the reader as "you". Use contractions.
2. **Start with the point, the problem, or a confession.** Never start with "In this article, we will…".
3. **End on a takeaway, a recommendation, or an open question.** Never end with "In conclusion" or a bulleted recap of what was "explored".
4. **Use active voice with a named actor.** Turn "~하게 되었다" into "we switched", "I found", or "R8 removes".
5. **Vary sentence length on purpose.** Short sentences carry the punchlines ("Problem solved." [DA]), and medium ones carry the explanation.
6. **Introduce code in plain words:** "Here's…", "Consider…", "Let's look at an example:". Then say what to notice.
7. **Hedge with real uncertainty, and only where it exists:** "I'm pretty sure", "I think", "as far as I can tell". Don't pile up "can" and "may".
8. **Give numbers with a baseline and conditions:** "~20% improvement in scroll performance" [ADB], "p50 … 30 minutes" → "10 minutes" [CASH-2].
9. **Cut intensifiers and nominalizations.** "Very significantly improved the performance" becomes "made it 3x faster".
10. **Avoid the AI register:** "delve", "leverage", "seamless", "robust", "It's important to note", "Let's dive in", and the em dash character (never use it).

---

## 1. Voice and stance

### First person
- **"I" is the default on personal blogs.** Jake Wharton opens a post with "I really like Jetpack Compose." [JW-1]. Chris Banes writes "I've been under the impression…" and "I wrote a benchmark" [CB-2]. Julia Evans uses "I" in nearly every paragraph [JE-1, JE-2]. Zac Sweers calls Metro "the proudest work of my career" [ZS-1].
- **"We" means a specific team.** Cash App posts use "we" for the Cash team: "we had a single service powering all backend…" [CASH-2], and "the Cash App Android team" with "we/our" [JW-3]. Corporate blogs use "we" for the company: "Today we are excited to release Kotlin 2.0" [KB], "we're releasing version 1.6" [ADB].
- **"We" as "you and me working through this"** is a teaching voice. Moskała uses "lets us decide which thread" [MM], and Abramov writes "Or can we?" [DA]. That's fine in tutorials. It is **not** a stand-in for an author who doesn't want to say "I". On a solo post, a Korean "저희는" usually becomes "I", or "my team" when the work really was shared.

### Expressing opinion and uncertainty
- Opinions are stated plainly and owned:
  - "one of the hills I'll die on" [JW-1]
  - "I don't personally like compiler flags that change the runtime behavior." [JPW]
  - "Spotless… are frankly too heavy-weight" [ZS-2]
  - "I'd recommend replacing all your references to CoroutineDispatcher…" [CASH-1]
- Uncertainty is stated directly and only where it's real. Julia Evans writes "I'm pretty sure", "I believe", "not sure about this one", and "I haven't used zdiff3" [JE-2].
- Revising your own view is normal and makes you more credible. Chris Banes tells the reader the post began as "an ode to Sequence" and then turned into "Eating humble pie 🥧" once the benchmark disagreed [CB-2]. Julia Evans writes "I couldn't have been more wrong" [JE-1].
- Recommendations come as imperatives or "I'd recommend…". Manuel Vivo's dek is a single imperative: "Use the `collectAsStateWithLifecycle` API to collect flows…" [MV]. Official blogs say "the recommended way to…" [MV] or "we do not consider it ready for production usage yet" [ADB].

### Humor
- It's light, dry, and occasional. Examples: "This seems like a boring change… Unfortunately it wasn't!" [JPW], "Can't avoid memo this time, right? Or can we?" [DA], "Hell, name it Crane" [JW-1], and Comeau's 😅 after admitting he didn't understand re-renders [JC].
- Self-deprecation does better than jokes. Nobody uses humor to fill space.
- Emoji show up in some personal blogs (Banes uses them in headings, Comeau uses them inline). Official and company blogs don't use them. **Default to none.**

### Addressing the reader
- "You" is everywhere: "If you're migrating…" [JPW], "you might create something like this:" [CB-1], "If you ask a group of React developers…" [JC].
- Rhetorical questions are a common way to address the reader: "So what should we do instead?" [CB-1], "But is that actually true?" [CB-2], "Wondering what's next?" [ADB], "New here?" [ZS-1].

### Contractions
- They're standard on personal and company blogs: it's, don't, isn't, we're, you'll, I'd [JW-1, DA, JC, JE-1, CB-2, ZS-1].
- Official Google/JetBrains posts use fewer of them but still write "we're" and "we've" [ADB, KB].
- Writing with **no** contractions reads as stiff or translated. The Colin White post was noticeably more formal [CASH-1]. Either register is acceptable, but for a personal blog voice, use contractions.

### Tense in narratives
- **What happened:** simple past, active voice, with a named actor. "we realized", "we refactored" [CASH-2], "I then wrote a quick benchmark test" [CB-2], "One of our users reported…" [JPW].
- **How things work:** present tense. "R8 adds optimization passes over the IR…" [JW-2], "Compose synchronously recomposes when first initialized" [JW-3].
- **Present perfect for the state now:** "Metro's come a long way since…" [ZS-1], "I've been under the impression…" [CB-2].
- Avoid the Korean habit of putting everything in a resultative past ("~하게 되었다"). Describe mechanisms in the present and events in the past.

---

## 2. Openings and endings

### Opening hook types seen

| Hook type | Example | Source |
|---|---|---|
| Personal stance | "I really like Jetpack Compose." It then pivots to a concern. | [JW-1] |
| Confession or relatable struggle | Worked with React for years "without really understanding" re-rendering | [JC] |
| Scene or anecdote | "I was talking to Jemma recently about what stops people from blogging." | [JE-1] |
| Wish or frustration | "I always wish that command line tools came with data about…" | [JE-2] |
| Thesis as an aphorism | "Formatting should be boring, in the best possible way." | [ZS-2] |
| Context that sets up a twist | OkHttp migrated to Kotlin and "our assertions migrated too", then a bug | [JPW] |
| History, then problem | Cash had a single backend service, which became a slow monolith | [CASH-2] |
| Framing the landscape | "In Kotlin, we're spoilt for choice in how we model collections…" | [CB-2] |
| Definition-first (reference style) | "Dispatchers.Unconfined is one of kotlinx.coroutines' built in CoroutineDispatchers." | [CASH-1] |
| Announcement | "Today, as part of the Compose January '24 Bill of Materials, we're releasing…" | [ADB], [KB] |
| Series note | "Note: This post is part of a series on D8 and R8…" | [JW-2] |

What none of them do: announce a table of contents ("이번 글에서는 A, B, C를 알아보겠습니다"). If a roadmap is needed, keep it to one plain sentence: "In this post, I want to share two different techniques." [DA]. Note that this is "I want to share", not "we will explore".

### Ending moves seen
- **Restate the recommendation as one actionable line.** "If you're building an Android app with Jetpack Compose, use `collectAsStateWithLifecycle`…" [MV]. "I'd recommend replacing all your references…" [CASH-1].
- **Give the moral, or put it in perspective.** "This is not a new idea…" and the observation "deserves a bit more love" [DA]. Comeau warns "Don't over-optimize!" [JC].
- **Ask an open question about the future.** "Is this the final form in the evolution of how we manage state?" [JW-3].
- **Make a personal plea.** Wharton ends by urging Google not to "relegate this amazing, general-purpose, multiplatform compiler and runtime" [JW-1].
- **State the payoff.** "With faster builds developers are able to spend more time…" [CASH-2].
- **Say what's next or tease the next post.** "Stay tuned for the next post…" [JW-2]; "What's Next?" [ZS-1].
- **Thank people by name.** "Thanks to Jemma, Kamal…" [JE-1]; "Thank you to everyone that's contributed…" [ZS-1].
- **Sign off (official blogs only).** "Happy composing!" [ADB]; "Have fun!" [KB].
- **Hope it helped (use sparingly; it's the most generic ending).** "Hopefully this blog post has been useful…" [CB-1].

Nobody in the sample wrote "In conclusion". Moskała has a "Summary" section, but that's book-chapter style [MM]. **Translate 마치며/결론 as a descriptive heading, or drop the heading entirely.** Good options are "Wrapping up", "Takeaways", "Was it worth it?", or "Lessons learnt" [CB-2]. Often the last section just ends with no heading at all.

---

## 3. Sentence and paragraph rhythm

- **Paragraphs are usually 2–5 sentences** in every source. One-sentence paragraphs are used for emphasis: "Problem solved." [DA], "Not all is lost!" [JE-1], "So here's me eating humble pie" [CB-2].
- **Sentences mostly run 12–22 words**, with short ones mixed in on purpose. Wharton follows a long paragraph with "It's great!" [JW-1]. Zac Sweers puts "Metro 1.0.0 is out now and stable" on its own line [ZS-1].
- **Prose carries the reasoning, and lists carry inventories.** Feature lists, config options, and changelogs go in bullets [ZS-1, ADB, JE-2]. Explanations and trade-offs stay in prose. Translated Korean posts tend to bullet everything, including arguments, and that reads as notes, not writing.
- **Headings are sentence case**, short, and often conversational:
  - Sentence case is the norm: "Under the hood", "Flow collection in your architecture" [MV]; "Configuring the stability of external classes" [ADB]; "Multiplatform from the ground up", "Migrate with confidence" [KB]; "What's in the box", "By the numbers" [ZS-1].
  - Question headings are common: "What is the 'Slot' API pattern?", "Should you use this for your in-house components?" [CB-1]; "What's the moral?" [DA]; "What about context?" [JC]; "Why Is It So Slow Anyway?", "Can We Throw Money at the Problem?" [CASH-2].
  - Claim headings: "It's not about the props" [JC]; "Benchmark Results: Sequences can be slow" [CB-2]; "There's a better way" [CASH-1].
  - Verb-first headings: "Putting the pieces together" [CB-1]; "Removing Dead Code" [CASH-2]; "Get started!" [ADB].
  - Playful headings: "Enter Molecule" [JW-3]; "Eating humble pie 🥧" [CB-2].
  - Julia Evans writes whole headings in lowercase as full sentences, such as "write it down while you still remember what was hard" [JE-1]. That's her signature and not a general norm.
- **Title Case for headings** shows up on some company blogs [CASH-2, SD]. It's not wrong, but sentence case is more common on dev blogs today. Pick one and stay consistent.

---

## 4. Technical explanation moves

### Introducing code
Real lead-ins from the sample:
- "Here's a component with a severe rendering performance problem:" [DA]
- "Here's what it looks like:" / "Here's how this would affect the re-renders:" [JC]
- "Let's look at an example:" [JC]; "let's look at the actual `TopAppBar` implementation:" [CB-1]
- "As a quick example, consider this:" [JC]
- "you might create something like this:" [CB-1]
- "The following code structure is probably something which you've written hundreds of times:" [CB-2]
- "Now let's add in a Sequence'd version." [CB-2]
- "…is equivalent to this source code:" [JPW]
- "It's pretty easy to verify this from its (simplified) implementation:" [CASH-1]
- "To see this dispatcher in action, run the following code:" [MM]
- "Here's what it looks like in `~/.gitconfig`" [JE-2]
- "First and foremost, what does it look like?" [JW-3]
- "dumping the bytecode shows…" [JW-2]

Patterns to follow:
- Use one short lead-in ending with a colon. Then show the code. Then say **what to notice**: "Notice that…", "The key line is…", "This line will crash!" [CASH-1].
- Use "Consider…" for a hypothetical setup and "Here's…" for real or final code.
- Avoid "The code is as follows" and "as demonstrated in the example below". They're correct English but stiff.

### Referring to APIs and types inline
- Put the identifier in backticks and use it as a noun: "`collectAsStateWithLifecycle` stops collecting when…" [MV].
- Add a type word only the first time or when it's ambiguous: "the `collectAsStateWithLifecycle` API", "the `repeatOnLifecycle` API", "the … composable function" [MV]; "the nested `Companion` class" [JW-2].
- Pluralize and possessivize naturally: "kotlinx.coroutines' built in CoroutineDispatchers" [CASH-1], "a Sequence'd version" [CB-2].
- Don't write "the `remember` function function" or "the `LazyColumn` composable component". Pick one type word, or use none.

### Trade-offs
- Say the cost directly: "that power comes with an associated cost" [JW-2]; "there are some tradeoffs in how we're using Compose" [JW-3].
- Name what you're optimizing for: "It depends on what we care about more" [MM].
- Use "The biggest problem with this approach is…" [MM] and "The reason this is exciting…" [JW-3].
- Show a rejected option with its reason: "Could we speed up the build just by paying for more test workers? We quickly realized that this was extremely inefficient" [CASH-2].
- Give your preference after the facts: "in my experience leaning on the side of flexibility tends to lead to fewer future problems" [CB-1].

### Performance numbers
- Give a relative change plus the metric: "~20% improvement in scroll performance", "~12% improvement to startup time" [ADB]; "about 9% slower", "45% slower" [CB-2]; "compiles it twice as fast" [KB].
- Give before and after, with percentiles if you have them: "p50 build time was 30 minutes and p99 was 45 minutes" → "p50 … 10 minutes and p99 to 22 minutes" [CASH-2].
- Approximate honestly: "took a bit more than two seconds" [MM]; "upwards of 50-80%" [ZS-1].
- Give the context or baseline: "on top of the improvements from the August '23 release" [ADB].
- Caveat the method: Banes tells readers to "take this one with a pinch of salt" and later adds "Update 1"/"Update 2" sections with new data [CB-2]. Corrections are added as dated updates, not edited away silently.
- Don't write "performance was greatly/significantly improved". Give the number, or say "noticeably faster" and explain how you noticed.

### Hedging
- Hedge once, and attach it to the specific claim: "I'm pretty sure…", "I believe…" [JE-2]; "as far as I can tell".
- Use "might/may" only for real possibility: "you might be thinking…" [JE-1], "This could change in the future!" [JC].
- Don't hedge facts you verified. If you benchmarked it, say "Sequences were 45% slower in my benchmark". Don't write "it can be seen that sequences may be slower".

### Explaining a mechanism
- Lead with the plain idea, then the detail: "In a nutshell, it implements the idea of components having a single responsibility." [CB-1]
- Name a misconception and knock it down: Comeau labels "Big Misconception #1" and writes "Let's clear away…" [JC].
- Put "Under the hood" in a section heading when the post moves from usage to internals [MV].

---

## 5. Natural phrases and collocations dev writers actually use

Status key: **OK** means natural and fine to use. **Sparingly** means natural but noticeable if repeated, so use it at most once per post. **Avoid** means cliché or AI-marked.

| Situation | Natural English | Status | Seen in / notes |
|---|---|---|---|
| Result was unexpected | "It turns out (that)…" | OK | Very common across blogs; "it turned out that…" [JE-1] |
| Explaining internals | "under the hood" | Sparingly | Heading in [MV] |
| Downside of an approach | "The catch is…" / "The problem is…" | OK | "The biggest problem with this approach is" [MM] |
| A bug that hurt you | "This bit me when…" / "This bit us in production" | OK | Informal; good for 삽질 stories |
| Works with no setup | "out of the box" | Sparingly | "React is already very optimized out of the box" [JC] |
| Mild caveat | "Note that…" | OK | Shorter than "It's worth noting" |
| Caveat with emphasis | "It's worth noting that…" | Sparingly | Fine once; repetition is an AI tell |
| Formal caveat | "It's important to note that…" | Avoid | Occurs [JW-2, SD] but it's the #1 LLM tell; prefer "Note that" |
| Summarizing the idea | "In a nutshell, …" | Sparingly | [CB-1] |
| Getting to the point | "Here's the thing:" | Sparingly | [JC] |
| Honest admission | "I'll be honest, …" / "To be honest, …" | Sparingly | [JC] |
| Introducing code | "Here's what that looks like:" | OK | [JC], [JE-2] |
| Hypothetical setup | "Consider the following…" / "Say you have…" | OK | [JC] |
| Walking through | "Let's look at an example." | OK | [JC], [CB-1] |
| Rhetorical pivot | "So what should we do instead?" | OK | [CB-1] |
| Testing an assumption | "But is that actually true?" | OK | [CB-2] |
| Something looked simple | "This seems like a boring change, but…" | OK | [JPW] |
| Being wrong | "I couldn't have been more wrong." | OK | [JE-1] |
| Admitting a mistake | "eating humble pie" | Sparingly | [CB-2] |
| Strong opinion | "a hill I'll die on" | Sparingly | [JW-1] |
| Mild opinion | "I'm not a fan of…" / "I don't personally like…" | OK | [JPW] |
| Recommending | "I'd recommend…" / "I'd suggest…" | OK | [CASH-1] |
| Easy decision | "a no-brainer" | Sparingly | [MV] |
| Not the main goal | "the cherry on top, not the end goal" | Sparingly | [DA] |
| Deferring detail | "too extensive to detail here" / "That's a topic for another post." | OK | [JW-3] |
| Something needs explaining | "that requires some explaining" | OK | [JW-1] |
| Not underrated | "deserves a bit more love" | Sparingly | [DA] |
| Rough approximation | "a bit more than two seconds" / "roughly 2x" | OK | [MM] |
| Uncertain belief | "I'm pretty sure…" / "as far as I can tell" | OK | [JE-2] |
| Taking a claim lightly | "take this with a pinch of salt" | Sparingly | [CB-2] (UK "pinch", US "grain") |
| Making things worse | "a sneakier version of this problem" | OK | [JC] |
| Surprising | "surprisingly basic" / "fairly counter-intuitive" | OK | [DA], [JC] |
| Saving time | "shaved several minutes off our builds" | OK | [CASH-2] |
| Bottleneck | "in the critical path" | OK | [CASH-2] |
| Rejected solution | "We quickly realized that this was…" | OK | [CASH-2] |
| Enables something | "unlocks…" | Sparingly | [JW-3], [DA]; overuse turns into marketing talk |
| Default choice | "reach for X" / "my go-to" | OK | Common dev idiom |
| Plain answer | "The answer is remarkably plain:" | OK | [DA] |
| Explaining why | "The reason this matters is…" | OK | [JW-3] "The reason this is exciting" |
| Fix landed | "The fix was a one-liner." | OK | Natural for 트러블슈팅 |
| Narrowing down a bug | "I narrowed it down to…" / "tracked it down to…" | OK | |
| Reproducing a bug | "I could reproduce it reliably by…" | OK | |
| Root cause | "The root cause was…" / "The culprit was…" | OK | "culprit" is informal but common |
| Workaround | "As a workaround, …" / "a stopgap" | OK | |
| Bad design | "a footgun" | Sparingly | Informal |
| Boilerplate | "a lot of ceremony" / "boilerplate" | OK | |
| Getting started | "If you're new to X, …" / "New here?" | OK | [ZS-1] |
| Future note | "This could change in the future." | OK | [JC] |
| Call to action | "Give it a try and let me know what breaks." | OK | |
| Official sign-off | "Happy composing!" / "Have fun!" | Sparingly | [ADB], [KB]; corporate only |
| Fancy verb | "delve into" | Avoid | Strong AI tell |
| Opening line | "Let's dive in!" / "Let's dive deep" | Avoid | AI/marketing tell |
| Opening line | "In this article, we will explore…" | Avoid | Translationese + AI tell |
| Fancy verb | "leverage" (as a verb) | Avoid | Use "use". It occurs in [CASH-2], but it's corporate-speak |
| Fancy verb | "utilize" | Avoid | Always "use" |
| Adjective | "seamless(ly)", "robust", "powerful" | Avoid | Marketing register; [KB] uses "powerful", which suits official copy only |
| Adjective | "crucial", "pivotal", "vital" | Avoid | Use "important", or better, explain why |
| Wrap-up | "In conclusion," / "To sum up," | Avoid | Not found in any personal blog sampled |
| Wrap-up | "I hope this article was helpful" | Sparingly | Generic; [CB-1] does a version once |
| Intro filler | "As we all know," | Avoid | Condescending and empty |
| Emphasis | "game-changer" / "next level" | Avoid | Hype |

---

## 7. Things that mark text as machine-translated or AI-written

**Lexical tells.** Replace or delete these.
- "delve (into)", "dive (deep) into", "Let's dive in"
- "leverage", "utilize", "facilitate", "harness", "empower" ([KB]'s "empowering you" is fine for official copy only)
- "seamless(ly)", "robust", "powerful", "cutting-edge", "comprehensive", "crucial", "pivotal", "vital", "paramount"
- "a testament to", "in the realm of", "the landscape of", "navigate the complexities", "unlock the full potential", "elevate"
- "It is important to note", "It's worth noting" (repeated), "Notably,", "Importantly,", "Furthermore,", "Moreover,", "Additionally," (stacked)
- "In this article, we will…", "In conclusion,", "To sum up,", "I hope this article was helpful."
- "Whether you're a beginner or an experienced developer…"

**Structural tells.**
- **Em dashes.** Never use the em dash character (U+2014). It is the most recognizable AI tell. Use commas, colons, parentheses, or split the sentence.
- **Groups of three.** "fast, reliable, and scalable", over and over. Real writers give one concrete reason.
- **"Not just X, but Y" / "It's not X, it's Y"** as a default pivot. [JC] uses "It's not about the props" once, as a real misconception.
- **Every section ends with a moral.** "This ensures…", "This allows developers to…" Real posts move on.
- **Bullet lists for reasoning, with bold lead-ins on every bullet** ("**Performance**: …"). Use them for feature inventories only.
- **Uniform paragraph length.** Every paragraph has 3 sentences of 20 words. Vary it [DA, JC, JE-1].
- **Section-intro rituals.** "Now, let's take a look at…" before every heading.
- **Over-formatted "Key takeaways" box** at the end that repeats the headings.

**Grammar and translationese tells, especially from Korean.**
- Stacked nominalizations: "the performance optimization implementation of the recomposition reduction". Write "how we cut recompositions" instead.
- Agentless passives: "It was decided that…", "It was found that…", "It became possible to…"
- "can" and "may" on every verb, which comes from ~할 수 있다.
- Missing or misused articles ("We used Compose compiler metrics" vs "the Compose compiler's metrics"). Check them, because missing articles are the fastest give-away of non-native text.
- "the reason why… is because…"
- "very very", "so much", "greatly", "significantly" with no number
- Overusing "we" in a solo post
- "In case of X" (→ "For X" / "With X")
- "make a function", "do an optimization", "proceed the migration" (→ "write a function", "optimize", "migrate")
- Countable "informations", "feedbacks", "codes" (→ "information", "feedback", "code")
- "As you know" or "As we all know" as an opener

**Tone tells.**
- Unrelenting enthusiasm ("amazing", "incredible") on every page. Wharton uses "amazing" once, in a closing plea [JW-1], and it lands because it's rare.
- No opinions. Every claim is hedged equally, so the reader can't tell what the author actually believes.
- No specifics: no numbers, file names, versions, dates, or names of colleagues.

---

## 8. Titles and summaries (deks)

### What titles look like in the sample
| Title | Style | Source |
|---|---|---|
| A Jetpack Compose by any other name | Idiom/pun, sentence case | [JW-1] |
| The state of managing state (with Compose) | Wordplay plus a parenthetical, sentence case | [JW-3] |
| R8 Optimization: Staticization | Series: topic | [JW-2] |
| Slotting in with Compose UI | Pun on the API name | [CB-1] |
| Should you use Kotlin Sequences for Performance? | Reader question | [CB-2] |
| Kotlin's Assert Is Not Like Java's Assert | Surprising claim, Title Case | [JPW] |
| 🚇 Metro is Stable | Announcement | [ZS-1] |
| Introducing Kempt | Announcement | [ZS-2] |
| Before You memo() | Advice with an API name | [DA] |
| Why React Re-Renders | "Why X" explainer | [JC] |
| Blog about what you've struggled with | Imperative | [JE-1] |
| Popular git config options | Plain descriptive noun phrase | [JE-2] |
| Consuming flows safely in Jetpack Compose | Gerund plus outcome | [MV] |
| Dispatchers.Unconfined and why you actually want EmptyCoroutineContext | API plus contrarian twist | [CASH-1] |
| From 30 Minutes Down to 10: Taming our Monolith's CI Build Times | Number result: topic | [CASH-2] |
| Mocking isn't evil, but avoid it anyway | Opinion | Cash App index |
| Cash Android Moves to Metro | News-style statement | Cash App index |
| What's new in the Jetpack Compose January '24 release | Official release note | [ADB] |
| Celebrating Kotlin 2.0: Fast, Smart, and Multiplatform | Official announcement plus three adjectives | [KB] |

**Conventions**
- **Length:** usually 3–9 words. Long titles split with a colon (outcome: topic).
- **Casing:** sentence case dominates personal blogs [JW-1, JW-3, CB-1, JE-1, JE-2, MV] and official Google/JetBrains headings. Title Case appears on some company and Medium posts [CASH-2, JPW, DA]. Both are acceptable, so choose one per blog and keep it.
- **Strong title types:** a specific outcome with a number ("From 30 Minutes Down to 10"), a question the reader has ("Should you use…?"), a "Why X does Y" explainer, a contrarian claim ("X isn't evil, but avoid it anyway"), "How we…/How I…", or wordplay on the API name.
- **Keep code identifiers exact** in titles (`memo()`, `Dispatchers.Unconfined`).
- **Avoid:** "[Android]" bracket tags, "A Complete Guide to…" or "Everything You Need to Know" (fine for SEO, weak for blogs), "삽질기"-style "Diary of…", and generic "Jetpack Compose Performance Optimization".
- **Translating Korean titles.** Rebuild them around the outcome or the question:
  - "Compose 리컴포지션 최적화 적용기" → "How we cut recompositions in our feed by half" or "Fixing a recomposition storm in Compose"
  - "Hilt에서 Metro로 마이그레이션 회고" → "Moving from Hilt to Metro: what went well and what didn't"
  - "StateFlow vs SharedFlow 정리" → "StateFlow or SharedFlow? A practical guide" or "When to reach for SharedFlow"

### The one-line summary (dek or subtitle)
- **One sentence, 12–25 words**, with an imperative or a direct claim, aimed at the reader. Manuel Vivo's dek is the model: "Use the `collectAsStateWithLifecycle` API to collect flows in a lifecycle-aware manner from your UI." [MV]
- Other patterns:
  - Outcome: "We cut p50 CI time from 30 to 10 minutes. Here's what worked and what didn't."
  - Question → answer: "Sequences are supposed to be faster. My benchmarks say otherwise."
  - Problem → fix: "`Dispatchers.Unconfined` breaks a key coroutine guarantee; `EmptyCoroutineContext` doesn't."
- **Don't** start with "This article explains…" or "In this post, I will…". **Don't** pack three buzzwords into it.

---

## Sources

- [JW-1] Jake Wharton, "A Jetpack Compose by any other name" (2020-12-30). https://jakewharton.com/a-jetpack-compose-by-any-other-name/
- [JW-2] Jake Wharton, "R8 Optimization: Staticization" (2018-12-11). https://jakewharton.com/r8-optimization-staticization/
- [JW-3] Jake Wharton, "The state of managing state (with Compose)", Cash App Code Blog (2021-11-11). https://code.cash.app/the-state-of-managing-state-with-compose
- [CB-1] Chris Banes, "Slotting in with Compose UI" (2021-12-06). https://chrisbanes.me/posts/slotting-in-with-compose-ui/
- [CB-2] Chris Banes, "Should you use Kotlin Sequences for Performance?" (2025-02-13). https://chrisbanes.me/posts/use-sequence/
- [ZS-1] Zac Sweers, "🚇 Metro is Stable" (2026-04-27). https://www.zacsweers.dev/metro-is-stable/
- [ZS-2] Zac Sweers, "Introducing Kempt" (2026-06-22). https://www.zacsweers.dev/introducing-kempt/
- [JPW] Jesse Wilson, "Kotlin's Assert Is Not Like Java's Assert" (2019-11). https://publicobject.com/2019/11/18/kotlins-assert-is-not-like-javas-assert/
- [CASH-1] Colin White, "Dispatchers.Unconfined and why you actually want EmptyCoroutineContext", Cash App Code Blog (2025-01-15). https://code.cash.app/dispatchers-unconfined
- [CASH-2] Yissachar Radcliffe, "From 30 Minutes Down to 10: Taming our Monolith's CI Build Times", Cash App Code Blog (2023-10-12). https://code.cash.app/taming-the-monolith
- Cash App Code Blog index (titles only: "Mocking isn't evil, but avoid it anyway", "Cash Android Moves to Metro"). https://code.cash.app/
- [MV] Manuel Vivo, "Consuming flows safely in Jetpack Compose" (2022-08-10). https://manuelvivo.dev/consuming-flows-compose (also on the Android Developers Medium publication)
- [MM] Marcin Moskała, "Kotlin Coroutines dispatchers", Kt. Academy. https://kt.academy/article/cc-dispatchers
- [ADB] Ben Trengrove, "What's new in the Jetpack Compose January '24 release", Android Developers Blog (2024-01-24). https://android-developers.googleblog.com/2024/01/whats-new-in-jetpack-compose-january-24-release.html
- [KB] Egor Tolstoy, "Celebrating Kotlin 2.0: Fast, Smart, and Multiplatform", Kotlin Blog (2024-05). https://blog.jetbrains.com/kotlin/2024/05/celebrating-kotlin-2-0-fast-smart-and-multiplatform/
- [DA] Dan Abramov, "Before You memo()" (2021-02-23). https://overreacted.io/before-you-memo/
- [JC] Josh W. Comeau, "Why React Re-Renders" (2022-08-16, updated 2025). https://www.joshwcomeau.com/react/why-react-re-renders/
- [JE-1] Julia Evans, "Blog about what you've struggled with" (2021-05-24). https://jvns.ca/blog/2021/05/24/blog-about-what-you-ve-struggled-with/
- [JE-2] Julia Evans, "Popular git config options" (2024-02-16). https://jvns.ca/blog/2024/02/16/popular-git-config-options/

**Method notes.** Posts were fetched with a summarizing web fetcher and analyzed for style, not content. Quotes are short excerpts as returned, so check one against the live page before quoting it anywhere public. Medium-hosted originals returned 403, so Manuel Vivo's post was read on his personal mirror. The Zac Sweers posts ended in a site-wide tagline that the fetcher mistook for the closing, and it was excluded. The "Status" ratings in section 5 and the tells in section 7 combine what the sample showed with general editorial judgment. Where a phrase appears in a source but is still flagged, the table says so.
