---
title: 버튼 클릭 몇 번으로 앱 배포하기: GitHub Actions CD 구축
date: 2025-03-19
category: 안드로이드
summary: GitHub Actions 워크플로우로 Firebase App Distribution에 안드로이드 앱을 자동 배포하는 방법을 다룹니다.
thumbnail: images/thumbnail.webp
draft: false
---

### 배경

프로젝트에서 QA 테스트를 위해 Firebase App Distribution을 사용하였다. 그동안은 debug 버전과 release 버전의 APK를 수동으로 Firebase App Distribution에 업로드하여 테스터들에게 배포하였고, 테스터와 릴리즈 노트를 모두 수동으로 지정/작성하였다. 이러한 과정을 GitHub Actions를 통해 테스터들에게 자동 배포하는 방법을 기록해보려고 한다.

### 목표

> 프로젝트마다 CD(Continous Delivery) 전략이 다를 수 있으므로, 이번 글에서 사용한 전략을 요약해보았다. 적용 시에 참고하면 좋을 것 같다.

- **개발자가 원하는 시점**에 간단한 버튼 클릭 / 텍스트 입력으로 **배포**할 수 있는 워크 플로우 구축
- debug와 release **빌드 타입을 선택적**으로 배포할 수 있는 기능
- **테스터 그룹**은 고정 지정 / **릴리즈 노트**는 수동으로 작성 (commit message에서 추출할 수 있으나, 프로젝트 컨벤션 특징 상 도입하지 않았다.)
- 배포 시 **Git tag 자동 생성** 옵션 제공

## 구현 과정

### 1. Firebase 서비스 계정 설정 및 앱 ID 추출

GitHub Action을 사용해서 Firebase App Distribution CD를 구축하기 위해서는 2가지 값이 필요하다.

위 과정은 이미 많은 블로그에 자세한 과정이 나와 있으므로 간단하게 설명하겠다.

가장 먼저 Firebase Console → IAM 및 관리자 → 서비스 계정 → 키 생성 후 **JSON 파일**을 다운로드한다. 이 JSON 파일 내부의 값들을 GitHub Repository의 Setting → Secrets and variables → Actions → New repository secret에 등록 후 저장한다. 이 값은 GitHub Actions가 Firebase API에 인증하기 위한 **자격 증명**이라고 보면 된다. 이를 통해 GitHub Actions가 Firebase App Distribution에 APK에 업로드할 권한을 부여한다.

Firebase Console → 프로젝트 설정에서 확인할 수 있는 **앱 ID**를 마찬가지로 GitHub Secrets에 등록한다. 필자의 경우는 debug/release 앱 2개가 존재하므로 각각의 ID를 GitHub Secrets에 등록하였다. 이 값은 Firebase 프로젝트 내에서 특정 앱을 식별하는 **고유 식별자**이다.

### 2. GitHub Actions 워크플로우 파일 생성

먼저 프로젝트의 루트에서 `.github/workflows/firebase_distribution.yml` 파일을 생성한다.

#### 1) workflow_dispatch 생성

요구사항에 부합하도록 개발자가 수동으로 워크 플로우를 실행할 수 있도록 해야 한다. 이를 `workflow_dispatch`로 트리거 할 수 있다.

```yaml
name: Firebase App Distribution

on:
  workflow_dispatch:
    inputs:
      buildVariant:
        description: 'Build Variant'
        required: true
        default: 'debug'
        type: choice
        options:
          - debug
          - release
      branch:
        description: '빌드할 브랜치'
        required: true
        default: 'release'
        type: string
      releaseNotes:
        description: '릴리즈 노트'
        required: false
        type: string
      createTag:
        description: 'Git 태그를 생성할까요?'
        required: true
        default: true
        type: boolean
```

`workflow_dispatch`를 통해 Build Variant, 빌드할 브랜치, 릴리즈 노트, Git tag에 대한 설정을 하였다.

- `required` : 필수로 입력할지에 대한 여부를 결정한다.
- `defalut` : 기본값을 지정할 수 있다.
- `type` : **string / boolean** 등등 타입을 지정할 수 있고, **choice**를 통해서 보기 중 하나를 선택하는 UI를 제공할 수 있다.

위 워크 플로우를 통해 GitHub에서 간단한 GUI를 제공할 수 있으며, 만약 자동으로 배포를 원할 경우에는 별도로 이 설정은 필요가 없을 것이다.

#### 2) job 생성 및 기초 설정

```yaml
permissions:
  contents: write

jobs:
  deploy:
    name: Build and Deploy to Firebase
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v4
        with:
          ref: ${{ github.event.inputs.branch }}
          fetch-depth: 0

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: gradle
          
      - name: Setup Gradle Cache
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
            ~/.android/build-cache
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties', '**/libs.versions.toml') }}
          restore-keys: |
            ${{ runner.os }}-gradle-
            
      - name: Grant execute permission for gradlew
        run: chmod +x ./gradlew
```

- `permissions`: 태그를 생성하기 위해서는 쓰기 권한이 부여되어야 한다.
- `Checkout code`: 지정된 브랜치의 코드를 가져온다.
- `Set up JDK 17`: JDK 환경을 설정한다.
- `Setup Gradle Cache`: Gradle 빌드 캐시를 설정하여 빌드 속도를 향상시킨다.
  - 이 단계는 이전 워크플로우 실행에서 생성된 Gradle 캐시를 재사용하여 의존성 다운로드 시간을 크게 단축시킬 수 있다.
