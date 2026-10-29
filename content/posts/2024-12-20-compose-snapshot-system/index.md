---
title: [Compose] Snapshot System을 분석해보자
date: 2024-12-20
category: 안드로이드
summary: Compose를 관통하는 Snapshot 시스템의 내부 구조를 살펴보고 예시를 통해 설명해보았습니다.
thumbnail: images/thumbnail.webp
draft: false
---

지난 [**Effective Kotlin 읽고 Compose에 적용하기**](post/effective-kotlin) 글에서 Compose에서는 **가변적인 상태를 효과적으로 제어**하기 위해 내부적으로 **Snapshot 시스템**이라는 것을 사용한다는 것을 다뤄보았다. 또한, **다중 버전 동시성 제어(MVCC)를 통해 다중 스레드 환경에서도 안정성을 유지**한다는 것을 간단한 예시를 통해 알아보았다. 이번 글에서는 이 Snapshot 시스템이 구체적으로 어떻게 작동하고, 의도하지 않은 SideEffect를 방지하기 위해 작성되는 코드들(ex. `deriveStateOf`)에서 어떻게 사용되고 있는지 알아보겠다.

## Snapshot System 이란?

다들 살면서 사진을 찍어본 적이 있을 것이다. **어떤 순간, 상태를 간직하기 위해 “찰칵”하고 캡쳐해 둔 뒤, 나중에 그 사진을 보며 그 시점의 장면을 되살리는 것**과 비슷한 개념이 바로 Compose의 Snapshot 시스템이다.

Jetpack Compose에서 Snapshot System이란 **현재 상태를 특정 순간에 캡처**해놓고, 이를 안전하게 관리하기 위한 메커니즘이다. 쉽게 말해, 시간이 흐르면서 값이 변하는 `state`가 있다고 할 때, 이 **상태를 특정 시점에 “사진” 찍어서 보관해두는 방식**으로 생각할 수 있다. 이 덕분에, 여러 스레드나 여러 동작들이 동시에 상태를 변경하더라도, **Compose는 이전에 찍어둔 “사진”을 기반으로 안정적으로 UI를 업데이트**할 수 있다.

좀 더 자세히 살펴보자.

Compose에서는 Recomposition을 최적화하는 것이 가장 중요한 과제이다. Compose는 UI를 “어떻게 그릴지”보다 “**무엇을 그릴지**”에 더 집중한다. 이것은 선언형 UI의 특징이기도 하다. 이런 상황에서 상태가 의도하지 않게 변경되면 UI도 의도하지 않게 변경되어서 유저에게 혼란을 주고, 치명적인 버그로 이어질 수 있다. 예를 들어 서버통신이 진행되어서 상태가 바뀌는 과정 중에서 유저의 터치로 상태가 변경된다면, 즉 멀티 스레드 환경에서 동시에 상태를 읽고 변경하는 과정에서 상태가 망가질 수 있다.

이런 상황에서 상태 변화를 “**트랜잭션**”처럼 처리해서 언제나 상태가 일관된 모습으로 유지되고, 스레드 안정성을 갖출 수 있게 하는 것이 Compose의 Snapshot 시스템이다.

## 다중 버전 동시성 제어(MVCC)

동시성 제어 기법중 **Jetpack Compose는 다중 버전 동시성 제어(Multi-Version concurrency control, MVCC)를 채택하였다**. “Multi-Version”이라는 이름에서 유추할 수 있듯이, 이 기법은 동일한 데이터(상태)에 대해 여러 ‘버전’을 동시에 관리한다.

동시성을 제어하기 위해 상태를 격리하는 방법을 채택할 수 있다. 격리를 수행하는 가장 간단한 방법은 작성자가 작업을 완료할 때까지 모든 구독자를 차단하는 것이지만, 이는 성능 측면에서 좋지 않을 수 있다. MVCC에서는 그보다 더 나은 성능을 발휘한다.

격리를 달성하기 위해 MVCC는 데이터의 다중 복사본(여러 버전)을 유지하여 각 스레드가 주어진 순간에 상태의 격리된 버전, 즉 스냅샷으로 작업을 할 수 있게 한다. 한 스레드에 의한 수정 사항은 모든 작업이 완료되고 전파하기 이전에는 다른 스레드에게 보여지지 않는다.

좀 더 쉽게 접근해보겠다.

- **여러 버전의 사진을 저장**

  하나의 상태를 변경할 때마다 이전 상태도 버전 형태로 보관한다. 즉, 상태가 변할 때마다 “사진”을 한 장씩 찍어두는 것과 비슷하다.
