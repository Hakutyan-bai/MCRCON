<h3 align="center">
 ⭐ 还不知道取啥名字! ⭐️
</h3>

## 如何使用

准备</br>
- JDK17
- MySQL
- 一个Minecraft Java 服务器
- SMTP(可选)</br>

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