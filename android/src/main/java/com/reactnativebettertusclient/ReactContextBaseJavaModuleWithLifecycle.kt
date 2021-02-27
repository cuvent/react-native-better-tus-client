package com.reactnativebettertusclient

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReactContextBaseJavaModule

/**
 * This class gets the lifecycle from the applications activity and provide the class
 * to be a LifecycleOwner.
 */
abstract class ReactContextBaseJavaModuleWithLifecycle(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext), LifecycleOwner {
  private val activityLifecycle: Lifecycle by lazy {
    ((reactContext as ReactContext).currentActivity as AppCompatActivity).lifecycle
  }


  override fun getLifecycle(): Lifecycle = activityLifecycle
}
