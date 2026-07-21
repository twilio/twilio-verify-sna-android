package com.twilio.verify_sna.networking

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.google.common.truth.Truth.assertThat
import com.twilio.verify_sna.common.TwilioVerifySnaException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext

@RunWith(RobolectricTestRunner::class)
class VerifySnaNetworkCallbackProviderTest {

  private val url = "test.url.com"
  private val networkRequestProvider: NetworkRequestProvider = mockk(relaxed = true)
  private val connectivityManager: ConnectivityManager = mockk(relaxed = true)
  private val network: Network = mockk(relaxed = true)

  private val results = mutableListOf<Result<NetworkRequestResult>>()
  private val continuation = object : Continuation<NetworkRequestResult> {
    override val context = EmptyCoroutineContext
    override fun resumeWith(result: Result<NetworkRequestResult>) {
      results.add(result)
    }
  }

  private val provider = VerifySnaNetworkCallbackProviderImpl(networkRequestProvider)

  private fun validatedCapabilities(): NetworkCapabilities {
    val capabilities: NetworkCapabilities = mockk(relaxed = true)
    every {
      capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    } returns true
    return capabilities
  }

  @Test
  fun `Resumes the continuation only once when capabilities change multiple times`() {
    val expectedResult = NetworkRequestResult(status = 200, message = "Success")
    every { networkRequestProvider.performRequest(url, network) } returns expectedResult

    val callback = provider.provide(url, continuation, connectivityManager)
    val capabilities = validatedCapabilities()

    // Simulate onCapabilitiesChanged firing repeatedly before the callback is unregistered.
    callback.onCapabilitiesChanged(network, capabilities)
    callback.onCapabilitiesChanged(network, capabilities)
    callback.onCapabilitiesChanged(network, capabilities)

    assertThat(results).hasSize(1)
    assertThat(results.first().getOrNull()).isEqualTo(expectedResult)
    verify(exactly = 1) {
      connectivityManager.unregisterNetworkCallback(callback)
    }
  }

  @Test
  fun `Resumes with exception and unregisters when the request fails`() {
    val exception = TwilioVerifySnaException.NetworkRequestException(Exception("boom"))
    every { networkRequestProvider.performRequest(url, network) } throws exception

    val callback = provider.provide(url, continuation, connectivityManager)

    callback.onCapabilitiesChanged(network, validatedCapabilities())

    assertThat(results).hasSize(1)
    assertThat(results.first().exceptionOrNull())
      .isInstanceOf(TwilioVerifySnaException.NetworkRequestException::class.java)
    verify(exactly = 1) {
      connectivityManager.unregisterNetworkCallback(callback)
    }
  }
}
