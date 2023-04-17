import Foundation
import CoreBluetooth
import React

@objc(BluetoothClient)
class BluetoothClient: RCTEventEmitter, CBPeripheralManagerDelegate{
    var advertising: Bool = false
    var hasListeners: Bool = false
    var name: String = "abcdefghijklmnopqrstuvwxyz"
    var serviceMap = Dictionary<String, CBMutableService>()
    var manager: CBPeripheralManager!
    var startPromiseResolve: RCTPromiseResolveBlock?
    var startPromiseReject: RCTPromiseRejectBlock?
    var notiDevices = Array<CBCentral>()
    var blState: Any = ""
    override init(){
        super.init()
        manager = CBPeripheralManager(delegate: self, queue: nil, options: nil)
        print("BLEPeripheral init, advertising: \(advertising)")
    }
    
    // RCTEventEmitter의 하위 클래스에 startObserving 및 stopObserving을 재정의 할 수 있다.
    override func startObserving() { // 구성 요소의 관찰이 시작되면 실행된다고 한다.
        print("observing start")
        hasListeners = true
    }
    override func stopObserving(){
        print("observing stop")
        hasListeners = false
    }
    
    // 사용할 이벤트를 내보내주는 함수. 이 코드를 작성하지 않으면 이벤트가 나가지 않는다.
    // 안드로이드의  getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onReceiveData", map);
    // 와 같다.
    override func supportedEvents() -> [String]! { return ["onWarning", "onReceiveData"] }
    
