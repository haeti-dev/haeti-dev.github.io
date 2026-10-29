---
title: [Compose] Snapshot Mutation Policy를 분석하고 활용해보자.
date: 2024-12-31
category: 안드로이드
summary: Jetpack Compose에서 근본적으로 사용되는 SnapshotMutationPolicy을 알아보고 활용해보는 글입니다.
thumbnail: images/thumbnail.webp
draft: false
---

![](images/01.webp){width=100 align=center}

지난 [\[Compose\] Snapshot System을 분석해보자](post/compose-snapshot-system) 글에서 다루지 않은 내용 중 가장 궁금했던 부분이 있다. 바로 **SnapshotMutationPolicy**이다. 클래스 이름만 두고 보았을 때는 무언가를 정의하는 규율, 규칙이지 않을까 하고 넘어갔지만 `mutableStateOf` 에서도 사용되었다는 것을 보고 자세히 분석해보기 위해 이번 글을 작성하게 되었다.

![](images/02.webp){width=100 align=center}

## 들어가며..

Jetpack Compose로 UI를 구축하다 보면 State를 어떻게 관리하느냐가 중요한 화두가 된다. 특히 Compose는 **스냅샷(Snapshot) 기반으로 상태 변경을 추적**하기 때문에, 어떤 기준으로 상태 변화를 ‘같은 값’ 혹은 ‘다른 값’으로 볼 것인지가 꽤 중요한 이슈이다. 여기서 등판하는 개념이 바로 **SnapshotMutationPolicy** 이다.

SnapshotMutationPolicy를 이해하면 다음과 같은 상황에서 좀 더 탄탄한 Compose 코드를 작성할 수 있다.

- 값이 참조만 다를 뿐 실질적으로는 동일할 때, 굳이 리컴포지션(Recomposition)이 일어나지 않게 최적화 가능
- 다중 스냅샷(Multi-Snapshot) 환경에서 상태 충돌이 발생할 때, “이건 충돌인가 아닌가?”를 비교하여 머지(Merge)하는 기준 제공
- 복잡한 데이터 구조를 다룰 때, 값 변경이 어떻게 인지되는지 세밀하게 커스터마이징

이 글에서는 Jetpack Compose의 SnapshotMutationPolicy 인터페이스가 어떻게 작동하고, Jetpack Compose에서 기본으로 제공하는 세 가지 정책(referentialEqualityPolicy, structuralEqualityPolicy, neverEqualPolicy)이 각각 어떤 특징을 갖는지, 적용 시점과 활용 방안에 대해서 살펴보겠다.

## **SnapshotMutationPolicy란 무엇인가**?

Jetpack Compose는 모든 UI 상태를 추적하고, 상태가 변화할 때마다 UI를 다시 그려(Recompose) 준다. 하지만 “**값이 변했다고 판단하는 기준**”은 상황에 따라 다를 수 있다. 예를 들어 어떤 값이 참조로만 달라졌지 실제로는 같은 객체라면, 이를 상태 변화로 간주하지 않을 수도 있다. 반면 무조건 새로운 객체 참조이면 다른 값으로 취급해야 할 수도 있다.

SnapshotMutationPolicy의 구조를 해석하기 전에, 먼저 기본적으로 제공된 주석을 하나씩 살펴보겠다.

> A policy to control how the result of mutableStateOf report and merge changes to the state object.

- state 객체에 대한 `mutableStateOf`의 변경 보고 및 병합 결과를 제어하는 정책이다.

> A mutation policy can be passed as an parameter to mutableStateOf, and compositionLocalOf.

- 뮤테이션 정책은 `mutableStateOf` 및 `compositionLocalOf`에 파라미터로 전달할 수 있다.

> Typically, one of the stock policies should be used such as referentialEqualityPolicy, structuralEqualityPolicy, or neverEqualPolicy. However, a custom mutation policy can be created by implementing this interface, such as a counter policy,

