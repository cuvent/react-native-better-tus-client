#if __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#else
#import <React/RCTBridgeModule.h>
#endif

#import "BetterTusClient-Bridging-Header.h"

@interface RCT_EXTERN_MODULE(BetterTusClient, NSObject)

+ (BOOL) requiresMainQueueSetup {
    return NO;
}

RCT_EXTERN_METHOD(createUpload:(NSString *)withId
                  filePath:(NSString *)filePath
                  fileType:(NSString *)fileType
                  headers:(NSDictionary *)headers
                  )

RCT_EXTERN_METHOD(getStateForUploadById:(NSString *)withId
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(resumeAll:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
                  )

@end
