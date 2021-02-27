import * as React from 'react';

import { StyleSheet, View, Text, Button } from 'react-native';
import {
  ImagePickerResponse,
  launchImageLibrary,
} from 'react-native-image-picker';
import BetterTusClient, {
  BetterTusClientEventOnProgress,
} from 'react-native-better-tus-client';

export default function App() {
  const [uploadQueue, setUploadQueue] = React.useState<ImagePickerResponse[]>(
    []
  );

  const [uploading, setUploading] = React.useState(0);
  const [uploaded, setUploaded] = React.useState(0);

  const startUpload = React.useCallback(() => {
    setUploading(0);
    setUploaded(0);
    uploadQueue.forEach((response) => {
      BetterTusClient.createUpload(
        response.fileSize + '',
        // @ts-expect-error We checked earlier that this isn't null!
        response.uri,
        '.jpg',
        {}
      );
      setUploading((prev) => prev + 1);
    });
    setUploadQueue([]);
  }, [uploadQueue]);

  const openImagePicker = React.useCallback(() => {
    launchImageLibrary(
      {
        mediaType: 'photo',
      },
      (response: ImagePickerResponse) => {
        if (response.uri != null) {
          console.log(response);
          setUploadQueue((prev) => [...prev, response]);
        }
      }
    );
  }, []);

  React.useEffect(() => {
    BetterTusClient.eventEmitter.addListener('onProgress', (_payload) => {
      const payload = _payload as BetterTusClientEventOnProgress;
      console.log(
        'UPLOAD PROGRESS',
        payload.uploadId,
        payload.bytesUploaded,
        payload.bytesRemaining
      );
    });
    BetterTusClient.eventEmitter.addListener('onSuccess', (_payload) => {
      console.log('UPLOADED!!!');
      setUploaded((prev) => prev + 1);
      setUploading((prev) => prev - 1);
    });

    return () => {
      BetterTusClient.eventEmitter.removeAllListeners();
    };
  }, []);

  return (
    <View style={styles.container}>
      <Text>Added for upload {uploadQueue.length}</Text>
      <Text>Uploading {uploading}</Text>
      <Text>Uploaded {uploaded}</Text>
      <Button title="Select image from gallery" onPress={openImagePicker} />
      <Button title="Start upload" onPress={startUpload} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
