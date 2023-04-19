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

export interface AdvertiseSetting {
    connectable: boolean;
    txPower: number;
    mode: number;
    includeName: boolean;
    includeTxPower: boolean;
}

export function checkBluetooth(): Promise<string> {
  return BluetoothClient.checkBluetooth();
}

export function enableBluetooth() {
  return BluetoothClient.enableBluetooth();
}

export function startAdvertising(t: number, options?: AdvertiseSetting): Promise<string> {
  return BluetoothClient.startAdvertising(t, options);
}

export function stopAdvertising(): Promise<string> {
  return BluetoothClient.stopAdvertising();
}

export function addAdvertiseService(uuid: string, serviceData: string): string {
  return BluetoothClient.addAdvertiseService(uuid, serviceData);
}

export function addService(uuid: string, primary: boolean): string {
  return BluetoothClient.addService(uuid, primary);
}

export function addCharacteristicToService(
  serviceUUID: string,
  uuid: string,
  permissions: number,
  properties: number,
  data: string
): string {
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

export function setCharacteristicData(
  serviceUUID: string,
  charUUID: string,
  data: string
): Promise<string> {
  return BluetoothClient.setCharacteristicData(serviceUUID, charUUID, data);
}

export function removeAllServices() {
  return BluetoothClient.removeAllServices();
}

export function setName(name: String) {
  return BluetoothClient.setName(name);
}
