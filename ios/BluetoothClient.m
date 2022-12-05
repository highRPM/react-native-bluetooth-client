#import "React/RCTBridgeModule.h"
#import <Foundation/Foundation.h>
#import "React/RCTEventEmitter.h"

@interface RCT_EXTERN_MODULE(BluetoothClient, RCTEventEmitter)

RCT_EXTERN_METHOD(multiply:(float)a withB:(float)b
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)


RCT_EXTERN_METHOD(
    isAdvertising:
    (RCTPromiseResolveBlock)resolve
    rejecter: (RCTPromiseRejectBlock)reject
)

RCT_EXTERN_METHOD(
    setName: (NSString *)string
)
RCT_EXTERN_METHOD(
    addService: (NSString *)uuid
    primary:    (BOOL)primary
)
RCT_EXTERN_METHOD(
    addCharacteristicToService:serviceUUID
    uuid:                       (NSString *)uuid
    permissions:                (NSInteger *)permissions
    properties:                 (NSInteger *)properties
    data:                       (NSString *)data
)
RCT_EXTERN_METHOD(
    startAdvertising:(NSInteger *)time
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

RCT_EXTERN_METHOD(setSendData: (NSString *) data)

RCT_EXTERN_METHOD(removeAllServices: (RCTPromiseResolveBlock) resolve
                  reject: (RCTPromiseRejectBlock) reject)

@end
