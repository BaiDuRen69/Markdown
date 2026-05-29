---
title: "Obsidian邪修用法，免费云同步，AI，手机端，进阶技巧"
url: "https://www.bilibili.com/video/BV1fZCyBYEuT/?spm_id_from=333.1007.top_right_bar_window_custom_collection.content.click&vd_source=a0ae2f9638f63e280dd3406cd1f8433e"
bvid: "BV1fZCyBYEuT"
cid: "34024720903"
author: "技术爬爬虾"
upload_date: "2025-11-15"
subtitle_lang: "中文"
created: "2026-05-28"
tags: ["clippings", "bilibili"]
---

<iframe src="https://player.bilibili.com/player.html?aid=115553561739950&bvid=BV1fZCyBYEuT&cid=34024720903&page=1&autoplay=0" scrolling="no" border="0" frameborder="no" framespacing="0" allow="fullscreen; picture-in-picture" allowfullscreen="true" style="height:100%;width:100%; aspect-ratio: 16 / 9;"> </iframe>


### 核心观点：选择 Obsidian 的三大理由

1. **数据安全**：笔记本质是本地独立的 Markdown 文件，即使软件停止维护，数据仍可用其他编辑器打开；配合 GitHub 备份比云笔记更安全。
2. **界面丝滑流畅**：UI 响应速度极快，切换笔记无白屏、无卡顿，有助于保持工作心流。
3. **与 AI 工具绝配**：本地 Markdown 文件天然适合 AI 编程工具（如 Claude Code, Gemini CLI）处理，可实现查找笔记、整理文件夹、模仿文风写作等功能。

---

### 一、 云同步方案：GitHub + Git 插件

- **优势**：免费、安全稳定（计算机科学基础设施）、支持私有仓库。
- **电脑端配置**：
    1. 在 GitHub 创建 **Private** 仓库。
    2. 使用 GitHub Desktop 克隆仓库到本地。
    3. 用 Obsidian 打开该本地文件夹作为库。
    4. **关键步骤**：创建 `.gitignore` 文件，排除 `workspace.json` 等频繁变动的配置文件，避免同步冲突。
    5. 安装社区插件 **Git**：
        - 开启 `Auto commit and sync after stopping file edits`（停止编辑后自动同步）。
        - 设置自动同步间隔（如 1 分钟）。
        - 开启 `Pull on startup`（启动时拉取最新更改）。
- **备份策略**：除 GitHub 外，建议结合网盘或硬盘进行冷备份。

### 二、 AI 玩法：推荐使用 AI 编程工具

- **推荐工具**：Gemini CLI（免费、文风适配度高）或 Claude Code。相比社区插件，大厂出品的编程工具文件系统交互能力更强。
- **环境准备**：安装 Node.js -> 安装 Gemini CLI -> 登录授权。
- **实战案例**：
    1. **自媒体选题**：让 AI 分析过往脚本，结合网络热点生成新选题及大纲。
    2. **批量文件处理**：让 AI 自动创建子文件夹、移动文件、清理临时文件。
    3. **模仿写作**：让 AI 搜索相关文章，并参考作者往期笔记风格撰写视频脚本。
- **安全保障**：利用 Git 的版本控制功能，若 AI 修改出错，可随时通过 `Discard changes` 还原，无需担心文件丢失。
- **补充工具**：若需本地 AI 知识库搜索，推荐 **Hyperlink**。

### 三、 Markdown 基础与进阶

- **基础语法**：标题（#）、加粗（\*\*\*\*）、删除线（\~\~\~\~）、高亮（\=\=\=\=）、代码块（\`\`\`）、引用（>）、列表（- 或 1.）、表格与公式块。
- **掌握程度**：基础语法即可覆盖 80% 的使用场景。

### 四、 图像存储优化（解决原生痛点）

- **原生问题**：图片存放混乱、链接非标准 Markdown 格式、跨平台/网页端无法显示。
- **解决方案**：
    1. 安装插件 **Custom Attachment Location**。
    2. 设置附件存储路径为同名子文件夹，开启重命名附件。
    3. 在 Obsidian 设置中关闭“使用 Wiki 链接”，选择“基于当前笔记的相对路径”。
- **效果**：图片自动归类到独立文件夹，链接符合标准 Markdown 语法，VS Code 和 GitHub 网页端均可正常预览，且无需付费云图床。

### 五、 手机端使用与同步

- **初始导入**：通过数据线将电脑端笔记文件夹复制到手机 `Documents` 目录，用 Obsidian App 打开。
- **Git 同步配置**：
    1. 在手机 Obsidian 的 Git 插件设置中填写 GitHub 用户名、邮箱。
    2. 在 GitHub 网页端生成 **Personal Access Token (Classic)**，勾选 `repo` 权限，填入手机端。
- **注意事项**：避免手机和电脑同时编辑同一文件以防冲突；若产生冲突需手动解决。

### 六、 导出与知识图谱

- **格式导出**：安装 **Enhancing Export** 插件 + 本地安装 **Pandoc** 工具，可将笔记导出为 Word、HTML 等多种格式，且保留配图。
- **双向链接**：使用 `` 链接其他笔记。
- **关系图谱**：可视化展示笔记间的关联，帮助归纳整理知识、发现隐藏联系并激发灵感。

### 💡 总结

这套 Obsidian 工作流偏向程序员/极客风格，但所有工具组合均**完全免费**。它完美解决了数据安全、多端同步、AI 协作和图片管理等核心需求，是一套高效且可持续的个人知识管理系统。