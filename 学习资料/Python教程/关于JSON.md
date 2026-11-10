# JSON 基础教程

[返回 json 教程首页](https://jsontoolbox.cn/json-tutorial)

很多人觉得 JSON 很简单，直到线上报了一个“Unexpected token”才发现自己踩了语法坑。JSON 的规则其实不多，但非常严格；把这些规则吃透，写配置、接口参数、Mock 数据会稳很多。

## 1. JSON 是什么

JSON 全称 JavaScript Object Notation（JavaScript 对象标记法），是一种基于文本的开放式数据交换格式，用来在系统之间描述和传递结构化数据。和 XML 相比，它写法更短、更轻量，也更容易阅读。

它的核心结构很少，主要由对象、数组和几种基础值组成。理解这几类结构，就能看懂绝大多数接口响应和配置文件。

## 2. 六种合法值类型

JSON 里只有对象、数组、字符串、数字、布尔值、null 这六类值。

```json
{
  "object": { "name": "json" },
  "array": [1, 2, 3],
  "string": "hello",
  "number": 3.14,
  "boolean": true,
  "nullValue": null
}
```

## 3. 对象、数组与嵌套结构

### 3.1 对象（Object）

对象用 `{}` 包裹，内部由键值对组成，键和值之间用冒号 `:` 分隔，字段之间用逗号分隔。**键必须是双引号包裹的字符串**，最后一个键值对后面不加逗号。字段值可以是字符串、数字、布尔值、null、对象或数组。

```json
{
  "id": 1001,
  "username": "alice",
  "enabled": true
}
```

### 3.2 数组（Array）

数组用 `[]` 包裹，元素之间用逗号分隔，元素可以是任意合法 JSON 值。数组常用于表示列表。

```json
[
  "apple",
  "banana",
  "orange"
]
```

### 3.3 嵌套结构（对象 + 数组）

接口返回里最常见的结构：外层对象 + 内层数组 + 子对象。

```json
{
  "page": 1,
  "pageSize": 20,
  "list": [
    { "id": 1, "name": "Keyboard" },
    { "id": 2, "name": "Mouse" }
  ],
  "hasMore": false
}
```

### 3.4 空数组、空对象和空值

`"list": []` 表示列表存在但暂时没有元素；`"list": null` 表示这个字段当前没有值；`{}` 表示一个空对象。三者含义不同，接口文档里应写清楚。

### 3.5 完整样例

```json
{
  "orderId": "SO-2026-0001",
  "status": "PAID",
  "amount": "199.00",
  "currency": "CNY",
  "customer": {
    "id": "U1024",
    "name": "Lin"
  },
  "items": [
    {
      "sku": "KB-65",
      "qty": 1
    }
  ],
  "createdAt": "2026-05-31T08:12:00Z"
}
```

> 注意 `amount` 用的是字符串而不是数字。金额、高精度数字和超长 ID 按字符串传输，可以避开跨语言的精度问题，见第 7 节。

## 4. 字符串转义

双引号和反斜杠必须转义；**单引号不需要转义**。JSON 只支持 `\"`、`\\`、`\/`、`\b`、`\f`、`\n`、`\r`、`\t`、`\uXXXX` 这些转义序列，写成 `\'` 之类的非法转义会直接报错。

```json
{
  "quote": "He said: \"JSON is strict\"",
  "path": "C:\\work\\demo",
  "lineBreak": "first line\nsecond line"
}
```

## 5. 数字写法

支持整数、小数、科学计数法；不支持 NaN、Infinity。

```json
{
  "int": 10,
  "float": 9.99,
  "negative": -42,
  "exp": 1.2e3
}
```

## 6. JSON 字符串与 JSON 对象

解析 JSON 有两个方向：把 JSON 字符串变成语言里的对象叫**反序列化**，把对象转成 JSON 字符串叫**序列化**。

JSON 字符串本质是一段文本，它看起来像对象，但程序还没有把它解析成可访问的数据结构。从接口、文件、localStorage、日志中拿到的数据，很多时候最开始都是 JSON 字符串。解析成对象后，才能通过属性读取字段。字符串适合传输和保存，对象适合在程序里读取、修改和计算。

以 JavaScript 为例，重点记住：`JSON.parse()` 的输入必须是字符串，输出才是可直接访问字段的对象。

```js
const jsonText = '{"userId":"U1001","active":true,"tags":["new","vip"]}'

const jsonObj = JSON.parse(jsonText)

console.log(jsonObj.userId)   // U1001
console.log(jsonObj.active)   // true
console.log(jsonObj.tags[0])  // new
```

### 6.1 多层嵌套的 JSON 字符串

有些系统会把一个 JSON 再塞进另一个 JSON 的字符串字段里，这时需要按层级多次 `JSON.parse()`。

```json
{
  "topic": "order.sync",
  "payload": "{\"orderId\":\"SO-1001\",\"buyer\":{\"id\":\"U200\"},\"meta\":\"{\\\"traceId\\\":\\\"t-900\\\",\\\"retry\\\":2}\"}"
}
```

```js
// 用 String.raw 保留反斜杠，否则模板字符串会先吃掉一层转义
const outerText = String.raw`{
  "topic": "order.sync",
  "payload": "{\"orderId\":\"SO-1001\",\"buyer\":{\"id\":\"U200\"},\"meta\":\"{\\\"traceId\\\":\\\"t-900\\\",\\\"retry\\\":2}\"}"
}`

const level1 = JSON.parse(outerText)       // 第一层对象
const level2 = JSON.parse(level1.payload)  // 第二层对象
const level3 = JSON.parse(level2.meta)     // 第三层对象

console.log(level1.topic)    // order.sync
console.log(level2.orderId)  // SO-1001
console.log(level3.traceId)  // t-900
```

- 先 parse 外层，再 parse 内层字符串字段。
- 看到大量 `\"` 就说明你在处理“字符串里的 JSON”。
- 如果你可以控制接口设计，优先直接返回对象，尽量避免多层字符串嵌套。

## 7. 新手常见错误

- **字段名没有使用双引号**：`{ name: "Lin" }` 非法，标准 JSON 要写成 `{ "name": "Lin" }`；单引号同样不行。
- **对象或数组最后多了逗号**：`{ "name": "Lin", }` 非法，最后一个键值对或元素后面不要加逗号。
- **把空数组和 null 混用**：`[]` 表示空列表，`null` 表示没有值，业务含义不同。接口文档中要明确字段为空时用 `[]`、`{}`、`null` 还是空字符串。
- **字符串没有反序列化就直接取字段**：先 `JSON.parse(text)` 再读属性，字符串不能直接当对象用。
- **数字、金额和 ID 类型不统一**：订单金额、超长 ID 在不同语言中用数字处理可能出现精度问题，按字符串传输更稳，例如 `"amount": "199.00"`。

## 8. 实战：写一个最小可用 API 响应

下面是一段可直接用于接口返回的 JSON，可以复制到解析器里验证。

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "userId": "U10086",
    "nickname": "tom",
    "roles": ["admin", "editor"]
  },
  "requestId": "req-20260531-0001"
}
```

## 9. 快速自检

用在线解析工具验证比写脚本更快，也更直观。

1. 打开 [json 在线解析](https://jsontoolbox.cn/) 或 [json 格式化](https://jsontoolbox.cn/json-formatter)。
2. 先粘贴“合法示例”，确认显示 JSON 有效。
3. 再粘贴“故意错误示例”，观察报错位置和报错信息。

**合法示例（应解析成功）**

```json
{
  "name": "json",
  "version": 1,
  "enabled": true
}
```

**故意错误示例（应提示报错：`"version"` 后面多了尾逗号）**

```json
{
  "name": "json",
  "version": 1,
}
```

## 10. 常用语言 JSON 解析方法

| 语言 | 反序列化 / 解析 | 序列化 / 转字符串 | 说明 |
| --- | --- | --- | --- |
| JavaScript | `JSON.parse()` | `JSON.stringify()` | 浏览器和 Node.js 原生支持。 |
| Python | `json.loads()` | `json.dumps()` | 标准库 `json` 模块直接支持。 |
| PHP | `json_decode()` | `json_encode()` | 接口开发和后端模板项目中常见。 |
| Java | Jackson / Gson | Jackson / Gson | 常用于 Spring Boot 接口入参和响应。 |
| Go | `json.Unmarshal()` | `json.Marshal()` | 标准库 `encoding/json`。 |
| Rust | `serde_json::from_str()` | `serde_json::to_string()` | 常配合 serde 使用。 |
| C++ | nlohmann/json 等库 | `dump()` | 通常依赖第三方 JSON 库。 |

## 11. 对比 XML：优势与不足

**JSON 的优势**

- 简单：主要使用大括号、中括号、冒号和逗号，结构直观。
- 快捷：文本更短，接口传输时通常比 XML 更省空间。
- 易于理解：字段和值一眼能看出对应关系，新手上手快。
- 适合前后端协作：JavaScript 可以直接解析，其他语言也普遍支持。

**JSON 的不足**

- **不能添加注释**：标准 JSON 不支持注释。很多新手把 `//` 或 `/* */` 写进 JSON，结果解析时报 “Unexpected token”。配置文件如果必须说明字段含义，建议放到文档、README、JSON Schema 或额外的说明字段中。
- **不算最高效的数据格式**：JSON 是文本格式，可读性很好，但解析和传输效率并不是最高。极高吞吐、强类型、低延迟场景下，Protobuf、MessagePack 等二进制格式可能更合适。不过对大多数 Web 接口、后台管理系统、配置文件和调试场景来说，JSON 的可读性和通用性仍然非常划算。