- **동시 읽기/쓰기 지원**

  어떤 쓰레드가 상태를 읽을 때, 다른 쓰레드가 동시에 상태를 변경하고 있어도 문제없다. **읽는 쪽은 자신이 참조하는 시점의 ‘사진’을 보면 되고, 쓰는 쪽은 새로운 ‘사진’을 만들어내면 된다**.
- **결정적 적용 시점(Commit**)

  여러 변경이 끝난 뒤, “이제 이 사진을 공식 버전으로 사용할게!” 라고 **선포(apply**)하는 순간이 있다. 이때만 전역 상태가 실제로 변경된다.

MVCC를 통해 각 스레드는 마치 자신만의 타임머신이 있는 것처럼 동작할 수 있다. 과거 시점의 상태를 읽고, 새 버전을 만들어내고, 최종적으로 전역 상태에 반영하는 과정에서 다른 스레드와 충돌이 발생해도, 이 충돌을 명확히 감지하고 처리할 수 있다. 이 말을 다른 관점에서 보면, 새 버전을 만들어내는 과정에서 충돌이 발생해도, 변경 사항을 전파하는 시점에만 감지된다. 즉, **낙관적**이다.

### Snapshot의 라이프사이클 소개

앞서 Snapshot을 “어떤 시점의 상태를 사진 찍어 둔 것”으로 비유했다. 이제 여기서 한 걸음 더 나아가, **Snapshot이 생성되고 사용되다가 폐기되는 라이프사이클**을 정리해보자.

![](images/01.webp){width=100 align=center}

1. **생성(Create**):

   `takeSnapshot()` 또는 `takeMutableSnapshot()` 등의 API를 호출해 새로운 스냅샷을 만든다. 이때 스냅샷은 글로벌 상태를 특정 시점 그대로 캡처한다.
2. **변경(Modify, 선택사항**):

   가변 스냅샷(mutable snapshot)을 만들었다면, 여기서 실제 상태 값을 변경할 수 있다. 이 변경은 외부에 곧바로 반영되지 않고 스냅샷 내부에만 머문다.
3. **적용(Apply) 또는 폐기(Dispose**):

   변경사항을 최종적으로 전역 상태에 반영하려면 `apply()`를 호출한다. 이렇게 하면 기존 글로벌 상태를 새로운 버전으로 “갈아 끼우게” 된다. 만약 이 변경이 불필요해졌다면 `dispose()`를 호출하여 스냅샷을 폐기하고, 메모리 리소스를 해제한다.
4. **관찰(Observe**):

   다양한 컴포저블(Composable)이나 다른 부분에서 이 스냅샷을 통해 상태를 관찰하게 된다. 하지만 이미 다른 스냅샷으로 바뀌고 더 이상 필요 없는 스냅샷이면, 자연스럽게 GC(가비지 컬렉션)의 대상이 될 수 있다.

이 라이프사이클을 통해 상태 변경 전후를 명확히 분리하고, 불필요한 스냅샷을 정리함으로써 상태 관리의 복잡도를 줄일 수 있다.

### 스냅샷 트리(Snapshot Tree): 다중 버전 상태 관리의 핵심

앞서 MVCC 개념을 통해 하나의 상태에 대해 여러 버전을 관리한다는 점을 짚었다. 그럼 이 여러 버전은 어떻게 구조화될까?

스냅샷들은 서로 부모-자식 관계를 맺으며, 계층적인 **트리(Tree**) 형태로 관리된다. 글로벌 스냅샷이 트리의 루트(root)라고 할 수 있고, 여기서 가지를 친 형태로 가변 스냅샷이 만들어진다.

- **Global Snapshot**

  모든 상태의 “현재 승인된(적용된) 버전”을 나타낸다. UI 재구성(Recomposition)은 주로 이 글로벌 스냅샷을 기준으로 일어난다.
- **Mutable Snapshot**

  새로운 상태 변경을 시도할 때 글로벌 스냅샷을 기반으로 파생한 스냅샷이다. 이 스냅샷은 부모 스냅샷에 종속적이며, 아직 글로벌 상태에 반영되지 않은 변경 내용을 담고 있다.

예시를 통해 확인해보자.

1. **초기 상태**:

   글로벌 스냅샷(G0)이 있다고 하자. 현재 모든 UI와 로직은 G0를 기준으로 상태를 본다.
2. **가변 스냅샷 생성**:

   쓰레드 A가 G0로부터 가변 스냅샷 S1을 만든다. 여기서 A는 S1에서 상태를 여러 번 변경한다. 이 때도 글로벌 상태는 G0를 유지하므로 다른 부분에는 영향이 없다.
