import { AppRegistry } from 'react-native';
import App from './src/App';
import { name as appName } from './app.json';
import BetterTusClient from 'react-native-better-tus-client';

AppRegistry.registerComponent(appName, () => App);
BetterTusClient.initialize('https://tusd.tusdemo.net/files/');
