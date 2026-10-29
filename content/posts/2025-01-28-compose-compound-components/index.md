---
title: [Compose] Compound Component 패턴으로 복잡한 UI 설계하기
date: 2025-01-28
category: 안드로이드
summary: Compose에서 Compound Component 패턴을 활용하는 방법과 장단점에 대해 정리하는 글입니다.
thumbnail: images/thumbnail.webp
draft: false
---

## 들어가며

최근 진행하고 있는 프로젝트에서 기존에 Java/Kotlin + XML 방식으로 구현되어 있었던 디자인 시스템을 Jetpack Compose로 마이그레이션하는 작업을 수행하고 있다. 이번 글에서는 기존에 조건문 지옥으로 구현되어 있던 레거시 코드를 **Compound Component 패턴**으로 리팩토링하는 과정과 그 이점을 예시와 함께 다뤄보겠다. 이에 더불어 React에서 사용하고 있는 Context API에 대해 간단히 알아보며 글을 마무리하겠다.

김수현님의 [Jetpack Compose UI 조합(Composition)하기 심화](https://medium.com/@wisemuji/jetpack-compose-ui-%EC%A1%B0%ED%95%A9-composition-%ED%95%98%EA%B8%B0-%EC%8B%AC%ED%99%94-33910e8f09df) 아티클에서 Compound Component 패턴을 배워 이 글을 작성했다. 패턴에 대해 더 자세히 학습하고 싶거나, 패턴과 관련된 소프트웨어적인 관점을 살펴보고 싶다면 아티클을 정독해보는 것을 추천한다. 정말 도움을 많이 받은 글이다.

## 문제 상황

![](images/01.webp){width=100 align=center}

디자인 시스템이 있는 환경에서 개발을 진행한다면, 위와 같은 디자인을 전달받을 것이다. 만약 위와 같은 버튼이 필요할 때마다 직접 구현한다면 리소스를 낭비할 것이고, 매번 다르게 구현할 위험이 있다. 따라서 **디자인 시스템을 미리 구현해서 코드의 일관성을 지키고 리소스 낭비를 방지**할 수 있다. 디자인 시스템이 없는 환경일지라도, 여러 곳에서 공통적으로 사용되는 컴포넌트를 미리 추출해 구현해 놓는다면 많은 이점이 존재할 것이다.

처음에 안드로이드 개발을 시작할 때 폰트나 컬러 시스템을 미리 구현해본 경험이 있었다. 이런 작업들도 모두 디자인 시스템을 구현했다고 볼 수 있으며, 필자는 디자인 시스템 모듈을 별도로 만들고 이 모듈을 UI가 존재하는 모든 모듈에 기본적으로 의존하게 설정해 놓는다.

디자인 시스템을 구현하다보면, 특정 컴포넌트를 제작할 때 너무 많은 경우를 맞닥뜨려 어떻게 좋은 방식으로 고민할 때가 있다. 보통 컴포즈를 처음 공부할 때 TopAppBar를 구현하다가 이런 상황을 자주 마주치는데, 그때는 [**Slot API**](https://developer.android.com/develop/ui/compose/layouts/basics?hl=ko#slot-based-layouts)라는 아주 좋은 방식이 존재하여 적용해볼 수 있다. 하지만 Slot API로는 부족한, 훨씬 **복잡한 UI 로직이 존재할 때는 Slot API는 오히려 독이 될 수 있다**. 이 경우는 앞서 들어가기에서 설명한 [아티클](https://medium.com/@wisemuji/jetpack-compose-ui-%EC%A1%B0%ED%95%A9-composition-%ED%95%98%EA%B8%B0-%EC%8B%AC%ED%99%94-33910e8f09df)에도 잘 나와있으니 참고하길 바란다.

어떤 경우인지 잘 와닿지 않은 분들을 위해 예시를 하나 들겠다. 앱 개발을 하다 보면 구현할 일이 아주 잦은 다이얼로그이다. 다이얼로그인 경우에는 보통 **제목과 내용, 그리고 확인 버튼과 취소 버튼**이 존재한다. 하지만 여기서 **한 가지 변수가 추가**되었다고 가정하자.

![](images/02.webp){width=100 align=center}

위와 같이 서브 버튼과 메인 버튼이 **가로로 정렬되는 경우와 세로로 정렬되는 경우가 추가**되었다. Slot API를 통해 구현이 가능하지만, Composable 컴포넌트를 4개 만들어야 한다. 다른 방법으로 조건문을 사용하면 결합도가 높아지고 추후 조건이 추가되면 조건문이 복잡해지게 되므로 좋은 방법은 아니지만, 당장은 편한 방법이기도 하고 조건이 추가될 여지를 현재 알 수가 없기에 조건문 방식을 택하는 사람이 많을 것이라 생각된다. (이에 대한 내용도 참고한 아티클에 자세히 나와있다. 또한, Slot API로 구현하는 것이 잘못됐다는 것은 절대 아니다.)

![](images/03.webp){width=100 align=center}

많은 개발자들은 미래에 일어날 일을 알기 어려워 확장성을 잘 생각하지 않는다. 위와 같이 **네거티브 버튼과 서브 버튼**이 존재하는 케이스가 추가되었다. 심지어 이 경우에는 가로 정렬일 때 서브 버튼은 오른쪽에 위치한다. (메인 버튼이 존재했던 경우에는 왼쪽에 있었다.) 또, 제목 상단에 **이미지**가 추가되는 케이스도 존재한다. 버튼이 아예 존재하지 않거나, 제목만 존재하는 케이스도 있다. 이렇게 되면 **총 14가지의 케이스**가 존재하게 된다.

**Slot API를 사용하자니 많은 슬롯을 뚫어놔야 하고, 조건문 방식을 사용하자니 조건문 지옥에 빠지게 된다**. 두 방법 다 UI 추가/변경 사항이 일어날 때, Slot을 추가하거나 조건문을 추가해야 하므로 유지보수 측면에 별로 좋지 않다. 또한, 부모와 자식 컴포넌트 간에 결합도가 커지므로 서로 복잡하게 연결될 수 있다. 필자가 마주한 상황에서는 조건문 지옥으로 구현되어 있었다.

## Compound Component 패턴 활용

> [**Compound Component 패턴**](https://www.patterns.dev/react/compound-pattern): **부모 컴포넌트의 상태를 자식 컴포넌트들과 공유하여 컴포넌트 간 결합도를 낮추고, UI와 비즈니스 로직을 분리하는 데 유용한 패턴이다**.

현재 문제 상황에서 **부모와 자식의 결합도를 낮추고 UI 구조 변경에 대응하기 훨씬 유연**한 패턴이다. React의 디자인 패턴 중 하나이고 **React에서는** [**Context API**](https://react.dev/learn/passing-data-deeply-with-context)**를 통해 구현하는 것이 일반적이지만, Compose에서는 Lambda Receiver를 활용하여 구현할 수 있다**. Context API는 이 글의 끝 부분에서 한번 더 다룰 예정이다.

부모 컴포넌트의 상태를 자식 컴포넌트와 공유하는 부분이 이 패턴의 핵심이다. 예시를 통해 더 자세히 알아보겠다.

### 1. Scope 정의

```kotlin
@Stable
interface MyDialogScope {
    val onDismissRequest: () -> Unit

    val imageRes: Int?
    val imageTint: Color?

    val title: String?
    val message: String?

    val isButtonLayoutVertical: Boolean

    val negativeButtonText: String?
    val onNegativeClick: () -> Unit

    val subButtonText: String?
    val onSubClick: () -> Unit

    val mainButtonText: String?
    val onMainClick: () -> Unit
}
```

- 이 scope에서는 Dialog에서 관리해야하는 모든 데이터와 함수를 정의해둔다. 자식 컴포넌트는 이 scope만 알면 부모의 상태에 자유롭게 접근하여 UI를 구성할 수 있다.
- 추가로 `@Stable` 을 활용하여 MyDialogScope를 구현한 클래스에서 불필요한 리컴포지션이 발생하지 않도록 최적화하였다.

```kotlin
class DefaultMyDialogScope(
    override val onDismissRequest: () -> Unit,
    override val imageRes: Int?,
    override val imageTint: Color?,
    override val title: String?,
    override val message: String?,
    override val isButtonLayoutVertical: Boolean,
    override val negativeButtonText: String?,
    override val onNegativeClick: () -> Unit,
    override val subButtonText: String?,
    override val onSubClick: () -> Unit,
    override val mainButtonText: String?,
    override val onMainClick: () -> Unit,
) : MyDialogScope
```

- MyDialogScope를 단순히 구현한 디폴트 클래스를 활용하여 자식들에게 전달하는 **scope 구현체 역할**을 담당하게 한다. 이 클래스를 Dialog 컴포저블 내부에서 `remember`로 생성해 활용할 예정이다.

### 2. 자식 컴포넌트 구현

Compound Component 패턴의 핵심인 자식 컴포넌트를 살펴보겠다. **각 자식 컴포넌트는 MyDialogScope를 리시버로 받아 scope에 정의된 프로퍼티를 활용해 UI를 그릴 수 있다**.

```kotlin
@Composable
fun MyDialogScope.MyDialogTitle(
    modifier: Modifier = Modifier,
) {
    val dialogTitle = title ?: return
    val textStyle = if (message.isNullOrBlank()) {
        typography.subHeading1A
    } else {
        typography.subHeading2A
    }

    Text(
        text = dialogTitle,
        modifier = modifier,
        style = textStyle,
        color = colors.gray900,
        textAlign = TextAlign.Center,
    )
}
```

- 이 컴포저블 함수는 제목을 나타내는 자식 컴포넌트이다. 각 자식 컴포넌트에는 자신이 필요한 부모의 값들만 가져와서 UI 로직을 구성할 수 있다.
- 위 제목 컴포넌트는 메시지가 있을 때와 없을 때 타이포그래피가 다르기에 해당 작업을 이 자식 컴포넌트가 담당하게 하였다.

```kotlin
@Composable
fun MyDialogScope.MyDialogNegativeButton(
    modifier: Modifier = Modifier,
) {
    val negativeText = negativeButtonText ?: return

    MyThrottleButton(
        modifier = modifier,
        text = negativeText,
        onClick = onNegativeClick,
        level = EviButtonLevel.Negative,
    )
}
```

- 위 컴포저블 함수에서는 네거티브 버튼을 구현했다. 네거티브 버튼이 존재하지 않고 서브-메인 버튼만 존재하는 경우를 대비하기 위해 코틀린의 엘비스 연산자 `?:` 를 활용하여 네거티브 버튼에 들어갈 문구가 존재하지 않으면 얼리 리턴한다. 이런 방식으로 해당 자식 컴포넌트가 존재하지 않을 때와 존재할 때 모두 부모의 값을 조정하여 대응할 수 있다.

### **3. MyDialog 함수: 부모 컴포넌트**

```kotlin
@Composable
fun MyDialog(
    show: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    message: String? = null,
    imageRes: Int? = null,
    imageTint: Color? = null,
    isButtonLayoutVertical: Boolean = false,
    negativeButtonText: String? = null,
    subButtonText: String? = null,
    mainButtonText: String? = null,
    onNegativeClick: () -> Unit = {},
    onSubClick: () -> Unit = {},
    onMainClick: () -> Unit = {},
    content: @Composable MyDialogScope.() -> Unit,
) {
    if (!show) return

    val scope = remember(


















        onDismissRequest, 
imageRes, imageTint,
        title,
        message,
        isButtonLayoutVertical,
        negativeButtonText,

        subButtonText,
        mainButtonText,
        onNegativeClick,
        onSubClick,


        onMainClick,





    ) {
        DefaultMyDialogScope(
            onDismissRequest = onDismissRequest,
            imageRes = imageRes,
            imageTint = imageTint,
            title = title,
            message = message,
            isButtonLayoutVertical = isButtonLayoutVertical,
            negativeButtonText = negativeButtonText,
            onNegativeClick = onNegativeClick,
            subButtonText = subButtonText,
            onSubClick = onSubClick,
            mainButtonText = mainButtonText,
            onMainClick = onMainClick,
        )
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier.wrapContentHeight(),
            shape = RoundedCornerShape(8.dp),
            color = colors.white,
            shadowElevation = 2.dp,
        ) {
            scope.content()
        }
    }
}
```

- 위 부모 컴포넌트에서는 Compose에서 제공하는 `Dialog`과 `Surface`로 기본적인 다이얼로그의 골격을 생성한다.
- 또한 `remember` 블록에서 `DefaultMyDialogScope` 인스턴스를 생성하여 프로퍼티가 변경되지 않으면 scope를 재생성하지 않도록 최적화한다.
- 마지막으로, scope의 content 람다(`@Composable MyDialogScope.() -> Unit`)를 활용하여 자식 컴포넌트들에게 실제 UI 구조를 맡기는 구조를 완성한다.

이로써 부모는 공통 상태 관리 및 UI 골격만 담당하고 자식에게 UI 구조를 맡기는, 결합도를 낮추는 구조를 만들 수 있다.

### **4. 디폴트 레이아웃**

위에서 만든 부모 컴포넌트와 자식 컴포넌트를 활용하여 다이얼로그가 필요한 시점에 매번 구현한다면 조금 아쉬운 구조가 될 것이다. 디자인 시스템의 이점을 살려 미리 정의된 구조들을 구현해둔다면 이 Compound Component 패턴의 장점을 극대화할 수 있다.

```kotlin
@Composable
fun MyDialogScope.MyDialogDefaultLayout() {
   
    val hasMessage = !message.isNullOrBlank()

    ... 생략 ...

    Column(
        modifier = columnModifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MyDialogImage()
        MyDialogTitle()
        MyDialogMessage()

        if (hasButton) {
            Spacer(Modifier.height(24.dp))

            if (isButtonLayoutVertical) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    MyDialogMainButton(Modifier.fillMaxWidth())
                    MyDialogNegativeButton(Modifier.fillMaxWidth())
                    MyDialogSubButton(Modifier.fillMaxWidth())
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MyDialogNegativeButton(modifier = Modifier.weight(1f))
                    MyDialogSubButton(modifier = Modifier.weight(1f))
                    MyDialogMainButton(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
```

- 기존에 구현해 둔 자식 컴포넌트들을 활용하여 14가지 다이얼로그 케이스를 미리 정의할 수 있다.
- 자식 컴포넌트에서 이미 각자의 존재여부와 커스텀(폰트, 간격 등)을 부모 컴포넌트의 상태(Scope)를 참조하여 직접 담당하기에, 이 디폴트레이아웃에서는 그저 배치만 담당하면 된다.
- `hasButton`이나 `isButtonLayoutVertical`과 같은 조건문도 자식 컴포넌트에게 담당하는 구조로 리팩토링이 가능하지만, 필자는 이 정도 조건문은 괜찮다고(가독성과 유지보수 측면에서) 판단하여 그대로 두었다.

### 5. `PreviewParameterProvider`로 확인

14가지 케이스의 다이얼로그를 각각 Preview를 통해 확인한다면 중복되는 코드가 많아질 것이고 컴포저블 함수도 많아져 코드의 길이가 굉장히 길 것이다. 이런 경우에 컴포즈에서 기본적으로 제공하는 [PreviewParameterProvider](https://developer.android.com/develop/ui/compose/tooling/previews?hl=ko#preview-data)를 활용하면 쉽게 프리뷰를 확인할 수 있다.

```kotlin
data class MyDialogPreviewData(
    val title: String,
    val message: String?,
    @DrawableRes val imageRes: Int?,
    val mainButtonText: String? = null,
    val negativeButtonText: String? = null,
    val subButtonText: String? = null,
    val isButtonLayoutVertical: Boolean = false,
)
```

- 먼저 프리뷰에서 필요한 데이터들만 정의해둔다. 버튼 클릭 시 발생하는 이벤트 등은 프리뷰에서 굳이 확인할 필요가 없으므로 생략이 가능하다.

```kotlin
class MyDialogPreviewParameterProvider : PreviewParameterProvider<MyDialogPreviewData> {
    override val values: Sequence<MyDialogPreviewData> = sequenceOf(
        // 1) 제목과 긴 main 버튼만 있는 경우
        MyDialogPreviewData(
            title = "제목과 메인 버튼",
            message = null,
            imageRes = null,
            mainButtonText = "확인",
            negativeButtonText = null,
            subButtonText = null,
            isButtonLayoutVertical = false,
        ),

        // 2) 제목과 sub, main 버튼이 horizontal로 있는 경우
        MyDialogPreviewData(
            title = "제목과 서브, 메인 버튼(가로)",
            message = null,
            imageRes = null,
            mainButtonText = "확인",
            subButtonText = "취소",
            negativeButtonText = null,
            isButtonLayoutVertical = false,
        ),

        ... 생략 ...
       
        // 14) 제목과 내용만 있는 경우
        MyDialogPreviewData(
            title = "제목과",
            message = "내용만 존재하는 경우",
            imageRes = null,
            mainButtonText = null,
            negativeButtonText = null,
            subButtonText = null,
            isButtonLayoutVertical = false,
        ),
    )
}
```

- `PreviewParameterProvider`를 구현하고 테스트가 필요한 14가지 샘플 데이터를 시퀀스로 반환하는 클래스를 만든다. 각 다이얼로그에 필요한 데이터만 기입하면 된다.

```kotlin
@Preview(showBackground = true)
@Composable
fun AllDialogCasePreview(
    @PreviewParameter(MyDialogPreviewParameterProvider::class) data: MyDialogPreviewData,
) {
    MyTheme {
        MyDialog(
            show = true,
            onDismissRequest = { },
            imageRes = data.imageRes,
            title = data.title,
            message = data.message,
            isButtonLayoutVertical = data.isButtonLayoutVertical,
            mainButtonText = data.mainButtonText,
            negativeButtonText = data.negativeButtonText,
            subButtonText = data.subButtonText,
            onMainClick = { },
            onNegativeClick = { },
            onSubClick = { },
        ) {
            MyDialogDefaultLayout()
        }
    }
}
```

- 이제 `@PreviewParameter` 주석이 있는 매개변수를 추가하여 컴포저블 프리뷰 함수에 샘플 데이터를 전달하기만 하면 된다.

![](images/04.webp){width=100 align=center}

미리 정의해둔 데이터를 통해 컴포저블 프리뷰 함수 하나면 위와 같은 깔끔한 프리뷰를 확인할 수 있으니 상당히 유용하다.

### 6. 실제 적용

```kotlin
    if (state.showDialog) {
        MyDialog(
            show = true,
            onDismissRequest = {},
            title = "제목",
            message = "메세지",
            subButtonText = "취소",
            onSubClick = onCancelClick,
            mainButtonText = "확인",
            onMainClick = onConfrimClick,
        ) {
            MyDialogDefaultLayout()
        }
    }
```

- 이제 필요한 곳에서 내가 보여주고 싶은 자식 컴포넌트들에 대한 정보만 기입하면 쉽게 다이얼로그를 사용할 수 있다.
- 만약 기존에 정의해 둔 14가지 케이스가 아닌 다른 조합과 배치가 필요하다면, 사용부에서 디폴트 레이아웃 대신 자식 컴포넌트들을 직접 배치하는 UI 구조를 만들면 된다.

## Slot API vs 조건문 vs Compound Component

지금까지 소개한 Compound Component 패턴이 당연히 그렇듯 항상 유용한 것은 아니다. 언제 사용하기에 적절한지 알아보기 위해 기존에 널리 사용되던 Slot API, 조건문 방식과 장단점을 비교해보았다.

### **1. Slot API 방식**

- **장점**: “**간단히 원하는 부분만 교체**”할 수 있어, 범용적인 **치환**에 편하다.
- **단점**: **UI 구조를 완전히 바꾸거나**, 슬롯 개수가 많아지면 “슬롯 지옥”이 생길 수 있다.
  - 예시: “버튼이 2개일 때/3개일 때/수직/수평” 등 레이아웃 변화가 자주 생기면, “slot1, slot2, slot3”를 다 정의하거나, 각 슬롯 내부에서 또 조건문을 써야 하므로 복잡해질 수 있다.

### **2. 조건문 기반 방식**

- **장점**: 초창기에는 매우 **직관적**이고 **구현 속도**가 빠를 수 있다.
- **단점**: 요구사항이 늘어날수록 분기가 계속 쌓여서 **가독성 저하**와 **유지보수성 악화**로 이어진다.

### **3. Compound Component 패턴**

- **장점**
  1. 조건문이 한곳에 몰리지 않고, 자식들이 알아서 “자신의 존재여부(ex. 텍스트 있나/없나)”를 판단한다.
  2. 부모-자식 간 결합도가 낮아서, **UI 구조 바꾸기**(버튼이 수직/수평, 이미지 위치 변경 등)에 대응하기 훨씬 유연하다.
  3. Slot API보다 **전체 레이아웃** 변경이 수월하다.
  4. **확장**에 유리해, “디자인 시스템 + Default Layout”을 만들어두고, 특수한 배치는 사용자가 직접 조립할 수 있다.
- **단점**
  1. 람다 리시버에 익숙지 않은 경우, 다른 방법들에 비해 러닝 커브가 상대적으로 높다.
  2. “버튼 1개만 교체” 같은 아주 간단한 시나리오에는, 오히려 **Slot API**가 더 직관적일 수도 있다.

위 방법들 말고도 다른 방법들이 존재하지만, 이 장단점들을 이해하며 적절하게 사용한다면 유지보수와 가독성, 확장성에 용이한 디자인 시스템을 제작할 수 있을 것이다. 필자가 [참고한 아티클](https://medium.com/@wisemuji/jetpack-compose-ui-%EC%A1%B0%ED%95%A9-composition-%ED%95%98%EA%B8%B0-%EC%8B%AC%ED%99%94-33910e8f09df)에도 결론은 **Simple is the Best** 라고 마무리하였다. 더 많은 패턴들을 학습하여 주어지는 상황마다 최적의 솔루션을 제공할 수 있는 개발자가 되어야겠다고 다짐하게 되었다.

## React에서의 Context API와 Props Drilling에 관하여

React의 Compound Component 패턴은 [Context API](https://www.patterns.dev/react/compound-pattern) 방식을 사용하여 구현한다.

```jsx
// 1) Context 생성
const ModalContext = React.createContext();

// 2) 부모(Compound) 역할: Provider 범위
function ModalProvider({ text, onClose, children }) {
    const value = { text, onClose };

    return (
        <ModalContext.Provider value={value}>
          <div className="modal-backdrop">
            <div className="modal-content">
              {children}
            </div>
          </div>
        </ModalContext.Provider>
    );
}

// 3) 자식(Compound)들: Context로부터 직접 상태를 가져옴
function ModalTitle() {
    const { text } = React.useContext(ModalContext);
    return <h2>{text}</h2>;
}

function ModalCloseButton() {
  const { onClose } = React.useContext(ModalContext);
  return <button onClick={onClose}>Close</button>;
}

// 4) 사용 예제
function MyScreen() {
    const [showModal, setShowModal] = React.useState(false);

    return (
        <div>
          <button onClick={() => setShowModal(true)}>Show Modal</button>
          {showModal && (
            <ModalProvider
              text="This is a modal"
              onClose={() => setShowModal(false)}
            >
              <ModalTitle />
              <p>Modal Body here</p>
              <ModalCloseButton />
            </ModalProvider>
          )}
        </div>
    );
}
```

코드는 자세히 보지 않아도 된다. 결국 우리가 주목할 것은 Compose의 람다 리시버 방식처럼 부모의 state를 자식 컴포넌트가 사용하여 **Props Drilling을 제거하는**, 즉 **Modal 범위에서만 사용하는 전역 상태처럼 사용되는 Context**를 사용한다는 방식을 사용하고 있다는 것이다.

결국 Compose와 React의 두 방식 모두 **부모의 공통 상태를 여러 자식이 쉽게 참조**할 수 있도록 만들며, **조합형 UI** 철학을 공유한다는 점에서 유사하다.

### Props Drilling을 반드시 제거해야 하는가?

![](images/05.webp){width=100 align=center}

Props Drilling은 **부모에서 자식까지 여러 단계에 걸쳐, 사실상 중간 단계에서 사용하지 않는 props도 계속 넘겨야 하는 상황**을 뜻한다. Props Drilling의 문제점은 데이터를 부모에서 자식으로 전달 할 때 그 행방의 추적이 어렵다는 것이다.

이와 관련해 최근 주목받고 있는 [토스의 지침서](https://frontend-fundamentals.com/) 속에서 [Props Drilling에 관련한 토론](https://frontend-fundamentals.com/code/examples/item-edit-modal.html)이 있었다.

![](images/06.webp){width=100 align=center}

보통 Props Drilling을 해결하기 위해 전역 상태 관리와 조합 방식을 사용하는데, **항상 Props Drilling을 제거하는 것이 정답은 아니고 팀과의 컨벤션과 마주친 상황에 따라 적절히 사용해야 한다. 전역 상태 관리도 남발하면 좋지 않다**. 라는 의견이 많았다. 이를 통해 전역 상태를 생성할 시에는 적절한 Context로 생성해야 하고, Compose 개발 시 람다 리시버를 적절히 사용해야겠다는 영감을 받았다.

![](images/07.webp){width=100 align=center}

Compose를 만드신 개발자도 CompositionLocal을 사용하면 모듈 의존성을 깨고 코드의 유지보수성을 낮출 수 있다고 언급한다. 이에 대해 Props Drilling을 항상 제거하는 것이 정답이 아니고 팀과의 적절한 소통을 통해서 통일성을 지키면서 명시적인 전달이 필요할 경우에 적절히 사용해야 한다고 생각한다.

이에 관련해 [React의 전역 상태 관리 라이브러리 전성시대와 관련한 간단한 토론 글](https://github.com/toss/frontend-fundamentals/discussions/5#discussioncomment-11831458)이 있는데 모든 프론트엔드 개발자들이 한 번씩 읽어보았으면 좋겠다.

## 끝으로..

네이버 부스트 캠프 과정 중 iOS 마스터 님이신 JK님이 저자로 참여한 \[개발자 원칙\]이라는 책에서 학습 습관에 대한 주제가 있었다. 이 글에서는 개발자가 앞으로 공부해야 하는 방향성에 대해서 제시하는데, **업무와 관련된 지식들에 대한 학습 비율은 50%, 앞으로 관련될 것 같은 지식들은 30%, 업무와 관련은 없지만 관심 있는 것은 20% 정도 공부하는 것**을 추천한다. Compose를 공부하면서 항상 React에서 많은 지식들을 얻고 있지만 코드는 아예 쓸 줄 모른다. 그래도 꾸준히 공부를 하여 영감을 얻고 시야를 키워야겠다는 다짐을 하며 글을 마무리하겠다.
