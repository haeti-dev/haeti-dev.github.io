---
title: Baseline Profile로 콜드 스타트 줄이기
date: 2025-12-08
category: 안드로이드
summary: 첫 실행이 느린 이유를 측정부터 다시 하고, 프로파일을 붙여 실제로 얼마나 빨라지는지 확인했다
---

## 측정부터

느낌으로 최적화하지 않는다. Macrobenchmark로 콜드 스타트를 열 번 재고 중앙값을 기준으로 삼았다.

## 프로파일 생성

```kotlin
@Test
fun generate() = rule.collect(packageName = "log.haeti.sample") {
    pressHome()
    startActivityAndWait()
}
```

## 결과

중앙값 기준으로 첫 프레임까지의 시간이 눈에 띄게 줄었다. 숫자는 기기마다 다르니 꼭 직접 재 보길 권한다.
