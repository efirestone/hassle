package khome.core.mapping.adapter.default

import khome.core.mapping.KhomeTypeAdapter
import kotlinx.datetime.Instant

class InstantTypeAdapter : KhomeTypeAdapter<Instant> {
    override fun <P> from(value: P): Instant {
        return Instant.parse(value as String)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <P> to(value: Instant): P {
        return value.toString() as P
    }
}
