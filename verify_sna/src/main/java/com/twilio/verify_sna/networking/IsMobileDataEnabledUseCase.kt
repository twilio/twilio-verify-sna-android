package com.twilio.verify_sna.networking

import android.net.ConnectivityManager
import java.lang.reflect.Method

interface IsMobileDataEnabledUseCase {
  operator fun invoke(connectivityManager: ConnectivityManager): Boolean
}

class IsMobileDataEnabledUseCaseImpl : IsMobileDataEnabledUseCase {

  /**
   * Android Framework doesn't count with a pre-build way of getting mobile network status,
   * when Wi-Fi is active. Reflection fits well.
   * Taken from https://stackoverflow.com/a/8243305
   */
  override fun invoke(connectivityManager: ConnectivityManager): Boolean {
    return try {
      val c = Class.forName(connectivityManager.javaClass.name)
      val m: Method = c.getDeclaredMethod("getMobileDataEnabled")
      m.isAccessible = true
      m.invoke(connectivityManager) as Boolean
    } catch (exception: Exception) {
      exception.printStackTrace()
      false
    }
  }
}
