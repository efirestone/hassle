package khome.core.mapping.adapter.default

import khome.core.mapping.KhomeTypeAdapter
import kotlinx.datetime.LocalDate

internal class LocalDateAdapter : KhomeTypeAdapter<LocalDate> {
    override fun <P> from(value: P): LocalDate {
        return LocalDate.parse(value as String)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <P> to(value: LocalDate): P {
        return value.toString() as P
    }
}
