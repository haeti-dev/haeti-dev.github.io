---
title: [Android] Edge-to-Edge 이해하고 적용해보기
date: 2024-08-26
category: 안드로이드
summary: 최신 안드로이드 기기에 대응하기 위하여 Edge-to-Edge를 대응하는 방법을 소개합니다.
thumbnail: images/thumbnail.webp
draft: false
---

> 2025/07/21 Updated❗

2025년 8월 31일까지 targetSDK를 35로 업데이트 해야 앱이 내려가지 않고, 리젝당하지 않습니다. 아래와 코드를 themes.xml에 적용하면, 일괄적으로 Edge To Egde 설정을 강제적으로 일시적으로 해제할 수 있습니다. Android14(API34) 이하 버전과 동일하게 시스템 바 영역 밖으로 콘텐츠를 그리지 않는 기존 레거시 동작을 유지할 수 있습니다.

```
<item name="android:windowOptOutEdgeToEdgeEnforcement">true</item>
```

🚨 **주의사항** : **Android 16(API 36) 이상**으로 타겟을 올리면 이 속성은 deprecated 처리되고, 더 이상 동작하지 않아 opt-out이 불가능합니다.

---

Android 15(API 레벨 35) 이상을 타겟팅하는 앱은 기본적으로 시스템 UI 경계를 넘어 전체 디스플레이 영역을 활용한다. 이를 `edge-to-edge`라고 표현한다. 윈도우는 전체 너비와 높이를 포함하고, 시스템 바 뒷 영역 또한 포함한다. (여기서 시스템 바는 상태 표시줄과 네비게이션 바, 캡션 바를 포함한다.)

![](images/01.webp){width=100 align=center caption="노치는 아이폰에만 존재하니 무시하자"}

시중의 많은 앱들이 TopAppBar나 BottomNavigationBar를 가지고 있으며, 이 바는 edge-to-edge를 활용하면 화면의 상단 가장자리까지 확장되어 시스템의 Status bar나 Navigation bar 뒤에 표시될 수 있다.

#### edge-to-edge 레이아웃을 구현할 때 고려해야 할 사항

1. **Edge-to-edge 디스플레이 활성화**: 앱이 SDK 35 이상을 타겟팅하는 경우, 안드로이드 15 이상의 기기에서는 자동으로 edge-to-edge 디스플레이가 활성화된다.
2. **시각적 겹침 처리**: 앱의 일부 뷰가 시스템 바 뒤에 그려질 수 있으므로, 겹침 문제를 해결하기 위해 인셋을 사용해 처리해야 한다.
3. **시스템 바 뒤에 스크림 표시 고려**: 시스템 바 뒤에 스크림(반투명한 오버레이)을 추가하는 것도 고려해볼 수 있다.

![](images/02.webp){width=100 align=center}

> 💡 앱이 SDK 35를 타겟팅할 경우, 안드로이드 15(API 레벨 35) 이상에서는 edge-to-edge가 강제로 적용된다. 만약 앱이 이미 edge-to-edge로 설정되지 않은 경우, 앱의 일부가 가려질 수 있으며, inset을 처리해야 한다. 앱에 따라 중요도가 달라질 수 있다.

#### Android 15

![](images/03.webp){width=100 align=center caption="2024년 기준"}

곧 릴리즈 될 예정이라고 한다. 현재는 베타 서비스이다.

