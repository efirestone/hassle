package khome

import khome.core.koin.KhomeKoinContext

fun withApplication(block: KhomeApplication.() -> Unit) {
    val application = khomeApplication()
    block(application)
    KhomeKoinContext.application?.close()
}
