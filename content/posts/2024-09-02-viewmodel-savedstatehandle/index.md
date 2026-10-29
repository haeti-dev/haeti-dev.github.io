---
title: [Android] ViewModel의 구조와 함께 알아보는 데이터 보관 방법
date: 2024-09-02
category: 안드로이드
summary: ViewModel의 내부 구조, 인스턴스 생성 방법 및 SavedStateHandle로 데이터를 보관하는 방법에 대해 다룹니다.
thumbnail: images/thumbnail.webp
draft: false
---

이번 글에서는 ViewModel의 개념과 장점 그리고 내부 구조와 ViewModel로 데이터를 안전하게 저장 및 복구하는 방법에서 알아보겠다.

## ViewModel이란?

`ViewModel` 클래스는 UI와 관련된 데이터를 생명주기에 맞게 저장하고 관리하기 위해 설계된 클래스이다. 이 클래스는 UI에 상태를 노출하고 관련된 비즈니스 로직을 캡슐화한다. `ViewModel`의 주요 장점은 **상태를 캐시하고 구성 변경(예: 화면 회전) 시에도 상태를 유지한다는 점**이다. 이를 통해 액티비티 간 이동이나 화면 회전과 같은 구성 변경 후에도 UI가 데이터를 다시 가져올 필요가 없게 된다.

위 정보에서 알 수 있듯이 MVVM(Model-View-ViewModel) 아키텍처 패턴의 ViewModel에서 영감을 받아 만들어졌으며, 이와 유사한 역할을 한다.

## ViewModel의 장점

1. **UI 데이터 저장 및 공유**: `ViewModel`은 UI 관련 데이터를 저장하며, 이 데이터는 `LiveData`를 통해 UI 핸들링 클래스에 노출된다. 이러한 데이터는 액티비티 범위 내에서 다른 프래그먼트 간에 공유될 수 있다.
2. **화면 회전 시 데이터 유지**: 화면이 회전하여 현재 액티비티 인스턴스가 파괴되고 새 인스턴스가 생성되더라도, `ViewModel`은 데이터를 유지하여, 데이터 손실 없이 그대로 사용할 수 있게 해준다.
3. **생명주기 인식**: `ViewModel`은 생명주기를 인식하며, 관찰하는 생명주기가 영구적으로 파괴될 때 자동으로 정리된다. 이를 통해 메모리 누수를 방지하고, 안정적인 데이터 관리를 보장할 수 있다.
4. **비즈니스 로직에 접근**: 액티비티나 프래그먼트가 직접 데이터를 처리하지 않고, `ViewModel`을 통해 데이터를 처리하도록 함으로써 UI와 비즈니스 로직을 분리할 수 있다.

위 정보에 따르면, `ViewModel`은 액티비티, 그리고 생명주기와 밀접하게 관련이 있는 것을 볼 수 있다.

> 💡*그렇다면* `ViewModel`*이 어떻게* ***Configuration Change***(*화면 회전과 같은) 상황에서 인스턴스를 유지할 수 있을까*?

**참고**: ViewModel은 Hilt, Navigation, Compose와 같은 주요 Jetpack 라이브러리와의 통합을 완벽하게 지원한다.

## ViewModel의 구조

`ViewModel` 객체를 생성하려면 `ViewModelProvider` 클래스가 필요하다. `ViewModelProvider`는 `ViewModel` 인스턴스를 생성하기 위한 유틸리티 클래스이며, 다음과 같은 절차로 사용된다.

1. **ViewModelProvider 인스턴스 생성**

   `ViewModelProvider` 인스턴스를 생성하려면 두 가지 인자를 전달해야 한다.

![](images/01.webp){width=100 align=center}

- **ViewModelStoreOwner**: `ViewModelStoreOwner`는 인터페이스로, `ViewModelStore`를 반환하는 한 가지 메서드만을 가진다. 기본적으로, 액티비티와 프래그먼트는 `ViewModelStoreOwner`이며, 이는 안드로이드 SDK의 `ComponentActivity`와 `Fragment` 클래스가 이 인터페이스를 구현하고 있기 때문이다.
- **Factory**: `Factory`는 `ViewModelProvider` 클래스의 중첩 인터페이스로, `ViewModel` 객체를 생성하는 데 사용된다. 기본적으로 `Factory`가 전달되지 않으면 기본 팩토리가 생성되며, 파라미터화된 `ViewModel`을 생성하기 위해 사용자 정의 팩토리도 만들 수 있다.

