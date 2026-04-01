package com.twilio.verify_sna.domain.requestmanager

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import com.google.common.truth.Truth.assertThat
import com.twilio.verify_sna.common.TwilioVerifySnaException
import com.twilio.verify_sna.networking.IsMobileDataEnabledUseCase
import com.twilio.verify_sna.networking.NetworkRequestResult
import com.twilio.verify_sna.networking.RequestNetworkWithRetryUseCase
import com.twilio.verify_sna.networking.VerifySnaNetworkCallbackProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

@RunWith(RobolectricTestRunner::class)
class RequestManagerTest {

  private val context: Context = mockk(relaxed = true)
  private val isMobileDataEnabledUseCase: IsMobileDataEnabledUseCase = mockk(relaxed = true)
  private val verifySnaNetworkCallbackProvider: VerifySnaNetworkCallbackProvider =
    mockk(relaxed = true)
  private val requestNetworkWithRetryUseCase: RequestNetworkWithRetryUseCase =
    mockk(relaxed = true)
  private val requestManager: RequestManager = ConcreteRequestManager(
    context,
    isMobileDataEnabledUseCase,
    verifySnaNetworkCallbackProvider,
    requestNetworkWithRetryUseCase
  )

  @Test
  fun `Process an Url returns Success result`() = runTest {
    val testUrl = "test.url.com"
    val connectivityManager: ConnectivityManager = mockk(relaxed = true)
    val expectedResult = NetworkRequestResult(
      status = 200,
      message = "Success"
    )
    val networkCallback: NetworkCallback = mockk()

    every {
      context.getSystemService(Context.CONNECTIVITY_SERVICE)
    } returns connectivityManager

    every {
      isMobileDataEnabledUseCase(connectivityManager)
    } returns true

    val continuationSlot = slot<Continuation<NetworkRequestResult>>()
    every {
      verifySnaNetworkCallbackProvider.provide(
        testUrl,
        capture(continuationSlot),
        connectivityManager
      )
    } returns networkCallback

    every {
      requestNetworkWithRetryUseCase(
        connectivityManager,
        any(),
        networkCallback
      )
    } answers {
      continuationSlot.captured.resume(expectedResult)
    }

    val result = requestManager.processUrl(testUrl)

    assertThat(result).isEqualTo(expectedResult)
    verify {
      requestNetworkWithRetryUseCase(
        connectivityManager,
        any(),
        any()
      )
    }
  }

  @Test
  fun `Process an Url returns Cellular Network Not Available exception`() = runTest {
    val testUrl = "test.url.com"
    val connectivityManager: ConnectivityManager = mockk(relaxed = true)

    every {
      context.getSystemService(Context.CONNECTIVITY_SERVICE)
    } returns connectivityManager

    every {
      isMobileDataEnabledUseCase(connectivityManager)
    } returns false

    try {
      requestManager.processUrl(testUrl)
    } catch (e: Exception) {
      assertThat(e).isInstanceOf(TwilioVerifySnaException.CellularNetworkNotAvailable::class.java)
    }
  }
}
