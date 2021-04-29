package com.reactnativebettertusclient

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import io.tus.java.client.ProtocolException
import io.tus.java.client.TusExecutor
import io.tus.java.client.TusUpload
import io.tus.java.client.TusUploader
import java.io.File

//TODO: implement onStopped (trigger failure so that JS receives failure event?)
class UploadWorker(context: Context, parameters: WorkerParameters) :
  CoroutineWorker(context, parameters) {

  private val tusCacheDir = applicationContext.cacheDir.absolutePath + File.separator + "TUS" + File.separator

  init {
    // Make sure that the cache dir exists
    val cacheDir = File(tusCacheDir);
    if (!cacheDir.exists()) {
      cacheDir.mkdir()
    }
  }

  override suspend fun doWork(): Result {
    val serializedUpload = inputData.getString(KEY_UPLOAD)
      ?: return Result.failure()
    val payload = UploadWorkPayload.fromJsonString(serializedUpload)
    Log.d("UploadWorker", "Queued work with id ${payload.id}, worker id ${this.id}")

    try {
        val upload = prepareUpload(payload)
        val uploadedUrl = syncUpload(upload)

        cleanup(getUploadPathForPayload(payload))

        if (uploadedUrl == null) {
            return Result.failure()
        }

        return Result.success(workDataOf(
                KEY_UPLOADED_URL to uploadedUrl
        ))
    } catch (e: ProtocolException) {
        e.printStackTrace()
        Log.e("UploadWorker", "Failed to upload file, error code ${e.causingConnection.responseCode}")
        return Result.failure(workDataOf(
                KEY_HTTP_ERROR_CODE to e.causingConnection.responseCode,
                KEY_ERROR_MESSAGE to e.message
        ))
    }
  }

  private fun prepareUpload(payload: UploadWorkPayload): TusUpload {
    // we need to copy the file to a save place
    val inputStream = applicationContext.contentResolver.openInputStream(Uri.parse(payload.filePath))
      ?: throw Error("Can't open input stream from file ${payload.filePath}")
    val file = inputStream.toFile(getUploadPathForPayload(payload))
    Log.d("UploadWorker", "Moving file to ${file.absolutePath}")
    val upload = TusUpload(file)
    upload.metadata = payload.metadata
    return upload
  }

  private fun syncUpload(upload: TusUpload): String? {
    val selfRef = this
    var uploadedUrl: String? = null
    // We wrap our uploading code in the TusExecutor class which will automatically catch
    // exceptions and issue retries with small delays between them and take fully
    // advantage of tus' resume-ability to offer more reliability.
    val executor = object : TusExecutor() {
      override fun makeAttempt() {
        // First try to resume an upload. If that's not possible we will create a new
        // upload and get a TusUploader in return. This class is responsible for opening
        // a connection to the remote server and doing the uploading.
        val uploader: TusUploader = TUSClientShared.getInstance(applicationContext).resumeOrCreateUpload(upload)

        // Upload the file in chunks of 1MB sizes.
        uploader.chunkSize = 1024 * 1000

        // Upload the file as long as data is available. Once the
        // file has been fully uploaded the method will return -1
        do {
          // Calculate the progress using the total size of the uploading file and
          // the current offset.
          val totalBytes: Long = upload.size
          val bytesUploaded = uploader.offset
          selfRef.setProgressAsync(bytesUploaded.toDouble(), totalBytes.toDouble())
        } while (uploader.uploadChunk() > -1)

        uploadedUrl = uploader.uploadURL.toString()

        // Allow the HTTP connection to be closed and cleaned up
        uploader.finish()
      }
    }
    executor.makeAttempts()
    return uploadedUrl
  }

  fun setProgressAsync(uploaded: Double, total: Double) {
    setProgressAsync(
            Data.Builder()
                    .putDouble(KEY_PROGRESS_UPLOADED, uploaded)
                    .putDouble(KEY_PROGRESS_TOTAL, total)
                    .build()
    )
  }

  private fun cleanup(filePath: String) {
    val file = File(filePath)
    if (file.exists()) {
      file.delete()
    }
  }

  private fun getUploadPathForPayload(payload: UploadWorkPayload): String {
    return tusCacheDir + payload.id + payload.fileExtension
  }

  companion object {
    const val KEY_UPLOAD = "KEY_UPLOAD"
    const val KEY_PROGRESS_UPLOADED = "KEY_PROGRESS_UPLOADED"
    const val KEY_PROGRESS_TOTAL = "KEY_PROGRESS_TOTAL"
    const val KEY_UPLOADED_URL = "KEY_URL"
    const val KEY_HTTP_ERROR_CODE = "KEY_HTTP_ERROR_CODE"
    const val KEY_ERROR_MESSAGE = "KEY_ERROR_MESSAGE"
  }
}

