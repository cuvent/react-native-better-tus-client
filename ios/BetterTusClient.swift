import TUSKit

@objc(BetterTusClient)
public class BetterTusClient: NSObject {
    
    @objc(initTUSClient:)
    static func initTUSClient(endpoint: String) {
        let config = TUSConfig(withUploadURLString: endpoint, andSessionConfig: URLSessionConfiguration.default)
        TUSClient.setup(with: config)
    }
}
