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
git status
```

跟踪新文件（未必忽略的）：
```bush
git add <files> #所有尖括号都是不用输入的
git add . #添加当前目录下（包括子目录）所有新文件
```
`git add`后可接目录，会添加子目录

