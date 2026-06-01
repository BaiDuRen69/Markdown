---
created: 2026-05-28
---

可能让你失望了，这里没有什么从零开始的Obsidian教程，不过是我踩过的一部分坑。如果你想要从零开始具体学怎么使用Obsidian，可以去这里[Obsidian 教程 | 菜鸟教程](https://www.runoob.com/obsidian/obsidian-tutorial.html)。

当然我通过B站视频入坑的[Obsidian入门_B站技术爬爬虾](Obsidian入门_B站技术爬爬虾.md)，感兴趣的可以看看。

## 同步方案——坚果云同步、github备份  

如果你愿意付费大可不必看下面的内容，你可以先去看看官方同步的费用  

我开始时使用的是Git+GitHub同步，但是由于git同步插件是全英文的，我难以看懂，加之我在手机上文件时两次弹出不能推送到github，我实在心力交瘁（毕竟一般是用好用的想法在手机上临时记录一下，可是当回到寝室打开电脑，发现文件没有被成功同步，我实在难以忍受。并且还要开梯子才可以成功连上，我实在没有这份心力在每次看文件前要去确认梯子。）  

至于OneDrive同步，我实在接受不了这么慢的同步速度 。  

### 坚果云同步

当然在使用坚果云之前，或许你要知道
1. 收费  

| 版本    | 价格      | 空间   | 特点                            |
| ----- | ------- | ---- | ----------------------------- |
| 免费版   |         |      | 上传流量1GB/月，下载流量3GB/月           |
| 专业版   | 199.9/年 | 42GB | 上传下载流量不限；保留3个月历史版本；误删恢复；全平台搜索 |
| 高级专业版 | 399.9/年 | 96GB | 包含专业版所有功能，空间更大，适合重度同步用户       |

2. API请求限制，免费版每30分钟限600次请求，付费版限1500次。Obsidian 的自动保存机制非常频繁（打几个字就保存一次），极易在短时间内耗尽额度  
3. 无高级恢复功能：部分高级数据恢复或批量回滚功能可能仅限付费用户，免费版在数据灾难面前的容错率更低。  

如果你可以接受以上及其他未知问题，你可以试试 **PC用坚果云客户端 + 手机用插件**
十分简单，前往官方教程[官宣！坚果云 x Obsidian 官方同步插件，正式上架！](https://mp.weixin.qq.com/s/5wRWhsgler12xMU6mVzybg)  
设置→第三方插件→关闭安全模式→浏览→Nutstore Sync→安装→启用→选项→登录  

### Github备份

如果你并非开发者，且对备份没有这么高的追求，你或许可以去试试其他的备份方式，在这[Obsidian 同步、备份与进阶技巧 | 菜鸟教程](https://www.runoob.com/obsidian/obsidian-sync-backup-tips.html)提到了不少的方案。请不必将别人的方案当做自己的真理。
[Git](https://git-scm.com/)这个网站有关于安装、使用的方式，如果你不愿看文字可以[git-bilibili](https://search.bilibili.com/all?keyword=git&from_source=web_search&spm_id_from=333.1007&search_source=5)去找视频看。  
接着你可以试着去找一些git和obsidian综合使用的视频或文章。  
接着你可能会遇到在手机上使用git插件同步的教程，但是我认为大可不必如此，手机只需用坚果云同步即可：

| 电脑         | 手机  |
| ---------- | --- |
| github+坚果云 | 坚果云 |

## 插件推荐

### 1. Custom Attachment Location

这款插件让您能够通过变量（如`${noteFileName}`, `${date:format}`等）完全自定义附件的存储位置，就像Typora那样灵活高效  
具体的我并不明白，可以去看[Obsidian邪修用法，免费云同步，AI，手机端，进阶技巧_哔哩哔哩_bilibili](https://www.bilibili.com/video/BV1fZCyBYEuT/?spm_id_from=333.1007.top_right_bar_window_custom_collection.content.click&vd_source=a0ae2f9638f63e280dd3406cd1f8433e)的**章节：图像**跟随设置

### 2. File Cleaner Redux

这款插件可以帮助用户清理空文件和未使用附件的工具

### 3.Local REST API

它是一个辅助插件帮助浏览器插件完成导入。  

edge插件：[Bilibili Obsidian Clipper｜一键保存B站字幕](https://microsoftedge.microsoft.com/addons/detail/bilibili-obsidian-clipper/fbeeapnjdjgacilaobonekidbfjcmdjo)  
它可以在B站视频页面直接获取字幕内容，支持作者字幕和 AI 字幕，多语言可选，还能复制 Markdown、下载 txt / srt，一键写入 Obsidian 指定目录。这样就可以把收藏的内容沉淀到本地知识库里，再交给 AI 继续整理、总结和搜索。
前往作者教程[视频内容一键保存到 Obsidian：打通本地知识库_哔哩哔哩_bilibili](https://www.bilibili.com/video/BV15qQwB4EZ9/?spm_id_from=333.1387.homepage.video_card.click&vd_source=040bc5ea7866b419558ec2682a2ccb59)

再讲一个浏览器插件，它能够剪藏浏览器上的内容保存到本地
[Obsidian Web Clipper - Microsoft Edge Addons](https://microsoftedge.microsoft.com/addons/detail/obsidian-web-clipper/eigdjhmgnaaeaonimdklocfekkaanfme)  
这或许可以防止原内容被删除、修改，同时也能更好的在自己的电脑上使用AI调用  

## 怎么使用AI

网上有很多教Obsidian接入AI，感兴趣的可以去了解  

但是大多都只是调用AI来完成一些任务，并没有实现用自己的笔记来训练AI让它能够和自己同步，当然也有本地部署的方式，但那就仁者见仁，智者见智了   
哪些调用API的方式或许只不过让我们少了一些复制粘贴的步骤而已，当然如果你认为花一些钱来省事那自然可行  

### WorkBuddy

WorkBuddy 是腾讯推出的全场景职场 AI 智能体桌面工作台，面向各类职能角色设计。您只需用一句话描述需求，WorkBuddy 便能像同事一样自主规划和执行任务，并交付可验收的结果。  

感兴趣的可以去了解[WorkBuddy](https://www.codebuddy.cn/docs/workbuddy/Overview)

作为腾讯开发的🦞，我认为还不错  

虽然不能直接接入Obsidian，没有那么高的适配度，但是作为白嫖已经很不错了  

**当然我建议你在使用时进行备份或者告诉它不要直接修改原文件**

### ima

如果你需要自己的知识库

你或许可以试试[ima.copilot-腾讯AI工作台](https://ima.qq.com/download)

jiang'zi