---
title: "快速上手 — Requests 2.34.2 文档"
source: "https://requests.readthedocs.io/en/latest/user/quickstart/"
author:
published:
created: 2026-11-06
description:
tags:
  - "clippings"
---
## 快速上手

迫不及待想开始了吗？本页会很好地介绍如何开始使用 Requests。

首先，请确认：

- Requests 已[安装](https://requests.readthedocs.io/en/latest/user/install/#install)
- Requests 是[最新版本](https://requests.readthedocs.io/en/latest/community/updates/#updates)

让我们从几个简单的示例开始。

## 发起请求

用 Requests 发起请求非常简单。

首先导入 Requests 模块：

```
>>> import requests
```

现在，我们来试着获取一个网页。在这个例子中，我们获取 GitHub 的公共时间线：

```
>>> r = requests.get('https://api.github.com/events')
```

现在，我们得到了一个名为 `r` 的 [`Response`](https://requests.readthedocs.io/en/latest/api/#requests.Response "requests.Response") 对象。我们可以从这个对象中获取所需的全部信息。

Requests 简洁的 API 让各种形式的 HTTP 请求都同样一目了然。例如，发起一个 HTTP POST 请求是这样的：

```
>>> r = requests.post('https://httpbin.org/post', data={'key': 'value'})
```

很不错，对吧？那其他 HTTP 请求类型呢：PUT、DELETE、HEAD 和 OPTIONS？它们也都同样简单：

```
>>> r = requests.put('https://httpbin.org/put', data={'key': 'value'})
>>> r = requests.delete('https://httpbin.org/delete')
>>> r = requests.head('https://httpbin.org/get')
>>> r = requests.options('https://httpbin.org/get')
```

这些固然不错，但也只是 Requests 能力的开端。

## 在 URL 中传递参数

你常常需要在 URL 的查询字符串中发送某种数据。如果手工构造 URL，这些数据会以键/值对的形式出现在 URL 中问号之后，例如 `httpbin.org/get?key=val`。Requests 允许你通过 `params` 关键字参数，以字符串字典的形式提供这些参数。举例来说，如果你想向 `httpbin.org/get` 传递 `key1=value1` 和 `key2=value2`，可以这样写：

```
>>> payload = {'key1': 'value1', 'key2': 'value2'}
>>> r = requests.get('https://httpbin.org/get', params=payload)
```

打印这个 URL，就能看到它已被正确编码：

```
>>> print(r.url)
https://httpbin.org/get?key2=value2&key1=value1
```

注意，字典中值为 `None` 的键不会被添加到 URL 的查询字符串中。

也可以将列表作为值传入：

```
>>> payload = {'key1': 'value1', 'key2': ['value2', 'value3']}

>>> r = requests.get('https://httpbin.org/get', params=payload)
>>> print(r.url)
https://httpbin.org/get?key1=value1&key2=value2&key2=value3
```

## 响应内容

我们可以读取服务器响应的内容。再来看看 GitHub 时间线：

```
>>> import requests

>>> r = requests.get('https://api.github.com/events')
>>> r.text
'[{"repository":{"open_issues":0,"url":"https://github.com/...
```

Requests 会自动解码来自服务器的内容。大多数 unicode 字符集都能无缝解码。

发起请求时，Requests 会根据 HTTP 头对响应的编码做出合理推测。访问 `r.text` 时使用的就是 Requests 推测出的文本编码。你可以通过 `r.encoding` 属性查看 Requests 当前使用的编码，也可以修改它：

```
>>> r.encoding
'utf-8'
>>> r.encoding = 'ISO-8859-1'
```

如果修改了编码，之后每次调用 `r.text` 时，Requests 都会使用 `r.encoding` 的新值。凡是可以通过特殊逻辑推断出内容编码的场景，你都可能想这样做。例如，HTML 和 XML 可以在自身正文中指定编码。在这种情况下，应当先用 `r.content` 找出编码，再设置 `r.encoding`。这样就能以正确的编码使用 `r.text`。

如果你需要自定义编码，Requests 同样支持。若你创建了自己的编码并已通过 `codecs` 模块注册，只需把该编解码器的名称作为 `r.encoding` 的值，Requests 就会替你完成解码。

## 二进制响应内容

对于非文本请求，你也可以以字节形式访问响应体：

```
>>> r.content
b'[{"repository":{"open_issues":0,"url":"https://github.com/...
```

`gzip` 和 `deflate` 传输编码会自动为你解码。

如果安装了 [brotli](https://pypi.org/project/brotli) 或 [brotlicffi](https://pypi.org/project/brotlicffi) 之类的 Brotli 库，`br` 传输编码也会自动为你解码。

例如，要用请求返回的二进制数据创建图像，可以使用以下代码：

```
>>> from PIL import Image
>>> from io import BytesIO

>>> i = Image.open(BytesIO(r.content))
```

## JSON 响应内容

如果要处理 JSON 数据，还内置了 JSON 解码器：

```
>>> import requests

>>> r = requests.get('https://api.github.com/events')
>>> r.json()
[{'repository': {'open_issues': 0, 'url': 'https://github.com/...
```

如果 JSON 解码失败，`r.json()` 会抛出异常。例如，响应为 204（No Content），或响应中包含无效 JSON 时，尝试调用 `r.json()` 会抛出 `requests.exceptions.JSONDecodeError`。这个包装异常为不同 python 版本和 json 序列化库可能抛出的多种异常提供了互操作性。

需要注意的是，`r.json()` 调用成功**并不**表示响应成功。有些服务器会在失败的响应中返回 JSON 对象（例如 HTTP 500 时附带错误详情）。这类 JSON 仍会被解码并返回。要确认请求是否成功，请使用 `r.raise_for_status()`，或检查 `r.status_code` 是否符合预期。

## 原始响应内容

在极少数情况下，如果你想获取来自服务器的原始套接字响应，可以访问 `r.raw`。若要这样做，请务必在最初的请求中设置 `stream=True`。设置之后，就可以这样操作：

```
>>> r = requests.get('https://api.github.com/events', stream=True)

>>> r.raw
<urllib3.response.HTTPResponse object at 0x101194810>

>>> r.raw.read(10)
b'\x1f\x8b\x08\x00\x00\x00\x00\x00\x00\x03'
```

不过，通常应当用下面这样的方式把流式传输的内容保存到文件：

```
with open(filename, 'wb') as fd:
    for chunk in r.iter_content(chunk_size=128):
        fd.write(chunk)
```

使用 `Response.iter_content` 会替你处理掉直接使用 `Response.raw` 时需要自行处理的许多事情。在流式下载时，上面这种是获取内容的首选且推荐的方式。注意，`chunk_size` 可以自由调整成更适合你使用场景的数值。

> [!note] 注意
> 关于使用 `Response.iter_content` 还是 `Response.raw`，有一点很重要。`Response.iter_content` 会自动解码 `gzip` 和 `deflate` 传输编码。`Response.raw` 是原始的字节流——它不会对响应内容做任何转换。如果你确实需要访问原样返回的字节，请使用 `Response.raw`。

## 自定义请求头

如果想给请求添加 HTTP 请求头，只需向 `headers` 参数传入一个 `dict`。

例如，在前面的示例中我们没有指定 user-agent：

```
>>> url = 'https://api.github.com/some/endpoint'
>>> headers = {'user-agent': 'my-app/0.0.1'}

>>> r = requests.get(url, headers=headers)
```

注意：自定义请求头的优先级低于更具体的信息来源。例如：

- 通过 `headers=` 设置的 Authorization 请求头，如果 `.netrc` 中指定了凭据，就会被覆盖；而 `.netrc` 中的凭据又会被 `auth=` 参数覆盖。Requests 会在 ~/.netrc、~/\_netrc 或 NETRC 环境变量指定的路径下查找 netrc 文件。详情请查看 [netrc 认证](https://requests.readthedocs.io/en/latest/user/authentication/#authentication)。
- 如果请求被重定向到其他主机，Authorization 请求头会被移除。
- Proxy-Authorization 请求头会被 URL 中提供的代理凭据覆盖。
- 当我们能够确定内容长度时，Content-Length 请求头会被覆盖。

此外，Requests 完全不会因为指定了哪些自定义请求头而改变自身行为。这些请求头只是被原样传入最终的请求。

注意：所有请求头的值都必须是 `string`、bytestring 或 unicode。虽然允许，但建议避免传入 unicode 型的请求头值。

## 更复杂的 POST 请求

通常你会想发送一些表单编码的数据——就像 HTML 表单那样。要做到这一点，只需向 `data` 参数传入一个字典。发起请求时，你的数据字典会被自动进行表单编码：

```
>>> payload = {'key1': 'value1', 'key2': 'value2'}

>>> r = requests.post('https://httpbin.org/post', data=payload)
>>> print(r.text)
{
  ...
  "form": {
    "key2": "value2",
    "key1": "value1"
  },
  ...
}
```

`data` 参数也可以让每个键对应多个值。做法是把 `data` 设为元组列表，或者设为以列表作为值的字典。当表单中有多个元素使用同一个键时，这一点尤其有用：

```
>>> payload_tuples = [('key1', 'value1'), ('key1', 'value2')]
>>> r1 = requests.post('https://httpbin.org/post', data=payload_tuples)
>>> payload_dict = {'key1': ['value1', 'value2']}
>>> r2 = requests.post('https://httpbin.org/post', data=payload_dict)
>>> print(r1.text)
{
  ...
  "form": {
    "key1": [
      "value1",
      "value2"
    ]
  },
  ...
}
>>> # httpbin may embed non-deterministic metadata,
>>> # so we only compare our submitted data here.
>>> r1.json()['form'] == r2.json()['form']
True
```

有时你可能想发送未经表单编码的数据。如果传入的是 `string` 而不是 `dict`，该数据会被直接提交。

例如，GitHub API v3 接受 JSON 编码的 POST/PATCH 数据：

```
>>> import json

>>> url = 'https://api.github.com/some/endpoint'
>>> payload = {'some': 'data'}

>>> r = requests.post(url, data=json.dumps(payload))
```

请注意，上面的代码不会添加 `Content-Type` 请求头（尤其是不会把它设为 `application/json`）。

如果你需要设置该请求头，又不想自己编码 `dict`，也可以直接用 `json` 参数（2.4.2 版本新增）传入，它会被自动编码：

```
>>> url = 'https://api.github.com/some/endpoint'
>>> payload = {'some': 'data'}
```

```
>>> r = requests.post(url, json=payload)
```

注意，如果传入了 `data` 或 `files`，`json` 参数会被忽略。

## 发送 Multipart 编码文件的 POST 请求

Requests 让上传 Multipart 编码文件变得很简单：

```
>>> url = 'https://httpbin.org/post'
>>> files = {'file': open('report.xls', 'rb')}

>>> r = requests.post(url, files=files)
>>> r.text
{
  ...
  "files": {
    "file": "<censored...binary...data>"
  },
  ...
}
```

你可以显式设置文件名、content\_type 和 headers：

```
>>> url = 'https://httpbin.org/post'
>>> files = {'file': ('report.xls', open('report.xls', 'rb'), 'application/vnd.ms-excel', {'Expires': '0'})}

>>> r = requests.post(url, files=files)
>>> r.text
{
  ...
  "files": {
    "file": "<censored...binary...data>"
  },
  ...
}
```

如果愿意，也可以发送字符串，让它们作为文件被接收：

```
>>> url = 'https://httpbin.org/post'
>>> files = {'file': ('report.csv', 'some,data,to,send\nanother,row,to,send\n')}

>>> r = requests.post(url, files=files)
>>> r.text
{
  ...
  "files": {
    "file": "some,data,to,send\\nanother,row,to,send\\n"
  },
  ...
}
```

如果要以 `multipart/form-data` 请求 POST 一个非常大的文件，你可能希望以流式方式发送请求。默认情况下 `requests` 并不支持这一点，但有一个独立的包可以做到——`requests-toolbelt`。关于如何使用它，你应该阅读 [toolbelt 的文档](https://toolbelt.readthedocs.io/) 了解更多细节。

关于在一次请求中发送多个文件，请参阅[高级用法](https://requests.readthedocs.io/en/latest/user/advanced/#advanced)章节。

> [!warning] 警告
> 强烈建议以[二进制模式](https://docs.python.org/3/tutorial/inputoutput.html#tut-files "(in Python v3.15)")打开文件。这是因为 Requests 可能会尝试为你提供 `Content-Length` 请求头，一旦如此，该值会被设为文件的*字节*数。如果以*文本模式*打开文件，可能会出错。

## 响应状态码

我们可以查看响应状态码：

```
>>> r = requests.get('https://httpbin.org/get')
>>> r.status_code
200
```

Requests 还内置了一个状态码查询对象，方便查阅：

```
>>> r.status_code == requests.codes.ok
True
```

如果发出了错误的请求（返回 4XX 客户端错误或 5XX 服务器错误响应），可以用 [`Response.raise_for_status()`](https://requests.readthedocs.io/en/latest/api/#requests.Response.raise_for_status "requests.Response.raise_for_status") 抛出异常：

```
>>> bad_r = requests.get('https://httpbin.org/status/404')
>>> bad_r.status_code
404

>>> bad_r.raise_for_status()
Traceback (most recent call last):
  File "requests/models.py", line 832, in raise_for_status
    raise http_error
requests.exceptions.HTTPError: 404 Client Error
```

不过，由于 `r` 的 `status_code` 是 `200`，调用 `raise_for_status()` 时我们得到：

```
>>> r.raise_for_status()
None
```

一切正常。

## 响应头

我们可以用一个 Python 字典查看服务器的响应头：

```
>>> r.headers
{
    'content-encoding': 'gzip',
    'transfer-encoding': 'chunked',
    'connection': 'close',
    'server': 'nginx/1.0.4',
    'x-runtime': '148ms',
    'etag': '"e1ca502697e5c9317743dc078f67693f"',
    'content-type': 'application/json'
}
```

不过这个字典很特殊：它是专为 HTTP 请求头打造的。根据 [RFC 7230](https://tools.ietf.org/html/rfc7230#section-3.2)，HTTP Header 名称不区分大小写。

因此，我们可以用任意大小写形式访问这些请求头：

```
>>> r.headers['Content-Type']
'application/json'

>>> r.headers.get('content-type')
'application/json'
```

它的另一个特殊之处在于：服务器可能多次发送同一个标头且值各不相同，但 requests 会把它们合并，以便按照 [RFC 7230](https://tools.ietf.org/html/rfc7230#section-3.2) 的规定，在字典中用单个映射来表示：

> 接收者可以将多个同名的首部字段合并为一个“field-name: field-value”对，方法是将后续的每个字段值按顺序以逗号分隔追加到合并后的字段值中，而不改变消息的语义。

## Cookies

如果响应中包含一些 Cookies，你可以快速访问它们：

```
>>> url = 'http://example.com/some/cookie/setting/url'
>>> r = requests.get(url)

>>> r.cookies['example_cookie_name']
'example_cookie_value'
```

要把自己的 cookies 发送给服务器，可以使用 `cookies` 参数：

```
>>> url = 'https://httpbin.org/cookies'
>>> cookies = dict(cookies_are='working')

>>> r = requests.get(url, cookies=cookies)
>>> r.text
'{"cookies": {"cookies_are": "working"}}'
```

Cookies 以 [`RequestsCookieJar`](https://requests.readthedocs.io/en/latest/api/#requests.cookies.RequestsCookieJar "requests.cookies.RequestsCookieJar") 的形式返回，它的行为类似 `dict`，但还提供了更完整的接口，适合跨多个域名或路径使用。Cookie jar 也可以传入请求：

```
>>> jar = requests.cookies.RequestsCookieJar()
>>> jar.set('tasty_cookie', 'yum', domain='httpbin.org', path='/cookies')
>>> jar.set('gross_cookie', 'blech', domain='httpbin.org', path='/elsewhere')
>>> url = 'https://httpbin.org/cookies'
>>> r = requests.get(url, cookies=jar)
>>> r.text
'{"cookies": {"tasty_cookie": "yum"}}'
```

## 重定向与历史

默认情况下，Requests 会对除 HEAD 以外的所有请求方法执行地址重定向。

我们可以用 Response 对象的 `history` 属性来跟踪重定向。

[`Response.history`](https://requests.readthedocs.io/en/latest/api/#requests.Response.history "requests.Response.history") 列表包含为完成该请求而创建的 [`Response`](https://requests.readthedocs.io/en/latest/api/#requests.Response "requests.Response") 对象。列表按从最早到最新的响应排序。

例如，GitHub 会把所有 HTTP 请求重定向到 HTTPS：

```
>>> r = requests.get('http://github.com/')

>>> r.url
'https://github.com/'

>>> r.status_code
200

>>> r.history
[<Response [301]>]
```

如果使用的是 GET、OPTIONS、POST、PUT、PATCH 或 DELETE，可以通过 `allow_redirects` 参数禁用重定向处理：

```
>>> r = requests.get('http://github.com/', allow_redirects=False)

>>> r.status_code
301

>>> r.history
[]
```

如果使用的是 HEAD，也可以启用重定向：

```
>>> r = requests.head('http://github.com/', allow_redirects=True)

>>> r.url
'https://github.com/'

>>> r.history
[<Response [301]>]
```

## 超时

通过 `timeout` 参数，你可以让 Requests 在等待指定秒数后停止等待响应。几乎所有生产代码都应当在几乎所有请求中使用这个参数。否则可能导致程序无限期挂起：

```
>>> requests.get('https://github.com/', timeout=0.001)
Traceback (most recent call last):
  File "<stdin>", line 1, in <module>
requests.exceptions.Timeout: HTTPConnectionPool(host='github.com', port=80): Request timed out. (timeout=0.001)
```

> [!note] 注意
> `timeout` 并不是整个响应下载过程的时限；更确切地说，如果服务器在 `timeout` 秒内没有给出响应（更准确地说，是底层套接字在 `timeout` 秒内没有收到任何字节），就会抛出异常。如果没有显式指定超时，请求就不会超时。

## 错误与异常

出现网络问题（例如 DNS 故障、连接被拒绝等）时，Requests 会抛出 [`ConnectionError`](https://requests.readthedocs.io/en/latest/api/#requests.ConnectionError "requests.exceptions.ConnectionError") 异常。

如果 HTTP 请求返回了表示失败的状态码，[`Response.raise_for_status()`](https://requests.readthedocs.io/en/latest/api/#requests.Response.raise_for_status "requests.Response.raise_for_status") 会抛出 [`HTTPError`](https://requests.readthedocs.io/en/latest/api/#requests.HTTPError "requests.exceptions.HTTPError")。

如果请求超时，会抛出 [`Timeout`](https://requests.readthedocs.io/en/latest/api/#requests.Timeout "requests.exceptions.Timeout") 异常。

如果请求超过了所配置的最大重定向次数，会抛出 [`TooManyRedirects`](https://requests.readthedocs.io/en/latest/api/#requests.TooManyRedirects "requests.exceptions.TooManyRedirects") 异常。

Requests 显式抛出的所有异常都继承自 [`requests.exceptions.RequestException`](https://requests.readthedocs.io/en/latest/api/#requests.RequestException "requests.exceptions.RequestException")。

---

还想了解更多？请查看[高级用法](https://requests.readthedocs.io/en/latest/user/advanced/#advanced)章节。
