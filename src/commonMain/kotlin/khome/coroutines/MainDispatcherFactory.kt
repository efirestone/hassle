package khome.coroutines

import kotlinx.coroutines.CoroutineDispatcher

expect object MainDispatcherFactory {
    fun create(): CoroutineDispatcher
}