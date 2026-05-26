"""
============================================================
📖 GenerateTree.py — 目录树生成器（Python 版）
============================================================

使用方法：
    python GenerateTree.py [项目根目录路径]

    [项目根目录路径] 可选，默认为当前目录。
    程序会自动在指定根目录下寻找 README.md 并更新其中的目录树。

    示例：
        python GenerateTree.py /home/user/myproject
        python GenerateTree.py D:/programme/GitHub/Markdown

一键运行（Windows 批处理）：
    新建一个 .bat 文件（如 run.bat），内容如下：
        @echo off
        cd /d "%~dp0"
        python GenerateTree.py
        pause
    双击该文件即可扫描批处理所在目录。

自定义配置（修改下方常量即可）：
    EXTRA_IGNORES  - 额外忽略规则（支持 ! 取反）
    COLLAPSE_DIRS  - 不展开的文件夹（相对路径）
    START_MARK / END_MARK - README 中目录树的占位标记

README.md 要求：
    需包含以下两行标记（若不存在则自动追加到文件末尾）：
        <!-- TREE START -->
        <!-- TREE END -->
    生成的目录树会填充在二者之间。

============================================================
"""

import os
import re
import sys
from pathlib import Path

# ============================================================
# 🔧 自定义额外忽略的文件/目录（遵循 .gitignore 语法）
# ============================================================
# 注：取反规则会让匹配到的文件/目录"不被忽略"，从而在最终目录树中显示出来。
EXTRA_IGNORES = [
    "/.git/",       # 忽略仓库根目录下的 .git 文件夹
    # "*.log",      # 示例：忽略所有 .log 文件
    # "!/keep.log", # 示例：但不忽略 keep.log
    # "/build/",    # 示例：忽略根目录下的 build 目录
    ".gitignore",
    "/.obsidian/",
    "一键运行Generate.java.bat",
    "GenerateTree.java",
    "一键运行Generate.py.bat",
    "GenerateTree.py",  # 忽略自身
]

# ============================================================
# 📁 不想展开内部内容的文件夹（名称或相对路径，用 / 分隔）
# ============================================================
COLLAPSE_DIRS = [
    "assets",
    # "build/output",  # 示例：不展开 build/output 目录
]

# README.md 中目录树的占位标记
START_MARK = "<!-- TREE START -->"
END_MARK = "<!-- TREE END -->"


# ============================================================
# 忽略规则内部类
# ============================================================
class IgnoreRule:
    """将一条 .gitignore 规则编译为正则表达式，用于匹配路径。"""

    def __init__(self, pattern: str, dir_only: bool, negate: bool):
        """
        :param pattern: 去掉 ! 和尾斜杠后的原始模式字符串
        :param dir_only: 是否只匹配目录（原规则以 / 结尾）
        :param negate:  是否取反（原规则以 ! 开头）
        """
        self.dir_only = dir_only
        self.negate = negate
        self.regex = self._build_regex(pattern)

    @staticmethod
    def _build_regex(pattern: str) -> re.Pattern:
        """
        将 .gitignore 模式转换为正则表达式。

        转换规则：
            - 以 / 开头 → 锚定到根目录（^）
            - 不含 /  → 可匹配任意深度（^(.*/)?）
            - **       → 匹配任意多层目录（.*）
            - *        → 匹配不含 / 的任意字符（[^/]*）
            - ?        → 匹配单个非 / 字符（[^/]）
            - 其余字符 → 作为字面量匹配（用 re.escape 转义）
        """
        sb = []  # 用列表拼接正则表达式字符串

        has_slash = "/" in pattern
        if pattern.startswith("/"):
            # 以 / 开头的规则，锚定到根目录
            pattern = pattern[1:]
            sb.append("^")
        elif not has_slash:
            # 不含 / 的规则，可在任意深度匹配
            sb.append("^(.*/)?")
        else:
            # 包含 / 但不以 / 开头，也锚定到开头
            sb.append("^")

        # 按 ** 分割，** 在 .gitignore 中表示匹配任意多层目录
        parts = pattern.split("**")
        for i, part in enumerate(parts):
            if i > 0:
                sb.append(".*")  # ** 转换为正则的 .*

            # re.escape 将所有正则特殊字符转义为字面量
            escaped = re.escape(part)
            # 恢复 * 和 ? 的通配符含义（re.escape 会把它们也转义掉）
            escaped = escaped.replace(r"\*", "[^/]*").replace(r"\?", "[^/]")
            sb.append(escaped)

        sb.append("$")
        return re.compile("".join(sb))

    def matches(self, relative_path: str, is_dir: bool) -> bool:
        """
        判断路径是否匹配此规则。

        :param relative_path: 相对于根目录的路径（使用 / 分隔）
        :param is_dir:        该路径是否为目录
        :return:              是否匹配
        """
        if self.dir_only and not is_dir:
            return False
        return self.regex.match(relative_path) is not None


