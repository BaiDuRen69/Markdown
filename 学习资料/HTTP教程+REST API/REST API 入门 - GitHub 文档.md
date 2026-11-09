## 简介

本文介绍如何将GitHub REST API 与GitHub CLI、 `curl` 或 JavaScript 配合使用。 有关快速入门指南，请参阅 [GitHub REST API 快速入门](https://docs.github.com/zh/rest/quickstart) 。

## 关于对 REST API 的请求

本节介绍构成 API 请求的元素：

每个对 REST API 的请求都包含一个 HTTP 方法和一个路径。 取决于 REST API 终结点，可能还需要指定请求标头、身份验证信息、查询参数或正文参数。

REST API 参考文档介绍了每个终结点的 HTTP 方法、路径和参数。 它还显示每个终结点的示例请求和响应。 有关详细信息，请查看 [REST 参考文档](https://docs.github.com/zh/rest) 。

### HTTP 方法

终结点的 HTTP 方法定义它对给定资源执行的操作类型。 常见的一些 HTTP 方法有 `GET` 、 `POST` 、 `DELETE` 和 `PATCH` 。 REST API 参考文档介绍了每个终结点的 HTTP 方法。

例如， [“列出存储库问题”终结点](https://docs.github.com/zh/rest/issues/issues#list-repository-issues) 的 HTTP 方法为 `GET` 。

如果可能， GitHub REST API 会努力为每个操作使用适当的 HTTP 方法。

- `GET` ：用于检索资源。
- `POST` ：用于创建资源。
- `PATCH` ：用于更新资源的属性。
- `PUT` ：用于替换资源或资源集合。
- `DELETE` ：用于删除资源。

### 路径

每个终结点都有一个路径。 REST API 参考文档介绍了每个终结点的路径。 例如， [“列出存储库问题”终结点](https://docs.github.com/zh/rest/issues/issues#list-repository-issues) 的路径为 `/repos/{owner}/{repo}/issues` 。

路径中的大括号 `{}` 表示需要指定的路径参数。 路径参数修改终结点路径，在请求中是必需的。 例如， [“列出存储库问题”终结点](https://docs.github.com/zh/rest/issues/issues#list-repository-issues) 的路径参数为 `{owner}` 和 `{repo}` 。 要在 API 请求中使用此路径，请将 `{repo}` 替换为想要请求问题列表的存储库的名称，并将 `{owner}` 替换为存储库所有者帐户的名称。

### 标头

标头包含有关请求和所需响应的其它信息。 下面是可在对 GitHub REST API 的请求中使用的标头的一些示例。 有关使用标头的请求示例，请参阅 [发出请求](#making-a-request) 。

#### Accept

大多数 GitHub REST API 终结点要求您传递一个值为 `Accept` 的 `application/vnd.github+json` 标头。 `Accept` 标头的值为媒体类型。 有关媒体类型的详细信息，请参阅 [媒体类型](#media-types) 。

#### X-GitHub-Api-Version

应使用此标头指定要用于请求的 REST API 版本。 有关详细信息，请参阅“ [API 版本](https://docs.github.com/zh/rest/about-the-rest-api/api-versions) ”。

#### User-Agent

所有 API 请求都必须包含有效的 `User-Agent` 标头。 `User-Agent` 标头标识发出请求的用户或应用程序。

默认情况下， `curl` 会发送有效的 `User-Agent` 标头。 但是，GitHub 建议使用 GitHub 用户名或你的应用程序的名称作为 `User-Agent` 标头值。 这允许 GitHub 在出现问题时与你联系。

下面的示例 `User-Agent` 是一个名为 `Awesome-Octocat-App` 的应用：

```shell
User-Agent: Awesome-Octocat-App
```

没有 `User-Agent` 标头的请求将被拒绝。 如果提供无效的 `User-Agent` 标头，则将收到 `403 Forbidden` 响应。

### 媒体类型

可以通过将媒体类型添加到请求的 `Accept` 标头来指定一种或多种媒体类型。 有关 `Accept` 标头的详细信息，请参阅 [`Accept`](#accept) 。

媒体类型指定要从 API 获取的数据格式。 媒体类型特定于资源，允许它们独立更改并支持其他资源不支持的格式。 每个 GitHub REST API 终结点的文档将描述它支持的媒体类型。 有关详细信息，请参阅 [GitHub REST API 文档](https://docs.github.com/zh/rest) 。

REST API 支持的 GitHub 最常见媒体类型是 `application/vnd.github+json` 和 `application/json` 。

还可以将自定义媒体类型和某些端点搭配使用。 例如，用于管理 [提交](https://docs.github.com/zh/rest/commits/commits#get-a-commit) 和 [提取请求](https://docs.github.com/zh/rest/pulls/pulls) 的 REST API 支持媒体类型 `diff` 、 `patch` 和 `sha` 。 某些其他端点使用媒体类型 `full` 、 `raw` 、 `text` 或 `html` 。

所有自定义媒体类型 GitHub 如下所示： `application/vnd.github.PARAM+json` ，其中 `PARAM` 是媒体类型的名称。 例如，要指定 `raw` 媒体类型，可以使用 `application/vnd.github.raw+json` 。

有关使用媒体类型的请求示例，请参阅 [发出请求](#making-a-request) 。

### 身份验证

许多终结点需要身份验证或是在进行身份验证后返回其他信息。 此外，进行身份验证后，每小时可以发出更多请求。

要对请求进行身份验证，需要提供具有所需作用域或权限的身份验证令牌。 有几种不同的方式可以获取令牌：可以创建一个 personal access token、使用 GitHub App 生成令牌，或者在 GitHub Actions 工作流中使用内置的 `GITHUB_TOKEN` 。 有关详细信息，请参阅“ [对 REST API 进行身份验证](https://docs.github.com/zh/rest/authentication/authenticating-to-the-rest-api) ”。

有关使用身份验证令牌的请求示例，请参阅 [发出请求](#making-a-request) 。

### 参数

许多 API 方法要求或允许在请求的参数中发送其他信息。 有几种不同类型的参数：路径参数、正文参数和查询参数。

#### 路径参数

路径参数会修改终结点路径。 这些是请求中的必需参数： 有关详细信息，请参阅 [Path](#path) 。

#### 正文参数

正文参数使你可以将其他数据传递给 API。 上述参数可以是可选参数，也可以是必需参数，具体取决于终结点。 例如，正文参数可能允许在创建新问题时指定问题标题，或在启用/禁用功能时指定某些设置。 每个 GitHub REST API 终结点的文档将描述它支持的正文参数。 有关详细信息，请参阅 [GitHub REST API 文档](https://docs.github.com/zh/rest) 。

例如， [“创建问题”终结点](https://docs.github.com/zh/rest/issues/issues#create-an-issue) 要求为请求中的新问题指定标题。 此外，还允许选择指定其他信息，例如要放入问题正文中的文本、要分配给新问题的用户或要应用于新问题的标签。 有关使用正文参数的请求示例，请参阅 [发出请求](#making-a-request) 。

必须对请求进行身份验证才能传递正文参数。 有关详细信息，请参阅 [身份验证](#authentication) 。

#### 查询参数

查询参数使你可以控制为请求返回的数据。 这些参数通常是可选的。 每个 GitHub REST API 终结点的文档将描述它支持的任何查询参数。 有关详细信息，请参阅 [GitHub REST API 文档](https://docs.github.com/zh/rest) 。

例如， [“列出公共事件”终结点](https://docs.github.com/zh/rest/activity/events#list-public-events) 默认返回 30 个问题。 可以使用 `per_page` 查询参数返回 2 个问题，而不是 30 个问题。 可以使用 `page` 查询参数仅提取结果的第一页。 有关使用查询参数的请求示例，请参阅 [发出请求](#making-a-request) 。

## 发出请求

本部分演示如何使用 GitHub 向 `curl` REST API 发出经过身份验证的请求。

### 1\. 设置

必须在计算机上安装 `curl` 。 要检查是否安装了 `curl` ，请在命令行中运行 `curl --version` 。

- 如果输出是有关 `curl` 版本的信息，则表示已安装 `curl` 。
- 如果收到类似 `command not found: curl` 的消息，则表示未安装 `curl` 。 下载并安装 `curl` 。 有关详细信息，请参阅 [curl 下载页面](https://curl.se/download.html) 。

### 2\. 为请求选择终结点

1. 选择要向其发出请求的终结点。 可以浏览 GitHub 的 [REST API 文档](https://docs.github.com/zh/rest) ，以发现可用于与 GitHub 交互的终结点。
2. 标识终结点的 HTTP 方法和路径。 您将发送这些内容与您的请求一起。 有关详细信息，请参阅 [HTTP 方法](#http-method) 和 [路径](#path) 。
	例如， [“创建问题”终结点](https://docs.github.com/zh/rest/issues/issues#create-an-issue) 使用 HTTP 方法和 `POST` 路径 `/repos/{owner}/{repo}/issues` 。
3. 标识任何必需的路径参数。 必需的路径参数显示在终结点路径的大括号 `{}` 中。 将每个参数占位符替换为想要的值。 有关详细信息，请参阅 [Path](#path) 。
	例如， [“创建问题”终结点](https://docs.github.com/zh/rest/issues/issues#create-an-issue) 使用路径 `/repos/{owner}/{repo}/issues` ，路径参数为 `{owner}` 和 `{repo}` 。 要在 API 请求中使用此路径，请将 `{repo}` 替换为想要创建新问题的存储库的名称，并将 `{owner}` 替换为存储库所有者帐户的名称。

### 3\. 创建身份验证凭据

创建访问令牌对请求进行身份验证。 可以保存令牌并将其用于多个请求。 为令牌提供访问终结点所需的任何作用域或权限。 将会在 `Authorization` 标头中与请求一起发送此令牌。 有关详细信息，请参阅 [身份验证](#authentication) 。

### 4\. 发出 curl 请求。

使用 `curl` 命令发出请求。 有关详细信息，请参阅 [curl 文档](https://curl.se/docs/manpage.html) 。

在请求中指定以下选项和值：

- **`--request` 或 `-X`** 后跟 HTTP 方法作为值。 有关更多信息，请参阅 [HTTP 方法](#http-method) 。
- **`--url`** 后跟完整路径作为值。 完整路径是一个 URL，包含 GitHub REST API 的基 URL（具体取决于访问 `https://api.github.com` ）以及端点路径，如下所示： `https://api.github.com/PATH`. 将 `PATH` 替换为终结点的路径。 有关详细信息，请参阅 [Path](#path) 。
	要使用查询参数，先在路径末尾添加 `?`，然后采用 `parameter_name=value` 形式追加查询参数名称和值。 使用 `&` 分隔多个查询参数。 如果需要在查询字符串中发送数组，请为每个数组项使用查询参数一次，并在查询参数名称后追加 `[]` 。 例如，要提供两个存储库 ID 的数组，请使用 `?repository_ids[]=REPOSITORY_A_ID&repository_ids[]=REPOSITORY_B_ID` 。 有关详细信息，请参阅 [查询参数](#query-parameters) 。 有关示例，请参阅 [使用查询参数的示例请求](#example-request-using-query-parameters-1) 。
- **`--header` 或 `-H` ：**
	- **`Accept` ：** 在 `Accept` 标头中传递媒体类型。 要在标头 `Accept` 中传递多个媒体类型，请使用逗号分隔媒体类型，例如： `Accept: application/vnd.github+json,application/vnd.github.diff` 。 有关详细信息，请参阅 [`Accept`](#accept) 和 [媒体类型](#media-types) 。
	- **`X-GitHub-Api-Version` ：** 在 `X-GitHub-Api-Version` 标头中传递 API 版本。 有关详细信息，请参阅 [`X-GitHub-Api-Version`](#x-github-api-version) 。
	- **`Authorization` ：** 在 `Authorization` 标头中传递身份验证令牌。 在大多数情况下，可以使用 `Authorization: Bearer` 或 `Authorization: token` 传递令牌。 但是，如果要传递 JSON Web 令牌 (JWT)，则必须使用 `Authorization: Bearer` 。 有关详细信息，请参阅 [身份验证](#authentication) 。 有关使用 `Authorization` 标头的请求示例，请参阅 [使用正文参数的示例请求](#example-request-using-body-parameters-1) 。
- **`--data` 或 `-d`** 后跟 JSON 对象中的任意主体参数。 如果不需要在请求中指定任何正文参数，请忽略此选项。 有关详细信息，请参阅 [正文参数](#body-parameters) 。 有关示例，请参阅 [使用正文参数的示例请求](#example-request-using-body-parameters-1) 。

#### 示例请求

以下示例请求使用 [“获取 Octocat”终结点](https://docs.github.com/zh/rest/meta/meta#get-octocat) 将 Octocat 返回为 ASCII 艺术。

```shell
curl --request GET \
--url "https://api.github.com/octocat" \
--header "Accept: application/vnd.github+json" \
--header "X-GitHub-Api-Version: 2022-11-28"
```

#### 使用查询参数的示例请求

[“列出公共事件”终结点](https://docs.github.com/zh/rest/activity/events#list-public-events) 默认返回 30 个问题。 以下示例使用 `per_page` 查询参数返回两个问题而不是 30 个，查询参数 `page` 仅提取结果的第一页。

```shell
curl --request GET \
--url "https://api.github.com/events?per_page=2&page=1" \
--header "Accept: application/vnd.github+json" \
--header "X-GitHub-Api-Version: 2022-11-28" \
  https://api.github.com/events
```

#### 使用正文参数的示例请求

以下示例使用 [“创建问题](https://docs.github.com/zh/rest/issues/issues#create-an-issue) 终结点”在指定的 存储库中创建新问题。 将您在上一步中创建的身份验证令牌替换 `YOUR-TOKEN` 。

```shell
curl \
--request POST \
--url "https://api.github.com/repos/octocat/Spoon-Knife/issues" \
--header "Accept: application/vnd.github+json" \
--header "X-GitHub-Api-Version: 2022-11-28" \
--header "Authorization: Bearer YOUR-TOKEN" \
--data '{
  "title": "Created with the REST API",
  "body": "This is a test issue created by the REST API"
}'
```

## 使用响应

发出请求后，API 会返回响应状态代码、响应头，并可能返回响应正文。

### 关于响应代码和标头

每个请求都会返回 HTTP 状态代码，以指示响应是否成功。 有关响应代码的详细信息，请参阅 [MDN HTTP 响应状态代码文档](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status) 。

此外，响应会包含标头，以提供有关响应的更多详细信息。 以 `X-` 或 `x-` 开头的标头是 GitHub 的自定义标头。 例如， `x-ratelimit-remaining` 和 `x-ratelimit-reset` 标头会告知你在一段时间内可以发出的请求数。

要查看状态代码和标头，请在发送请求时使用 `--include` 或 `--i` 选项。

例如，此请求获取指定的 存储库中的问题列表：

```shell
curl --request GET \
--url "https://api.github.com/repos/octocat/Spoon-Knife/issues?per_page=2" \
--header "Accept: application/vnd.github+json" \
--header "Authorization: Bearer YOUR-TOKEN" \
--include
```

它会返回如下所示的响应代码和标头：

```shell
HTTP/2 200
server: GitHub.com
date: Thu, 04 Aug 2022 20:07:51 GMT
content-type: application/json; charset=utf-8
cache-control: public, max-age=60, s-maxage=60
vary: Accept, Accept-Encoding, Accept, X-Requested-With
etag: W/"7fceb7e8c958d3ec4d02524b042578dcc7b282192e6c939070f4a70390962e18"
x-github-media-type: github.v3; format=json
link: <https://api.github.com/repositories/1300192/issues?per_page=2&sort=updated&direction=asc&page=2>; rel="next", <https://api.github.com/repositories/1300192/issues?per_page=2&sort=updated&direction=asc&page=7409>; rel="last"
access-control-expose-headers: ETag, Link, Location, Retry-After, X-RateLimit-Limit, X-RateLimit-Remaining, X-RateLimit-Used, X-RateLimit-Resource, X-RateLimit-Reset, X-OAuth-Scopes, X-Accepted-OAuth-Scopes, X-Poll-Interval, X-GitHub-Media-Type, X-GitHub-SSO, X-GitHub-Request-Id, Deprecation, Sunset
access-control-allow-origin: *
strict-transport-security: max-age=31536000; includeSubdomains; preload
x-frame-options: deny
x-content-type-options: nosniff
x-xss-protection: 0
referrer-policy: origin-when-cross-origin, strict-origin-when-cross-origin
content-security-policy: default-src 'none'
x-ratelimit-limit: 15000
x-ratelimit-remaining: 14996
x-ratelimit-reset: 1659645535
x-ratelimit-resource: core
x-ratelimit-used: 4
accept-ranges: bytes
content-length: 4936
x-github-request-id: 14E0:4BC6:F1B8BA:208E317:62EC2715
```

在此示例中，响应代码为 `200` ，指示请求成功。

### 关于响应正文

许多终结点会返回响应正文。 除非另外指定，否则响应正文会采用 JSON 格式。 以 `null` 的形式包含空白字段，而不是省略。 所有时间戳以 ISO 8601 格式返回 UTC 时间： `YYYY-MM-DDTHH:MM:SSZ` 。

与指定所需信息的 GraphQL API 不同，REST API 通常会返回比所需信息更多的信息。 如果需要，可以分析响应以拉取特定信息片段。

例如，可使用 `>` 将响应重定向到文件。 在以下示例中，将 `REPO-OWNER` 替换为拥有存储库的帐户的名称，并将 `REPO-NAME` 替换为存储库的名称。

```shell
curl --request GET \
--url "https://api.github.com/repos/REPO-OWNER/REPO-NAME/issues?per_page=2" \
--header "Accept: application/vnd.github+json" \
--header "Authorization: Bearer YOUR-TOKEN" > data.json
```

然后可以使用 jq 获取每个问题的标题和创建者 ID：

```shell
jq '.[] | {title: .title, authorID: .user.id}' data.json
```

前面两个命令返回类似于下面这样的内容：

```json
{
  "title": "Update index.html",
  "authorID": 10701255
}
{
  "title": "Edit index file",
  "authorID": 53709285
}
```

有关 jq 的详细信息，请参阅 [jq 文档](https://stedolan.github.io/jq/) 。

#### 详细表示形式与摘要表示形式

响应可以包含资源的所有属性，也可以仅包含属性的子集，具体取决于是提取单个资源还是资源列表。

- 提取具体某个存储库等这样的\_单个资源\_时，响应通常会包含该资源的所有属性。 这就是资源的“详细”表示形式。
- 提取\_资源列表\_（如多个存储库的列表）时，响应将仅包含每个资源的属性子集。 这就是资源的“摘要”表示形式。

请注意，授权有时会影响表示形式中包含的详细信息量。

这是因为某些属性的计算成本高昂，API 难以提供，因此 GitHub 将这些属性排除在摘要表示形式之外。 要获得这些属性，可以提取详细表示形式。

本文档提供每种 API 方法的示例响应。 示例响应说明了该方法返回的所有属性。

#### 超媒体

所有资源都可以具有一个或多个链接到其他资源的 `*_url` 属性。 这些属性旨在提供明确的 URL，使适当的 API 客户端不需要自己构建 URL。 强烈建议 API 客户端使用这些属性。 这样做有助于开发者未来更容易升级 API。 所有 URL 都应该是适当的 [RFC 6570](https://datatracker.ietf.org/doc/html/rfc6570) URI 模板。

然后，可以使用 [uri\_template](https://github.com/hannesg/uri_template) gem 之类的内容来扩展这些模板：

```ruby
>> tmpl = URITemplate.new('/notifications{?since,all,participating}')
>> tmpl.expand
=> "/notifications"

>> tmpl.expand all: 1
=> "/notifications?all=1"

>> tmpl.expand all: 1, participating: 1
=> "/notifications?all=1&participating=1"
```

## 速率限制

GitHub REST API 限制可以在给定时间段内发出的请求数。 有关速率限制以及如何检查当前速率限制状态的详细信息，请参阅 [REST API 的速率限制](https://docs.github.com/zh/rest/using-the-rest-api/rate-limits-for-the-rest-api) 。

## 后续步骤

本文演示了如何在存储库中列出和创建问题。 有关更多做法，请尝试对问题添加注释、编辑问题的标题或关闭问题。 有关详细信息，请参阅 [“创建问题注释”终结点](https://docs.github.com/zh/rest/issues/comments#create-an-issue-comment) 和 [“更新问题”终结点](https://docs.github.com/zh/rest/issues/issues#update-an-issue) 。

有关可使用的其他端点的详细信息，请参阅“ [REST 参考文档](https://docs.github.com/zh/rest) ”。