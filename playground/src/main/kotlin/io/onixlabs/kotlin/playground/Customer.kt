package io.onixlabs.kotlin.playground

import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.ZoneOffset

data class Customer(
    val name: String,
    val country: String,
    val birthday: Instant,
    val subscribed: Instant,
    val loyaltyPoints: Int
) {
    val age: Int get() = Period.between(LocalDate.ofInstant(birthday, ZoneOffset.UTC), LocalDate.now()).years
    val yearsSubscribed: Int get() = Period.between(LocalDate.ofInstant(subscribed, ZoneOffset.UTC), LocalDate.now()).years
}
