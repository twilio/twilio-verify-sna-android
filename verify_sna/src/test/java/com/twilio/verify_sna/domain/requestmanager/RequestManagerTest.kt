package com.twilio.verify_sna.domain.requestmanager

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import com.google.common.truth.Truth.assertThat
import com.twilio.verify_sna.common.TwilioVerifySnaException
import com.twilio.verify_sna.networking.IsMobileDataEnabledHelper
import com.twilio.verify_sna.networking.NetworkRequestResult
import com.twilio.verify_sna.networking.RequestNetworkWithRetryHelper
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
  private val isMobileDataEnabledHelper: IsMobileDataEnabledHelper = mockk(relaxed = true)
  private val verifySnaNetworkCallbackProvider: VerifySnaNetworkCallbackProvider =
    mockk(relaxed = true)
  private val requestNetworkWithRetryHelper: RequestNetworkWithRetryHelper =
    mockk(relaxed = true)
  private val requestManager: RequestManager = ConcreteRequestManager(
    context,
    isMobileDataEnabledHelper,
    verifySnaNetworkCallbackProvider,
    requestNetworkWithRetryHelper
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
      isMobileDataEnabledHelper()
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
      requestNetworkWithRetryHelper(
        connectivityManager,
        any(),
        networkCallback,
        any()
      )
    } answers {
      continuationSlot.captured.resume(expectedResult)
    }

    val result = requestManager.processUrl(testUrl)

    assertThat(result).isEqualTo(expectedResult)
    verify {
      requestNetworkWithRetryHelper(
        connectivityManager,
        any(),
        any(),
        any()
      )
    }
  }

  @Test
  fun `Process an Url surfaces the original cause when requesting the network keeps failing`() =
    runTest {
      val testUrl = "test.url.com"
      val connectivityManager: ConnectivityManager = mockk(relaxed = true)
      val requestNetworkException = SecurityException("CHANGE_NETWORK_STATE denied")

      every {
        context.getSystemService(Context.CONNECTIVITY_SERVICE)
      } returns connectivityManager

      every { isMobileDataEnabledHelper() } returns true

      val onFailureSlot = slot<(Exception) -> Unit>()
      every {
        requestNetworkWithRetryHelper(
          connectivityManager,
          any(),
          any(),
          capture(onFailureSlot)
        )
      } answers {
        onFailureSlot.captured(requestNetworkException)
      }

      var thrown: Throwable? = null
      try {
        requestManager.processUrl(testUrl)
      } catch (e: Throwable) {
        thrown = e
      }

      assertThat(thrown)
        .isInstanceOf(TwilioVerifySnaException.NetworkRequestException::class.java)
      assertThat(thrown).hasCauseThat().isSameInstanceAs(requestNetworkException)
    }

  @Test
  fun `Process an Url ignores a retry failure reported after the request already timed out`() =
    runTest {
      val testUrl = "test.url.com"
      val connectivityManager: ConnectivityManager = mockk(relaxed = true)

      every {
        context.getSystemService(Context.CONNECTIVITY_SERVICE)
      } returns connectivityManager

      every { isMobileDataEnabledHelper() } returns true

      // Capture onFailure without resuming, so the request times out first.
      val onFailureSlot = slot<(Exception) -> Unit>()
      every {
        requestNetworkWithRetryHelper(
          connectivityManager,
          any(),
          any(),
          capture(onFailureSlot)
        )
      } answers { }

      var thrown: Throwable? = null
      try {
        requestManager.processUrl(testUrl)
      } catch (e: Throwable) {
        thrown = e
      }
      assertThat(thrown)
        .isInstanceOf(TwilioVerifySnaException.NetworkRequestTimeoutException::class.java)

      // The delayed retry reports its failure after the continuation was already cancelled.
      // Resuming a cancelled continuation must be a no-op rather than a crash.
      onFailureSlot.captured(SecurityException("CHANGE_NETWORK_STATE denied"))
    }

  @Test
  fun `Process an Url returns Cellular Network Not Available exception`() = runTest {
    val testUrl = "test.url.com"
    val connectivityManager: ConnectivityManager = mockk(relaxed = true)

    every {
      context.getSystemService(Context.CONNECTIVITY_SERVICE)
    } returns connectivityManager

    every {
      isMobileDataEnabledHelper()
    } returns false

    try {
      requestManager.processUrl(testUrl)
    } catch (e: Exception) {
      assertThat(e).isInstanceOf(TwilioVerifySnaException.CellularNetworkNotAvailable::class.java)
    }
  }

  @Test
  fun `Process an Url resumes with Cellular Network Not Available when ConnectivityManager is null`() =
    runTest {
      val testUrl = "test.url.com"

      every {
        context.getSystemService(Context.CONNECTIVITY_SERVICE)
      } returns null

      var thrown: Throwable? = null
      try {
        requestManager.processUrl(testUrl)
      } catch (e: Throwable) {
        thrown = e
      }
      assertThat(thrown)
        .isInstanceOf(TwilioVerifySnaException.CellularNetworkNotAvailable::class.java)
    }

  @Test
  fun `Process an Url resumes with Network Request Exception when the callback never resumes`() =
    runTest {
      val testUrl = "test.url.com"
      val connectivityManager: ConnectivityManager = mockk(relaxed = true)

      every {
        context.getSystemService(Context.CONNECTIVITY_SERVICE)
      } returns connectivityManager

      every {
        isMobileDataEnabledHelper()
      } returns true

      var thrown: Throwable? = null
      try {
        requestManager.processUrl(testUrl)
      } catch (e: Throwable) {
        thrown = e
      }
      assertThat(thrown)
        .isInstanceOf(TwilioVerifySnaException.NetworkRequestTimeoutException::class.java)
      verify {
        connectivityManager.unregisterNetworkCallback(any<NetworkCallback>())
      }
    }
}
