package com.twilio.verify_sna.networking

import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.NetworkRequest
import android.os.Looper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class RequestNetworkWithRetryUseCaseTest {

  @Test
  fun `Invoke request network with retry performs a request successfully`() {
    // Given
    val connectivityManager: ConnectivityManager = mockk(relaxed = true)
    val networkRequest: NetworkRequest = mockk(relaxed = true)
    val networkCallback: NetworkCallback = mockk(relaxed = true)

    val requestNetworkWithRetryUseCase = RequestNetworkWithRetryUseCaseImpl()

    requestNetworkWithRetryUseCase(connectivityManager, networkRequest, networkCallback)

    // Then
    verify(exactly = 1) {
      connectivityManager.requestNetwork(networkRequest, networkCallback)
    }
  }
}
