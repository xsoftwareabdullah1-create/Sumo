# SUMO WinDesk

SUMO WinDesk is a landscape Android desktop shell with a touch-following cursor, a private EXE library, Android file-picker import, and support for receiving shared `.exe` files.

## Current functionality

- Landscape-first desktop layout.
- Import an `.exe` through Android's Storage Access Framework.
- Receive supported `.exe` share/open intents.
- Copy imported files into SUMO's private app storage so they remain available across app restarts.
- List imported files, show their sizes, and delete them from the library.
- GitHub Actions builds a debug APK on pushes to `main`.

## Important runtime limitation

**SUMO does not yet install or execute Windows programs.** The EXE library is only file management; it is not a Windows compatibility layer. This repository does not yet contain Wine, Box86/Box64, a Windows filesystem/container, or the native runtime integration required to run installers. Imported files must not be described as installed or executed.

The next major engineering phase is to integrate and maintain a compatible open-source Windows runtime for supported Android CPU architectures, then implement a persistent virtual Windows prefix, process launching, graphics/input/audio, and installer testing. This is a substantial native project, not a Java-only button or manifest change. Compatibility will vary by EXE and device.

## Build the APK

GitHub Actions builds a debug APK on pushes to `main`. Open the repository's Actions tab, choose **Build Sumo WinDesk APK**, and download the `Sumo-WinDesk-debug-apk` artifact from a successful run. Install the contained `app-debug.apk` on Android.

## Project links

- Repository: https://github.com/xsoftwareabdullah1-create/Sumo
- Wine: https://www.winehq.org/
- Winlator (reference architecture/runtime project): https://github.com/brunodev85/winlator
- Codex CLI (separate Termux workflow): https://github.com/openai/codex
