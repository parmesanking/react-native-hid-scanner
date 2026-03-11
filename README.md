# react-native-scanner

A React Native **Android-only** native module for hardware barcode scanner integration.
It listens for key events from external HID keyboard/scanner devices, buffers characters, and emits an `onBarcodeScan` event to JavaScript when the scanner fires its terminating ENTER keystroke.

---

## Installation

```bash
npm install react-native-scanner
# or
yarn add react-native-scanner
```

### 1. Register the Package in `MainApplication.kt`

```kotlin
import com.reactnativescanner.ScannerPackage

override fun getPackages(): List<ReactPackage> {
    return PackageList(this).packages.apply {
        add(ScannerPackage())
    }
}
```

### 2. Wire up `dispatchKeyEvent` in `MainActivity.kt`

`ScannerModule.handleKeyEvent` must be called from the host Activity so it can intercept hardware scanner key events **before** they reach React Native's TextInputs.

```kotlin
import android.view.KeyEvent
import com.facebook.react.ReactActivity
import com.facebook.react.bridge.ReactContext
import com.reactnativescanner.ScannerModule

class MainActivity : ReactActivity() {

    override fun getMainComponentName() = "YourAppName"

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val reactContext = reactInstanceManager?.currentReactContext as? ReactContext
        val scanner = reactContext
            ?.getNativeModule(ScannerModule::class.java)

        if (scanner?.handleKeyEvent(event) == true) {
            return true // event consumed by scanner module
        }
        return super.dispatchKeyEvent(event)
    }
}
```

---

## JavaScript / TypeScript Usage

```typescript
import { useEffect } from 'react';
import Scanner, { onBarcodeScan, clearBuffer } from 'react-native-scanner';

export function MyScreen() {
  useEffect(() => {
    const sub = onBarcodeScan((barcode) => {
      console.log('Scanned barcode:', barcode);
      // handle the barcode...
    });

    return () => sub.remove(); // always clean up
  }, []);

  return /* ... */;
}
```

### API

| Function | Signature | Description |
|---|---|---|
| `onBarcodeScan` | `(callback: (barcode: string) => void) => EmitterSubscription` | Subscribe to scan events. Call `.remove()` to unsubscribe. |
| `clearBuffer` | `() => void` | Manually flush the internal character buffer. |

---

## How it works

1. A hardware barcode scanner presents itself as an **external HID keyboard** to Android.
2. The module's `isScanner()` check filters for external, non-touchscreen keyboard sources — so normal keypresses from a physical keyboard are unaffected.
3. Characters are accumulated in a `StringBuilder`; when `KEYCODE_ENTER` arrives, the buffer is flushed and the barcode string is emitted to JS via `DeviceEventManagerModule`.
4. The event is **consumed** (returns `true`) so React Native TextInput fields never see raw scanner keystrokes.

### Optional: Vendor-ID Filtering

For extra certainty you can uncomment the vendor-ID check in `ScannerModule.kt`:

```kotlin
// val isKnownScanner = device.vendorId == 0x05E0 // Symbol/Zebra
```

Common scanner vendor IDs:
- `0x05E0` — Honeywell / Symbol / Zebra
- `0x0536` — Hand Held Products

---

## Platform Support

| Platform | Supported |
|---|---|
| Android | ✅ |
| iOS | ❌ (Android-only module) |

---

## License

MIT
