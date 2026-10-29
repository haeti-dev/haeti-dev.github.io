---
title: [Android] build-logic으로 Custom Plugin을 설계해 활용하기
date: 2024-11-10
category: 안드로이드
summary: build-logic을 활용하여 Custom Plugin을 설계하고 적용하는 법에 대해 다룹니다.
thumbnail: images/thumbnail.webp
draft: false
---

멀티 모듈 구조를 설계하다 보면, 중복된 파일들이 굉장히 많아진다. `AndroidManifest.xml`, `proguard-rules.pro`, `build.gradle.kts`과 같은 파일들이 필요 없거나 다른 곳에 정의한 내용과 일치하는 경우가 많다. 이때 기존에 개발자들은 수동으로 일일이 삭제하거나, 중복된 코드를 방치하고 있었다.

이러한 배경 속에서 여러가지 방법론들이 나왔지만, 그중에서 **buildSrc**가 가장 많이 사용되었다. 여러 모듈에서 중복되는 설정을 **buildSrc**에서 **일괄 관리**할 수 있어서 관리하는 데 편리하다는 장점이 있었다.

```kotlin
// buildSrc 예시 
object Configuration {
  const val majorVersion = 1const val minorVersion = 1const val patchVersion = 2const val versionName = "$majorVersion.$minorVersion.$patchVersion"const val versionCode = 10
}
```

하지만 일괄 관리에 항상 제기되는 문제점인 **빌드 시간 증가(이는 싱글 모듈이 가지고 있는 단점과 같다), 범용성과 캡슐화 부족**이 존재했다. 따라서 이를 **증분**하여 좀 더 **유연하게** 만들 필요가 있었으며, 이를 **build-logic**을 통해 해소하고자 하였다.

이제 build-logic을 구성하는 여러가지 Plugin 선언법이 존재하지만 이번 글에서는 **Custom Plugin** 선언법에 대해 다루겠다.Custom Plugin 방법은 독립적인 플러그인 클래스를 정의하여 모듈 별 공통 설정을 관리할 수 있고, 이 플러그인을 모듈 간에 공유하여 사용할 수 있다.

gradle, compiler option와 같은 로직들이 적용되는지 까지는 이 글에서 다루지 않고, 단순히 소개하고 가이드하는 글을 작성해보겠다.

### build-logic 모듈 생성

```kotlin
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
```

기본적으로 위 코드를 프로젝트 루트의 `settings.gradle.kts`에 기입해준다. 이 코드는 `TYPESAFE_PROJECT_ACCESSORS`를 활성화하여 프로젝트 간 종속성을 더 안전하게 사용할 수 있게 해준다.

예를 들어

```kotlin
dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":feature:home"))
}
```

위와 같이 사용하던 기존 방식에서, `TYPESAFE_PROJECT_ACCESSORS` 를 활성화하면,

```kotlin
dependencies {
    implementation(projects.core.model)
    implementation(projects.core.ui)
    implementation(projects.feature.home)
}
```

이렇게 컴파일 타임에서 안전하게 사용할 수 있다.

```kotlin
// setting.gralde.kts(:build-logic)
dependencyResolutionManagement {
	repositories {
		google()
		mavenCentral()
	}
	versionCatalogs {
		create("libs") {
			from(files("../gradle/libs.versions.toml"))
		}
	}
}

rootProject.name = "build-logic"include(":convention")
```

이후 위 코드를 build-logic 모듈의 `setting.gradle.kts` 파일에 작성하여 의존성 버전 관리를 `libs.version.toml` 파일에서 중앙화하게끔 한다.

```kotlin
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configureondemand=true
```

그 다음 빌드 속도와 관련된 `gradle-properties` 파일을 build-logic에 첨부한다. 각 속성은 아래와 같은 역할을 한다.

- `parallel`: 병렬 빌드를 활성화하여 빌드 속도를 높인다.
- `caching`: 캐싱을 활성화하여 빌드가 변경되지 않은 작업을 캐시에서 가져올 수 있게 한다.
- `configureondemand`: 필요한 프로젝트만 구성하도록 하여 빌드 초기화 속도를 최적화한다.

