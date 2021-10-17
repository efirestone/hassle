package hassemble.core.boot.servicestore

/**
 * Creates a ConcurrentHashMap on JVM and regular HashMap on other platforms.
 * To make actual use of cache in Kotlin/Native, mark a top-level object with this map
 * as a @[ThreadLocal].
 */
internal actual fun <K, V> createMapForServiceStore(initialCapacity: Int): MutableMap<K, V> = HashMap(initialCapacity)
