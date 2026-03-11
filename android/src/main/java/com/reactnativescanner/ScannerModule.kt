package com.reactnativescanner

import android.view.InputDevice
import android.view.KeyEvent
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.modules.core.DeviceEventManagerModule

// ScannerModule.kt
class ScannerModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    override fun getName() = "ScannerModule"

    // Call this from your Activity's dispatchKeyEvent
    fun handleKeyEvent(event: KeyEvent): Boolean {
        val device = InputDevice.getDevice(event.deviceId) ?: return false

        if (!isScanner(device)) return false // let normal keys pass through

        if (event.action == KeyEvent.ACTION_DOWN) {
            if (event.keyCode == KeyEvent.KEYCODE_ENTER) {
                flushBuffer()
            } else {
                val char = event.unicodeChar.toChar()
                if (char.isLetterOrDigit() || char.isWhitespace()) {
                    buffer.append(char)
                }
            }
        }
        return true // consume the event — TextInputs never see it
    }

    private fun isScanner(device: InputDevice): Boolean {
        // Scanners are external, non-virtual, non-touchscreen sources
        val sources = device.sources
        val isExternal = !device.isVirtual
        val isKeyboard = sources and InputDevice.SOURCE_KEYBOARD == InputDevice.SOURCE_KEYBOARD
        val isNotFinger = sources and InputDevice.SOURCE_TOUCHSCREEN == 0

        // Optional: match by known vendor/product ID for certainty
        // val isKnownScanner = device.vendorId == 0x05E0 // Symbol/Zebra

        return isExternal && isKeyboard && isNotFinger
    }

    private val buffer = StringBuilder()

    private fun flushBuffer() {
        if (buffer.isEmpty()) return
        val barcode = buffer.toString()
        buffer.clear()

        reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit("onBarcodeScan", barcode)
    }

    // Expose a method so JS can manually clear the buffer if needed
    @ReactMethod
    fun clearBuffer() {
        buffer.clear()
    }

    // Allow JS to add listeners (required by RN event emitter convention)
    @ReactMethod
    fun addListener(eventName: String) {
        // Required by RN — no-op
    }

    @ReactMethod
    fun removeListeners(count: Int) {
        // Required by RN — no-op
    }
}
