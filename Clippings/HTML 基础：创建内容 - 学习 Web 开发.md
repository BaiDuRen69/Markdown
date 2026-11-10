## HTML 基础：创建内容

超文本标记语言（ **H** yper **T** ext **M** arkup **L** anguage，HTML）是一种用来构建 Web 网页及其内容的标记语言。本文将帮助你掌握 HTML 的基础知识及其功能，并指导你创建首个网站的基本内容。

| 前提： | 对计算机操作系统、建站所需基础软件以及文件系统的基本了解。 |
| --- | --- |
| 学习成果： | - HTML 的目的与功能。 - HTML 语法的基本组成部分——开始标签与结束标签、元素、属性、头部（head）和主体（body）。 - 常见 HTML 元素，包括段落、标题、图片、列表和链接。 |

## HTML 到底是什么？

HTML 是一种 *标记语言* ，由一系列 [**元素**](https://developer.mozilla.org/zh-CN/docs/Glossary/Element) 组成，用于包裹（或包含）文本内容以定义其结构，并使其表现出特定行为。

让我们看一个示例——以下内容在网页上显示时将全部呈现在同一行，因为它们没有任何结构化处理：

```
人生的指示：
吃饭
睡觉
重复
```

如果我们用以下 HTML 元素包裹此内容，即可将该单行内容转换为一个段落（ [`<p>`](https://developer.mozilla.org/zh-CN/docs/Web/HTML/Reference/Elements/p) ）和三个项目符号点（ [`<li>`](https://developer.mozilla.org/zh-CN/docs/Web/HTML/Reference/Elements/li) ）：

```html
<p>人生的指示：</p>

<ul>
  <li>吃饭</li>
  <li>睡觉</li>
  <li>重复</li>
</ul>
```

这些 HTML 在 web 浏览器中将渲染如下：

运行

除了组织文本结构外，HTML 还有许多其他用途——例如使文本或图像链接到其他网页、嵌入图像或视频、创建数据表格等等。

## 创建你的第一个 HTML 文档

让我们看看各个元素是如何组合成 HTML 页面的。在本节中，你将创建一个基本的 HTML 文件，并了解它的构成。

1. 在你的 `web-projects` 文件夹内，创建一个名为 `first-website` 的新文件夹。
2. 在 `first-website` 文件夹中，创建一个名为 `index.html` 的新文件，并将以下代码按原样插入文件中：

```html
<!doctype html>
<html lang="en-US">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width" />
    <title>My test page</title>
  </head>
  <body>
    <img src="images/firefox-icon.png" alt="My test image" />
  </body>
</html>
```

这里有：

- `<!doctype html>` —— [文档类型](https://developer.mozilla.org/zh-CN/docs/Glossary/Doctype) 。这是必不可少的开头。混沌初分，HTML 尚在襁褓（大约是 1991/92 年）之时，这个元素用来关联 HTML 编写规范，以供自动查错等功能所用。而在当今，它作用有限，可以说仅用于保证文档正常读取。现在知道这些就足够了。
- `<html></html>` —— [`<html>`](https://developer.mozilla.org/zh-CN/docs/Web/HTML/Reference/Elements/html) 元素。该元素包含整个页面的所有内容，有时候也称作根元素。它还包含 `lang` [属性](https://developer.mozilla.org/zh-CN/docs/Glossary/Attribute) ，设置页面的主要语种。
- `<head></head>` —— [`<head>`](https://developer.mozilla.org/zh-CN/docs/Web/HTML/Reference/Elements/head) 元素。该元素作为想在 HTML 页面中包含但不想向用户显示的内容的容器。包括想在搜索结果中显示的 [关键字](https://developer.mozilla.org/zh-CN/docs/Glossary/Keyword) 和页面描述、用于设置页面样式的 CSS、字符集声明等等。
- `<meta charset="utf-8">` ——该元素指明你的文档使用 [UTF-8](https://developer.mozilla.org/zh-CN/docs/Glossary/UTF-8) 字符编码，UTF-8 包括世界绝大多数书写语言的字符。它基本上可以处理任何文本内容。以它为编码还可以避免以后出现某些问题，没有理由再选用其他编码。
- `<meta name="viewport" content="width=device-width">` —— [视口元素](https://developer.mozilla.org/zh-CN/docs/Web/CSS/Guides/CSSOM_view/Viewport_concepts#%E7%A7%BB%E5%8A%A8%E8%AE%BE%E5%A4%87%E7%9A%84%E8%A7%86%E5%8F%A3) 可以确保页面以视口宽度进行渲染，避免移动端浏览器以比视口更宽的宽度渲染内容，导致内容缩小。
- `<title></title>` —— [`<title>`](https://developer.mozilla.org/zh-CN/docs/Web/HTML/Reference/Elements/title) 元素。该元素设置页面的标题，显示在浏览器标签页上，也作为收藏网页的描述文字。
- `<body></body>` —— [`<body>`](https://developer.mozilla.org/zh-CN/docs/Web/HTML/Reference/Elements/body) 元素。该元素包含期望让用户在访问页面时看到的 *全部* 内容，包括文本、图像、视频、游戏、可播放的音轨或其他内容。此时我们只有一个 `<img>` 元素，但接下来我们将添加更多的内容。

**备注：** 大多数 HTML 元素包含 **开始标签** （例如 `<body>` ），随后是元素内容，最后以 **结束标签** （例如 `</body>` ）收尾。某些 HTML 元素还包含 **属性** ，用于存储元素的额外设置或信息——例如代码示例中的 `charset` 、 `name` 和 `src` 属性。

## 嵌入图像

让我们将注意力集中在 [`<img>`](https://developer.mozilla.org/zh-CN/docs/Web/HTML/Reference/Elements/img) 元素上：

```html
<img src="" alt="My test image" />
```

这段代码通过 `src` （源）属性将图片嵌入页面，该属性包含我们要嵌入的图片文件路径。

该元素还包括一个 `alt` （alternative，替代）属性。在 [`alt` 属性](https://developer.mozilla.org/zh-CN/docs/Web/HTML/Reference/Elements/img#%E4%BD%BF%E7%94%A8%E6%9C%89%E5%AE%9E%E9%99%85%E6%84%8F%E4%B9%89%E7%9A%84%E5%A4%87%E7%94%A8%E6%8F%8F%E8%BF%B0) 中，是图像的描述内容，用于当图像不能被用户看见时显示，不可见的原因可能是：

1. 用户有视觉障碍。有严重视觉障碍的用户可以使用屏幕阅读器来朗读 alt 属性的内容。
2. 有些错误使图像无法显示。可以试着故意将 `src` 属性里的路径改错。保存并刷新页面就可以在图像位置看到：

![文字“My test image”](https://developer.mozilla.org/en-US/docs/Learn_web_development/Getting_started/Your_first_website/Creating_the_content/alt-text-example.png)

编写的替代文本应为读者提供足够的信息，使其能够充分理解图像所传达的内容。在此示例中，当前文本“My test image”并不理想，因为它未能描述图像的具体信息。对于 Firefox 徽标而言，更佳的替代方案应为：“The Firefox logo: a flaming fox surrounding the Earth.”。

**备注：** 诸如 `<img>` 之类的元素既无内容也无闭合标签，因此被称为 [**空元素**](https://developer.mozilla.org/zh-CN/docs/Glossary/Void_element) 。有时会在单标签末尾添加 **尾随斜杠** （ `<img />` ），但此写法并非强制要求。

现在我们来让图像显示出来。

1. 在 `first-website` 文件夹内创建名为 `images` 的新文件夹，并将前例中选定的图片放入该文件夹。
2. 在 `<img>` 标签的 `src` 属性值中输入图片路径。该图片位于名为 `images` 的文件夹内，该文件夹与 `index.html` 文件位于同一目录下，因此路径应为 `images/` 加上图片文件名。例如，若图片名为 `firefox-icon.png` ，则 `src` 属性应为： `src="images/firefox-icon.png"` 。
3. 将 `alt` 属性的值—— `My test image` ——替换为更准确描述图片的文本。
4. 在 web 浏览器中打开 `index.html` 文件。此时应能看到图片显示。若未显示，请对照示例代码检查 `<img>` 元素：确保语法正确（如引号位置），并确认图片文件名无误。

若图片过大导致无法完整显示，请不必担心。我们将在下篇文章中解决此问题。

**备注：** 在我们的 [无障碍多媒体教程](https://developer.mozilla.org/zh-CN/docs/Learn_web_development/Core/Accessibility/Multimedia) 和 [替代文本决策树](https://www.w3.org/WAI/tutorials/images/decision-tree/ "外部链接（在新标签页中打开）") 中，了解如何在不同场景下为图片添加 `alt` 属性。

## 标记文本

本小节包含了一些最常用的文本标记 HTML 元素。

**备注：** Scrimba 的 [语义 HTML 基础](https://scrimba.com/the-frontend-developer-career-path-c0j/~0xid?via=mdn "外部链接（在新标签页中打开）") <sup><a href="https://developer.mozilla.org/en-US/docs/MDN/Writing_guidelines/Learning_content#partner_links_and_embeds"><em>MDN 学习合作伙伴</em></a></sup> 提供了一门互动课程，对 HTML 进行了实用性阐释，尤其强调了其 *语义* 特性的重要性。

### 标题

标题（Heading）元素可用于指定内容的标题或子标题。就像一本书的书名、每章的大标题、小标题，等。HTML 文档也是一样。HTML 包括六个级别的标题， [从 \<h1> 到 \<h6>](https://developer.mozilla.org/zh-CN/docs/Web/HTML/Reference/Elements/Heading_Elements) ，一般最多用到 3-4 级标题。

```html
<!-- 4 个级别的标题 -->
<h1>主标题</h1>
<h2>顶层标题</h2>
<h3>子标题</h3>
<h4>次子标题</h4>
```

**备注：** 在 HTML 中， `<!--` 和 `-->` 之间的任何内容都是 **HTML 注释** 。浏览器在渲染代码时，会忽略掉注释。换句话说，注释在页面上不可见——仅停留在代码中。HTML 注释是添加代码或逻辑说明的一种方式，这些说明可能对共同开发该代码的其他人有所帮助，也可能在你六个月后重返项目却记不清当初所为时派上用场。

在 HTML 页面中，将页面标题添加到 [`<img>`](https://developer.mozilla.org/zh-CN/docs/Web/HTML/Reference/Elements/img) 元素的正上方，并用 `<h1> ... </h1>` 标签包裹。保存文件后在浏览器中查看效果。

### 段落

如上文所讲， [`<p>`](https://developer.mozilla.org/zh-CN/docs/Web/HTML/Reference/Elements/p) 元素是用来指定段落的。通常用于指定常规的文本内容：

```html
<p>这是一个段落</p>
```

将上一篇文章中提取的示例文本添加到一个或几个段落中，并把它们放在你的 [`<img>`](https://developer.mozilla.org/zh-CN/docs/Web/HTML/Reference/Elements/img) 元素下方。保存后在浏览器中查看页面效果。

### 列表

web 上有大量内容属于列表形式，HTML 为此提供了专用元素。列表标记至少包含两个元素。最常见的列表类型是序列表和无序列表：

1. **无序列表** （Unordered List）中项目的顺序并不重要，就像购物列表。用一个 [`<ul>`](https://developer.mozilla.org/zh-CN/docs/Web/HTML/Reference/Elements/ul) 元素包装。
2. **有序列表** （Ordered List）中项目的顺序很重要，就像菜谱中的烹调指南。用一个 [`<ol>`](https://developer.mozilla.org/zh-CN/docs/Web/HTML/Reference/Elements/ol) 元素包装。

列表的每个项目用一个列表项目（List Item）元素 [`<li>`](https://developer.mozilla.org/zh-CN/docs/Web/HTML/Reference/Elements/li) 包围。

比如，要将下面的段落片段改成一个列表：

```html
<p>
  At Mozilla, we're a global community of technologists, thinkers, and builders
  working together…
</p>
```

可以这样更改标记：

```html
<p>At Mozilla, we're a global community of</p>

<ul>
  <li>technologists</li>
  <li>thinkers</li>
  <li>builders</li>
</ul>

<p>working together…</p>
```

尝试在示例页面中添加有序或无序列表，并在浏览器中查看结果。

## 链接

链接非常重要——它们赋予 Web 网络属性。要植入一个链接，我们需要使用一个简单的元素—— [`<a>`](https://developer.mozilla.org/zh-CN/docs/Web/HTML/Reference/Elements/a) ——“a”是“anchor”（锚）的缩写。要将一些文本添加到链接中，只需如下几步：

1. 选取一些文本。比如“Mozilla Manifesto”。
2. 将文本包装在 [`<a>`](https://developer.mozilla.org/zh-CN/docs/Web/HTML/Reference/Elements/a) 元素内，就像这样：
	```html
	<a>Mozilla Manifesto</a>
	```
3. 为此 [`<a>`](https://developer.mozilla.org/zh-CN/docs/Web/HTML/Reference/Elements/a) 元素添加一个 `href` 属性，就像这样：
	```html
	<a href="">Mozilla Manifesto</a>
	```
4. 把属性的值设置为所需网址：
	```html
	<a href="https://www.mozilla.org/zh-CN/about/manifesto/">
	  Mozilla Manifesto
	</a>
	```

如果网址开始部分省略了 `https://` 或者 `http://` （称作 *协议* ），可能会得到错误的结果。在完成一个链接后，可以试着点击它来确保指向正确。

**备注：** `href` 这个名字可能一开始看起来有点令人费解。如果它很难记忆的话，记住它代表的是超文本引用（ ***h** ypertext **ref** erence* ）。

若尚未添加，请立即为页面添加链接。

## 总结

如果你一直跟着这篇文章里的指导做的话，你应该完成了一个像下面这样的页面（也可以 [查看这里](https://mdn.github.io/beginner-html-site/ "外部链接（在新标签页中打开）") ）：

![一张网页截图，包含：Firefox 标志，写着“Mozilla is cool”的标题以及两个文本段落](https://developer.mozilla.org/en-US/docs/Learn_web_development/Getting_started/Your_first_website/Creating_the_content/finished-test-page-small.png)

如果你遇到困难，你可以将 Github 上的 [完整示例代码](https://github.com/mdn/beginner-html-site/blob/main/index.html "外部链接（在新标签页中打开）") 与你的文件进行比较。

在此，我们对 HTML 的介绍仅是浅尝辄止。在课程后期的核心模块 [使用 HTML 组织内容](https://developer.mozilla.org/zh-CN/docs/Learn_web_development/Core/Structuring_content) 中，将学习到更多内容。

## 参见

[学习 HTML 和 CSS](https://scrimba.com/learn-html-and-css-c0p?via=mdn "外部链接（在新标签页中打开）") ，Scrimba <sup><a href="https://developer.mozilla.org/en-US/docs/MDN/Writing_guidelines/Learning_content#%E5%90%88%E4%BD%9C%E4%BC%99%E4%BC%B4%E9%93%BE%E6%8E%A5%E5%92%8C%E5%B5%8C%E5%85%A5"><em>MDN 学习伙伴</em></a></sup>

[Scrimba 的](https://scrimba.com/?via=mdn "外部链接（在新标签页中打开）") *学习 HTML 和 CSS* 课程通过构建和部署五个精彩项目来教你 HTML 和 CSS，课程包含有趣的互动式教学和挑战，由知识渊博的教师授课。