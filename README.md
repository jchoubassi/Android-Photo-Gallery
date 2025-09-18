# PhotoGallery (Android)

Basic Photo Gallery: loads device images from MediaStore into a 3-column grid. 
Tap to open a pinch-to-zoom viewer. Refresh icon in the top bar.

## Features
- Runtime Photos permission (API 26–36)
- MediaStore query (newest first)
- 3-column Compose grid
- Full-screen viewer (pinch-to-zoom)
- ContentObserver auto-refresh
- Refresh action in top app bar

## Build
- Android Studio (Ladybug+), JDK 17
- Kotlin 2.x, AGP 8.6.x, Compose Material3

## Run
1) Open in Android Studio → Run on emulator/device
2) Grant **Photos** permission
3) To add images on emulator: upload JPG/PNG to `/sdcard/Pictures` or `DCIM/Camera` (Device File Explorer)

## Permissions
- `READ_MEDIA_IMAGES` (API 33+)
- `READ_EXTERNAL_STORAGE` (API ≤32)
