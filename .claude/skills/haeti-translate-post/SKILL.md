---
name: haeti-translate-post
description: Writes or updates the English version (index.en.md) of a Haeti.log blog post so it reads like a post written by a native English-speaking Android/Kotlin developer, not a translation. Use this whenever the user asks to translate a post, make or update an English version (영어 번역, 번역본, 영문판, EN 버전, "번역 갱신"), publish or upload a post (글 올리기, 발행, 배포 전 점검), or when the admin shows an "EN 갱신"/"EN 없음" badge or `checkTranslations` warns about missing or stale translations. Also use it when migrating old Korean posts into content/posts and they need English versions.
---

# Haeti.log post translation

Haeti.log is 이태희's personal Android/Kotlin dev blog. Every post lives in
`content/posts/<yyyy-MM-dd>-<slug>/index.md` (Korean, the source of truth). The
English version sits next to it as `index.en.md`, and the web app shows it when
a visitor switches to EN.

The goal is **not** a faithful translation. The goal is the post the author
would have written if they were a native English-speaking engineer blogging on
the same topic, in the voice of people like Jake Wharton, Chris Banes, or Julia
Evans: first person, direct, specific, contractions, no ceremony. Readers
should never be able to tell it started in Korean. Phrasing, sentence order,
paragraph breaks, and headings may all change to get there. The facts may not.

## What must stay identical

The reader of either version should learn the same things, so these are fixed:

- Technical claims, numbers, versions, dates, names, and the order of sections.
- Code blocks, character for character, except Korean comments and Korean
  example strings, which you translate. Never rename identifiers or touch logic.
- Inline code (`` `like this` ``), URLs, and image paths. One exception: a diagram from
  `design/diagrams/posts/<post dir>/` has an English render with `-en` in its name
  (`images/flow-en-light.png`). Use it if it exists; if it doesn't, keep the Korean image
  and tell the user the diagram needs an English version (the `haeti-diagram` skill).
- Image directive attributes (`width`, `align`) and callout markers (`[!note]`,
  `[!tip]`, `[!warning]`). Translate alt text, `caption="..."`, and callout titles.
- Tables keep their shape. Translate cells that are prose.

When a Korean sentence only works for Korean readers (a pun, a domestic
service, a Korean-only meetup), adapt it with a short gloss instead of
dropping it: "Droid Knights (Korea's largest Android conference)". Links to
Korean-only resources stay, followed by "(in Korean)".

## Workflow

1. **Find the target.** Run
   `python3 .claude/skills/haeti-translate-post/scripts/translation_status.py status`
   (pass `content/samples` for the sample posts). `missing` means write a new
   translation. `stale` means the Korean changed after the last translation.
2. **Read the whole Korean post** before writing anything. Figure out what
   kind of post it is (debugging story, explainer, retrospective, release
   notes, announcement), because that decides the voice and the opening.
3. **Read the references** once per session:
   - `references/style-guide.md`: how native dev-blog English sounds (voice,
     openings, endings, rhythm, code lead-ins, numbers, banned AI phrasing).
   - `references/ko-en-phrasebook.md`: Korean patterns that turn into
     translationese and what to write instead.
4. **Write the English post** (see "How to rewrite" below).
5. **Write the frontmatter.** Only `title` and `summary`. Date, category, and
   thumbnail come from index.md, so don't copy them.
   ```yaml
   ---
   title: Sentence-case title, 3 to 9 words
   summary: One sentence, 12 to 25 words, aimed at the reader
   ---
   ```
6. **Stamp it:** `python3 .claude/skills/haeti-translate-post/scripts/translation_status.py stamp <post dir>`.
   This writes `source: <hash>` into the frontmatter so the admin and CI know
   the translation matches the current Korean. Stamp only after the English is
   final; re-stamping a translation you didn't update hides real drift.
7. **Self-review** with the checklist below, fix what fails, and run `status`
   again to confirm `up-to-date`.
8. **Report to the user in Korean** (format below).

### Updating a stale translation

The user may have hand-edited the English, so don't regenerate the whole file.
Run `translation_status.py changed <post dir>` to see the Korean diff since the
English file was last committed, update only the matching English passages,
then stamp. If `changed` says there's no baseline (the English was never
committed, or it has no `source:` line yet), compare the two files section by
section and treat it as a full review: keep English sentences that are already
accurate and natural, and rewrite the rest.

## How to rewrite

Work paragraph by paragraph, but think in terms of what the paragraph is
*doing* (setting up a problem, showing code, reporting a result) and write
the English that does that job. A few moves matter more than the rest:

