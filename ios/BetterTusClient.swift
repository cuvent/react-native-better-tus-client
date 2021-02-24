import TUSKit

let eventOnGlobalProgress = "onGlobalProgress";
let eventOnProgress = "onProgress";
let eventOnSuccess = "onSuccess";
let eventOnFailure = "onFailure";

@objc(BetterTusClient)
public class BetterTusClient: RCTEventEmitter, TUSDelegate {
    
    @objc(initTUSClient:)
    static func initTUSClient(endpoint: String) {
        let config = TUSConfig(withUploadURLString: endpoint, andSessionConfig: URLSessionConfiguration.default)
        TUSClient.setup(with: config)
        TUSClient.shared.delegate = self as? TUSDelegate;
    }
    
    func createUpload(withId: String, filePath: String, fileType: String, headers: [String: String] = [:]) {
        let upload = TUSUpload(withId: withId, andFilePathString: filePath, andFileType: fileType);
        TUSClient.shared.createOrResume(forUpload: upload, withCustomHeaders: headers);
    }
    
    // Progress listeners, event emitter
    public func TUSProgress(bytesUploaded uploaded: Int, bytesRemaining remaining: Int) {
        if (self.bridge != nil) {
            self.sendEvent(withName: eventOnGlobalProgress, body: [
                "bytesUploaded": uploaded,
                "bytesRemaining": remaining
            ])
        }
    }
    
    public func TUSProgress(forUpload upload: TUSUpload, bytesUploaded uploaded: Int, bytesRemaining remaining: Int) {
        if (self.bridge != nil) {
            self.sendEvent(withName: eventOnProgress, body: [
                "uploadId": upload.id,
                "bytesUploaded": uploaded,
                "bytesRemaining": remaining
            ])
        }
    }
    
    public func TUSSuccess(forUpload upload: TUSUpload) {
        if (self.bridge != nil) {
            self.sendEvent(withName: eventOnSuccess, body: [
                "uploadId": upload.id,
                "url": upload.uploadLocationURL?.absoluteString,
            ])
        }
    }
    
    public func TUSFailure(forUpload upload: TUSUpload?, withResponse response: TUSResponse?, andError error: Error?) {
        if (self.bridge != nil) {
            self.sendEvent(withName: eventOnFailure, body: [
                "uploadId": upload?.id,
                "error": error?.localizedDescription,
                "message": response?.message,
            ])
        }
    }
}
