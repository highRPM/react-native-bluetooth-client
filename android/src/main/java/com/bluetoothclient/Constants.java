/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bluetoothclient;

import android.os.ParcelUuid;

/**
 * Constants for use in the Bluetooth Advertisements sample
 */
public class Constants {

    /**
     *이 앱으로 식별된 UUID - BLE 광고에 대한 서비스 UUID로 설정됩니다.
     *  Bluetooth에는 서비스와 관련된 UUID에 대한 특정 형식이 필요합니다.
     *  공식 사양은 {@link https:www.bluetooth.orgen-usspecificationassigned-numbersservice-discovery}에서 확인할 수 있습니다.
     */
    public static final ParcelUuid Service_UUID = ParcelUuid
            .fromString("00002901-0000-1000-8000-00805f9b34fb");

    public static final int REQUEST_ENABLE_BT = 1;

}
