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
package io.onixlabs.kotlin.playground

import io.onixlabs.kotlin.core.specifications.Specification

class CustomerAgeSpecification(private val minAge: Int, private val maxAge: Int) : Specification<Customer>() {
    override fun isSatisfiedBy(value: Customer): Boolean = value.age in minAge..maxAge
}

class CustomerCountrySpecification(private vararg val countries: String) : Specification<Customer>() {
    override fun isSatisfiedBy(value: Customer): Boolean = value.country in countries
}

class CustomerYearsSubscribedSpecification(private val minYearsSubscribed: Int) : Specification<Customer>() {
    override fun isSatisfiedBy(value: Customer): Boolean = value.yearsSubscribed >= minYearsSubscribed
}

class CustomerLoyaltyPointsSpecification(private val minLoyaltyPoints: Int) : Specification<Customer>() {
    override fun isSatisfiedBy(value: Customer): Boolean = value.loyaltyPoints >= minLoyaltyPoints
}
