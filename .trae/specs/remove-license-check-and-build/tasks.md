# Tasks
- [x] Task 1: 创建 GitHub Actions 编译工作流
  - [x] SubTask 1.1: 创建 `.github/workflows/build.yml`，配置 push 和 workflow_dispatch 触发。
  - [x] SubTask 1.2: 在工作流中配置将 `KEYSTORE_B64` 还原为 `.keystore` 文件的步骤。
  - [x] SubTask 1.3: 修改 `presentation/build.gradle`，在 `signingConfigs` 的 release 配置中读取环境变量完成签名。
  - [x] SubTask 1.4: 在工作流中配置 `./gradlew assembleRelease` 编译。
  - [x] SubTask 1.5: 将产出的 APK 上传为 Artifact。
- [x] Task 2: 自动完成付费校验去除
  - [x] SubTask 2.1: 修改 `domain/src/main/java/org/cryptomator/domain/usecases/DoLicenseCheck.java`，将 `execute` 方法直接返回 `() -> "bypassed@example.com"`，移除原有的 JWT 本地校验逻辑。
  - [x] SubTask 2.2: 修改 `presentation/src/main/java/org/cryptomator/presentation/licensing/LicenseEnforcer.kt`，将 `hasWriteAccess`、`hasPaidLicense`、`hasActiveTrial` 等方法改为始终返回 true。
  - [x] SubTask 2.3: 确保 `ensureWriteAccess` 和 `ensureWriteAccessForVault` 直接返回 true，不再启动 `LicenseCheckIntent`。
- [x] Task 3: 工作流中内置验证步骤
  - [x] SubTask 3.1: 在 `build.yml` 的编译完成后追加步骤：安装 apktool，反编译生成的 APK。
  - [x] SubTask 3.2: 使用 grep 搜索反编译出的 smali 文件中的 `onLicenseInvalid`、`LicenseCheckActivity` 等关键字。
  - [x] SubTask 3.3: 将 grep 结果写入 `verification_report.txt` 并作为 Artifact 上传。
- [x] Task 4: 输出最终报告
  - [x] SubTask 4.1: 编写包含修改的文件、修改前后对比、Action 运行成功与否、验证结果摘要及 APK 下载位置的报告。

# Task Dependencies
- [Task 2] depends on [Task 1]
- [Task 3] depends on [Task 2]
- [Task 4] depends on [Task 3]