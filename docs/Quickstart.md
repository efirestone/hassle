### Quick start

#### Initialization & Configuration

Before you can start using Hassle in your application, you need to initialize and configure it:

```kotlin
val client = homeAssistantApiClient(
    Credentials(
        name = "Home Assistant",
        host = "localhost",
        port = 8123,
        accessToken = "Your super secret token",
        secure = false
    )
)
```

##### Required Parameters

- **host**: String <br> 
    Local ip address or url from your Home-Assistant server instance

- **port**: Int <br> 
    The port of Home-Assistant (defaults to 8123)

- **accessToken**: String <br> 
    You need to create a [long-lived access token](https://developers.home-assistant.io/docs/en/auth_api.html#long-lived-access-token).
    You can do so within the Lovelace UI. Just go to your user profile, scroll to the bottom, and generate one.

- **secure**: Boolean <br> 
    If you want to establish a secure WebSocket connection, you need to set this parameter to true (defaults to false).

#### Connect to the web socket API

```kotlin
val client = homeAssistantApiClient(...)
        
client.connect()
```

By calling the `HomeAssistantApiClient::connect` method, you establish a connection to the Home-Assistant WebSocket API
and run the start sequences like authentication, entity registration validation, and so on.
When all went as supposed, you should see the following output in your console. 

```bash
[main] INFO Authenticator - Authentication required!
[main] INFO Authenticator - Sending authentication message.
[main] INFO Authenticator - Authenticated successfully to homeassistant version 0.111.0
[main] INFO ServiceStoreInitializer - Requested registered homeassistant services
[main] INFO ServiceStoreInitializer - Stored homeassistant services in local service store
[main] INFO EntityStateInitializer - Requested initial entity states
[main] INFO EntityRegistrationValidation - Entity registration validation succeeded
[main] INFO StateChangeEventSubscriber - Successfully started listening to state changes
```

## Start writing your home automation application

Basically, a Hassle client is a collection of observers attached to some entities. For your convenience, Hassle
comes with a lot of predefined entity types. For most uses cases, [here is all you need](PredefinedEntityTypes.md) to
build your application. Since Home Assistant evolves rapidly and has the ability to be extended with custom integrations,
it comes along with a low-level API to build your own entities, based on your needs. You find more on that topic in the
[Build your own entities](BuildEntitiesFromScratch.md) section.

The following examples to get you off and running are based on the [predefined entity types](PredefinedEntityTypes.md)
and the [notification API](NotificationApi.md) provided by Hassle.
For a deeper understanding of Hassle's capabilities, we encourage you to read the
[Sensors, Actuators, and Observer](SensorsAndActuators.md) section.

### Lower complexity

1. Turn on a light, when a motion sensor detects movement, and the sun is below horizon.

```kotlin
val client = homeAssistantApiClient(...)

val hallwayLight = client.DimmableLight(ObjectId("hallway_main"))
val hallwayMotionSensor = client.MotionSensor(ObjectId("hallway"))
val sun = client.Sun()

hallwayMotionSensor.attachObserver {
    if (Sun.measurement.value == SunValue.BELOW_HORIZON) {
        when(measurement.value) {
            ON -> HallwayLight.turnOn()
            OFF -> HallwayLight.turnOff()
        }
    }
}

client.connect()
```

2. Iterate over a list of covers and set them to a specific position when the sun has risen.

```kotlin
val client = homeAssistantApiClient(...)
val sun = client.Sun()
val bedRoomCovers = listOf(
    client.PositionableCover(ObjectId("bedroom_one")),
    client.PositionableCover(ObjectId("bedroom_two")),
    client.PositionableCover(ObjectId("bedroom_three")),
    client.PositionableCover(ObjectId("bedroom_four")),
)

sun.onSunrise {
    for (cover in BedRoomCovers) {
        cover.setDesiredState(PositionalCoverSettableState(value = CoverValue.OPEN, position = 60))
    }
}

client.connect()
```

### Intermediate complexity

1. Send a notification to your mobile app when door sensor reports "door open" at night.

```kotlin
val client = homeAssistantApiClient(...)

val gardenShedDoor = client.ContactSensor(ObjectId("garden_shed"))
val lateNight = client.DayTime(ObjectId("late_night"))

enum class MobilePhone {
    MY_PHONE
}

gardenShedDoor.attachObserver {
    if (lateNight.measurement.value == SwitchableValue.ON &&
        history[1].state.value == ContactValue.CLOSED &&
        measurement.value == ContactValue.OPEN
    ) {
        client.notifyMobileApp(MobilePhone.MY_PHONE) {
            title = "INTRUDER ALARM"
            message = "Garden shed door opened"
            data {
                sound(critical = 1, volume = 1.0)
            }
        }
    }
}

client.connect()
```

### Higher complexity

1. When the Television got turned on, the livin groom covers get set to a specific position to comfortably watch some tv.
The previous positions get stored in a state store and when the tv got turned off, the covers get reset to the former positions.

```kotlin
val client = homeAssistantApiClient(...)

val televisionLivingRoom = client.Television(ObjectId("tv_livingroom"))
val resetStateHistory = mutableMapOf<Cover,CoverState>()

val televisionWatchingCoverPosition = 
    client.InputNumber(ObjectId("television_watching_cover_position")).state.toInt()
val defaultCoverPosition = 75

val livingRoomCovers = listOf(
    client.PositionableCover(ObjectId("livingroom_one")),
    client.PositionableCover(ObjectId("livingroom_two")),
    client.PositionableCover(ObjectId("livingroom_three"))
)

televisionLivingroom.attachObserver {
    if (turnedOn) {
        for (cover in livingRoomCovers) {
            resetStateHistory[cover] = cover.state
            cover.setCoverPosition(televisionWatchingCoverPosition)
        }
    }

    if (turnedOff) {
        for (cover in livingRoomCovers) {
            cover.setDesiredState(resetStateHistory[cover] ?: CoverState(CoverValue.OPEN, defaultCoverPosition))
        }
    }
}

client.connect()
```