이 **ViewModelProvider**는 이제 **ViewModel의 인스턴스 생성과 재사용을 담당**한다.

**2. ViewModel 인스턴스 생성**

![](images/02.webp){width=100 align=center}

`ViewModelProvider` 객체가 생성되면, `get` 메서드를 사용하여 원하는 `ViewModel` 클래스를 참조해 `ViewModel` 객체를 가져온다.

`get` 메서드는 먼저 `ViewModel` 클래스의 정규 이름을 가져와 `DEFAULT_KEY`를 붙여 키를 생성한 다음, 이 키와 `ViewModel` 클래스 참조를 이용해 또 다른 `get` 함수를 호출힌다.

![](images/03.webp){width=100 align=center}

이 함수는 먼저 `ViewModelStore`에서 `ViewModel` 인스턴스를 찾는다. 만약 `ViewModel` 인스턴스가 존재하면 해당 인스턴스를 반환하고, 존재하지 않으면 **팩토리를 사용해 새 인스턴스를 생성한 뒤 이를** `ViewModelStore`**에 저장하고 반환**한다.

이 `ViewModelStore`는 `ViewModelStoreOwner`에 연결되어 있으며, 액티비티나 프래그먼트마다 고유한 `ViewModelStore`를 가진다. 이로 인해 화면 회전이 발생하더라도 `ViewModelStore`가 유지되며, 새 액티비티 인스턴스에서도 동일한 `ViewModel` 인스턴스를 반환할 수 있게 된다.

> ***Activity나 Fragment가 파괴되고 재생성되는데, 어떻게 ViewModelStore를 유지할 수 있을까***?

![](images/04.webp){width=100 align=center}

`ViewModelStoreOwner`는 인터페이스이다. `ComponentActivity`가 이 인터페이스를 구현하는 구조이다.

그럼 어떻게 구현되어 있을까?

![](images/05.webp){width=100 align=center}

- `Application`이 아직 액티비티에 연결되지 않은 상태에서는 예외를 던진다.
  - 즉, `onCreate()` 호출 전에 `ViewModel`을 요청하면 안된다.
- `ensureViewModelStore()`을 호출하여 `ViewModelStore`를 반환한다

![](images/06.webp){width=100 align=center}

- 위 코드에서 볼 수 있듯이, 새로운 액티비티 객체에서 `ViewModelStore`가 `null`인 경우, 먼저 `NonConfigurationInstance`에서 이전 액티비티의 `ViewModelStore`를 가져온다.
- 만약 액티비티가 처음 생성되는 경우에는 항상 새로운 `ViewModelStore` 객체가 생성된다.
- 따라서 화면 회전 시 파괴된 이전 액티비티의 `NonConfigurationInstance` 객체가 새로 생성된 액티비티로 전달되며, 이 객체가 이전 액티비티의 `ViewModelStore`를 포함하고 있어 동일한 `ViewModel` 인스턴스를 사용할 수 있게 되는 것이다.

> **지금까지** `ViewModel`**이** `Configuration Change` **시에 어떻게 인스턴스를 유지하는지에 대해 알아보았다**.
>
> **그럼 이제 어떻게 인스턴스를 생성하여 활용할 수 있을까**?

### Delegate로 ViewModel 인스턴스 생성하기

`ViewModel`을 더 쉽게 생성하고 관리하기 위해 Kotlin의 **위임 프로퍼티(Delegate Property**)를 활용할 수 있다. 이는 코드를 더 간결하게 만들고, `ViewModel`의 사용을 더욱 직관적으로 만들어 준다.

![](images/07.webp){width=100 align=center}

