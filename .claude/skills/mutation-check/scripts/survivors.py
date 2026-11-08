# PIT 결과(mutations.xml)에서 잡히지 않은 가짜 실수를 소스 줄과 함께 줄별로 묶어 보인다.
# 사용: python3 survivors.py <mutations.xml> <소스 루트>...
# 원인이 우리 코드가 아닌 것은 따로 센다: Kotlin이 넣는 null 확인, 표준 함수 인라인(소스 줄 수를 넘는 줄 번호).
# 소스 파일을 못 찾으면 거르지 않고 멈춘다. 소스 루트가 틀렸을 때 진짜 구멍이 조용히 빠지지 않게 한다.
import sys
import xml.etree.ElementTree as ET
from collections import Counter, defaultdict
from pathlib import Path

report, *roots = sys.argv[1:]
mutations = list(ET.parse(report).getroot())


def source_line(file_name, package, line):
    """소스 줄. 줄 번호가 파일 끝을 넘으면(인라인된 표준 함수) None. 파일이 없으면 FileNotFoundError."""
    for root in roots:
        path = Path(root) / package.replace(".", "/") / file_name
        if path.is_file():
            lines = path.read_text().split("\n")
            return lines[line - 1].strip() if line <= len(lines) else None
    raise FileNotFoundError(f"{package}.{file_name}")


status = Counter(m.get("status") for m in mutations)
print(f"가짜 실수 {len(mutations)}개: " + ", ".join(f"{k} {v}" for k, v in status.most_common()))

groups = defaultdict(list)
generated = Counter()
missing = set()
for m in mutations:
    if m.get("status") == "KILLED":
        continue
    cls = m.find("mutatedClass").text
    line = int(m.find("lineNumber").text)
    description = m.find("description").text
    try:
        text = source_line(m.find("sourceFile").text, cls.rsplit(".", 1)[0], line)
    except FileNotFoundError as error:
        missing.add(str(error))
        continue
    if "Intrinsics::check" in description:
        generated["Kotlin null 확인"] += 1
    elif text is None:
        generated["표준 함수 인라인(파일 끝을 넘는 줄)"] += 1
    else:
        groups[(m.find("sourceFile").text, line, text)].append(f"{m.get('status')}: {description}")

for (file_name, line, text), items in sorted(groups.items()):
    print(f"\n{file_name}:{line}  {text[:110]}")
    for item in items:
        print(f"    {item}")
if generated:
    print("\n우리 코드가 아니라 뺀 것: " + ", ".join(f"{k} {v}" for k, v in generated.items()))
if missing:
    sys.exit("\n소스를 찾지 못했다. 소스 루트를 확인한다(KMP는 src/commonMain/kotlin, build-logic은 src/main/kotlin): "
             + ", ".join(sorted(missing)))
