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

tip：GitHub 有一个十分详细的针对数十种项目及语言的 `.gitignore` 文件列表， 你可以在 [gitignore](https://github.com/github/gitignore) 找到它。

