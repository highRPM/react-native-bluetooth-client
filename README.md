# react-native-bluetooth-client

React Native 블루투스 Peripheral 모드를 가능하게 해주는 라이브러리

## Installation



```jsx
npm install react-native-bluetooth-client
```

### Android - Update Manifest

```xml
 <uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />
 <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

```

이 권한을 주지 않으면 앱이 정상적으로 작동하지 않습니다.

### IOS - Update info.plist

```xml
<key>NSBluetoothAlwaysUsageDescription</key>
<string></string>
```

위 내용을 추가해준다. `<string></string>` 사이에는 권한 요청 때 보여줄 설명을 입력한다.

## Usage



```jsx
import { checkBluetooth } from 'react-native-bluetooth-client';

// ...

//블루투스를 실행할 수 있는지 확인하는 함수
checkBluetooth()
    .then((res) => {})
    .catch((e) => {})
```

## Methods



사용가능한 함수

### `checkBluetooth`

블루투스를 사용할 수 있는지 확인하는 함수

사용 가능하면 then, 불가능하면 catch로 응답이 나온다.

**Examples**

```jsx
checkBluetooth()
    .then((res) => {})
    .catch((e) => {})
```

### `enableBluetooth`(Android 전용)

블루투스가 꺼져있는 경우 사용자의 권한을 받아서 블루투스를 켜주는 함수

**Examples**

```jsx
enableBluetooth();
```

### `startAdvertising`

Peripheral 모드를 실행하는 함수. Android 기준 받은 매개변수의 시간동안(최대 3분) 해당 함수가 실행된다.

3분이 지나면 자동으로 stopAdvertising이 된다.

**매개변수**

t(Number) → 홍보할 시간을 정한다.(1 입력하면 1분동안 홍보)  
0을 입력하면 무한으로 홍보하게 된다.
**Examples**
```jsx
startAdvertising(t)
      .then((e) => console.log(e))
      .catch((err) => console.log(err));
```

error 발생시 catch 가 실행된다.

각 error code 별 error 내용은

```jsx
ADVERTISE_FAILED_DATA_TOO_LARGE = 1;
ADVERTISE_FAILED_TOO_MANY_ADVERTISERS = 2;
ADVERTISE_FAILED_ALREADY_STARTED = 3;
ADVERTISE_FAILED_INTERNAL_ERROR = 4;
ADVERTISE_FAILED_FEATURE_UNSUPPORTED = 5;
```

### `stopAdvertising`

Peripheral 모드를 중지하는 함수. 주변 Central 기기에 더이상 검색되지 않는다.

```jsx
stopAdvertising()
      .then((e) => {
        console.log(e);
      })
      .catch((e) => console.log(e));
```

### `addAdvertiseService`

**매개변수**

UUID(String) → 서비스 등록에 사용할 UUID 값을 입력해준다.

ServiceData(String) → 홍보할 데이터를 입력해준다.

**Examples**

```jsx
addAdvertiseService('0000XXXX-0000-1000-8000-00805f9b34fb', 'SGVsbG8gV29ybGQ=');
```

### `addService`

BLE Service를 추가해주는 함수이다.

**매개변수**

UUID(String) → 서비스 등록에 사용할 UUID 값을 입력해준다.

ServiceType(Boolean) → Primary = true, Secondary = false

**Examples**

```jsx
addService('0000XXXX-0000-1000-8000-00805f9b34fb', true);
```

### `addCharacteristicToService`

Service 안에 특성을 추가해주는 함수

각 서비스는 여러개의 특성을 가질 수 있고 특성마다 다른 권한 및 속성을 줄 수 있다.

**매개변수**

serviceUUID(String) → 특성을 등록할 서비스의 UUID를 넣어준다.

UUID(String) → 등록할 특성의 UUID

permission(number) → 특성이 가지는 권한

properties(number) → 특성이 가지는 속성

**Examples**

