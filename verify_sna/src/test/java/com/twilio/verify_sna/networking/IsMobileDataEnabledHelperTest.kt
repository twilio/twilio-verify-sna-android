package com.twilio.verify_sna.networking

import android.content.Context
import android.net.ConnectivityManager
import android.telephony.TelephonyManager
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class IsMobileDataEnabledHelperTest {

  private val context: Context = mockk(relaxed = true)
  private val connectivityManager: ConnectivityManager = mockk(relaxed = true)
  private val telephonyManager: TelephonyManager = mockk(relaxed = true)

  private val helper = IsMobileDataEnabledHelperImpl(context)

  @Config(sdk = [28])
  @Test
  fun `Returns true when TelephonyManager reports data enabled on Android O and above`() {
    every { context.getSystemService(Context.TELEPHONY_SERVICE) } returns telephonyManager
    every { telephonyManager.isDataEnabled } returns true

    assertThat(helper(connectivityManager)).isTrue()
  }

  @Config(sdk = [28])
  @Test
  fun `Returns false when TelephonyManager reports data disabled on Android O and above`() {
    every { context.getSystemService(Context.TELEPHONY_SERVICE) } returns telephonyManager
    every { telephonyManager.isDataEnabled } returns false

    assertThat(helper(connectivityManager)).isFalse()
  }

  @Config(sdk = [28])
  @Test
  fun `Returns false when TelephonyManager is unavailable`() {
    every { context.getSystemService(Context.TELEPHONY_SERVICE) } returns null

    assertThat(helper(connectivityManager)).isFalse()
  }

  @Config(sdk = [28])
  @Test
  fun `Returns false when reading data enabled state throws a SecurityException`() {
    every { context.getSystemService(Context.TELEPHONY_SERVICE) } returns telephonyManager
    every { telephonyManager.isDataEnabled } throws SecurityException("READ_PHONE_STATE denied")

    assertThat(helper(connectivityManager)).isFalse()
  }
}
