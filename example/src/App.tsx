import * as React from 'react';

import {
  StyleSheet,
  View,
  Text,
  Platform,
  PermissionsAndroid,
  Button,
  DeviceEventEmitter,
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
} from 'react-native-bluetooth-client';

import { bytesToString } from 'convert-string';

export default function App() {
  const [result, setResult] = React.useState<number | undefined>();
  const [rData, setRData] = React.useState<string>('');
  React.useEffect(() => {
    permission().then(() => {
      if (Platform.OS === 'android') {
        checkBluetooth()
          .then((res) => {
            console.log(res);
            enableBluetooth();
            addService('00002901-0000-1000-8000-00805f9b34fb');
            addCharacteristicToService(
              '00002901-0000-1000-8000-00805f9b34fb',
              '00002a00-0000-1000-8000-00805f9b34fb',
              16,
              8
            );

            addCharacteristicToService(
              '00002901-0000-1000-8000-00805f9b34fb',
              '00002ab1-0000-1000-8000-00805f9b34fb',
              1,
              2
            );

            // startAdvertising(10)
          })
          .catch((e) => console.log(e));
      }
      DeviceEventEmitter.addListener('onReceiveData', onReceiveData);
    });
    multiply(3, 7).then(setResult);
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
    startAdvertising(10)
      .then((e) => console.log(e))
      .catch((err) => console.log(err));
  };

  const permission = async () => {
    const granted = await PermissionsAndroid.request(
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
    setSendData(
      'hello nice me too, hi everyone this is bluetooth led app, but not production. 1 month late year hello nice me too, hi everyone this is bluetooth led app, but not production. 1 month late year hello nice me too, hi everyone this is bluetooth led app, but not production. 1 month late year hello nice me too, hi everyone this is bluetooth led app, but not production. 1 month late year'
    );
  };

  return (
    <View style={styles.container}>
      <Text>Result: {result}</Text>

      <View>
        <Button title={'광고 중지'} onPress={stopAd} />
      </View>
      <View style={{ margin: 50 }}>
        <Button title={'광고 시작'} onPress={startAd} />
      </View>
      <View style={{ margin: 50 }}>
        <Button title={'메시지 보내기'} onPress={sendMessage} />
      </View>

      <View style={{ margin: 50 }}>
        <Text style={{ fontSize: 30 }}>{rData}</Text>
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