```kotlin
// build.gradle.kts (build-logic:convention)
plugins {
	`kotlin-dsl`
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
	compileOnly(libs.android.gradlePlugin)
	compileOnly(libs.kotlin.gradlePlugin)
	compileOnly(libs.compose.compiler.gradlePlugin)
	compileOnly(libs.ksp.gradlePlugin)
}
```

이제 위와 같이 빌드 로직을 구성할 때 필요한 의존성들을 `compileOnly`로 추가하여 컴파일 시점에만 필요하고 런타임에는 필요하지 않도록 설정한다. 이 프로젝트에서는 Kotlin 2.0.21 버전을 사용하기에 compose-compiler 플러그인을 설정하고, hilt를 위한 ksp 플러그인을 설정했다.

## 어떤 커스텀 플러그인을 만들 것인가?

현재 적용한 프로젝트는 모두 Jetpack Compose로 이루어진 프로젝트이다. 이에 따라 Compose Jetpack Navigation, Compose Coil 등 Compose 전용 라이브러리들을 사용했으며, 이는 편리한 추가를 위해 버전 카탈로그 번들 형태로 `libs.versions.toml` 파일에 선언해두었다.

```kotlin
[bundles]
# Compose Libraries
compose = [
	"compose-ui",
	"compose-ui-graphics",
	"compose-ui-tooling-preview",
	"compose-material",
	"compose-material3",
	"compose-foundation",
	"compose-runtime",
	"androidx-activity-compose",
	"androidx-lifecycle-viewmodel-compose",
]

# Coil Libraries
coil = [
	"coil-compose",
	"coil-network-okhttp"
]
```

Compose로만 이루어진 프로젝트라는 것을 염두하여, 커스텀 플러그인들을 아래와 같이 설계하였다.

- **AndroidApplicationPlugin** : 앱 모듈에 필요한 공통 설정 관리
- **AndroidComposePlugin** : Compose를 사용하는 모듈을 위한 공통 설정 관리
- **AndroidLibraryPlugin** : Android 라이브러리 모듈에 필요한 공통 설정 관리
- **HiltPlugin** : Hilt를 라이브러리를 사용해 DI를 구현하는 모듈에 필요한 공통 설정 관리
- **JavaLibraryPlugin** : Android에 종속되지 않은 Java 기반의 공통 모듈에 필요한 공통 설정 관리

그리고 위 플러그인들과 추가적인 라이브러리들을 추가하여 DataPlugin과 FeaturePlugin을 설계하였는데, 이는 추후에 다시 언급하겠다.

## 어떻게 커스텀 플러그인을 만들 것인가?

> 필자는 `DependencyHandlerScope`, `VersionCatalog`, `Project` 관련 확장 함수를 별도로 구현하여 사용하였다.

여기서 이제 의문이 드는 점이 있다. **위에 플러그인들을 구현하기 위한 코드가 중복될 수도 있지 않을까**? 예를 들어 Kotlin 관련 기본 설정은 AndroidApplicationPlugin, AndroidLibraryPlugin에 모두 들어가지 않을까? 이를 방지하기 위해 추상화의 방법 중 **하나인 확장함수**를 사용해보면 어떨까?

위와 같은 사고 과정으로 확장함수를 사용하기로 했다. 플러그인 구축에 사용한 모든 확장함수를 소개하기에는 글이 너무 길어질 것 같아서, Kotlin 관련 확장함수만 소개하겠다.

![](images/01.webp){width=50 align=center caption="KotlinAndroid.kt"}

위 확장함수는 Android Application 및 Library 플러그인에서 공통으로 사용되며, **코틀린 및 기본 빌드 설정을 일관되게 유지**할 수 있도록 한다. 적용한 내용은 아래와 같다.

- 코틀린 플러그인 적용
- SDK 관련 설정
- Vector Drawble 사용 지원
- Java 버전 설정
- BuildType 설정 (추후 빌드 버전에 대해 로직이 추가되면, 별도의 플러그인 또는 확장함수로 추출할 예정이다.)
- 린트 오류 발생 시, 실험적 API 컴파일 시 빌드 중단 방지
- JVM 타겟 설정

