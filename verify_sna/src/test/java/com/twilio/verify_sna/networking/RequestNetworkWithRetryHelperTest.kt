package com.twilio.verify_sna.networking

import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.NetworkRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import android.os.Looper
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class RequestNetworkWithRetryHelperTest {

  @Test
  fun `Invoke request network with retry performs a request successfully`() {
    // Given
    val connectivityManager: ConnectivityManager = mockk(relaxed = true)
    val networkRequest: NetworkRequest = mockk(relaxed = true)
    val networkCallback: NetworkCallback = mockk(relaxed = true)

    val requestNetworkWithRetryHelper = RequestNetworkWithRetryHelperImpl()

    requestNetworkWithRetryHelper(connectivityManager, networkRequest, networkCallback)

    // Then
    verify(exactly = 1) {
      connectivityManager.requestNetwork(networkRequest, networkCallback)
    }
  }

  @Test
  fun `Invoke request network schedules a retry when the first request fails`() {
    val connectivityManager: ConnectivityManager = mockk(relaxed = true)
    val networkRequest: NetworkRequest = mockk(relaxed = true)
    val networkCallback: NetworkCallback = mockk(relaxed = true)

    // First call throws, second (retry) succeeds.
    every {
      connectivityManager.requestNetwork(networkRequest, networkCallback)
    } throws RuntimeException("first attempt failed") andThenAnswer { }

    val requestNetworkWithRetryHelper = RequestNetworkWithRetryHelperImpl()

    requestNetworkWithRetryHelper(connectivityManager, networkRequest, networkCallback)
    // Advance past the 500ms postDelayed retry.
    shadowOf(Looper.getMainLooper()).idleFor(500, TimeUnit.MILLISECONDS)

    verify(exactly = 2) {
      connectivityManager.requestNetwork(networkRequest, networkCallback)
    }
  }

  @Test
  fun `Invoke request network does not propagate when the retry also fails`() {
    val connectivityManager: ConnectivityManager = mockk(relaxed = true)
    val networkRequest: NetworkRequest = mockk(relaxed = true)
    val networkCallback: NetworkCallback = mockk(relaxed = true)

    // Both the first call and the retry fail; the helper must swallow both.
    every {
      connectivityManager.requestNetwork(networkRequest, networkCallback)
    } throws RuntimeException("attempt failed")

    val requestNetworkWithRetryHelper = RequestNetworkWithRetryHelperImpl()

    requestNetworkWithRetryHelper(connectivityManager, networkRequest, networkCallback)
    // Advancing past the 500ms retry must not throw.
    shadowOf(Looper.getMainLooper()).idleFor(500, TimeUnit.MILLISECONDS)

    verify(exactly = 2) {
      connectivityManager.requestNetwork(networkRequest, networkCallback)
    }
  }
}