- 일반적으로 referentialEqualityPolicy, structuralEqualityPolicy 또는 neverEqualPolicy와 같은 스톡 정책 중 하나를 사용해야 한다. 그러나 이 인터페이스를 구현하여 카운터 정책과 같은 사용자 정의 뮤테이션 정책을 만들 수 있다.

compositionLocalOf에도 사용되는 정보를 알 수 있었고, 커스텀 정책을 만들어 활용할 수 있다는 정보를 알게 되었다. 이제 내부 구조를 살펴보겠다.

![](images/03.webp){width=100 align=center}

**핵심 역할**

1. `equivalent(a, b)`: 새로운 값 a와 b가 **동등**하다고 간주되는지 여부를 결정
2. `merge(previous, current, applied)`: 스냅샷 간 충돌이 생겼을 때, 어떻게 세 값(previous, current, applied)을 하나로 잘 **병합**할 것인지 로직을 정의

코드의 주석에서도 알 수 있듯이 이 인터페이스는 이름과 어울리게 정책을 결정하는 역할을 한다. 이제 Compose에서 기본적으로 제공하는 3가지 정책에 대해서 알아보겠다.

## 기본 제공 정책 살펴보기

#### 1. `referentialEqualityPolicy`

> 같은 메모리 참조(===)면 동일한 값이다.

![](images/04.webp){width=100 align=center}

참조적으로 (===) 동일한 경우 MutableState.value를 동등한 것으로 취급하는 정책이다.

아래와 같은 경우에 사용될 수 있을 것 같다.

- **싱글턴 객체**를 사용할 때: 항상 같은 인스턴스라면, 값을 변경해도 실제 참조가 같으면 업데이트로 처리하지 않는다.
- MutableState처럼 불변(immutable) 원시 타입이 아니라, 큰 객체를 참조하는 경우: 해당 객체가 바뀌지 않았다면(참조가 그대로라면), 리컴포지션을 방지할 수 있음.

그러나 새로운 값이 “내용적으로”는 같더라도, **객체 참조가 다르면 다른 값**으로 처리하므로 주의해야 한다. 예를 들어 두 개의 서로 다른 리스트 인스턴스가 실제로 동일한 내용이어도, 참조가 다르면 값이 변경된 것으로 간주합니다.

#### 2. `structuralEqualityPolicy`

> 구조적으로 동일(==)하면 같은 값이다.

![](images/05.webp){width=100 align=center}

구조적으로 (==) 동일한 경우 MutableState의 값을 동등한 것으로 취급하는 정책이다.

![](images/06.webp){width=100 align=center}

위에서 확인할 수 있듯이 Compose에서 `mutableStateOf`를 생성할 때 기본 정책으로 사용된다.

아래와 같은 경우에 사용되면 유용할 것 같다.

- **데이터 클래스**를 사용하는 경우: 코틀린의 데이터 클래스는 == 연산자에서 내부 프로퍼티들을 비교하므로, 내용이 모두 같다면 “동일한 상태”로 간주한다.
- **리스트**, **맵** 등 컬렉션을 새로 만들어서 교체했는데 내용이 이전과 똑같다면, 리컴포지션이 필요 없도록 방지할 수 있다.

하지만 구조적 비교 자체가 빈번하게 일어나면, 성능 부담이 생길 수 있다. 예를 들어 매우 많은 멤버 변수를 가진 데이터 클래스를 계속 변경하면, 매번 내부 필드를 전부 비교해야 한다.

#### 3. `neverEqualPolicy`

> 어떤 경우에도 무조건 다른 값이다.

![](images/07.webp){width=100 align=center}

항상 값이 변경되었다고 간주한다. a와 b가 같은 내용이든 참조든지 상관 없이 무조건 false를 반환한다.

상태가 자주 바뀌어야 하고, 매번 최신 상태가 UI에 반영되어야 하는 경우에 단순하게 적용될 수 있을 것 같지만, 사소한 변경에도 계속 리컴포지션이 일어나 성능에 부담을 줄 수 있을 것 같다.

