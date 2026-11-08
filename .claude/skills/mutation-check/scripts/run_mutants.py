# 가짜 실수 실행기. 하나씩 넣고 → 테스트 → 원복한다. 판정은 테스트 결과로만 한다.
# 사용: python3 run_mutants.py <mutants.json> <대상 파일> <작업 디렉터리> <테스트 명령...>
# 결과는 <mutants.json>.result.json. 원본에서 테스트가 실패하면 시작하지 않는다.
import filecmp
import json
import shutil
import subprocess
import sys
import time
from pathlib import Path

TIMEOUT_SECONDS = 600  # 무한 반복을 만든 실수가 실행기를 멈추지 않게 한다.

mutants_path, target_name, workdir, *test_cmd = sys.argv[1:]
mutants = json.loads(Path(mutants_path).read_text())
target = Path(target_name)
backup = target.with_name(target.name + ".mutation-backup")
# 이전 실행이 강제 종료되면 원본은 백업에만 남는다. 덮어쓰면 원본을 잃으니 멈추고 복구를 맡긴다.
try:
    with open(backup, "xb") as copy:
        copy.write(target.read_bytes())
except FileExistsError:
    sys.exit(f"이전 실행의 백업이 남아 있다: {backup}\n원본일 수 있으니 확인해 {target}로 되돌리고 백업을 지운 뒤 다시 실행한다.")


def run_tests():
    """테스트를 돌려 (판정, 걸린 초)를 돌려준다. 실수를 넣지 않은 기준 실행에도 쓴다."""
    started = time.time()
    try:
        result = subprocess.run(test_cmd, cwd=workdir, capture_output=True, text=True, timeout=TIMEOUT_SECONDS)
    except subprocess.TimeoutExpired:
        return "시간 초과(잡힘)", time.time() - started
    output = result.stdout + result.stderr
    if "e: file://" in output or "Compilation error" in output:
        return "컴파일 안 됨", time.time() - started
    return ("통과" if result.returncode == 0 else "잡힘"), time.time() - started


try:
    verdict, seconds = run_tests()
    if verdict != "통과":
        sys.exit(f"원본에서 테스트가 '{verdict}'. 중단한다.")
    print(f"기준 실행: 통과 ({seconds:.0f}초)")
    results = []
    source = backup.read_text()
    for mutant in mutants:
        if source.count(mutant["original"]) != 1:
            results.append({**mutant, "status": "적용 불가", "note": "원문이 정확히 한 번 나오지 않음"})
            continue
        target.write_text(source.replace(mutant["original"], mutant["replacement"]))
        verdict, seconds = run_tests()
        status = "살아남음" if verdict == "통과" else verdict
        results.append({**mutant, "status": status, "note": f"{seconds:.0f}초"})
        shutil.copyfile(backup, target)
        print(f"#{mutant['id']} {status}")
    Path(mutants_path + ".result.json").write_text(json.dumps(results, ensure_ascii=False, indent=2))
finally:
    shutil.copyfile(backup, target)
    if filecmp.cmp(backup, target, shallow=False):
        backup.unlink()
    else:
        print(f"경고: 원복 확인 실패. 백업은 {backup}")
