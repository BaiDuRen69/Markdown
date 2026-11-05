# Git 速查与复习

面向已掌握 Git 基本用法、需要按需回顾命令的读者。按查阅场景组织，只保留命令、选项、易混点与易错点。官方手册：[Git User Manual](https://git-scm.com/docs/user-manual)。

**约定**

- `<file>`、`<branch>`、`<hash>` 等为占位符，实际输入不带尖括号。
- 代码块统一为 `bash`（历史示意图用 `text`），行尾 `#` 后为注释。
- 术语统一：工作区、暂存区、本地仓库、远程跟踪分支（如 `origin/main`）、提交、上游（upstream）。
- 新脚本优先用 `switch` / `restore`；`checkout` 旧写法仅在必要时列出。
- `> **注意**：` 标记易错点，全文统一使用这一格式。

**目录**：[查看状态与历史](#查看状态与历史) · [记录更新](#记录更新) · [撤销与回退](#撤销与回退) · [远程仓库](#远程仓库) · [标签](#标签) · [分支与合并](#分支与合并) · [暂存现场与挑选提交](#暂存现场与挑选提交) · [变基](#变基) · [配置与别名](#配置与别名)

---

## 查看状态与历史

### 状态与差异

```bash
git status            # 详细状态
git status -s         # 简洁状态
```

| 命令 | 比较对象 |
| --- | --- |
| `git diff` | 工作区 ↔ 暂存区（未暂存的修改） |
| `git diff --staged` / `git diff --cached` | 暂存区 ↔ `HEAD`（下次提交的内容） |
| `git diff HEAD` | 工作区 ↔ `HEAD`（含已暂存部分） |
| `git diff <hash1> <hash2>` | 两次提交之间 |

> **注意**：`git status -s` 输出**两列**状态码，左列 = 暂存区 ↔ `HEAD`，右列 = 工作区 ↔ 暂存区。`M ` 表示已暂存、` M` 表示改了但未暂存、`MM` 表示暂存后又改过、`??` 表示未跟踪。看错列最容易误判改动去向。

### 提交日志

```bash
git log --oneline --graph --decorate -10   # 常用：图形、单行、近 10 条
git log -p -2                             # 近两次提交，按补丁显示差异
git log --stat                            # 每次提交的文件增删统计
git log --pretty=format:"%h - %an, %ar : %s"
```

| `--pretty` 格式 | 输出 |
| --- | --- |
| `oneline` | **完整** 40 位哈希 + 提交标题 |
| `short` / `full` / `fuller` | 依次增加作者、提交者、作者日期与提交日期 |
| `format:"..."` | 自定义占位符格式 |

> **注意**：`--pretty=oneline` 是完整哈希；`--oneline` 才是「短哈希 + 标题」，等价于 `--pretty=oneline --abbrev-commit`。

**常用显示选项**

| 选项 | 说明 |
| --- | --- |
| `-p` / `--patch` | 按补丁格式显示每次提交的差异 |
| `--stat` | 文件修改统计（增删行数） |
| `--shortstat` | 只保留 `--stat` 最后的总计行 |
| `--name-only` | 只列改动文件名 |
| `--name-status` | 列出文件名及新增/修改/删除状态 |
| `--abbrev-commit` | 只显示哈希前几位 |
| `--relative-date` | 相对时间（`2 weeks ago`） |
| `--graph` | ASCII 图显示分支与合并历史 |
| `--oneline` | `--pretty=oneline --abbrev-commit` 的简写 |
| `--all` | 包含所有引用可达的提交（本地、远程跟踪分支、标签等） |

**`format` 常用占位符**

| 占位符 | 说明 | 占位符 | 说明 |
| --- | --- | --- | --- |
| `%H` / `%h` | 提交完整 / 简写哈希 | `%T` / `%t` | 树完整 / 简写哈希 |
| `%P` / `%p` | 父提交完整 / 简写哈希 | `%s` | 提交说明 |
| `%an` / `%ae` | 作者名字 / 邮箱 | `%cn` / `%ce` | 提交者名字 / 邮箱 |
| `%ad` | 作者日期（配合 `--date=` 定制格式） | `%ar` | 作者日期，相对时间 |
| `%cd` | 提交日期 | `%cr` | 提交日期，相对时间 |

> **注意**：作者（author）是实际改代码的人，提交者（committer）是最后把工作成果写入仓库的人。`cherry-pick`、`rebase`、通过补丁应用都会让两者不同。

**过滤输出**

```bash
git log -3                                              # 最近 3 条，写作 -<n>
git log --since="1 week ago" --until="now"
git log --author="me" --grep="fix"
git log --all-match --grep="fix" --grep="login"         # 两个关键字都要匹配
git log -S function_name -- src/                       # 添加或删除过该字符串的提交
git log --oneline -- README.md                         # 只查某个文件/目录
```

| 选项 | 说明 |
| --- | --- |
| `-<n>` | 仅显示最近 n 条提交 |
| `--since` / `--after` | 指定时间之后 |
| `--until` / `--before` | 指定时间之前 |
| `--author` / `--committer` | 按作者 / 提交者匹配 |
| `--grep` | 提交说明中包含指定字符串 |
| `--all-match` | 多个 `--grep` 需**全部**匹配（默认匹配任意） |
| `-S <string>` | 添加或删除该字符串的提交 |
| `-- <path>` | 只显示指定文件/目录的提交 |

> **注意**：`-- <path>` 必须放在命令**最后**，否则其后的选项会被当成路径。默认多个 `--author` / `--grep` 条件是「匹配任意一个」，加 `--all-match` 才要求全部 `--grep` 命中。

---

## 记录更新

### 暂存与提交

```bash
git add <file>            # 暂存指定文件或目录（目录递归）
git add .                 # 暂存当前目录及子目录的全部变更
git add -A                # 暂存整个仓库的全部变更（含删除）
git add -p                # 交互式按块暂存
git commit -m "说明"
git commit                # 打开 core.editor 写提交信息
git commit -a -m "说明"   # 跳过 git add，提交已跟踪文件的修改与删除
```

> **注意**：`git commit -a` **不包含未跟踪的新文件**，必须先 `git add`。`git add .` 的作用范围是当前目录；在仓库子目录下执行时，其他路径的变更要用 `git add -A` 才会包含。

### 忽略文件

详见 [gitignore 极简指南](gitignore极简指南.md)。模板集合：[github/gitignore](https://github.com/github/gitignore)。

| 写法 | 作用 |
| --- | --- |
| 空行、`# 注释` | 不参与匹配 |
| `*.log` | 标准 glob，递归应用于整个工作区 |
| `/build` | 开头 `/` 只匹配仓库根目录，不递归 |
| `dist/` | 结尾 `/` 指定目录 |
| `!important.log` | 取反，不忽略（父目录未被忽略时才有效） |
| `a/**/z` | `**` 匹配任意中间目录 |

glob 语法：`*` 匹配零个或多个字符，`?` 匹配单个字符，`[abc]` / `[0-9]` 匹配字符集合与范围。

> **注意**：`.gitignore` **只对未跟踪文件生效**。已被跟踪的文件要先 `git rm --cached <file>` 并提交，再补忽略规则。父目录整体被忽略时，单用 `!` 无法「救回」目录内的文件。

### 移除与重命名

```bash
git rm <file>              # 删除磁盘文件并暂存删除
git rm -f <file>           # 文件已修改或已暂存时强制删除
git rm --cached <file>     # 只从仓库/暂存区移除，保留磁盘文件
git mv <old> <new>         # 重命名或移动
```

`git rm` 后可接目录或 glob 模式。`git mv a b` 等价于 `mv a b` + `git rm a` + `git add b`；Git 不记录「重命名」这个动作，而是按内容相似度在查看时识别。

---

## 撤销与回退

### 分场景速查

| 场景 | 命令 |
| --- | --- |
| 改写最后一次提交 | `git commit --amend --no-edit` 或 `--amend -m "新说明"` |
| 取消暂存（保留修改） | `git restore --staged <file>`（旧：`git reset HEAD <file>`） |
| 丢弃工作区修改 | `git restore <file>`（旧：`git checkout -- <file>`） |
| 回退提交、保留改动 | `git reset --soft <hash>` / `git reset --mixed <hash>` |
| 彻底回退到某提交 | `git reset --hard <hash>` |
| 找回被丢弃的提交 | `git reflog` → `git reset --hard <hash>` |
| 撤销已推送的提交 | `git revert <hash>` |
| 放弃进行中的合并 / 变基 | `git merge --abort` / `git rebase --abort` |

### 修正最后一次提交

```bash
git commit --amend                  # 打开编辑器重写提交信息
git commit --amend -m "新说明"
git commit --amend --no-edit        # 只把暂存区内容补进去，沿用原信息
```

> **注意**：`--amend` 会用**新提交替换**原提交，哈希改变。已推送到共享分支的提交不要 amend，否则只能强推。

### 撤销工作区与暂存区

```bash
git restore --staged <file>    # 取消暂存，改动仍在工作区
git restore <file>             # 丢弃工作区修改，回到暂存区/HEAD 状态
git restore --source=HEAD --staged --worktree <file>   # 两处一起回退到 HEAD
```

> **注意**：`git restore <file>` 丢弃的未提交修改**无法从 Git 找回**。`git reset HEAD <file>` 与 `git checkout -- <file>` 是旧写法，新脚本统一用 `restore` / `switch`，避免 `checkout` 语义过载。

### 三种回退模式

`HEAD` 是当前提交，`HEAD^` / `HEAD~1` 是上一版，`HEAD~n` 是往前第 n 版，也可直接写提交哈希。

| 模式 | `HEAD` | 暂存区 | 工作区 | 典型用途 |
| --- | --- | --- | --- | --- |
| `--soft` | 移动 | 保留 | 保留 | 重做提交信息、把多个提交压成一个 |
| `--mixed`（默认） | 移动 | 重置 | 保留 | 重新挑选要暂存的内容 |
| `--hard` | 移动 | 重置 | 重置 | 彻底丢弃改动，**会丢代码** |

```bash
git reset --soft HEAD^
git reset --hard HEAD~3
git reset --hard a1b2c3d
```

### 找回丢失的提交

```bash
git reflog                            # 查看 HEAD 的全部移动记录
git switch -c rescue <hash>           # 先建分支确认内容，比直接 reset 安全
git reset --hard <hash>               # 确认后再把当前分支移回去
```

> **注意**：`reflog` 只记录**本地** HEAD 变动，有保留期限（默认约 90 天），不会同步到远程；未提交过的工作区修改不在其中。

### 撤销已推送的提交

```bash
git revert <hash>                  # 生成反向提交，抵消该提交
git revert HEAD                    # 撤销最近一次
git revert HEAD~3..HEAD            # 撤销最近 3 次，区间左开右闭
git revert --no-commit <hash>      # 只应用改动，手动合并成一次提交
```

> **注意**：共享分支不要用 `reset --hard` + 强推改写历史，会破坏他人基于旧历史的提交。`revert` 追加新提交，历史只增不改，可安全推送。

---

## 远程仓库

### 仓库获取与连接

```bash
git init                       # 当前目录初始化仓库
git clone <url>                # 克隆，目录名默认取仓库名
git clone <url> <dir>          # 自定义本地目录名
```

| 协议 | 格式 | 适用场景 |
| --- | --- | --- |
| HTTPS | `https://<host>/<owner>/<repo>.git` | 通用，能穿透代理/防火墙，需凭证 |
| SSH | `git@<host>:<owner>/<repo>.git` | 已配置密钥，免每次输入凭证 |

```bash
ssh-keygen -t ed25519 -C "me@example.com"   # 生成密钥对
ssh -T git@github.com                       # 验证连接
```

`~/.ssh/id_ed25519` 是私钥（绝不外泄）；`~/.ssh/id_ed25519.pub` 是公钥，把内容添加到平台的 SSH Keys。

### 管理远程

```bash
git remote -v                          # 列出远程简写与读写 URL
git remote show origin                 # 分支、跟踪关系、推送拉取配置
git remote add <name> <url>
git remote set-url origin <new-url>    # 换地址（HTTPS ↔ SSH）
git remote rename <old> <new>
git remote remove <name>               # 或 git remote rm
```

### 获取与推送

```bash
git fetch origin               # 只更新 origin/* 远程跟踪分支，不动本地分支与工作区
git pull                       # = fetch + merge 到当前分支的上游
git pull --rebase              # = fetch + rebase，避免无意义的 merge commit
git push                       # 推送当前分支到上游
git push -u origin dev         # 首次推送并建立上游（-u = --set-upstream）
git push origin main
git push --tags                # 推送全部标签
git push origin v1.0.0         # 推送单个标签
git push --force-with-lease    # 强推，但远程被他人更新过时拒绝
```

> **注意**：`fetch` 只更新 `origin/*`，想先看差异再动手，用 `git fetch` → `git log main..origin/main` → `merge`/`rebase`；`pull` 会直接改动当前分支。`git push` 默认**不推送标签**。强推一律用 `--force-with-lease`，不要用无条件覆盖的 `--force`。

---

## 标签

### 创建与查看

```bash
git tag                              # 列出标签
git tag -l "v1.8.5*"                 # 按模式筛选
git tag -a v1.4 -m "version 1.4"     # 附注标签（推荐）
git tag v1.4-lw                      # 轻量标签
git tag -a v1.2 9fceb02d             # 给历史提交补打标签
git show v1.4                        # 查看标签与对应提交
```

| 类型 | 本质 | 元数据 |
| --- | --- | --- |
| 附注标签 `-a` | 独立的 Git 对象 | 打标签者、邮箱、日期、说明，可签名 |
| 轻量标签 | 指向某个提交的引用 | 无 |

### 推送与删除

```bash
git push origin v1.4                       # 推送单个标签
git push origin --tags                     # 推送全部标签
git tag -d v1.4-lw                         # 删除本地标签
git push origin --delete v1.4              # 删除远程标签
git push origin :refs/tags/v1.4-lw         # 等价的旧写法
```

### 检出标签

```bash
git switch --detach v1.0.0                 # 检出标签
git switch -c hotfix-v1.0.0 v1.0.0         # 基于标签建分支后再改代码
```

> **注意**：检出标签会进入 **detached HEAD**，此时新提交不属于任何分支，切走后容易丢失；要改代码先建分支。标签不会随普通 `git push` 上传，删除远程标签也不会自动同步到别人的本地。

---

## 分支与合并

### 创建、查看、切换、删除

```bash
git branch                    # 本地分支，* 标记当前分支
git branch -a                 # 本地 + 远程跟踪分支
git branch -v                 # 各分支最新提交
git branch --merged           # 已合并进当前分支的分支（可安全删除）
git branch -vv                # 本地分支与上游的对应关系

git branch dev                # 创建但不切换
git switch dev                # 切换
git switch -                  # 切回上一个分支
git switch -c dev             # 创建并切换（推荐）
git switch -c hotfix v1.2.0   # 基于标签
git switch -c fix a1b2c3d     # 基于某个提交
git switch -c fix main~3      # 基于 main 往前第 3 个提交

git branch -d dev             # 删除已合并分支
git branch -D dev             # 强制删除未合并分支
git push origin --delete dev  # 删除远程分支
```

```bash
git fetch origin
git switch dev                  # 存在唯一同名远程分支时自动创建跟踪分支
git switch -c dev origin/dev    # 显式基于远程跟踪分支创建
git push -u origin dev          # 推送并建立上游
```

### 合并与快进

```bash
git switch main
git merge dev                             # 把 dev 合入当前分支 main
git merge --no-ff -m "merge dev" dev      # 强制生成合并提交
git merge --abort                         # 冲突中放弃合并
```

当 `main` 的 `HEAD` 是 `dev` 的祖先时，Git 默认 **Fast-forward**：只把 `main` 指针前移到 `dev`，不产生新提交；删掉分支后历史上看不出曾经分过叉。`--no-ff` 强制生成合并提交，保留分支信息。

| 方式 | 历史形态 | 适用 |
| --- | --- | --- |
| 默认（可快进时） | 线性，无合并提交 | 短命分支、无需留痕 |
| `--no-ff` | 保留分叉与合并点 | 需要体现功能分支的合并 |

### 合并冲突

两边修改了**同一文件的同一区域**时产生冲突，文件内出现 `<<<<<<< HEAD`、`=======`、`>>>>>>> dev` 标记。

```bash
git status                     # 定位冲突文件（both modified）
# 手动编辑：删除标记，保留正确内容
git add <file>                 # 标记为已解决
git commit                     # 完成合并，提交信息已预填
git merge --abort              # 放弃合并，回到合并前状态
```

```bash
git diff                                    # 查看冲突差异
git checkout --ours <file>                  # 冲突处取当前分支版本
git checkout --theirs <file>                # 冲突处取对方版本
git log --graph --oneline --decorate --all  # 查看分支合并图
```

> **注意**：`--ours` / `--theirs` 在 **merge** 中指「当前分支 / 被合并分支」，在 **rebase** 中含义**相反**（ours = 变基到的基底，theirs = 被重放的提交）。这是最常见的踩坑点之一。

---

## 暂存现场与挑选提交

### 暂存现场

```bash
git stash push -m "说明"       # 保存工作区 + 暂存区的修改
git stash push -u -m "说明"    # 同时保存未跟踪文件（-a 连忽略文件一起）
git stash list                 # stash@{0} 为最新
git stash show -p stash@{0}    # 查看某次暂存的差异
git stash apply stash@{1}      # 恢复但保留记录
git stash pop                  # 恢复最近一次并删除记录
git stash apply --index        # 恢复时一并还原「原本已暂存」的状态
git stash drop stash@{0}       # 删除某次记录
git stash clear                # 清空全部
```

> **注意**：默认 `git stash` **不含未跟踪文件**；`apply` / `pop` 默认把改动全部放回工作区，需要 `--index` 才恢复原暂存状态。`pop` 若遇冲突**不会删除** stash 记录，需手动 `drop`。

**标准流程**（切分支前、`pull` 前、临时修 bug 都适用）：

```bash
git stash push -m "功能开发中"
git switch main && git pull
git switch -c bugfix/xxx
# 修复并提交后
git switch feature/xxx
git stash pop
```

### 挑选提交

```bash
git cherry-pick <hash>                 # 复制某提交的改动到当前分支，生成新提交
git cherry-pick <hash1>..<hash3>       # 批量，区间左开右闭
git cherry-pick --no-commit <hash>     # 只应用改动，不自动提交
git cherry-pick --abort                # 冲突后放弃
```

> **注意**：`cherry-pick` 生成的是**新提交**（父提交与哈希都不同），适合把单个 bugfix 同步到另一分支，而不是合并整条分支。

---

## 变基

### 基本用法

`merge` 保留分叉再合并；`rebase` 把当前分支独有的提交**逐个重放**到新基底之后，形成线性历史。重放生成新哈希，原提交被弃用。

```text
      C---D  feature
     /
A---B---E---F  main
```

在 `feature` 上执行 `git rebase main` 后：

```text
A---B---E---F---C'---D'  feature
```

```bash
git switch feature
git rebase main                 # 把 feature 独有提交重放到 main 最新提交之后
git rebase main feature         # 同上，无需先切换分支
git pull --rebase               # = fetch + rebase，替代默认 fetch + merge
git rebase --continue           # 解决冲突并 add 后继续
git rebase --abort              # 放弃，回到变基前
```

> **注意**：**不要对已推送、他人正在基于其工作的分支做 rebase**。改写历史会让所有人的本地分支与远程分叉，最后只能靠强推收场。`rebase` 只用于整理本地尚未推送的提交。

### 交互式变基

```bash
git rebase -i HEAD~3       # 整理 HEAD~3 之后的 3 个提交（基底本身不含）
```

编辑器中把 `pick` 改成其他指令：

```text
pick   a1b2c3d 添加登录接口
fixup  e4f5g6h 修复拼写
fixup  i7j8k9l 补充测试
```

| 指令 | 作用 |
| --- | --- |
| `pick` | 保留该提交 |
| `reword` | 保留内容，改写提交信息 |
| `edit` | 暂停，允许修改提交内容（改完 `--continue`） |
| `squash` | 并入前一个提交，并编辑合并后的信息 |
| `fixup` | 并入前一个提交，丢弃当前提交信息 |
| `drop` | 删除该提交 |
| `exec` | 执行一条 shell 命令 |

> **注意**：交互式中列表顺序是「从旧到新」，`squash` / `fixup` 总是合并到**它上面**那个提交。冲突解决后用 `git add` + `git rebase --continue`，中途可用 `git rebase --abort` 全身而退。

---

## 配置与别名

### 配置

```bash
git config --global user.name "John Doe"
git config --global user.email johndoe@example.com
git config --global core.editor "code --wait"     # 需输入文本时自动打开的编辑器
git config --global color.ui auto
git config --global pull.rebase true              # 让 git pull 默认走 rebase
git config --list --show-origin                  # 查看全部配置及其来源文件
```

> **注意**：配置优先级为 `--local`（当前仓库）> `--global`（当前用户）> `--system`；同名配置被覆盖时，用 `--show-origin` 定位来自哪个文件。

### 别名

```bash
git config --global alias.co switch
git config --global alias.br branch
git config --global alias.ci commit
git config --global alias.unstage 'restore --staged --'
git config --global alias.lg 'log --oneline --graph --decorate -10'
```

> **注意**：别名只是把 `git` 后的第一个参数做简单替换。以 `!` 开头则整串交给 shell 执行，可组合多条命令、调用外部程序：

```bash
git config --global alias.sync '!git fetch --all && git pull --rebase'
git sync
```
