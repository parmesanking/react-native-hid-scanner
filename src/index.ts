import { NativeModules, NativeEventEmitter, EmitterSubscription } from 'react-native';

const { ScannerModule } = NativeModules;

if (!ScannerModule) {
  throw new Error(
    '[react-native-scanner] ScannerModule native module is not available. ' +
    'Make sure the library is correctly linked and you are running on Android.'
  );
}

const scannerEmitter = new NativeEventEmitter(ScannerModule);

export type BarcodeScanCallback = (barcode: string) => void;

/**
 * Subscribe to barcode scan events emitted by the hardware scanner.
 *
 * The native module listens for key events from external keyboard/scanner
 * devices, buffers characters, and fires this event when ENTER is received.
 *
 * @param callback - called with the scanned barcode string
 * @returns an EmitterSubscription — call `.remove()` to unsubscribe
 *
 * @example
 * const sub = onBarcodeScan((barcode) => console.log('Scanned:', barcode));
 * // later…
 * sub.remove();
 */
export function onBarcodeScan(callback: BarcodeScanCallback): EmitterSubscription {
  return scannerEmitter.addListener('onBarcodeScan', callback);
}

/**
 * Manually clear the internal character buffer in the native module.
 * Useful if you need to discard a partial/interrupted scan.
 */
export function clearBuffer(): void {
  ScannerModule.clearBuffer();
}

export default {
  onBarcodeScan,
  clearBuffer,
};