# ============================================================
# 移除行尾注释（保留转义的 \#）
# ============================================================
def remove_comment(line: str) -> str:
    """
    移除 # 及其之后的内容，但保留转义的 \\#。

    :param line: 原始行
    :return:     去掉注释后的内容
    """
    length = len(line)
    i = 0
    while i < length:
        if line[i] == "#":
            # 如果前一个字符不是反斜杠，则认为 # 是注释的开始
            if i == 0 or line[i - 1] != "\\":
                return line[:i]
        i += 1
    return line


# ============================================================
# 从一行文本中解析忽略规则
# ============================================================
def add_rule_from_line(rules: list, line: str) -> None:
    """
    解析一行 .gitignore 语法文本，生成 IgnoreRule 并添加到规则列表。

    :param rules: 规则列表（会被原地修改）
    :param line:  原始行文本
    """
    # 去掉注释，再去除首尾空白
    trimmed = remove_comment(line).strip()
    if not trimmed:
        return

    # 处理取反语法（! 开头）
    negate = False
    if trimmed.startswith("!"):
        negate = True
        trimmed = trimmed[1:].strip()
    if not trimmed:
        return

    # 处理目录语法（/ 结尾）
    dir_only = trimmed.endswith("/")
    if dir_only:
        trimmed = trimmed[:-1]

    rules.append(IgnoreRule(trimmed, dir_only, negate))


# ============================================================
# 加载所有忽略规则
# ============================================================
def load_ignore_rules(repo_root: Path) -> list:
    """
    从 .gitignore 文件和 EXTRA_IGNORES 常量加载忽略规则。

    :param repo_root: 项目根目录
    :return:          IgnoreRule 对象列表
    """
    rules = []

    # 读取 .gitignore 文件（如果存在）
    gitignore_file = repo_root / ".gitignore"
    if gitignore_file.exists():
        content = gitignore_file.read_text(encoding="utf-8")
        for line in content.splitlines():
            add_rule_from_line(rules, line)

    # 添加自定义的额外忽略规则
    for line in EXTRA_IGNORES:
        add_rule_from_line(rules, line)

    return rules


# ============================================================
# 判断路径是否应被忽略
# ============================================================
def is_ignored(absolute_path: Path, repo_root: Path, rules: list) -> bool:
    """
    根据所有忽略规则，判断给定路径是否应被忽略。

    多条规则按顺序匹配，后面的规则可以覆盖前面的（与 .gitignore 行为一致）。

    :param absolute_path: 待判断的绝对路径
    :param repo_root:     项目根目录
    :param rules:         忽略规则列表
    :return:              是否应忽略
    """
    try:
        # 如果路径不在根目录下，直接忽略
        if not absolute_path.is_relative_to(repo_root):
            return True
    except AttributeError:
        # Python 3.8 及以下没有 is_relative_to，手动判断
        try:
            absolute_path.relative_to(repo_root)
        except ValueError:
            return True

    # 计算相对路径，并将分隔符统一为 /
    relative = absolute_path.relative_to(repo_root)
    path_str = relative.as_posix()

    is_dir = absolute_path.is_dir()
    ignored = False

    for rule in rules:
        if rule.matches(path_str, is_dir):
            # 如果是取反规则，则取消忽略；否则标记为忽略
            ignored = not rule.negate

    return ignored


# ============================================================
# 判断目录是否应折叠（不展开内部）
# ============================================================
def should_collapse(dir_path: Path, repo_root: Path) -> bool:
    """
    检查目录是否在 COLLAPSE_DIRS 列表中。

    :param dir_path:  目录的绝对路径
    :param repo_root: 项目根目录
    :return:          是否应折叠
    """
    relative = dir_path.relative_to(repo_root)
    for pattern in COLLAPSE_DIRS:
        # 将 pattern 转为 Path 对象，用 endswith 逐段匹配，避免字符串包含误判
        if relative == Path(pattern) or relative.as_posix().endswith("/" + pattern):
            return True
    return False


# ============================================================
# 获取排序后的子项列表（目录优先，文件按字母排序）
# ============================================================
def get_sorted_children(dir_path: Path, repo_root: Path, rules: list) -> list:
    """
    获取目录下的直接子项，过滤忽略项后排序返回。

    排序规则：目录排在文件前面，同类按名称不区分大小写排序。

    :param dir_path:  目录路径
    :param repo_root: 项目根目录
    :param rules:     忽略规则列表
    :return:          排序后的 Path 列表
    """
    children = []
    for entry in dir_path.iterdir():
        # 过滤掉被忽略的文件/目录
        if not is_ignored(entry, repo_root, rules):
            children.append(entry)

    def sort_key(p: Path):
        # 目录优先：目录排 0，文件排 1
        is_dir = 0 if p.is_dir() else 1
        # 名称不区分大小写排序
        name = p.name.lower()
        return (is_dir, name)

    children.sort(key=sort_key)
    return children