이제 위 확장함수를 두 플러그인에 적용하였다.

```kotlin
class AndroidApplicationPlugin : Plugin<Project> {
	override fun apply(target: Project) {
		with(target) {
			pluginManager.apply("com.android.application")

			extensions.configure<ApplicationExtension> {
				configureKotlinAndroid(this)
				configureComposeAndroid(this)

				with(defaultConfig) {
					targetSdk = libs.getVersion("targetSdk").requiredVersion.toInt()
					versionCode = libs.getVersion("versionCode").requiredVersion.toInt()
					versionName = libs.getVersion("versionName").requiredVersion
				}

				dependencies {
					implementation(libs.getLibrary("timber"))
				}
			}
		}
	}
}
```

Android Application 플러그인을 설정하고, SDK 관련 설정과 모든 곳에서 로깅 시스템을 Timber로 설정하기 위해 위와 같이 코드를 작성하였다. 이 글에서 소개하지는 않았지만, Compose 관련 확장 함수도 반영하였다.

```kotlin
class AndroidLibraryPlugin : Plugin<Project> {
	override fun apply(target: Project) {
		with(target) {
			pluginManager.apply("com.android.library")

			extensions.configure<LibraryExtension> {
				configureKotlinAndroid(this)
				configureKotlinCoroutine(this)
			}

			dependencies {
				implementation(libs.getLibrary("timber"))
			}
		}
	}
}
```

Android Library 플러그인을 설정하고, 코루틴 관련 확장함수를 적용하였다. 마찬가지로 Timber를 전역에서 사용하기 위해 미리 정의하였다.

그다음 할 일은 위에서 잠깐 언급했던 **feature, data 모듈을 위한 커스텀 플러그인**을 구축하는 것이다. 개인적으로 여기서 커스텀 플러그인의 장점이 가장 많이 느껴졌다. feature 모듈은 기본적으로 안드로이드 라이브러리 플러그인을 사용하고, 프로젝트 전역으로 Hilt와 Compose를 사용할 것이다. 그리고 core:designsystem과 같은 core 모듈들에 의존하는 것은 모든 feature 모듈이 공통적으로 가지고 있는 특징이다. (적어도 내 프로젝트는 이러하다. 당연하게도 무조건 위에서 언급한 것들이 공통적이지 않을 수도 있다.)

```kotlin
class MapisodeFeaturePlugin : Plugin<Project> {
	override fun apply(target: Project) {
		with(target) {
			pluginManager.apply {
				apply("com.android.library")
				apply("mapisode.android.hilt")
				apply("mapisode.android.compose")
			}

			dependencies {
				implementation(project(":core:ui"))
				implementation(project(":core:designsystem"))
				implementation(project(":core:model"))
				implementation(project(":core:navigation"))
				implementation(libs.getBundle("navigation"))
			}
		}
	}
}
```

위와 같이 내가 생각한대로 feature 모듈을 위한 플러그인을 설계할 수 있다. 위에서 설명한대로 의존성과 플러그인을 적용하여 feature 모듈에서 필요한 공통 로직을 미리 정의해둔 뒤, feature 모듈들에게 모두 적용하면 된다.

(위에서 아직 이해가 안가는 플러그인 적용 부분이 있을 것이다. 이 부분은 곧 이어서 설명할 예정이다. )

```kotlin
	override fun apply(target: Project) {
		with(target) {
			pluginManager.apply {
				apply("mapisode.android.library")
				apply("mapisode.android.hilt")
				apply("org.jetbrains.kotlin.plugin.serialization")
			}

			dependencies {
				val retrofitBom = libs.getLibrary("retrofit-bom")
				implementation(platform(retrofitBom))
				implementation(project(":core:model"))
				implementation(project(":core:network"))
				implementation(libs.getLibrary("kotlinx.serialization.json"))
				implementation(libs.getBundle("retrofit"))
			}
		}
	}
}
```

data 모듈을 위한 플러그인도 마찬가지이다. 공통적으로 필요한 의존성과 플러그인을 미리 적용한 플러그인을 구현하였으며, core:model과 Okhttp 관련 로직이 있는 core:network 모듈을 의존하게끔 설계하였다.

