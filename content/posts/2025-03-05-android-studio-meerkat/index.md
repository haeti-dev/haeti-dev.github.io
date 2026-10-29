---
title: Android Studio Meerkat 버전에서 추가된 기능들에 대하여
date: 2025-03-05
category: 안드로이드
summary: Android Studio Meerkat | 2024.3.1 버전에서 추가된 기능들과 개선 사항들에 대해 다루는 글입니다.
thumbnail: images/thumbnail.webp
draft: false
---

2025년 3월 4일 부로 Stable이 된 **Android Studio Meerkat** 버전에서 업데이트 된 내용들을 정리해보았다. 추가로 다음 버전인 2024.3.2 Preview 버전에 대해서도 정리해보았다. (흥미로운 부분들에 대해 ⭐ 이모지를 붙였으니 참고하시길 바랍니다)

## 1. Compose Preview 개선 사항 ⭐

![](images/01.gif){width=100 align=center}

- Preview 창을 확대/축소할 때 좀 더 부드럽고 반응이 빨라졌다.
- Preview 그룹별로 접고 펼칠 수 있게 되었다. Notion의 **토글 기능**처럼 Preview를 보기 좋게 정리할 수 있다.
- Grid로 보기가 디폴트가 되었다. 마우스 우클릭을 통해 갤러리 모드로 전환할 수 있으며, 리스트로 보기는 삭제되었다.

## 2. Compose Design Tools ⭐

![](images/02.webp){width=60 align=center}

- 애니메이션을 ms 단위로 쪼개 검사할 수 있는 기능이 추가되었다.

## 3. Android Application과 KMP Shared Module 통합 ⭐

![](images/03.webp){width=100 align=center}

- 새로운 Android 프로젝트를 생성한 뒤, New → Module 을 클릭하면 위와 같이 KMP Shared Module을 생성할 수 있다.
- 이를 통해 간단하게 기존의 [KMP Wizard](https://kmp.jetbrains.com/) 대신에 프로젝트에서 KMP Shared Module을 생성할 수 있어 유용한 기능인 것 같다.

## 4. Device Manager UX 업데이트

![](images/04.webp){width=100 align=center}

- Device Manager에서 Create Virtual Device 또는 Select Remote Devices를 통해 기기를 추가할 때, 필터링과 추천 기능을 사용할 수 있게 되었다.
- 이를 통해 다양한 환경에서 가장 우수한 성능을 발휘하는 기기를 만들 수 있다.

## 5. 새로운 Gemini 기능 ⭐

![](images/05.webp){width=100 align=center}

- Class에 우클릭을 하여 Gemini → Generate Unit Test Scenarios를 선택하면 Gemini가 추천한 단위 테스트 코드를 생성할 수 있다.

![](images/06.webp){width=100 align=center}

- App Quality Insights 도구 창에서 발생하는 비정상 종료 보고서를 분석하고, 통계를 생성하고, 요약을 제공 받을 수 있다.
- 추가로 샘플 코드 및 관련 문서 링크를 획득할 수 있다.

---

이제 다음 **2024.3.2** 버전에 나올 **Meerkat Feature Drop**에는 어떠한 기능들이 추가될지 알아보자.

## 1. Android Studio 내 Gemini에서 이미지 첨부를 지원

![](images/07.webp){width=50 align=center}

- 많은 생성형 UI 툴에서 지원하는 이미지 첨부 기능을, Android Studio 내 Gemini 모달에서도 지원한다.

## 2. 앱 백업 및 복원을 사용하여 테스트 및 개발

- 사용자가 새 기기로 전환하거나 클라우드에서 복원한 후에도 앱을 정상적으로 사용할 수 있는지를 테스트할 수 있는 기능이 추가되었다.
- 자세한 백업 및 복원 구현 과정은 [공식 문서](https://developer.android.com/studio/preview/features?hl=ko#test-backup-restore)에 자세히 나와있다.

## 3. 테마 아이콘 지원 ⭐

기존에 Android 13 이상의 기기에서는 앱 아이콘에 휴대폰에서 지정한 테마를 적용할 수 있다. 대표적으로 ChatGPT나 Galaxy Store 또는 Google의 기본적인 앱들은 이를 이미 적용하고 있었다.

![](images/08.webp){width=50 align=center}

아이콘의 모양을 완전히 제어하려면 [적응형 런처 아이콘](https://developer.android.com/develop/ui/views/launch/icon_design_adaptive?hl=ko)을 추가해야 하며, Preview를 통해 아이콘이 어떻게 표시될 지 미리 확인할 수 있다.

## 4. Android Studio Config Directory 변경

- 카나리, 베타, Stable 버전에서 동일한 사용자 구성을 사용하도록 변경되었다.
- 이에 따라 카나리 및 베타 버전의 Android Studio Config Directory 경로에서 preview가 삭제되었다.

## 5. 프롬프트 라이브러리

- 자주 사용하는 Gemini 프롬프트를 저장하고 검색할 수 있다.

## 6. Android Studio XR 지원 사용 가능

- XR 템플릿, XR 에뮬레이터, XR용 Layout Inspector가 추가되었다.

## 7. Gemini를 사용하여 Composable Preview 생성 ⭐

![](images/09.webp){width=100 align=center}

- composable에 마우스 우클릭을 한 뒤 Gemini → Generate “Composable Name” Preview를 선택하면 Preview를 생성할 수 있다.
- 만족스러운 Preview가 아닐 수도 있으나, 생산성 향상에 도움이 되는 것은 분명하다.

---

업데이트 된 날짜에 실제로 릴리즈 노트에 나온 내용들이 진짜로 동작하는지 확인해보는 과정이 재밌었고, 더 기억에 남을 것 같다. 앞으로도 새로운 버전이 나올 때마다 직접 실험해보자고 마음을 먹게된 블로그 포스팅이었다.

+) 추후 구글의 공식 유튜브 영상이 업로드 되면 내용을 추가/수정해보겠습니다.

#### 출처

[https://developer.android.com/studio](https://developer.android.com/studio)

[https://developer.android.com/studio/releases](https://developer.android.com/studio/releases)

[https://developer.android.com/studio/preview/features?hl=ko](https://developer.android.com/studio/preview/features?hl=ko)
