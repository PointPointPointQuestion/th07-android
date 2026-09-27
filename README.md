# TH07 Android 云端构建包

这是一个**不需要安装 Android Studio**的 GitHub Actions 构建辅助工程。

它不会包含或上传《东方妖妖梦》的原版数据、音乐或字体。构建时会从公开的 `some100/th07` 仓库检出 `reallyportable` 分支，并使用该分支已有的 Android / SDL3 / OpenGL ES 支持来生成一个可安装的 Debug APK。

## 你需要准备

- 一个 GitHub 账号；
- 你合法拥有的《东方妖妖梦 ～ Perfect Cherry Blossom》日文版 1.00b 的：
  - `th07.dat`
  - `thbgm.dat`
  - `msgothic.ttc`

这些文件**不要上传到 GitHub**。

## 最简单的构建方法

1. 在 GitHub 新建一个空仓库，例如 `th07-android-build`。
2. 把这个压缩包解压后的所有文件上传到仓库根目录，注意 `.github` 目录也要一起上传。
3. 打开仓库顶部的 **Actions**。
4. 选择 **Build TH07 Android APK**。
5. 点 **Run workflow**。
6. 构建成功后，在该次运行页面底部的 **Artifacts** 下载 `TH07-Android-APK`。
7. 解压后得到 `TH07-Android-debug.apk`，传到手机安装。

> 第一次安装第三方 APK 时，Android 可能要求你允许浏览器/文件管理器“安装未知应用”。

## 第一次启动

APK 打开后会先出现资源导入页面。依次选择：

1. `th07.dat`
2. `thbgm.dat`
3. `msgothic.ttc`

三个文件导入成功后，“启动游戏”按钮才会启用。

导入采用 Android 系统文件选择器，不需要手动进入受限制的 `Android/data` 目录，也不需要存储权限。

## 触屏操作

`reallyportable` 分支本身已经实现触屏：

- 菜单：滑动移动光标，单击确认，双指点击返回；
- 游戏：手指拖动控制自机；
- 移动时再按住另一根手指：低速 / Focus；
- 点击画面两侧黑边或左下角：Bomb；
- 游戏区域内四指按住：暂停；
- 长按约 0.5 秒：跳过对话。

## 注意

- APK 使用上游 `reallyportable` 分支，属于社区移植，不是 Team Shanghai Alice / ZUN 官方 Android 版。
- 本工程只提供构建脚本、资源导入启动器和原创占位图标，不分发原作素材。
- 当前工作流构建的是 Debug APK，因此 Android 会使用标准 Debug 签名；适合个人安装测试。
- 上游目前注明文字渲染可能与原版略有差异，部分旧功能（例如 MIDI）未实现。

## 上游

- `some100/th07` — `reallyportable` branch
- SDL3 / SDL3_image / SDL3_ttf 由上游仓库作为子模块/依赖处理

## 本辅助工程内容

- `.github/workflows/build-apk.yml`：GitHub 云端自动构建 APK
- `LauncherActivity.java`：资源导入页
- `AndroidManifest.xml`：把资源导入页设为启动页
- `ic_launcher.png`：不使用原作图像的占位图标