- `viewModels`는 기본적으로 `Lazy`를 반환하여, `ViewModel`에 최초로 접근할 때 `ViewModel` 인스턴스가 생성되고, 이후에는 동일한 인스턴스를 반환하게 된다.
- `ownerProducer` (기본값: `this`)
  - `ownerProducer`는 `ViewModelStoreOwner`를 제공하는 함수이다. 기본적으로 현재 `Fragment`를 `ViewModelStoreOwner`로 사용한다.
  - 이 매개변수를 사용하여 `ViewModel`의 범위를 변경할 수 있다. 예를 들어, `requireParentFragment()`를 사용하여 부모 프래그먼트의 `ViewModelStore`를 사용할 수 있다.
  - 부모-자식 프래그먼트간의 데이터를 공유하는 작업을 수행할 시에 활용할 수 있다.
- `extrasProducer` (기본값: `null`)
  - `extrasProducer`는 `ViewModel`을 생성할 때 사용할 `CreationExtras`를 제공하는 함수이다. `CreationExtras`는 `ViewModel` 생성 시 추가적인 데이터를 전달하는 역할을 한다.
  - 이 매개변수는 주로 고급 설정에서 사용되며, 기본적으로는 `CreationExtras.Empty`가 사용된다.
- `factoryProducer` (기본값: `null`)
  - `factoryProducer`는 `ViewModel`을 생성하기 위한 사용자 정의 `ViewModelProvider.Factory`를 제공하는 함수이다. 이를 통해 커스텀 팩토리를 사용하여 `ViewModel`을 생성할 수 있다.

#### 1. **기본 ViewModelDelegate 사용**

코틀린에서 `by viewModels()` 또는 `by activityViewModels()`를 사용하여 `ViewModel`을 delegate로 쉽게 생성할 수 있다.

- `by viewModels()`: 이 방법은 액티비티 또는 프래그먼트에서 사용하며, 해당 클래스 전용의 `ViewModel` 인스턴스를 생성한다.
- `by activityViewModels()`: 이 방법은 액티비티와 프래그먼트 간에 `ViewModel`을 공유할 때 사용한다. 이를 통해 같은 액티비티 안에 있는 여러 프래그먼트가 동일한 `ViewModel` 인스턴스를 공유할 수 있다.

```
class MyFragment : Fragment() {

    // Fragment 전용 ViewModel 인스턴스 생성
    private val viewModel: MyViewModel by viewModels()

    // Activity와 Fragment 간에 공유되는 ViewModel 인스턴스 생성
    private val sharedViewModel: MyViewModel by activityViewModels()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // ViewModel을 사용한 데이터 처리 및 UI 업데이트
        viewModel.someLiveData.observe(viewLifecycleOwner) { data ->
            // UI 업데이트
        }
    }
}
```

이제 이 코드를 보면, 내부적으로 아래와 같은 과정을 거친다고 생각해야 한다.

1. `ViewModelProvider` 인스턴스 생성
2. `ViewModelProvider.get()` 메서드 호출
3. `ViewModelStore`에서 `ViewModel` 검색 또는 새로 생성

#### 2. **ViewModelProvider.Factory를 활용한 Custom ViewModel 인스턴스 생성**

`ViewModel`이 생성자에서 **특정 파라미터를 필요로 하는 경우**, `ViewModelProvider.Factory`를 사용하여 `ViewModel`을 생성할 수 있다. 하지만, delegate 방식으로 더 간편하게 생성할 수 있다.

```
class MyViewModelFactory(private val repository: MyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
```

먼저 위와 같이 커스텀 팩토리를 생성한다.

```kotlin
class MyFragment : Fragment() {

    private val repository by lazy { MyRepository() }
    
    private val viewModel: MyViewModel by viewModels {
        MyViewModelFactory(repository)
    }

    // 또는
    
    val viewModel2 = ViewModelProvider(this, MyViewModelFactory(repository))
    .get(MyViewModel::class.java)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // ViewModel을 사용한 데이터 처리 및 UI 업데이트
        viewModel.someLiveData.observe(viewLifecycleOwner) { data ->
            // UI 업데이트
        }
    }
}
```

