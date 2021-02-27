import {
  EmitterSubscription,
  EventEmitter,
  NativeEventEmitter,
  NativeModules,
} from 'react-native';

export type BetterTusClientEventNames =
  | 'onGlobalProgress'
  | 'onProgress'
  | 'onSuccess'
  | 'onFailure';

type OnProgressEvent = {
  bytesUploaded: number;
  bytesRemaining: number;
};

export type BetterTusClientEventOnGlobalProgress = OnProgressEvent;
export type BetterTusClientEventOnProgress = OnProgressEvent & {
  uploadId: string;
};
export type BetterTusClientEventOnSuccess = {
  uploadId: string;
  url: string;
};
export type BetterTusClientEventOnFailure = {
  uploadId?: string;
  error?: string;
  message?: string;
};

type BetterTusClientType = {
  createUpload(
    withId: string,
    filePath: string,
    fileType: string,
    headers?: Record<string, string>
  ): Promise<void>;
  eventEmitter: Omit<EventEmitter, 'addListener'> & {
    addListener(
      name: BetterTusClientEventNames,
      callback: (
        payload:
          | BetterTusClientEventOnFailure
          | BetterTusClientEventOnProgress
          | BetterTusClientEventOnGlobalProgress
          | BetterTusClientEventOnSuccess
      ) => void
    ): EmitterSubscription;
  };
};

const { BetterTusClient } = NativeModules;

BetterTusClient.eventEmitter = new NativeEventEmitter(BetterTusClient);

const BetterTusClientPatched = {
  ...BetterTusClient,
  createUpload: (withId, filePath, fileType, headers = {}) => {
    return NativeModules.BetterTusClient.createUpload(
      withId,
      filePath,
      fileType,
      headers
    );
  },
} as BetterTusClientType;

export default BetterTusClientPatched;
