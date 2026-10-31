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

### 提交跟新

使用`git commit`提交更新
```bush
git commit #这样会启动你选择的文本编辑器来输入说明
git commit -m "提交的内容" #也可以添加-m，将提交信息与命令放在一起
```

### 跳过使用暂存区

在提交时加上`-a`，Git 就会自动把所有已经跟踪过的文件暂存起来一并提交，从而跳过 `git add` 步骤：
```bush
git commit -a -m "added an file"
```

### 移除文件  

要从 Git 中移除某个文件，就必须要从已跟踪文件清单中移除（确切地说，是从暂存区域移除），然后提交。可以用 `git rm` 命令完成此项工作，并连带从工作目录中删除指定的文件，这样以后就不会出现在未跟踪文件清单中了。  

如果要删除之前修改过或已经放到暂存区的文件，则必须使用强制删除选项 `-f`

从 Git 仓库中删除（亦即从暂存区域移除），但仍保留在当前工作目录中（想让文件保留在磁盘，但是并不想让 Git 继续跟踪）。
```bush
git rm --cached README.md
```