# ============================================================
# 递归生成目录树字符串
# ============================================================
def append_tree(node: Path, prefix: str, is_last: bool, repo_root: Path,
                rules: list, collapse_dirs: list) -> str:
    """
    递归地将一个文件/目录添加到目录树字符串中。

    :param node:         当前节点路径
    :param prefix:       当前行前面的缩进前缀（如 "│   " 或 "    "）
    :param is_last:      是否为同级中的最后一项
    :param repo_root:    项目根目录
    :param rules:        忽略规则列表
    :param collapse_dirs: 折叠目录列表（保留参数兼容性，实际使用模块级常量）
    :return:             生成的目录树文本片段
    """
    if is_ignored(node, repo_root, rules):
        return ""

    # 选择连接符：最后一项用 └──，否则用 ├──
    connector = "└── " if is_last else "├── "
    line = prefix + connector + node.name

    if node.is_dir():
        line += "/"
        result = line + "\n"

        # 如果是被设置为"不展开"的目录，则停止递归
        if should_collapse(node, repo_root):
            return result

        # 跳过符号链接：符号链接可能指向祖先目录，递归进去会造成无限循环
        if node.is_symlink():
            return result

        # 递归处理子项
        children = get_sorted_children(node, repo_root, rules)
        # 下一层的前缀：最后一项后面加 4 个空格，否则加 "│   "
        child_prefix = prefix + ("    " if is_last else "│   ")
        for i, child in enumerate(children):
            child_is_last = (i == len(children) - 1)
            result += append_tree(child, child_prefix, child_is_last, repo_root, rules, collapse_dirs)

        return result
    else:
        return line + "\n"


# ============================================================
# 更新 README.md
# ============================================================
def update_readme(repo_root: Path, tree_text: str) -> None:
    """
    将目录树写入 README.md 中 START_MARK 和 END_MARK 之间的位置。

    如果标记不存在，自动追加到文件末尾。

    :param repo_root: 项目根目录
    :param tree_text: 生成的目录树文本
    """
    readme_path = repo_root / "README.md"

    # 读取现有内容（文件不存在则从空列表开始）
    if readme_path.exists():
        lines = readme_path.read_text(encoding="utf-8").splitlines()
    else:
        lines = []

    # 查找标记位置
    start_idx = -1
    end_idx = -1
    for i, line in enumerate(lines):
        if line.strip() == START_MARK:
            start_idx = i
        if line.strip() == END_MARK:
            end_idx = i

    # 构造替换内容：用代码块包裹目录树
    code_block = "```\n" + tree_text + "```"

    if start_idx >= 0 and end_idx > start_idx:
        # 找到标记对，替换中间内容
        # 先删除旧的中间内容（start_idx+1 到 end_idx-1）
        lines[start_idx + 1:end_idx] = [code_block]
    else:
        # 未找到标记，追加到末尾
        lines.append("")
        lines.append(START_MARK)
        lines.append(code_block)
        lines.append(END_MARK)

    # 强制 UTF-8 写入，杜绝乱码
    readme_path.write_text("\n".join(lines) + "\n", encoding="utf-8")


# ============================================================
# 主函数
# ============================================================
def main():
    """主入口：解析参数，生成目录树，更新 README.md。"""
    # 解析命令行参数：有参数用参数路径，无参数用当前目录
    # os.path.abspath 会把相对路径转为绝对路径
    # os.path.normpath 会清理路径中的冗余部分（如 ./ 和 ../）
    target = sys.argv[1] if len(sys.argv) > 1 else "."
    root = Path(target).resolve()  # resolve() 同时完成绝对路径转换和规范化

    try:
        # 加载忽略规则
        rules = load_ignore_rules(root)

        # 构建目录树（直接用字符串拼接，append_tree 返回的每行已自带 \n）
        tree = root.name + "/\n"
        children = get_sorted_children(root, root, rules)
        for i, child in enumerate(children):
            is_last = (i == len(children) - 1)
            tree += append_tree(child, "", is_last, root, rules, COLLAPSE_DIRS)

        # 更新 README.md
        update_readme(root, tree)
        print("目录结构已更新到 README.md")

    except Exception as e:
        print(f"错误：生成目录树失败 - {e}", file=sys.stderr)
        import traceback
        traceback.print_exc()


if __name__ == "__main__":
    main()
