# react-native-better-tus-client

A (better & up-to-date) tus client for react-native with background upload support

## Installation

```sh
npm i --save @cuvent/react-native-better-tus-client
```

## Setup:

Both on android and iOS the default concurrency mode is in-sequence, which means that one upload
will be processed after each other. This behavior can be changed on android.

### Android

To change the concurrency mode in android change the config from e.g. you MainApplication's `onCreate`:

```java
  @Override
  public void onCreate() {
    super.onCreate();
    BetterTusClientConfig.INSTANCE.getConfig().setConcurrencyMode(ConcurrencyMode.PARALLEL);
    // ...
  }
```

Two modes are available:

- `ConcurrencyMode.PARALLEL` executes all uploads in parallel. Good when you only process a few uploads.
- `ConcurrencyMode.SEQUENCE` (Default) executes one upload after another. Good when you have a lot of uploads
  and want to make sure that each upload gets processed without timeouts or similar.

#### Modify the auto-resume behavior / max uploads at a time in parallel mode

_These changes are only needed if you want to change the auto-resume and concurrency behavior of the uploading queue_

When you apply these changes the queue won't automatically resume on app start. You need to call
`resumeAll` to continue with the uploading of prior uploads.

Add the to your AndroidManifest under the `Application` tag. _Note_: You probably need to add the `tools`
namespace to your manifest definition:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.myapp"
    xmlns:tools="http://schemas.android.com/tools">
```

```xml
<provider
    android:name="androidx.work.impl.WorkManagerInitializer"
    android:authorities="${applicationId}.workmanager-init"
    tools:node="remove" />
```

> This enables on-demand initialization of the worker manager, which improves the app startup time + gives
> the possibility to configure the worker manager on our own.

In your Application class implement the `Configuration.Provider` interface, like so:

```java
import androidx.work.Configuration;
import java.util.concurrent.Executors;

class MainApplication extends Application implements Configuration.Provider {
  //...
  @NonNull
  @Override
  public Configuration getWorkManagerConfiguration() {
    return new Configuration.Builder()
                .setExecutor(Executors.newFixedThreadPool(2))       // you can set here the number of concurrent worker (= uploads per time)
                //.setExecutor(Executors.newSingleThreadExecutor()) // use this to have a sequential upload queue --> use concurrencyMode SEQUENCE for this!
                .build();
  }
}

```

## Usage

```js
// TODO
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
