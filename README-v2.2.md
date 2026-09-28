# TH07 Android v2.2 — Practice+

基于 v2.1，针对“Extra 解锁困难、想单练非符/符卡”的移动端练习需求增加一组 Practice+ 功能。

## 新功能

- Android 主菜单的 **Extra** 不再依赖通关记录；没有 clear 记录也可以进入 Extra。
- Practice 关卡列表扩展为 **Stage 1–6 + Extra**，Extra 也不依赖 `score.dat` 解锁历史。
- 右侧新增 Practice+ 控件：
  - `INV`：练习模式无敌开关。只在 Practice 生效，普通游戏不受影响。
  - `NEXT PHASE`：Boss 出现后，强制推进到下一段攻击（非符/符卡）。
  - `RETRY PHASE`：尝试重新执行当前 Boss 段落入口，用来重复练习当前非符/符卡。
- v2.1 的 Z/S 单击锁定、X、ESC、FREE/LIMIT 移动全部保留。
- Practice 打完后仍然绕过当前 Android 上不稳定的 Result/Score 页面，直接回标题。

## 使用建议

1. 进入 Practice，直接选择任意 Stage 1–6 或 Extra。
2. 想快速稳定到 Boss，可以打开 `INV`。
3. Boss 出现后点 `NEXT PHASE`，逐段跳过非符/符卡，直到你想练的那一段。
4. 在目标段落点 `RETRY PHASE` 重练。

## 重要限制

`NEXT/RETRY PHASE` 是针对原 ECL Boss callback 做的“thprac-lite”实现，不是完整 thprac 的独立 Spell Practice 菜单。不同 Boss 的脚本结构并不完全一致：

- `NEXT PHASE` 只在已经出现 Boss 且该 Boss 有 life/timer callback 时工作。
- `RETRY PHASE` 对大多数常规阶段应可用，但部分特殊阶段、对话切换、双 Boss 或特殊 survival spell 可能不能完全恢复到原始状态。
- 第一个 Boss 段落的 `RETRY` 是懒记录的，少数脚本可能会回到 Boss 入口而不是精确的第一非符。
- 正常模式的 Result/Score 闪退仍未宣称彻底修复；Practice 继续绕过该页面。

如果 v2.2 的阶段推进验证稳定，下一步才能比较安全地做真正的“Boss/Nonspell/Spell 列表选择菜单”。

## 构建

上传本包内容到仓库根目录，提交后运行：

`Actions → Build TH07 Android v2.2 APK → Run workflow`

成功后下载 Artifact：

`TH07-Android-v2.2-APK`

APK：

`TH07-Android-v2.2-debug.apk`

上游固定 commit：

`5412dc42f25951de96d0b094b97fb916652ea1bf`
