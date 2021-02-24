import { NativeModules } from 'react-native';

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
};

const { BetterTusClient } = NativeModules;

export default BetterTusClient as BetterTusClientType;
