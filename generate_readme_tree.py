#!/usr/bin/env python3
"""
自动生成 Markdown 仓库的目录结构树，并更新 README.md。
只遍历一级目录和文件，不深入子目录内部（保持简洁）。

使用方法：
    cd D:/programme/GitHub/Markdown
    python generate_readme_tree.py
"""

import os
import re
from pathlib import Path

# 要排除的文件/目录
EXCLUDE = {'assets', '.git', '.DS_Store', 'README.md', 'generate_readme_tree.py',
           '.obsidian', '.gitignore', 'Thumbs.db'}

def get_tree_lines(root: Path) -> list[str]:
    """生成目录树的文本行（不含根目录名）"""
    entries = sorted(
        [p for p in root.iterdir() if p.name not in EXCLUDE],
        key=lambda p: (not p.is_dir(), p.name.lower())
    )
    lines = []
    for i, p in enumerate(entries):
        is_last = (i == len(entries) - 1)
        prefix = '└── ' if is_last else '├── '
        label = p.name + '/' if p.is_dir() else p.name
        lines.append(prefix + label)
    return lines

def main():
    root = Path(__file__).parent
    readme_path = root / 'README.md'

    tree_lines = get_tree_lines(root)
    tree_block = '```text\nMarkdown/\n' + '\n'.join(tree_lines) + '\n```'

    if readme_path.exists():
        content = readme_path.read_text(encoding='utf-8')
        # 替换 ## 目录结构 到下一个 ## 或 --- 之间的内容
        pattern = r'(## 目录结构\s*\n)```text[\s\S]*?```'
        replacement = r'\1' + tree_block
        new_content = re.sub(pattern, replacement, content)
        readme_path.write_text(new_content, encoding='utf-8')
        print('README.md 已更新目录结构。')
    else:
        print('未找到 README.md，请先创建。')

if __name__ == '__main__':
    main()
