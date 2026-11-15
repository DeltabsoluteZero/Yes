# DfRoot - Advanced Root Management App

A powerful root management application for Android devices, inspired by dfroot. Features temporary root, root hiding (Magisk Hide/Zygisk/Shamiko), and exploit management - all without unlocking the bootloader.

## Features

### 🔐 Root Management
- **Temporary Root**: Enable root access for a specified duration (auto-revokes)
- **Root Status Detection**: Detects Magisk, KernelSU, SuperSU, PHH Superuser
- **SELinux Management**: View and modify SELinux status
- **Boot Persistence**: Auto-restore root state on boot

### 🕵️ Root Hiding
- **Magisk Hide** (Legacy): Traditional magiskhide daemon
- **Zygisk** (Recommended): Modern Zygisk-based hiding with deny/allow lists
- **Shamiko**: Advanced hiding via Shamiko module
- **KernelSU Hide**: Kernel-level root hiding
- **Per-App Configuration**: Selective hiding for target applications
- **Deny/Allow List Modes**: Flexible hide list management

### 💣 Exploit Management
- **Built-in Exploits**: Dirty COW, Janus, KPTI Bypass, RAMbleed, BlueBorne, and more
- **Custom Exploit Support**: Install and manage custom exploits
- **Compatibility Checking**: Automatic API/architecture compatibility verification
- **Checksum Verification**: SHA-256 verification for downloaded exploits
- **Execution Logging**: Detailed execution results and output capture

### 📊 Monitoring & Logs
- **Real-time Root Status**: Live root status updates
- **Comprehensive Logging**: Categorized logs with filtering
- **Log Export**: Export logs for debugging
- **Persistent Notifications**: Status notifications with quick actions

### 🔒 Security
- **Biometric Authentication**: Protect app access with fingerprint/face
- **Encrypted Storage**: Secure configuration storage
- **Root Access Control**: Require auth for sensitive operations

## Requirements

- Android 7.0 (API 24) or higher
- **Magisk** installed (for root features)
- **Root access** (for full functionality)
- **Zygisk** enabled in Magisk (for best hiding)

## Architecture

```
DfRootApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/dfroot/
│   │   │   ├── model/          # Data models (RootStatus, Exploit, AppInfo, etc.)
│   │   │   ├── root/           # Root management (RootManager)
│   │   │   ├── exploit/        # Exploit management (ExploitManager)
│   │   │   ├── hide/           # Root hiding (HideManager)
│   │   │   ├── service/        # Background services
│   │   │   ├── ui/             # UI layer (fragments, viewmodels)
│   │   │   └── utils/          # Utilities (ShellExecutor, Logger)
│   │   ├── res/                # Resources (layouts, drawables, values)
│   │   └── assets/exploits/    # Built-in exploit scripts
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Key Components

### RootManager
Handles all root operations:
- Root status detection
- Enable/disable root
- Temporary root with auto-expiry
- SELinux management

### ExploitManager
Manages exploit lifecycle:
- Built-in exploit database
- Download/install from assets/URLs
- Execute with timeout handling
- Result logging and callbacks

### HideManager
Implements root hiding:
- Magisk Hide / Zygisk deny lists
- Shamiko integration
- KernelSU hide
- Per-app rules with custom props

### Services
- **RootManagementService**: Foreground service for status monitoring
- **ExploitExecutionService**: Background exploit execution
- **RootHideService**: Hide configuration persistence
- **BootReceiver**: Auto-start on boot
- **PackageChangeReceiver**: App install/uninstall handling

## Building

```bash
# Clone the repository
git clone https://github.com/yourusername/DfRootApp.git
cd DfRootApp

# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing config)
./gradlew assembleRelease
```

## Configuration

### Gradle Properties (`gradle.properties`)
```properties
# Signing configuration
SIGNING_KEY_ALIAS=your_key_alias
SIGNING_KEY_PASSWORD=your_key_password
SIGNING_STORE_PASSWORD=your_store_password
SIGNING_STORE_FILE=../keystore.jks
```

### App Settings
Settings are stored in encrypted SharedPreferences:
- Auto-start on boot
- Default temp root duration
- Hide mode preferences
- Biometric lock
- Theme preference

## Exploit Development

### Adding New Exploits
1. Add exploit script to `app/src/main/assets/exploits/`
2. Register in `ExploitManager.loadBuiltInExploits()`
3. Provide metadata (CVE, API range, architectures)

### Exploit Script Format
```bash
#!/system/bin/sh
# Exploit: CVE-XXXX-XXXX
# Description: Brief description

set -e

EXPLOIT_DIR="$(dirname "$0")"
BINARY="$EXPLOIT_DIR/exploit_binary"

if [ -f "$BINARY" ]; then
    chmod +x "$BINARY"
    exec "$BINARY" "$@"
else
    echo "Binary not found"
    exit 1
fi
```

## Safety & Disclaimer

⚠️ **WARNING**: This application modifies system-level permissions and can potentially brick your device if used incorrectly.

- **Exploits are experimental** - Use at your own risk
- **Always backup** before using exploits
- **Test on spare device** first
- **Root hiding is not foolproof** - Advanced detection may still find root

## License

```
MIT License

Copyright (c) 2024 DfRoot Contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

## Credits

- **Magisk** by topjohnwu - Root framework
- **KernelSU** by xiaoxiangmoe - Kernel-level root
- **Shamiko** by LSPosed - Advanced root hiding
- **LibSuperUser** by Chainfire - Root shell library
- **Material Design** - UI components

## Support

- **GitHub Issues**: Bug reports and feature requests
- **Telegram**: @DfRootSupport
- **Documentation**: Wiki pages

---

**DfRoot** - Root management without compromise