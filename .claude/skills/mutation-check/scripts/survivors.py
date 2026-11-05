# PIT 결과(mutations.xml)에서 잡히지 않은 가짜 실수를 소스 줄과 함께 줄별로 묶어 보인다.
# 사용: python3 survivors.py <mutations.xml> <소스 루트>...
# 원인이 우리 코드가 아닌 것은 따로 센다: Kotlin이 넣는 null 확인, 표준 함수 인라인(소스 줄 수를 넘는 줄 번호).
import sys
import xml.etree.ElementTree as ET
from collections import Counter, defaultdict
from pathlib import Path

report, *roots = sys.argv[1:]
mutations = list(ET.parse(report).getroot())


def source_line(file_name, package, line):
    for root in roots:
        path = Path(root) / package.replace(".", "/") / file_name
        if path.is_file():
            lines = path.read_text().split("\n")
            return lines[line - 1].strip() if line <= len(lines) else None
    return None


status = Counter(m.get("status") for m in mutations)
print(f"가짜 실수 {len(mutations)}개: " + ", ".join(f"{k} {v}" for k, v in status.most_common()))

groups = defaultdict(list)
generated = Counter()
for m in mutations:
    if m.get("status") == "KILLED":
        continue
    cls = m.find("mutatedClass").text
    line = int(m.find("lineNumber").text)
    text = source_line(m.find("sourceFile").text, cls.rsplit(".", 1)[0], line)
    description = m.find("description").text
    if "Intrinsics::check" in description:
        generated["Kotlin null 확인"] += 1
    elif text is None:
        generated["표준 함수 인라인 또는 소스 없음"] += 1
    else:
        groups[(m.find("sourceFile").text, line, text)].append(f"{m.get('status')}: {description}")

for (file_name, line, text), items in sorted(groups.items()):
    print(f"\n{file_name}:{line}  {text[:110]}")
    for item in items:
        print(f"    {item}")
if generated:
    print("\n우리 코드가 아니라 뺀 것: " + ", ".join(f"{k} {v}" for k, v in generated.items()))
