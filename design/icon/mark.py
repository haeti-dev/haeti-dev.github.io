"""Haeti 마크(A안 "h.") SVG 생성기. 16칸 격자 좌표. h는 덧칠 없이 한 윤곽선으로 그린다."""
import math

# 기둥 x 3~5(위 끝 반지름 1), 아치 중심 (6.5, 10.3). 세로 획이 16·32px 픽셀 경계에 맞고 좌우 여백이 3:3 바깥 반지름 3.5 · 안쪽 1.5, 기준선 y 13
_JOIN = 10.3 - math.sqrt(3.5 ** 2 - 1.5 ** 2)   # 기둥 오른쪽 변이 바깥 아치와 만나는 높이
H_PATH = (f"M3 13V4A1 1 0 0 1 5 4V{_JOIN:.4f}"
          "A3.5 3.5 0 0 1 10 10.3V13H8V10.3A1.5 1.5 0 0 0 5 10.3V13Z")

def mark_svg(bg, fg, dot, dark=None, inset=0.0, radius=3.6):
    """inset: 맥 앱 아이콘처럼 바탕을 캔버스 안쪽으로 들일 비율."""
    pad = 16 * inset
    k = (16 - 2 * pad) / 16
    style = ""
    if dark:
        style = (f"<style>.bg{{fill:{bg}}}.fg{{fill:{fg}}}"
                 f"@media (prefers-color-scheme: dark){{.bg{{fill:{dark[0]}}}.fg{{fill:{dark[1]}}}}}</style>")
    bga = 'class="bg"' if dark else f'fill="{bg}"'
    fga = 'class="fg"' if dark else f'fill="{fg}"'
    return (f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16">{style}'
            f'<g transform="translate({pad:.4f} {pad:.4f}) scale({k:.6f})">'
            f'<rect {bga} width="16" height="16" rx="{radius}"/>'
            f'<path {fga} d="{H_PATH}"/>'
            f'<rect fill="{dot}" x="11" y="11" width="2" height="2" rx="1"/>'
            '</g></svg>\n')

INK, OFF, BLUE = "#16181D", "#FCFCFD", "#2B6BEF"
open("favicon.svg", "w").write(mark_svg(INK, OFF, BLUE, dark=(OFF, INK)))
open("mark-light.svg", "w").write(mark_svg(INK, OFF, BLUE))
open("mark-dark.svg", "w").write(mark_svg(OFF, INK, BLUE))
open("mac.svg", "w").write(mark_svg(INK, OFF, BLUE, inset=100 / 1024))
print("ok")
open("apple-touch.svg", "w").write(mark_svg(INK, OFF, BLUE, radius=0))
