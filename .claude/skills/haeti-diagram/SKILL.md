---
name: haeti-diagram
description: Draws structure, flow, layer, and map diagrams for Haeti.log in the blog's own fonts and colors, rendered as light and dark PNGs from the design/diagrams kit. Use this whenever the user asks for a diagram, figure, chart of a structure, flow, architecture picture, module map, or "그림" in a blog post, README, or docs (구조도, 흐름도, 다이어그램, 아키텍처 그림, 모듈 지도, 그림 넣어줘, 그려줘, 시각화), when they want to replace a mermaid block, or when a post or doc explains a pipeline, layering, or sequence that would be clearer as a picture. Never use mermaid in this repo; this skill replaces it.
---

# Haeti.log diagrams

Every diagram in this repo comes from one kit, `design/diagrams/`, so README,
`docs/`, and blog posts all look like the blog itself: Pretendard and JetBrains
Mono, `HaetiColors` tokens, the same card and line shapes. A diagram is a small
HTML file built from the kit's parts, rendered by Playwright into a light and a
dark PNG. The PNGs are committed; nothing renders at build time, because CI
should never need a browser to build the blog.

Read `design/diagrams/README.md` once per session. It lists the parts in
`kit.css` and the rules. Open the existing diagrams (`flow.html`,
`modules.html`, `pipeline.html`) and the `templates/` before writing a new one;
copying the closest one is faster and keeps the style consistent.

## 1. Decide what the picture has to say

Read the surrounding text first (the whole post, or the doc section). Write
down, for yourself, the one sentence the diagram should make obvious at a
glance. If you can't, or the text already says it in two lines, tell the user
the post doesn't need a diagram there instead of drawing one.

Then pick the shape from the kit:

| The text describes | Use |
|---|---|
| steps in order (build, request, data passing through) | `.flow` (docs) or `.flow-v` (posts) of `.node` cards |
| layers or groups (modules, app/feature/core, before/after) | `.tiers` with `.tier` rows of `.chip`s |
| a set of named things (files, modules, APIs) | `.chip`s inside a `.tier` |
| a split that isn't a real object (source sets, build time vs runtime) | `.dashed` boxes or `.tier-label`s |
| a relationship that skips over steps | `svg.links` (`under` or `drop`) plus a `.pill` label |

Highlight (`.hl`, the blue line) exactly one thing: the part the sentence is
about. That one thing can be a node, a chip, or a whole tier row. Two
highlights means neither stands out.

Only draw what serves that sentence. If the text lists seven plugins but the
sentence is about how three of them stack, the other four stay out; a diagram
is not an inventory of the post.

## 2. Where the files go

| Target | Source HTML | `<meta name="out">` | Width |
|---|---|---|---|
| README or `docs/` | `design/diagrams/<name>.html` | `docs/diagrams/<name>` (README images stay in `docs/readme/`) | default 1240px |
| Blog post | `design/diagrams/posts/<post dir>/<name>.html` | `content/posts/<post dir>/images/<name>` | `<div id="diagram" class="post">` (680px) |

Post sources live outside the post folder because the whole post folder is
deployed. Inside `posts/<post dir>/`, the kit links are `../../kit.css` and
`../../kit.js`.

Post diagrams are read on phones at about half size. Use `class="post"` (bigger
type, 680px), stack more than three steps vertically with `.flow-v`, and keep
at most three nodes side by side. Keep the smallest text at 16px or more (the
`post` defaults already are) and the height under about 1000px; if it grows
taller, cut items or split it into two diagrams placed where each idea comes up.

A local `<style>` in the diagram file may adjust layout (grid columns, gaps,
widths), as `modules.html` does. Colors and fonts always come from `kit.css`.

## 3. Write the text on the diagram

- Korean, short. A node's description is one line, two at most. Names in code
  (`PostsResourcesTask`, `core:data`) stay as they are in the code.
- Same facts as the surrounding text. Don't add claims the text doesn't make.
- Never use the em dash character (U+2014) anywhere, in labels or in HTML.
- Only kit tokens for color. If something truly needs a new color, it belongs in
  `HaetiColors` first, so ask the user.

## 4. Render and look at it

```bash
cd design/diagrams
[ -d node_modules/playwright ] || npm install   # first time on this machine
node render.mjs posts/<post dir>/<name>          # or just <name> for docs diagrams
```

If Playwright says the browser is missing, run `npx playwright install chromium`.

Then actually look at both PNGs with the Read tool. Check:

- nothing overflows its card, and no text wraps into a third line
- lines run under labels, not through them (`svg.links` must be the first child of `#diagram`)
- the highlight is on the right node, and the dark version is as readable as the light one
- for posts: imagine it at 360px wide. If the smallest text would be unreadable, cut words or go vertical

Fix and re-render until all of these pass. Don't hand the user a diagram you
haven't looked at.

## 5. Put it in the text

**README and docs** use `<picture>` so GitHub picks the theme (paths relative to the markdown file):

```html
<picture>
  <source media="(prefers-color-scheme: dark)" srcset="diagrams/<name>-dark.png">
  <img alt="<what the diagram says, in one sentence>" src="diagrams/<name>-light.png">
</picture>
```

**Blog posts** use the image directive with `dark`, which the blog and the
admin preview both understand:

```markdown
![<what the diagram says, in one sentence>](images/<name>-light.png){width=100 dark=images/<name>-dark.png}
```

Put it right after the paragraph that introduces the idea, on its own line.
Alt text says what the picture shows, not "다이어그램".

## 6. English version of a post

If the post has `index.en.md`, a Korean diagram in the English post looks
broken. Make an English copy of the source (`<name>.en.html`, out
`.../images/<name>-en`), with labels written the way an English dev blog would
name things (not word-for-word), render it, and add the same image line to
`index.en.md` at the matching spot with the `-en` paths.

Adding a line to `index.md` changes the Korean fingerprint, so right after your
edit the English version shows as "stale". That is expected. What decides
whether you may stamp is the status *before* you touched anything, so run
`python3 .claude/skills/haeti-translate-post/scripts/translation_status.py status`
before you start and remember the result:

- If the post was `up-to-date` before your change and the diagram is the only
  change, run
  `python3 .claude/skills/haeti-translate-post/scripts/translation_status.py stamp content/posts/<post dir>`
  (the path from the repo root, not the bare folder name) after updating both
  files, then run `status` again and confirm `up-to-date`.
- If it was already `stale` or `missing`, don't stamp. Leave the English for the
  translation skill and tell the user.

## 7. Finish

- Run `./gradlew spotlessApply jvmTest build` only if you touched Kotlin; a
  diagram alone doesn't need it.
- Commit locally only, never push. Commit the HTML source together with the PNGs
  so the diagram can be edited later.
- Report to the user in Korean: what the diagram says, where it went, the light
  and dark images (send them if you can), and anything you decided not to draw.
