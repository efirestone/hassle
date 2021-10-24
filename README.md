
![GitHub Actions status](https://github.com/efirestone/hassle/workflows/Latest%20push/badge.svg)
![LINE](https://img.shields.io/badge/line--coverage-31%25-red.svg)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.codellyrandom.hassle/hassle/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.codellyrandom.hassle/hassle)

# Hassle

Hassle is a smart home automation library for [**Home Assistant**](https://www.home-assistant.io), written in Kotlin.
It allows your application or service to interact with Home Assistant, such as to observe state changes, listen to
events, send commands, and much more. Hassle establishes a background WebSocket connection to Home Assistant, and 
aims to be resilient to network connectivity issues when possible.

To connect to your Home Assistant server:

```kotlin
val credentials = Credentials(
    name = "Home Assistant",
    host = "homeassistant.myserver.com",
    port = 8123,
    accessToken = "access_token_obtained_from_home_assistant",
    isSecure = false // true for HTTPS/WSS
)

val client = homeAssistantApiClient(credentials)

client.connect()
```

Hassle provides predefined factory functions, data classes, and observers for all of the common entity types.

In the following example we'll observe the motion sensor in the living room, and when motion is detected we'll turn on
the living room light with a 5000K color temperature.

```kotlin
val client = homeAssistantApiClient(...)
client.connect()

val motionSensor = client.MotionSensor(
    EntityId.fromString(ObjectId("living_room_motion"))
)
val light = client.RGBWLight(ObjectId("living_room_main_light"))

motionSensor.onMotionAlarm { //this: MotionSensor
    light.setColorTemperature(5000.kelvin)
}
```

Hassle abstracts away the lower level service calls and state observation and encourages you to think in states
instead. This is less error-prone and hopefully easier to understand.

## Home Assistant
 
HA is an open-source home-automation platform written in Python 3 that puts local control and privacy first. Powered by
a worldwide community of tinkerers and DIY enthusiasts.

If you're not already familiar with Home Assistant, you'll find all you need on the
[Getting Started page](https://www.home-assistant.io/getting-started/).

## Installation

#### Home Assistant
Further information on this topic is available on the official
[Home Assistant Documentation](https://www.home-assistant.io/getting-started/) page.

#### Hassle
Hassle is available from Maven Central. Add the following lines to your `build.gradle.kts`:

```kotlin
repositories {
    // ...
    mavenCentral()
}
```
```kotlin
dependencies {
    // ...
    implementation("com.codellyrandom.hassle:hassle:${replace with a version}")
}
```

## Documentation

Hassle has no opinion on how you want to run your application, what other libraries or pattern you choose, or what
else is best for what you like to build. All Hassle needs is a Kotlin environment to run properly.

If you want to make modifications or enhancements to Hassle, the IntelliJ IDEA by JetBrains is your best bet. The
[Community Edition](http://www.jetbrains.com/idea/download/index.html) is free and should provide what you need.

## Q&A

**Q: Can I use Hassle to write my Home Assistant automations?**

A: You definitely could, although Hassle was really designed to let your Kotlin service or application interact with
Home Assistant. You could wrap your Hassle logic in a small program that spins the run loop in order to continue 
listening for events. You also might look at [Khome](https://github.com/dennisschroeder/khome), which was designed more 
directly for this purpose.

**Q: Why Kotlin?**

A: Kotlin's a great, modern, language. It's also compatible with other languages such as Objective-C (and by extension
Swift) and JavaScript via [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html).

**Q: So is Hassle compatible with Kotlin Multiplatform?**

A: Sort of. Hassle uses only multiplatform-compatible libraries, and therefore compiles for all platforms, but it 
doesn't actually work with iOS because Ktor, the networking library used by Hassle, [doesn't yet support WebSocket
connections](https://github.com/ktorio/ktor/issues/1894). Once that support is in place the aim is to get Hassle
working for iOS as well.

It's quite possible that Hassle works when transpiled to JavaScript, but this hasn't been tested.

### Working with Hassle
- [Quick start](docs/Quickstart.md)
- [Sensors, Actuators and Observers](docs/SensorsAndActuators.md)
- [Predefined entity types](docs/PredefinedEntityTypes.md) (to be finished)
- [Build entity types from scratch](docs/BuildEntitiesFromScratch.md) (coming soon)
- [Notifications API](docs/NotificationApi.md)
- [Safety's first](docs/SafetyFirst.md)


## Credits
Hassle was forked from [Khome](https://github.com/dennisschroeder/khome), and a huge thank you goes out to 
[dennisschroeder](https://github.com/dennisschroeder) and the contributors to that project.   
