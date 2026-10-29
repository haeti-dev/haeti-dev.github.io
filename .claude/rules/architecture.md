# Architecture Rules

## Module Boundaries

- `core:model`: pure Kotlin + `@Immutable`. NO compose UI, NO IO, NO platform APIs.
- `core:common`: `Result`/`asResult` only. Coroutines allowed.
- `core:data`: parsing + repositories + bundled source. Compose resources OK,
  NO composables. Bindings via Metro `@ContributesBinding(AppScope::class)`.
- `core:navigation`: NavKeys (`@Serializable`), `NavigationState`, `Navigator`,
  path mapping. Pure logic must stay testable without a browser.
- `core:markdown`: rendering only. Callers pass raw body strings in.
- `core:designsystem`: theme + atoms + interaction. NO business logic, NO other core deps.
- `core:ui`: composed components, `LayoutTier`/`LocalLayoutTier`, `UrlOpener`.
- `feature:*`: `XxxViewModel` (lifecycle ViewModel, Metro-injected) +
  `XxxUiState` + `XxxScreen` (stateful, `metroViewModel()`) + stateless
  `XxxContent` + `navigation/XxxEntryProvider.kt`. Screens draw only their
  content column; the app shell owns the top bar.
- `app:web`: wasmJs only. Browser interop (history, localStorage, document title,
  `<base href>`) lives here. Core modules use `window`/`document` only behind
  expect/actual in wasmJsMain (YouTube embed, clipboard, URL opener).
  `app:web` provides `LocalLayoutTier` from `BoxWithConstraints`.
- `app:admin`: jvm only. All file IO lives here (java.nio) behind `PostFileStore`.

## Convention Plugins (build-logic)

- `haeti.kmp.library`: jvm + wasmJs targets, toolchain 21, ktlint, kotlin-test.
- `haeti.kmp.compose`: + Compose plugins, path-derived `Res` package.
- `haeti.kmp.feature`: + serialization, Metro, core deps, lifecycle/Nav3, test deps.
- App modules apply raw plugins (they need a single target).
- New library deps go to `gradle/libs.versions.toml`; no inline coordinates.

## Navigation (web)

- Single Nav3 back stack rooted at `HomeNavKey`. `Navigator.navigate` dedups keys.
- Browser History API sync in `app/web`: on stack change `historyUpdateFor` compares
  the screen (NavKey), not the path string: push for a new screen, replaceState when
  only the shape differs (trailing slash). App paths end with `/` like the Pages shells.
  popstate -> `Navigator.replaceAll(backStackFor(navKeyFromPath(path)))`.
- `document.title` from `NavKey.documentTitle(postTitle)` on every change.
- Deep links: dev server `historyApiFallback`; Pages per-page shells (`post/<slug>/index.html`), `404.html` for the rest.

## Content Pipeline

1. `content/posts/<yyyy-MM-dd-slug>/index.md` -- frontmatter + body.
2. Admin edits these files directly (only writer).
3. `generatePostsResources` (core:data) copies published posts + `index.json`
   into `build/generated/postsResources`, wired via `compose.resources.customDirectory`.
4. `BundledPostRepository` parses once, caches in memory, exposes Flows.
5. `generateSiteFiles` (app:web, build-logic `SiteFilesTask`): per-post OG shells, static page
   shells, `og/` images (thumbnail JPG or the same code-minimap cover as `PostCover`), `sitemap.xml`,
   `feed.xml` (RSS), `robots.txt`. Absolute URLs come from `-Phaeti.siteUrl`. `OgCover.kt` mirrors
   `CategoryPalette`/`PostCover`; change them together.

## Security

- `app:admin` must NEVER be wired into `app:web` or any deploy artifact.
- No secrets in repo. No network calls in admin (local files only). One exception:
  `TranslationRunner` launches the local `claude` CLI when the user clicks a
  translate badge/button; admin code itself never opens a connection.
