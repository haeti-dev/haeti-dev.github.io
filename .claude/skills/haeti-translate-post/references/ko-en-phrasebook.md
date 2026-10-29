# Korean → English phrasebook

Korean discourse habits that survive literal translation, and what a native dev writer would
write instead. Read this alongside `style-guide.md`. Section numbers match the original research
note (the style guide holds sections 1–5, 7, 8).

## 6. Korean→English pitfalls

Literal translation keeps Korean discourse structure, which leans on agentless passives, nominal endings, ritual openings and closings, and a humble hedge register. A native writer rebuilds the sentence around **who did what**.

| Korean | Literal (don't) | Natural (do) | Why |
|---|---|---|---|
| 이번 글에서는 ~에 대해 알아보겠습니다 | "In this article, we will learn about X." | Start with the problem: "Compose recomposes more than you'd think." Or: "This post walks through how we cut X." | Native posts open with a hook [JC, JW-1, ZS-2] |
| ~에 대해 살펴보자 / 알아보자 | "Let's take a look at X." (every section) | Use a heading and go straight into content. Occasionally: "Let's look at an example." | "Let's look" works for code, not as a section ritual |
| ~에 대해 (everywhere) | "about X" / "regarding X" / "for X" | Make X the subject or object: "X skips recomposition when…" | Topic markers become verbs |
| ~하는 이유는 ~때문이다 | "The reason why X is because Y." | "X happens because Y." / "Why? Y." | "reason why… is because" is redundant |
| 정리하면 | "To summarize, …" | "So: …" / "In short, …" / "The short version: …" Or drop it. | |
| 결론적으로 / 결론부터 말하면 | "In conclusion, …" | Put the conclusion first: "TL;DR: use X." / "Short answer: no." | Natives front-load the answer [MV dek] |
| 마치며 / 맺음말 | "Closing" / "Conclusion" | "Wrapping up", "Takeaways", "What's next", or no heading | [CB-2] "Lessons learnt" |
| 느낀 점 | "What I felt" | "What I learned" / "Takeaways" / "Looking back" | "felt" sounds emotional in English |
| 회고 | "Retrospective" (as a title noun) | "Looking back on…", "What I learned from…", "Lessons from…", or "postmortem" for incidents | "Retrospective" is fine as a team-ritual name but stiff as a post frame |
| 삽질 | "shovel work" / "digging" | "I spent two days chasing…" / "a rabbit hole" / "a lot of trial and error" / "This bit me." | There's no single equivalent, so describe what you spent time on |
| 트러블슈팅 | "Troubleshooting" (title) | "Debugging a…" / "Tracking down…" / "Why our X crashed on Y" | Put the symptom in the title |
| ~를 적용해보았다 | "I tried applying X." | "I added X to our app." / "We switched to X." / "We moved to X." [CASH Metro post title] | "apply" sounds like paint |
| ~를 도입했다 | "We introduced X." | "We adopted X." / "We started using X." / "We moved to X." | |
| 고민했다 / 고민이 많았다 | "I worried about…" / "I had many concerns." | "I went back and forth on…" / "The question was whether…" / "I wasn't sure whether…" | 고민 is deliberation, not worry |
| 개선했다 | "We improved X." (no number) | "We cut X from 30 to 10 minutes." [CASH-2] / "X is now 2x faster." | Be specific |
| 구현했다 | "I implemented X." (everywhere) | "I built X" / "I wrote X" / "X now does Y" | "implemented" is fine but repetitive |
| ~하게 되었다 (agentless) | "It became possible to…" / "It came to be used" | "Now we can…" / "We ended up using…" / "This let us…" | Name the actor |
| ~할 수 있다 (overuse) | "We can see that X can be Y." | "X is Y." / "Notice that X is Y." | Korean 할 수 있다 often isn't about ability |
| ~할 수 있습니다 (feature intro) | "You can use X to do Y." (repeated) | "X does Y." / "Use X for Y." | Imperatives are natural [MV] |
| ~인 것 같습니다 | "It seems like X is Y." | "X is Y." Or, if you really aren't sure, "I think X is Y" / "I'm pretty sure…" [JE-2] | Korean softening isn't real uncertainty |
| ~라고 생각합니다 (every claim) | "I think that…" ×10 | Keep one "I think" for real opinions and state the facts plainly | |
| ~해야 합니다 | "We must…" / "It is necessary to…" | "You need to…" / "Make sure you…" / "Don't forget to…" | "must" sounds like a legal spec |
| 굉장히 / 매우 / 정말 | "very very important", "extremely" | Cut it, or use a number. Or a stronger word: "far slower", "a lot" | Intensifier stacking reads as translated |
| 여기서 문제는 | "The problem here is that…" | "Here's the catch:" / "The problem:" / "But there's a catch." | "The problem here is" is OK once |
| 다음과 같다 / 아래와 같이 | "It is as follows." / "as shown below" | "Here's what it looks like:" / "Like this:" [JC] | |
| 위에서 언급한 것처럼 | "As mentioned above" | "As we saw earlier" / "Remember the X from before?" Or just restate it. | |
| 참고 | "Reference" / "Note for reference" | "Note:" / "Further reading" / "Related:" / a footnote | |
| 참고로 | "For your reference, …" | "By the way, …" / "Side note: …" / "(Fun fact: …)" | |
| ~하는 방법 | "The method of doing X" | "How to do X" (title) / "Doing X" | |
| 사용성 / 가독성이 좋아진다 | "Usability becomes good." | "It's easier to use." / "It reads better." | Use an adjective, not a nominalization |
| ~를 진행했다 | "We proceeded with the migration." | "We migrated." | "진행" is almost always droppable |
| ~를 통해 | "Through X, we could…" | "With X, we…" / "X let us…" / "Using X, …" | |
| ~의 경우 | "In the case of X, …" | "For X, …" / "X, on the other hand, …" / "With X, …" | |
| 이를 해결하기 위해 | "In order to solve this, …" | "To fix this, …" / "The fix: …" | "In order to" is padding |
| 또한 / 그리고 (sentence-initial) | "Also, … Also, … And, …" | Merge the sentences, or use "On top of that", "Plus,", "It also…" | |
| 하지만 / 그러나 | "However, …" at every turn | "But…" at the start of a sentence is fine. Natives use it constantly [CB-2] | |
| 저희 팀은 / 우리는 (solo work) | "We implemented…" | "I…" for solo work, "My team…" or "we" only for team work | |
| Redundant subject repetition | "The LazyColumn… The LazyColumn… The LazyColumn…" | Use pronouns ("it") once the referent is clear | Korean drops subjects; translation over-restores them |
| ~라는 것을 알 수 있었다 | "We were able to know that…" | "We found that…" / "It turned out…" / "Turns out…" | |
| 성공적으로 | "We successfully migrated." | "We migrated." | Success is implied unless contrasted |
| Hashtag-y title like "[Android] Compose 성능 최적화 삽질기" | "[Android] Compose performance optimization digging diary" | "What I learned optimizing a slow Compose list" | No bracket category tags in English titles |

---

## Worked mini-example

**Korean original (typical):**
> 이번 글에서는 Compose에서 리컴포지션이 과도하게 발생하는 문제를 해결한 과정에 대해 알아보겠습니다. 저희 팀은 최근 홈 화면 스크롤 시 버벅임이 발생하는 이슈를 겪게 되었고, 이를 해결하기 위해 많은 고민을 하게 되었습니다.

**Literal (don't):**
> In this article, we will learn about the process of solving the problem of excessive recomposition in Compose. Our team recently experienced an issue where jank occurred when scrolling the home screen, and in order to solve this, we did a lot of worrying.

**Natural (do):**
> Our home feed stuttered every time you scrolled it. The profiler showed why: every row was recomposing on every frame, even the ones that hadn't changed. Here's how we tracked it down and got recompositions back to near zero.

What changed: the post opens on the symptom, the passive "겪게 되었다" became an active sentence with a subject, "고민" became the action actually taken, and the last line promises a concrete result.

---


---

## Haeti.log specifics

### The author's register

The Korean posts are written in plain 해라체 (~다), first person, matter-of-fact, with
occasional dry self-mockery. That maps to a relaxed first-person English voice with
contractions, not to formal documentation English. Keep the self-mockery when it's there.

### Summaries that end in 기록 / 정리

Korean summaries often end in "~한 기록" or "~를 정리했다". Don't translate them as "A record
of…" or "A summary of…". Turn them into a direct claim or the reader's question:

| Korean summary | Literal (don't) | Natural (do) |
|---|---|---|
| LazyColumn 스크롤 버벅임을 잡은 기록 | "A record of fixing LazyColumn scroll jank" | "My `LazyColumn` dropped frames on every fling. The fix was one `key` away." |
| Hilt에서 Metro로 옮기며 겪은 일 정리 | "Summary of things experienced moving from Hilt to Metro" | "What broke, and what got simpler, when I moved a mid-size app from Hilt to Metro" |
| SharedFlow replay 동작 정리 | "Summary of SharedFlow replay behavior" | "How `replay` decides what a late subscriber sees, with the edge cases that bit me" |

### Terms Korean Android devs write in Hangul

| Korean | English | Notes |
|---|---|---|
| 리컴포지션 / 컴포지션 | recomposition / composition | lowercase in prose |
| 컴포즈 | Compose (Jetpack Compose, Compose Multiplatform on first mention when it matters) | |
| 상태 호이스팅 | state hoisting | |
| 안정성 / 안정적인 타입 | stability / stable type | |
| 스킵 | skipping, "gets skipped" | |
| 의존성 주입 | dependency injection (DI) | |
| 생명주기 | lifecycle | |
| 구독하다 (Flow) | collect | "subscribe" is for SharedFlow subscribers and Rx |
| 방출하다 | emit | |
| 콜드/핫 스트림 | cold/hot flows | |
| 지연 초기화 | lazy initialization | |
| 난독화 / 코드 축소 | minification, obfuscation (R8) | |
| 빌드 속도 | build times | plural |
| 기기 / 단말 | device | |
| 화면 | screen | |
| 기여하다 | contribute | |
| PR을 올리다 / 머지되다 | open a PR / got merged | |
| 리뷰를 받다 | got review comments, a reviewer pointed out | |
| 발표 / 연사 | talk / speaker | |
| 스터디 | study group | |
| 사이드 프로젝트 | side project | |
| 현업에서 / 회사에서 | at work | |
| 사수 / 시니어 | a senior engineer on my team | "sasu" has no direct equivalent |
| 신입 | new grad, junior developer | |
| 드로이드나이츠 | Droid Knights (Korea's largest Android conference) | gloss once |
| 대체텍스트, 캡션 | alt text, caption | |

### Korean-only context

Explain a Korean-only reference once, briefly, the first time it appears: "Velog and Tistory
(popular Korean blogging platforms)". Don't explain things every Android dev knows.