그 다음, 이 펙토리를 사용해 ViewModel을 delegate로 생성할 수 있다. `by viewModels`가 호출되면 이 `factoryProducer` 가 전달되고, ViewModel을 생성할 때 이 팩토리가 사용된다.

반면, 직접적으로 ViewModelProvider를 사용할 수도 있다.

- `ViewModelProvider(this, MyViewModelFactory(repository))`: `this`는 `ViewModelStoreOwner`(예: `Activity`나 `Fragment`)를 가르킨다. 두 번째 인자로 팩토리를 전달하여, ViewModel을 생성하는 데 필요한 파라미터를 전달한다.
- `get(MyViewModel::class.java)`: 이 메서드를 사용하여 ViewModel 클래스에 맞는 인스턴스를 가져온다.

> `Factory`**는** `ViewModel`**생성 방법을 정의하고**, `Provider`**는 이** `Factory`**를 사용하여 실제로** `ViewModel`**을 생성하고 관리한다고 보면 된다**.

### ViewModel의 생명주기

![](images/08.webp){width=100 align=center}

- 생성 시점: `ViewModel`은 `ViewModelProvider`에 의해 처음 액세스될 때 생성된다. 예를 들어, 액티비티나 프래그먼트에서 `ViewModel`을 초기화할 때 `ViewModelProvider`를 통해 인스턴스가 제공되며, 이는 `ViewModel`의 초기 생성 시점을 의미한다.
- 유지 관리: UI 컨트롤러(액티비티 또는 프래그먼트)의 **구성 변경**이 발생하면, `ViewModel`은 메모리에 그대로 유지되며, 새로 생성된 UI 컨트롤러 인스턴스에서 기존의 `ViewModel`을 다시 사용할 수 있다. 이는 `ViewModelStore`와 `NonConfigurationInstance` 메커니즘을 통해 관리된다.
- 소멸 시점: UI 컨트롤러가 **영구적으로 종료**되면, `ViewModel`도 소멸된다. 이 시점에서 `ViewModel`의 `onCleared()` 메서드가 호출되어, 메모리에서 해제되기 전에 필요한 정리 작업을 수행할 수 있다.

여기서 언급한 **영구적으로 종료**되는 것은 단순히 `onDestroy` 이후를 의미하는 것은 아니다.

- 유저가 뒤로 가기 버튼을 눌러 Activity가 종료될 때
- Fragment 트랜잭션을 통해 Fragment가 제거될 때(remove or replace)

구성 변경 시에도 `onDestory`가 호출되지만, 이 경우 `ViewModel`은 메모리에 유지되며 기존 인스턴스를 다시 사용할 수 있다. 하지만 위와 같은 상황에서는 `onDestroy`가 호출된 후 더 이상 해당 컨트롤러가 복구되지 않으며, `ViewModel`의 `onCleared`가 호출되어 `ViewModel`이 메모리에서 제거되게 된다.

> **이러한 특성 덕분에 화면 전환 시에 데이터를 보관하기 위해** `ViewModel`**을 활용하지만, 오히려 이러한 특성 때문에** `onCleared`**에서 필요 없는 데이터나 리소스를 정리해줘야 한다. 그렇지 않으면 메모리 누수가 발생하게 된다**.

또한, ViewModel은 `View`, `LifeCycle`, 또는 `Activity`, `Context`를 참조해서는 안 된다. 이는`ViewModel`의 수명이 UI의 수명보다 길기 때문에, 이런 참조가 **메모리 누수**를 발생시킬 수 있기 때문이다.

- 추가로 `androidx.lifecycle` Version 2.5 이상부터는 ViewModel 인스턴스가 삭제될 때 자동으로 닫히는 `Closeable` 객체를 한 개 이상 ViewModel 생성자에 전달할 수 있다.

  ```kotlin
  class CloseableCoroutineScope(
      context: CoroutineContext = SupervisorJob() + Dispatchers.Main.immediate
  ) : Closeable, CoroutineScope {
      override val coroutineContext: CoroutineContext = context
      override fun close() {
          coroutineContext.cancel()
     }
  }
  
  class MyViewModel(
      private val coroutineScope: CoroutineScope = CloseableCoroutineScope()
  ) : ViewModel(coroutineScope) {
      // Other ViewModel logic ...
  }
  ```

