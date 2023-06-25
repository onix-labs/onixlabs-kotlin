package io.onixlabs.kotlin.graph

class GraphBuilder {

    @GraphDslMarker
    fun <T> configure(type: Class<T>, action: GraphConfigurationBuilder<T>.() -> Unit) {

    }

    @GraphDslMarker
    inline fun <reified T> configure(noinline action: GraphConfigurationBuilder<T>.() -> Unit) {
        configure(T::class.java, action)
    }
}
