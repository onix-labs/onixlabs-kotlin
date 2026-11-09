/*
 * Copyright 2020-2026 ONIXLabs
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
package io.onixlabs.kotlin.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class TerminalExtensionTests {

    private fun captureOutput(action: () -> Unit): String {
        val original = System.out
        val buffer = ByteArrayOutputStream()
        System.setOut(PrintStream(buffer, true))
        try {
            action()
        } finally {
            System.setOut(original)
        }
        return buffer.toString()
    }

    @Test
    fun `print should write the value in the specified colors, then reset the colors`() {

        // Act
        val output = captureOutput { print("hello", TerminalColor.DARK_RED, TerminalColor.WHITE) }

        // Assert
        assertEquals("\u001B[31m\u001B[107mhello\u001B[0m", output)
    }

    @Test
    fun `println should write the value in the specified color, reset the colors, and terminate the line`() {

        // Act
        val output = captureOutput { println("hello", TerminalColor.DARK_GREEN) }

        // Assert
        assertEquals("\u001B[32mhello\u001B[0m" + System.lineSeparator(), output)
    }

    @Test
    fun `print and println should write the value returned by a function`() {

        // Act
        val output = captureOutput {
            print { 1 + 1 }
            println { "!" }
        }

        // Assert
        assertEquals("2!" + System.lineSeparator(), output)
    }
}
