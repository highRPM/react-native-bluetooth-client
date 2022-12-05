package com.bluetoothclient;

import static androidx.core.content.PackageManagerCompat.LOG_TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.AdvertisingSet;
import android.bluetooth.le.AdvertisingSetCallback;
import android.bluetooth.le.AdvertisingSetParameters;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.provider.SyncStateContract;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@ReactModule(name = BluetoothClientModule.NAME)
public class BluetoothClientModule extends ReactContextBaseJavaModule {

    private static final String TAG = BluetoothClientModule.class.getSimpleName();
    public static final String NAME = "BluetoothClient";
    public static final int ADVERTISING_TIMED_OUT = 6;
    private BluetoothAdapter bluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private Handler mHandler;
    private Intent enableBtIntent;
    private Runnable timeoutRunnable;
    private long TIMEOUT = TimeUnit.MILLISECONDS.convert(3, TimeUnit.MINUTES);
    private AdvertiseCallback mAdvertiseCallback;
    HashMap<String, BluetoothGattService> servicesMap;
    HashSet<BluetoothDevice> mBluetoothDevices;
    BluetoothGattServer mGattServer;
    private String sendData ="";
    private String name = "RnBLE";
    private BluetoothGatt mBluetoothGatt;
    public BluetoothClientModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.servicesMap = new HashMap<String, BluetoothGattService>();
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }

    @ReactMethod
    public void setName(String name){
        Log.d(TAG, "set name = "+ name);
        this.name = name;
//        ReactApplicationContext context = getReactApplicationContext();
//        Intent enableBtIntent = new Intent(BluetoothDevice.ACTION_NAME_CHANGED);
//        context.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT, null);
    }

    // Example method
    // See https://reactnative.dev/docs/native-modules-android
    @ReactMethod
    public void multiply(double a, double b, Promise promise) {
        promise.resolve(a * b);
    }


    /**
     * 블루투스를 지원하는 기기인지 확인하는 메소드
     *
     * @param promise
     */
    @ReactMethod
    public void checkBluetooth(Promise promise) {
        if (bluetoothAdapter == null) {
            promise.reject("bluetooth not supported", "...");
        } else {
            promise.resolve("supported");
        }
    }

    /**
     * 블루투스가 꺼져있으면 블루투스를 활성화 시킬 수 있게 도와주는 메소드
     * 구현하는 측에서 권한을 설정해야 정상적으로 사용이 가능하다.
     */
    @ReactMethod
    public void enableBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            ReactApplicationContext context = getReactApplicationContext();
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT, null);

        }
    }


    @SuppressLint("MissingPermission")
    @ReactMethod
    public void startAdvertising(int timeout, Promise promise) {

        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.d(TAG, "ad start");
        if (mBluetoothLeAdvertiser == null) {
            Log.d(TAG, "advertiser not null");
            ReactApplicationContext context = getReactApplicationContext();
            BluetoothManager mBluetoothManger = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);

            if (mBluetoothManger != null) {
                Log.d(TAG, "manager not null");
                Log.d(TAG, this.name);
                bluetoothAdapter = mBluetoothManger.getAdapter();
                bluetoothAdapter.setName(this.name);

                if (bluetoothAdapter != null) {
                    // 기본 광고 시간 관련해서 기본 값을 10분으로 준다.
                    if (timeout <= 0) {
                        timeout = 3;
                    }
                    TIMEOUT = TimeUnit.MILLISECONDS.convert(timeout, TimeUnit.MINUTES);
                    if (bluetoothAdapter.isMultipleAdvertisementSupported()) {
                        //여기에 광고 시작 코드를 넣는다.
                        mBluetoothDevices = new HashSet<>();
                        mGattServer = mBluetoothManger.openGattServer(getReactApplicationContext(), mGattServerCallback);
                        for (BluetoothGattService service : this.servicesMap.values()) {
                            mGattServer.addService(service);
                        }
                        mBluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();

                        if (mAdvertiseCallback == null) {
                            AdvertiseSettings settings = buildAdvertiseSettings();
                            AdvertiseData data = buildAdvertiseData();
                            AdvertiseData scanRes = new AdvertiseData.Builder()
                                .setIncludeDeviceName(true)
                                .build();
                            mAdvertiseCallback = new SampleAdvertiseCallback();

                            if (mBluetoothLeAdvertiser != null) {
                                Log.d(TAG, settings.toString());
                                Log.d(TAG, data.toString());
                                if(mAdvertiseCallback != null){
                                    mBluetoothLeAdvertiser.startAdvertising(settings, data, scanRes,mAdvertiseCallback);

                                    promise.resolve("Bluetooth BLE Advertising start.");
                                }
                            }
                        }

                    } else {
                        promise.reject("Bluetooth BLE not supported.");

                    }
                }
            }
        }else{
            if (mAdvertiseCallback == null) {
                bluetoothAdapter.setName(this.name); 
                AdvertiseSettings settings = buildAdvertiseSettings();
                AdvertiseData data = buildAdvertiseData();
                AdvertiseData scanRes = new AdvertiseData.Builder()
                    .setIncludeDeviceName(true)
                    .build();
                mAdvertiseCallback = new SampleAdvertiseCallback();

                if (mBluetoothLeAdvertiser != null) {
                    Log.d(TAG, settings.toString());
                    Log.d(TAG, data.toString());
                    if(mAdvertiseCallback != null){
                        mBluetoothLeAdvertiser.startAdvertising(settings, data, scanRes,mAdvertiseCallback);

                        promise.resolve("Bluetooth BLE Advertising start.");
                    }
                }
            }
        }

    }


    /**
     * Starts a delayed Runnable that will cause the BLE Advertising to timeout and stop after a
     * set amount of time.
     */
    private void setTimeout() {
        mHandler = new Handler();
        Log.d(TAG, "set timeout");
        timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "AdvertiserService has reached timeout of " + TIMEOUT + " milliseconds, stopping advertising.");
                sendFailureIntent(ADVERTISING_TIMED_OUT);
                ReactApplicationContext context = getReactApplicationContext();
                context.stopService(enableBtIntent);
            }
        };
        mHandler.postDelayed(timeoutRunnable, TIMEOUT);
    }


    /**
     * 광고가 실패했다는 것을 보내주는 함수
     */
    private int sendFailureIntent(int errorCode) {
        return errorCode;
    }

    /**
     * 저전력을 사용하도록 설정된 AdvertiseSettings 개체를 반환하고(배터리 수명을 유지하기 위해) 이 코드는 자체 타임아웃 실행 가능 파일을 사용하기 때문에 기본 제공 타임아웃을 비활성화합니다.
     * @return
     */
    private AdvertiseSettings buildAdvertiseSettings() {
        AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);
        settingsBuilder.setTimeout((int) TimeUnit.MILLISECONDS.convert(2, TimeUnit.MINUTES));
        settingsBuilder.setConnectable(true);
        return settingsBuilder.build();
    }


    /**
     * 서비스 UUID 및 장치 이름을 포함하는 AdvertiseData 개체를 반환합니다.
     */
    private AdvertiseData buildAdvertiseData() {

        /**
         * 참고: BLE 광고를 통해 전송된 패킷에는 31바이트로 엄격한 제한이 있습니다.
         * 여기에는 UUID, 장치 정보, 임의 서비스 또는 제조업체 데이터를 포함하여 AdvertiseData에 입력된 모든 것이 포함됩니다.
         * 이 제한을 초과하여 패킷을 보내려고 하면 오류 코드 AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE와 함께 실패합니다.
         * AdvertiseCallback 구현의 onStartFailure() 메소드에서 이 오류를 포착하십시오.
         */

        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        dataBuilder.addServiceUuid(Constants.Service_UUID);
        dataBuilder.setIncludeDeviceName(true);

        /* For example - this will cause advertising to fail (exceeds size limit) */
//        String failureData = "1";

        return dataBuilder.build();
    }


    /**
     * 광고 시작 후 사용자 정의 콜백 성공 또는 실패.
     * AdvertiserFragment에 의해 선택될 인텐트의 오류 코드를 브로드캐스트하고 이 서비스를 중지합니다.
     */
    private class SampleAdvertiseCallback extends AdvertiseCallback {

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);

            Log.d(TAG, "Advertising failed");
            sendFailureIntent(errorCode);
            ReactApplicationContext context = getReactApplicationContext();
            context.stopService(enableBtIntent);

        }

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.d(TAG, "Advertising successfully started");
        }
    }

    /**
     * 홍보를 중지하는 메소드. 광고를 중지하기 위해서는 해당 메소드를 실행하거나 앱을 완전히 종료해야한다.
     * @param promise
     */
    @ReactMethod
    private void stopAdvertising(Promise promise) {
        try {
            Log.d(TAG, "Service: Stopping Advertising");
            if (mBluetoothLeAdvertiser != null) {
                if (ActivityCompat.checkSelfPermission(getReactApplicationContext(), Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    promise.reject("permission denied", "permission denied");
                    return;
                }
                mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
                mAdvertiseCallback = null;

                promise.resolve("ok");
            }
        } catch (Exception e) {
            promise.reject("error", e);
        }
    }

    @ReactMethod
    public void addService(String uuid, boolean primary) {
        UUID SERVICE_UUID = UUID.fromString(uuid);
//        int type = BluetoothGattService.SERVICE_TYPE_PRIMARY;
        int type = primary == true ? 0 : 1;
        BluetoothGattService tempService = new BluetoothGattService(SERVICE_UUID, type);
        Log.d(TAG, tempService.getCharacteristics().toString());
        Log.d(TAG,  tempService.getIncludedServices().toString());
        if (!this.servicesMap.containsKey(uuid))
            this.servicesMap.put(uuid, tempService);
    }

    @ReactMethod
    public void addCharacteristicToService(String serviceUUID, String uuid, Integer permissions, Integer properties, String data) {
        UUID CHAR_UUID = UUID.fromString(uuid);
        BluetoothGattCharacteristic tempChar = new BluetoothGattCharacteristic(CHAR_UUID, properties, permissions);
        this.servicesMap.get(serviceUUID).addCharacteristic(tempChar);
    }


    private final BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, final int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            Log.d(TAG, "status:"+status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    mBluetoothDevices.add(device);
                    // TODO 디바이스 연결 관련 이벤트 추가
                    Log.d(TAG, "devices:"+mBluetoothDevices.toString());
//                    getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
//                        .emit("onConnectedDevice", device.toString());
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
//                    getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
//                        .emit("onDisconnectedDevice", device.toString());
                    // TODO 디바이스 연결 해제 관련 이벤트 추가
                    Log.d(TAG, "devices:"+mBluetoothDevices.toString());
                    mBluetoothDevices.remove(device);
                }
            } else {
                mBluetoothDevices.remove(device);
            }
        }
        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
                                                BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "데이터를 요청?");
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            if (offset != 0) {
                if (ActivityCompat.checkSelfPermission(getReactApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_INVALID_OFFSET, offset,
                    /* value (optional) */ "hi".getBytes(StandardCharsets.UTF_8));
                return;
            }
            characteristic.setValue(sendData.getBytes(StandardCharsets.UTF_8));
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS,
                offset, characteristic.getValue());
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                                 BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded,
                                                 int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite,
                responseNeeded, offset, value);
            characteristic.setValue(value);
            WritableMap map = Arguments.createMap();
            WritableArray data = Arguments.createArray();
            for (byte b : value) {
                data.pushInt((int) b);
            }
            map.putArray("data", data);
            map.putString("device", device.toString());
            Log.d(TAG, "데이터는 ~~~~~~~~");
            Log.d(TAG, map.toString());
            if (responseNeeded) {
                if (ActivityCompat.checkSelfPermission(getReactApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
                getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("onReceiveData", map);
            }
        }
    };

    @SuppressLint("MissingPermission")
    @ReactMethod
    public void sendNotificationToDevice(String serviceUUID, String charUUID, String message, Promise promise){
        Log.d(TAG, "데이터는 ~~~~~~~~");
        Log.d(TAG, serviceUUID+"/"+charUUID+"/"+ message);
        byte[] decoded = message.getBytes(StandardCharsets.UTF_8);

        try{
            // 서비스 uuid를 가지고 특성 uuid를 꺼내오는 과정
            BluetoothGattCharacteristic characteristic = servicesMap.get(serviceUUID).getCharacteristic(UUID.fromString(charUUID));
            characteristic.setValue(decoded);
            boolean indicate = (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) == BluetoothGattCharacteristic.PROPERTY_INDICATE;

            for(BluetoothDevice device : mBluetoothDevices){
                mGattServer.notifyCharacteristicChanged(device, characteristic, false);
                Log.d(TAG, "device = "+device);
            }
            promise.resolve("Send Notification Success");
        }catch (Exception e){
            promise.reject("fail", "Send Notification Fail");
        }

    }

    @ReactMethod
    public void setSendData(String data){
        this.sendData = data;
    }

    @SuppressLint("MissingPermission")
    @ReactMethod
    public void removeAllServices(Promise promise){
        try{
            for (BluetoothGattService service : this.servicesMap.values()) {
                mGattServer.removeService(service);
            }
            mGattServer.clearServices();
            this.servicesMap = new HashMap<String, BluetoothGattService>();
            promise.resolve("remove success");
        }catch (Exception e){
            promise.reject("remove fail");
        }
    }

    @ReactMethod
    public void deviceDisconnect(){

    }


}
