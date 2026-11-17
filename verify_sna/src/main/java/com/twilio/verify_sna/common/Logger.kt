/*
 * Copyright (c) 2026 Twilio Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.verify_sna.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * In-memory log of what the SDK did while processing an SNA URL.
 *
 * Each entry is `"<timestamp> [<file>:<line>]: <message>"`. Call [startNewSession] before a
 * verification to clear previous entries, and [getText] afterwards to read them, for example to
 * show them in a debug screen or attach them to a support ticket.
 *
 * Only the most recent [MAX_ENTRIES] entries are kept. All functions are thread-safe.
 */
object Logger {

  /**
   * Maximum number of entries kept; older entries are dropped first.
   */
  const val MAX_ENTRIES = 500

  private val logs = ArrayDeque<String>()
  private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

  /**
   * Removes all entries.
   */
  @JvmStatic
  fun startNewSession() {
    synchronized(logs) { logs.clear() }
  }

  /**
   * Appends [message], tagged with the current time and the caller's source file and line.
   */
  @JvmStatic
  fun log(message: String) {
    val caller = Throwable().stackTrace.getOrNull(1)
    val location = caller?.let { "${it.fileName}:${it.lineNumber}" } ?: "unknown"
    synchronized(logs) {
      // SimpleDateFormat isn't thread-safe, so it's only used under the lock.
      logs.addLast("${dateFormat.format(Date())} [$location]: $message")
      while (logs.size > MAX_ENTRIES) logs.removeFirst()
    }
  }

  /**
   * Returns all entries, oldest first, separated by new lines.
   */
  @JvmStatic
  fun getText(): String = synchronized(logs) { logs.joinToString("\n") }
}
