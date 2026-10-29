---
title: [트러블 슈팅] Compose Dialog의 너비에 대한 속성
date: 2024-05-22
category: 안드로이드
summary: Dialog 너비 지정에 대한 트러블 슈팅
thumbnail: images/thumbnail.webp
draft: false
---

## 문제 상황

기기의 너비를 가져와서 dialog 크기를 기기 너비 - 36dp 로 지정해 어떠한 기기에서든 기기 너비 기준 항상 동일한 padding을 가지도록 했다.

하지만 **특정 기기에서** 지정해준 padding 보다 더 크게 벌어져, TextOverflow.Ellipsis로 지정해준 **Text가 짤리는 현상이 일어났다**.

![](images/01.webp){width=100 align=center}

## 해결 방법

Dialog의 너비를 정적으로 지정해주었기에 이 코드가 제대로 동작이 왜 되지 않는가에 대해 알아보았다. Compose Dialog에서는 DialogProperties를 통해 Dialog에 속성을 부여할 수 있다.

![](images/02.webp){width=100 align=center}

설명을 읽어보면 dialog 외부를 눌러도 꺼지지 않게 하는 속성, 뒤로가기를 눌러도 꺼지지 않게 하는 속성 등 총 5가지의 속성이 있다. 이 중 `usePlatformDafaultWidth`가 나의 문제 상황과 관련이 있는 속성이란 것을 알게 되었다.

![](images/03.webp){width=100 align=center}

Dialog는 사용되는 Platform에 맞게 미적으로 보기 좋고 다양한 화면 크기에 쉽게 상호작용 할 수 있도록 제한된다. 하지만 이 속성을 사용하면 개발자가 원하는 너비를 정적으로 지정할 수 없기에 이 값을 false로 제한하고 너비를 지정해줘야 한다.

이 정보를 바탕으로 코드를 수정해보았다.

### AS-IS

![](images/04.webp){width=100 align=center}

### TO-BE

![](images/05.webp){width=100 align=center}

위와 같이 `screenWidth`대신 `usePlatformDefaultWidth`속성을 `false`로 한 다음, Dialog의 너비를 `Modifier`를 이용해 전체 너비에서 양옆으로 18dp 만큼 벌어지게 지정하였다.

Compose 컴포넌트들에는 정말 유용한 속성들이 많으니 앞으로 잘 이용해봐야겠다!
