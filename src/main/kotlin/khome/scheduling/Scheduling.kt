package khome.scheduling

import java.util.*
import java.time.ZoneId
import khome.core.logger
import java.time.LocalDate
import kotlin.concurrent.*
import java.time.LocalDateTime
import khome.listening.getState
import java.lang.RuntimeException
import java.util.concurrent.TimeUnit
import java.time.format.DateTimeFormatter
import khome.core.LifeCycleHandlerInterface
import khome.core.entities.Sun
import khome.listening.getStateValue
import java.time.temporal.ChronoUnit

inline fun runDailyAt(timeOfDay: String, crossinline action: TimerTask.() -> Unit): LifeCycleHandler {
    val startDate = createLocalDateTimeFromTimeOfDayAsString(timeOfDay)
    val nextStartDate = startDate.plusDays(1)
    val nextExecution =
        if (nowIsAfter(timeOfDay)) nextStartDate else startDate
    val periodInMilliseconds = TimeUnit.DAYS.toMillis(1)
    return runEveryAt(periodInMilliseconds, nextExecution, action)
}

inline fun runHourlyAt(timeOfDay: String, crossinline action: TimerTask.() -> Unit): LifeCycleHandler {
    val startDate = createLocalDateTimeFromTimeOfDayAsString(timeOfDay)
    val now = LocalDateTime.now()
    val hoursSinceStartDate = startDate.until(now, ChronoUnit.YEARS) + 1
    val nextStartDate = startDate.plusHours(hoursSinceStartDate)
    val nextExecution =
        if (nowIsAfter(timeOfDay)) nextStartDate else startDate
    val periodInMilliseconds = TimeUnit.HOURS.toMillis(1)

    return runEveryAt(periodInMilliseconds, nextExecution, action)
}

inline fun runMinutelyAt(timeOfDay: String, crossinline action: TimerTask.() -> Unit): LifeCycleHandler {
    val startDate = createLocalDateTimeFromTimeOfDayAsString(timeOfDay)
    val now = LocalDateTime.now()
    val minutesSinceStartDate = startDate.until(now, ChronoUnit.MINUTES) + 1
    val nextStartDate = startDate.plusMinutes(minutesSinceStartDate)
    val nextExecution =
        if (nowIsAfter(timeOfDay)) nextStartDate else startDate
    val periodInMilliseconds = TimeUnit.MINUTES.toMillis(1)

    return runEveryAt(periodInMilliseconds, nextExecution, action)
}

inline fun runEveryAt(period: Long, localDateTime: LocalDateTime, crossinline action: TimerTask.() -> Unit): LifeCycleHandler {
    val timer = fixedRateTimer("scheduler", false, localDateTime.toDate(), period, action)

    return LifeCycleHandler(timer)
}

inline fun runOnceAt(timeOfDay: String, crossinline action: TimerTask.() -> Unit): LifeCycleHandler {
    val startDate = createLocalDateTimeFromTimeOfDayAsString(timeOfDay)
    return runOnceAt(startDate, action)
}

inline fun runOnceAt(dateTime: LocalDateTime, crossinline action: TimerTask.() -> Unit): LifeCycleHandler {
    val timer = Timer("scheduler", false)
    timer.schedule(dateTime.toDate(), action)

    return LifeCycleHandler(timer)
}

inline fun runOnceInSeconds(seconds: Int, crossinline callback: TimerTask.() -> Unit): LifeCycleHandler {
    val timer = Timer("scheduler", false)
    timer.schedule(seconds * 1000L, callback)

    return LifeCycleHandler(timer)
}

inline fun runOnceInMinutes(minutes: Int, crossinline action: TimerTask.() -> Unit) =
    runOnceInSeconds(minutes * 60, action)

inline fun runOnceInHours(hours: Int, crossinline action: TimerTask.() -> Unit) =
    runOnceInSeconds((hours * 60) * 60, action)

fun createLocalDateTimeFromTimeOfDayAsString(timeOfDay: String): LocalDateTime {
    val (hour, minute) = timeOfDay.split(":")
    val startLocalDate = LocalDate.now().atTime(hour.toInt(), minute.toInt())
    return startLocalDate
}

fun runEverySunRise(action: TimerTask.() -> Unit) {
    val now = LocalDateTime.now()
    val dailyPeriodInMillis = TimeUnit.DAYS.toMillis(1)

    runEveryAt(dailyPeriodInMillis, now) {
        val nextSunrise = nextSunrise()
        runOnceAt(nextSunrise, action)
    }
}

fun runEverySunSet(action: TimerTask.() -> Unit) {
    val now = LocalDateTime.now()
    val dailyPeriodInMillis = TimeUnit.DAYS.toMillis(1)

    runEveryAt(dailyPeriodInMillis, now) {
        val nextSunrise = nextSunset()
        runOnceAt(nextSunrise, action)
    }
}

fun nextSunrise() = getNextSunPosition("next_rising")

fun nextSunset()= getNextSunPosition("next_setting")

fun isSunUp() = getStateValue<String>(Sun) == "above_horizon"

fun isSunDown() = getStateValue<String>(Sun) == "below_horizon"

fun getNextSunPosition(nextPosition: String): LocalDateTime {
    val sunset = getState("sun.sun").getAttribute<String>(nextPosition)
        ?: throw RuntimeException("Could not fetch $nextPosition time from state-attribute")

    return LocalDateTime.parse(sunset, DateTimeFormatter.ISO_DATE_TIME)
}

class LifeCycleHandler(timer: Timer) : LifeCycleHandlerInterface {
    override val lazyCancellation: Unit by lazy {
        timer.cancel()
        logger.info { "schedule canceled." }
    }

    override fun cancel() = lazyCancellation
    override fun cancelInSeconds(seconds: Int) = runOnceInSeconds(seconds) { lazyCancellation }
    override fun cancelInMinutes(minutes: Int) = runOnceInMinutes(minutes) { lazyCancellation }
}

fun LocalDateTime.toDate(): Date = Date
    .from(atZone(ZoneId.systemDefault()).toInstant())

fun nowIsAfter(timeOfDay: String): Boolean {
    val timeOfDayDate = createLocalDateTimeFromTimeOfDayAsString(timeOfDay)
    return nowIsAfter(timeOfDayDate)
}

fun nowIsAfter(localDateTime: LocalDateTime): Boolean {
    val now = LocalDateTime.now().toDate()
    return now.after(localDateTime.toDate())
}