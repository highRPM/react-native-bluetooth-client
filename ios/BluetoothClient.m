#import "React/RCTBridgeModule.h"
#import <Foundation/Foundation.h>
#import "React/RCTEventEmitter.h"

@interface RCT_EXTERN_MODULE(BluetoothClient, RCTEventEmitter)

RCT_EXTERN_METHOD(
    isAdvertising:
    (RCTPromiseResolveBlock)resolve
    rejecter: (RCTPromiseRejectBlock)reject
)

RCT_EXTERN_METHOD(
    setName: (NSString *)string
)
RCT_EXTERN_METHOD(
    addAdvertiseService: (NSString *)uuid
    serviceData: (NSString *)serviceData
)
RCT_EXTERN_METHOD(
    addService: (NSString *)uuid
    primary:    (BOOL)primary
)
RCT_EXTERN_METHOD(
    addCharacteristicToService: (NSString *)serviceUUID
    uuid:                       (NSString *)uuid
    permissions:                (NSInteger *)permissions
    properties:                 (NSInteger *)properties
    data:                       (NSString *)data
)
RCT_EXTERN_METHOD(
    startAdvertising:(NSInteger *)time
    options : (NSDictionary *)options
    resolve :   (RCTPromiseResolveBlock)resolve
    rejecter:   (RCTPromiseRejectBlock)reject
)
RCT_EXTERN_METHOD(stopAdvertising:(RCTPromiseResolveBlock) resolve
                  rejecter:(RCTPromiseRejectBlock) reject)

RCT_EXTERN_METHOD(checkBluetooth: (RCTPromiseResolveBlock) resolve
                  reject: (RCTPromiseRejectBlock) reject
)

RCT_EXTERN_METHOD(
    sendNotificationToDevice: (NSString *) serviceUUID
                  charUUID: (NSString *)characteristicUUID
                  sendString: (NSString *)data
                  resolve: (RCTPromiseResolveBlock) resolve
                  reject: (RCTPromiseRejectBlock) reject
)

RCT_EXTERN_METHOD(setCharacteristicData: (NSString *) serviceUUID
                  charUUID: (NSString *) charUUID
                  data: (NSString *) data
                  resolve: (RCTPromiseResolveBlock) resolve
                  reject: (RCTPromiseRejectBlock) reject)

RCT_EXTERN_METHOD(removeAllServices: (RCTPromiseResolveBlock) resolve
                  reject: (RCTPromiseRejectBlock) reject)

@end
