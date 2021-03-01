package com.reactnativebettertusclient

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * This service is used to detect when the application
 * has been terminated.
 */
class DetectAppStopService: Service() {
  override fun onBind(intent: Intent?): IBinder? {
    return null
  }

  // The invocation of this function indicates that the app has been killed
  override fun onTaskRemoved(rootIntent: Intent?) {
    super.onTaskRemoved(rootIntent)
    // We start a new service, that will cancel all work
    val intent = Intent(this, CancelWorkService::class.java)
    // We need to launch this as a new service as this service will be killed, and
    // does not have enough time to process the cancellation.
    this.startService(intent)
    this.stopSelf()
  }
}
