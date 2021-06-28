package khome.core.koin

import org.koin.core.Koin
import org.koin.core.component.KoinComponent

internal interface KhomeComponent : KoinComponent {
    override fun getKoin(): Koin =
        checkNotNull(KhomeKoinContext.application) { "No KoinApplication found" }.koin
}

internal object KoinContainer : KhomeComponent
