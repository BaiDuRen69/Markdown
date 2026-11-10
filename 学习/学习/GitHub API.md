
## 查看用户

### 查看自己

#### [Get the authenticated user 获取已认证用户信息](https://docs.github.com/zh/rest/users/users#get-the-authenticated-user)

OAuth app tokens and personal access tokens (classic) need the `read:user` scope, or the broader `user` scope, for this endpoint to return the private user response. The private user response includes additional fields such as `private_gists`, `total_private_repos`, `owned_private_repos`, `disk_usage`, `collaborators`, and `two_factor_authentication`. Tokens without these scopes receive the public user response.  
>OAuth应用令牌和个人访问令牌（经典版）需要具备`read:user`或更高权限的`user`作用域，才能通过此端点返回用户的私有响应。私有响应包含额外字段，如`private_gists`、`total_private_repos`、`owned_private_repos`、`disk_usage`、`collaborators`和`two_factor_authentication`。若令牌不具备这些作用域，则只能获取公共用户响应。

The private and public user response types are unrelated to the [private profile](https://docs.github.com/account-and-profile/concepts/personal-profile#private-profiles) setting. A token without scopes still authenticates as the token's owner, so values subject to private profile visibility, such as `followers` and `following`, may differ from an unauthenticated response.  
>私有与公共用户响应类型与“私密个人资料”设置无关。即使没有作用域的令牌仍会认证为令牌所有者，因此受私密资料可见性限制的信息（如关注者和关注列表）可能与非认证响应不同。

For enterprise-managed users, if their email is unverified, this endpoint will return a placeholder email in the format `email+shortcode@domain.com` instead of their actual email. This placeholder email [may cause issues with some third-party apps](https://github.blog/changelog/2024-08-01-enterprise-managed-user-accounts-now-require-email-verification/). Once the user has [verified their email address](https://docs.github.com/account-and-profile/how-tos/email-preferences/verifying-your-email-address), the endpoint will return their real email.  
>对于企业管理的用户，如果其邮箱未验证，此端点将返回格式为`email+shortcode@domain.com`的占位邮箱，而非真实邮箱。此占位邮箱可能导致部分第三方应用出现异常。待用户完成邮箱验证后，端点将返回真实邮箱地址。

[“Get the authenticated user”的细粒度访问令牌](https://docs.github.com/zh/rest/users/users#get-the-authenticated-user--fine-grained-access-tokens)

此端点支持以下精细令牌类型:

- [GitHub应用用户访问令牌](https://docs.github.com/zh/apps/creating-github-apps/authenticating-with-a-github-app/generating-a-user-access-token-for-a-github-app)
- [精细化的个人访问令牌](https://docs.github.com/zh/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens#creating-a-fine-grained-personal-access-token)

细粒度令牌不需要任何权限。

[“Get the authenticated user”的 HTTP 响应状态代码](https://docs.github.com/zh/rest/users/users#get-the-authenticated-user--status-codes)

|状态代码|说明|
|---|---|
|`200`|OK|
|`304`|Not modified  <br>未修改|
|`401`|Requires authentication  <br>需要身份验证|
|`403`|Forbidden  <br>禁止访问|

```cURL
curl -L \
  -H "Accept: application/vnd.github+json" \
  -H "X-GitHub-Api-Version: 2026-03-10" \
  https://api.github.com/user
```


### [Get a user 获取用户](https://docs.github.com/zh/rest/users/users#get-a-user)

Provides publicly available information about someone with a GitHub account.  
>提供有关拥有 GitHub 账户之人的公开可用信息。

If the specified user has a [private profile](https://docs.github.com/account-and-profile/concepts/personal-profile#private-profiles), the `followers` and `following` values are `0` unless the request is authenticated as that user. A request authenticated as the specified user returns the actual values even if the token has no OAuth scopes.  
>如果指定的用户具有私密个人资料，则在未经该用户认证的情况下，关注者和关注值均为 0。以该用户身份认证的请求将返回实际值，即使令牌没有任何 OAuth 范围。

The `events_url` value is a URI template. Replace `{/privacy}` with `/public` to retrieve only public events. Omit it to retrieve public events and, when authenticated as the user, private events. For more information, see "[List events for the authenticated user](https://docs.github.com/rest/activity/events#list-events-for-the-authenticated-user)."  
>events_url 值是一个 URI 模板。将 {/privacy} 替换为 /public 以仅检索公共事件。省略它以检索公共事件，以及在用户认证下检索私有事件。有关更多信息，请参阅“列出已认证用户的事件”。

If you are requesting information about an [Enterprise Managed User](https://docs.github.com/enterprise-cloud@latest/admin/managing-iam/understanding-iam-for-enterprises/about-enterprise-managed-users), or a GitHub App bot that is installed in an organization that uses Enterprise Managed Users, your requests must be authenticated as a user or GitHub App that has access to the organization to view that account's information. If you are not authorized, the request will return a `404 Not Found` status.  
>如果您正在请求有关企业管理用户或安装在采用企业管理用户组织的 GitHub App 机器人的信息，则您的请求必须作为有组织访问权限的用户或 GitHub App 进行身份验证以查看该账户的信息。如果未经授权，请求将返回 404 Not Found 状态。

The `email` key in the following response is the publicly visible email address from your GitHub [profile page](https://github.com/settings/profile). When setting up your profile, you can select a primary email address to be public which provides an email entry for this endpoint. If you do not set a public email address for `email`, then it will have a value of `null`. You only see publicly visible email addresses when authenticated with GitHub. For more information, see [Authentication](https://docs.github.com/rest/guides/getting-started-with-the-rest-api#authentication).  
>以下响应中的 email 键是您在 GitHub 个人资料页面上公开显示的电子邮件地址。在设置个人资料时，您可以选择主电子邮件地址公开，从而为此端点提供一个电子邮件条目。如果未为电子邮件设置公开电子邮件地址，则其值为 null。只有通过 GitHub 认证才能看到公开可见的电子邮件地址。有关更多信息，请参阅身份验证。

The Emails API enables you to list all of your email addresses, and toggle a primary email to be visible publicly. For more information, see [Emails API](https://docs.github.com/rest/users/emails).  
>电子邮件 API 使您能够列出所有电子邮件地址，并切换主电子邮件地址以公开显示。有关更多信息，请参阅电子邮件 API。

#### [“Get a user”的细粒度访问令牌](https://docs.github.com/zh/rest/users/users#get-a-user--fine-grained-access-tokens)

此端点支持以下精细令牌类型:

- [GitHub应用用户访问令牌](https://docs.github.com/zh/apps/creating-github-apps/authenticating-with-a-github-app/generating-a-user-access-token-for-a-github-app)
- [GitHub应用安装访问令牌](https://docs.github.com/zh/apps/creating-github-apps/authenticating-with-a-github-app/generating-an-installation-access-token-for-a-github-app)
- [精细化的个人访问令牌](https://docs.github.com/zh/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens#creating-a-fine-grained-personal-access-token)

细粒度令牌不需要任何权限。

如果仅请求公共资源，则无需身份验证即可使用此终结点。

#### [获取用户的参数](https://docs.github.com/zh/rest/users/users#get-a-user--parameters)

标头

|名称, 类型, 说明|
|---|
|`accept` string<br><br>Setting to `application/vnd.github+json` is recommended.  <br>建议设置为 application/vnd.github+json。|

路径参数

|名称, 类型, 说明|
|---|
|`username` string 必须  <br>username string 必需<br><br>The handle for the GitHub user account.  <br>GitHub 用户账户的标识符。|

#### [HTTP 响应状态代码](https://docs.github.com/zh/rest/users/users#get-a-user--status-codes)

|状态代码|说明|
|---|---|
|`200`|OK|
|`404`|Resource not found  <br>资源未找到|




## 查看用户仓库——[List repositories for a user](https://docs.github.com/zh/rest/repos/repos?apiVersion=2026-03-10#list-repositories-for-a-user)

列出指定用户的公开仓库。

细粒度令牌必须具有以下权限集:

- "Metadata" repository permissions (read)

如果仅请求公共资源，则无需身份验证或上述权限即可使用此终结点。

### List repositories for a user 的参数  

标头

|名称, 类型, 说明|
|---|
|`accept` string<br><br>Setting to `application/vnd.github+json` is recommended.|


路径参数

|名称, 类型, 说明|
|---|
|`username` string 必须<br><br>The handle for the GitHub user account.|

查询参数

|名称, 类型, 说明|
|---|
|`type` string<br><br>Limit results to repositories of the specified type.<br><br>默认: `owner`<br><br>可以是以下选项之一: `all`, `owner`, `member`|
|`sort` string<br><br>The property to sort the results by.<br><br>默认: `full_name`<br><br>可以是以下选项之一: `created`, `updated`, `pushed`, `full_name`|
|`direction` string<br><br>The order to sort by. Default: `asc` when using `full_name`, otherwise `desc`.<br><br>可以是以下选项之一: `asc`, `desc`|
|`per_page` integer<br><br>The number of results per page (max 100). For more information, see "[Using pagination in the REST API](https://docs.github.com/rest/using-the-rest-api/using-pagination-in-the-rest-api)."<br><br>默认: `30`|
|`page` integer<br><br>The page number of the results to fetch. For more information, see "[Using pagination in the REST API](https://docs.github.com/rest/using-the-rest-api/using-pagination-in-the-rest-api)."<br><br>默认: `1`|

### “List repositories for a user”的 HTTP 响应状态代码

|状态代码|说明|
|---|---|
|`200`|OK|


