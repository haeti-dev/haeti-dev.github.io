# Haeti 마크 ("h.")

흰 소문자 h + 브랜드 블루 마침표(워드마크 `Haeti.log`의 점). 16칸 격자 위 도형이라 16·32px에서 세로 획이 픽셀에 맞는다.
색: 오프블랙 `#16181D`, 오프화이트 `#FCFCFD`, 블루 `#2B6BEF`. 다크 모드에서는 바탕과 h를 뒤집는다.

다시 만들기 (rsvg-convert·iconutil·Pillow 필요):

```bash
python3 mark.py   # favicon.svg, mark-light/dark.svg, mac.svg, apple-touch.svg
```

산출물 위치: `app/web/src/wasmJsMain/resources/`(favicon.svg·ico, apple-touch-icon.png),
`app/admin/icons/HaetiAdmin.icns`(맥 앱, 1024 캔버스 안 824 사각형). 래스터 변환 명령은 커밋 기록 참고.
