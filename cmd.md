
打开cmd进入指定目录普通方法：
打开cmd：  
1. `Win`+`R`打开“运行”对话框
2. 运行框输入`cmd`回车即可

进入其他盘符（符号都要是英文符号）：  
`盘符名`+`:`
```cmd
C:\Users\bai>D:
```
可以使用终端命令`cd`（表示change directory，即切换目录）在命令窗口中浏览文件系统。  
使用命令`dir`（表示directory，即目录）可以显示当前目录中的所有文件。
进入目录：  
`cd`+`目录`  
需要：
1. 你想要进入的目录要在其所在盘
2. 你输入的目录要在当前目录，不能跳级输入  
```cmd
D:\>cd Python
系统找不到指定的路径

D:\>cd programme

D:\programme>cd D:\programme\GitHub\Python
```
在指定目录打开cmd简单方法：  
在`文件资源管理器地址栏`输入`cmd`回车即可