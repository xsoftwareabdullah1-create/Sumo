# Sumo WinDesk

Landscape Android desktop launcher with touch-following pointer, EXE selection, Winlator launcher, and Termux/Codex launcher.

## Runtime note
This project is the Android launcher layer. It does NOT embed the Winlator/Wine/Box64 runtime. Real Windows EXE execution is handled by a separately installed compatible Winlator build. The selected document is not automatically injected into Winlator because there is no stable public cross-fork launch-intent contract; use Winlator's file manager/shared storage to launch it. The visible pointer follows touch; a connected hardware mouse uses Android's native pointer.

- Winlator source: https://github.com/brunodev85/winlator
- Codex CLI: https://github.com/openai/codex

## Build APK
GitHub Actions builds a debug APK on pushes to main and can be run manually. Open Actions, choose Build Sumo WinDesk APK, then download artifact Sumo-WinDesk-debug-apk after a successful run. Install app-debug.apk on Android. Install Winlator separately for EXE execution and Termux separately for Codex CLI.

Local requirements: JDK 17, Android SDK Platform 35, Gradle 8.9-compatible tooling.
