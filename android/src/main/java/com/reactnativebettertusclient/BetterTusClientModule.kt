package com.reactnativebettertusclient

import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.work.*
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import kotlinx.coroutines.launch
import java.util.*


class BetterTusClientModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModuleWithLifecycle(reactContext) {
  private val moduleName = "BetterTusClient";

  override fun getName(): String {
    return moduleName
  }

  private val eventEmitter by lazy { reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java) }

  @ReactMethod
  fun createUpload(withId: String, filePath: String, fileType: String, headers: ReadableMap?) {
    val upload = UploadWorkPayload(withId, filePath, fileType, headers.toStringMap())
    lifecycleScope.launch {
      enqueueUploadAsWorkRequest(upload)
    }
  }

  @ReactMethod
  fun resumeAll(promise: Promise) {
    // get all enqueued and running or blocked work
    val query = WorkQuery.Builder
      .fromTags(listOf(KEY_WORKER_TAG))
      .addStates(listOf(WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING, WorkInfo.State.BLOCKED))
      .build()

    // two things are happening here:
    // - By calling `getInstance` the WorkManager gets (eventually) initiated, and thus the uploads
    //   will resume shortly.
    // - We get all "pending" work, so we can start observing it.
    val workInfos = WorkManager.getInstance(reactApplicationContext).getWorkInfos(query).get()

    lifecycleScope.launch {
      workInfos.forEach { workInfo ->
        val uploadId = workInfo.tags.firstOrNull {
          it != KEY_WORKER_TAG && UploadWorker.javaClass.canonicalName?.contains(it, true) == false
        }
        if (uploadId != null) {
          startObserving(workInfo.id, uploadId)
        }
      }

      promise.resolve(null)
    }
  }

  @ReactMethod
  fun getStateForUploadById(uploadId: String, promise: Promise) {
    val info = WorkManager.getInstance(reactApplicationContext).getWorkInfosByTag(uploadId).get()
    val state = info.first()?.state
    promise.resolve(state?.name)
  }

  private suspend fun enqueueUploadAsWorkRequest(upload: UploadWorkPayload) {
    val uploadSerialized = upload.toJsonString()

    // TODO: should we check that a upload with ID can't be added multiple times?
    val uploadWorkRequest = OneTimeWorkRequestBuilder<UploadWorker>()
      .setInputData(workDataOf(UploadWorker.KEY_UPLOAD to uploadSerialized))
      .addTag(KEY_WORKER_TAG)
      .addTag(upload.id)
      .build()

    if (BetterTusClientConfig.config.concurrencyMode == ConcurrencyMode.PARALLEL) {
      WorkManager
        .getInstance(reactApplicationContext)
        .enqueue(uploadWorkRequest)
        .await()
    } else {
      WorkManager
        .getInstance(reactApplicationContext)
        .enqueueUniqueWork(KEY_UNIQUE_WORKER, ExistingWorkPolicy.APPEND, uploadWorkRequest)
        .await()
    }
    startObserving(uploadWorkRequest.id, upload.id)
  }


  private fun startObserving(id: UUID, uploadId: String) {
    val uploadLiveData = WorkManager
      .getInstance(reactApplicationContext)
      .getWorkInfoByIdLiveData(id)

    // Create an observer that will emit events to JS
    val observer = Observer<WorkInfo> {
      val eventObject = Arguments.createMap()
      eventObject.putString("uploadId", uploadId)

      if (it.state == WorkInfo.State.RUNNING) {
        eventObject.putDouble("bytesUploaded", it.progress.getDouble(UploadWorker.KEY_PROGRESS_UPLOADED, 0.0))
        eventObject.putDouble("bytesRemaining", it.progress.getDouble(UploadWorker.KEY_PROGRESS_TOTAL, 0.0))
        eventEmitter.emit(EVENT_PROGRESS, eventObject)
      }

      if (it.state == WorkInfo.State.SUCCEEDED) {
        eventObject.putString("url", it.outputData.getString(UploadWorker.KEY_UPLOADED_URL))
        eventEmitter.emit(EVENT_SUCCESS, eventObject)
      }

      if (it.state == WorkInfo.State.FAILED) {
        eventEmitter.emit(EVENT_FAILURE, eventObject)
      }
    }

    uploadLiveData.observe(this, observer)
  }

  companion object {
    const val KEY_UNIQUE_WORKER = "upload-worker"
    const val KEY_WORKER_TAG = "upload"
    const val EVENT_PROGRESS = "onProgress"
    const val EVENT_SUCCESS = "onSuccess"
    const val EVENT_FAILURE = "onFailure"
  }
}
