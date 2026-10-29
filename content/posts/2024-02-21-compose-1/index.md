---
title: [Compose] Card에 Ripple Effect 제거하기
date: 2024-02-21
category: 안드로이드
summary: [트러블 슈팅] compose card의 ripple 효과 제거하는 방법
thumbnail: images/thumbnail.webp
draft: false
---

## 문제점

![](images/01.webp){width=50 align=center}

캘린더 안의 셀(DayItem)의 터치 시 Ripple Effect가 발생하는데, 이 Ripple Effect가 셀 border 바깥까지 적용되어 Ripple Effect를 없애보고자 함.

(RoundShape Border와 Shape를 주었는데도, 터치 시 정사각형의 Ripple Effect가 발생했는데, 이 이유는 뭔지 모르겠다.. - 추후 탐구해볼 예정)

### 기존 코드

```
Card(
		shape = RoundedCornerShape(6.dp),
		colors = CardDefaults.cardColors(
                    containerColor = when {
                        isToday -> MaterialTheme.colorScheme.primary
                       else -> MaterialTheme.colorScheme.background
                       },
                   ),
		border = BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                             ),
                modifier = modifier
                    .aspectRatio(1f)
                    .alpha(if (!isCurrentMonth) 0f else 1f)
                    .clickable(
                        enabled = isCurrentMonth,
                        onClick = { onDayClick(date) },
                        )
```

코드가 꽤 길지만 이 Card의 쓰임새는 다음과 같다.

- 6dp 만큼 Round한 Border와 Shape를 가짐
- 상황에 따라 containerColor와 border의 색깔이 다름
- 보고 있는 달에 해당되면 클릭이 가능 (2월 달력인데, 숨겨져 있는 1월의 날짜를 클릭하는 경우 클릭이 되지 않음)

## 방법 1 : indication = null 추가

```kotlin
.indication(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                )
```

- `clickable`에 `indication` 속성이 사라졌다.
- `modifier`에서 `Modifier.indication`을 추가하였다.
  - 먼저 유저의 상호작용을 추적하기 위하여 `interactionSource` 객체를 생성
  - 시각적인 피드백을 제공하는 역할을 하는 `indication`을 `null`로 적용한다.

⇒ 현재 Jetpack Compose 버전에서는 indication 속성을 null로 지정하여도 Ripple 효과가 제거되지 않는다.

## 방법 2 : `clickable()` → `pointerInput()`으로 대체

```kotlin
.pointerInput(Unit) {
            detectTapGestures { offset ->
                if (isCurrentMonth) {
                    onDayClick(date)
                }
            }
```

- 기존 `.clickable()`을 `pointerInput()`으로 대체하였다. (터치 이벤트를 감지하고 처리하는데 사용함)
- `detectTapGestures`함수를 호출하여 원하는 제스처를 핸들링 하고자 했다.

⇒ 일시적인 Ripple 효과는 제거되나, 다른 월로 넘어가니 아예 아이템 터치가 막혀버렸다.

## 방법3 : NoRippleTheme 적용

```kotlin
private object NoRippleTheme : RippleTheme {
    @Composable
    override fun defaultColor() = Color.Unspecified

    @Composable
    override fun rippleAlpha() = RippleAlpha(0.0f, 0.0f, 0.0f, 0.0f)
}
```

- 해당 파일에만 사용할 것이므로 private object 생성
- 기본 `Color`를 `Unspecified`로, `Alpha`를 모두 0.0f로 부여하였다.

```kotlin
CompositionLocalProvider(
        LocalRippleTheme provides NoRippleTheme,
    ) {
			Card ...
```

- Card가 담겨있는 코드 부분을 `NoRippleTheme`을 적용한 `CompositionLocalProvider`에 담아 Ripple 효과를 제거하였다.

=\> 실 기기에서는 잘 적용이 되나, API 33 버전의 Emulator에서 실행해봤을 때는 잘 적용 되지 않았다.

## 해결 방법 : NoRippleClickable 확장 함수 적용

```kotlin
inline fun Modifier.noRippleClickable(


 modifier: Modifier = Modifier,


    crossinline onClick: () -> Unit,
): 
Modifier = composed {
    clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
    ) {
        onClick()
    }
}
```

- `inline`함수로 선언하여, 함수 호출 시 오버헤드를 줄이도록 하였다. `inline`함수는 컴파일 시점에 함수를 호출하는 부분에 함수를 직접 삽입한다.
- `crossinline`키워드로 람다식의 비-로컬 반환을 금지시킨다.
- 리플 효과를 제거하고, 유저와의 상호작용 상태를 추적하는 `MutableInteractionSource`를 추가하는 `clickable Modifier`를 적용한다.
- 입력으로 받은 `onClick`람다 함수를 실행한다.

```kotlin
modifier = modifier
                .aspectRatio(1f)
                .alpha(if (!isCurrentMonth) 0f else 1f)
                .noRippleClickable(
                    onClick = { if (isCurrentMonth) onDayClick(date) else null },
                ),
```

⇒ 해당 확장 함수를 기존 clickable자리에 대체하였더니, 실 기기 및 Emulator에서도 정상 작동하였다.

### 배운 점

기존 `clickable`에 `indication`을 넣어보고자 했던 시도를 좀 더 매끄럽게 했으면 문제가 해결되었을 것 같다. 하지만 확장성을 생각해서 애초에 확장 함수를 만들어보고자 했으면, 더 좋은 시도가 되었을 것 같다.

해당 문제를 해결하며 Card의 세 가지 종류에 대해 알게 되었고, `clickable`과 `indication`에 대해서 알게 되었다.

애초에 왜 Ripple Effect가 기대한 대로 적용되지 않았는지 더 탐구해보려 한다.

### 참고 자료

[Jetpack Compose Card - Jetpack Compose World](https://jetpackcomposeworld.com/jetpack-compose-card/)
