package khome.core.mapping.adapter.default

import khome.core.mapping.KhomeTypeAdapter
import kotlinx.datetime.LocalDateTime

internal class LocalDateTimeAdapter : KhomeTypeAdapter<LocalDateTime> {
    override fun <P> from(value: P): LocalDateTime {
        return LocalDateTime.parse((value as String).replace(' ', 'T'))
    }

    @Suppress("UNCHECKED_CAST")
    override fun <P> to(value: LocalDateTime): P {
        return LocalDateTime(
            value.year,
            value.monthNumber,
            value.dayOfMonth,
            value.hour,
            value.minute,
            value.second
        ).toString() as P
    }
}
