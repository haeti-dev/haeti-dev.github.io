# CLAUDE.md

## Communication

- Always respond in Korean (한국어).
- Code comments and KDoc: Korean. Every public function/class needs KDoc.
- These instruction files are written in English.

## Project Overview

Haeti-log is a personal tech blog built with Compose Multiplatform (convention
plugins, core/feature modules, Metro DI, Navigation 3, ViewModel + UiState,
commonTest):

- `app/web/` -- public blog. Kotlin/Wasm (`wasmJs`), canvas-rendered CMP.
- `app/admin/` -- local-only authoring app. CMP Desktop (JVM). Edits markdown
  files directly on disk with live preview. NEVER deployed.
- Content source of truth: markdown files under `content/posts/`.

Owner writes a post every 2-3 months. Simplicity and long-term buildability
beat cleverness.

## Build Commands

```bash
./gradlew :app:web:wasmJsBrowserDevelopmentRun   # blog dev server, localhost:8080 (never opens a tab)
./gradlew :app:web:wasmJsBrowserDistribution     # production bundle
./gradlew :app:web:prepareGitHubPages -Phaeti.basePath=/repo/ -Phaeti.siteUrl=https://user.github.io/repo   # Pages artifact (OG shells, sitemap, RSS)
./gradlew :app:admin:run                         # authoring desktop app
./gradlew :app:admin:installAdminApp             # build the macOS app into ~/Applications (rerun after admin changes)
./gradlew jvmTest                                # all commonTest suites on JVM
./gradlew spotlessApply                          # ktlint format (CI runs spotlessCheck)
./gradlew build                                  # full build + checks
```

JDK 17+ runs Gradle; the daemon and toolchain are pinned to 21
(`gradle/gradle-daemon-jvm.properties`, foojay resolver). Never kill the Gradle
daemon or delete `build/` by hand; use `./gradlew clean`.

## Architecture

### Module Structure

```
build-logic/convention/  -- haeti.kmp.library | haeti.kmp.compose | haeti.kmp.feature | haeti.spotless
core/common/             -- Result + Flow.asResult() / flatMapLatestAsResult(), keepAll
core/model/              -- Post, PostMeta (pure Kotlin, @Immutable, kotlinx-datetime)
core/data/               -- PostParser, PostRepository, bundled Compose-resource source, Metro bindings
core/navigation/         -- NavKeys, NavigationState/Navigator (Nav3), path <-> key mapping
core/markdown/           -- markdown rendering (mikepenz), directives, callouts, code cards
core/designsystem/       -- HaetiTheme, tokens, fonts, atoms (chip, date column, skeleton), interaction
core/ui/                 -- composed components (top bar, intro, list row, footer), LayoutTier, UrlOpener
core/testing/            -- FakePostRepository, TestPosts, MainDispatcherRule
feature/home|post|pages/ -- ViewModel + UiState + Screen + Nav3 entry provider (pages: stateless placeholders)
app/web/                 -- shell (theme, top bar, NavDisplay), WebAppGraph, browser history sync
app/admin/               -- AdminAppGraph, editor, file IO
content/posts/           -- <yyyy-MM-dd-slug>/index.md + images/. Source of truth.
docs/                    -- public, curated: ARCHITECTURE, DESIGN, PUBLISHING
design/                  -- icon sources, diagram kit (design/diagrams: HTML -> light/dark PNG)
notes/                   -- private, gitignored: TODO, full troubleshooting log, design history
```

### Dependency Rules

- `app:*` -> `feature:*` -> `core:*`. Never app-to-app, never feature-to-feature.
- `core:model` and `core:common` have no Compose UI. `core:data` may read
  Compose resources but has no UI. `core:markdown` renders only (no parsing).
- Browser APIs (history, localStorage, `<base href>`) live in `app/web` wasmJsMain.
  Core modules touch `window`/`document` only behind expect/actual in wasmJsMain
  (YouTube embed, clipboard, URL opener).
- File IO lives in `app/admin`. Every `core:*`/`feature:*` compiles for jvm and wasmJs.
- Packages follow module paths: `log.haeti.core.<module>`, `log.haeti.feature.<name>`,
  apps use `log.haeti.web` / `log.haeti.admin`.

### Patterns

- DI: Metro. `@Inject` + `@ContributesBinding(AppScope::class)` on implementations,
  one `@DependencyGraph` per app. ViewModels via `metroViewModel()` /
  `assistedMetroViewModel()`; never `remember { ViewModel() }`.
- State: `StateFlow<UiState>` (sealed: Loading/Success/Error) from
  `repository.flow.asResult().stateIn(WhileSubscribed(5_000))`; screens
  `collectAsStateWithLifecycle()`. Stateless `XxxContent` takes values + lambdas.
- Navigation: Nav3 single back stack. Features expose
  `EntryProviderScope<NavKey>.xxxEntry(navigator)`. The web shell syncs the
  stack with `pushState`/`popstate` and derives paths from `core:navigation`.
- Resources: every Compose module gets `log.haeti.<path>.generated.resources.Res`.
  `core:data` serves posts from a build-generated directory (`generatePostsResources`),
  drafts excluded; nothing is copied into the source tree.

### Web constraints (canvas CMP)

- Everything renders into one <canvas>; the DOM has no article text.
- `<base href>` is injected at build (`-Phaeti.basePath`); routing and
  resources are relative to it. Pages deploy ships per-page shells with OG meta
  (`generateSiteFiles`) plus `404.html` for other paths.
- Fonts are preloaded before first paint; CMP 1.12 downloads Noto subsets for
  glyphs missing from the bundled Pretendard subset.

## Publishing

Posts ship in Korean plus an English `index.en.md`. Follow `docs/PUBLISHING.md`.
Translate with the `haeti-translate-post` skill (natural English dev-blog voice,
not literal). `index.en.md` records `source:` (hash of the Korean title, summary,
body); `:core:data:checkTranslations` and the admin flag missing/stale ones.

## Rules

Read all files under `.claude/rules/` before writing code:

- `code-style.md` -- 200-line file cap, 40-line functions, Korean KDoc, ktlint
- `architecture.md` -- module boundaries, DI, navigation, content pipeline
- `content-format.md` -- frontmatter schema, image/callout directives

## Verification

Before claiming done: `./gradlew spotlessApply jvmTest build` passes, and for UI
work run the relevant app target and check it renders in a real browser
(the embedded preview pane throttles frames when hidden; see notes/TROUBLESHOOTING.md).

## Docs

`docs/` is public and written for other developers: each entry shows the
problem, the options weighed, the criteria, and the trade-off accepted. Log
every new issue, work note, and TODO in `notes/` (gitignored). Promote an
entry to `docs/` only when the owner asks, and rewrite it in that form.
Never name reference repositories or other developers as an influence on this
project's design in public files. Never use the em dash character (U+2014).
Diagrams for README, docs, and posts come from `design/diagrams` (blog fonts and
colors, rendered to light/dark PNG) via the `haeti-diagram` skill. Never use mermaid.
