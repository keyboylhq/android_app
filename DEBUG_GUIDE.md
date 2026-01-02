# SMS Forwarder 调试指南

## 项目概述
这是一个完整的Android应用，用于接收短信并立即转发到指定邮箱。项目结构完整，代码逻辑正确，可以直接在Android Studio中打开和运行。

## 核心功能
- 接收短信广播
- 提取发件人和短信内容
- 通过JavaMail API发送邮件
- 处理运行时权限

## 环境要求
- Android Studio Arctic Fox或更高版本
- JDK 11或更高版本
- Android SDK 21或更高版本
- 连接的Android设备或模拟器

## 调试步骤

### 1. 打开项目
- 启动Android Studio
- 选择"Open an existing project"
- 导航到项目目录并打开

### 2. 配置Android SDK
- 在Android Studio中，进入`File > Project Structure > SDK Location`
- 确保Android SDK路径正确配置
- 确保已安装以下SDK组件：
  - Android SDK Build-Tools 33.0.0
  - Android SDK Platform 33
  - Android Support Repository

### 3. 连接设备
- 使用USB线连接Android设备
- 启用设备的开发者选项和USB调试
- 在Android Studio中，确认设备已被识别（查看右下角）

### 4. 构建并运行
- 点击"Run"按钮或使用快捷键`Shift+F10`
- 选择连接的设备
- Android Studio将构建项目并安装到设备上

### 5. 授予权限
- 首次运行应用时，会弹出权限请求对话框
- 点击"允许"授予短信接收权限
- 应用主界面会显示权限状态

### 6. 测试功能
- 使用另一台手机向测试设备发送短信
- 应用将自动接收短信并转发到指定邮箱
- 检查邮箱是否收到转发的短信

## 调试技巧

### 1. 查看日志
- 在Android Studio中，打开Logcat窗口
- 过滤标签为`SMSReceiver`的日志
- 可以查看邮件发送状态和任何错误信息

### 2. 检查权限
- 确保应用已获得`RECEIVE_SMS`权限
- 在设备的"设置 > 应用 > SMS Forwarder > 权限"中检查

### 3. 网络连接
- 确保设备已连接到互联网
- 检查SMTP服务器配置是否正确

### 4. 邮件配置
- 确保QQ邮箱已开启SMTP服务
- 确认授权码正确（不是QQ密码）
- 检查收件人邮箱地址是否正确

## 常见问题及解决方案

### 1. 应用无法接收短信
- 检查是否授予了`RECEIVE_SMS`权限
- 检查其他应用是否拦截了短信
- 确保设备能够正常接收短信

### 2. 邮件发送失败
- 检查设备网络连接
- 查看Logcat中的错误信息
- 确认SMTP服务器和端口配置
- 检查授权码是否正确

### 3. 应用崩溃
- 查看Logcat中的崩溃日志
- 检查代码中的空指针异常
- 确保所有依赖项都正确配置

## 代码结构说明

### MainActivity.java
- 处理权限请求
- 显示应用状态
- 入口点活动

### SMSReceiver.java
- 监听短信广播
- 提取短信信息
- 发送邮件
- 异步处理邮件发送

### AndroidManifest.xml
- 声明`RECEIVE_SMS`和`INTERNET`权限
- 注册短信广播接收器
- 配置活动和应用信息

## 进一步调试

### 使用adb命令
```bash
# 查看设备日志
adb logcat -s SMSReceiver

# 安装应用
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 查看已安装应用
adb shell pm list packages | grep smsforwarder

# 卸载应用
adb uninstall com.example.smsforwarder
```

### 调试广播接收器
- 在`SMSReceiver.onReceive()`方法中添加断点
- 使用`adb shell am broadcast -a android.provider.Telephony.SMS_RECEIVED`模拟短信广播

## 注意事项

1. 应用需要在前台至少运行一次，以便系统注册广播接收器
2. 某些设备可能限制后台应用接收短信广播，需要将应用添加到白名单
3. QQ邮箱的授权码需要定期更新，过期后需要重新生成
4. 确保在生产环境中使用安全的方式存储邮箱密码和授权码

## 总结

这个项目已经实现了完整的短信转发功能，代码结构清晰，逻辑正确。在正确配置的Android Studio环境中，应该可以顺利构建和运行。通过查看Logcat日志和使用调试技巧，可以解决大部分问题。

如果遇到无法解决的问题，可以尝试：
1. 检查Android Studio版本和SDK配置
2. 更新依赖项版本
3. 测试不同的Android设备
4. 查看详细的错误日志

祝你调试顺利！