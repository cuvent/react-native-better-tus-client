package com.reactnativebettertusclient

enum class ConcurrencyMode {
  PARALLEL,
  SEQUENCE
}

data class BetterTusClientConfigType(
  var concurrencyMode: ConcurrencyMode = ConcurrencyMode.SEQUENCE
)

object BetterTusClientConfig {
  var config = BetterTusClientConfigType()
}
