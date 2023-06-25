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
