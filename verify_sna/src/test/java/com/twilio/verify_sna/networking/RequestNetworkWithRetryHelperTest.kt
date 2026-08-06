package com.twilio.verify_sna.networking

import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.NetworkRequest
import com.google.common.truth.Truth.assertThat
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
    var failure: Exception? = null

    requestNetworkWithRetryHelper(connectivityManager, networkRequest, networkCallback) {
      failure = it
    }

    // Then
    verify(exactly = 1) {
      connectivityManager.requestNetwork(networkRequest, networkCallback)
    }
    assertThat(failure).isNull()
  }

  @Test
  fun `Invoke request network schedules a retry when the first request fails`() {
    val connectivityManager: ConnectivityManager = mockk(relaxed = true)
    val networkRequest: NetworkRequest = mockk(relaxed = true)
    val networkCallback: NetworkCallback = mockk(relaxed = true)

    every {
      connectivityManager.requestNetwork(networkRequest, networkCallback)
    } throws RuntimeException("first attempt failed") andThenAnswer { }

    val requestNetworkWithRetryHelper = RequestNetworkWithRetryHelperImpl()
    var failure: Exception? = null

    requestNetworkWithRetryHelper(connectivityManager, networkRequest, networkCallback) {
      failure = it
    }
    shadowOf(Looper.getMainLooper()).idleFor(500, TimeUnit.MILLISECONDS)

    verify(exactly = 2) {
      connectivityManager.requestNetwork(networkRequest, networkCallback)
    }
    assertThat(failure).isNull()
  }

  @Test
  fun `Invoke request network reports the retry exception when the retry also fails`() {
    val connectivityManager: ConnectivityManager = mockk(relaxed = true)
    val networkRequest: NetworkRequest = mockk(relaxed = true)
    val networkCallback: NetworkCallback = mockk(relaxed = true)
    val retryException = SecurityException("CHANGE_NETWORK_STATE denied")

    every {
      connectivityManager.requestNetwork(networkRequest, networkCallback)
    } throws RuntimeException("first attempt failed") andThenThrows retryException

    val requestNetworkWithRetryHelper = RequestNetworkWithRetryHelperImpl()
    var failure: Exception? = null

    requestNetworkWithRetryHelper(connectivityManager, networkRequest, networkCallback) {
      failure = it
    }
    shadowOf(Looper.getMainLooper()).idleFor(500, TimeUnit.MILLISECONDS)

    verify(exactly = 2) {
      connectivityManager.requestNetwork(networkRequest, networkCallback)
    }
    assertThat(failure).isSameInstanceAs(retryException)
  }
}
