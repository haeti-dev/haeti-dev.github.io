# 글 올리기 절차

한국어 원문과 영어 번역본을 함께 올린다. 번역은 직역이 아니라 영어권 개발자가 쓴 것처럼 다시 쓰고,
최종 판단은 사람이 한다.

## 1. 쓰기 (어드민)

1. 어드민을 연다: `~/Applications/Haeti.log Admin.app`(Dock·Spotlight). 처음 한 번, 그리고 어드민 코드를 고친 뒤에는
   `./gradlew :app:admin:installAdminApp`으로 앱을 만든다(`./gradlew :app:admin:run`으로 바로 띄워도 된다).
   **새 글**로 폴더를 만든다. 새 글은 `draft: true`로 시작한다.
2. 한국어로 쓴다. 미리보기(웹/모바일)로 확인한다. 썸네일은 `images/`에 넣고 `thumbnail:`에 적는다.
   이미지는 Finder에서 에디터로 끌어다 놓거나, 스크린샷을 ⌘V로 붙여넣거나, **이미지 넣기** 버튼으로 넣는다.
   `images/`에 저장되면서 폭 1600px를 넘으면 줄이고 webp(품질 85)로 바꾼다. GIF와 변환해도 작아지지 않는 이미지는 원본 그대로다.
3. 글이 다 됐으면 저장한다. 아직 `draft: true` 그대로 둔다.

## 2. 번역 (스킬)

1. 어드민에서 **번역 만들기/번역 갱신** 버튼(또는 사이드바 `EN 갱신`·`EN 없음` 배지)을 누른다. 어드민이
   `claude -p`로 아래 스킬을 돌리고 1~3분 뒤 결과를 사이드바 아래에 알린다. 로그는 `$TMPDIR/haeti-translate/`.
   CLI가 로그인돼 있어야 한다(터미널에서 `claude` → `/login`). Claude Code에서 직접
   "이 글 영어로 번역해줘", "번역본 만들어줘 <슬러그>"처럼 요청해도 같다.
   `.claude/skills/haeti-translate-post` 스킬이 `index.en.md`를 쓰고, 원문 지문(`source:`)을 기록한다.
2. 스킬은 직역 대신 영어 개발 블로그 문체로 다시 쓴다. 자연스러움을 위해 바꾼 부분은 보고에 따로 적는다.
3. 어드민의 **EN 탭**에서 읽어 보고 고친다. 미리보기도 영어로 확인한다.

## 3. 발행

1. 한국어 원문의 `draft: true`를 `false`로 바꾸고 저장한다. 발행 표시는 지문에 들어가지 않아 번역이 낡았다고 뜨지 않는다.
2. 커밋하고 푸시한다. CI가 `checkTranslations`로 번역본 상태를 점검하고, 배포 워크플로가 Pages에 올린다.

## 원문을 고쳤을 때

제목·요약·본문을 고치면 지문이 바뀌어 어드민 목록에 **EN 갱신** 배지가 뜨고, `checkTranslations`가 경고한다.
배지를 누르거나 스킬에 "번역 갱신해줘"라고 하면 바뀐 부분만 다시 옮기고 지문을 새로 기록한다.
바뀐 부분은 `translation_status.py changed <글 디렉터리>`(번역본을 마지막으로 커밋한 뒤의 원문 diff)로 찾는다.

## 상태 확인

```bash
python3 .claude/skills/haeti-translate-post/scripts/translation_status.py status   # 글마다 missing / stale / up-to-date
./gradlew :core:data:checkTranslations                                            # 발행 글만 점검 (CI와 같음)
```

어드민 배지: `EN` 최신 · `EN 갱신` 원문이 바뀜 · `EN 없음` 발행 글인데 번역본 없음(초안은 표시 안 함).
