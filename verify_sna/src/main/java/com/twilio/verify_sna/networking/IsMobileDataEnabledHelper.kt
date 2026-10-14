package com.twilio.verify_sna.networking

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.telephony.TelephonyManager

interface IsMobileDataEnabledHelper {
  /**
   * Whether the mobile data user setting is on, regardless of Wi-Fi being the active network.
   */
  operator fun invoke(): Boolean
}

class IsMobileDataEnabledHelperImpl(
  private val context: Context
) : IsMobileDataEnabledHelper {

  override fun invoke(): Boolean {
    if (VERSION.SDK_INT >= VERSION_CODES.O) {
      val telephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
      return try {
        telephonyManager?.isDataEnabled ?: false
      } catch (securityException: SecurityException) {
        false
      }
    }
    return isMobileDataEnabledByReflection()
  }

  /**
   * There is no public API for the mobile data setting below Android O (API 26), where
   * TelephonyManager.isDataEnabled() was added, so pre-O devices read the hidden
   * ConnectivityManager.getMobileDataEnabled().
   * Taken from https://stackoverflow.com/a/8243305
   */
  private fun isMobileDataEnabledByReflection(): Boolean {
    return try {
      val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
          ?: return false
      val method = connectivityManager.javaClass.getDeclaredMethod("getMobileDataEnabled")
      method.isAccessible = true
      method.invoke(connectivityManager) as Boolean
    } catch (exception: Exception) {
      false
    }
  }
}