릴리즈되면 출시 초기 [안드로이드 13](https://namu.wiki/w/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C%2013) 이상을 탑재한 스마트폰, 혹은 출시 초기 [안드로이드 11](https://namu.wiki/w/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C%2011)이지만 업데이트를 4회 지원하는 스마트폰에는 지원된다. 기본적으로 안드로이드 11을 탑재한 후 출시한 [갤럭시 S21](https://namu.wiki/w/%EA%B0%A4%EB%9F%AD%EC%8B%9C%20S21)부터 적용되며, [One UI](https://namu.wiki/w/One%20UI) 7.0으로 시작할 예정이다.

출처 : [https://android-developers.googleblog.com/2024/05/the-second-beta-of-android-15.html](https://android-developers.googleblog.com/2024/05/the-second-beta-of-android-15.html)

### Inset이란 무엇인가?

`Inset`은 **화면의 특정 영역이 시스템 UI(예: 상태 바, 내비게이션 바)와 교차하는 부분**을 나타내는 개념이다. 간단히 말해, `Inset`은 **앱의 콘텐츠가 시스템 UI와 겹치는 위치를 정의하는 값**이다. 이러한 교차는 콘텐츠가 시스템 바 뒤에 표시될 수 있음을 의미할 수 있으며, 앱에 시스템 제스처가 적용될 위치에 대한 정보를 제공하기도 한다.

안드로이드의 `WindowInsets` API는 다양한 종류의 `Inset`을 제공하여 앱 개발자가 이러한 겹침 문제를 해결할 수 있게 한다.

`Inset`은 크게 세 가지 유형으로 나뉜다:

#### **1. System Bars Insets**

- **정의**: 시스템 바(상태 바, 내비게이션 바 등)가 화면에 표시되는 Y축 영역을 나타낸다.
- **용도**: 주로 사용자가 탭할 수 있는 영역이 시스템 바에 의해 가려지지 않도록 영역을 이동하거나 패딩을 추가하는 데 사용된다. 예를 들어, 플로팅 액션 버튼(FAB)이 내비게이션 바에 의해 부분적으로 가려질 때, `systemBars` 인셋을 사용해 이 문제를 해결할 수 있다.

![](images/04.webp){width=100 align=center}

위와 같이 네비게이션 바와 FAB가 가려진 상황에서 아래와 같이 대응할 수 있다.

```kotlin
ViewCompat.setOnApplyWindowInsetsListener(fab) { v, windowInsets ->
  val insets = windowInsets.getInsets(**WindowInsetsCompat.Type.systemBars()**)
  // Apply the insets as a margin to the view. This solution sets// only the bottom, left, and right dimensions, but you can apply whichever// insets are appropriate to your layout. You can also update the view padding// if that's more appropriate.
  v.updateLayoutParams<MarginLayoutParams> {
      leftMargin = insets.left,
      bottomMargin = insets.bottom,
      rightMargin = insets.right,
  }

  // Return CONSUMED if you don't want want the window insets to keep passing// down to descendant views.WindowInsetsCompat.CONSUMED
}
```

- `ViewCompat.setOnApplyWindowInsetsListener(fab)` 메서드를 사용하여 FAB에 windowInset 리스너를 설정한다. 이 리스너는 시스템 창 인셋이 변경될 때마다 호출된다.
- `windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())` 를 사용하여 현재 화면의 시스템 바와 교차하는 부분의 인셋 값을 가져온다. 이 인셋 값은 각 측면 (왼쪽, 오른쪽, 하단)에서 시스템 바가 차지하는 공간을 나타낸다.
- `v.updateLayoutParams<MarginLayoutParams> { ... }` 내부에서 뷰(FAB)의 `MarginLayoutParams`를 업데이트한다. 여기서 `leftMargin`, `bottomMargin`, `rightMargin`에 시스템 바의 인셋 값을 할당한다. 이를 통해 FAB가 네비게이션 바에 겹치지 않도록 마진을 조정할 수 있다.
- `WindowInsetsCompat.CONSUMED`를 반환함으로써, 인셋이 FAB의 하위 뷰로 계속 전파되지 않도록 한다. 이는 FAB의 하위 뷰가 없는 경우 중요한 작업은 아니지만, 더 이상 인셋이 필요하지 않음을 명시한다.

![](images/05.webp){width=100 align=center}

위와 같이 FAB와 네비게이션 바가 겹치지 않게 된 것을 확인할 수 있다.

#### **2. Display Cutout Insets**

- **정의**: 일부 디바이스의 화면에 존재하는 컷아웃(예: 노치, 카메라 홀)의 위치와 크기를 나타낸다.
- **용도**: 컷아웃으로 인해 콘텐츠가 가려지지 않도록 패딩을 추가하는 데 사용된다. 예를 들어, 리스트가 화면 상단의 노치에 의해 잘리지 않도록, 상단에 패딩을 추가하는 방식으로 컷아웃 인셋을 활용할 수 있다.

디스플레이 컷아웃이 있는 기기에서 기본적으로 앱의 콘텐츠는 컷아웃 영역을 포함한 전체 화면에 그려진다. 이로 인해 중요한 UI 요소나 콘텐츠가 컷아웃에 의해 가려질 위험이 있다. 특히 사용자가 상단이나 측면에 배치된 UI 요소를 확인할 수 없게 되면 사용자 경험이 저하될 수 있다.

예를 들어

- 화면 상단에 리스트 항목이 배치된 경우, 디스플레이 컷아웃이 이 항목을 가릴 수 있다.
- 가로 모드에서 디스플레이 컷아웃이 화면의 측면에 위치하는 경우, 콘텐츠의 일부가 잘려 나갈 수 있다.

이러한 문제를 방지하기 위해 `Display Cutout Inset`을 사용하여 콘텐츠가 디스플레이 컷아웃 영역에 그려지지 않도록 패딩을 추가할 수 있다.

```kotlin
ViewCompat.setOnApplyWindowInsetsListener(binding.recyclerView) { v, insets ->
  val bars = insets.getInsets(
    WindowInsetsCompat.Type.systemBars()
      or WindowInsetsCompat.Type.displayCutout()
  )
  v.updatePadding(
    left = bars.left,
    top = bars.top,
    right = bars.right,
    bottom = bars.bottom,
  )
  WindowInsetsCompat.CONSUMED
}
```

`WindowInsetsCompat.Type.displayCutout()` 코드로 디스플레이 컷아웃의 인셋을 가져올 수 있으며, `or` 연산을 통해 더 적절한 Inset을 가져올 수 있다.

```kotlin
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/recycler_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:clipToPadding="false"
    app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager" />
```

추가로, 위와 같이 RecyclerView에서 `clipToPadding=”false”` 설정을 통해 패딩 영역을 포함한 전체 콘텐츠가 스크롤 될 수 있도록 할 수 있다. 이 설정을 통해, 콘텐츠가 시스템 바나 컷아웃 뒤로 스크롤되더라도 리스트 항목이 잘리지 않게 된다.

#### **3. System Gesture Insets**

- **정의**: 시스템 제스처(예: 홈 제스처, 뒤로 가기 제스처)가 우선적으로 사용되는 영역을 나타낸다.
- **용도**: 스와이프 가능한 영역을 제스처 영역에서 떨어뜨리거나, 그에 맞춰 패딩을 조정하는 데 사용된다. 예를 들어, 하단 시트, 게임의 스와이프 제스처, `ViewPager2`로 구현된 캐러셀 등이 이에 해당한다.

![](images/06.webp){width=100 align=center}

안드로이드 10 이상 버전에서는 제스처 기반 내비게이션이 도입되어, 사용자가 화면의 특정 영역에서 스와이프 제스처를 통해 홈으로 돌아가거나 이전 화면으로 이동할 수 있다. 이 제스처는 화면 가장자리에서 시작되므로, 앱의 중요한 UI 요소가 이 영역과 겹치는 경우 사용자 경험에 부정적인 영향을 줄 수 있다.

갤럭시 폰 사용자들이 애용하는 Good Lock의 One Hand Operation의 동작을 앱 내에서 영역 겹침 문제로 사용하지 못한다면, 유저들은 상당히 좋지 않은 경험을 할 것이라고 생각한다.

```kotlin
ViewCompat.setOnApplyWindowInsetsListener(view) { view, windowInsets ->
    val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemGestures())
    // Apply the insets as padding to the view. Here, set all the dimensions
    // as appropriate to your layout. You can also update the view's margin if
    // more appropriate.
    view.updatePadding(insets.left, insets.top, insets.right, insets.bottom)

    // Return CONSUMED if you don't want the window insets to keep passing down
    // to descendant views.
    WindowInsetsCompat.CONSUMED
}
```

위와 같이 `WindowInsetsCompat.Type.systemGestures()`를 통해 시스템 제스처 인셋을 가져와 패딩을 부여할 수 있다.

> 그럼, 위와 같은 세 가지 방법으로 개발자가 매번 수동으로 패딩을 부여해야 할까? 🤔

### Material Component를 사용하자

**Material Components**는 안드로이드의 UI 구성 요소 라이브러리로, 다양한 뷰 컴포넌트를 제공한다. 이 중 다수의 뷰는 시스템 인셋을 자동으로 처리하도록 설계되어 있다.

#### 자동으로 Inset을 처리하는 Material Components

> ⚠️ 버전에 따라 다를 수도 있으니 꼭 공식문서를 확인해보자.

- **BottomAppBar**
- **BottomNavigationView**
- **NavigationRailView**
- **NavigationView**

이 뷰들은 **시스템 바나 제스처 인셋과 같은 시스템 인셋을 자동으로 처리**하여, 콘텐츠가 시스템 UI와 겹치지 않도록 한다. 즉, 이러한 뷰를 사용할 때는 **별도의 인셋 처리가 필요하지 않을 수 있다**.

#### 자동으로 Inset을 처리하지 않는 AppBarLayout

- **AppBarLayout**은 자동으로 시스템 인셋을 처리하지 않으므로, 이를 직접 처리해주어야 한다.
  - `android:fitsSystemWindows="true"` 속성을 XML 레이아웃 파일에 추가하면, AppBarLayout이 시스템 인셋을 자동으로 처리하여 상태 바에 가려지지 않도록 한다.
  - 또는 `setOnApplyWindowInsetsListener`를 사용하여 시스템 인셋을 수동으로 처리할 수도 있다. 이를 통해 보다 세부적으로 인셋을 관리할 수 있다.

### Immersive Mode (몰입 모드)

**Immersive Mode**는 사용자에게 더욱 몰입감 있는 전체 화면 경험을 제공하기 위해 시스템 바(상태 바, 내비게이션 바)를 숨기는 기능이다. 이 모드를 사용하면 앱의 콘텐츠가 전체 화면에 표시되며, 시스템 바는 사용자가 상호작용할 때만 나타난다.

#### Immersive Mode 활성화 및 비활성화

Immersive Mode를 구현하려면 `WindowInsetsController` 또는 `WindowInsetsControllerCompat` 라이브러리를 사용한다.

```kotlin
val windowInsetsController =
      WindowCompat.getInsetsController(window, window.decorView)

// Hide the system bars.
windowInsetsController.hide(Type.systemBars())

// Show the system bars.
windowInsetsController.show(Type.systemBars())
```

- `windowInsetsController.hide(Type.systemBars())`: 이 메서드는 상태 바와 내비게이션 바를 숨겨 전체 화면을 활용할 수 있도록 한다. 이를 통해 몰입 모드가 활성화된다.
- `windowInsetsController.show(Type.systemBars())`: 이 메서드는 숨겨진 시스템 바를 다시 표시다. 사용자가 상호작용하거나 필요에 따라 시스템 바를 다시 나타나게 할 수 있다.

## 예제를 통해 더 알아보기

![](images/07.webp){width=100 align=center}

기본적으로 `enableEdgeToEdge()` 함수는 시스템 바를 투명하게 만든다. 단, 3버튼 내비게이션 모드에서는 상태 바에 반투명한 스크림이 적용된다. 시스템 아이콘과 스크림의 색상은 시스템의 라이트 또는 다크 테마에 따라 조정된다.

`enableEdgeToEdge()` 함수는 앱이 edge-to-edge로 레이아웃되어야 함을 자동으로 선언하고, 시스템 바의 색상을 조정한다.

위와 같은 네비게이션 바의 default background protection을 삭제하려면 아래와 같은 코드를 입력하면 된다.

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            // Add this block:
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
        }
    }
    ...
}
```

`window.isNavigationBarContrastEnforced` 는 완전히 투명한 배경이 요청될 때 네비게이션 바의 대비가 충분한지 확인한다. 이 속성을 false로 설정하면 사실상 **3 버튼 네비게이션 바의 배경을 투명으로 설정**하는 것이다. 이 속성은 3 버튼 네비게이션 바에만 영향을 미치며 다이나믹 바에는 영향을 미치지 않는다.

![](images/08.webp){width=100 align=center}

하지만 여전히 다른 화면에서는 네비게이션 바에 의해 뷰가 가려지는 현상이 발생하고 있다. 컴포즈에서 Scaffold를 사용하였다면 innerPadding을 통해 간단하게 해결할 수 있다.

```kotlin
InputBar(
    ...
    contentPadding = innerPadding.copy(layoutDirection, top = 0.dp), 
    ...
 )