> 이제 구성 변경 시에는 `ViewModel`을 사용하면 데이터가 안전하게 보관되겠지만, 바텀 네비게이션 바로 Fragment `replace`와 같은 상황에서도, 즉 `ViewModel`이 `onCleared`되고 나서도 데이터를 보관하고 싶다면 어떻게 하면 좋을까?

## SavedStateHandle로 데이터를 안전하게 유지하기

`SavedStateHandle`은 안드로이드 아키텍처 컴포넌트의 일부로, `ViewModel` 내에서 상태 데이터를 저장하고 복원할 수 있는 데이터 구조이다. `SavedStateHandle`은 `Bundle`과 유사한 방식으로 **키-값 쌍**으로 데이터를 저장하며, 구성 변경이나 프래그먼트 트랜잭션 후에도 이 데이터를 유지할 수 있다.

SavedStateHandle은 작업 스택에 연결된다. 작업 스택이 사라지면 저장된 상태도 사라진다. 이는 앱을 강제 종료하거나 최근 메뉴에서 앱을 삭제하거나 기기를 재부팅할 때 발생할 수 있다. 이러한 경우 작업 스택이 사라지고 저장된 상태의 정보를 복원할 수 없다.

`SavedStateHandle`을 사용하면 쿼리 값이 프로세스 종료 전반에 유지되어 액티비티나 프래그먼트에서 값을 수동으로 저장 및 복원하고 `ViewModel`에 다시 전달하지 않고도 재생성 전과 후에 동일한 필터링된 데이터 set이 사용자에게 표시된다.

```kotlin
class MyViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    companion object {
        private const val KEY_SOME_DATA = "some_data"
    }

    // 데이터 저장
    fun saveData(data: String) {
        savedStateHandle.set(KEY_SOME_DATA, data)
    }

    // 데이터 복원
    fun getData(): String? {
        return savedStateHandle.get(KEY_SOME_DATA)
    }
}
```

SavedStateHandle을 ViewModel에서 다루기 위해서는 다음과 같이 SavedStateHandle을 받는 생성자를 ViewModel에 포함해야 한다. 기존에는 SavedStateViewModelFactory를 사용해서 뷰모델의 인스턴스를 생성해야 했지만, 최신 프래그먼트 라이브러리에서는 이를 사용하지 않아도 추가 구성 없이 `ViewModel` 인스턴스를 가져올 수 있다. 기본 `ViewModel` 팩토리는 `ViewModel`에 적절한 `SavedStateHandle`을 제공한다.

`SavedStateHandle`에는 키-값 맵과 상호작용할 때 예상되는 다른 메서드도 있다.

- `contains(String key)` - 지정된 키의 값이 있는지 확인한다.
- `remove(String key)` - 지정된 키의 값을 삭제한다.
- `keys()` - `SavedStateHandle` 내에 포함된 모든 키를 반환한다.

또한 관측 가능한 데이터 홀더를 사용하여 `SavedStateHandle`에서 값을 가져올 수 있다. 지원되는 유형 목록은 다음과 같다.

