# Khome

Khome is a smart home-automation library for **Home Assistant**, written in Kotlin. It makes heavy usage of the **Kotlin-DSL** 
for a good programming experience. This library let's you write your own application, that can listen to (state change) events 
and fires actions via the [Home Assistant Websocket API](https://developers.home-assistant.io/docs/en/external_api_websocket.html).
Or you can call any other third party code or API.

The heart of Khome is the [Ktor-Websocket-Client](https://ktor.io/clients/websockets.html). Khome uses ktor for the communication 
between your application and your Home-Assistant Server.

Example:
```kotlin
listenState("sensor.livingroom_luminance") {
    constrain {
        newState.get<Double>() ?: 0.0 <= 3.0
    }

    action {
        callService {
            light {
                entityId = "light.livingroom_main"
                service = "turn_on"
            }
        }
    }
}
```

Khome is influenced by AppDeamon. AppDaemon is a loosely coupled, multithreaded, sandboxed, pluggable 
python execution environment for writing automation apps for Home-Assistant-home-automation software.

[AppDeamon@github](https://github.com/home-assistant/appdaemon) | [AppDeamon Documentation](https://appdaemon.readthedocs.io/en/latest/)

## Home Assistant

HA is an open-source home-automation platform written in Python 3 that puts local control and privacy first. Powered by 
a worldwide community of tinkerers and DIY enthusiasts. Perfect to run on a Raspberry Pi or a local server.

If you're not already familiar with Home Assistant, you find all you need on the [Getting Started page](https://www.home-assistant.io/getting-started/).

## Warning
This project is in early alpha state. You can't rely on it **yet**. But I encourage everybody to test it and report [issues](https://github.com/dennisschroeder/khome/issues).
Changes in the API, removal of features or other changes will occur.

## If you are from...

#### ... the Kotlin World:
Since you Home Assistant is written in Python 3, you may ask yourself if you need to write Python code on the Home Assistant
side. But you don't have to. All you need to do is configuring it via `.yaml` files. But you need to install and run it on 
your own server. There is plenty of information and tutorials on the web to support you with that. [Google](https://google.com)
will help you. Also there is a [Discord channel](https://discordapp.com/invite/c5DvZ4e) to get in touch easily with the community.

#### ... the Python World:
Yes you need to learn Kotlin. It is definitely worth a try. In my personal opinion it is worth even more. But that's a different story.
Probably the fastest way for you to get into Kotlin is the [Kotlin for Python Introduction](https://kotlinlang.org/docs/tutorials/kotlin-for-py/introduction.html)
from the official Kotlin documentation. Here is a list of the most important [Kotlin online resources](https://kotlinlang.org/community/#kotlin-online-resources).

## Installation

#### Home Assistant
Further information on this topic is available on the official [Home Assistant Documentation](https://www.home-assistant.io/getting-started/) page.

#### Khome
For now you can use [Jitpack](http://jitpack.io) to install Khome locally. Just add the following lines to your `build.gradle` or maven file.
Since there is no official release yet, use `master-SNAPSHOT` as version. After the first release, exchange `master-SNAPSHOT` with 
the release tag.

#### Gradle
```groovy
repositories {
    // ...
    maven { url "https://jitpack.io" }
}
```
```groovy
dependencies {
    // ...
    implementation 'com.github.dennisschroeder:khome:master-SNAPSHOT'
}
```

#### Maven
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
```xml
<dependency>
        <groupId>com.github.dennisschroeder</groupId>
        <artifactId>khome</artifactId>
        <version>master-SNAPSHOT</version>
</dependency>

```

## Documentation

Khome has no opinion on how you want to run your application, what other libraries or pattern you choose or what else  is best for what you like to build.
All Khome needs is an Kotlin environment to run properly. All dependencies comes with it.

Again, if you are new to Kotlin, you might check out [Getting Started with Intellij IDEA](https://kotlinlang.org/docs/tutorials/getting-started.html)
or [Working with the Command Line Compiler](https://kotlinlang.org/docs/tutorials/command-line.html).
I recommend using Kotlin with Intellij IDEA to get started. It's the best way to get into it. You can download the free [Community Edition](http://www.jetbrains.com/idea/download/index.html) from JetBrains.


