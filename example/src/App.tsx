import * as React from 'react';

import { StyleSheet, View, Button } from 'react-native';
import {
  ImagePickerResponse,
  launchImageLibrary,
} from 'react-native-image-picker';

export default function App() {
  const handleImagePickerSelection = React.useCallback(
    (response: ImagePickerResponse) => {
      console.log(response);
    },
    []
  );

  const openImagePicker = React.useCallback(() => {
    launchImageLibrary(
      {
        mediaType: 'photo',
      },
      handleImagePickerSelection
    );
  }, [handleImagePickerSelection]);

  return (
    <View style={styles.container}>
      <Button title="Select image from gallery" onPress={openImagePicker} />
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
