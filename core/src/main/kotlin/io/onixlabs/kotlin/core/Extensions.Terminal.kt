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

/**
 * Sets the foreground color of the terminal to the specified [TerminalColor].
 *
 * @param color The foreground color to set.
 */
public fun setForegroundColor(color: TerminalColor) {
    kotlin.io.print(color.foregroundColorModifier)
}

/**
 * Sets the background color of the terminal to the specified [TerminalColor].
 *
 * @param color The background color to set.
 */
public fun setBackgroundColor(color: TerminalColor) {
    kotlin.io.print(color.backgroundColorModifier)
}

/**
 * Resets the foreground and background colors of the terminal to their defaults.
 */
public fun resetTerminalColors() {
    kotlin.io.print(TERMINAL_RESET)
}

/**
 * Prints the specified value in the specified colors, then resets the terminal colors.
 *
 * @param value The value to print.
 * @param foreground The foreground color.
 * @param background The background color, or null to keep the current background color.
 */
public fun print(value: Any?, foreground: TerminalColor, background: TerminalColor? = null) {
    kotlin.io.print(colored(value, foreground, background))
}

/**
 * Prints the specified value in the specified colors, then resets the terminal colors and terminates the line.
 *
 * @param value The value to print.
 * @param foreground The foreground color.
 * @param background The background color, or null to keep the current background color.
 */
public fun println(value: Any?, foreground: TerminalColor, background: TerminalColor? = null) {
    kotlin.io.println(colored(value, foreground, background))
}

/**
 * Prints the value returned by the specified function.
 *
 * @param function The function that returns the value to print.
 */
public fun print(function: () -> Any?) {
    kotlin.io.print(function())
}

/**
 * Prints the value returned by the specified function, then terminates the line.
 *
 * @param function The function that returns the value to print.
 */
public fun println(function: () -> Any?) {
    kotlin.io.println(function())
}

/**
 * The escape sequence that resets the terminal colors.
 */
private const val TERMINAL_RESET = "\u001B[0m"

private fun colored(value: Any?, foreground: TerminalColor, background: TerminalColor?): String {
    return foreground.foregroundColorModifier + (background?.backgroundColorModifier ?: "") + value + TERMINAL_RESET
}
