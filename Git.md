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
git cl
```