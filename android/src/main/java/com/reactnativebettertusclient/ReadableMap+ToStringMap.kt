package com.reactnativebettertusclient

import com.facebook.react.bridge.ReadableMap

fun ReadableMap?.toStringMap(): Map<String, String> {
  val map = mutableMapOf<String, String>();
  // load RN ReadableMap to kotlin map
  this?.entryIterator?.forEach { if (it.value is String) map[it.key] = it.value as String }
  return map;
}
