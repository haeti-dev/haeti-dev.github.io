# Content Format

## Post Directory

```
content/posts/2026-08-18-hello-haeti-log/
├── index.md
└── images/
```

Directory name: `<yyyy-MM-dd>-<slug>`. Slug: lowercase, digits, hyphens.

## Frontmatter (required keys first)

```yaml
---
title: 글 제목
date: 2026-08-18
category: 안드로이드
summary: 목록/OG에 쓰일 한 줄 요약
thumbnail: images/thumb.webp
draft: false
---
```

`thumbnail` and `draft` are optional (`draft` defaults to false).

Parser: simple `key: value` lines between `---` fences. No nested YAML and no
`#` comments (a comment becomes part of the value). The build (`checkPost` in
build-logic) stops instead of skipping a post when the directory name is not
`yyyy-MM-dd-slug`, `draft` is not exactly `true`/`false`, a published post has
an empty required key, or `date` differs from the directory date.

## Image Directive

Attribute block immediately after a standard markdown image:

```markdown
![대체텍스트](images/foo.webp){width=60 align=center caption="설명"}
```

- `width`: percent of content width (10-100). Default 100.
- `align`: `left` | `center` | `right`. Default center.
- `caption`: rendered under the image. Optional.
- `dark`: image path shown instead in dark mode, e.g. `dark=images/flow-dark.png`. Optional.
- Plain `![alt](src)` without a block stays valid.

## Callout

GitHub alert syntax, first line of a blockquote:

```markdown
> [!note] 제목(선택)
> 본문...
```

Types: `note`, `tip`, `warning`. Unknown types render as `note`.

## English Translation (optional)

Put `index.en.md` next to `index.md`. Its frontmatter needs only `title`,
`summary`, and `source` (fingerprint of the Korean title/summary/body, written
by `translation_status.py stamp`); date, category, and thumbnail come from
`index.md`. Write it with the `haeti-translate-post` skill. Posts without a
translation show the Korean original with an "only available in Korean" notice
when the visitor picks EN.

## Links Between Posts

Link another post with a relative `post/<slug>` URL: `[제목](post/effective-kotlin)`.
It resolves against `<base href>`, so it survives domain changes, and the web app
opens it in-app. Never link to a full site URL or the old `haeti.palms.blog`.
