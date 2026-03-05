# Decart Realtime Video Example -- Android

Native Android app demonstrating Decart's Realtime Video models. Transform your camera feed in real time using AI-powered video restyling and editing.

## Features

- **Video Restyling (Mirage V2)** -- Transform your camera feed into different visual styles
- **Video Editing (Lucy 2 RT)** -- Real-time AI video editing
- **Multiple View Modes** -- Fullscreen, picture-in-picture, and vertical split
- **Front/Back Camera** -- Switch between cameras on the fly
- **90+ Styles** -- Pre-built style presets with swipe gesture navigation
- **Swipe Gestures** -- Swipe left/right to browse styles

## Prerequisites

- Android Studio (latest stable)
- Physical Android device (API 24+) -- emulator not supported for camera/WebRTC
- [Decart API key](https://decart.ai)

## Setup

1. Clone the repo:

```bash
git clone https://github.com/DecartAI/decart-example-android-realtime.git
```

2. Add your API key in `app/src/main/java/ai/decart/example/MainViewModel.kt`:

```kotlin
private val API_KEY = "your-api-key-here"
```

3. Open the project in Android Studio.

4. Connect a physical device and run the app.

## How It Works

The app captures your camera feed and streams it via WebRTC to Decart's servers, which apply the selected AI model and return transformed video frames in real time.

### Key Files

| File | Description |
|------|-------------|
| `MainViewModel.kt` | SDK integration, camera setup, connection management |
| `SkinLists.kt` | Style definitions and prompts for each model |
| `CameraScreen.kt` | Compose UI with video renderers and controls |
| `AppModel.kt` | Model definitions (Restyle / Edit) |
| `VideoRenderer.kt` | WebRTC video rendering component |

### Architecture

```
Camera -> WebRTC -> Decart Servers -> AI Model -> Transformed Frames -> Display
```

## Models

| Mode | Model | Description |
|------|-------|-------------|
| Restyle | Mirage V2 | Transform visual style (anime, cyberpunk, etc.) |
| Edit | Lucy 2 RT | Real-time video editing |

## Resources

- [Decart Android SDK](https://github.com/DecartAI/decart-android)
- [Decart Platform](https://decart.ai)
- [API Documentation](https://docs.decart.ai)
- [Get an API Key](https://decart.ai)

## License

MIT