    //메인쓰레드에 돌지 않아도 되기때문에 false 리턴
    override class func requiresMainQueueSetup() -> Bool { return false }
    
    
    func peripheralManagerDidUpdateState(_ peripheral: CBPeripheralManager) {
        print("changed state")
        var state: Any
              if #available(iOS 10.0, *) {
                  state = peripheral.state.rawValue.description
              } else {
                  state = peripheral.state
              }
        blState = state;
              alertJS("BT state change: \(state)")
    }
    
    // Advertising started
       func peripheralManagerDidStartAdvertising(_ peripheral: CBPeripheralManager, error: Error?) {
           if let error = error {
               alertJS("advertising failed. error: \(error)")
               advertising = false
               startPromiseReject!("AD_ERR", "advertising failed", error)
               return
           }
           advertising = true
           startPromiseResolve!(advertising)
           print("advertising succeeded!")
       }
    
    // 검색 대상에서 보여줄 이름 설정
    @objc func setName(_ name: String){
        self.name = name
    }
    
    // rejecter는 함수를 호출 할 때 파라미터 이름을 rejecter로 입력해준다.
    // isAdvertising(promiseValue, rejecter: rejectValue)
    // 광고 중인지 아닌지 확인하는 함수
    @objc func isAdvertising(_ resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock){
        resolve(advertising)
    
    }
      
    // BLE 서비스를 등록하는 함수
    @objc(addService:primary:)
    func addService(_ uuid: String, primary: Bool){
        let serviceUUID = CBUUID(string: uuid) // Corebluetooth의 UUID를 만드는 함수 Base UUID가 미리 채워진 상태로 만들어진다.
        print("serviceUUID \(serviceUUID)")
        let service = CBMutableService(type: serviceUUID, primary: primary) // 서비스를 로컬 데이터베이스에 추가하고 서비스가 게시되면 더 이상 변경할 수 없다.
        print("service \(service)")
        if(manager.state == .poweredOn){
            if(serviceMap.keys.contains(uuid) != true){
                serviceMap[uuid] = service
                serviceMap[uuid]?.characteristics = [CBCharacteristic]()
                print("서비스 등록 부분까지 들어왔다.")
                manager.add(service)    // 기기의 정보를 Central에서 요청했을 때 사용가능한 Service에 등록시켜주는 함수
            }else{
                alertJS("A \(uuid) that already exists.")
            }
        }
      
    }
    
    //서비스에 관련된 특성을 추가해주는 함수
    @objc(addCharacteristicToService:uuid:permissions:properties:data:)
    func addCharacteristicToService(_ serviceUUID: String!, uuid: String, permissions: UInt, properties: UInt, data: String){
        let charateristicUUID = CBUUID(string: uuid)
        var propertiesValue = CBCharacteristicProperties(rawValue: properties) // 해당 서비스의 특성에 적용될 유형
        var permissionVlaue = CBAttributePermissions(rawValue: permissions) // 해당 서비스의 특성에 적용될 권한. 이 유형들에 따라서 Peripheral 기기의 데이터를 read 하거나 write 할 수 있다.
        let byteData: Data = data.data(using: .utf8)! //!표의 경우 Optional 값을 강제로 해제시키는데 이럴경우 값에 nil이 들어가면 다른 코드에서 오류가 생기고 앱이 뻗을 가능성이 있으나.
        
        // Android 와 IOS의 서로 다른 값을 가진 항목을 Android 기준으로 통합
        switch permissions {
        case 1:
            permissionVlaue = CBAttributePermissions.readable
        case 2:
            permissionVlaue = CBAttributePermissions.readEncryptionRequired
        case 16:
            permissionVlaue = CBAttributePermissions.writeable
        case 32:
            permissionVlaue = CBAttributePermissions.writeEncryptionRequired
        default:
            print("지원하지 않는 값입니다.")
        }
        
        switch properties {
        case 1:
            propertiesValue = CBCharacteristicProperties.broadcast
        case 2:
            propertiesValue = CBCharacteristicProperties.read
        case 4:
            propertiesValue = CBCharacteristicProperties.writeWithoutResponse
        case 8:
            propertiesValue = CBCharacteristicProperties.write
        case 16:
            propertiesValue = CBCharacteristicProperties.notify
        case 32:
            propertiesValue = CBCharacteristicProperties.indicate
        case 64:
            propertiesValue = CBCharacteristicProperties.authenticatedSignedWrites
        case 128:
            propertiesValue = CBCharacteristicProperties.extendedProperties
        default:
            print("일치하는 값 존재하지 않음")
        }
        
        // 아래 클래스의 경우 nil 값이 들어오면 알아서 처리되는 로직이 있기 때문에 nil 체크를 해줄 필요가 없다.
//        print(propertiesValue.)
        print(CBCharacteristicProperties.write)
        print(CBAttributePermissions.writeable)
        let charateristic = CBMutableCharacteristic(type: charateristicUUID, properties: propertiesValue, value: nil, permissions: permissionVlaue)
        print("추가한 캐릭터는 \(charateristic) ")
        if(manager.state == .poweredOn){
            if(serviceMap[serviceUUID] != nil){
                
                serviceMap[serviceUUID]?.characteristics?.append(charateristic) // 파라미터로 받은 serivceUUID의 아래에 특성 값을 입력해준다.
                manager.removeAllServices() // 로컬에 등록된 서비스를 다 날린다.
                manager.remove(serviceMap[serviceUUID]!)
                manager.add(serviceMap[serviceUUID]!)   // 위의 특성을 추가한 서비스를 등록시켜준다.
            }
           
           
          
        }else {
            alertJS("권한이 설정되지 않았거나 블루투스 전원이 꺼져있습니다.")
        }
    }
    
    
    
    
    
  @objc(multiply:withB:withResolver:withRejecter:)
  func multiply(a: Float, b: Float, resolve:RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
    resolve(a*b)
  }
    
    @objc
    func startAdvertising(_ time: Int, resolve: @escaping RCTPromiseResolveBlock, rejecter reject: @escaping RCTPromiseRejectBlock) -> Void{
        
        if(manager.state != .poweredOn){
            alertJS("Bluetooth off")
            let error = NSError(domain: "", code: 100, userInfo: nil)
            reject("Bluetooth power off", "Please turn on Bluetooth in the settings.", error)
            return ;
        }
        
        // 시작에 성공하였을 때나 실패하였을 때 값을 js단으로 보내주기 위해서 클래스 변수에 데이터를 넣는다.
        startPromiseResolve = resolve
        startPromiseReject = reject
        
        let advertisementData = [
            CBAdvertisementDataLocalNameKey: name, //블루투스 검색시 나오게 될 이름
            CBAdvertisementDataServiceUUIDsKey: getServiceUUIDArray()
        ] as [String : Any] // 안에 데이터와 함께 초기화를 시켜줌
        print(advertisementData)
        manager.startAdvertising(advertisementData)
    }
    
    @objc
    func stopAdvertising(_ resolve:RCTPromiseResolveBlock, rejecter reject:RCTPromiseRejectBlock) -> Void {
        manager.stopAdvertising();
        manager.removeAllServices();
                                                                                                                                                         
        advertising = false
        resolve("stop advertising")
    }
    
    @objc(sendNotificationToDevice:charUUID:sendString:resolve:reject:)
    func sendNotificationToDevice(_ serviceUUID: String, charUUID charateristicUUID: String, sendString data: String, resolve:RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void {
        print("in noti")
        if(serviceMap.keys.contains(serviceUUID) == true){
            let service = serviceMap[serviceUUID]!
            let charateristic = getCharateristicService(service, charateristicUUID)
            if(charateristic == nil){
                alertJS("해당 서비스 UUID \(serviceUUID)는 \(charateristicUUID)을 가지고 있지 않습니다.")
            }else{
                let char = charateristic as! CBMutableCharacteristic
                print(char.subscribedCentrals)
                char.value = Data(base64Encoded: data) ?? Data()
                print(data.count)
                
                //onSubscribedCentrals 여기에 Central 객체를 넣어주면 해당 Central에게만 알림이 가게된다.
                let success = manager.updateValue(char.value!, for: char, onSubscribedCentrals: nil)
                
                if(success){
                    resolve("Send Notification Success")
                }else{
                    alertJS("\(serviceUUID) 서비스에 존재하지 않는 UUID입니다.")
                    let error = NSError(domain: "", code: 101, userInfo: nil)
                    reject("fail", "Send Notification Fail", error)
                }
            }
            
            
        }
    }
    
    
    func alertJS(_ message:Any){
        print(message)
        if(hasListeners){
            sendEvent(withName: "onWarning", body: message) // RCTEventEmitter를 통해 js로 이벤트를 전달해준다. js에서는 onWarning에 대한 이벤트를 추가하면 받아서 사용할 수 있다.
        }
    }
    
    func onReceiveData(_ data: String?, device: UUID){
        
        if(hasListeners){
            let dataDic = ["data":data, "device":device.uuidString] as [String : Any]
            print(dataDic)
            sendEvent(withName: "onReceiveData", body: dataDic)
            
        }
    }
    

  
    
    // 서비스 UUID 값을 가져오는 함수
    func getServiceUUIDArray() -> Array<CBUUID>{
        
        var serviceArray = [CBUUID]() // 배열 선언 및 초기화 CBUUID 타입으로 배열을 초기화 하고 선언한 것이다.
        for(_, service) in serviceMap{ // "_" 를 사용하는 이유는 사용하지 않는 값을 생략하고 사용하기 위해서 선언한다. wildcard pattern 이라고한다.
            serviceArray.append(service.uuid)
        }
        return serviceArray
    }
    
    func getCharateristicService(_ service: CBMutableService, _ charateristicUUID: String) -> CBCharacteristic? {
        for charateristic in service.characteristics ?? [] {
            let charUUID = CBUUID(string: charateristicUUID)
            print(charateristicUUID)
            print(charateristic.uuid)
            if(charateristic.uuid.isEqual(charUUID)){
                print("service \(service.uuid)는 특성 UUID \(charateristicUUID)를 가지고 있다.")
                if(charateristic is CBMutableCharacteristic){
                    return charateristic
                }
            }else{
                alertJS("접근하려는 특성이 일치하지 않습니다.")
            }
        }
        return nil
    }
    
    // read 요청에 따른 respond 이벤트
    func peripheralManager(_ peripheral: CBPeripheralManager, didReceiveRead request: CBATTRequest){
        print("read 요청")
        let charateristic = getCharacteristic(request.characteristic.uuid)
        
        if(charateristic != nil){
            request.value = charateristic.value
            
            manager.respond(to: request, withResult: .success)
        }else{
            print("특성이 존재하지 않아서 읽을 수 없습니다.")
            alertJS("특성이 존재하지 않아서 읽을 수 없습니다.")
        }
    }
    
    // wirte 요청에 따른 응답
    func peripheralManager(_ peripheral: CBPeripheralManager, didReceiveWrite requests: [CBATTRequest]){
        print("write 요청")
        for request in requests {
            let charateristic = getCharacteristic(request.characteristic.uuid)
            print(charateristic)
            if(charateristic == nil){
                alertJS("write에 필요한 charateristic이 존재하지 않습니다.")
            }
            if request.characteristic.uuid.isEqual(charateristic?.uuid){
                let char = charateristic as! CBMutableCharacteristic
                print(char.subscribedCentrals)
                print(request.central.identifier)
                char.value = request.value
                let val: Data? = request.value
                onReceiveData(val?.base64EncodedString(), device: request.central.identifier)
            }else{
                alertJS("액세스 하려는 특성이 일치하지 않습니다.")
            }
        }
        manager.respond(to: requests[0], withResult: .success) // 왜 0번째 배열을 응답으로 내보내는지는 확인해봐야 할 듯
    }
    
    // Respond to Subscription to Notification events
       func peripheralManager(_ peripheral: CBPeripheralManager, central: CBCentral, didSubscribeTo characteristic: CBCharacteristic) {
           print("notification 요청")
           print(central);
           let char = characteristic as! CBMutableCharacteristic
           print("subscribed centrals: \(String(describing: char.subscribedCentrals))")
       }

       // Respond to Unsubscribe events
       func peripheralManager(_ peripheral: CBPeripheralManager, central: CBCentral, didUnsubscribeFrom characteristic: CBCharacteristic) {
           let char = characteristic as! CBMutableCharacteristic
        
           print("notification 해제")
           print(central)
           print("unsubscribed centrals: \(String(describing: char.subscribedCentrals))")
       }

       // Service added
       func peripheralManager(_ peripheral: CBPeripheralManager, didAdd service: CBService, error: Error?) {
           print("add 됨")
           if let error = error {
               alertJS("error: \(error)")
               return
           }
           print("service: \(service)")
       }
    
    func getCharacteristic(_ characteristicUUID: CBUUID) -> CBCharacteristic? {
            for (uuid, service) in serviceMap {
                for characteristic in service.characteristics ?? [] {
                    if (characteristic.uuid.isEqual(characteristicUUID) ) {
                        print("서비스 \(uuid) 안에 특성 UUID인 \(characteristicUUID)값이 존재.")
                        if (characteristic is CBMutableCharacteristic) {
                            print(characteristic)
                            return characteristic
                        }
                        print("but it is not mutable")
                    } else {
                        alertJS("characteristic you are trying to access doesn't match")
                    }
                }
            }
            return nil
        }
    
    @objc
    func checkBluetooth(_ resolve:RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void{
        if(manager.state == .unsupported){
            let error = NSError(domain: "", code: 101, userInfo: nil)
            reject("not supported", "This device does not support Bluetooth.", error)
        }else{
            resolve(blState);
        }
    }
    
    @objc
    func setSendData(_ serviceUUID: String, charUUID: String, data: String, resolve:RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void{
        if let service = serviceMap[serviceUUID],
               char = getCharateristicService(service, charUUID),
               decodedData = Data(base64Encoded: data)
        {
            char.value = decodedData
            resolve("set success")
        } else {
            reject("fail", "set fail", nil)
        }
    }
    
    @objc
    func removeAllServices(_ resolve:RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void{
        manager.removeAllServices()
        serviceMap = Dictionary<String, CBMutableService>()
        resolve("remove success")
        
    }
    

    
    
}
