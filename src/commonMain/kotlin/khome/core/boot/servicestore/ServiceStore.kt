package khome.core.boot.servicestore

import java.util.concurrent.ConcurrentHashMap

interface ServiceStoreInterface {
    val list: MutableMap<String, List<String>>

    operator fun set(domain: String, services: List<String>)
    operator fun get(domain: String) = list[domain]
    operator fun contains(domain: String): Boolean
    fun clear()
}

internal class ServiceStore :
    Iterable<MutableMap.MutableEntry<String, List<String>>>, ServiceStoreInterface {
    override val list = createMapForServiceStore<String, List<String>>(10)
    override operator fun iterator() = list.iterator()
    override operator fun set(domain: String, services: List<String>) {
        list[domain] = services
    }

    override operator fun contains(domain: String) = list.containsKey(domain)
    override fun clear() = list.clear()
}

/**
 * Creates a ConcurrentHashMap. This method should be made multiplatform compatible.
 */
internal fun <K, V> createMapForServiceStore(initialCapacity: Int): MutableMap<K, V> =
    ConcurrentHashMap(initialCapacity)

