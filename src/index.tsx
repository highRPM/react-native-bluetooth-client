import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-bluetooth-client' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const BluetoothClient = NativeModules.BluetoothClient
  ? NativeModules.BluetoothClient
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export function multiply(a: number, b: number): Promise<number> {
  return BluetoothClient.multiply(a, b);
}

export function checkBluetooth(): Promise<string> {
  return BluetoothClient.checkBluetooth();
}

export function enableBluetooth() {
  return BluetoothClient.enableBluetooth();
}

export function startAdvertising(): Promise<string> {
  return BluetoothClient.startAdvertising();
}

export function stopAdvertising(): Promise<string> {
  return BluetoothClient.stopAdvertising();
}

export function addService(uuid: string, primary: boolean): Promise<string> {
  return BluetoothClient.addService(uuid, primary);
}

export function addCharacteristicToService(
  serviceUUID: string,
  uuid: string,
  permissions: number,
  properties: number,
  data: string
): Promise<string> {
  return BluetoothClient.addCharacteristicToService(
    serviceUUID,
    uuid,
    permissions,
    properties,
    data
  );
}

export function sendNotificationToDevice(
  serviceUUID: string,
  charUUID: string,
  message: string
): Promise<string> {
  return BluetoothClient.sendNotificationToDevice(
    serviceUUID,
    charUUID,
    message
  );
}

export function setSendData(data: string) {
  return BluetoothClient.setSendData(data);
}

export function removeAllServices() {
  return BluetoothClient.removeAllServices();
}

export function setName(name: String) {
  return BluetoothClient.setName(name);
}
