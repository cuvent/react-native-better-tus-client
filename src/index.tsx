import { NativeModules } from 'react-native';

type BetterTusClientType = {
  multiply(a: number, b: number): Promise<number>;
};

const { BetterTusClient } = NativeModules;

export default BetterTusClient as BetterTusClientType;
