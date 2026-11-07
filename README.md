# EMZARO OS — Prototype 0.1.0

An iPhone-inspired Android launcher prototype written in Kotlin and Jetpack Compose.

## Scope
This is a launcher application, not a replacement Android firmware or custom ROM. It can be installed as an ordinary APK and selected as the Home app. It does not replace Android or install Apple iOS.

## Features
- Dark navy and blue iPhone-inspired home screen
- Live clock and date
- Installed-app discovery and launch
- App search
- Dock-style shortcuts
- Control Centre visual preview
- GitHub Actions debug APK build

## Build from GitHub Actions
1. Open the repository's Actions tab.
2. Select **Build EMZARO OS prototype**.
3. Choose **Run workflow**, or push a commit to main.
4. Download the **EMZARO-OS-prototype-debug** artifact from the completed run.
5. Extract the artifact ZIP and install app-debug.apk on an Android test device.

## Set as Home app
After installation, press Home and choose EMZARO OS. Alternatively, open Settings → Apps → Default apps → Home app and select EMZARO OS.

## Technical details
- Package: com.emzaro.os
- Kotlin + Jetpack Compose
- JDK 17
- Android Gradle Plugin 8.7.3
- compileSdk 35 / targetSdk 35 / minSdk 23

## Next steps
Add proper app icons, persistent layout settings, widget support, accessibility testing, and device-specific UI tuning. Firmware/ROM work must wait until the exact device build, bootloader state, partition layout, and vendor support are verified.