```jsx
addCharacteristicToService(
      '0000XXXX-0000-1000-8000-00805f9b34fb',
      '0000XXXX-0000-1000-8000-00805f9b34fb',
      32,
      2 | 8 | 16,
      ''
    );
```

<aside>
💡 Permission <br/>
1 - Readable  <br/>
2 - Read Encrypted  <br/>
16 - Write  <br/>
32 - Write Encrypted  <br/>

</aside>

<br/>
<aside>
💡 Properties <br/>
1 - Broadcast <br/>
2 - Read<br/>
4 - Write No Response<br/>
8 - Write<br/>
16 - Notify<br/>
32 - Indicate<br/>
64 - Signed Write<br/>
128 - Extended Props<br/>

</aside>

### `removeAllServices`

등록한 모든 서비스를 제거하는 함수. 기본적으로 등록된 서비스는 삭제하지 않는다.

**Examples**

```jsx
removeAllServices()
              .then((e: any) => console.log(e))
              .catch((e: any) => console.log(e));
```

### `sendNotificationToDevice`

자신을 구독하고 있는 device에게 message를 일괄 전송한다.

**매개변수**

serviceUUID(string) → 서비스 UUID

UUID(string) → 서비스안에 등록된 특성의 UUID

message(string) → 자신을 구독하고있는 기기에게 보낼 데이터

**Examples**

```jsx
sendNotificationToDevice(
              '0000XXXX-0000-1000-8000-00805f9b34fb',
              '0000XXXX-0000-1000-8000-00805f9b34fb',
              'SGVsbG8gV29ybGQ=' // "Hello World"
            )
              .then((e) => console.log(e))
              .catch((e) => console.log(e));
```

## Events



### `onReceiveData`

Central 기기에서 Peripheral 기기로 write 하면 발생하는 이벤트

**매개변수**

event(any) → 바이트 배열로 데이터가 들어온다.

<aside>
💡 byte 배열을 string으로 쉽게 변환하는 법 “convert-string” 라이브러리를 다운 받는다.
다운링크 👇
https://www.npmjs.com/package/convert-string/v/0.1.0

</aside>

**Examples**

```jsx
import { bytesToString } from "convert-string";
import { NativeModules, NativeEventEmitter } from "react-native";

const BluetoothClientModule = NativeModules.BluetoothClient;
const event = new NativeEventEmitter(BluetoothClientModule);

//...
event.addListener('onReceiveData', onReceiveData);

const onReceiveData = (event: any) => {
    console.log(event);
    let data = bytesToString(event.data);
  };
```

### `setName`

Advertising 때 사용할 이름. Advertising 데이터는 31byte의 제한이 있고 등록된 서비스 및 특성에 따라 name으로 사용할 수 있는 영역의 크기가 달라지기 때문에 확인을 잘해보고 사용해야 한다.

**매개변수**

addName(string) → 지정할 이름을 넣어준다.

Bluetooth를 검색하는 기기에서 BLE_LIB라는 이름으로 검색이 된다.

**Examples**

```jsx
setName('BLE_LIB');
```

### `setCharacteristicData`

Advertising 때 사용할 이름. Advertising 데이터는 31byte의 제한이 있고 등록된 서비스 및 특성에 따라 name으로 사용할 수 있는 영역의 크기가 달라지기 때문에 확인을 잘해보고 사용해야 한다.

**매개변수**

serviceUUID(string) → 서비스 UUID

UUID(string) → 서비스안에 등록된 특성의 UUID

data(string) → Base64 형식의 데이터

**Examples**

```jsx
setCharacteristicData(
  '0000XXXX-0000-1000-8000-00805f9b34fb',
  '0000XXXX-0000-1000-8000-00805f9b34fb',
  'SGVsbG8gV29ybGQ=' // "Hello World"
)
  .then((e) => console.log(e))
  .catch((e) => console.log(e));
```

## License

MIT



Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
