# Flutter Assets Manager Plugin

A productivity-enhancing plugin for Android Studio, designed to simplify asset management in Flutter projects. It provides an intuitive way to reference assets (images, videos, etc.) without hard-coding paths, inspired by Android's `R.drawable` system.

## Features

### Phase 1: Core Asset Management
- **Automatic Asset Detection**: Detects the `assets/` folder from `pubspec.yaml` or allows manual folder selection.
- **Generated References**: Creates a `FlutterAssets` class (e.g., `FlutterAssets.img.imageName`) for easy asset access with IDE auto-completion.
- **Real-Time Updates**: Automatically updates references when assets are added, deleted, renamed, or replaced.
- **Smart Folder Management**: Organizes assets into type-based folders (e.g., `assets/img/`, `assets/video/`) and creates new folders for unrecognized file types.
- **Configurable Settings**: Customize naming conventions (camelCase/snake_case), enable/disable auto-regeneration, and manage ignored folders via a settings panel.

### Phase 2: Legacy Code Migration
- **Path Refactoring**: Converts hard-coded asset paths (e.g., `"assets/img/image.png"`) to `FlutterAssets` references in existing Flutter code.
- **Preview and Backup**: Offers a preview of changes and automatic backups before refactoring.
- **Scope Selection**: Supports migration across the entire project or specific modules/folders.
- **Error Handling**: Logs unresolvable paths for manual review, ensuring safe migrations.

## Installation
1. Install via **JetBrains Marketplace** (search for "Flutter Assets Manager").
2. Enable the plugin in **File > Settings > Plugins** in Android Studio.
3. Open a Flutter project to start using the plugin.

## Usage
- **Configure Assets Folder**: Use the toolbar action to select or auto-detect the assets folder.
- **Access Assets**: Reference assets in code (e.g., `Image.asset(FlutterAssets.img.myLogo)`).
- **Manage Assets**: Add or modify assets in the `assets/` folder; references update automatically.
- **Migrate Legacy Code**: Use the "Migrate Assets" action to refactor hard-coded paths (Phase 2).

## Requirements
- **IDE**: Android Studio Koala (2024.1.1+) or IntelliJ IDEA 2023.3+ with Flutter and Dart plugins.
- **Flutter**: Version 3.24+ (stable).
- **Project**: Flutter project with assets declared in `pubspec.yaml`.

## Support
- Report issues or suggest features on [GitHub](https://github.com/your-repo/flutter-assets-manager-plugin).
- Contact: Vishwet Nadimetla (vendor).

## License
MIT License