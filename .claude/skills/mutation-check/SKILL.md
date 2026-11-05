---
name: mutation-check
description: Checks whether Haeti.log unit tests actually catch bugs, not just run the code, by planting fake bugs (mutation testing with PIT plus a few LLM-designed ones) into changed logic and seeing which ones the tests miss. Use this before opening a PR that changes non-UI logic in build-logic, core:*, or feature:* (parsers, path mapping, navigation rules, repositories, ViewModels), when the user asks to "뮤테이션 검사", "뮤테이션 테스트", "테스트가 진짜 검증하는지", "가짜 실수", "mutation test", or "PIT", or when Claude wrote both the code and its tests in the same session. Skip it for Compose UI, resources, docs, and post content.
---

# Mutation check

When the same session writes the code and its tests, the tests share the code's
assumptions. Green tests and line coverage say the code ran, not that a wrong
result would be noticed. This check plants small fake bugs ("mutants") one at a
time and runs the tests: a mutant the tests catch is **killed**, one they miss
**survived**. Every survivor is either a test hole or a harmless change, and a
person decides which.

What it measures is test sensitivity, not correctness. "No survivors" does not
mean "no bugs"; the LLM-designed mutants are a sample. Never chase the score:
do not delete or weaken mutants to raise it.

## 1. Pick targets

Only logic changed on this branch:

```bash
git diff origin/main --name-only -- '*.kt' | grep -v -e Test -e /test/
```

Skip composables and screen assembly (`*Screen.kt`, `*Content`, components,
`NavigationState.kt`), DI graphs, resources, and generated code. Those belong to
browser checks, not this one. Map each file to a class pattern: top-level
functions in `Foo.kt` compile to `FooKt`, so `ImageSizes.kt` is
`log.haeti.buildlogic.ImageSizesKt*`.

## 2. Run PIT

`scripts/pitest.init.gradle` attaches PIT from outside the build (no build file
changes). The PIT Gradle plugin does not support Gradle 9, so the script runs
the PIT command line as a `pitest` task. Pass the init script as an **absolute**
path; `-p build-logic` resolves relative paths against `build-logic/`.

```bash
K=$PWD/.claude/skills/mutation-check/scripts
# KMP module (uses the jvm compilation; commonTest runs there)
./gradlew --no-configuration-cache --init-script $K/pitest.init.gradle \
  -PpitTargets='log.haeti.core.navigation.*' -PpitTests='log.haeti.core.navigation.*' \
  -PpitExclude='log.haeti.core.navigation.NavigationStateKt' :core:navigation:pitest
# build-logic (separate build)
./gradlew -p build-logic --no-configuration-cache --init-script $K/pitest.init.gradle \
  -PpitTargets='log.haeti.buildlogic.ImageSizesKt*' -PpitTests='log.haeti.buildlogic.*' :convention:pitest
python3 $K/survivors.py <module>/build/reports/pitest/mutations.xml <module>/src/commonMain/kotlin
```

Tests, `$$serializer`, and `*.generated.*` classes are always excluded. A module
takes seconds. If the report says 0 tests ran, stop: that is a broken setup,
not a pass.

## 3. Add LLM-designed mutants

PIT flips operators; it rarely makes the mistakes a developer would. Ask a
**different model** than the one that wrote the tests (Sonnet if the code was
written by Opus, otherwise Opus) through the Agent tool, giving it **only the
source file**, never the tests. A designer that sees the tests picks mutants it
knows will be caught. Ask for 5 to 8 mutants as a JSON array:

```json
[{"id": 1, "line": 108, "original": "exact text that appears once", "replacement": "...", "breaks": "which real input goes wrong, one sentence"}]
```

Then run them. The runner checks the baseline passes first, applies one mutant,
runs the tests, restores the file, and verifies the restore byte for byte. The
verdict comes only from the test exit code.

```bash
python3 $K/run_mutants.py mutants.json <source file> . ./gradlew :core:navigation:jvmTest
```

Keep `mutants.json` in the scratchpad, not the repo.

## 4. Sort the survivors

Drop these, and say how many and why:

- Kotlin-inserted null checks (`Intrinsics::check*`) and lines past the end of
  the file (inlined standard library code). `survivors.py` drops these for you.
- Equivalent mutants: the result cannot change. Example: a format check that a
  later branch repeats anyway, or a boundary at a file length no real file has.

For each remaining survivor, report it so someone new to mutation testing can
decide. Lead with the input, not the operator:

| # | Fake bug | Why the tests missed it | If left as is | Recommendation |
|---|---|---|---|---|
| 1 | byte-joining math reversed | test PNGs are 3x5, so no value above 255 | a 1600x800 PNG gets a wrong size and tests still pass | add |

Look hard at tests whose name promises something the assertion does not check
(a "keeps identity" test that only compares contents, an "outside file" test
whose file does not exist). Those are the most valuable finds.

Recommend, then let the user choose add / change / skip per row. Write the
reason for every skip ("no post uses quoted `dark=`").

## 5. Close the holes and re-run

Add or tighten the tests, re-run PIT and the LLM mutants, and confirm the chosen
survivors are now killed. When a test asserts "nothing happened", add a
companion that proves the detector sees a real change; a counter that never
counts makes every zero meaningless.

Put the numbers in the PR's 검증 section, for example:
"가짜 실수 69개 중 67개를 테스트가 잡음 (PIT 65 → 67), 다른 모델 설계 8개 중 8개".
Record anything worth keeping in `notes/` (gitignored), not `docs/`.