3. **적용(Apply**):

   A가 S1의 변경을 확정(apply)하면, 이제 새로운 글로벌 스냅샷 G1이 만들어진다. G1은 S1이 반영된 최신 상태다. 이제 전역적으로 G1 상태를 참조하게 된다.

위 상황을 코드로 구성해보았다.

![](images/02.webp){width=100 align=center}

트리 형태로 보면, G0에서 파생된 S1을 적용하여 G1이 탄생한다. 이런 식으로 G0 → G1 → G2 ... 식으로 변화를 버전 관리한다.

#### Git Flow와 Snapshot Tree 비교

위 상황을 생각해보니 협업시 자주 마주치는 Git Flow가 생각났다. 꽤나 유사한 상황인 것 같아 맵핑을 해보았다.

- **글로벌 스냅샷(Global Snapshot**)

  Git의 `main` 또는 `master` 브랜치에 해당하는, 모든 개발자와 시스템이 공유하는 “공식” 상태.
- **가변 스냅샷(Mutable Snapshot) S1**

  Git에서 새로운 기능을 추가하기 위해 만든 `feature` 브랜치와 유사하다. 여기서 마음껏 코드를 변경해볼 수 있지만, 이 `feature` 브랜치의 변경 사항은 아직 `main`에 반영되지 않으므로 다른 사람이나 시스템에는 영향이 없다.
- **apply 호출하여 글로벌 스냅샷 갱신 (S1 → G1**)

  Git Flow에서 `feature` 브랜치를 `main` 브랜치에 머지(Merge)하는 단계와 같다.

  머지가 성공적으로 이루어지면 `main` 브랜치(글로벌 스냅샷)가 새로운 변경사항을 반영한다.

  이 때, 과거 `main` 상태(G0)에서 새로운 `main` 상태(G1)로 “버전”이 진화하는 것과 비슷하다.
- **여러 번 반복**

  Git에서 `feature` 브랜치를 여러 번 만들어서 순서대로 `main`에 병합하듯, Compose Snapshot 시스템에서도 S2, S3 등의 가변 스냅샷을 만들어 적용하면 G2, G3 등의 새로운 글로벌 스냅샷 버전이 생긴다.

정리하면, Snapshot 시스템은 상태 변화를 단계적으로 관리하여 “공식 상태”를 명확하게 업데이트하는 방식이고, 이는 Git Flow에서 `feature` 브랜치로 작업하고 `main` 브랜치로 머지하는 과정과 개념적으로 유사하다. Git Flow를 통해 여러 개발자가 동시에 다양한 기능을 안전하게 개발하고 최종적으로 안정된 상태를 `main`에 반영하듯이, Snapshot 시스템도 여러 스냅샷에서 독립적인 상태 변경을 하다가, 최종적으로 전역 상태에 반영함으로써 **일관성과 안정성을 유지**한다.

### StateObject와 StateRecord: 내부 구조 이해하기

Compose는 단순히 `counter.value`처럼 하나의 값만 관리하는 것이 아니라, 다양한 상태를 버전별로 추적해야 한다. 이를 위해 내부적으로 **StateObject**와 **StateRecord**라는 추상화 계층을 둔다.

#### **StateObject**

![](images/03.webp){width=100 align=center}

`StateObject`는 스냅샷에 인식되는(state-aware) 모든 상태 객체가 구현하는 인터페이스다. 이 인터페이스의 주요 책임은 **여러 시점의 상태를 담는** `StateRecord`**들을 관리하는 것**이다.

위 코드에서 확인할 수 있듯이 새로운 `StateRecord`를 리스트의 맨 앞에 추가하는 기능을 제공하는 `prependStateRecord` 와 스냅샷 충돌이 발생했을 때 서로 다른 `StateRecord`들의 상태를 병합하는 로직을 담고 있는 `mergeRecords` 메소드를 가지고 있다.

#### **StateRecord**

![](images/04.webp){width=100 align=center}

`StateRecord`는 하나의 상태가 **특정 스냅샷 시점에서 어떤 값이었는지**를 담는 객체다. 즉, `StateObject`가 여러 시점의 상태를 관리하기 위한 구성 요소라면, `StateRecord`는 그 시점의 구체적인 상태를 실제로 저장하는 기록 단위이다.

`StateRecord`들은 `next` 포인터를 가지고 서로 연결되며, `StateObject`는 이 연결 리스트의 헤드(`firstStateRecord`)를 관리한다. `next`를 통해 이전 상태 기록 뒤에 새로운 기록을 연결할 수 있다.

또한, 각 `StateRecord`는 자신이 어느 스냅샷 시점에 생성되었는지를 나타내는 `snapshotId`를 가진다. 이를 통해 스냅샷 별로 어떤 상태를 참조해야 하는지 결정할 수 있다.

