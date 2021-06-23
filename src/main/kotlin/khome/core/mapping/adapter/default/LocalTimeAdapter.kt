package khome.core.mapping.adapter.default

import io.fluidsonic.time.LocalTime
import khome.core.mapping.KhomeTypeAdapter

class LocalTimeAdapter : KhomeTypeAdapter<LocalTime> {
    override fun <P> from(value: P): LocalTime {
        return LocalTime.parse(value as String)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <P> to(value: LocalTime): P {
        return value.toString() as P
    }
}