```

하지만 XML 방식에서는 어떻게 해결할 수 있을까? 이를 자세히 알아보기 위해 예시 상황과 코드를 가져와 보았다.

```kotlin
abstract class BindingActivity<T : ViewBinding>(
    @LayoutRes private val layoutResId: Int
) : AppCompatActivity() {

    protected val binding: T by lazy { getViewBinding() }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        setupWindowInsets()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    protected abstract fun getViewBinding(): T

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
```

Android Project를 처음 생성하면 기본적으로 생성되는 `enableEdgeToEdge`와 `systemBar`의 `Inset`을 가져와 전체 뷰에 패딩을 부여하는 코드이다.

![](images/09.webp){width=100 align=center}

`enableEdgeToEdge()` 함수로 인해 화면 전체를 사용하겠다 선언하였으니, 시스템 바의 영역 만큼 화면에 패딩을 주지 않으면, 오른쪽 화면과 같이 시스템 바와 뷰가 겹치는 현상이 발생한다.

이를 해결하려면 근본적으로 두 가지 방법이 떠오른다.

1. 시스템 바의 영역만큼 뷰 전체에 패딩을 준다.
2. 필요한 뷰에만 Inset을 줘서 겹침 현상을 피한다.

앱의 상황에 따라 선택할 수 있는 방법이 다를 것 같다. 하지만 안드로이드 프로젝트를 처음 생성하면 시스템 바의 영역만큼 뷰 전체에 패딩을 주는 코드가 들어가있기에, 그 방식을 활용하여 해결해보려고 한다.

왼쪽 화면을 기준으로 우리가 해결해야 될 문제는 `Bottom Navigation Bar`의 하단 영역에 padding이 존재하다는 거다. 왜 이런 문제가 발생할까? 위에서 살펴봤다시피 **Material Component**들에는 기본적으로 `Inset`이 설정되어 있다. 따라서 `setOnApplyWindowInsetsListener`를 통해 화면의 인셋이 구성될 때 문제 상황을 아래와 같이 나타낼 수 있을 것 같다.

![](images/10.webp){width=100 align=center}

인셋이 이미 적용된 바텀 네비게이션의 본 모습. 오른쪽 화면에서는 네비게이션바와 겹치기에 아랫 부분이 보이지 않은 것이었다.

따라서 이 문제를 해결하기 위해서는 쉽게 두 가지 방법이 있을 것 같다.

1. 시스템 바 패딩을 삭제한다.
2. 바텀 네비게이션 자체 패딩을 0으로 설정한다.

시스템 바 패딩을 삭제하면 다른 컴포넌트들에도 영향을 줄 수 있기에, 바텀 네비게이션 자체 패딩을 아래와 같이 설정할 수 있다.

```kotlin
    private fun setInsets() {
        val bottomNav = binding.bnvGame
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav) { view, insets ->
            view.setPadding(0, 0, 0, 0)
            insets
        }
    }
    
 // 또는 아래 코드 (onViewCreated())
 binding.bottomNavigationView.setOnApplyWindowInsetsListener { view, insets ->
        view.updatePadding(bottom = 0)
        insets
    }
