package com.reactnativebettertusclient

import android.content.Context
import android.content.SharedPreferences
import io.tus.android.client.TusPreferencesURLStore
import io.tus.java.client.TusClient
import java.net.URL


class TUSClientShared {
  companion object {
    @Volatile
    private var client: TusClient? = null;

    @JvmStatic
    fun getInstance(context: Context): TusClient {
      return client ?: synchronized(this) {
        client ?: initialize(context).also { client = it }
      }
    }

    private fun initialize(context: Context): TusClient {
      val pref: SharedPreferences = context.getSharedPreferences("tus", 0)
      val client = TusClient()
      client.uploadCreationURL = URL("https://tusd.tusdemo.net/files/")
      client.enableResuming(TusPreferencesURLStore(pref))
      return client
    }
  }
}
