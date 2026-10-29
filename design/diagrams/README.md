# 다이어그램 키트

Claude Code에서는 `haeti-diagram` 스킬이 이 문서대로 그림을 만든다. "이 글에 흐름도 넣어 줘"처럼 말하면 된다.

README, `docs/`, 블로그 글에 넣는 구조도와 흐름도를 블로그와 같은 글꼴(Pretendard, JetBrains Mono)과
색(`HaetiColors`)으로 만든다. 그림은 HTML로 짜고, Playwright로 라이트/다크 PNG를 2배 해상도로 굽는다.

## 만들기

```bash
cd design/diagrams
npm install                       # 처음 한 번. 브라우저가 없다고 나오면 npx playwright install chromium
node render.mjs                   # 전부(templates/ 제외)
node render.mjs flow              # flow.html 하나만
node render.mjs posts/<글>/<그림>  # 글용 그림 하나
```

각 HTML의 `<meta name="out">`이 출력 위치다(저장소 루트 기준, 확장자 없이). `-light.png`, `-dark.png` 두 장이 생긴다.

| 파일 | 출력 | 쓰는 곳 |
|---|---|---|
| `flow.html` | `docs/readme/flow` | README, ARCHITECTURE 전체 구조 |
| `modules.html` | `docs/readme/modules` | README, ARCHITECTURE 모듈 |
| `pipeline.html` | `docs/diagrams/pipeline` | ARCHITECTURE 데이터 흐름 |

마크다운에는 두 장을 `<picture>`로 넣는다.

```html
<picture>
  <source media="(prefers-color-scheme: dark)" srcset="diagrams/pipeline-dark.png">
  <img alt="그림이 말하는 내용을 한 문장으로" src="diagrams/pipeline-light.png">
</picture>
```

## 부품 (`kit.css`)

| 클래스 | 모양 | 쓰임 |
|---|---|---|
| `.node` | 역할 / 이름 / 경로 / 설명 네 줄 카드 | 흐름의 한 단계 |
| `.node.hl` | 파란 테두리와 옅은 파랑 바탕 | 그림에서 가장 중요한 한 곳 |
| `.flow` | 노드를 가로로 놓고 사이에 화살표 | 순서가 있는 단계. `style="--cols: 3"`로 칸 수 |
| `.tiers` `.tier` `.tier-label` `.tier-note` | 왼쪽 라벨 + 회색 바탕 줄 + 줄 사이 메모 | 계층, 시점별 묶음 |
| `.chip` `.chip.wide` | 모노 이름 + 한 줄 설명 | 모듈, 파일처럼 이름이 중요한 것 |
| `.dashed` | 점선 상자 | 실체가 아닌 구분(소스셋, 단계) |
| `.pill` | 알약 모양 한 줄 라벨 | 연결선 위의 설명 |
| `svg.links` | 노드를 잇는 선 | `data-shape="under"`(아래로 돌아 잇기), `"drop"`(다음 줄로 꺾어 내려가기) |

`svg.links`는 `#diagram`의 첫 자식으로 둔다. 그래야 선이 노드와 라벨 밑으로 깔린다.

## 규칙

- 색은 `kit.css`의 토큰만 쓴다. 새 색이 필요하면 `HaetiColors`에 먼저 넣는다.
- 강조(`.hl`, 파란 선)는 그림 하나에 한 곳만.
- 글자는 짧게. 노드 설명은 두 줄을 넘기지 않는다.
- 폭은 1240px로 고정한다. README와 블로그 본문에서 줄여 보여도 읽히는 크기다.
- mermaid는 쓰지 않는다. 글꼴과 색이 블로그와 달라진다.

## 블로그 글에 쓸 때

출력 경로를 글 폴더(`content/posts/<글>/images/<이름>`)로 두고, 이미지 지시어의 `dark`로 다크 버전을 잇는다.
블로그와 어드민 미리보기 모두 페이지 테마에 따라 둘 중 하나를 그린다.

```markdown
![글이 블로그에 실리는 흐름](images/flow-light.png){width=100 dark=images/flow-dark.png}
```

그림의 HTML 원본은 글 폴더에 두지 않고 `design/diagrams/posts/<글 폴더>/`에 둔다. 글 폴더는 통째로 배포되기 때문이다.
`templates/`의 `post-steps.html`(세로 단계), `post-layers.html`(계층)을 복사해서 시작한다.

글에 넣는 그림은 `<div id="diagram" class="post">`로 680px 폭(본문 컬럼 760dp보다 조금 좁다)에 그리고 글자를 키운다. 휴대폰에서는
절반 크기로 줄어들기 때문이다. 단계가 셋을 넘으면 세로 흐름(`.flow-v`)으로 쌓는다.
영어판(`index.en.md`)이 있는 글은 영어 그림(`<이름>.en.html`, 출력 `<이름>-en`)도 만든다.
