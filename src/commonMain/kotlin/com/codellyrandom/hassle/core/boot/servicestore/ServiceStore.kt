package com.codellyrandom.hassle.core.boot.servicestore

interface ServiceStore {
    val list: MutableMap<String, List<String>>

    operator fun set(domain: String, services: List<String>)
    operator fun get(domain: String) = list[domain]
    operator fun contains(domain: String): Boolean
    fun clear()
}

internal class ServiceStoreImpl :
    Iterable<MutableMap.MutableEntry<String, List<String>>>, ServiceStore {
    override val list = createMapForServiceStore<String, List<String>>(10)
    override operator fun iterator() = list.iterator()
    override operator fun set(domain: String, services: List<String>) {
        list[domain] = services
    }

    override operator fun contains(domain: String) = list.containsKey(domain)
    override fun clear() = list.clear()
}

/**
 * Creates a ConcurrentHashMap on JVM and regular HashMap on other platforms.
 * To make actual use of cache in Kotlin/Native, mark a top-level object with this map
 * as a @[ThreadLocal].
 *
 * Copied from https://github.com/Kotlin/kotlinx.serialization/blob/fc9343f06c5184d51df9ad1006d26c60c3230c2a/formats/json/commonMain/src/kotlinx/serialization/json/internal/SchemaCache.kt
 */
internal expect fun <K, V> createMapForServiceStore(initialCapacity: Int): MutableMap<K, V>
