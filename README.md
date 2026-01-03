# SMS Forwarder App

## 功能描述
这是一个Android应用，用于接收短信并立即转发到指定邮箱。

## 配置信息

### 安全配置方式
为了方便调试且不泄露敏感信息，本应用使用 `local.properties` 文件存储配置。该文件不会被Git跟踪，因此您可以安全地将敏感信息放在这里。

### 配置步骤
1. 复制配置模板文件：
   ```bash
   cp local.properties.template local.properties
   ```
2. 编辑 `local.properties` 文件，填写您的实际配置：
   ```properties
   # Email configuration
   email.from=your_qq_email@qq.com          # 发件人邮箱
   email.password=your_qq_email_app_password # 邮箱授权码（不是密码）
   email.to=recipient_email@example.com      # 收件人邮箱
   
   # SMTP server configuration (default: QQ SMTP)
   smtp.server=smtp.qq.com
   smtp.port=465
   ```

### 重要说明
- **QQ邮箱授权码获取**: 登录QQ邮箱 -> 设置 -> 账户 -> 开启POP3/SMTP服务 -> 获取授权码
- **不使用真实密码**: 必须使用授权码，而不是QQ邮箱登录密码
- **文件安全**: `local.properties` 文件已被添加到 `.gitignore`，不会被Git跟踪
- **调试方便**: 可以直接修改配置文件，无需修改代码，重启应用即可生效

## 项目结构
```
android_app/
├── app/
│   ├── build.gradle                 # 应用级构建配置
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml  # 应用清单，声明权限和组件
│           ├── java/
│           │   └── com/example/smsforwarder/
│           │       ├── MainActivity.java     # 主活动，请求权限
│           │       └── SMSReceiver.java      # 短信接收器，处理短信转发
│           └── res/
│               ├── layout/
│               │   └── activity_main.xml     # 主界面布局
│               └── values/
│                   ├── colors.xml            # 颜色资源
│                   ├── strings.xml           # 字符串资源
│                   └── themes.xml            # 主题资源
├── build.gradle                     # 项目级构建配置
└── settings.gradle                  # 项目设置
```

## 权限说明
- `RECEIVE_SMS`: 接收短信权限
- `INTERNET`: 网络访问权限，用于发送邮件

## 核心功能
1. `MainActivity`: 请求并检查短信接收权限
2. `SMSReceiver`: 监听短信广播，提取发件人和短信内容，通过JavaMail发送到指定邮箱

## 构建和运行
1. 确保已安装Android SDK和Java 17
2. 在项目根目录执行构建命令:
   ```bash
   ./gradlew build
   ```
3. 安装到设备:
   ```bash
   ./gradlew installDebug
   ```

## 注意事项
1. 首次运行需要授予短信接收权限
2. 确保设备可以访问互联网
3. QQ邮箱需要开启SMTP服务并使用授权码登录

## 技术栈
- Android SDK 33
- Java 8
- JavaMail API (com.sun.mail:android-mail:1.6.7)
- AndroidX库
