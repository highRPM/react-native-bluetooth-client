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
    private long TIMEOUT = 0;
    private AdvertiseCallback mAdvertiseCallback;
    HashMap<String, BluetoothGattService> servicesMap;
    HashSet<BluetoothDevice> mBluetoothDevices;
    BluetoothGattServer mGattServer;
    private String sendData = "";
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
    public void setName(String name) {
        Log.d(TAG, "set name = " + name);
        this.name = name;
    }

    // Example method
    // See https://reactnative.dev/docs/native-modules-android
    @ReactMethod
    public void multiply(double a, double b, Promise promise) {
        promise.resolve(a * b);
    }


    /**
     * ??????????????? ???????????? ???????????? ???????????? ?????????
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
     * ??????????????? ??????????????? ??????????????? ????????? ?????? ??? ?????? ???????????? ?????????
     * ???????????? ????????? ????????? ???????????? ??????????????? ????????? ????????????.
     */
    @ReactMethod
    public void enableBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            ReactApplicationContext context = getReactApplicationContext();
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT, null);

        }
    }


    @ReactMethod
    public void startAdvertising(int t ,Promise promise) {
        int timeout = 0;
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
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    promise.reject("permission error", "Please allow permission to use Bluetooth.");
                    return;
                }
                bluetoothAdapter.setName(this.name);

                if (bluetoothAdapter != null) {
                    // ?????? ?????? ?????? ???????????? ?????? ?????? 3????????? ??????.
                    if (timeout <= 0) {
                        timeout = 3;
                    }
                    TIMEOUT = timeout;
                    if (bluetoothAdapter.isMultipleAdvertisementSupported()) {
                        //????????? ?????? ?????? ????????? ?????????.
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
                            mAdvertiseCallback = new SampleAdvertiseCallback(promise);

                            if (mBluetoothLeAdvertiser != null) {
                                Log.d(TAG, settings.toString());
                                Log.d(TAG, data.toString());
                                if (mAdvertiseCallback != null) {
                                    mBluetoothLeAdvertiser.startAdvertising(settings, data, scanRes, mAdvertiseCallback);


                                }
                            }
                        }

                    } else {
                        promise.reject("Bluetooth BLE not supported.");

                    }
                }
            }
        } else {
            if (mAdvertiseCallback == null) {
                bluetoothAdapter.setName(this.name);
                AdvertiseSettings settings = buildAdvertiseSettings();
                AdvertiseData data = buildAdvertiseData();
                AdvertiseData scanRes = new AdvertiseData.Builder()
                    .setIncludeDeviceName(true)
                    .build();
                mAdvertiseCallback = new SampleAdvertiseCallback(promise);

                if (mBluetoothLeAdvertiser != null) {
                    Log.d(TAG, settings.toString());
                    Log.d(TAG, data.toString());
                    if (mAdvertiseCallback != null) {
                        mBluetoothLeAdvertiser.startAdvertising(settings, data, scanRes, mAdvertiseCallback);

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
//    private void setTimeout() {
//        mHandler = new Handler();
//        Log.d(TAG, "set timeout");
//        timeoutRunnable = new Runnable() {
//            @Override
//            public void run() {
//                Log.d(TAG, "AdvertiserService has reached timeout of " + TIMEOUT + " milliseconds, stopping advertising.");
//                sendFailureIntent(ADVERTISING_TIMED_OUT);
//                ReactApplicationContext context = getReactApplicationContext();
//                context.stopService(enableBtIntent);
//            }
//        };
//        mHandler.postDelayed(timeoutRunnable, TIMEOUT);
//    }


    /**
     * ????????? ??????????????? ?????? ???????????? ??????
     */
    private int sendFailureIntent(int errorCode) {
        return errorCode;
    }

    /**
     * ???????????? ??????????????? ????????? AdvertiseSettings ????????? ????????????(????????? ????????? ???????????? ??????) ??? ????????? ?????? ???????????? ?????? ?????? ????????? ???????????? ????????? ?????? ?????? ??????????????? ?????????????????????.
     * @return
     */
    private AdvertiseSettings buildAdvertiseSettings() {
        AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);
        settingsBuilder.setTimeout((int) TimeUnit.MILLISECONDS.convert(TIMEOUT, TimeUnit.MINUTES));
        settingsBuilder.setConnectable(true);
        return settingsBuilder.build();
    }


    /**
     * ????????? UUID ??? ?????? ????????? ???????????? AdvertiseData ????????? ???????????????.
     */
    private AdvertiseData buildAdvertiseData() {

        /**
         * ??????: BLE ????????? ?????? ????????? ???????????? 31???????????? ????????? ????????? ????????????.
         * ???????????? UUID, ?????? ??????, ?????? ????????? ?????? ???????????? ???????????? ???????????? AdvertiseData??? ????????? ?????? ?????? ???????????????.
         * ??? ????????? ???????????? ????????? ???????????? ?????? ?????? ?????? AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE??? ?????? ???????????????.
         * AdvertiseCallback ????????? onStartFailure() ??????????????? ??? ????????? ??????????????????.
         */

        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        dataBuilder.addServiceUuid(Constants.Service_UUID);
        dataBuilder.setIncludeDeviceName(true);

        /* For example - this will cause advertising to fail (exceeds size limit) */
//        String failureData = "1";

        return dataBuilder.build();
    }


    /**
     * ?????? ?????? ??? ????????? ?????? ?????? ?????? ?????? ??????.
     * AdvertiserFragment??? ?????? ????????? ???????????? ?????? ????????? ???????????????????????? ??? ???????????? ???????????????.
     */
    private class SampleAdvertiseCallback extends AdvertiseCallback {

        private Promise promise;

        public SampleAdvertiseCallback(Promise promise) {
            this.promise = promise;
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);

            Log.d(TAG, "Advertising failed");
            promise.reject("error", errorCode + "");
            sendFailureIntent(errorCode);
            ReactApplicationContext context = getReactApplicationContext();
            context.stopService(enableBtIntent);

        }

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            promise.resolve("Bluetooth BLE Advertising started.");
            Log.d(TAG, "Advertising successfully started");
        }
    }

    /**
     * ????????? ???????????? ?????????. ????????? ???????????? ???????????? ?????? ???????????? ??????????????? ?????? ????????? ??????????????????.
     * @param promise
     */
    @ReactMethod
    private void stopAdvertising(Promise promise) {
        try {
            Log.d(TAG, "Service: Stopping Advertising");
            if (mBluetoothLeAdvertiser != null) {
                if (ActivityCompat.checkSelfPermission(getReactApplicationContext(), Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                    promise.reject("error", "Please allow permission to use Bluetooth.");
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
                    // TODO ???????????? ?????? ?????? ????????? ??????
                    Log.d(TAG, "devices:"+mBluetoothDevices.toString());
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    // TODO ???????????? ?????? ?????? ?????? ????????? ??????
                    Log.d(TAG, "devices:"+mBluetoothDevices.toString());
                    mBluetoothDevices.remove(device);
                }
            } else {
                mBluetoothDevices.remove(device);
            }
        }
        @SuppressLint("MissingPermission")
        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
                                                BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "???????????? ???????");
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            if (offset != 0) {

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

        @SuppressLint("MissingPermission")
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
            Log.d(TAG, "???????????? ~~~~~~~~");
            Log.d(TAG, map.toString());
            if (responseNeeded) {

                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
                getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("onReceiveData", map);
            }
        }
    };

    @SuppressLint("MissingPermission")
    @ReactMethod
    public void sendNotificationToDevice(String serviceUUID, String charUUID, String message, Promise promise){
        Log.d(TAG, "???????????? ~~~~~~~~");
        Log.d(TAG, serviceUUID+"/"+charUUID+"/"+ message);
        byte[] decoded = message.getBytes(StandardCharsets.UTF_8);

        try{
            // ????????? uuid??? ????????? ?????? uuid??? ???????????? ??????
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

}
