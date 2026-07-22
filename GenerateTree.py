"""
============================================================
📖 GenerateTree.py — 目录树生成器（Obsidian排序版）
============================================================

使用方法：
    python GenerateTree.py [项目根目录路径]

一键运行（Windows 批处理）：
    @echo off
    cd /d "%~dp0"
    python GenerateTree.py
    pause

依赖：
    pip install pathspec natsort

============================================================
"""

import sys
import warnings
import re
from pathlib import Path

try:
    import pathspec
except ImportError:
    sys.exit("错误：需要 pathspec 库，请运行：pip install pathspec")

try:
    from natsort import natsort_keygen
except ImportError:
    sys.exit("错误：需要 natsort 库，请运行：pip install natsort")

# ============================================================
# 配置区域
# ============================================================

EXTRA_IGNORES = [
    "/.git/",
    ".gitignore",
    "/.obsidian/",
    "GenerateTree.java",
    "GenerateTree.py",
    "一键运行Generate.java.bat",
    "一键运行Generate.py.bat",
]

DEFAULT_COLLAPSE_NAMES = {"assets","res","Python-100-Days-master"}
_COLLAPSE_NAMES_LOWER = {name.lower() for name in DEFAULT_COLLAPSE_NAMES}

COLLAPSE_DIRS = [
    # 例如: "build/output",
]

EXPAND_DEFAULT_COLLAPSE_DIRS = [
    # 例如: "docs/assets",
]

START_MARK = "<!-- TREE START -->"
END_MARK = "<!-- TREE END -->"

# ============================================================
# 排序器（Obsidian风格）
# ============================================================

nat_key = natsort_keygen()


def natural_key(path: Path):
    """
    Obsidian 风格排序：
    1. 文件夹优先
    2. 自然排序（数字按数值）
    3. 中文顺序更自然
    """
    return (not path.is_dir(), nat_key(path.name))


# ============================================================
# 工具函数
# ============================================================

def _is_filesystem_root(path: Path) -> bool:
    """
    可靠检测任意平台的文件系统根目录。
    """
    if path.parent == path:
        return True

    if hasattr(path, "drive") and path.drive:
        if str(path).rstrip("\\/") == path.drive.rstrip("\\/"):
            return True

    return False


def _parse_gitignore_lines(repo_root: Path) -> list[str]:
    """
    读取 .gitignore 并返回有效模式行。
    """
    patterns = []

    gf = repo_root / ".gitignore"

    if gf.exists():
        for raw in gf.read_text(encoding="utf-8").splitlines():
            stripped = raw.strip()

            if not stripped or stripped.startswith("#"):
                continue

            patterns.append(raw.strip())

    return patterns


def load_ignore_spec(repo_root: Path) -> pathspec.PathSpec:
    """
    构建完整忽略规则。
    """
    patterns = _parse_gitignore_lines(repo_root)
    patterns.extend(EXTRA_IGNORES)

    return pathspec.PathSpec.from_lines(
        "gitwildmatch",
        patterns
    )


def should_collapse(rel_path: Path) -> bool:
    """
    判断目录是否需要折叠。
    """

    if rel_path.name.lower() in _COLLAPSE_NAMES_LOWER:

        parts = rel_path.parts

        for ep in EXPAND_DEFAULT_COLLAPSE_DIRS:
            if parts == tuple(Path(ep).parts):
                return False

        return True

    parts = rel_path.parts

    for pattern in COLLAPSE_DIRS:

        if not pattern.strip():
            continue

        pattern_parts = tuple(Path(pattern).parts)

        if parts[:len(pattern_parts)] == pattern_parts:
            return True

    return False


def get_children(
    dir_path: Path,
    spec: pathspec.PathSpec,
    repo_root: Path
) -> list[Path]:
    """
    返回目录下未被忽略的子项。
    """

    children = []

    for entry in dir_path.iterdir():

        rel = entry.relative_to(repo_root).as_posix()

        match_path = rel + "/" if entry.is_dir() else rel

        if not spec.match_file(match_path):
            children.append(entry)

    # Obsidian 风格排序
    children.sort(key=natural_key)

    return children


def generate_tree(
    root: Path,
    spec: pathspec.PathSpec,
    repo_root: Path
) -> str:
    """
    返回完整目录树字符串。
    """

    def walk(node: Path, prefix: str, is_last: bool):

        connector = "└── " if is_last else "├── "

        line = prefix + connector + node.name

        if node.is_dir():

            line += "/"

            yield line

            if (
                should_collapse(node.relative_to(repo_root))
                or node.is_symlink()
            ):
                return

            children = get_children(node, spec, repo_root)

            child_prefix = prefix + (
                "    " if is_last else "│   "
            )

            for i, child in enumerate(children):

                yield from walk(
                    child,
                    child_prefix,
                    i == len(children) - 1
                )

        else:
            yield line

    root_display = root.name or root.anchor or "/"

    if not root_display.endswith("/"):
        root_display += "/"

    lines = [root_display]

    top_children = get_children(root, spec, root)

    for i, child in enumerate(top_children):

        lines.extend(
            walk(
                child,
                "",
                i == len(top_children) - 1
            )
        )

    return "\n".join(lines) + "\n"


def update_readme(repo_root: Path, tree_text: str) -> None:
    """
    将目录树插入 README.md
    """

    readme_path = repo_root / "README.md"

    lines = (
        readme_path.read_text(
            encoding="utf-8"
        ).splitlines()

        if readme_path.exists()

        else []
    )

    start_idx = end_idx = -1

    for i, line in enumerate(lines):

        s = line.strip()

        if s == START_MARK:
            start_idx = i

        if s == END_MARK:
            end_idx = i

    code_block = "```\n" + tree_text + "```"

    if start_idx >= 0 and end_idx > start_idx:

        lines[start_idx + 1:end_idx] = [code_block]

    else:

        lines += [
            "",
            START_MARK,
            code_block,
            END_MARK
        ]

    readme_path.write_text(
        "\n".join(lines) + "\n",
        encoding="utf-8"
    )


def main():

    target = sys.argv[1] if len(sys.argv) > 1 else "."

    root = Path(target).resolve()

    # 配置校验
    for ep in EXPAND_DEFAULT_COLLAPSE_DIRS:

        if len(Path(ep).parts) == 1:

            warnings.warn(
                f"展开路径 '{ep}' 只有一个层级。"
            )

    if _is_filesystem_root(root):

        print(
            "错误：不允许直接扫描系统根目录。",
            file=sys.stderr
        )

        sys.exit(1)

    try:

        if not root.exists():
            raise FileNotFoundError(f"路径不存在: {root}")

        if not root.is_dir():
            raise NotADirectoryError(f"不是目录: {root}")

        spec = load_ignore_spec(root)

        tree = generate_tree(root, spec, root)

        update_readme(root, tree)

        print("✅ 目录树已更新到 README.md")

    except FileNotFoundError as e:

        print(f"错误：{e}", file=sys.stderr)

        sys.exit(1)

    except PermissionError as e:

        print(f"错误：权限不足 - {e}", file=sys.stderr)

        sys.exit(1)

    except Exception as e:

        print(f"未知错误：{e}", file=sys.stderr)

        import traceback

        traceback.print_exc()

        sys.exit(1)


if __name__ == "__main__":
    main()