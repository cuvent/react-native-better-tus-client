package com.reactnativebettertusclient

import java.io.File
import java.io.InputStream

fun InputStream.toFile(path: String): File {
  val file = File(path)
  if (!file.exists()) {
    file.createNewFile()
  }

  file.outputStream().use { this.copyTo(it) }
  return file
}

