# Sensors, Actuators and Observers

Every entity in a smart home system can either be a sensor or an actuator.
The main difference is the mutability of the state in an actuator. Or the immutability in a Sensor.

Observers, on the other hand, get attached to both and execute a user-defined action, whenever a state change occurs.

## Sensor
A Sensor in Hassle consists of the **observable state** and **attributes**. Since sensors usually measure something,
the state is called "measurement" in Hassle.

A sensor fulfills 2 purposes:

1. Observes its state changes and executes a user-defined action attached to it

```kotlin
val client = homeAssistantApiClient(...)

val bedRoomTemperature = client.TemperatureSensor(ObjectId("bedroom_temperature"))

bedRoomTemperature.attachObserver { snapshot, _ ->
    // gets executed every time a state change occurs
    // attribute change also triggers a state change
}

client.connect()
```

2. Source of information

```kotlin
val client = homeAssistantApiClient(...)
client.connect()

val bedRoomTemperature = client.TemperatureSensor(ObjectId("bedroom_temperature"))

if (bedRoomTemperature.measurement.value > 30.0) {
    // ...
}
```
See more about Sensors in the [source code](../src/commonMain/kotlin/com/codellyrandom/hassle/entities/devices/Sensor.kt)
or [kdocs](https://efirestone.github.io/hassle/hassle/com.codellyrandom.hassle.entities.devices/-sensor/index.html).

## Actuator

An Actuator in Hassle consists of the **state**, the **attributes**, an **observable** property and a service command resolver.
In an Actuator, the observable property is the one who holds the state. Since Actuators can mutate their state, not directly
in your application, but over home assistant, we need to differentiate between the actual state and the desired state.

Most home automation libraries require you to call a service to mutate the state of an entity.
This can be quite cumbersome and error-prone since it requires you to resolve the matching service
name and parameters, that does the desired mutation for you, every time it is needed.

Hassle, therefore, lets you stay in the mindset of states.

An actuator fulfills 3 purposes:

1. Observes its state changes and executes a user-defined action attached to it

```kotlin
val client = homeAssistantApiClient(...)
client.connect()

val bedRoomCoverOne = client.PositionableCover(ObjectId("bedroom_cover_1"))

bedRoomCoverOne.attachObserver {
    // gets executed every time a state change occurs
    // attribute change also triggers a state change
}
```

2. Source of information

```kotlin
val client = homeAssistantApiClient(...)
client.connect()

val bedRoomCoverOne = client.PositionableCover(ObjectId("bedroom_cover_1"))

if (bedRoomCoverOne.state.position > 30) {
    // ...
}
```

3. Mutate state

```kotlin
val client = homeAssistantApiClient(...)
client.connect()

val sun = client.Sun()
val bedRoomCoverOne = client.PositionableCover(ObjectId("bedroom_cover_1"))

sun.attachObserver { //this:Sensor<SunState,SunAttributes>
    if (measurement.value == ABOVE_HORIZON) {
        bedRoomCoverOne.setDesiredState(PositionalCoverSettableState(value = OPEN, currentPosition = 50))
    }
}
```

Under the hood, home assistant still offers a [service based API](https://developers.home-assistant.io/docs/api/websocket/#calling-a-service).
Therefore, Hassle resolves the matching service call from the desired state.
To learn more about this, read the [Service-Command-Resolver](./SensorsAndActuators.md#service-command-resolver) Section.

We are aware of the fact, that there are reasons to call a service instead of setting a desired state. We detected two reasons. First, some of you might feel more

comfortable calling services (still we encourage you to at least try out the desired state version). Secondly, home assistant offers some functionality that does affect the entity without
setting the state or attribute value directly. And some services do not affect the entity at all.

Therefore, an Actuator has the [`::callService`](https://efirestone.github.io/hassle/hassle/com.codellyrandom.hassle.entities.devices/-actuator/call-service.html) method.

The following example showcases this. We will build a cover lock. When the lock is active, every time the cover is changing its position, we call the [`stop_cover`](https://www.home-assistant.io/integrations/cover/)
service from the cover domain in home assistant, to prevent opening/closing.

```kotlin
val client = homeAssistantApiClient(...)
client.connect()

val coverLock = client.InputBoolean(ObjectId("cover_lock"))
val bedRoomCoverOne = client.PositionableCover(ObjectId("bedroom_cover_1"))

bedRoomCoverOne.attachObserver { // this: Actuator<CoverState,PositionalCoverAttributes>
    if (attributes.working == YES && coverLock.state.value == ON) {
        bedRoomCoverOne.callService(Service("stop_cover"))
    }
}
```

See more about Actuators in the [source code](../src/commonMain/kotlin/com/codellyrandom/hassle/entities/devices/Actuator.kt) or [kdocs](https://efirestone.github.io/hassle/hassle/com.codellyrandom.hassle.entities.devices/-actuator/index.html).


### Service command resolver
Since Home Assistant is awaiting a [service call](https://developers.home-assistant.io/docs/api/websocket/#calling-a-service),
and we only want to think in states, somebody needs to translate between those different concepts. It's the
responsibility of the service command resolver. Basically it is a just a
[factory function](../src/commonMain/kotlin/com/codellyrandom/hassle/communicating/ServiceCommandResolver.kt), that you pass a lambda
which has access to the desired state and returns a [ResolvedServiceCommand](../src/commonMain/kotlin/com/codellyrandom/hassle/communicating/ServiceCommandResolver.kt) instance.

Let's take a look at a simple example from the InputBoolean entity:

```kotlin
ServiceCommandResolver { desiredState ->
    when (desiredState.value) {
            SwitchableValue.ON -> DefaultResolvedServiceCommand(
                service = Service("turn_on"),
                serviceData = EntityIdOnlyServiceData()
            )
            SwitchableValue.OFF -> DefaultResolvedServiceCommand(
                service = Service("turn_off"),
                serviceData = EntityIdOnlyServiceData()
            )
        }
}
```

Ok, let's have a deeper look at all the elements involved:

#### state
The desired state is the same type as the actual state in an actuator.
In this example, its state type is `SwitchableState` and the
state value is an enum [SwitchableValue](../src/commonMain/kotlin/com/codellyrandom/hassle/extending/StateValueTypes.kt) which has two
options: ON and OFF.

```kotlin
data class SwitchableState(
    override val value: SwitchableValue
) : State<SwitchableValue>
```

#### DefaultResolvedServiceCommand
Last, but not least, the output of our `ServiceCommandResolver` is a [`DefaultResolvedServiceCommand`](../src/commonMain/kotlin/com/codellyrandom/hassle/communicating/ServiceCommandResolver.kt).
```kotlin
data class DefaultResolvedServiceCommand(
    override val service: Service,
    override val serviceData: CommandDataWithEntityId
) : ResolvedServiceCommand
```
A class, that later gets mapped to a ServiceCommand which then gets serialized and send to home assistant. Therefore, the `DefaultResolvedServiceCommand` has to answer two questions:

1. What `service` should be called?
2. What `serviceData` (parameters) should be attached to the call?

In our example, we need two different services `TURN_ON` and `TURN_OFF` and as a parameter, we need to tell home assistant which entity to be turned on/off.
This can be achieved using `EntityIdOnlyServiceData`, which is part of Hassle's toolbox and should be used when no other parameters have to be sent to home assistant.
Hassle will attach the entity id to the service data for you. When you want to build your own serviceData class, make sure to use the `abstract DesiredServiceData` class.

A more advanced example from the [DimmableLight](../src/commonMain/kotlin/com/codellyrandom/hassle/extending/actuators/Light.kt) entity might also help to better understand the purpose of the
`ServiceCommandResolver`:

```kotlin
ServiceCommandResolver { desiredState ->
    when (desiredState.value) {
        SwitchableValue.OFF -> {
            desiredState.brightness?.let { brightness ->
                DefaultResolvedServiceCommand(
                    service = "turn_on".service,
                    serviceData = DimmableLightServiceData(brightness)
                )
            } ?: DefaultResolvedServiceCommand(
                service = "turn_off".service,
                serviceData = EntityIdOnlyServiceData()
            )
        }
        SwitchableValue.ON -> {
            desiredState.brightness?.let { brightness ->
                DefaultResolvedServiceCommand(
                    service = "turn_on".service,
                    serviceData = DimmableLightServiceData(brightness)
                )
            } ?: DefaultResolvedServiceCommand(
                service = "turn_on".service,
                serviceData = EntityIdOnlyServiceData()
            )
        }
    }
}
```

## Observer

The heart of Hassle is the ability to observe state changes. The Sensor, as already mentioned above, has an observable property named `state`.
The Actuators pendant gets called `state`.

To execute an action every time a state has changed, you can create and attach an Observer to the entity you like to observe.

```kotlin
SomeCover.attachObserver { // this: Actuator<CoverState,PositionalCoverAttributes>
 //...
}
```

An Observer is bound to a specific type of Sensor or Actuator since Hassle injects a reference to it as a [receiver of the observer function literal](https://kotlinlang.org/docs/reference/lambdas.html#function-literals-with-receiver).
Inside the body of the function literal, the receiver object passed to a call becomes an implicit this, so that you can access the members of that receiver object without any additional qualifiers,
or access the receiver object using a this expression.

This behavior is similar to [extension functions](https://kotlinlang.org/docs/reference/extensions.html#extension-functions), which also allow you to access the members of the receiver object inside the body of the function.

With the Sensor or Actuator as an implicit this you get access to the current state and the history of its state. The history stores the youngest 10 states,
where index 0 is the current and index 10 would be the 10th youngest entry in the history.

### Constraints
In most use cases, you don't want your action to be executed every single time a state change occurs. Depending on the entity type, this can happen pretty often.
A dimmable light, for example, has a couple of state changes when turning on due to the power consumption attribute that decreases till the light is on at the
desired brightness level.

**Remember**:
***
The attributes are also a part of the entity state.
Therefore, a state change event gets emitted by home assistant also when only
an attribute gets updated.
***

To avoid such false executions, you have to specify a constraint
using the current state and the history data.

Let's take a look at some examples to show you the idea behind snapshot data and constraints:

```kotlin
InputBoolean.attachObserver { //this: Actuator<InputBooleanState,SwitchableSettableState>
    if (state.value == SwitchableValue.ON) {
        //...
    }
}
```

```kotlin
DimmableLight.attachObserver { //this: Actuator<DimmableLightState,DimmableLightSettableState>
    if (
            history[1].state.value == SwitchableValue.OFF &&
            state.value == SwitchableValue.ON
        ) { /*..*/ }
}
```

```kotlin
PositionalCover.attachObserver { //this: Actuator<PositionableCoverState,PositionableCoverSettableState>
    if (snapshot.working) {
        //..
    }
}
```
Another practical usage of the history snapshot is to set an entity state to a former value taken from the history.

 ```kotlin
TvTime.attachObserver { //this: Actuator<InputBooleanState,SwitchableSettableState>
     if (TvTime.state.value == SwitchableValue.OFF) {
         LivingRoomCover.setDesiredState(LivingRoomCover.history[1].state)
     }
 }
 ```
**Here is some context to the example, for clarification:**

We have an input boolean in home assistant, that serves as a switch to turn on/off a scene called tv time.
When we activate it, the cover in the living room gets closed, some nice mood light, and the tv turned on, etc.
And in our example, we observe this input boolean and when it gets deactivated, we roll back the living room cover
to its position that was before, which was stored in the history of the living room cover.

### Take over control

Inside the observer, we passed you a reference to itself as a [Switchable](https://efirestone.github.io/hassle/hassle/com.codellyrandom.hassle.observability/-switchable/index.html) which gives you the ability to enable/disable the observer action.
Since it is the only parameter available in your function literal, it is available as "it", or you can name it whatever you like.

Let's take a look at an example:

```kotlin
InputBoolean.attacheObserver { observer ->
    //... run some logic that should only be executed once
    observer.disable()
}
```

Besides using the Switchable inside the observer, you can get a reference to the attached observer from the `::attachObserver` method like
we see in this example:

```kotlin
val luminanceSwitchable: Switchable = LuminanceSensor.attachObserver {
    if (state.value < 5.0) {
        Light.setDesiredState(SwitchableState(ON))
    }
}

InputBoolean.attacheObserver {
    when(state.value) {
        ON -> luminanceSwitchable.enable()
        OFF -> luminanceSwitchable.disable()
    }
}
```
