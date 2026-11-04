[Git教程](https://git-scm.com/docs/user-manual)  

## 下载、更新、设置Git

[下载](https://git-scm.com/download/win),更新：
```bush
git update-git-for-windows
```

提交用户信息
```bush
git config --global user.name "John Doe"
git config --global user.email johndoe@example.com
```

设置文本编辑器（非必要）为VSCode
```bush
git config --global core.editor "code --wait"
```
作用：在某些必须输入文本（没有在命令中写时）时，Git会自动打开

### Github连接

SSH 全称 **Secure Shell**，是一种用于在不安全网络上安全登录远程主机、执行命令、传输文件的协议。平时说的 `ssh` 通常指 **OpenSSH** 这个最常用的实现，默认端口 **22**。

创建SSH Key：
```bush
ssh-keygen -t ed25519 -C "me@example.com"
```

- `~/.ssh/id_ed25519`：私钥，绝不能泄露
- `~/.ssh/id_ed25519.pub`：公钥，可以给别人

在GitHub上添加SSH Key：  
点击“Account settings”->“SSH Keys“ -> "Add SSH Key"，填写任意Title，在Key文本框粘贴`id_ed25519`文件的内容。

## Git基础

### 获取Git仓库

#### 初始化仓库

Windows：
```bush
cd /c/user/my_project #进入文件夹
git init #出现了一个.git文件夹(隐藏文件夹)
```

#### 克隆仓库

命令是`git clone <url>`
例如，克隆我的Markdown仓库：
```bush
git clone https://github.com/BaiDuRen69/Markdown
```

克隆远程仓库是可自定义本地仓库名字：
```bush
git clone https://github.com/BaiDuRen69/Markdown myMarkdown
```

### 记录更新

查看当前文件状态
```bush
git status #输出详细
git status -s #输出简洁
```
使用简洁输入时：新添加的未跟踪文件前面有 `??` 标记，新添加到暂存区中的文件前面有 `A` 标记，修改过的文件前面有 `M` 标记。

跟踪新文件（未必忽略的）：
```bush
git add <files> #所有尖括号都是不用输入的
git add . #添加当前目录下（包括子目录）所有新文件
```
`git add`后可接目录，会添加子目录

暂存已修改的文件：使用`git add`将已修改文件放到暂存区

忽略文件  

创建一个名为 `.gitignore` 的文件，列出要忽略的文件的模式

文件 `.gitignore` 的格式规范如下：

- 所有空行或者以 `#` 开头的行都会被 Git 忽略。
- 可以使用标准的 glob 模式匹配，它会递归地应用在整个工作区中。
- 匹配模式可以以`/`开头防止递归。
- 匹配模式可以以`/`结尾指定目录。
- 要忽略指定模式以外的文件或目录，可以在模式前加上叹`!`取反。

所谓的 glob 模式是指 shell 所使用的简化了的正则表达式。 星号`*`匹配零个或多个任意字符；`[abc]` 匹配任何一个列在方括号中的字符 （这个例子要么匹配一个 a，要么匹配一个 b，要么匹配一个 c）； 问号`?`只匹配一个任意字符；如果在方括号中使用短划线分隔两个字符， 表示所有在这两个字符范围内的都可以匹配（比如 `[0-9]` 表示匹配所有 0 到 9 的数字）。 使用两个星号（`**`）表示匹配任意中间目录，比如 `a/**/z` 可以匹配 `a/z` 、 `a/b/z` 或 `a/b/c/z` 等。  

    tip：GitHub 有一个十分详细的针对数十种项目及语言的 `.gitignore` 文件列表， 你可以在https://github.com/github/gitignore 找到它。

### 查看未暂存的修改  

可以用`git diff`比较的是工作目录中当前文件和暂存区域快照之间的差异, 也就是修改之后还没有暂存起来的变化内容。

可以用`git diff --staged`命令查看已暂存的将要添加到下次提交里的内容。

### 提交更新

使用`git commit`提交更新
```bush
git commit #这样会启动你选择的文本编辑器来输入说明
git commit -m "提交的内容" #也可以添加-m，将提交信息与命令放在一起
```

### 跳过暂存区提交

在提交时加上`-a`，Git 就会自动把所有已经跟踪过的文件暂存起来一并提交，从而跳过 `git add` 步骤：
```bush
git commit -a -m "added an file"
```

### 移除文件  

要从 Git 中移除某个文件，必须要从已跟踪文件清单中移除（确切地说，是从暂存区域移除），然后提交。可以用 `git rm` 命令完成此项工作，并连带从工作目录中删除指定的文件，这样以后就不会出现在未跟踪文件清单中了。  
如果要删除之前修改过或已经放到暂存区的文件，则必须使用强制删除选项 `-f`（force）
```bush
git rm README.md 
git rm -f README.me
```
从 Git 仓库中删除（亦即从暂存区域移除），但仍保留在当前工作目录中（想让文件保留在磁盘，但是并不想让 Git 继续跟踪）。
```bush
git rm --cached README.md
```

`git rm` 命令后面可以列出文件或者目录的名字，也可以使用 `glob` 模式。

### 移动/重命名文件

对文件重命名`git mv`：
```bush
git mv file_from file_to
```
实际上是移动文件相当于：
```bush
mv file_from file_to #只更改工作区
git rm file_from
git add file_to
```

### 查看提交历史/日志

查看提交历史`git log` 

```
tip：在bash查看日志触发分页器时，可以使用：
· 退出：q
· 向下翻整页：Space 或 f (forward) 或 Page Down
· 向上翻整页：b (backward) 或 Page Up
· 向下翻半页：d (down)
· 向上翻半页：u (up)
· 向下滚动一行：Enter 或 ↓ 或 j
· 向上滚动一行：↑ 或 k
```

可以使用`-p`或`-patch`选项来显示每次提交的差异（按补丁的格式输出）并且可以限制显示的日志数目`- <n>`  
```bush
git log -p -2 #只显示最近两次提交
```

使用`--stst`可以查看提交的简略信息  
```bush
git log --stst
```

使用`--pretty`切换`git log`的展示格式，`oneline`、`short`、`full`、`fuller`详细程度升高；还有`format`自定义显示格式。
```bush
git log --pretty=oneline
# 每个提交一行：短哈希 + 提交标题

git log --pretty=short
# 短哈希、作者、日期、标题

git log --pretty=full
# 比 short 更详细，包含作者和提交者信息

git log --pretty=fuller
# 最详细的内建格式之一，区分作者日期和提交者日期

git log --pretty=format:"%h - %an, %ar : %s"
```

`git log --pretty=format`常用选项：

| 选项    | 说明                          |
| ----- | --------------------------- |
| `%H`  | 提交的完整哈希值                    |
| `%h`  | 提交的简写哈希值                    |
| `%T`  | 树的完整哈希值                     |
| `%t`  | 树的简写哈希值                     |
| `%P`  | 父提交的完整哈希值                   |
| `%p`  | 父提交的简写哈希值                   |
| `%an` | 作者名字                        |
| `%ae` | 作者的电子邮件地址                   |
| `%ad` | 作者修订日期（可以用 --date=选项 来定制格式） |
| `%ar` | 作者修订日期，按多久以前的方式显示           |
| `%cn` | 提交者的名字                      |
| `%ce` | 提交者的电子邮件地址                  |
| `%cd` | 提交日期                        |
| `%cr` | 提交日期（距今多长时间）                |
| `%s`  | 提交说明                        |

	tip:作者指的是实际作出修改的人，提交者指的是最后将此工作成果提交到仓库的人

`git log`的常用选项：

| 选项                | 说明                                                                    |
| ----------------- | --------------------------------------------------------------------- |
| `-p`              | 按补丁格式显示每个提交引入的差异。                                                     |
| `--stat`          | 显示每次提交的文件修改统计信息。                                                      |
| `--shortstat`     | 只显示 --stat 中最后的行数修改添加移除统计。                                            |
| `--name-only`     | 仅在提交信息后显示已修改的文件清单。                                                    |
| `--name-status`   | 显示新增、修改、删除的文件清单。                                                      |
| `--abbrev-commit` | 仅显示 SHA-1 校验和所有 40 个字符中的前几个字符。                                        |
| `--relative-date` | 使用较短的相对时间而不是完整格式显示日期（比如“2 weeks ago”）。                                |
| `--graph`         | 在日志旁以 ASCII 图形显示分支与合并历史。                                              |
| `--pretty`        | 使用其他格式显示历史提交信息。可用的选项包括 oneline、short、full、fuller 和 format（用来定义自己的格式）。 |
| `--oneline`       | `--pretty=oneline --abbrev-commit` 合用的简写。                             |

### 限制输出长度

使用`- <n>`仅显示最近的n次提交，n为任何整数  
使用`--since`和`--until`按照时间限制  
```bush
git log --since=2.weeks #列出最近两周的提交

git log --since="1 week ago" \
        --until="now" \
        --pretty=format:"%h - %an, %ar : %s"
# 查看最近一周的提交 
```
使用`--author`指定作者的提交  
使用`--grep`搜索提交说明中的关键字  

你可以指定多个 `--author` 和 `--grep` 搜索条件，这样会只输出匹配 **任意** `--author` 模式和 **任意** `--grep` 模式的提交。  
然而，如果你添加了 `--all-match` 选项， 则只会输出匹配 **所有** `--grep` 模式的提交。

使用`-S <字符串>`过滤器只显示哪些**添加**或**删除**该字符串的提交
```bush
git log -S function_name
```
使用`git log -- <路径>`只显示某些文件或目录的提交，与其他选项组合时路径要在最后
```bush
git log -- src/
git log -- README.md
```
限制`git log`输出的选项：

| 选项                    | 说明                    |
| --------------------- | --------------------- |
| `-<n>`                | 仅显示最近的 n 条提交。         |
| `--since`, `--after`  | 仅显示指定时间之后的提交。         |
| `--until`, `--before` | 仅显示指定时间之前的提交。         |
| `--author`            | 仅显示作者匹配指定字符串的提交。      |
| `--committer`         | 仅显示提交者匹配指定字符串的提交。     |
| `--grep`              | 仅显示提交说明中包含指定字符串的提交。   |
| `-S`                  | 仅显示添加或删除内容匹配指定字符串的提交。 |
| `-- <path>`           | 仅显示某些文件或目录的提交         |

### 撤销操作

修正最后一次提交：`git commit --amend ...`，用当前提交代替上次提交。
选项`--no-edit`沿用原来的提交信息。
```bush
git commit --amend #打开编辑器
git commit --amend -m "新提交的信息"
git commit --amend --no-edit
```

撤销暂存区的文件：`git reset HEAD <file>...`和`git restore --staged <file>`
```bush
git reset HEAD README.md
```

撤销工作区的修改：`git checkout -- <file>...`和`gir restore <file>`（新版推荐）
```bush
git checkout -- README.md
```

### 版本回退

版本回退：`git reset` 
在Git中，用`HEAD`表示当前版本，也就是最新的提交`1094adb...`（注意我的提交ID和你的肯定不一样），上一个版本是`HEAD^`，上上个版本是`HEAD^^`，往上100个版本写成`HEAD~100`。  
选项`--hard`会回退到上个版本的已提交状态，而`--soft`会回退到上个版本的未提交状态，`--mixed`会回退到上个版本已添加但未提交的状态。

|模式|HEAD 移动|暂存区|工作区|
|---|---|---|---|
|`--soft`|✅|保留|保留|
|`--mixed`（默认）|✅|重置|保留|
|`--hard`|✅|重置|重置（丢代码）|

```bush
git reset --hard HEAD^
```
找回误删的提交  

查看所有HEAD移动记录：`git reflog`
```bush
git reflog
git reset --soft <目标hash> # 先软回退，保留改动
```

回退已push的提交

不要用 `reset --hard` 强推，会破坏别人的历史。用 `git revert`：

```bush
git revert <commit-hash>      # 生成一个反向提交，抵消该提交
git revert HEAD               # 撤销最近一次提交
git revert HEAD~3..HEAD       # 撤销最近 3 次提交
```
### 远程仓库

查看远程仓库：`git remote`列出指定的运程仓库的简写。选项`-v`显示需要读写远程仓库使用的 Git 保存的简写与其对应的 URL。
```bush
git remote #origin
git remote -v 
```

查看远程仓库的详细信息：`git remote show <remote>`
```bush
git remote #origin
git remote show origin
```

添加一个新的远程Git仓库：`git remote add <shortname> <url>`，shortname为仓库的简写。可以使用仓库的简写来代替整个URL。
```bush
git remote add md <https://github.com/BaiDuRen69/Markdown
git fetch md 
```

#### 获取更新

从运程仓库中抓取/拉取你还没有的数据：`git fetch <remote>`，**更新对应的远程跟踪分支，但不合并、不改工作区**。
```bush
git fetch md
```

从远程仓库**获取更新**并**合并**到当前分支：`git pull`
```bush
git pull
```

#### 推送

推送到远程仓库：`git push <remote> <branch>`
```bush
git push                              # 推当前分支到上游
git push origin main                  # 推 main 到 origin/main
git push -u origin feature            # 首次推送并设置上游（-u = --set-upstream）
git push --tags                       # 推送标签
git push origin v1.0.0                # 推送单个标签
git push --force                # 危险：直接覆盖远程
git push --force-with-lease     # 推荐：远程被别人更新过则拒绝
```


重命名远程仓库：`git remote rename`
```bush
git remote rename md markdown
#将仓库md重命名为markdown
```

移除远程仓库：`git remote remove`或`git remote rm`
```bush
git remote remove md
```

### 打标签

列出标签：`git tag`，使用`-l`或`-list`按特定的模式查找标签。
```bush
git tag
git tag -l "v1.8.5*"
```

创建标签：

创建附注标签：`git tag -a`
```bush
git tag -a v1.4 -m "my version 1.4"
```
查看标签信息和与之对应的提交信息：`git show`
```bush
git show v1.4
```
创建轻量标签：`git tag <版本>`
```bush
git tag v1.4-lw
```

	tip：核心区别是：附注标签是一个独立的 Git 对象，带元数据；轻量标签只是一个指向 commit 的引用。

后期打标签  
对过去的提交打标签：
```bush
git tag -a v1.2 9fceb02d
```

共享标签  
推送标签到共享服务器：`git push origin <tagname>`
可以使用`--tags`选项一次推送全部标签  
```bush
git push origin v1.5
git push origin --tags
```

	tip；`git push`默认不会推送标签

删除标签
删除本地仓库的标签：`git tag -d <tagname>`
```bush
git tag -d v1.4-lw
```
删除远程仓库标签：`git push <remote> :refs/tags/<tagname>`或者`git push origin --delete <tagname>`
```bush
git push origin :refs/tags/v1.4-lw
git push origin --delete v1.4
```

检出标签
把工作区切到某个标签指向的提交：`git checkout`或者`git switch`
```bush
git checkout v1.0.0
git switch --detach v1.0.0 #新版推荐
```

基于标签修改代码时应创建分支：
```bush
git switch -c hotfix-v1.0.0 v1.0.0
git checkout -b hotfix-v1.0.0 v1.0.0
```

### Git别名

如果不想每次都输入完整的 Git 命令，可以通过 `git config` 文件来轻松地为每一个命令设置一个别名
```bush
git config --global alias.co checkout
git config --global alias.br branch
git config --global alias.ci commit
git config --global alias.unstage 'reset HEAD --'
```

	tip：Git只是简单地将别名替换为对应的命令。

在 Git 别名中以 `!` 开头，可以让 Git 执行外部 shell 命令，而不是把它当成 Git 子命令。
```bush
git config --global alias.ll '!git log --online --decorate -10'
之后执行：
git ll
实际执行：
git log --online --decprate -10
```

## Git分支

### 创建分支

创建分支：`git branch`，选项`-b`表示创建并切换
```bush
git branch testing
git checkout -b testing # 创建并切换到testing
git switch -c testing # 创建并切换，推荐
```
基于某个commit或标签创建分支
```bush
git switch -c hotfix v1.2.0     # 基于标签
git switch -c fix a1b2c3d       # 基于某个提交
git switch -c fix main~3        # 基于 main 往前第 3 个提交
```

### 查看分支

查看分支：`git branch`
```bush
git branch          # 本地分支
git branch -a       # 本地 + 远程分支
git branch -v       # 显示各分支最新 commit
git branch --merged # 已合并到当前分支的分支
```

### 切换分支

切换分支：
```bush
git branch dev              # 创建 dev，不切换
git switch dev              # 切换到 dev
git switch -c dev           # 创建并切换，推荐
git checkout -b dev         # 旧写法，创建并切换
```

### 合并分支

合并指定分支到当前分支：`git merge`
先切到目标分支，再合并：
```bush
git switch main
git merge dev # 将dev分支合并到main分支
```

### 删除分支

```bush
git branch -d <name>     # 删除已合并分支
git branch -D <name>     # 强制删除未合并分支
```
删除远程分支：
```bush
git push origin --delete <name>
```

### 远程分支

```bush
git fetch origin
git branch -a
git switch -c dev origin/dev      # 基于远程分支建本地分支
git push -u origin dev            # 推送并设置 upstream
```

### 合并冲突

冲突发生在**同一文件的同一区域**被两边以不同方式修改时，文件里会出现冲突标记

解决流程：
```bush
git status                 # 查看冲突文件（both modified）
# 手动编辑，删掉标记，保留正确内容
git add .           # 标记为已解决
git commit                 # 完成合并（commit message 已预填）

# 想放弃合并：
git merge --abort
```

辅助命令：
```bush
git diff                   # 看冲突区域的差异
git checkout --ours  file  # 冲突处取当前分支版本
git checkout --theirs file # 冲突处取对方版本
```

用带参数的`git log`也可以看到分支的合并情况：
```bush
 git log --graph --pretty=oneline --abbrev-commit
```

**Fast-forward**：当被合并分支的提交历史**完全包含**在目标分支的"前方延长线"上时，Git 只需把目标分支的指针**向前移动**到对方的最新提交，不产生任何新提交。  
如果可能，合并分支时，Git通常会用`Fast forward`模式，但这种模式下，删除分支后，会丢掉分支信息。

```
                           HEAD
                             │
                             ▼
                          master
                             │
                             ▼
┌───┐    ┌───┐    ┌───┐    ┌───┐
│   │───▶│   │───▶│   │───▶│   │
└───┘    └───┘    └───┘    └───┘
                             ▲
                             │
                            dev
```

如果要强制禁用`Fast forward`模式，Git就会在merge时生成一个新的commit，这样，从分支历史上就可以看出分支信息。  

使用`--no-ff`参数，表示禁用`Fast forward`  
```bush
git merge --no-ff -m "merge with no-ff" <name>
```

```
                                 HEAD
                                  │
                                  ▼
                                master
                                  │
                                  ▼
                                ┌───┐
                         ┌─────▶│   │
┌───┐    ┌───┐    ┌───┐  │      └───┘
│   │───▶│   │───▶│   │──┤        ▲
└───┘    └───┘    └───┘  │  ┌───┐ │
                         └─▶│   │─┘
                            └───┘
                              ▲
                              │
                             dev
```

### bug分支

**临时保存未提交修改**（工作区和暂存区）的功能：`git stash`
```bush
# 保存当前工作区和暂存区的修改
git stash

# 保存并添加说明
git stash push -m "临时保存：登录页修改"

# 查看 stash 列表
git stash list

# 恢复最近一次 stash，并删除该记录
git stash pop

# 恢复最近一次 stash，但保留记录
git stash apply

# 恢复指定 stash
git stash apply stash@{1}

# 查看某个 stash 的内容
git stash show -p stash@{0}

# 删除最近一次 stash
git stash drop

# 清空所有 stash
git stash clear
```

#### 常用场景
 切换分支前保存修改
 ```bush
git stash
git checkout other-branch
# 处理完回来
git checkout original-branch
git stash pop
 ```

拉取远程代码前保存本地修改
```bush
git stash
git pull
git stash pop
```

保存包含未跟踪文件
保存时保留暂存区状态

“bug 分支”通常指在 Git 中为修复某个 bug 单独创建的分支，避免直接在主分支或开发分支上改代码。  

```bush
# 保存当前未提交修改
git stash push -m "功能开发中"

# 切到主分支拉 bug 分支
git checkout main
git pull
git checkout -b bugfix/xxx

# 修复完成提交后，回到原分支恢复
git checkout feature/xxx
git stash pop
```

`cherry-pick`命令，让我们能复制一个特定的提交到当前分支：`git cherry-pick <修复提交的commit-id>`  
```bush
# 保存当前未提交修改
# 切到主分支拉 bug 分支
# 修复完成提交后，回到原分支恢复
...
git stash pop

# 把修复同步到当前分支
git cherry-pick <修复提交的commit-id>
```

### Rebase

**`merge` 是保留分叉再合并，`rebase` 是把你的提交逐个“重放”到新基底上，从而形成线性历史。**

例如：
```text
      C---D  feature
     /
A---B---E---F  main
```
在 `feature` 分支执行：
```bush
git switch feature
git rebase main
# 把 feature 分支上有、但 main 上没有的提交，重放到 main 最新提交之后。
```
结果：
```text
A---B---E---F---C'---D'  feature
```

#### 指定分支变基

```bush
git rebase main feature
```
表示把 `feature` 变基到 `main` 上，但不一定先切换分支。

#### 拉取远程时使用 rebase

```bush
git pull --rebase
```
它等价于先 `fetch`，再把本地提交重放到远程最新提交之后，避免产生无意义的 merge commit。

#### 交互式rebase：整理提交历史