- `LiveData`
- `StateFlow`
- [Compose의 상태 API](https://developer.android.com/jetpack/compose/state?hl=ko)

#### 사용 예제

```kotlin
class MyViewModel : ViewModel() {

    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()
    
    fun updateName(name:String){
        _name.value = name
    }
}    
```

위와 같이 데이터를 갱신하는 코드를 savedStateHandle을 사용하여 리팩토링 해보면,

```kotlin
class MyViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    companion object {
        private const val NAME_KEY = "name"
    }

    val name: StateFlow<String> = savedStateHandle.getStateFlow(NAME_KEY, "")
 
    fun updateName(name: String) {
        savedStateHandle[NAME_KEY] = name
    }
}
```

위와 같이 작성할 수 있다. 적절한 시점에 `updateName()` 함수를 호출하여 시스템에 의한 프로세스 중단 시 데이터를 안전하게 보관할 수 있다.

바텀 네비게이션으로 화면 이동 시, RecyclerView의 스크롤 상태 보관 및 기타 변수 저장에 활용할 수 있을 것 같다.

#### SavedStateHandle 사용 시 주의할 점

![](images/09.webp){width=100 align=center}

공식 문서에 의하면 사용하는 범위가 간단한 구성 변경 시 데이터를 유지하는 경우와 로컬 저장소에 데이터를 저장해야 하는 경우의 딱 중간 지점에 위치한다. 즉, **간단한 구성 변경 시에만 데이터를 유지해야하는 경우** 또는 **사용자 액션에 의한 Activity 종료 시에도 데이터를 유지해야하는 경우**에는 `SavedStateHandle` 사용이 적절하지 않다.

정리하자면, **데이터의 일시적 저장이 필요하고, 복잡하지 않은 데이터를 관리할 때** `SavedStateHandle`**을 사용하는 것이 적절하다**.

> **핵심 사항**: 일반적으로 저장된 인스턴스 상태에 저장된 데이터는 사용자 입력 또는 탐색에 따라 달라지는 임시 상태입니다. 예를 들면 목록의 스크롤 위치, 사용자가 더 자세히 알고자 하는 항목의 ID, 진행 중인 사용자 환경설정 선택 또는 텍스트 필드 입력 등입니다.
>
> **중요**: 사용할 API는 상태가 유지된 위치와 필요한 로직에 따라 다릅니다. [비즈니스 로직](https://developer.android.com/topic/architecture/ui-layer/stateholders?hl=ko#logic)에 사용되는 상태의 경우 ViewModel에 유지하고 `SavedStateHandle`을 사용하여 저장합니다. [UI 로직](https://developer.android.com/topic/architecture/ui-layer/stateholders?hl=ko#logic)에 사용되는 상태의 경우에는 뷰 시스템에서 `onSaveInstanceState` API를 사용하거나 Compose에서 `rememberSaveable`을 사용합니다.
>
> **참고**: 상태는 단순하고 가벼워야 합니다. 복잡하거나 큰 데이터의 경우 [로컬 지속성](https://developer.android.com/topic/libraries/architecture/saving-states?hl=ko#local)을 사용해야 합니다.

위 내용은 공식문서에 나온 `SavedStateHandle`를 사용하는 일반적인 경우와 주의할 점이니 참고 바란다.

![](images/10.webp){width=100 align=center}

그리고 SavedStateHandle에 저장하는 데이터는 최종적으로 **Bundle**에 저장하므로 동일하게 처리 가능한 형태여야만 한다.

하지만 `Parcelable` 또는 `Serializable`을 활용하여 복잡한 데이터 구조를 저장할 수 있다.

```kotlin
@Parcelize
data class User(val name: String, val age: Int) : Parcelable

class MyViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    companion object {
        private const val USER_KEY = "user"
    }

    fun saveUser(user: User) {
        savedStateHandle.set(USER_KEY, user)
    }

    fun getUser(): User? {
        return savedStateHandle.get(USER_KEY)
    }
}
```

이 경우 `Parcelable`을 구현한 `User` 객체를 저장하고 필요할 때 복원할 수 있다. 다만, 큰 데이터 구조나 복잡한 데이터는 `SavedStateHandle`에 저장하는 대신 **로컬 데이터베이스**(예: Room)나 **외부 저장소**를 활용하는 것이 더 적합할 수 있다.

`SavedStateHandle`은 **Jetpack Navigation**과 함께 사용하면 더 큰 이점이 있다. 이에 관한 내용은 다음에 작성해보도록 하겠다.

---

> 출처 및 더 자세한 내용

[https://developer.android.com/topic/libraries/architecture/viewmodel?hl=ko](https://developer.android.com/topic/libraries/architecture/viewmodel?hl=ko)

[https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-savedstate?hl=ko](https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-savedstate?hl=ko)

[https://pluu.github.io/blog/android/2020/03/15/savedstate-flow/](https://pluu.github.io/blog/android/2020/03/15/savedstate-flow/)