```

위와 같이 설정하면 우리가 원하는대로 정상적인 화면이 나오게 된다.

![](images/11.webp){width=100 align=center}

#### 프로덕트에는 어떻게 적용하는게 좋을까?

`edge-to-edge`를 적용하면서 시스템 바의 패딩이 강제되었는데, 이 문제 상황을 배포되어 있는 프로젝트에 적용한다고 가정했을 때, 어떻게 하는게 좋을까?

`Material Component`에 의존하고 있는 프로덕트에서는 거의 대부분의 컴포넌트들이 자체 `Inset`을 가지고 있을 것이기에 별도의 조치를 하지 않아도 될 것이다. `AppBarLayout`과 같이 자체 `Inset`이 처리되지 않는 요소들에만 시스템 바 크기만큼 패딩을 부여하면 될 것이다.

자체 디자인 시스템으로 컴포넌트를 사용하고 있는 프로덕트에는 `Inset`이 설정된 요소들이 존재한지 확인한 뒤, 시스템 바 만큼 전체 화면의 패딩을 부여하면 최신 안드로이드 기기에도 대응이 될 것이다.

### +추가) adjustResize가 edge-to-edge에서는 동작하지 않는다…!

이 제목은 조금 틀렸다. 정확히 말하자면 동작하지 않는 것이 아니라, `EditText`를 터치했을 때 키보드 위로 버튼이 보이게 하기 위해서 `adjustResize`를 적용하기만 하면 됐는데 이제 아니라는 뜻이다..!

여전히 `adjustResize`는 필요하다. 이 설정을 통해 앱이 소프트웨어 `IME`의 크기를 인셋으로 수신할 수 있다.

`EnableEdgeToEdge()`로 인해 기존의 방법이 먹히지 않는 이유는 `adjustResize`는 시스템 바의 크기를 고려하여 레이아웃을 조정하는데, `Edge-to-Edge` 모드에서는 이 시스템 바가 화면에서 제거된 것처럼 취급되기 때문에, 레이아웃 크기 조정이 예상대로 작동하지 않는 것이다.

`Edge-to-Edge`와 `adjustResize`는 기본적으로 서로 다른 목표를 가진 모드이다. `Edge-to-Edge`는 콘텐츠를 시스템 바 뒤로 확장하는 데 초점을 맞추고, `adjustResize`는 콘텐츠를 키보드 위로 올리는 데 초점을 맞춘다. 이 두 가지가 충돌할 수밖에 없기 때문에, 한쪽을 선택하고 다른 한쪽을 수동으로 처리해야 한다.

그럼 어떻게 해야될까? 답은 간단하다. 수동으로 레이아웃을 조정하면 된다.

아래 코드를 해당하는 뷰와 바인딩 되어 있는 액티비티에 삽입하면 된다.

```kotlin
private fun setupKeyboardListener() {
      ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // 키보드가 올라올 때 (IME 인셋이 있을 때) 버튼을 이동
            binding.btnNext.translationY = if (imeInsets.bottom > 0) {
                -(imeInsets.bottom - systemBars.bottom).toFloat()
            } else {
                0f
            }

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }
    }
