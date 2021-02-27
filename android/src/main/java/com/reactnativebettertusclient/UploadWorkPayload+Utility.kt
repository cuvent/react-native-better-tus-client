package com.reactnativebettertusclient

import com.facebook.react.bridge.ReadableMap
import java.lang.Error

/**
 * Converts an "upload object" from the JS world, namely a {@see ReadableMap},
 * to a {@see UploadWorkPayload} object instance.
 */
@Throws(Error::class)
fun UploadWorkPayload.Companion.fromReadableMap(map: ReadableMap): UploadWorkPayload {
  val id = map.getString("id")
  val filePath = map.getString("filePath")
  val fileExtension = map.getString("fileType")
  val headers = map.getMap("headers")

  if (id == null || filePath == null || fileExtension == null) {
    throw Error("Can't convert UploadWorkPayload from ReadableMap if attributes are null.");
  }

  val headersMap = headers.toStringMap()

  return UploadWorkPayload(id, filePath, fileExtension, headersMap)

}
