## 高级用法

本文介绍 Requests 的一些更高级特性。

## 会话对象

Session 对象让你可以在多次请求之间保留某些参数。它还会在该 Session 实例发出的所有请求之间保留 cookies，并使用 `urllib3` 的[连接池](https://urllib3.readthedocs.io/en/latest/reference/urllib3.connectionpool.html)。因此，如果你要向同一个主机发起多次请求，底层的 TCP 连接会被复用，这可能带来显著的性能提升（参见 [HTTP 持久连接](https://en.wikipedia.org/wiki/HTTP_persistent_connection)）。

Session 对象拥有 Requests 主 API 的全部方法。

让我们在多次请求之间保留一些 cookies：

```
s = requests.Session()

s.get('https://httpbin.org/cookies/set/sessioncookie/123456789')
r = s.get('https://httpbin.org/cookies')

print(r.text)
# '{"cookies": {"sessioncookie": "123456789"}}'
```

Session 也可以用来为请求方法提供默认数据。做法是给 Session 对象上的属性赋值：

```
s = requests.Session()
s.auth = ('user', 'pass')
s.headers.update({'x-test': 'true'})

# both 'x-test' and 'x-test2' are sent
s.get('https://httpbin.org/headers', headers={'x-test2': 'true'})
```

你传给请求方法的任何字典都会与已设置的会话级值合并。方法级参数会覆盖会话级参数。

但请注意，即使使用了会话，方法级参数*也不会*在多次请求之间保留。下面这个例子只会在第一次请求中发送 cookies，第二次不会：

```
s = requests.Session()

r = s.get('https://httpbin.org/cookies', cookies={'from-my': 'browser'})
print(r.text)
# '{"cookies": {"from-my": "browser"}}'

r = s.get('https://httpbin.org/cookies')
print(r.text)
# '{"cookies": {}}'
```

如果想手动向会话添加 cookies，可以使用 [Cookie 工具函数](https://requests.readthedocs.io/en/latest/api/#api-cookies) 来操作 [`Session.cookies`](https://requests.readthedocs.io/en/latest/api/#requests.Session.cookies "requests.Session.cookies")。

Session 也可以作为上下文管理器使用：

```
with requests.Session() as s:
    s.get('https://httpbin.org/cookies/set/sessioncookie/123456789')
```

这样能确保一旦退出 `with` 块，会话就会被关闭，即使发生了未处理的异常。

> [!note] 从字典参数中移除某个值
> 有时你会希望从字典参数中略去会话级的键。要做到这一点，只需在方法级参数中把该键的值设为 `None`。它会被自动略去。

会话中包含的所有值都可以直接供你使用。更多信息请参阅 [Session API 文档](https://requests.readthedocs.io/en/latest/api/#sessionapi)。

## Request 与 Response 对象

每当调用 `requests.get()` 及同类方法时，你其实做了两件主要的事。第一，构造一个 `Request` 对象，它会被发往服务器以请求或查询某个资源。第二，当 Requests 从服务器拿回响应后，会生成一个 `Response` 对象。`Response` 对象包含服务器返回的全部信息，也包含你最初创建的那个 `Request` 对象。下面是一个简单的请求，用来从 Wikipedia 的服务器获取一些非常重要的信息：

```
>>> r = requests.get('https://en.wikipedia.org/wiki/Monty_Python')
```

如果想访问服务器返回给我们的响应头，可以这样做：

```
>>> r.headers
{'content-length': '56170', 'x-content-type-options': 'nosniff', 'x-cache':
'HIT from cp1006.eqiad.wmnet, MISS from cp1010.eqiad.wmnet', 'content-encoding':
'gzip', 'age': '3080', 'content-language': 'en', 'vary': 'Accept-Encoding,Cookie',
'server': 'Apache', 'last-modified': 'Wed, 13 Jun 2012 01:33:50 GMT',
'connection': 'close', 'cache-control': 'private, s-maxage=0, max-age=0,
must-revalidate', 'date': 'Thu, 14 Jun 2012 12:59:39 GMT', 'content-type':
'text/html; charset=UTF-8', 'x-cache-lookup': 'HIT from cp1006.eqiad.wmnet:3128,
MISS from cp1010.eqiad.wmnet:80'}
```

不过，如果想拿到我们发给服务器的请求头，只需先访问 request，再访问它的 headers：

```
>>> r.request.headers
{'Accept-Encoding': 'identity, deflate, compress, gzip',
'Accept': '*/*', 'User-Agent': 'python-requests/1.2.0'}
```

## 预备请求

无论通过 API 调用还是 Session 调用拿到 [`Response`](https://requests.readthedocs.io/en/latest/api/#requests.Response "requests.Response") 对象，它的 `request` 属性实际上就是所用到的 `PreparedRequest`。在某些情况下，你可能希望在发送请求前对请求体或请求头（其实任何东西都可以）再做些处理。最简单的做法如下：

```
from requests import Request, Session

s = Session()

req = Request('POST', url, data=data, headers=headers)
prepped = req.prepare()

# do something with prepped.body
prepped.body = 'No, I want exactly this as the body.'

# do something with prepped.headers
del prepped.headers['Content-Type']

resp = s.send(prepped,
    stream=stream,
    verify=verify,
    proxies=proxies,
    cert=cert,
    timeout=timeout
)

print(resp.status_code)
```

由于对 `Request` 对象没有特殊处理，你直接把它预备好，然后修改 `PreparedRequest` 对象。随后连同原本会传给 `requests.*` 或 `Session.*` 的其他参数一起发送出去。

不过，上面的代码会失去使用 Requests [`Session`](https://requests.readthedocs.io/en/latest/api/#requests.Session "requests.Session") 对象的一部分好处。尤其是 cookies 之类的 [`Session`](https://requests.readthedocs.io/en/latest/api/#requests.Session "requests.Session") 级状态不会被应用到你的请求上。要得到已应用这些状态的 [`PreparedRequest`](https://requests.readthedocs.io/en/latest/api/#requests.PreparedRequest "requests.PreparedRequest")，把对 [`Request.prepare()`](https://requests.readthedocs.io/en/latest/api/#requests.Request.prepare "requests.Request.prepare") 的调用换成对 [`Session.prepare_request()`](https://requests.readthedocs.io/en/latest/api/#requests.Session.prepare_request "requests.Session.prepare_request") 的调用即可，像这样：

```
from requests import Request, Session

s = Session()
req = Request('GET',  url, data=data, headers=headers)

prepped = s.prepare_request(req)

# do something with prepped.body
prepped.body = 'Seriously, send exactly these bytes.'

# do something with prepped.headers
prepped.headers['Keep-Dead'] = 'parrot'

resp = s.send(prepped,
    stream=stream,
    verify=verify,
    proxies=proxies,
    cert=cert,
    timeout=timeout
)

print(resp.status_code)
```

使用预备请求流程时，请记住它不会考虑环境。如果你用环境变量来改变 requests 的行为，就可能引发问题。例如：`REQUESTS_CA_BUNDLE` 中指定的自签名 SSL 证书不会被考虑在内，结果会抛出 `SSL: CERTIFICATE_VERIFY_FAILED`。要绕开这一行为，可以显式地把环境设置合并进会话：

```
from requests import Request, Session

s = Session()
req = Request('GET', url)

prepped = s.prepare_request(req)

# Merge environment settings into session
settings = s.merge_environment_settings(prepped.url, {}, None, None, None)
resp = s.send(prepped, **settings)

print(resp.status_code)
```

## SSL 证书验证

与浏览器一样，Requests 会为 HTTPS 请求验证 SSL 证书。默认情况下 SSL 验证是开启的，如果无法验证证书，Requests 会抛出 SSLError：

```
>>> requests.get('https://requestb.in')
requests.exceptions.SSLError: hostname 'requestb.in' doesn't match either of '*.herokuapp.com', 'herokuapp.com'
```

我在这个域名上没有配置 SSL，所以它抛出了异常。很好。GitHub 倒是配置了：

```
>>> requests.get('https://github.com')
<Response [200]>
```

你可以给 `verify` 传入一个 CA\_BUNDLE 文件的路径，或者包含受信任 CA 证书的目录：

```
>>> requests.get('https://github.com', verify='/path/to/certfile')
```

或者持久生效：

```
s = requests.Session()
s.verify = '/path/to/certfile'
```

> [!note] 注意
> 如果 `verify` 设为某个目录的路径，该目录必须已用 OpenSSL 附带的 `c_rehash` 工具处理过。

这份受信任 CA 列表也可以通过 `REQUESTS_CA_BUNDLE` 环境变量指定。如果未设置 `REQUESTS_CA_BUNDLE`，则回退使用 `CURL_CA_BUNDLE`。

把 `verify` 设为 False，Requests 也可以忽略 SSL 证书验证：

```
>>> requests.get('https://kennethreitz.org', verify=False)
<Response [200]>
```

注意，当 `verify` 设为 `False` 时，requests 会接受服务器出示的任何 TLS 证书，并忽略主机名不匹配和/或证书过期，这会让你的应用容易遭受中间人（MitM）攻击。在本地开发或测试期间，把 verify 设为 `False` 可能有用。

默认情况下 `verify` 为 True。`verify` 选项只作用于主机证书。

## 客户端证书

你也可以指定一个本地证书作为客户端证书，既可以是单个文件（同时包含私钥和证书），也可以是由两个文件路径组成的元组：

```
>>> requests.get('https://kennethreitz.org', cert=('/path/client.cert', '/path/client.key'))
<Response [200]>
```

或者持久生效：

```
s = requests.Session()
s.cert = '/path/client.cert'
```

如果指定了错误的路径或无效的证书，就会得到 SSLError：

```
>>> requests.get('https://kennethreitz.org', cert='/wrong_path/client.pem')
SSLError: [Errno 336265225] _ssl.c:347: error:140B0009:SSL routines:SSL_CTX_use_PrivateKey_file:PEM lib
```

> [!warning] 警告
> 本地证书的私钥*必须*是未加密的。目前 Requests 不支持使用加密的密钥。

## CA 证书

Requests 使用 [certifi](https://certifiio.readthedocs.io/) 包中的证书。这样用户无需改动 Requests 的版本就能更新受信任的证书。

在 2.16 版本之前，Requests 自带一组它信任的根 CA，来源于 [Mozilla 信任库](https://hg.mozilla.org/mozilla-central/raw-file/tip/security/nss/lib/ckfw/builtins/certdata.txt)。这些证书每个 Requests 版本只更新一次。如果未安装 `certifi`，使用明显较旧的 Requests 版本时就会导致证书包极其过时。

出于安全考虑，我们建议经常升级 certifi！

## 响应体内容的工作流程

默认情况下，发起请求时会立即下载响应体。你可以用 `stream` 参数改变这一行为，把响应体的下载推迟到访问 [`Response.content`](https://requests.readthedocs.io/en/latest/api/#requests.Response.content "requests.Response.content") 属性时：

```
tarball_url = 'https://github.com/psf/requests/tarball/main'
r = requests.get(tarball_url, stream=True)
```

此时只下载了响应头，连接仍保持打开，因此可以让我们有条件地获取内容：

```
if int(r.headers['content-length']) < TOO_LONG:
  content = r.content
  ...
```

还可以借助 [`Response.iter_content()`](https://requests.readthedocs.io/en/latest/api/#requests.Response.iter_content "requests.Response.iter_content") 和 [`Response.iter_lines()`](https://requests.readthedocs.io/en/latest/api/#requests.Response.iter_lines "requests.Response.iter_lines") 方法进一步控制这一流程。或者，也可以读取底层 urllib3 [`urllib3.HTTPResponse`](https://urllib3.readthedocs.io/en/latest/reference/urllib3.response.html#urllib3.response.HTTPResponse "(in urllib3 v2.8.1.dev31)") 中未经解码的响应体，该对象位于 [`Response.raw`](https://requests.readthedocs.io/en/latest/api/#requests.Response.raw "requests.Response.raw")。

如果发起请求时把 `stream` 设为 `True`，除非你读完所有数据或调用 [`Response.close`](https://requests.readthedocs.io/en/latest/api/#requests.Response.close "requests.Response.close")，否则 Requests 无法把连接释放回连接池。这会导致连接使用效率低下。如果在使用 `stream=True` 时发现自己只读取了部分请求体（或根本没读），应当把请求放在 `with` 语句中，以确保连接总会被关闭：

```
with requests.get('https://httpbin.org/get', stream=True) as r:
    # Do things with the response here.
```

## Keep-Alive

好消息——多亏了 urllib3，会话内的 keep-alive 是 100% 自动的！你在同一个会话中发出的任何请求都会自动复用合适的连接！

注意，只有在所有响应体数据都被读取后，连接才会释放回连接池以供复用；务必把 `stream` 设为 `False`，或者读取 `Response` 对象的 `content` 属性。

## 流式上传

Requests 支持流式上传，可以发送很大的数据流或文件，而无需把它们读入内存。要流式上传，只需为请求体提供一个类文件对象：

```
with open('massive-body', 'rb') as f:
    requests.post('http://some.url/streamed', data=f)
```

> [!warning] 警告
> 强烈建议以[二进制模式](https://docs.python.org/3/tutorial/inputoutput.html#tut-files "(in Python v3.15)")打开文件。这是因为 Requests 可能会尝试为你提供 `Content-Length` 请求头，一旦如此，该值会被设为文件的*字节*数。如果以*文本模式*打开文件，可能会出错。

## 分块编码请求

Requests 对发出和收到的请求都支持 Chunked 传输编码。要发送分块编码的请求，只需为请求体提供一个生成器（或任何没有长度的迭代器）：

```
def gen():
    yield 'hi'
    yield 'there'

requests.post('http://some.url/chunked', data=gen())
```

对于分块编码的响应，最好用 [`Response.iter_content()`](https://requests.readthedocs.io/en/latest/api/#requests.Response.iter_content "requests.Response.iter_content") 来迭代数据。理想情况下你已在请求上设置了 `stream=True`，这样把 `iter_content` 的 `chunk_size` 参数设为 `None` 就可以逐块迭代。如果想给块大小设上限，可以把 `chunk_size` 参数设为任意整数。

## 一次 POST 多个 Multipart 编码文件

你可以在一次请求中发送多个文件。例如，假设你要把图片文件上传到某个 HTML 表单中名为 ‘images’ 的多文件字段：

```
<input type="file" name="images" multiple="true" required="true"/>
```

要做到这一点，只需把 files 设为由 `(form_field_name, file_info)` 组成的元组列表：

```
>>> url = 'https://httpbin.org/post'
>>> multiple_files = [
...     ('images', ('foo.png', open('foo.png', 'rb'), 'image/png')),
...     ('images', ('bar.png', open('bar.png', 'rb'), 'image/png'))]
>>> r = requests.post(url, files=multiple_files)
>>> r.text
{
  ...
  'files': {'images': 'data:image/png;base64,iVBORw ....'}
  'Content-Type': 'multipart/form-data; boundary=3131623adb2043caaeb5538cc7aa0b3a',
  ...
}
```

> [!warning] 警告
> 强烈建议以[二进制模式](https://docs.python.org/3/tutorial/inputoutput.html#tut-files "(in Python v3.15)")打开文件。这是因为 Requests 可能会尝试为你提供 `Content-Length` 请求头，一旦如此，该值会被设为文件的*字节*数。如果以*文本模式*打开文件，可能会出错。

## 事件钩子

Requests 提供了一套钩子系统，你可以用它来操纵请求过程中的某些环节，或者发出事件处理信号。

可用的钩子：

`response`：

由 Request 生成的响应。

把 `{hook_name: callback_function}` 字典传给请求的 `hooks` 参数，就可以按单次请求指定钩子函数：

```
hooks={'response': print_url}
```

该 `callback_function` 的第一个参数会收到一块数据。

```
def print_url(r, *args, **kwargs):
    print(r.url)
```

你的回调函数必须自行处理异常。任何未处理的异常都不会被悄悄放过，因此应当由调用 Requests 的代码来处理。

如果回调函数返回了一个值，就认为它是要替换传入的数据。如果函数什么都不返回，则不会产生其他影响。

```
def record_hook(r, *args, **kwargs):
    r.hook_called = True
    return r
```

让我们在运行时打印一些请求方法的参数：

```
>>> requests.get('https://httpbin.org/', hooks={'response': print_url})
https://httpbin.org/
<Response [200]>
```

可以给单次请求添加多个钩子。我们一次调用两个钩子：

```
>>> r = requests.get('https://httpbin.org/', hooks={'response': [print_url, record_hook]})
https://httpbin.org/
>>> r.hook_called
True
```

也可以给 `Session` 实例添加钩子。添加的任何钩子随后会在该会话发出的每个请求上被调用。例如：

```
>>> s = requests.Session()
>>> s.hooks['response'].append(print_url)
>>> s.get('https://httpbin.org/')
 https://httpbin.org/
 <Response [200]>
```

一个 `Session` 可以有多个钩子，它们会按添加顺序被调用。

## 自定义认证

Requests 允许你指定自己的认证机制。

任何作为 `auth` 参数传给请求方法的可调用对象，都有机会在请求被发出之前修改它。

认证实现都是 [`AuthBase`](https://requests.readthedocs.io/en/latest/api/#requests.auth.AuthBase "requests.auth.AuthBase") 的子类，定义起来很容易。Requests 在 `requests.auth` 中提供了两种常见认证方案的实现：[`HTTPBasicAuth`](https://requests.readthedocs.io/en/latest/api/#requests.auth.HTTPBasicAuth "requests.auth.HTTPBasicAuth") 和 [`HTTPDigestAuth`](https://requests.readthedocs.io/en/latest/api/#requests.auth.HTTPDigestAuth "requests.auth.HTTPDigestAuth")。

假设有这样一个 Web 服务：只有当 `X-Pizza` 请求头被设为某个密码值时才会响应。不太可能，但姑且就这么设定。

```
from requests.auth import AuthBase

class PizzaAuth(AuthBase):
    """Attaches HTTP Pizza Authentication to the given Request object."""
    def __init__(self, username):
        # setup any auth-related data here
        self.username = username

    def __call__(self, r):
        # modify and return the request
        r.headers['X-Pizza'] = self.username
        return r
```

然后，就可以用我们的 Pizza Auth 发起请求：

```
>>> requests.get('http://pizzabin.org/admin', auth=PizzaAuth('kenneth'))
<Response [200]>
```

## 流式请求

借助 [`Response.iter_lines()`](https://requests.readthedocs.io/en/latest/api/#requests.Response.iter_lines "requests.Response.iter_lines")，你可以轻松迭代 [Twitter Streaming API](https://dev.twitter.com/streaming/overview) 之类的流式 API。只需把 `stream` 设为 `True`，再用 [`iter_lines`](https://requests.readthedocs.io/en/latest/api/#requests.Response.iter_lines "requests.Response.iter_lines") 迭代响应：

```
import json
import requests

r = requests.get('https://httpbin.org/stream/20', stream=True)

for line in r.iter_lines():

    # filter out keep-alive new lines
    if line:
        decoded_line = line.decode('utf-8')
        print(json.loads(decoded_line))
```

在 [`Response.iter_lines()`](https://requests.readthedocs.io/en/latest/api/#requests.Response.iter_lines "requests.Response.iter_lines") 或 [`Response.iter_content()`](https://requests.readthedocs.io/en/latest/api/#requests.Response.iter_content "requests.Response.iter_content") 中使用 decode\_unicode=True 时，若服务器没有提供编码，你会希望准备一个回退编码：

```
r = requests.get('https://httpbin.org/stream/20', stream=True)

if r.encoding is None:
    r.encoding = 'utf-8'

for line in r.iter_lines(decode_unicode=True):
    if line:
        print(json.loads(line))
```

> [!warning] 警告
> [`iter_lines`](https://requests.readthedocs.io/en/latest/api/#requests.Response.iter_lines "requests.Response.iter_lines") 不是可重入安全的。多次调用这个方法会导致部分已接收数据丢失。如果需要从多处调用它，请改用返回的迭代器对象：
> 
> ```
> lines = r.iter_lines()
> # Save the first line for later or just skip it
> 
> first_line = next(lines)
> 
> for line in lines:
>     print(line)
> ```

## 代理

如果需要使用代理，可以通过任意请求方法的 `proxies` 参数来配置单次请求：

```
import requests

proxies = {
  'http': 'http://10.10.1.10:3128',
  'https': 'http://10.10.1.10:1080',
}

requests.get('http://example.org', proxies=proxies)
```

也可以为整个 [`Session`](https://requests.readthedocs.io/en/latest/api/#requests.Session "requests.Session") 一次性配置：

```
import requests

proxies = {
  'http': 'http://10.10.1.10:3128',
  'https': 'http://10.10.1.10:1080',
}
session = requests.Session()
session.proxies.update(proxies)

session.get('http://example.org')
```

> [!warning] 警告
> 设置 `session.proxies` 的表现可能与预期不同。所提供的值会被环境代理（即 [urllib.request.getproxies](https://docs.python.org/3/library/urllib.request.html#urllib.request.getproxies) 返回的那些）覆盖。要在存在环境代理的情况下确保使用代理，请按上文最初说明的那样，在所有单次请求上显式指定 `proxies` 参数。
> 
> 详情参见 [#2018](https://github.com/psf/requests/issues/2018)。

如果没有像上面那样按请求覆盖代理配置，Requests 会依赖标准环境变量 `http_proxy`、`https_proxy`、`no_proxy` 和 `all_proxy` 所定义的代理配置。这些变量的大写形式同样受支持。因此你可以设置它们来配置 Requests（只设置与你需求相关的那些）：

```
$ export HTTP_PROXY="http://10.10.1.10:3128"
$ export HTTPS_PROXY="http://10.10.1.10:1080"
$ export ALL_PROXY="socks5://10.10.1.10:3434"

$ python
>>> import requests
>>> requests.get('http://example.org')
```

要为代理使用 HTTP Basic Auth，可在上述任意配置项中使用 http://user:password@host/ 这种写法：

```
$ export HTTPS_PROXY="http://user:pass@10.10.1.10:1080"

$ python
>>> proxies = {'http': 'http://user:pass@10.10.1.10:3128/'}
```

> [!warning] 警告
> 把敏感的用户名和密码信息存放在环境变量或纳入版本控制的文件中存在安全风险，强烈不建议这样做。

要为特定的协议和主机指定代理，键使用 scheme://hostname 形式。这会匹配发往该协议和该确切主机名的所有请求。

```
proxies = {'http://10.20.1.128': 'http://10.10.1.10:5323'}
```

注意，代理 URL 必须包含协议。

最后要注意，为 https 连接使用代理，通常需要你的本机信任该代理的根证书。默认情况下，Requests 信任的证书列表可以这样查到：

```
from requests.utils import DEFAULT_CA_BUNDLE_PATH
print(DEFAULT_CA_BUNDLE_PATH)
```

把 `REQUESTS_CA_BUNDLE`（或 `CURL_CA_BUNDLE`）环境变量设为另一个文件路径，即可覆盖这个默认证书包：

```
$ export REQUESTS_CA_BUNDLE="/usr/local/myproxy_info/cacert.pem"
$ export https_proxy="http://10.10.1.10:1080"

$ python
>>> import requests
>>> requests.get('https://example.org')
```

### SOCKS

2.10.0 版本新增。

除了基本的 HTTP 代理，Requests 也支持使用 SOCKS 协议的代理。这是一项可选功能，使用前需要额外安装第三方库。

这项功能所需的依赖可以通过 `pip` 获取：

```
$ python -m pip install 'requests[socks]'
```

装好这些依赖后，使用 SOCKS 代理就和使用 HTTP 代理一样简单：

```
proxies = {
    'http': 'socks5://user:pass@host:port',
    'https': 'socks5://user:pass@host:port'
}
```

使用 `socks5` 协议会让 DNS 解析发生在客户端，而不是代理服务器上。这与 curl 一致——它用协议来决定在客户端还是代理上做 DNS 解析。如果希望在代理服务器上解析域名，就用 `socks5h` 作为协议。

## 规范遵从

Requests 力求在所有相关规范和 RFC 上保持遵从，前提是这样的遵从不会给用户带来麻烦。这种对规范的较真，可能会带来一些不熟悉相关规范的人看来不太寻常的行为。

### 编码

收到响应时，Requests 会推测用哪种编码来解码响应，以便你访问 [`Response.text`](https://requests.readthedocs.io/en/latest/api/#requests.Response.text "requests.Response.text") 属性。Requests 会先检查 HTTP 头中有没有指定编码；如果没有，就用 [charset\_normalizer](https://pypi.org/project/charset_normalizer/) 或 [chardet](https://github.com/chardet/chardet) 尝试推测编码。

如果安装了 `chardet`，`requests` 就会使用它；不过在 python3 上，`chardet` 已不再是必需的依赖。`chardet` 库采用 LGPL 许可，而有些 requests 用户不能依赖强制性的 LGPL 许可依赖。

如果安装 `requests` 时没有指定 `[use_chardet_on_py3]` extra，且尚未安装 `chardet`，`requests` 会用 `charset-normalizer`（MIT 许可）来推测编码。

Requests 唯一不会推测编码的情况是：HTTP 头中没有显式给出 charset，**并且** `Content-Type` 头中包含 `text`。在这种情况下，[RFC 2616](https://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.7.1) 规定默认 charset 必须是 `ISO-8859-1`。Requests 在此遵循规范。如果你需要别的编码，可以手动设置 [`Response.encoding`](https://requests.readthedocs.io/en/latest/api/#requests.Response.encoding "requests.Response.encoding") 属性，或者使用原始的 [`Response.content`](https://requests.readthedocs.io/en/latest/api/#requests.Response.content "requests.Response.content")。

## HTTP 动词

Requests 几乎支持全部 HTTP 动词：GET、OPTIONS、HEAD、POST、PUT、PATCH 和 DELETE。下面以 GitHub API 为例，详细展示在 Requests 中使用这些动词的方法。

先从最常用的动词开始：GET。HTTP GET 是幂等方法，返回给定 URL 上的资源。因此，当你想从某个 Web 地址取回数据时，就该用这个动词。一个典型用法是从 GitHub 获取某次特定提交的信息。假设我们想取 Requests 上的 `a050faf` 这次提交，可以这样获取：

```
>>> import requests
>>> r = requests.get('https://api.github.com/repos/psf/requests/git/commits/a050faf084662f3a352dd1a941f2c7c9f886d4ad')
```

先确认 GitHub 是否正确响应了。如果响应正常，我们还想弄清内容是什么类型。可以这样做：

```
>>> if r.status_code == requests.codes.ok:
...     print(r.headers['content-type'])
...
application/json; charset=utf-8
```

可见 GitHub 返回的是 JSON。很好，可以用 [`r.json`](https://requests.readthedocs.io/en/latest/api/#requests.Response.json "requests.Response.json") 方法把它解析成 Python 对象。

```
>>> commit_data = r.json()

>>> print(commit_data.keys())
['committer', 'author', 'url', 'tree', 'sha', 'parents', 'message']

>>> print(commit_data['committer'])
{'date': '2012-05-10T11:10:50-07:00', 'email': 'me@kennethreitz.com', 'name': 'Kenneth Reitz'}

>>> print(commit_data['message'])
makin' history
```

到目前为止都很简单。好吧，我们稍微探究一下 GitHub API。本来可以去查文档，但用 Requests 来玩会更有意思。我们可以利用 Requests 的 OPTIONS 动词，看看刚才用的 url 支持哪些 HTTP 方法。

```
>>> verbs = requests.options(r.url)
>>> verbs.status_code
500
```

呃，什么？这可没什么用！原来 GitHub 和许多 API 提供方一样，并没有真正实现 OPTIONS 方法。这个疏漏挺烦人，不过没关系，我们老老实实查文档就好。不过，假如 GitHub 正确实现了 OPTIONS，它应该在响应头里返回允许的方法，例如：

```
>>> verbs = requests.options('http://a-good-website.com/api/cats')
>>> print(verbs.headers['allow'])
GET,HEAD,POST,OPTIONS
```

翻看文档可以看到，commits 上唯一另外允许的方法是 POST，用来创建新提交。既然用的是 Requests 仓库，我们大概不该对它乱发 POST。不如去玩玩 GitHub 的 Issues 功能吧。

这篇文档正是为回应 [Issue #482](https://github.com/psf/requests/issues/482) 而添加的。既然这个 issue 已经存在，我们就拿它当例子。先把它取回来。

```
>>> r = requests.get('https://api.github.com/repos/psf/requests/issues/482')
>>> r.status_code
200

>>> issue = json.loads(r.text)

>>> print(issue['title'])
Feature any http verb in docs

>>> print(issue['comments'])
3
```

不错，有三条评论。我们看看最后一条。

```
>>> r = requests.get(r.url + '/comments')
>>> r.status_code
200

>>> comments = r.json()

>>> print(comments[0].keys())
['body', 'url', 'created_at', 'updated_at', 'user', 'id']

>>> print(comments[2]['body'])
Probably in the "advanced" section
```

嗯，这个地方听起来有点傻。我们发条评论告诉发帖的人他很傻吧。话说发帖的是谁来着？

```
>>> print(comments[2]['user']['login'])
kennethreitz
```

好，那就告诉这位 Kenneth，我们觉得这个例子应该放到快速上手指南里。按 GitHub API 文档，做法是向这个帖子 POST。来吧。

```
>>> body = json.dumps({u"body": u"Sounds great! I'll get right on it!"})
>>> url = u"https://api.github.com/repos/psf/requests/issues/482/comments"

>>> r = requests.post(url=url, data=body)
>>> r.status_code
404
```

咦，真奇怪。大概需要认证吧。那会很麻烦，对吧？错。Requests 让多种认证方式都用起来很简单，包括非常常见的 Basic Auth。

```
>>> from requests.auth import HTTPBasicAuth
>>> auth = HTTPBasicAuth('fake@example.com', 'not_a_real_password')

>>> r = requests.post(url=url, data=body, auth=auth)
>>> r.status_code
201

>>> content = r.json()
>>> print(content['body'])
Sounds great! I'll get right on it.
```

漂亮。哦，等等，不对！我本来还想说要花点时间，因为我得去喂猫。要是能编辑这条评论就好了！幸好 GitHub 允许用另一个 HTTP 动词 PATCH 来编辑评论。就这么办。

```
>>> print(content[u"id"])
5804413

>>> body = json.dumps({u"body": u"Sounds great! I'll get right on it once I feed my cat."})
>>> url = u"https://api.github.com/repos/psf/requests/issues/comments/5804413"

>>> r = requests.patch(url=url, data=body, auth=auth)
>>> r.status_code
200
```

很好。现在，为了折腾一下这位 Kenneth，我决定让他干着急，不告诉他我正在做这件事。也就是说，我想把这条评论删掉。GitHub 允许我们用名字贴切到不行的 DELETE 方法来删除评论。把它清掉吧。

```
>>> r = requests.delete(url=url, auth=auth)
>>> r.status_code
204
>>> r.headers['status']
'204 No Content'
```

很好，全没了。最后我还想知道自己的 ratelimit 用掉了多少。来查一下。GitHub 把这类信息放在响应头里，所以与其下载整个页面，不如发一个 HEAD 请求只拿响应头。

```
>>> r = requests.head(url=url, auth=auth)
>>> print(r.headers)
...
'x-ratelimit-remaining': '4995'
'x-ratelimit-limit': '5000'
...
```

很好。是时候写个 Python 程序，用各种刺激的方式再把 GitHub API 折腾 4995 次了。

## 自定义动词

有时你会碰到这样一类服务器：出于某种原因，它允许甚至要求使用上文没提到的 HTTP 动词。比如某些 WEBDAV 服务器使用的 MKCOL 方法。别发愁，这些照样能在 Requests 里用。它们借助内置的 `.request` 方法。例如：

```
>>> r = requests.request('MKCOL', url, data=data)
>>> r.status_code
200 # Assuming your call was correct
```

利用这一点，你可以使用服务器允许的任何方法动词。

## Link 响应头

许多 HTTP API 都带有 Link 响应头。它们让 API 更具自描述性，也更容易被探索。

例如 GitHub 在自家 API 中用它们来做[分页](https://docs.github.com/en/rest/guides/using-pagination-in-the-rest-api)：

```
>>> url = 'https://api.github.com/users/kennethreitz/repos?page=1&per_page=10'
>>> r = requests.head(url=url)
>>> r.headers['link']
'<https://api.github.com/users/kennethreitz/repos?page=2&per_page=10>; rel="next", <https://api.github.com/users/kennethreitz/repos?page=6&per_page=10>; rel="last"'
```

Requests 会自动解析这些 Link 响应头，让它们易于使用：

```
>>> r.links["next"]
{'url': 'https://api.github.com/users/kennethreitz/repos?page=2&per_page=10', 'rel': 'next'}

>>> r.links["last"]
{'url': 'https://api.github.com/users/kennethreitz/repos?page=7&per_page=10', 'rel': 'last'}
```

## 传输适配器

从 v1.0.0 起，Requests 转向了基于传输适配器的模块化内部设计。这些对象提供一种机制，用来定义与某个 HTTP 服务交互的方式。尤其是，它们让你可以针对每个服务应用各自的配置。

Requests 自带一个传输适配器：[`HTTPAdapter`](https://requests.readthedocs.io/en/latest/api/#requests.adapters.HTTPAdapter "requests.adapters.HTTPAdapter")。它借助强大的 [urllib3](https://github.com/urllib3/urllib3) 库，提供 Requests 与 HTTP、HTTPS 交互的默认实现。每当初始化一个 Requests [`Session`](https://requests.readthedocs.io/en/latest/api/#requests.Session "requests.Session") 时，都会有一个用于 HTTP、一个用于 HTTPS 的适配器挂到该 [`Session`](https://requests.readthedocs.io/en/latest/api/#requests.Session "requests.Session") 对象上。

Requests 允许用户创建并使用自己的传输适配器，以提供特定功能。创建好之后，可以把传输适配器挂载到 Session 对象上，同时指明它应当作用于哪些 Web 服务。

```
>>> s = requests.Session()
>>> s.mount('https://github.com/', MyAdapter())
```

mount 调用会把传输适配器的某个具体实例注册到一个前缀上。挂载之后，凡是通过该会话发出、URL 以这个前缀开头的 HTTP 请求，都会使用这个传输适配器。

> [!note] 注意
> 适配器按最长前缀匹配来选择。注意，`http://localhost` 这样的前缀也会匹配 `http://localhost.other.com` 或 `http://localhost@other.com`。建议在完整主机名后面加上 `/`。

实现传输适配器的诸多细节超出了本文档的范围，不过可以看下面这个简单的 SSL 用例示例。若还不满足，可以考虑继承 [`BaseAdapter`](https://requests.readthedocs.io/en/latest/api/#requests.adapters.BaseAdapter "requests.adapters.BaseAdapter")。

### 示例：指定 SSL 版本

Requests 团队特意选择了直接采用底层库（[urllib3](https://github.com/urllib3/urllib3)）默认的 SSL 版本。通常这没问题，但有时你可能需要连接某个服务端点，而它使用的版本与默认版本不兼容。

为此可以使用传输适配器：沿用 HTTPAdapter 现有的大部分实现，再加上一个会透传给 urllib3 的参数 *ssl\_version*。我们来做一个让库使用 SSLv3 的传输适配器：

```
import ssl
from urllib3.poolmanager import PoolManager

from requests.adapters import HTTPAdapter

class Ssl3HttpAdapter(HTTPAdapter):
    """"Transport adapter" that allows us to use SSLv3."""

    def init_poolmanager(self, connections, maxsize, block=False):
        self.poolmanager = PoolManager(
            num_pools=connections, maxsize=maxsize,
            block=block, ssl_version=ssl.PROTOCOL_SSLv3)
```

### 示例：自动重试

默认情况下，Requests 不会重试失败的连接。不过，在 Requests [`Session`](https://requests.readthedocs.io/en/latest/api/#requests.Session "requests.Session") 中借助 [urllib3.util.Retry](https://urllib3.readthedocs.io/en/stable/reference/urllib3.util.html#urllib3.util.Retry) 类，可以实现自动重试，还能用上包括退避在内的一系列强大特性：

```
from urllib3.util import Retry
from requests import Session
from requests.adapters import HTTPAdapter

s = Session()
retries = Retry(
    total=3,
    backoff_factor=0.1,
    status_forcelist=[502, 503, 504],
    allowed_methods={'POST'},
)
s.mount('https://', HTTPAdapter(max_retries=retries))
```

## 阻塞还是非阻塞？

使用默认传输适配器时，Requests 不提供任何形式的非阻塞 IO。[`Response.content`](https://requests.readthedocs.io/en/latest/api/#requests.Response.content "requests.Response.content") 属性会一直阻塞，直到整个响应下载完毕。如果需要更细的粒度，本库的流式功能（参见 ）可以让你每次只取回少量响应数据。不过，这些调用依然是阻塞的。

如果你在意阻塞 IO 的问题，外面有不少把 Requests 与 Python 某个异步框架结合起来的项目。很好的例子包括 [requests-threads](https://github.com/requests/requests-threads)、[grequests](https://github.com/spyoungtech/grequests)、[requests-futures](https://github.com/ross/requests-futures) 和 [httpx](https://github.com/encode/httpx)。

## 超时

大多数发往外部服务器的请求都应当带上超时，以防服务器迟迟不响应。默认情况下，除非显式设置超时值，否则请求不会超时。没有超时，你的代码可能挂起几分钟甚至更久。

**connect** 超时是 Requests 等待客户端与远程主机建立连接的秒数（对应套接字上的 [connect()](https://linux.die.net/man/2/connect) 调用）。把连接超时设为略大于 3 的倍数是个好习惯，3 是默认的 [TCP 数据包重传窗口](https://datatracker.ietf.org/doc/html/rfc2988)。

客户端连上服务器并发出 HTTP 请求之后，**read** 超时就是客户端等待服务器发出响应的秒数。（确切地说，是客户端在服务器发来的字节*之间*等待的秒数。在 99.9% 的情况下，这就是服务器发出第一个字节之前的等待时间。）

如果给超时指定单个值，像这样：

```
r = requests.get('https://github.com', timeout=5)
```

这个超时值会同时应用于 `connect` 和 `read` 超时。如果想分别设置这两个值，就传入一个元组：

```
r = requests.get('https://github.com', timeout=(3.05, 27))
```

如果远程服务器非常慢，可以把超时值设为 None，让 Requests 无限期等待响应，然后去拿杯咖啡。

```
r = requests.get('https://github.com', timeout=None)
```

> [!note] 注意
> 连接超时作用于对某个 IP 地址的每一次连接尝试。如果一个域名对应多个地址，底层的 `urllib3` 会依次尝试每个地址，直到有一个连上为止。这可能导致实际的总连接超时是设定时长的*数倍*，例如一台同时拥有 IPv4 和 IPv6 地址且无响应的服务器，其感知到的超时会*翻倍*，设置连接超时时要把这一点考虑进去。

> [!note] 注意
> 连接超时和读取超时都不是 [wall clock](https://wiki.php.net/rfc/max_execution_wall_time)。这意味着，如果你发起一个请求时看一眼时间，等请求完成或超时时再看一眼时间，真实经过的时间可能比你设定的要长。