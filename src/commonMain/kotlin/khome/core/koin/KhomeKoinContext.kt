package khome.core.koin

import co.touchlab.kermit.Kermit
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.koinApplication
//import org.koin.environmentProperties
//import org.koin.fileProperties

/**
 * The Khome encapsulated public Koin context
 * @link https://insert-koin.io/docs/2.0/documentation/reference/index.html#_koin_context_isolation
 */

internal object KhomeKoinContext {

    var application: KoinApplication? = null
    private val logger = Kermit()

    fun startKoinApplication() {
        application = koinApplication {
//            environmentProperties()
            runCatching {
//                fileProperties("/khome.properties")
            }.onFailure {
                logger.w { "No khome.properties file found" }
            }
        }
    }

    fun addModule(vararg modules: Module) {
        val koinApplication = checkNotNull(application) { "Khomes koin application is not ready yet " }
        koinApplication.modules(*modules)
    }
}
