<h3 align="center">
 ⭐ 还不知道取啥名字! ⭐️
</h3>

## 项目介绍

MC RCON 白名单管理系统，基于 Spring Boot + MySQL开发，提供管理员管理 Minecraft 服务器 RCON 白名单的功能。</br>
注意，这不是插件，是一个独立的 Web 应用程序，需要连接到 Minecraft 服务器的 RCON 接口来管理白名单，适合原版 Minecraft Java 服务器。</br>

## 功能特性
1. 网页申请加入白名单与审核白名单：玩家可以通过网页申请加入白名单，管理员可以审核申请并通过 RCON 命令将玩家添加到 Minecraft 服务器的白名单中。
2. 管理员登录与权限管理：管理员可以通过用户名和密码登录系统，拥有审核申请和管理白名单的权限。
3. 邮箱验证：玩家申请时会验证邮箱，管理员审核结果会发送邮件通知玩家。

## 技术栈
- Spring Boot
- MySQL
- Smtp

## 如何使用

准备</br>
- JDK17
- MySQL
- 一个Minecraft Java 服务器
- SMTP</br>

1. 创建一个数据库，名为 **mcrcon** (名称随意你记得住就好)
2. 开启Minecraft服务器 RCON 并设置密码
3. 启动 ⭐ 还不知道取啥名字! ⭐️生成.properties配置文件
```bash
java -jar demo.jar
```
```properties
# MC_RCON 白名单管理系统配置文件
# 此文件在首次启动时自动生成，您可以根据需要修改配置
# 修改后需要重启应用程序才能生效

# ========== 数据库配置 ==========
database.host=
database.port=3306
# 数据库名称就填上面新建那个
database.name=mcrcon   
database.username=
database.password=

# ========== 管理员默认账号配置（记得在后台改而不是这里改） ==========
admin.default.username=admin
admin.default.password=admin123

# ========== RCON服务器配置 ==========
rcon.host=
rcon.port=25575
rcon.password=
rcon.timeout=3000

# ========== 邮件通知（审核结果）配置（自动补齐）==========
# 修改为您自己的网易邮箱账号与授权码，并将 mail.enabled 设置为 true
mail.enabled=false
mail.host=smtp.163.com
mail.port=465
mail.username=
mail.password=
mail.from=
mail.fromName=
mail.charset=UTF-8
mail.ssl=true
mail.timeout=10000


```
4. 再次启动
```bash
java -jar demo.jar
```
5. 访问8080端口

如果你喜欢这个项目，欢迎给个⭐️⭐️⭐️⭐️⭐️，谢谢！