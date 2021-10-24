# Predefined components

For your convenience, Hassle comes with a lot of predefined factory functions, data classes, observers and more for
generic entity types.

## Helpers

Helpers in home assistant are part of the [automation integrations](https://www.home-assistant.io/integrations/#automation).
Some of those are represented as entities, therefore you can use them in Hassle.

### Input Boolean

The input_boolean integration allows the user to define boolean values that can be controlled via the frontend and can be 
used within conditions of automation. See [home assistant documentation](https://www.home-assistant.io/integrations/input_boolean/) for more.

| Element    | Name | Sourcecode | KDocs |
|------------|------|------------|-------|
| Factory    | HomeAssistantApiClient::InputBoolean   |  [Link](../src/commonMain/kotlin/com/codellyrandom/hassle/extending/entities/actuators/inputs/InputBoolean.kt) | [Link](https://efirestone.github.io/hassle/hassle/com.codellyrandom.hassle.extending.actuators/-input-boolean.html)      |
| State      | SwitchableState     |  [Link](../src/commonMain/kotlin/com/codellyrandom/hassle/extending/entities/SwitchableEntityComponents.kt) | [Link](https://efirestone.github.io/hassle/hassle/com.codellyrandom.hassle.extending/-switchable-state/index.html)      |
| StateValue | SwitchableStateValue | [Link](../src/commonMain/kotlin/com/codellyrandom/hassle/extending/entities/SwitchableEntityComponents.kt) | [Link](https://efirestone.github.io/hassle/hassle/com.codellyrandom.hassle.extending/-switchable-value/index.html)      |
| Attributes | InputBooleanAttributes    |  [Link](../src/commonMain/kotlin/com/codellyrandom/hassle/extending/entities/actuators/inputs/InputBoolean.kt) | [Link](https://efirestone.github.io/hassle/hassle/com.codellyrandom.hassle.extending/-input-boolean-attributes/index.html)      |

Example:

```kotlin
val client = homeAssistantApiClient(...)
val sleepMode: InputBoolean = client.InputBoolean(ObjectId("sleep_mode"))

sleepMode.attachObserver { //this:Actuator<SwitchableState,InputBooleanAttributes>
    if (actualState.value = SwitchableValue.ON) {
        //... turn off lights, close covers, activate the alarm, etc.
    }
}
```

#### Properties

##### isOn

```kotlin 
val Actuator<SwitchableState, *>.isOn
```

Is true when the actual state value is equal to `SwitchableStateValue.ON`.

##### isOff

```kotlin 
val Actuator<SwitchableState, *>.isOff
```

Is true when the actual state value is equal to `SwitchableStateValue.OFF`.

#### Methods

##### turnOn()

```kotlin
fun <A : Attributes> Actuator<SwitchableState, A>.turnOn() 
```

Since in Home Assistant InputBoolean states are ON and OFF, you can "turn on" the InputBoolean-Helper by calling this method resulting in
a state change.

##### turnOff()

```kotlin
fun <A : Attributes> Actuator<SwitchableState, A>.turnOff() 
```

Since in Home Assistant InputBoolean states are ON and OFF, you can "turn off" the InputBoolean-Helper by calling this method.

##### onTurnedOn {...}

```kotlin 
inline fun <A : Attributes> Actuator<SwitchableState, A>.onTurnedOn(
    crossinline f: Actuator<SwitchableState, A>.(Switchable) -> Unit
)
```

The passed lambda gets executed when the state of the InputBoolean entity switches from OFF to ON.
Inside the lambda you have access to all properties and members of your entity. 

##### onTurnedOff {...}

```kotlin 
inline fun <A : Attributes> Actuator<SwitchableState, A>.onTurnedOff(
    crossinline f: Actuator<SwitchableState, A>.(Switchable) -> Unit
)
```

The passed lambda gets executed when the state of the InputBoolean entity switches from ON to OFF.
Inside the lambda you have access to all properties and members of your entity. 

### Input Number

The input_number integration allows the user to define values that can be controlled via the frontend and can be used within conditions of automation. 
The frontend can display a slider, or a numeric input box. Changes to the slider or numeric input box generate state events. 
These state events can be utilized as automation triggers as well. See [home assistant documentation](https://www.home-assistant.io/integrations/input_number/) for more.

| Element    | Name | Sourcecode | KDocs |
|------------|------|------------|-------|
| Factory    | HomeAssistantApiClient::InputNumber   |  [Link](../src/commonMain/kotlin/com/codellyrandom/hassle/extending/entities/actuators/inputs/InputNumber.kt) | [Link](https://efirestone.github.io/hassle/hassle/com.codellyrandom.hassle.extending.actuators/-input-number.html)      |
| State      | InputNumberState     |  [Link](../src/commonMain/kotlin/com/codellyrandom/hassle/extending/entities/SwitchableEntityComponents.kt) | [Link](https://efirestone.github.io/hassle/hassle/com.codellyrandom.hassle.extending/-input-number-state/index.html)      |
| StateValue | Double | - | -      |
| Attributes | InputNumberAttributes    |  [Link](../src/commonMain/kotlin/com/codellyrandom/hassle/extending/entities/actuators/inputs/InputNumber.kt) | [Link](https://efirestone.github.io/hassle/hassle/com.codellyrandom.hassle.extending/-input-number-attributes/index.html)      |

Example:

```kotlin
val client = homeAssistantApiClient(...)
val maxVolume: InputNumber = client.InputNumber(ObjectId("max_volume"))
val googleHomeKitchen: MediaReceiver = client.MediaReceiver(ObjectId("google_home_kitchen"))

googleHomeKitchen.attachObserver { //this:Actuator<InputNumberState, InputNumberAttributes>
    if (actualState.value <= 10.0) {
        //... turn off lights, close covers, activate the alarm, etc.
    }
}
```

### Input Text

The input_text integration allows the user to define values that can be controlled via the frontend and can be used within conditions of automation.
Changes to the value stored in the text box generate state events. These state events can be utilized as automation triggers as well.
It can also be configured in password mode (obscured text). See [home assistant documentation](https://www.home-assistant.io/integrations/input_text/) for more.

...<br>
...<br>
...<br>

### Input Select

The input_select integration allows the user to define a list of values that can be selected via the frontend and can be used within conditions of automation.
When a user selects a new item, a state transition event gets generated. This state event can be used in an automation trigger. See [home assistant documentation](https://www.home-assistant.io/integrations/input_select/) for more.

...<br>
...<br>
...<br>

### Input Datetime

The input_datetime integration allows the user to define date and time values that can be controlled via the frontend and can be used within automations and templates.
See [home assistant documentation](https://www.home-assistant.io/integrations/input_datetime/) for more.

#### InputTime

#### InputDate

#### InputDateTime

## Lights

...coming soon!

## Cover

...coming soon!

## ...
