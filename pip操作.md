pip 本身提供了检查过期包的命令，这是最通用、无需额外安装的方法。[](https://pip.pypa.io/en/stable/cli/pip_list/)

**列出所有非最新的包**：
```bush
pip list --outdated
```


该命令会列出所有有新版本可用的包，并显示当前版本和最新版本。你还可以使用 `--format` 参数让输出更清晰
```bush
pip list --outdated --format columns
```

反之，如果想列出所有已是最新版的包，可以使用：

```bush
pip list --uptodate
```

> **提示**：如果包比较多，`pip list --outdated` 的执行速度可能会受网络影响，因为它需要依次查询每个包在 PyPI 上的最新版本。

更新单个库（最常用、最安全）

这是风险最低的方式，推荐优先使用。命令格式统一为 `pip install --upgrade 包名`，可以简写为 `pip install -U 包名`。

例如，要更新 `requests` 库：


```powershell
pip install -U requests
```

在更新任何库之前，建议先更新 `pip` 自身，以确保能正常获取最新版本信息。官方推荐使用 `python -m pip` 的形式，这样可以避免某些环境下的路径混乱问题。

```powershell
python -m pip install --upgrade pip
```

