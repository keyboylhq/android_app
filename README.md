# SMS Forwarder App

## 功能描述
这是一个Android应用，用于接收短信并立即转发到指定邮箱。

## 配置信息
- 发件人邮箱: keyboylhq@qq.com
- 授权码: zxgcnueijiuyhcdh
- 收件人邮箱: keyboylhq@qq.com
- SMTP服务器: smtp.qq.com
- SMTP端口: 465

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