## 12. 发展历程

| 时间 | 事件 | 意义 |
| --- | --- | --- |
| 1995 | Brendan Eich 创建 JavaScript | 后来的 JSON 借用了 JavaScript 对象字面量的写法，语法形态由此而来。 |
| 1999 | ECMA-262 第三版发布 | json.org 明确提到 JSON 基于该标准的一个子集。 |
| 2001 前后 | Douglas Crockford 推动 JSON 成形 | 在 State Software 工作期间整理、命名并推广这种轻量格式；Chip Morningstar 也是早期团队的关键人物。 |
| 2002 | json.org 上线 | 注册域名并发布 JSON 说明，有了稳定的公开文档入口。 |
| 2005 前后 | AJAX 和 Web API 兴起 | 前端需要频繁与服务端交换数据，JSON 因体积小、解析方便快速流行。 |
| 2006 | RFC 4627 发布 | 作者 Douglas Crockford，JSON 获得正式的互联网标准说明，`application/json` 媒体类型被明确。 |
| 2009 | `JSON.parse()` / `JSON.stringify()` 进入 ECMAScript 5 | JavaScript 原生支持解析与序列化，浏览器里可直接处理。 |
| 2013 | ECMA-404 第一版发布 | JSON 语法进一步标准化，跨语言、跨平台使用更稳定。 |
| 2014 | RFC 7159 更新 JSON 说明 | 对可接受的 JSON 文本和互操作细节做进一步整理。 |
| 2017 | RFC 8259 与 ECMA-404 第二版发布 | RFC 8259 成为新的互联网标准参考，标准描述更加统一。 |
| 2020 前后 | JSON Schema、OpenAPI 与云原生配置普及 | 接口文档、参数校验、自动化测试、配置管理中成为常用基础设施。 |

如今在 REST API、前端请求响应、配置文件、日志数据、低代码平台和测试工具中，JSON 已经是默认选择之一。

## 13. 哪些地方会遇到 JSON

- 测试接口：接口测试工具里填写 JSON Body，查看 JSON 响应。
- REST API：前后端使用 JSON 传递用户、订单、支付、商品等数据。
- 对接接口：第三方开放平台通常用 JSON 描述请求参数和返回结果。
- 开发者模式网络面板：浏览器 DevTools 的 Network 中，请求与响应经常是 JSON。
- 配置文件：很多项目使用 `package.json`、`tsconfig.json`、`appsettings.json` 等 JSON 配置。
- 日志和埋点：服务端日志、前端埋点、数据上报也常用 JSON 表达结构化数据。

## 写在最后

记住一句话：JSON 规则少，但非常严格。只要坚持“双引号、无尾逗号、无注释、值类型合法”，就已经超过很多初学者了。

技术成长不只在于会用工具，也在于能把概念讲清楚、把边界写清楚。当你能把数据结构、接口约定和错误原因说明白，工程判断力也会慢慢长出来。
