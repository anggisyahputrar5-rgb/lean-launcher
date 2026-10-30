# Lean Launcher

Launcher minimalis untuk performa, efisiensi, dan battery - fokus Realme 5 Pro (Android 11 / API 30).

## Fitur
- Pure app drawer (grid 5 kolom) - tanpa home grid, widget, atau background service.
- Pencarian instan.
- Refresh daftar app hanya saat install/uninstall.
- Lazy icon loading + LruCache.

## Build via GitHub Actions
1. Push ke branch main.
2. Tab Actions - workflow Build APK.
3. Download artifact lean-launcher-debug atau lean-launcher-release.

## Build lokal
gradle wrapper --gradle-version 8.7
./gradlew :app:assembleDebug

APK: app/build/outputs/apk/debug/app-debug.apk

## Set sebagai launcher default
Settings - Apps - Default apps - Home app - Lean Launcher.
Realme UI 2.0: Settings - Home screen & wallpaper - Set default launcher.
