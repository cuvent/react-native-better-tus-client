import TUSKit

let eventOnGlobalProgress = "onGlobalProgress"
let eventOnProgress = "onProgress"
let eventOnSuccess = "onSuccess"
let eventOnFailure = "onFailure"

@objc(BetterTusClient)
public class BetterTusClient: RCTEventEmitter, TUSDelegate {
    
    @objc(initialize:resolver:rejecter:)
    func initialize(endpoint: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        let config = TUSConfig(withUploadURLString: endpoint, andSessionConfig: URLSessionConfiguration.default)
        TUSClient.setup(with: config)
        TUSClient.shared.delegate = self
    }

    @objc(createUpload:filePath:fileType:metadata:headers:)
    func createUpload(withId: String, filePath: String, fileType: String, metadata: [String: String] = [:], headers: [String: String] = [:]) {
        let upload = TUSUpload(withId: withId, andFilePathString: filePath, andFileType: fileType)
        upload.metadata = metadata
        TUSClient.shared.createOrResume(forUpload: upload, withCustomHeaders: headers)
    }
    
    @objc(getStateForUploadById:resolver:rejecter:)
    func getStateForUploadById(withId: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        let upload = TUSClient.shared.currentUploads?.first(where: { (_upload) -> Bool in
            return _upload.id == withId
        })
        let status = upload?.getStatus()
        if (status == nil) {
            resolve(nil)
            return
        }
        
        switch status {
        case .paused, .canceled:
            resolve("CANCELLED")
            break;
        case .failed:
            resolve("FAILED")
            break;
        case .finished:
            resolve("SUCCEEDED")
            break;
        case .uploading:
            resolve("RUNNING")
            break;
        default:
            resolve("ENQUEUED")
        }
    }
    
    @objc(resumeAll:rejecter:)
    func resumeAll(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        TUSClient.shared.resumeAll()
        resolve(nil)
    }

    // MARK: Progress listeners, event emitter

    @objc
    override public func supportedEvents() -> [String]! {
        return [eventOnGlobalProgress, eventOnProgress, eventOnFailure, eventOnSuccess]
    }

    // create a function that will only execute if the last
    // execution happened longer than 800ms ago.
    // This is used to not spam the JS bridge <3
    var lastFireTime = DispatchTime.now();
    func sendEventDebounced(name: String, data: [String: Any?]) {
        let diff = DispatchTime.now().rawValue - lastFireTime.rawValue;
        if (diff >= NSEC_PER_MSEC * 800) {
            if self.bridge != nil {
                self.sendEvent(withName: name, body: data)
            }
            lastFireTime = DispatchTime.now();
        }
    }

    public func TUSProgress(bytesUploaded uploaded: Int, bytesRemaining remaining: Int) {
        sendEventDebounced(name: eventOnGlobalProgress, data: [
            "bytesUploaded": uploaded,
            "bytesRemaining": remaining,
        ])
    }

    public func TUSProgress(forUpload upload: TUSUpload, bytesUploaded uploaded: Int, bytesRemaining remaining: Int) {
        sendEventDebounced(name: eventOnProgress, data: [
            "uploadId": upload.id,
            "bytesUploaded": uploaded,
            "bytesRemaining": remaining,
        ])
    }

    public func TUSSuccess(forUpload upload: TUSUpload) {
        if bridge != nil {
            sendEvent(withName: eventOnSuccess, body: [
                "uploadId": upload.id,
                "url": upload.uploadLocationURL?.absoluteString,
            ])
        }
    }

    public func TUSFailure(forUpload upload: TUSUpload?, withResponse response: TUSResponse?, andError error: Error?) {
        if bridge != nil {
            sendEvent(withName: eventOnFailure, body: [
                "uploadId": upload?.id,
                "error": error?.localizedDescription,
                "message": response?.message,
            ])
        }
    }
}
