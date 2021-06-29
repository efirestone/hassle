package khome.core.boot

import khome.KhomeSession
import khome.communicating.HassApiClient
import khome.communicating.HassApiClientImpl
import khome.core.koin.KhomeKoinContext
import org.koin.dsl.module

internal class HassApiInitializerImpl(
    private val khomeSession: KhomeSession
) : HassApiInitializer {

    private val systemBeansModule =
        module {
            single<HassApiClient> { HassApiClientImpl(khomeSession, get(), get()) }
        }

    override suspend fun initialize() {
        KhomeKoinContext.addModule(systemBeansModule)
    }
}

interface HassApiInitializer {
    suspend fun initialize()
}
