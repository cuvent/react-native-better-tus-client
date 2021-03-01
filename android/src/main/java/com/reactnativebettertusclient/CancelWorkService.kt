package com.reactnativebettertusclient

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.work.WorkManager
import androidx.work.await
import kotlinx.coroutines.runBlocking

/**
 * Creating this service cancels all work
 * tagged with {@see BetterTusClientModule.KEY_WORKER_TAG}
 * in the WorkManager.
 */
class CancelWorkService: Service() {
  override fun onBind(intent: Intent?): IBinder? {
    return null
  }


  private suspend fun cancelAllTasks() {
    Log.d(DetectAppStopService::class.simpleName, "[BetterTusClient] detecting application termination, canceling all uploads!")
    val cancelOperation = WorkManager.getInstance(this).cancelAllWorkByTag(BetterTusClientModule.KEY_WORKER_TAG)
    cancelOperation.await()
    Log.d(DetectAppStopService::class.simpleName, "[BetterTusClient] Canceled all uploads!")
  }

  // As this service is called when the application is being killed
  // using onStartCommand doesn't work here.
  override fun onCreate() {
    super.onCreate()
    val selfRef = this
    runBlocking {
      cancelAllTasks()
      selfRef.stopSelf()
    }
  }
}
