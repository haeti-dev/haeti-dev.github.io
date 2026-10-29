# Code Style Rules

## File and Function Length

- Maximum **200 lines** per file. Split if exceeded.
- Maximum **40 lines** per function. Extract helpers if exceeded.

## Formatting

- ktlint (`ktlint_official`, 120 cols) via Spotless. Run `./gradlew spotlessApply`
  before finishing; CI fails on `spotlessCheck`.
- Trailing commas on multiline declarations and calls.
- No wildcard imports. Explicit visibility for public API.

## File Separation

- One public class/interface per file (private helpers allowed in-file).
- Data classes and enums in their own files under `model/`.
- Composable screens: `XxxScreen.kt` (stateful, ViewModel) + `XxxContent`
  (stateless) in the same file until the cap, then split.

## KDoc

All public functions and classes MUST have KDoc written in Korean.

- One-line description. Second line only for non-obvious constraints.
- `@param`: short noun phrase. No usage examples, no change history.
- Omit anything obvious from the signature.

## Kotlin

- Prefer immutable `val`, data classes, `ImmutableList` in UiState.
- Compose: state hoisting; stateless composables take values + lambdas.
- Coroutines: `viewModelScope`, `SharingStarted.WhileSubscribed(5_000)`.
- Tests: `commonTest`, kotlin.test + turbine + `runTest`; fakes from `core:testing`.
