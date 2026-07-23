package com.twilio.verify_sna.networking

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.telephony.TelephonyManager
import java.lang.reflect.Method

interface IsMobileDataEnabledHelper {
  operator fun invoke(connectivityManager: ConnectivityManager): Boolean
}

class IsMobileDataEnabledHelperImpl(
  private val context: Context
) : IsMobileDataEnabledHelper {

  override fun invoke(connectivityManager: ConnectivityManager): Boolean {
    // TelephonyManager.isDataEnabled() is a public API since Android O (API 26). Reflecting into
    // the hidden ConnectivityManager.getMobileDataEnabled() is greylisted/blocked on Android P+
    // and would throw, wrongly reporting mobile data as disabled on modern devices.
    if (VERSION.SDK_INT >= VERSION_CODES.O) {
      val telephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
      return try {
        telephonyManager?.isDataEnabled ?: false
      } catch (securityException: SecurityException) {
        securityException.printStackTrace()
        false
      }
    }
    return isMobileDataEnabledByReflection(connectivityManager)
  }

  /**
   * Android Framework doesn't count with a pre-build way of getting mobile network status,
   * when Wi-Fi is active. Reflection fits well.
   * Taken from https://stackoverflow.com/a/8243305
   */
  private fun isMobileDataEnabledByReflection(connectivityManager: ConnectivityManager): Boolean {
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
