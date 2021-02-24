import TUSKit

@objc(BetterTusClient)
public class BetterTusClient: NSObject {

    @objc(multiply:withB:withResolver:withRejecter:)
    func multiply(a: Float, b: Float, resolve:RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
        resolve(a*b)
    }
    
    @objc(initTUSCLient:)
    static func initTUSClient(endpoint: String) {
        let config = TUSConfig(withUploadURLString: endpoint, andSessionConfig: URLSessionConfiguration.default)
        TUSClient.setup(with: config)
    }
}
