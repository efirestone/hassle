package khome.scheduling

import java.util.*
import java.time.ZoneId
import java.time.LocalDate
import kotlin.concurrent.*
import khome.core.entities.Sun
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import khome.listening.getEntityInstance
import khome.core.LifeCycleHandlerInterface
import khome.Khome.Companion.timeBasedEvents
import khome.Khome.Companion.isSandBoxModeActive
import khome.core.entities.inputDateTime.AbstractTimeEntity
import java.lang.Thread.sleep

inline fun <reified Entity : AbstractTimeEntity> runDailyAt(crossinline action: TimerTask.() -> Unit): LifeCycleHandler {
    val entity = getEntityInstance<Entity>()
    val dayTime = determineDayTimeFromTimeEntity(entity)

    return runDailyAt(dayTime, action)
}

inline fun runDailyAt(timeOfDay: String, crossinline action: TimerTask.() -> Unit): LifeCycleHandler {
    val startDate = createLocalDateTimeFromTimeOfDayAsString(timeOfDay)
    val nextStartDate = startDate.plusDays(1)
    val nextExecution =
        if (nowIsAfter(timeOfDay)) nextStartDate else startDate
    val periodInMilliseconds = TimeUnit.DAYS.toMillis(1)
    return runEveryAt(periodInMilliseconds, nextExecution, action)
}

inline fun <reified Entity : AbstractTimeEntity> runHourlyAt(crossinline action: TimerTask.() -> Unit): LifeCycleHandler {
    val entity = getEntityInstance<Entity>()
    val dayTime = determineDayTimeFromTimeEntity(entity)

    return runHourlyAt(dayTime, action)
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

inline fun <reified Entity : AbstractTimeEntity> runMinutelyAt(crossinline action: TimerTask.() -> Unit): LifeCycleHandler {
    val entity = getEntityInstance<Entity>()
    val dayTime = determineDayTimeFromTimeEntity(entity)

    return runMinutelyAt(dayTime, action)
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

fun determineDayTimeFromTimeEntity(timeEntity: AbstractTimeEntity): String {
    val hour = timeEntity.time.hour
    val minute = timeEntity.time.minute
    return "$hour:$minute"
}

inline fun runEveryAt(
    period: Long,
    localDateTime: LocalDateTime,
    crossinline action: TimerTask.() -> Unit
): LifeCycleHandler {

    val timerTask = timerTask(action)
    timeBasedEvents += { action(timerTask) }

    val timer = Timer("scheduler", false)
    if (!isSandBoxModeActive()) timer.scheduleAtFixedRate(timerTask, localDateTime.toDate(), period)
    return LifeCycleHandler(timer)
}

inline fun runOnceAt(timeOfDay: String, crossinline action: TimerTask.() -> Unit): LifeCycleHandler {
    val startDate = createLocalDateTimeFromTimeOfDayAsString(timeOfDay)
    return runOnceAt(startDate, action)
}

inline fun runOnceAt(dateTime: LocalDateTime, crossinline action: TimerTask.() -> Unit): LifeCycleHandler {
    val timerTask = timerTask(action)
    timeBasedEvents += { action(timerTask) }

    val timer = Timer("scheduler", false)
    if (!isSandBoxModeActive()) timer.schedule(timerTask, dateTime.toDate())

    return LifeCycleHandler(timer)
}

inline fun runEveryTimePeriodFor(
    timePeriod: Long,
    executions: Int,
    crossinline task: () -> Unit
): LifeCycleHandler {
    var counter = 0
    val timer = runEveryAt(timePeriod, LocalDateTime.now()) {
        task()
        counter++
        if (counter == executions) cancel()
    }

    return timer
}

inline fun runOnceInMinutes(minutes: Int, crossinline action: TimerTask.() -> Unit) =
    runOnceInSeconds(minutes * 60, action)

inline fun runOnceInHours(hours: Int, crossinline action: TimerTask.() -> Unit) =
    runOnceInSeconds((hours * 60) * 60, action)

inline fun runOnceInSeconds(seconds: Int, crossinline action: TimerTask.() -> Unit): LifeCycleHandler {
    val timerTask = timerTask(action)
    timeBasedEvents += { action(timerTask) }

    val timer = Timer("scheduler", false)
    if (!isSandBoxModeActive()) timer.schedule(timerTask, seconds * 1000L)
    return LifeCycleHandler(timer)
}

fun createLocalDateTimeFromTimeOfDayAsString(timeOfDay: String): LocalDateTime {
    val (hour, minute) = timeOfDay.split(":")
    return LocalDate.now().atTime(hour.toInt(), minute.toInt())
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
        val nextSunset = nextSunset()
        runOnceAt(nextSunset, action)
    }
}

fun nextSunrise() = getNextSunPosition("next_rising")

fun nextSunset() = getNextSunPosition("next_setting")

private fun getNextSunPosition(nextPosition: String): LocalDateTime {
    val nextSunPositionChange = Sun.getAttributeValue<String>(nextPosition)
    return convertUtcToLocalDateTime(nextSunPositionChange)
}

private fun convertUtcToLocalDateTime(utcDateTime: String): LocalDateTime {
    val offsetDateTime = OffsetDateTime.parse(utcDateTime)
    val zonedDateTime = offsetDateTime.atZoneSameInstant(ZoneId.systemDefault())
    return zonedDateTime.toLocalDateTime()
}

class LifeCycleHandler(private val timer: Timer) : LifeCycleHandlerInterface {
    override val lazyCancellation by lazy {
        timer.cancel()
    }

    fun cancel() = lazyCancellation
    fun cancelInSeconds(seconds: Int) = runOnceInSeconds(seconds) { lazyCancellation }
    fun cancelInMinutes(minutes: Int) = runOnceInMinutes(minutes) { lazyCancellation }
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

fun <T> runAfterDelay(millis: Long, action: () -> T): T {
    if (!isSandBoxModeActive()) sleep(millis)
    return action()
}
