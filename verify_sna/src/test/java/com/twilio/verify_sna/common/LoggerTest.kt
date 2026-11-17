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

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import kotlin.concurrent.thread

class LoggerTest {

  @Before
  fun setUp() {
    Logger.startNewSession()
  }

  @Test
  fun `log adds an entry with timestamp, caller location and message`() {
    Logger.log("hello")

    assertThat(Logger.getText()).matches(
      """\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3} \[LoggerTest\.kt:\d+]: hello"""
    )
  }

  @Test
  fun `getText joins entries in order with new lines`() {
    Logger.log("first")
    Logger.log("second")

    val lines = Logger.getText().lines()
    assertThat(lines).hasSize(2)
    assertThat(lines[0]).endsWith(": first")
    assertThat(lines[1]).endsWith(": second")
  }

  @Test
  fun `startNewSession clears entries`() {
    Logger.log("hello")

    Logger.startNewSession()

    assertThat(Logger.getText()).isEmpty()
  }

  @Test
  fun `only the most recent MAX_ENTRIES entries are kept`() {
    repeat(Logger.MAX_ENTRIES + 10) { Logger.log("entry $it") }

    val lines = Logger.getText().lines()
    assertThat(lines).hasSize(Logger.MAX_ENTRIES)
    assertThat(lines.first()).endsWith(": entry 10")
    assertThat(lines.last()).endsWith(": entry ${Logger.MAX_ENTRIES + 9}")
  }

  @Test
  fun `concurrent logs are not lost`() {
    val threads = 4
    val perThread = Logger.MAX_ENTRIES / threads

    (0 until threads)
      .map { t -> thread { repeat(perThread) { Logger.log("t$t-$it") } } }
      .forEach { it.join() }

    assertThat(Logger.getText().lines()).hasSize(threads * perThread)
  }
}
