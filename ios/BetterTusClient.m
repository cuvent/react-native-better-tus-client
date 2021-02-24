#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(BetterTusClient, NSObject)

RCT_EXTERN_METHOD(multiply:(float)a withB:(float)b
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(initTUSClient:(NSString *)endpoint)

@end
