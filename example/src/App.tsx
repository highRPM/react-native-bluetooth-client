import * as React from 'react';

import {
  StyleSheet,
  View,
  Text,
  Platform,
  PermissionsAndroid,
  Button,
  NativeEventEmitter,
  NativeModules,
  TextInput,
} from 'react-native';
import {
  multiply,
  checkBluetooth,
  enableBluetooth,
  startAdvertising,
  stopAdvertising,
  addService,
  addCharacteristicToService,
  sendNotificationToDevice,
  setSendData,
  removeAllServices,
  setName,
} from 'react-native-bluetooth-client';
import QRCode from 'react-native-qrcode-svg';

import { bytesToString } from 'convert-string';

export default function App() {
  const [result, setResult] = React.useState<number | undefined>();
  const [rData, setRData] = React.useState<string>('');
  const event = new NativeEventEmitter(NativeModules.BluetoothClient);
  const [adName, setAdName] = React.useState<string>('test');
  React.useEffect(() => {
    let receiveEvent: any = null;
    permission().then(() => {
      checkBluetooth()
        .then((res) => {
          console.log('checkBluetooth 완료');
          console.log(res);
          if (Platform.OS === 'android') {
            enableBluetooth();
          }

          // addCharacteristicToService(
          //   '00002901-0000-1000-8000-00805f9b34fb',
          //   '00002AB1-0000-1000-8000-00805f9b34fb',
          //   4,
          //   2 | 16,
          //   ''
          // );
          // startAdvertising(10)
        })
        .catch((e) => console.log(e));
    });
    receiveEvent = event.addListener('onReceiveData', onReceiveData);
    multiply(3, 7).then(setResult);

    return () => {
      sendNotificationToDevice(
        '00002901-0000-1000-8000-00805f9b34fb',
        '00002A05-0000-1000-8000-00805f9b34fb',
        '{"status": "99"}'
      )
        .then((e) => console.log(e))
        .catch((e) => console.log(e));
      if (receiveEvent !== null) {
        receiveEvent.remove();
      }
    };
  }, []);

  const onReceiveData = (event: any) => {
    console.log(event);
    setRData(bytesToString(event.data));
  };

  const stopAd = async () => {
    stopAdvertising()
      .then((e) => {
        console.log(e);
      })
      .catch((e) => console.log(e));
  };

  const startAd = async () => {
    // setName('Purple');
    startAdvertising(10)
      .then((e) => console.log(e))
      .catch((err) => console.log(err));
  };

  const permission = async () => {
    // @ts-ignore
    await PermissionsAndroid.request(
      PermissionsAndroid.PERMISSIONS.BLUETOOTH_CONNECT,
      {
        title: 'Cool Photo App Camera Permission',
        message:
          'Cool Photo App needs access to your camera ' +
          'so you can take awesome pictures.',
        buttonNeutral: 'Ask Me Later',
        buttonNegative: 'Cancel',
        buttonPositive: 'OK',
      }
    );

    await PermissionsAndroid.request(
      PermissionsAndroid.PERMISSIONS.BLUETOOTH_SCAN,
      {
        title: 'Cool Photo App Camera Permission',
        message:
          'Cool Photo App needs access to your camera ' +
          'so you can take awesome pictures.',
        buttonNeutral: 'Ask Me Later',
        buttonNegative: 'Cancel',
        buttonPositive: 'OK',
      }
    );
    await PermissionsAndroid.request(
      PermissionsAndroid.PERMISSIONS.BLUETOOTH_ADVERTISE,
      {
        title: 'Cool Photo App Camera Permission',
        message:
          'Cool Photo App needs access to your camera ' +
          'so you can take awesome pictures.',
        buttonNeutral: 'Ask Me Later',
        buttonNegative: 'Cancel',
        buttonPositive: 'OK',
      }
    );
  };

  const sendMessage = () => {
    setSendData('How long can');
  };

  const sendNoti = () => {
    sendNotificationToDevice(
      '00002901-0000-1000-8000-00805f9b34fb',
      '00002A05-0000-1000-8000-00805f9b34fb',
      'this data is Peripheral device to Central device movemovemovemovemovemovemovemovemove'
    )
      .then((e) => console.log(e))
      .catch((e) => console.log(e));
  };

  const regiService = () => {
    addService('00002901-0000-1000-8000-00805f9b34fb', true);
    addCharacteristicToService(
      '00002901-0000-1000-8000-00805f9b34fb',
      '00002A05-0000-1000-8000-00805f9b34fb',
      4 | 32,
      2 | 8 | 16,
      ''
    );
  };

  return (
    <View style={styles.container}>
      <Text style={{ color: 'red' }}>Result: {result}</Text>

      <View>
        <Button title={'광고 중지'} onPress={stopAd} />
      </View>
      <View style={{ margin: 20 }}>
        <Button title={'광고 시작'} onPress={startAd} />
      </View>
      <View style={{ margin: 20 }}>
        <Button title={'메시지 보내기'} onPress={sendMessage} />
      </View>

      <View style={{ margin: 20 }}>
        <Button title={'noti 보내기'} onPress={sendNoti} />
      </View>
      <View style={{ margin: 20 }}>
        <Button title={'서비스등록'} onPress={regiService} />
      </View>
      <View style={{ margin: 20 }}>
        <Button title={'서비스 제거'} onPress={() => removeAllServices()} />
      </View>
      <View style={{ margin: 20 }}>
        <Button title={'홍보 이름 변경'} onPress={() => setName(adName)} />
      </View>
      <View style={{ margin: 20 }}>
        <Button
          title={'전체기기 연결 끊기'}
          onPress={() => {
            sendNotificationToDevice(
              '00002901-0000-1000-8000-00805f9b34fb',
              '00002A05-0000-1000-8000-00805f9b34fb',
              '{"status": "99"}'
            )
              .then((e) => console.log(e))
              .catch((e) => console.log(e));
          }}
        />
      </View>
      {adName !== '' ? (
        <View style={{ margin: 20 }}>
          <QRCode
            value={adName}
            size={150}
            color={'white'}
            backgroundColor={'black'}
          />
        </View>
      ) : null}
      <View>
        <TextInput
          value={adName}
          style={{ backgroundColor: 'white', width: 200, height: 25 }}
          onChangeText={(e) => {
            console.log(e);
            setAdName(e);
          }}
        />
      </View>
      <View style={{ margin: 25 }}>
        <Text style={{ fontSize: 30, color: 'white' }}>{rData}</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
