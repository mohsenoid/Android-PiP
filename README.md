# Android-PiP
[![Maven Central](https://img.shields.io/maven-central/v/com.mohsenoid.pip/pip-core.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.mohsenoid.pip%22%20AND%20a:%22pip-core%22)
![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)

Android Picture-in-Picture feature helper library

This repository holds an Android Library that helps with the use of Picture-in-Picture mode.
It also includes a sample app that shows how to use the library.

![Sample Demo](/sample-demo.gif)

More to learn about Android PiP API:
https://youtu.be/bvCKd_XctNg

## Setup

Add the dependencies to your project:

```groovy
    // the core library
    implementation("com.mohsenoid.pip:pip-core:1.0.0")
    // the UI library including ViewGroups which are PiP aware
    implementation("com.mohsenoid.pip:pip-ui:1.0.0")
```

## Usage

First you need to initialize the library inside you application class:

```kotlin
    class App : Application() {
        override fun onCreate() {
            super.onCreate()
            Pip.init(this)
        }
    }
```

## PipSupportAppCompatActivity

By extending the `PipSupportAppCompatActivity` you can easily use the Picture-in-Picture mode.

```kotlin
    class PlayerActivity : PipSupportAppCompatActivity() {
    // ...
}
```

Make sure that your PlayerActivity is setup correctly for PiP mode:

```xml
    <activity android:name=".PlayerActivity"
        android:configChanges="screenSize|smallestScreenSize|screenLayout|orientation"
        android:launchMode="singleTask"
        android:supportsPictureInPicture="true" />
```

You can inform the PiP library about your player status changes so that it can update the PiP auto
enter accordingly:

```kotlin
    private fun setupPlayer() {
    // ...
    val exoPlayer = ExoPlayer.Builder(this).build().apply {
        // ...
        addListener(
            object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    updatePlayerState(isPlaying)
                }
            },
        )
    }
    // ...
}
```

You can set the Player rectangle so that the PiP library can use it for a smooth animation when
entering/exiting PiP mode:

```kotlin
    val rect = Rect()
    binding.player.getGlobalVisibleRect(rect)
    updatePipRect(rect)
```

You can pass actions to the PiP library so that it can show them in the PiP window:

```kotlin
    updatePipActions(listOfNotNull(getPipAction()))
```

```kotlin
    private fun getPipAction(): RemoteAction? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val actionUri = Uri.parse("https://youtube.com/AndroidDeveloperTips")
            val actionIntent = Intent(Intent.ACTION_VIEW, actionUri)
            val actionPendingIntent =
                PendingIntent.getActivity(this, 0, actionIntent, PendingIntent.FLAG_IMMUTABLE)
            val remoteAction = RemoteAction(
                Icon.createWithResource(this, R.drawable.ic_picture_in_picture_action),
                "More info",
                "More info action",
                actionPendingIntent,
            )
            remoteAction
        } else {
            null
        }
    }
```

## PipController

By getting this controller interface you can control the PiP mode:

```kotlin
    private val pipController = Pip.mgetPipController()
```

Use it to enter PiP mode if allowed:

```kotlin
    pipCommander.enterPip(
        { alreadyInPipMode ->
            // switching to PiP was successful
        },
        { pipEnterError ->
            // switching to PiP was not successful
        },
    )
```

Or to exit PiP mode:

```kotlin
    pipCommander.closePip()
```

You may also pass a content ID to the `updatePlayerState` method and use that to close the correct
PiP window:

```kotlin
    updatePlayerState(isPlaying, contentId)

// ...

pipCommander.closePip(contentId)
```

## PipObservable

By getting this observable interface you can observe the PiP mode change:
```kotlin
    private val pipObservable = Pip.getPipObservable()
```

You can check if the PiP mode is allowed based on player state and device for instance to enabling/disabling PiP mode button:
```kotlin
    lifecycleScope.launch {
        pipObservable.isPipAllowedStateFlow.collectLatest {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                binding.button.isVisible = it
            }
        }
    }
```

Or indicate of the player is in PiP mode or not:
```kotlin
    lifecycleScope.launch {
        pipObservable.isInPipModeStateFlow.collectLatest { isInPipMode ->
            binding.player.useController = !isInPipMode
        }
    }
```

Or check if a content is being played in PiP mode:
```kotlin
    val result = isInPipMode(contentId)
```

## Pip aware ViewGroups

By adding the pip-ui library to your project you can use the `PipAwareFrameLayout` and `PipAwareConstraintLayout` in your layout files and show/hide views which should not be visible in the PiP mode:

```xml
    <com.mohsenoid.pip.ui.PipAwareFrameLayout
        android:id="@+id/titleContainer"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintTop_toBottomOf="@id/player">
    
        <TextView
            android:id="@+id/title"
            style="@style/TextAppearance.MaterialComponents.Headline4"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:gravity="center"
            android:padding="8dp"
            tools:text="Video Title" />
    
    </com.mohsenoid.pip.ui.PipAwareFrameLayout>
```

Just make sure to init the views once the layout is inflated:

```kotlin
    override fun onCreate(savedInstanceState: Bundle?) {
        // inflate the layout
        // ...
        binding.titleContainer.init()
    }
```

## License

Copyright 2023 Mohsen Mirhoseini

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
