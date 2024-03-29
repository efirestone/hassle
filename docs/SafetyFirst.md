# Safety's First

Hasseble was written with safety in mind, which in this case means using a fail-fast approach.
We detected three different application stages the application runs through. Every stage with a slightly higher level
of negativity in consequences, if you, the user, is not aware of any failure that might occur. The three stages are: 

## Stage I: Compile time

At first, we always try to fail at compile time. Remember: "The compiler is our friend!".
But not everything can be checked at compile time. Therefore we need to wait until Hassle gathers some more information.

## Stage II: Hassle starting

### Entity Registration Validation

[TODO: Not currently accurate]
As soon as Hassle is connected with your home assistant instance and successfully authenticated themself, it requests
all entity states that are configured in your home assistant instance, with their initial state.
But before we enrich all Hassle entities, it checks if the entity you created in Hassle also exists as a Home
Assistant entity in your home assistant instance. To achieve this, we simply check if the registered entity matches any
entity in the above-mentioned entity state request. If not, the Hassle start sequence gets aborted and your Hassle
application exits.

### State and Attribute casting

[TODO: Not currently accurate]
Only when the entity registration validation sequence is successful, Hassle enriches all entities with their initial
state. At this time, Hassle also tries to cast the information about the entities from the above-mentioned entity
state request to the state and attributes that were set as a type parameter in your Sensor or Actuator at design time.
If the mapper can't resolve, map and cast the property types, the Hassle initial state setting sequence gets aborted,
and your Hassle application exits.

## Stage III: Hassle observing

The most dangerous failure that can occur, happens in an observer function since those will be detected only (far) in the future. Think about an observer function that should get executed when a smoke detector sensor signals a detection. But it fails. Not good! So we encourage you to be careful in what you do in your observers. All we can do here to protect you is to serve two error handlers:

### Error response handler

Every time, the Home Assistant Websocket API responds with an error, Hassle's default error response handler will
print out a message like this:

```bash
[main] ERROR HomeAssistantApiClientImpl - CommandId: 5 -  errorCode: home_assistant_error Unable to find service covers/open_cover
```

If you wish to add your custom error handler, that e.g. sends a push notification to your smartphone, you can do so by
attaching a new ErrorResponseHandler to Hassle.

```kotlin
val client = homeAssistantApiClient(...)

client.setErrorResponseHandler { errorResponseData ->
    // Place your custom error response logic in here
    // You can use errorResponseData.commandId, errorResponseData.errorResponse.code and 
    // errorResponseData.errorResponse.message in your logic
}

// ... 

client.connect()
```

Inside your error response handler lambda function, you have a parameter with an ErrorResponseData data class, that
gives you some context information like the command id, the home assistant error code, and message.

### Observer exception handler

Every time an Exception gets thrown in an observer, Hassle's default observer exception handler prints out a message,
like this:

```bash
[main] ERROR HomeAssistantApiClientImpl - Caught exception in observer
java.lang.IllegalStateException: Ups there us a illegal state
	at HomeAssistantApiClientImplKt$main$onObserver$1.invoke(HomeAssistantApiClient.kt:54)
	at HomeAssistantApiClientKt$main$onObserver$1.invoke(HomeAssistantApiClient.kt)
	at com.codellyrandom.hassle.observability.ObserverImpl.update(Observer.kt:33)
	at com.codellyrandom.hassle.observability.ObservableHistoryNoInitialDelegate.setValue(HistoryObservation.kt:51)
	at com.codellyrandom.hassle.entities.devices.ActuatorImpl.setState(ActuatorImpl.kt)
	at com.codellyrandom.hassle.entities.devices.ActuatorImpl.trySetStateFromAny(ActuatorImpl.kt:58)
	at com.codellyrandom.hassle.entities.ActuatorStateUpdater.invoke(DeviceStateUpdater.kt:19)
	at com.codellyrandom.hassle.core.boot.EventResponseConsumer.handleStateChangedResponse(EventResponseConsumer.kt:77)
	at com.codellyrandom.hassle.core.boot.EventResponseConsumer.access$handleStateChangedResponse(EventResponseConsumer.kt:30)
	at ...
```
If the default exception handler does not fit your expectations, you can overwrite the default exception handler with your custom implementation.

```kotlin
val client = homeAssistantApiClient(...)
client.setObserverExceptionHandler { exception: Throwable ->
    // Place your custom exception handler logic in here
}
//...
client.runBlocking()
```
