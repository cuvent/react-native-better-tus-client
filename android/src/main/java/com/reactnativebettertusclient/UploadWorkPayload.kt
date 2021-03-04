package com.reactnativebettertusclient

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class UploadWorkPayload(
  val id: String,
  val filePath: String,
  val fileExtension: String,
  val metadata: MutableMap<String, String> = mutableMapOf(),
  val headers: Map<String, String> = mapOf()
) {

  init {
    metadata["filename"] = "${id}${if (fileExtension.startsWith(".")) fileExtension else ".${fileExtension}"}"
  }

  fun toJsonString(): String {
    return Json.encodeToString(serializer(), this);
  }

  companion object {
    @JvmStatic
    fun fromJsonString(jsonString: String): UploadWorkPayload {
      return Json.decodeFromString(serializer(), jsonString);
    }
  }
}
