# Remove License Check and Automate Build Spec

## Why
用户需要去除应用中的付费校验逻辑，并通过 GitHub Actions 自动编译出签名的 release APK，并自动验证去除结果。

## What Changes
- 添加 GitHub Actions 工作流文件 `.github/workflows/build.yml` 以实现自动构建、签名和验证。
- 修改 `presentation/build.gradle`（原应用的主模块构建文件），增加读取环境变量配置 release 签名的逻辑。
- 修改 `domain/src/main/java/org/cryptomator/domain/usecases/DoLicenseCheck.java`，直接返回成功回调，绕过 JWT 校验。
- 修改 `presentation/src/main/java/org/cryptomator/presentation/licensing/LicenseEnforcer.kt`，使其 `hasPaidLicense` 及相关写权限方法始终返回 true。

## Impact
- Affected specs: 编译系统、付费状态校验。
- Affected code: 
  - `.github/workflows/build.yml`
  - `presentation/build.gradle`
  - `domain/src/main/java/org/cryptomator/domain/usecases/DoLicenseCheck.java`
  - `presentation/src/main/java/org/cryptomator/presentation/licensing/LicenseEnforcer.kt`

## ADDED Requirements
### Requirement: GitHub Actions 自动构建与验证
The system SHALL provide 一个 GitHub Actions 工作流，触发条件为 push 或 workflow_dispatch。
自动配置 keystore 文件和签名，执行 `assembleRelease`，然后使用 apktool 反编译并用 grep 检查 LicenseCheckActivity、onLicenseInvalid 等关键字，生成 `verification_report.txt`，最后将 APK 和验证报告作为 Artifact 上传。

## MODIFIED Requirements
### Requirement: 绕过付费校验
原有的 JWT 和标志位校验逻辑被修改为直接通过，所有需要付费才能使用的功能默认开启，不再跳转到 `LicenseCheckActivity` 界面。