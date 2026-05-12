# .gitignore使用指南

`.gitignore` 文件本质上是一个“黑名单”，用于告诉 Git 忽略特定的文件和目录，使其不被提交到代码仓库中。

## 1.语法

---

- 每一行代表一个忽略的文件或目录规则。
- `#` 号开头的行是注释。
- 可以使用通配符 `*`、`?` 和 `[]` 进行模式匹配。
- `!` 号开头表示“例外”，即强制跟踪某些文件，即使它们被前面的规则忽略。

---
## 2.规则

- 忽略任何层级同名文件
> config.json
> secret.key

- 忽略任何层级同名目录
> logs/
> temp/

- 忽略仓库根目录下的改文件或目录
> /logs/
> /TODO.txt

- 忽略某种类型的文件
> \*.log
> \*.tmp
> \*.swp

- 忽略某个目录中的所有文件，当保留部分文件
> logs/\*
> !logs/important.log

- 忽略所有`node_modules`目录（递归）
> node_modules/

- 忽略某个特定路径的文件，但允许其他路径的相同文件
> /config/\*.json    # 忽略config目录下的所有.json文件
> !/config/keep.json    # 当保留keep.json


