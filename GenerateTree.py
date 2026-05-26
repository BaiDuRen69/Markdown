"""
============================================================
📖 GenerateTree.py — 目录树生成器（已彻底优化）
============================================================

使用方法：
    python GenerateTree.py [项目根目录路径]

    [项目根目录路径] 可选，默认为当前目录。
    程序会自动在指定根目录下寻找 README.md 并更新其中的目录树。

一键运行（Windows 批处理）：
    新建 .bat 文件，内容如下：
        @echo off
        cd /d "%~dp0"
        python GenerateTree.py
        pause

依赖：
    pip install pathspec

自定义配置：
    EXTRA_IGNORES  - 额外忽略规则（.gitignore 语法）
    COLLAPSE_DIRS  - 不展开的文件夹（相对于项目根的路径片段）
    START_MARK / END_MARK - README 中目录树的占位标记

============================================================
"""

import os
import sys
from pathlib import Path

try:
    import pathspec
except ImportError:
    sys.exit(
        "错误：需要 pathspec 库，请运行：pip install pathspec"
    )

# ============================================================
# 配置区域
# ============================================================
EXTRA_IGNORES = [
    "/.git/",
    ".gitignore",
    "/.obsidian/",
    "GenerateTree.py",
]

# 折叠目录：传入相对路径的各个部分（自动处理跨平台）
# 例如 ["assets", "build/output"] 表示不展开 assets 和 build/output
COLLAPSE_DIRS = [
    "assets",
]

START_MARK = "<!-- TREE START -->"
END_MARK = "<!-- TREE END -->"

# ============================================================
# 辅助函数
# ============================================================

def _parse_gitignore_lines(repo_root: Path) -> list[str]:
    """读取 .gitignore 并返回去除注释和空行的有效模式列表。"""
    patterns = []
    gf = repo_root / ".gitignore"
    if gf.exists():
        for raw in gf.read_text(encoding="utf-8").splitlines():
            line = raw.strip()
            if not line or line.startswith("#"):
                continue
            # 去除行内注释 (简单处理：找到第一个 # 且不在转义后)
            # .gitignore 规范没有转义注释，所以直接去掉 # 及之后即可
            comment_pos = line.find("#")
            if comment_pos != -1:
                line = line[:comment_pos].strip()
            if line:
                patterns.append(line)
    return patterns


def load_ignore_spec(repo_root: Path) -> pathspec.PathSpec:
    """构建完整的忽略规则集合。"""
    patterns = _parse_gitignore_lines(repo_root)
    patterns.extend(EXTRA_IGNORES)
    return pathspec.PathSpec.from_lines("gitwildmatch", patterns)


def should_collapse(rel_path: Path) -> bool:
    """
    用路径的各个部分精确匹配 COLLAPSE_DIRS。
    COLLAPSE_DIRS 中的每一项会被按 '/' 拆分成序列，与 rel_path.parts 的前缀比较。
    """
    parts = rel_path.parts
    for pattern in COLLAPSE_DIRS:
        pattern_parts = tuple(Path(pattern).parts)
        if parts[:len(pattern_parts)] == pattern_parts:
            return True
    return False


def get_children(dir_path: Path, spec: pathspec.PathSpec, repo_root: Path) -> list[Path]:
    """返回目录下未被忽略的子项，目录优先、按名称排序。"""
    children = []
    for entry in dir_path.iterdir():
        rel = entry.relative_to(repo_root).as_posix()
        # pathspec 要求目录以 '/' 结尾
        match_path = rel + "/" if entry.is_dir() else rel
        if not spec.match_file(match_path):
            children.append(entry)
    children.sort(key=lambda p: (not p.is_dir(), p.name.lower()))
    return children


def generate_tree(root: Path, spec: pathspec.PathSpec, repo_root: Path) -> str:
    """返回完整目录树字符串（使用生成器避免大量临时字符串拼接）。"""
    # 内部递归生成器
    def walk(node: Path, prefix: str, is_last: bool):
        connector = "└── " if is_last else "├── "
        line = prefix + connector + node.name
        if node.is_dir():
            line += "/"
            yield line
            if should_collapse(node.relative_to(repo_root)) or node.is_symlink():
                return
            children = get_children(node, spec, repo_root)
            child_prefix = prefix + ("    " if is_last else "│   ")
            for i, child in enumerate(children):
                yield from walk(child, child_prefix, i == len(children) - 1)
        else:
            yield line

    lines = [root.name + "/"]
    top_children = get_children(root, spec, repo_root)
    for i, child in enumerate(top_children):
        lines.extend(walk(child, "", i == len(top_children) - 1))
    return "\n".join(lines) + "\n"


def update_readme(repo_root: Path, tree_text: str) -> None:
    """将目录树插入或替换到 README.md 的标记之间。"""
    readme_path = repo_root / "README.md"
    if readme_path.exists():
        lines = readme_path.read_text(encoding="utf-8").splitlines()
    else:
        lines = []

    start_idx = end_idx = -1
    for i, line in enumerate(lines):
        if line.strip() == START_MARK:
            start_idx = i
        elif line.strip() == END_MARK:
            end_idx = i

    code_block = "```\n" + tree_text + "```"
    if start_idx >= 0 and end_idx > start_idx:
        lines[start_idx + 1:end_idx] = [code_block]
    else:
        lines += ["", START_MARK, code_block, END_MARK]

    readme_path.write_text("\n".join(lines) + "\n", encoding="utf-8")


def main():
    target = sys.argv[1] if len(sys.argv) > 1 else "."
    root = Path(target).resolve()

    # 分层错误处理
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