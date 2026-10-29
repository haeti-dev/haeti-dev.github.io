#!/usr/bin/env python3
"""Haeti.log 번역본 상태 도구.

원문 지문(source hash) 규칙: index.md의 title, summary, 본문(프론트매터 뒤, 앞뒤 공백 제거)을
'\n'으로 이어 붙인 UTF-8 문자열의 SHA-256 앞 12자리. draft·date 등은 넣지 않는다.
같은 규칙이 app/admin(TranslationSource.kt)과 build-logic(TranslationCheckTask.kt)에도 있다.

사용법:
  translation_status.py status [content/posts]   # 글마다 번역 상태 표
  translation_status.py hash <글 디렉터리>         # 그 글 원문의 지문
  translation_status.py stamp <글 디렉터리>        # index.en.md 프론트매터의 source를 현재 지문으로 기록
  translation_status.py changed <글 디렉터리>      # 번역본을 마지막으로 커밋한 뒤 바뀐 원문 diff (stale 갱신용)
  translation_status.py hash-text < 원문          # 표준입력 원문의 지문 (테스트용)
"""
import hashlib
import subprocess
import sys
from pathlib import Path


def split_frontmatter(text: str):
    text = text.replace("\r\n", "\n")
    lines = text.split("\n")
    if not lines or lines[0].strip() != "---":
        return {}, text, None
    for i in range(1, len(lines)):
        if lines[i].strip() == "---":
            fields = {}
            for line in lines[1:i]:
                if ":" in line and line.index(":") > 0:
                    key, value = line.split(":", 1)
                    fields[key.strip()] = value.strip()
            return fields, "\n".join(lines[i + 1:]), lines[1:i]
    return {}, text, None


def source_hash(korean: str) -> str:
    fields, body, _ = split_frontmatter(korean)
    payload = "\n".join([fields.get("title", ""), fields.get("summary", ""), body.strip()])
    return hashlib.sha256(payload.encode("utf-8")).hexdigest()[:12]


def status_of(post_dir: Path):
    korean = (post_dir / "index.md").read_text(encoding="utf-8")
    fields, _, _ = split_frontmatter(korean)
    draft = fields.get("draft", "false").lower() == "true"
    english_file = post_dir / "index.en.md"
    if not english_file.exists():
        return draft, "missing"
    recorded = split_frontmatter(english_file.read_text(encoding="utf-8"))[0].get("source")
    return draft, "up-to-date" if recorded == source_hash(korean) else "stale"


def stamp(post_dir: Path):
    english_file = post_dir / "index.en.md"
    text = english_file.read_text(encoding="utf-8").replace("\r\n", "\n")
    digest = source_hash((post_dir / "index.md").read_text(encoding="utf-8"))
    fields, body, header = split_frontmatter(text)
    if header is None:
        raise SystemExit("index.en.md에 프론트매터(---)가 없습니다")
    header = [line for line in header if not line.strip().startswith("source:")] + [f"source: {digest}"]
    english_file.write_text("---\n" + "\n".join(header) + "\n---\n" + body, encoding="utf-8")
    print(digest)


def changed(post_dir: Path):
    """번역본이 마지막으로 커밋된 시점부터 지금(작업 트리 포함)까지 원문 index.md의 diff를 출력한다."""
    def git(*args):
        return subprocess.run(["git", *args], capture_output=True, text=True, check=False).stdout

    since = git("log", "-1", "--format=%H", "--", str(post_dir / "index.en.md")).strip()
    if not since:
        raise SystemExit("index.en.md가 커밋된 적이 없어 비교 기준이 없습니다. 원문 전체를 다시 확인하세요.")
    diff = git("diff", since, "--", str(post_dir / "index.md"))
    print(f"# 기준 커밋 {since[:8]} 이후 원문 변경\n")
    print(diff or "(원문 변경 없음: 지문만 다르면 index.en.md의 source가 손으로 바뀌었는지 확인)")


def main():
    if len(sys.argv) < 2:
        raise SystemExit(__doc__)
    command = sys.argv[1]
    if command == "hash":
        print(source_hash((Path(sys.argv[2]) / "index.md").read_text(encoding="utf-8")))
    elif command == "hash-text":
        print(source_hash(sys.stdin.read()))
    elif command == "stamp":
        stamp(Path(sys.argv[2]))
    elif command == "changed":
        changed(Path(sys.argv[2]))
    elif command == "status":
        root = Path(sys.argv[2] if len(sys.argv) > 2 else "content/posts")
        for post_dir in sorted((p for p in root.iterdir() if (p / "index.md").exists()), reverse=True):
            draft, state = status_of(post_dir)
            print(f"{state:<11} {'draft' if draft else 'published':<9} {post_dir.name}")
    else:
        raise SystemExit(__doc__)


if __name__ == "__main__":
    main()
