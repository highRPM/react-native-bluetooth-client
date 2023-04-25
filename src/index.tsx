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

export enum TxPower {
  ULTRA_LOW = 0,
  LOW = 1,
  MEDIUM = 2,
  HIGH = 3,
}

export enum AdvertiseMode {
  LOW_LATENCY = 0,
  LOW_POWER = 1,
  BALANCED = 2,
}

export interface AdvertiseSetting {
  connectable?: boolean;
  txPower?: TxPower;
  mode?: AdvertiseMode;
  includeDeviceName?: boolean;
  includeTxPower?: boolean;
  manufacturerId?: number;
  manufacturerData?: string;
}

export enum Permission {
  READABLE = 1,
  READ_ENCRYPTED = 2,
  WRITEABLE = 4,
  WRITE_ENCRYPTED = 8,
}

export enum Property {
  BROADCAST = 1,
  READ = 2,
  WRITE_NO_RESPONSE = 4,
  WRITE = 8,
  NOTIFY = 16,
  INDICATE = 32,
  SIGNED_WRITE = 64,
  EXTENDED_PROPS = 128,
}

export function checkBluetooth(): Promise<string> {
  return BluetoothClient.checkBluetooth();
}

export function enableBluetooth() {
  return BluetoothClient.enableBluetooth();
}

export function startAdvertising(
  t: number,
  options?: AdvertiseSetting
): Promise<string> {
  return BluetoothClient.startAdvertising(t, options ?? {});
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
  permissions: Permission,
  properties: Property,
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