```

- `WindowInsetsCompat.Type.ime()`: 키보드(IME)의 인셋을 가져온다. 키보드가 나타나면 하단 인셋 값이 증가하고, 키보드가 사라지면 0으로 설정된다.
- `nextButton.translationY`: 키보드가 나타날 때 `Next` 버튼을 위로 이동시키기 위해 `translationY`를 조정한다. `imeInsets.bottom` 값을 사용해 키보드 높이만큼 버튼을 위로 이동시킨다. 하지만 `imeInsets.bottom` 에는 네비게이션 바의 높이가 포함되어 있기에 이를 빼준다.
- 시스템 바 인셋 처리: 전체 화면에 `systemBarsInsets`를 적용하여 상태 바나 내비게이션 바와 겹치지 않도록 한다.

위와 같이 처리하면 이제 EditText에 focus를 줄 시에 키보드가 올라오면서 뷰 하단의 버튼이 키보드 위로 올라오게 된다.

---

> 출처

[https://developer.android.com/develop/ui/views/layout/edge-to-edge](https://developer.android.com/develop/ui/views/layout/edge-to-edge)

[https://developer.android.com/develop/ui/views/layout/sw-keyboard?hl=ko](https://developer.android.com/develop/ui/views/layout/sw-keyboard?hl=ko)

[https://developer.android.com/codelabs/edge-to-edge?hl=ko#2](https://developer.android.com/codelabs/edge-to-edge?hl=ko#2)

[https://developer.android.com/develop/ui/views/layout/display-cutout?hl=ko](https://developer.android.com/develop/ui/views/layout/display-cutout?hl=ko)
