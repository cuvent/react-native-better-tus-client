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
  // the error message
  error?: string;
  // the http status code that was received when the upload request failed
  errorCode?: number;
  message?: string;
};

type BetterTusClientType = {
  initialize(endpoint: string): Promise<void>;
  createUpload(
    withId: string,
    filePath: string,
    fileType: string,
    metadata?: Record<string, string>,
    headers?: Record<string, string>
  ): void;
  resumeAll(): Promise<void>;
  cancel(id: string): void;
  getStateForUploadById(
    id: string
  ): Promise<
    'ENQUEUED' | 'RUNNING' | 'SUCCEEDED' | 'CANCELLED' | 'FAILED' | undefined
  >;
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

const { BetterTusClient: BetterTusClientNM } = NativeModules;

BetterTusClientNM.eventEmitter = new NativeEventEmitter(BetterTusClientNM);

const BetterTusClient = {
  ...BetterTusClientNM,
  createUpload: (withId, filePath, fileType, metadata = {}, headers = {}) => {
    return NativeModules.BetterTusClient.createUpload(
      withId,
      filePath,
      fileType,
      metadata,
      headers
    );
  },
} as BetterTusClientType;

export default BetterTusClient;
