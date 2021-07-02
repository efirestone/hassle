package khome.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlin.coroutines.CoroutineContext

actual object MainDispatcherFactory {
    actual fun create(): CoroutineDispatcher = MainDispatcher()
}

private class MainDispatcher: CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) = Dispatchers.Main.dispatch(context, block)
}
