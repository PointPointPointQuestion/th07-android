# TH07 Android v2 构建包

这是给已经能运行 `reallyportable` Android 版 TH07 的测试升级包。它不会包含 `th07.dat`、`thbgm.dat` 或 `msgothic.ttc`。

## v2 已实际修改的内容

- 左上角半透明 `ESC`。
- 左下角竖排 `Z / S / X`：
  - `Z` = 射击 / 确认
  - `S` = Focus（实际发送 Shift）
  - `X` = Bomb / 返回
- 取消游戏中的“拖动即自动射击”“第二指低速”“边缘 Bomb”“四指 ESC”逻辑，避免多点误触。
- 右上角 `MOVE FREE / MOVE LIMIT`：
  - `FREE` 默认：拖多少移动多少，不受原作每帧移动速度上限约束；按住 S 时仍降低触控位移，便于微操。
  - `LIMIT`：保留上游原来的速度上限逻辑。
  - 选择会保存到 Android SharedPreferences。
- Android 存档不再跟外部 DAT 资源混在同一目录；`score.dat` / cfg / replay 使用 SDL 私有持久目录。
- 保存改为临时文件写完再替换，并保留 `.bak`，降低异常退出损坏存档的概率。
- 对空/损坏 `score.dat` 增加保护。
- Android 的高分姓名输入暂时自动使用 `MOBILE` 并跳过原来的旧字节字符输入表；这是为了规避你报告的结算/记录界面闪退。高分与统计会立即写盘。
- Practice 结束时也立即写入练习记录。

## 构建

1. 把本 ZIP **解压后的内容**上传到你的 GitHub 仓库根目录。
2. 提交到 `main`。
3. GitHub 顶部打开 **Actions**。
4. 进入 **Build TH07 Android v2 APK**。
5. 点 **Run workflow**（如果 push 后已经自动在跑，可以直接等）。
6. 成功后，在运行页面底部下载 Artifact：`TH07-Android-v2-APK`。
7. 解压 Artifact，安装 `TH07-Android-v2-debug.apk`。

## 第一次从旧版升级时的重要说明

这个构建包开始使用一个固定的开发签名，让**今后的 v2 测试包可以互相覆盖安装**。
但你之前 GitHub Actions 生成的旧 APK 很可能使用了另一个临时 Debug 签名。因此 Android 若提示“无法安装/签名不一致”，需要先卸载旧版再安装 v2。

卸载应用通常会删除应用自己的资源目录，所以请确保手机其他位置仍保留你自己的：

- `th07.dat`
- `thbgm.dat`
- `msgothic.ttc`

然后用启动页重新导入即可。不要把这些文件上传到公开 GitHub。

## 测试重点

请优先测试：

1. 拖动 + 按住 Z 是否能同时工作。
2. 拖动 + Z + S 是否能同时工作。
3. X 是否可靠放 Bomb。
4. ESC 是否能正常暂停/返回。
5. FREE/LIMIT 切换是否立即生效，并在重启后保留。
6. 打完一局进入结算时是否还闪退。
7. 回到标题后退出应用，再打开，看 High Score / 通关与符卡记录是否保留。
8. Practice 打完后退出重进，看 Practice Score 是否保留。

## 当前未包含

- 中文化（计划在存档/触控稳定后做）。
- THPrac 风格 Custom Practice（需要单独改菜单和关卡内部状态，暂未塞进 v2，以免同时引入太多变量）。

## 上游版本

本构建固定到你提供源码报告对应的 commit：

`5412dc42f25951de96d0b094b97fb916652ea1bf`