## 이제 어떻게 커스텀 플러그인을 사용할 것인가?

이제 플러그인들을 구현하였으면, 사용을 위해 플러그인을 등록해야 한다.

```kotlin
// build.gradle.kts (build-logic:convention)// 이전에 설명했던 코드는 생략 

gradlePlugin {
	plugins {
		register("androidApplication") {
			id = "mapisode.android.application"
			implementationClass = "AndroidApplicationPlugin"
		}

		register("androidLibrary") {
			id = "mapisode.android.library"
			implementationClass = "AndroidLibraryPlugin"
		}

		register("feature") {
			id = "mapisode.feature"
			implementationClass = ".MapisodeFeaturePlugin"
		}

		register("data") {
			id = "mapisode.data"
			implementationClass = "MapisodeDataPlugin"
		}
		...

	}
}
```

gradlePlugin 블록을 사용해 각 플러그인에 대한 `id`와 구현한 플러그인을 `implementationClass`로 정의하고 register를 통해 등록한다. `register` 안에 들어가는 이름은 임의의 식별자로, 프로젝트 내에서 해당 플러그인을 참조할 때 사용하는 Gradle에서의 로컬 식별자이다. 이 이름은 `id`와는 별개로 플러그인을 `gradlePlugin` 블록 내에서 구분하는 용도로 사용된다.

이제 여기서 Version Catalog 방식을 첨가하면 훨씬 더 안정적이게 사용이 가능하다.

```kotlin
# gradle/libs.versions.toml
# Custom Plugins (Build Logic)
mapisode-android-application = { id = "mapisode.android.application", version = "unspecified" }
mapisode-android-library = { id = "mapisode.android.library", version = "unspecified" }
mapisode-feature = { id = "mapisode.feature", version = "unspecified" }
mapisode-data = { id = "mapisode.data", version = "unspecified" }
...
```

`build.gradle.kts`에서 정의한 각 커스텀 플러그인의 `id`를 추가하여 버전 카탈로그로 관리할 수 있게 한다. 여기서 `mapisode-feature`와 같은 별칭을 부여하여 각 모듈에서 쉽게 호출할 수 있도록 한다.

```kotlin
// build.gradle.kts(feature:home) 
plugins {
    alias(libs.plugins.mapisode.feature)  
}
```

기존에 `feature:home` 모듈의 `build.gradle.kts`에는 SDK 설정부터 의존성 추가까지 중복되는 코드가 상당히 많았을 것이다. 하지만 **build-logic**을 적용하고 나서는 위 3줄이면 공통적으로 적용한 플러그인 및 안드로이드 설정, 의존성들이 모두 적용된다. 물론 `feature:home` 모듈에서만 사용되는 의존성과 플러그인은 별도로 등록해야 한다.

## 개인적인 감상

기존에 멀티 모듈 프로젝트에서는 각 모듈마다 사용하는 라이브러리들이 같은데도 어쩔 수 없이 중복 코드를 작성해야만 했다. 버전 카탈로그 방식이 도입되기 전에는, 모듈들에 적용한 같은 라이브러리에 버전을 서로 다른 것을 사용한 적도 있다.

이제 build-logic을 사용하면 위 문제들이 전부 사라진다. 사용하지 않을 이유를 아직 느끼지 못하고 있으며, 이 부분은 아마 관리하는 프로젝트, 모듈이 훨씬 더 많아지면 단점이 슬슬 보일 것 같다. 아직 더 공부하고 연구해보아야 할 것 같다.

하지만 공통 로직 추출이라는 것은 그리 간단한 것이 아니다. 무엇을 추출할지는 매우 어려운 결정이며, 시스템을 잘 설계하는 것을 예술이라고 표현하기도 한다. 항상 **균형**을 찾아야 하며, 이 균형이라는 것은 어쩔 수 없이 경험을 통해 배워야 할 것 같다. 이에 관련해 다룬 글이 있으니 참고하면 좋을 것 같다.

[**Effective Kotlin읽고 Compose에 적용하기**](post/effective-kotlin)