- `Grant execute permission`: Gradle 래퍼에 실행 권한을 부여한다.

#### 3) Target 설정

선택한 **Build Variant**에 따라 환경 변수를 설정한다.

```yaml
- name: Set Distribution Target
  id: set-target
  run: |
    if [[ "${{ github.event.inputs.buildVariant }}" == "debug" ]]; then
      echo "APP_ID=${{ secrets.FIREBASE_DEV_APP_ID }}" >> $GITHUB_ENV
      echo "TESTERS=qa팀,android팀" >> $GITHUB_ENV
      echo "VARIANT=Debug" >> $GITHUB_ENV
    else
      echo "APP_ID=${{ secrets.FIREBASE_RELEASE_APP_ID }}" >> $GITHUB_ENV
      echo "TESTERS=qa팀,android팀" >> $GITHUB_ENV
      echo "VARIANT=Release" >> $GITHUB_ENV
    fi
```

**환경 변수의 용도**

- `APP_ID`: 배포할 Firebase 앱의 ID이다.
- `TESTERS`: APK가 배포될 테스터 그룹을 쉼표로 구분하여 지정한다.
- `VARIANT`: Gradle 빌드 변형 이름이다.

#### 4) 태그 설정

사용자가 GitHub Workflow UI에서 태그 생성을 체크했을 경우 배포 버전에 대한 Git tag를 생성한다.

```yaml
- name: Extract Version from toml
  run: |
    VERSION=$(grep -Po 'versionName\\s*=\\s*"\\K[^"]*' gradle/libs.versions.toml)
    echo "APP_VERSION=$VERSION" >> $GITHUB_ENV

- name: Create Git Tag
  if: ${{ github.event.inputs.createTag == 'true' }}
  run: |
    git config --local user.email "깃허브@이메일.com"
    git config --local user.name "깃허브 유저 네임"
    git tag -a "v${{ env.APP_VERSION }}" -m "Release version ${{ env.APP_VERSION }}"
    git push origin "v${{ env.APP_VERSION }}"
```

- 먼저 `libs.versions.toml` 파일에서 **앱 버전을 추출**한다. 앱 버전이 `build.gradle.kts`에 기록되어 있거나, 다른 파일에 있을 경우 코드를 수정해야 한다.
- 추출한 앱 버전을 활용하여 태그를 생성한다. ex) versionName : 1.0.1 → tag : v1.0.1
- 이때 태그를 생성할 깃허브 계정에 대한 정보를 등록해야 한다. 이 계정은 프로젝트에서 공통으로 사용할 수 있는 계정으로 등록하는 것을 추천한다.

#### 4) 릴리즈 노트 설정 및 APK 배포

```yaml
- name: Build APK
  run: ./gradlew assemble${{ env.VARIANT }}

- name: Upload APK to Firebase App Distribution
  uses: wzieba/Firebase-Distribution-Github-Action@v1
  with:
    appId: ${{ env.APP_ID }}
    serviceCredentialsFileContent: ${{ secrets.FIREBASE_SERVICE_ACCOUNT }}
    groups: ${{ env.TESTERS }}
    file: app/build/outputs/apk/${{ github.event.inputs.buildVariant }}/app-${{ github.event.inputs.buildVariant }}.apk
    releaseNotes: ${{ env.RELEASE_NOTES }}
```

- 선택한 build variant에 따라 APK를 빌드한다.
- `wzieba의 Firebase-Distribution-Github-Action`을 사용하여 APK를 배포한다.
  - 앞서 설정한 환경 변수들을 활용하여 필요한 정보를 함께 담아 배포한다.

## 구현 결과 및 사용 방법

수동으로 Workflow를 실행하기 위해서 해당 Repository의 Actions에 접속하여 현재 작성한 Firebase App Distribution Action을 선택하고, Run workflow를 선택하면 아래와 같은 화면을 볼 수 있다.

![](images/01.webp){width=100 align=center}

해당 yml 파일이 잘동작하는지 확인하기 위해 테스트를 진행해볼 필요가 있다. GitHub Workflow에는 **Use workflow from** 이라는 브랜치 선택창을 디폴트로 제공한다. 이 곳에서 현재 작성한 yml 파일이 들어가 있는 브랜치를 선택하면 된다. 기본적으로는 디폴트 브랜치가 선택되어 있다.

이제 필요한 옵션에 대한 값들을 입력하고 Run workflow를 클릭하면 된다. 워크플로우가 성공적으로 실행되면 Firebase App Distribution에 정상적으로 APK가 업로드된 것을 확인할 수 있고, Git tag가 Push 된 것을 확인할 수 있다.

이제 버튼 클릭 몇 번만으로 배포 과정을 자동화할 수 있다. 물론 수동으로 워크플로우를 조작하기 때문에 반(?)자동이라고 할 수 있을 것 같다. 하지만, 특정 branch에 push나 merge 될 때마다 배포하는 것을 구현할 수 있으므로, 이 기능이 필요한 프로젝트에는 적절하게 변형하여 사용하면 좋을 것 같다.

### 레퍼런스

[https://github.com/wzieba/Firebase-Distribution-Github-Action](https://github.com/wzieba/Firebase-Distribution-Github-Action)
