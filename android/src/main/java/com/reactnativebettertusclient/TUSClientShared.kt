package com.reactnativebettertusclient

import android.content.Context
import android.content.SharedPreferences
import io.tus.android.client.TusPreferencesURLStore
import io.tus.java.client.TusClient
import java.net.URL


class TUSClientShared {
  companion object {
    private var client: TusClient? = null

    @JvmStatic
    fun getInstance(context: Context): TusClient {
      if (client == null) {
        throw Error("You havent called BetterTusClient.initialize('your_endpoint') yet!")
      }

      return client as TusClient;
    }

    @JvmStatic
    fun initialize(context: Context, endpoint: String): TusClient {
      val pref: SharedPreferences = context.getSharedPreferences("tus", 0)
      val client = TusClient()
      client.uploadCreationURL = URL(endpoint)
      client.enableResuming(TusPreferencesURLStore(pref))
      this.client = client
      return client
    }
  }
}
