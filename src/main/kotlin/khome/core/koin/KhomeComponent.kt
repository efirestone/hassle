package khome.core.koin

import io.ktor.util.KtorExperimentalAPI
import org.koin.core.Koin
import org.koin.core.component.KoinComponent

@KtorExperimentalAPI
internal interface KhomeComponent : KoinComponent {

    override fun getKoin(): Koin =
        checkNotNull(KhomeKoinContext.application) { "No KoinApplication found" }.koin
}

@OptIn(KtorExperimentalAPI::class)
internal object KoinContainer : KhomeComponent