그리고 상태의 복사 및 생성을 위해 아래 두 함수를 지원한다.

- `assign(value: StateRecord)`: 동일한 `StateObject` 타입을 공유하는 다른 `StateRecord`로부터 상태 값을 복사한다. 이를 통해 기존 기록을 재활용하거나 새로운 스냅샷 상태를 손쉽게 설정할 수 있다.
- `create()`: 현재 기록과 같은 유형의 새로운 `StateRecord` 인스턴스를 생성한다. 새로운 스냅샷 버전을 만들 때 기존 기록을 템플릿으로 사용하여 새 상태 레코드를 만들 수 있다.

## Effect API에서는 어떻게 사용되고 있을까? (feat. derivedStateOf)

의도하지 않은 SideEffect를 방지하기 위해 개발자는 **Effect API**의 여러가지 코드들을 활용할 수 있다. 가장 흔이 쓰이는 `LaunchedEffect`나 `derivedStateOf`를 예시로 들 수 있을 것 같다. 이번 글에서는 `derivedStateOf`에서 Snapshot 시스템이 어떻게 사용되고 있는지 분석해보겠다.

Effect API에 대한 내용은 공식문서([Compose의 부수 효과](https://developer.android.com/develop/ui/compose/side-effects?hl=ko))에서 자세히 확인 할 수 있다.

먼저 개념부터 살펴보자. `derivedStateOf`는 다른 상태를 기반으로 파생된(derived) 상태를 정의하여, 원본 상태가 바뀔 때만 파생 상태를 업데이트하고, 변경 전파를 최소화하기 위해 사용된다.

예를 들어 버튼의 활성화 여부(A)를 어떤 숫자(B)에 대한 조건을 통해 조절하는 상황에서, 숫자(B)가 변경될 때마다 버튼의 활성화 여부(A)의 값이 지속적으로 업데이트 된다면, 불필요하게 A의 상태를 업데이트하게될 수 있다. 이런 경우에 `derivedStateOf`를 통해 불필요한 상태 업데이트를 방지할 수 있다. 자세한 내용은 [해당 아티클](https://velog.io/@beokbeok/%EC%96%B8%EC%A0%9C-derivedStateOf%EB%A5%BC-%EC%8D%A8%EC%95%BC%ED%95%A0%EA%B9%8C)에 나와있다.

이제 내부 구조를 뜯어보자.

![](images/05.webp){width=100 align=center}

여기서 `derivedStateOf`는 `calculation` 람다를 인자로 받아 `DerivedSnapshotState` 인스턴스를 생성한다. 이 `DerivedSnapshotState`가 우리가 설명하고 있는 `StateObject`와 `StateRecord` 메커니즘을 활용하는 핵심 클래스다.

![](images/06.webp){width=100 align=center}

여기서 `DerivedState.Record<T>`는 파생 상태의 한 시점(버전) 정보를 담는 구조를 정의한다. `interface Record<T>`에서 `currentValue`와 `dependencies`를 통해 해당 시점에 계산된 값과 참조한 `StateObject` 집합을 나타낸다. `dependencies` 필드가 바로 다양한 `StateObject`를 추적해, 이 파생 상태가 어떤 다른 상태에 의존하는지 알려주는 핵심이다.

![](images/07.webp){width=100 align=center}

- `DerivedSnapshotState<T>`는 `StateObjectImpl()`를 상속받아 `StateObject` 역할을 한다.
- `private var first: ResultRecord<T>` 필드는 최신 상태를 나타내는 `StateRecord`를 가리킨다. 여기서 `first`가 바로 `StateObject`가 관리하는 레코드 리스트의 첫 번째 노드다.

![](images/08.webp){width=100 align=center}

`StateObject`로서 `DerivedSnapshotState`는 `prependStateRecord()`를 통해 새로운 레코드를 리스트 맨 앞에 추가할 수 있으며, `firstStateRecord`를 통해 현재 최신 레코드에 접근한다.

이 부분이 `StateObject` 역할의 핵심으로, 새로운 스냅샷 상태를 추가할 때마다 `ResultRecord`를 prepend 하는 방식으로 다중 버전을 관리한다.

![](images/09.webp){width=100 align=center}

여기서 중요한 포인트는 `ResultRecord<T>`가 `StateRecord`를 상속받고 `DerivedState.Record<T>`를 구현하고 있다는 점이다.

- **StateRecord**: 특정 스냅샷 시점의 상태 값을 나타내는 추상 계층
- **DerivedState.Record\<T**\>: 파생 상태에서 필요한 인터페이스로, `currentValue`와 `dependencies`를 제공

`ResultRecord<T>`는 이 둘을 결합한 구현체로서, `result` 필드에 파생 계산 결과를 저장하고, `dependencies` 필드에 파생 계산 시 읽힌 `StateObject`들을 추적한다. 또한 `assign()`와 `create()` 메서드를 통해 기존 레코드를 재사용하거나 새로운 레코드를 만들 수 있다:

![](images/10.webp){width=100 align=center}

여기서 `assign()`는 이전 레코드의 내용을 복사해오는 역할을 하며, `create()`는 빈 새로운 레코드를 생성한다. 이러한 메서드를 통해 Compose는 불필요한 객체 할당을 줄이거나, 새로운 스냅샷 시점에 맞게 상태 기록을 갱신할 수 있다.

![](images/11.webp){width=100 align=center}

여기서 `isValid()`는 현재 `ResultRecord`가 주어진 `snapshot` 시점에서도 여전히 최신 상태인지 확인한다. 변경된 의존성이 있다면, `result`를 재계산해야 한다. `readableHash()`는 의존성들의 해시를 계산해, 그 의존성들이 변했는지를 감지하는 데 사용한다.

아래와 같은 코드에서 이 로직들이 합쳐져 실제 재계산이 필요할 경우 `Snapshot.observe(...)`를 통해 `calculation()`을 다시 호출하여 새로운 `dependencies`와 `result`를 설정하는 과정을 볼 수 있다.

![](images/12.webp){width=100 align=center}

![](images/13.webp){width=100 align=center}

이 `currentRecord()` 메서드 내에서 `isValid()`를 확인하고, 유효하지 않다면 `Snapshot.observe` 블록을 통해 다시 `calculation`을 실행하면서 의존성을 재추적한다. 이 부분이 바로 `derivedStateOf`가 Snapshot 시스템을 적극적으로 활용하는 핵심이다. 재계산 시에 `StateObject`를 읽으면 Snapshot 시스템이 이를 추적하고, 이후 의존하는 상태가 변하면 파생 상태를 다시 invalidate(무효화)하고 재계산을 유도한다.

마지막으로 정리를 해보겠다.

1. **StateObject** (`DerivedSnapshotState`)
   - `firstStateRecord`와 `prependStateRecord()` 메서드 구현 부분: 파생 상태의 여러 시점(`ResultRecord`)을 관리하는 “컨테이너” 역할을 명확히 보여준다.
2. **StateRecord** (`ResultRecord<T>`)
   - `ResultRecord<T>` 클래스 정의 부분: `result`, `dependencies`, `assign()`, `create()` 메서드를 통해 특정 스냅샷 시점의 파생 결과를 저장하고 관리한다.
   - `isValid()` / `readableHash()` 메서드: 이 레코드가 현재 스냅샷에도 유효한지 검사하고, 필요 시 재계산을 트리거하는 로직이 담겨 있다.
3. **재계산 흐름** (`currentRecord()`, `Snapshot.observe`)
   - `currentRecord()` 메서드 구현 부분: 레코드가 유효하지 않을 경우 `Snapshot.observe`를 통해 다시 `calculation`을 실행하고, 이 과정에서 어떤 `StateObject`가 읽혔는지 Snapshot 시스템이 추적한다.
   - 재계산 후 `dependencies` 갱신, `result` 갱신, 그리고 필요 시 새로운 `StateRecord`를 prepend 하는 단계를 통해 MVCC 기반 스냅샷 시스템이 파생 상태를 항상 최신으로 유지한다.

이렇게 원본 코드블록의 특정 클래스와 메서드를 지적하며 설명함으로써, `derivedStateOf`가 Snapshot 시스템(`StateObject`와 `StateRecord`)을 어떻게 사용하고 있는지 구체적으로 이해할 수 있다. 이 구조 덕분에 파생 상태는 의존하는 상태 변경에 반응하고, 필요할 때만 재계산함으로써 성능과 일관성을 동시에 확보한다.

## 소감

평소 자주 사용하던 mutableStateOf, derivedStateOf에서 이런 복잡한 내부 로직을 가지고 있었는지 상상도 하지 못하였다. 이 아티클을 작성하며 많은 것을 배운 것 같다. 소프트 웨어적인 솔루션은 항상 자료구조와 면밀하게 얽혀 있고, 꾸준히 변화하는 개발 세상에서 살아남으러면 결국 기본에 충실해야 된다는 것을 깨달았다. 현실에 안주하지 않고 항상 기본기를 연마하는 개발자가 되겠다 다짐하며 글을 마무리하겠다.