디버깅 목적으로 사용되면 꽤나 유용할 것 같다.

## 커스텀 정책을 만들어 활용해보자

기본 정책 세 가지 이외에도, 직접 `SnapshotMutationPolicy<T>`를 구현해 나만의 정책을 정의할 수 있다. 복잡한 데이터 구조에서 일부 필드만 비교하거나, 기존 값과 신규 값이 모두 변동되었을 때 이전 버전과 병합하는 로직 같이 세밀한 조건을 정의할 수 있다.

![](images/08.webp){width=100 align=center}

위와 같이 이름, 나이, 태그를 가지고 있는 MyState 데이터 클래스가 존재한다고 가정해보자.

동명이인이 존재할 수 있기 때문에, 이름과 나이가 모두 같아야 동일하다고 정의한다.

또한, 멀티 스냅샷 환경에서 과거, 현재 스냅샷, 적용된 값들이 충돌을 일으키면, 아래와 같은 규칙을 통해 병합한다고 가정하자.

- name: 마지막으로 적용된 applied의 값을 유지
- age: current, applied 모두 이전 스냅샷 값과 달라졌다면 더 큰 쪽 선택
- tags: current.tags 와 applied.tags 를 합치되, 중복 없이 유지

이런 규칙을 가지도록 SnapshotMutationPolicy를 구현하면 아래와 같이 코드를 구성해볼 수 있다.

![](images/09.webp){width=100 align=center}

이제 이 MyStatePolicy를 아래와 같이 적용해볼 수 있다.

![](images/10.webp){width=100 align=center}

이제 이 상태는 개발자가 사전에 정의한 “값이 같다고 보는 기준”과 “충돌 시 병합” 방식을 따르기 때문에 불필요한 리컴포지션을 줄일 수 있고, 여러 개의 스냅샷에서 동시에 값이 변경되었을 때도 원하는 방식으로 병합할 수 있다.

이 과정도 [이전 아티클에서 Snapshot Tree에서 Git과 비교한 것](post/compose-snapshot-system)과 유사하다. Git에서 한 파일의 두 버전을 어떻게 합칠지 결정하는 맥락과 같다고 볼 수 있다.

## 어떻게 활용하면 좋을까?

대부분의 상황에서 기본 정책인 `structuralEqualityPolicy()`로 충분하지만, 특수한 경우에는 다른 정책을 고려할 수 있다.

1. **성능 최적화**가 필요할 때
   - 값의 비교가 너무 자주 일어나거나, 구조적 비교가 너무 무거운 경우 → `referentialEqualityPolicy()`로 변경해볼 수 있음
   - 대규모 리스트를 사용하는 Compose UI를 구현할 때, 매번 새로운 리스트 인스턴스를 주입하지만 “내부 아이템 내용”은 동일할 수 있기에 불필요한 리컴포지션을 막을 수 있다.
2. **항상 변화를 감지해야 할 때**
   - “사용자가 어떤 작은 변경이라도 100% UI에 반영되길 원한다” → `neverEqualPolicy()` 고려
3. **멀티스레드 / 멀티 스냅샷** 상황에서 세밀한 충돌 처리가 필요할 때
   - 직접 `SnapshotMutationPolicy`를 구현하여 merge 로직에서 충돌 처리를 커스터마이징

Jetpack Compose는 **선언형 UI**인 동시에 **스냅샷 기반**으로 동작하기 때문에, “**상태가 언제, 어떻게 바뀌는지**”를 제어하는 것이 중요하다. SnapshotMutationPolicy는 값 비교와 스냅샷 충돌 해결 방법을 설정함으로써, **성능 최적화**와 **효율적인 상태 관리**를 모두 가능케 하는 강력한 도구이다.

프로젝트 내에서 “상태는 언제 변경된 것으로 볼 것인가?”라는 질문을 던지고, 정책을 세워보면 좋은 경험이 될 것 같다. 이 아티클이 다른 개발자 분들에게 도움이 되었으면 좋겠다.