- **Open on the point.** Drop "이번 글에서는 ~를 알아보겠습니다" openings. Start
  with the problem, the surprise, or the author's stance. If the Korean
  intro is all ritual, compress it to one or two sentences.
- **Name the actor.** Korean leans on agentless and resultative forms (~하게
  되었다, ~할 수 있다). English wants "I switched", "R8 removes", "Compose
  skips". Mechanisms go in present tense, events in past tense.
- **Solo post means "I".** The author writes alone, so 우리/저희 becomes "I"
  unless the text is clearly about a team ("my team", "the Droid Knights
  team"). Teaching "we" ("let's look at") is fine in walkthroughs.
- **Use contractions** (it's, don't, I'd). Without them the post sounds stiff.
- **Lead into code with a plain line ending in a colon** ("Here's the
  collector:"), then after the block say what to notice.
- **Numbers carry the weight.** Keep every number, add the baseline if the
  Korean implies one, and cut adjectives that the number already proves.
- **Replace the closing ritual.** 마치며/정리/결론 headings become "Wrapping up",
  "Takeaways", "What's next", or no heading at all. End on the takeaway, a
  recommendation, or an honest open question, never "In conclusion".
- **Headings are sentence case** and may be rephrased as a claim or a
  question ("Why is it so slow?") instead of a noun label.
- **Keep the author's personality.** If the Korean is casual or self-mocking,
  the English should be too. Don't add jokes that aren't there, and don't add
  enthusiasm (amazing, powerful, seamless) the author didn't express.

- **Re-format Korean emphasis habits.** Korean dev posts bold whole clauses
  and wrap paraphrased ideas in quotation marks for emphasis. In English that
  reads as shouting and as fake quotes. Keep bold for the one or two phrases
  per section that really carry the point, use quotation marks only for actual
  quotes or literal strings, and turn a sentence that was split across
  bullets ("Internal logic can update freely," / "while outside code…") into
  bullets that each stand on their own, or back into prose.

It's fine to merge two short Korean sentences, split a long one, reorder
clauses, or drop a filler sentence that only restates the previous one. It's
not fine to add claims, examples, or opinions the author didn't make. If a
sentence needs a number to land and the post's own data gives it (a median
from a log shown in the post), you may write it, but list it in the report
as an addition.

### When the Korean has problems

Translating forces a close read, so you'll notice things the author missed.
Handle them so the author stays in control of the facts:

- **Obvious typos in names** (a misspelled API constant, "Effect Kotlin" for
  "Effective Kotlin", a broken `**bold**` marker): write the correct form in
  English and list it in the report so the Korean can be fixed too.
- **Technical claims that look wrong**: translate what the author said, and
  flag it in the report with the reason. Don't silently "fix" the argument;
  that's the author's call.
- **Leftovers that aren't part of the post** (old-blog footers, "share" widgets,
  related-post cards, author bios scraped along with a migrated post): don't
  translate them, and tell the user to delete them from the Korean, since
  they'll show up on the Korean page too.

## Self-review checklist

Read the finished English top to bottom as a native reader would, then check:

- [ ] Every fact, number, code block, link, and image from index.md is present.
- [ ] No banned phrasing from the style guide's "AI tells" list (delve,
      leverage, seamless, robust, crucial, "It's important to note", "Let's
      dive in", "In this article, we will", "In conclusion").
- [ ] No em dash (U+2014) anywhere in the post. Use commas, colons,
      parentheses, or split the sentence.
- [ ] No sentence starts with "Also," or "However," more than once per section.
- [ ] Bold is rare, quotation marks wrap only real quotes, and every bullet
      reads on its own.
- [ ] Articles (a/the) are right. Missing articles are the fastest tell of
      non-native text.
- [ ] "I" for the author's own work; no stray "we" from 저희.
- [ ] Title and summary follow the conventions in the style guide.
- [ ] The opening paragraph would make sense if it were the only thing read.
- [ ] `status` reports `up-to-date` for this post.

## Report format (Korean)

After writing, tell the user in Korean:

1. 대상 글과 상태 변화 (예: `missing → up-to-date`).
2. 영어 제목과 요약.
3. **자연스러움을 위해 의도적으로 바꾼 부분** 3~6개. 원문 표현 → 영어 표현, 왜 바꿨는지
   한 줄씩. 사실이 바뀐 게 아니라는 점이 보이게 쓴다.
4. 확인이 필요한 부분: 원문이 모호해서 해석을 고른 곳, 한국 독자용 맥락에 설명을 붙인 곳.
5. 다음 단계: 어드민 EN 탭에서 읽어 보고, 발행할 때 `draft: false`로 바꾼다.

Don't paste the whole translation into the chat; the user reads it in the
admin's EN tab or the file itself.
