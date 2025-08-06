# IntelliJ Gradle Utilities Plugin

<img src="src/main/resources/META-INF/pluginIcon.svg" alt="Plugin Logo" width="120px"/>

This IntelliJ plugin provides some useful utilities to support the daily work with Gradle. 

[**It's available on the official IntelliJ plugin marketplace**](https://plugins.jetbrains.com/plugin/16800-gradle-utilities).

(This plugin has no relation to the official Gradle project.)

## Utilities Actions

The utilities are available in the toolbar of the Gradle tool window (select *View | Tool Windows | Gradle*) under the *Gradle Utilities* action popup button:

<img src="screenshots/main-menu.png" alt="Main Menu" width="499px"/>

### Manage Running Gradle Daemons

Lists all running Gradle daemons with their status. Additionally, actions to terminate daemons or viewing their command lines are available.

<img src="screenshots/manage-gradle-daemons.png" alt="Manage Running Gradle Daemons" width="500px"/>

### Manage Gradle Home

#### Clear Gradle Caches

Clears all Gradle caches. This includes all downloaded dependencies and Gradle daemon caches.

#### Manage Gradle Wrapper Distributions

Lists all downloaded Gradle wrapper distributions and their sizes on the disk and offers an action to delete them.

<img src="screenshots/manage-distributions.png" alt="Manage Gradle Wrapper Distributions" width="400px"/>

#### Manage Gradle Daemons Caches

Lists all Gradle daemon caches and their sizes on the disk and offers an action to delete them.

<img src="screenshots/manage-daemons-caches.png" alt="Manage Gradle Daemons Caches" width="400px"/>

### Collect Gradle Environment Information

Collects information about the current Gradle environment, like relevant paths, versions, user/project Gradle properties, and environment variables.

<img src="screenshots/gradle-environment-information.png" alt="Collect Gradle Environment Information" width="650px"/>

### Verification

Verifies the integrity of the downloaded Gradle Wrapper JAR and distributions by comparing their checksums with the official ones.

<img src="screenshots/verification.png" alt="Gradle Wrapper JAR and Distributions Verification" width="700px"/>


### Find the Latest Gradle Releases

Finds the latest Gradle releases (productive and pre releases) and provides useful links, like the release notes.

<img src="screenshots/latest-gradle-releases.png" alt="Find the Latest Gradle Releases" width="720px"/>

### Other

* Edit the user Gradle properties file.
* Bookmarks to important Gradle related websites.

## Development

This plugin is not seen as a library, this means that code changes do not necessarily adhere to the semantics version rules.

If you want to contribute something, please follow the code style in the `.editorconfig` and sign your commits.
