package com.twilio.verify_sna.networking

import android.net.Network
import com.google.common.truth.Truth.assertThat
import com.twilio.verify_sna.common.TwilioVerifySnaException
import io.mockk.every
import io.mockk.mockk
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import javax.net.SocketFactory

@RunWith(RobolectricTestRunner::class)
class NetworkRequestProviderTest {

  private lateinit var networkRequestProvider: NetworkRequestProvider
  private lateinit var server: MockWebServer
  private val mockNetwork: Network = mockk(relaxed = true)

  @Before
  fun setup() {
    networkRequestProvider = ConcreteNetworkRequestProvider()
    server = MockWebServer()
  }

  @Test
  fun `Perform request returns Success result`() {
    val expectedStatus = 200
    val expectedMessage = "Success"
    server.enqueue(
      MockResponse(code = expectedStatus, body = expectedMessage)
    )
    server.start()
    val baseUrl  = server.url("/test")
    every {
      mockNetwork.socketFactory
    } returns SocketFactory.getDefault()

    val result = networkRequestProvider.performRequest(baseUrl.toString(), mockNetwork)

    assertThat(result.status).isEqualTo(expectedStatus)
    assertThat(result.message).isEqualTo(expectedMessage)
  }

  @Test(expected = TwilioVerifySnaException.NetworkRequestException::class)
  fun `Perform request throws a Network Request Exception`() {
    val expectedStatus = 404
    server.enqueue(
      MockResponse(code = expectedStatus)
    )
    server.start()
    val baseUrl  = server.url("/test")
    every {
      mockNetwork.socketFactory
    } returns SocketFactory.getDefault()

    networkRequestProvider.performRequest(baseUrl.toString(), mockNetwork)
    fail("Expected a TwilioVerifySnaException.NetworkRequestException to be thrown")
  }
}